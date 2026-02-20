package com.omerakpul.localpdf.presentation.feature.split.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omerakpul.localpdf.data.service.PdfService
import com.omerakpul.localpdf.domain.model.Pdf
import com.omerakpul.localpdf.domain.model.PdfSourceType
import com.omerakpul.localpdf.domain.repository.PdfRepository
import com.omerakpul.localpdf.presentation.feature.split.ui.SplitMode
import com.omerakpul.localpdf.presentation.feature.split.ui.SplitPdfUiState
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
    private val pdfRepository: PdfRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplitPdfUiState())
    val uiState: StateFlow<SplitPdfUiState> = _uiState.asStateFlow()

    fun selectPdf(uri: Uri) {
        viewModelScope.launch {
            val pdfInfo = getPdfInfo(uri)
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
                    SplitMode.EXTRACT_ALL -> {
                        // Her sayfa ayrı bir PDF
                        (1..state.pageCount).map { it..it }
                    }
                    SplitMode.RANGE -> {
                        // Tek bir aralık
                        listOf(state.rangeStart..state.rangeEnd)
                    }
                    SplitMode.SELECT_PAGES -> {
                        // Her seçilen sayfa ayrı bir PDF
                        state.selectedPages.sorted().map { it..it }
                    }
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
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Split failed"
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

    private suspend fun getPdfInfo(uri: Uri): Triple<String, Int, Long> {
        return withContext(Dispatchers.IO) {
            val name = getFileName(uri) ?: "unknown.pdf"
            val pageCount = getPageCount(uri)
            val size = getFileSize(uri)
            Triple(name, pageCount, size)
        }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    private fun getFileSize(uri: Uri): Long {
        var size = 0L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst() && sizeIndex >= 0) {
                size = cursor.getLong(sizeIndex)
            }
        }
        return size
    }

    private fun getPageCount(uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                PDDocument.load(inputStream).use { document ->
                    document.numberOfPages
                }
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }
}
