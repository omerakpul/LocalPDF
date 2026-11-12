package com.omerakpul.localpdf.presentation.feature.convert.viewmodel

import com.omerakpul.localpdf.domain.util.ExportImageFormat
import kotlinx.coroutines.CancellationException
import com.omerakpul.localpdf.R
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import com.omerakpul.localpdf.data.service.FileInfoProvider
import androidx.lifecycle.viewModelScope
import com.omerakpul.localpdf.data.service.PdfService
import com.omerakpul.localpdf.presentation.feature.convert.ui.ConversionMode
import com.omerakpul.localpdf.presentation.feature.convert.ui.ConvertUiState
import com.omerakpul.localpdf.domain.util.MemoryUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConvertViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pdfService: PdfService,
    private val memoryUtil: MemoryUtil,
    private val fileInfo: FileInfoProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConvertUiState())
    val uiState: StateFlow<ConvertUiState> = _uiState.asStateFlow()

    fun setConversionMode(mode: ConversionMode) {
        _uiState.update { ConvertUiState(conversionMode = mode, imageFormat = it.imageFormat) }
    }

    fun selectFile(uri: Uri) {
        val size = fileInfo.getFileSize(uri)
        val maxSize = memoryUtil.getMaxAllowedFileSizeBytes()

        if (size > maxSize) {
            _uiState.update { current ->
                current.copy(
                    error = context.getString(R.string.error_file_too_large, memoryUtil.formatSize(size), memoryUtil.formatSize(maxSize))
                )
            }
            return
        }

        val name = fileInfo.getFileName(uri) ?: "unknown"
        _uiState.update { current ->
            current.copy(
                selectedFileUri = uri,
                fileName = name,
                fileSize = size
            )
        }
    }

    fun clearSelection() {
        _uiState.update { current -> current.copy(selectedFileUri = null, fileName = "", fileSize = 0) }
    }

    fun convert() {
        val fileUri = _uiState.value.selectedFileUri ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, progress = 0f) }

            try {
                if (_uiState.value.conversionMode == ConversionMode.PDF_TO_IMAGES) {
                    val baseName = _uiState.value.fileName.substringBeforeLast('.')
                    val count = pdfService.exportPdfToImages(fileUri, baseName, _uiState.value.imageFormat) { progress ->
                        _uiState.update { it.copy(progress = progress) }
                    }
                    _uiState.update { it.copy(isLoading = false, imagesExported = count) }
                    return@launch
                }

                val resultFile = when (_uiState.value.conversionMode) {
                    ConversionMode.WORD_TO_PDF -> pdfService.convertWordToPdf(fileUri)
                    else -> pdfService.convertPdfToWord(fileUri)
                }
                val resultUri = Uri.fromFile(resultFile)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        convertSuccess = true,
                        convertedFileUri = resultUri
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: context.getString(R.string.error_convert_failed)
                    )
                }
            }
        }
    }

    fun setImageFormat(format: ExportImageFormat) {
        _uiState.update { it.copy(imageFormat = format) }
    }

    fun resetImagesExported() {
        _uiState.update { it.copy(imagesExported = 0) }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(convertSuccess = false, convertedFileUri = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }


}
