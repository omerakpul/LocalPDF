package com.omerakpul.localpdf.presentation.feature.phototopdf.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omerakpul.localpdf.data.service.PdfService
import com.omerakpul.localpdf.presentation.feature.phototopdf.ui.PhotoToPdfUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoToPdfViewModel @Inject constructor(
    private val pdfService: PdfService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhotoToPdfUiState())
    val uiState: StateFlow<PhotoToPdfUiState> = _uiState.asStateFlow()

    fun addPhotos(uris: List<Uri>) {
        _uiState.update { current ->
            current.copy(selectedPhotos = current.selectedPhotos + uris)
        }
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
}
