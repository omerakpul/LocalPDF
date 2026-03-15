package com.omerakpul.localpdf.presentation.feature.phototopdf.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import android.provider.OpenableColumns
import androidx.lifecycle.viewModelScope
import com.omerakpul.localpdf.data.service.PdfService
import com.omerakpul.localpdf.presentation.feature.phototopdf.ui.PhotoToPdfUiState
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
class PhotoToPdfViewModel @Inject constructor(
    @ApplicationContext private val context: android.content.Context,
    private val pdfService: PdfService,
    private val memoryUtil: MemoryUtil
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhotoToPdfUiState())
    val uiState: StateFlow<PhotoToPdfUiState> = _uiState.asStateFlow()

    fun addPhotos(uris: List<Uri>) {
        val maxSize = memoryUtil.getMaxAllowedFileSizeBytes()
        var hasOversizedFile = false

        val validPhotos = uris.filter { uri ->
            val size = getFileSize(uri)
            if (size > maxSize) {
                hasOversizedFile = true
                false
            } else {
                true
            }
        }

        if (hasOversizedFile) {
            _uiState.update { current ->
                current.copy(
                    error = "One or more photos are too large. Your device strongly limits processing files above ${memoryUtil.formatSize(maxSize)} for stability."
                )
            }
        }

        if (validPhotos.isNotEmpty()) {
            _uiState.update { current ->
                current.copy(selectedPhotos = current.selectedPhotos + validPhotos)
            }
        }
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

    fun removePhoto(uri: Uri) {
        _uiState.update { current ->
            current.copy(selectedPhotos = current.selectedPhotos.filter { it != uri })
        }
    }

    fun reorderPhotos(fromIndex: Int, toIndex: Int) {
        _uiState.update { current ->
            val mutableList = current.selectedPhotos.toMutableList()
            val item = mutableList.removeAt(fromIndex)
            mutableList.add(toIndex, item)
            current.copy(selectedPhotos = mutableList)
        }
    }

    fun clearAll() {
        _uiState.update { PhotoToPdfUiState() }
    }

    fun createPdf() {
        if (_uiState.value.selectedPhotos.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val resultFile = pdfService.createPdfFromPhotos(_uiState.value.selectedPhotos)
                val resultUri = Uri.fromFile(resultFile)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        createSuccess = true,
                        createdPdfUri = resultUri
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to create PDF"
                    )
                }
            }
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(createSuccess = false, createdPdfUri = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
