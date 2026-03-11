package com.omerakpul.localpdf.presentation.feature.compress.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omerakpul.localpdf.data.service.PdfService
import com.omerakpul.localpdf.presentation.feature.compress.ui.CompressPdfUiState
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
class CompressPdfViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pdfService: PdfService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompressPdfUiState())
    val uiState: StateFlow<CompressPdfUiState> = _uiState.asStateFlow()

    fun selectPdf(uri: Uri) {
        viewModelScope.launch {
            val pdfInfo = getPdfInfo(uri)
            _uiState.update { current ->
                current.copy(
                    selectedPdf = uri,
                    pdfName = pdfInfo.first,
                    originalSize = pdfInfo.second,
                    pageCount = pdfInfo.third,
                    estimatedSize = (pdfInfo.second * _uiState.value.quality).toLong()
                )
            }
        }
    }

    fun setQuality(quality: Float) {
        _uiState.update { current ->
            current.copy(
                quality = quality,
                estimatedSize = (current.originalSize * quality).toLong()
            )
        }
    }

    fun clearSelection() {
        _uiState.update { CompressPdfUiState() }
    }

    fun compressPdf() {
        val pdfUri = _uiState.value.selectedPdf ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val resultFile = pdfService.compressPdf(pdfUri, _uiState.value.quality)
                val resultUri = Uri.fromFile(resultFile)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        compressSuccess = true,
                        compressedPdfUri = resultUri
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Compression failed"
                    )
                }
            }
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(compressSuccess = false, compressedPdfUri = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private suspend fun getPdfInfo(uri: Uri): Triple<String, Long, Int> {
        return withContext(Dispatchers.IO) {
            val name = getFileName(uri) ?: "unknown.pdf"
            val size = getFileSize(uri)
            val pageCount = getPageCount(uri)
            Triple(name, size, pageCount)
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
