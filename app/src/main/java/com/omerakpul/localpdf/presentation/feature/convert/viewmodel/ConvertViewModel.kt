package com.omerakpul.localpdf.presentation.feature.convert.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omerakpul.localpdf.data.service.PdfService
import com.omerakpul.localpdf.presentation.feature.convert.ui.ConversionMode
import com.omerakpul.localpdf.presentation.feature.convert.ui.ConvertUiState
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
    private val pdfService: PdfService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConvertUiState())
    val uiState: StateFlow<ConvertUiState> = _uiState.asStateFlow()

    fun setConversionMode(mode: ConversionMode) {
        _uiState.update { ConvertUiState(conversionMode = mode) }
    }

    fun selectFile(uri: Uri) {
        val name = getFileName(uri) ?: "unknown"
        val size = getFileSize(uri)
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
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val resultFile = when (_uiState.value.conversionMode) {
                    ConversionMode.WORD_TO_PDF -> pdfService.convertWordToPdf(fileUri)
                    ConversionMode.PDF_TO_WORD -> pdfService.convertPdfToWord(fileUri)
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
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Conversion failed"
                    )
                }
            }
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(convertSuccess = false, convertedFileUri = null) }
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
}
