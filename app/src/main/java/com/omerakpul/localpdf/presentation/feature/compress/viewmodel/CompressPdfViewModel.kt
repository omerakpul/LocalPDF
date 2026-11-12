package com.omerakpul.localpdf.presentation.feature.compress.viewmodel

import kotlinx.coroutines.CancellationException
import com.omerakpul.localpdf.R
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import com.omerakpul.localpdf.domain.util.CompressionEstimator
import com.omerakpul.localpdf.data.service.FileInfoProvider
import androidx.lifecycle.viewModelScope
import com.omerakpul.localpdf.data.service.PdfService
import com.omerakpul.localpdf.presentation.feature.compress.ui.CompressPdfUiState
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
class CompressPdfViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pdfService: PdfService,
    private val memoryUtil: MemoryUtil,
    private val fileInfo: FileInfoProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompressPdfUiState())
    val uiState: StateFlow<CompressPdfUiState> = _uiState.asStateFlow()

    fun selectPdf(uri: Uri) {
        viewModelScope.launch {
            val pdfInfo = getPdfInfo(uri)
            val fileSize = pdfInfo.second
            val maxSize = memoryUtil.getMaxAllowedFileSizeBytes()

            if (fileSize > maxSize) {
                _uiState.update { current ->
                    current.copy(
                        error = context.getString(R.string.error_file_too_large, memoryUtil.formatSize(fileSize), memoryUtil.formatSize(maxSize))
                    )
                }
                return@launch
            }

            val imageBytes = try {
                pdfService.analyzeImageBytes(uri)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                -1L
            }

            _uiState.update { current ->
                current.copy(
                    selectedPdf = uri,
                    pdfName = pdfInfo.first,
                    originalSize = pdfInfo.second,
                    pageCount = pdfInfo.third,
                    imageBytes = imageBytes,
                    estimatedSize = CompressionEstimator.estimate(pdfInfo.second, imageBytes, current.quality)
                )
            }
        }
    }

    fun setQuality(quality: Float) {
        _uiState.update { current ->
            current.copy(
                quality = quality,
                estimatedSize = CompressionEstimator.estimate(current.originalSize, current.imageBytes, quality)
            )
        }
    }

    fun clearSelection() {
        _uiState.update { CompressPdfUiState() }
    }

    fun compressPdf() {
        val pdfUri = _uiState.value.selectedPdf ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, progress = 0f) }

            try {
                val resultFile = pdfService.compressPdf(pdfUri, _uiState.value.quality) { progress ->
                    _uiState.update { it.copy(progress = progress) }
                }
                val resultUri = Uri.fromFile(resultFile)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        compressSuccess = true,
                        compressedPdfUri = resultUri
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: context.getString(R.string.error_compress_failed)
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
            val name = fileInfo.getFileName(uri) ?: "unknown.pdf"
            val size = fileInfo.getFileSize(uri)
            val pageCount = fileInfo.getPageCount(uri)
            Triple(name, size, pageCount)
        }
    }



}
