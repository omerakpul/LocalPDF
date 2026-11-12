package com.omerakpul.localpdf.presentation.feature.merge.viewmodel

import kotlinx.coroutines.CancellationException
import com.omerakpul.localpdf.R
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import com.omerakpul.localpdf.data.service.FileInfoProvider
import androidx.lifecycle.viewModelScope
import com.omerakpul.localpdf.data.service.PdfService
import com.omerakpul.localpdf.domain.model.PdfFile
import com.omerakpul.localpdf.presentation.feature.merge.ui.MergePdfUiState
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
import javax.inject.Inject

@HiltViewModel
class MergePdfViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pdfService: PdfService,
    private val memoryUtil: MemoryUtil,
    private val fileInfo: FileInfoProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(MergePdfUiState())
    val uiState: StateFlow<MergePdfUiState> = _uiState.asStateFlow()

    fun addPdfs(uris: List<Uri>) {
        viewModelScope.launch {
            val maxSize = memoryUtil.getMaxAllowedFileSizeBytes()
            var hasOversizedFile = false

            val newPdfs = uris.mapNotNull { uri ->
                val info = getPdfInfo(uri)
                if (info != null && info.size > maxSize) {
                    hasOversizedFile = true
                    null
                } else {
                    info
                }
            }

            if (hasOversizedFile) {
                _uiState.update { current ->
                    current.copy(
                        error = context.getString(R.string.error_files_too_large, memoryUtil.formatSize(maxSize))
                    )
                }
            }

            if (newPdfs.isNotEmpty()) {
                _uiState.update { current ->
                    current.copy(selectedPdfs = current.selectedPdfs + newPdfs)
                }
            }
        }
    }

    fun removePdf(id: String) {
        _uiState.update { current ->
            current.copy(selectedPdfs = current.selectedPdfs.filter { it.id != id })
        }
    }

    fun clearAll() {
        _uiState.update { it.copy(selectedPdfs = emptyList()) }
    }

    fun reorderPdfs(fromIndex: Int, toIndex: Int) {
        _uiState.update { current ->
            val mutableList = current.selectedPdfs.toMutableList()
            val item = mutableList.removeAt(fromIndex)
            mutableList.add(toIndex, item)
            current.copy(selectedPdfs = mutableList)
        }
    }

    fun mergePdfs() {
        if (_uiState.value.selectedPdfs.size < 2) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val uris = _uiState.value.selectedPdfs.map { it.uri }
                val resultFile = pdfService.mergePdfs(uris)
                val resultUri = Uri.fromFile(resultFile)
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        mergeSuccess = true,
                        mergedPdfUri = resultUri
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: context.getString(R.string.error_unknown)
                    )
                }
            }
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(mergeSuccess = false, mergedPdfUri = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private suspend fun getPdfInfo(uri: Uri): PdfFile? {
        return withContext(Dispatchers.IO) {
            try {
                val name = fileInfo.getFileName(uri) ?: "unknown.pdf"
                val size = fileInfo.getFileSize(uri)
                val pageCount = fileInfo.getPageCount(uri)
                PdfFile(uri = uri, name = name, size = size, pageCount = pageCount)
            } catch (e: Exception) {
                null
            }
        }
    }



}
