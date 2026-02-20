package com.omerakpul.localpdf.presentation.feature.detail.viewmodel

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omerakpul.localpdf.domain.model.Pdf
import com.omerakpul.localpdf.domain.repository.PdfRepository
import com.omerakpul.localpdf.presentation.feature.detail.ui.PdfDetailUiState
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PdfDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pdfRepository: PdfRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(PdfDetailUiState())
    val uiState: StateFlow<PdfDetailUiState> = _uiState.asStateFlow()

    private val pdfPath: String = savedStateHandle.get<String>("pdfPath") ?: ""
    private val sourceType: String = savedStateHandle.get<String>("sourceType") ?: "UNKNOWN"

    init {
        PDFBoxResourceLoader.init(context)
        loadPdfInfo()
    }

    private fun loadPdfInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val file = File(pdfPath)
                if (!file.exists()) {
                    _uiState.update { it.copy(isLoading = false, error = "File not found") }
                    return@launch
                }

                val pageCount = withContext(Dispatchers.IO) {
                    PDDocument.load(file).use { doc ->
                        doc.numberOfPages
                    }
                }

                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                val createdDate = dateFormat.format(Date(file.lastModified()))

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        filePath = pdfPath,
                        fileName = file.nameWithoutExtension,
                        fileSize = file.length(),
                        pageCount = pageCount,
                        createdDate = createdDate
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Unknown error")
                }
            }
        }
    }

    fun updateFileName(newName: String) {
        _uiState.update { it.copy(fileName = newName) }
    }

    fun saveToDownloads() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val savedPath = withContext(Dispatchers.IO) {
                    val sourceFile = File(pdfPath)
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                    )
                    val fileName = "${_uiState.value.fileName}.pdf"
                    val destFile = File(downloadsDir, fileName)
                    
                    sourceFile.copyTo(destFile, overwrite = true)

                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(destFile.absolutePath),
                        arrayOf("application/pdf"),
                        null
                    )

                    // Insert into Room DB so it appears in Files list
                    val pdf = Pdf(
                        name = fileName,
                        filePath = destFile.absolutePath,
                        fileSize = destFile.length(),
                        createdAt = System.currentTimeMillis(),
                        pageCount = _uiState.value.pageCount,
                        sourceType = try { com.omerakpul.localpdf.domain.model.PdfSourceType.valueOf(sourceType) } catch (e: Exception) { com.omerakpul.localpdf.domain.model.PdfSourceType.UNKNOWN }
                    )
                    pdfRepository.insertPdf(pdf)

                    destFile.absolutePath
                }

                _uiState.update { it.copy(isLoading = false, savedFilePath = savedPath) }
            } catch (e: Exception) {
                _uiState.update {
                     it.copy(isLoading = false, error = e.message ?: "Save failed")
                }
            }
        }
    }

    fun deleteFile() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    File(pdfPath).delete()
                }
                _uiState.update { it.copy(isDeleted = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Delete failed") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetSavedState() {
        _uiState.update { it.copy(savedFilePath = null) }
    }
}
