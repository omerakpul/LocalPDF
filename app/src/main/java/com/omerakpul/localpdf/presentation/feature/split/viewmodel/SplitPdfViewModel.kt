package com.omerakpul.localpdf.presentation.feature.split.viewmodel

import com.omerakpul.localpdf.domain.util.PageRanges
import kotlinx.coroutines.CancellationException
import com.omerakpul.localpdf.R
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import com.omerakpul.localpdf.data.service.FileInfoProvider
import androidx.lifecycle.viewModelScope
import com.omerakpul.localpdf.data.service.PdfService
import com.omerakpul.localpdf.domain.model.Pdf
import com.omerakpul.localpdf.domain.model.PdfSourceType
import com.omerakpul.localpdf.domain.repository.PdfRepository
import com.omerakpul.localpdf.presentation.feature.split.ui.SplitMode
import com.omerakpul.localpdf.presentation.feature.split.ui.SplitPdfUiState
import com.omerakpul.localpdf.domain.util.MemoryUtil
import com.tom_roush.pdfbox.pdmodel.PDDocument
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SplitPdfViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pdfService: PdfService,
    private val pdfRepository: PdfRepository,
    private val memoryUtil: MemoryUtil,
    private val fileInfo: FileInfoProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplitPdfUiState())
    val uiState: StateFlow<SplitPdfUiState> = _uiState.asStateFlow()

    fun selectPdf(uri: Uri) {
        viewModelScope.launch {
            val pdfInfo = getPdfInfo(uri)
            val fileSize = pdfInfo.third
            val maxSize = memoryUtil.getMaxAllowedFileSizeBytes()

            if (fileSize > maxSize) {
                _uiState.update { current ->
                    current.copy(
                        error = context.getString(R.string.error_file_too_large, memoryUtil.formatSize(fileSize), memoryUtil.formatSize(maxSize))
                    )
                }
                return@launch
            }

            _uiState.update { current ->
                current.copy(
                    selectedPdf = uri,
                    pdfName = pdfInfo.first,
                    pageCount = pdfInfo.second,
                    fileSize = pdfInfo.third,
                    rangeEnd = pdfInfo.second
                )
            }
        }
    }

    fun setSplitMode(mode: SplitMode) {
        _uiState.update { it.copy(splitMode = mode) }
    }

    fun setRangeStart(start: Int) {
        _uiState.update { current ->
            current.copy(
                rangeStart = start.coerceIn(1, current.pageCount)
            )
        }
    }

    fun setRangeEnd(end: Int) {
        _uiState.update { current ->
            current.copy(
                rangeEnd = end.coerceIn(1, current.pageCount)
            )
        }
    }

    fun togglePage(page: Int) {
        _uiState.update { current ->
            val newSelected = if (page in current.selectedPages) {
                current.selectedPages - page
            } else {
                current.selectedPages + page
            }
            current.copy(selectedPages = newSelected)
        }
    }

    fun selectAllPages() {
        _uiState.update { current ->
            current.copy(selectedPages = (1..current.pageCount).toSet())
        }
    }

    fun deselectAllPages() {
        _uiState.update { it.copy(selectedPages = emptySet()) }
    }

    fun clearSelection() {
        _uiState.update { SplitPdfUiState() }
    }

    fun splitPdf() {
        val pdfUri = _uiState.value.selectedPdf ?: return
        val state = _uiState.value

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val pageRanges = when (state.splitMode) {
                    SplitMode.EXTRACT_ALL -> PageRanges.eachPage(state.pageCount)
                    SplitMode.RANGE -> PageRanges.single(state.rangeStart, state.rangeEnd, state.pageCount)
                    SplitMode.SELECT_PAGES -> PageRanges.selectedPages(state.selectedPages)
                }
                if (pageRanges.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, error = context.getString(R.string.error_split_failed)) }
                    return@launch
                }

                val resultFiles = pdfService.splitPdfs(pdfUri, pageRanges)
                
                // Save each split file to database
                resultFiles.forEach { file ->
                    savePdfToDatabase(file)
                }
                
                val resultUris = resultFiles.map { Uri.fromFile(it) }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        splitSuccess = true,
                        splitPdfUris = resultUris
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: context.getString(R.string.error_split_failed)
                    )
                }
            }
        }
    }
    
    private suspend fun savePdfToDatabase(file: File) {
        withContext(Dispatchers.IO) {
            val pageCount = try {
                PDDocument.load(file).use { it.numberOfPages }
            } catch (e: Exception) {
                1
            }
            
            val pdf = Pdf(
                id = 0,
                name = file.name,
                filePath = file.absolutePath,
                fileSize = file.length(),
                pageCount = pageCount,
                createdAt = System.currentTimeMillis(),
                sourceType = PdfSourceType.SPLIT
            )
            pdfRepository.insertPdf(pdf)
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(splitSuccess = false, splitPdfUris = emptyList()) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private suspend fun getPdfInfo(uri: Uri): Triple<String, Int, Long> {
        return withContext(Dispatchers.IO) {
            val name = fileInfo.getFileName(uri) ?: "unknown.pdf"
            val pageCount = fileInfo.getPageCount(uri)
            val size = fileInfo.getFileSize(uri)
            Triple(name, pageCount, size)
        }
    }



}
