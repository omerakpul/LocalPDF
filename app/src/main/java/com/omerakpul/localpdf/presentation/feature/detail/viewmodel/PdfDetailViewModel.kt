package com.omerakpul.localpdf.presentation.feature.detail.viewmodel

import kotlinx.coroutines.CancellationException
import com.omerakpul.localpdf.R
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omerakpul.localpdf.domain.model.Pdf
import com.omerakpul.localpdf.domain.repository.PdfRepository
import com.omerakpul.localpdf.presentation.feature.detail.ui.PdfDetailUiState
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException
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
import java.io.IOException
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
                    _uiState.update { it.copy(isLoading = false, error = context.getString(R.string.error_file_not_found)) }
                    return@launch
                }

                val isPdf = file.extension.lowercase() == "pdf"
                var isEncrypted = false
                val pageCount = if (isPdf) {
                    withContext(Dispatchers.IO) {
                        try {
                            PDDocument.load(file).use { doc ->
                                doc.numberOfPages
                            }
                        } catch (e: InvalidPasswordException) {
                            // Password-protected: page count is unavailable without the password
                            isEncrypted = true
                            0
                        }
                    }
                } else 0

                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                val createdDate = dateFormat.format(Date(file.lastModified()))

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        filePath = pdfPath,
                        fileName = file.nameWithoutExtension,
                        fileSize = file.length(),
                        pageCount = pageCount,
                        isEncrypted = isEncrypted,
                        createdDate = createdDate
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: context.getString(R.string.error_unknown))
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
                    val extension = sourceFile.extension
                    val mimeType = if (extension.lowercase() == "pdf") "application/pdf" else "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    val fileName = "${_uiState.value.fileName}.$extension"

                    copyToDownloads(sourceFile, fileName, mimeType)

                    // Keep the app-private copy in the Files list (always readable by the app)
                    val pdf = Pdf(
                        name = fileName,
                        filePath = sourceFile.absolutePath,
                        fileSize = sourceFile.length(),
                        createdAt = System.currentTimeMillis(),
                        pageCount = _uiState.value.pageCount,
                        sourceType = try { com.omerakpul.localpdf.domain.model.PdfSourceType.valueOf(sourceType) } catch (e: Exception) { com.omerakpul.localpdf.domain.model.PdfSourceType.UNKNOWN }
                    )
                    pdfRepository.insertPdf(pdf)

                    sourceFile.absolutePath
                }

                _uiState.update { it.copy(isLoading = false, savedFilePath = savedPath) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                     it.copy(isLoading = false, error = e.message ?: context.getString(R.string.error_save_failed))
                }
            }
        }
    }

    /**
     * Copies a file into the public Downloads folder.
     * Android 10+ requires MediaStore (direct File writes to Downloads are blocked).
     */
    private fun copyToDownloads(sourceFile: File, fileName: String, mimeType: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IOException(context.getString(R.string.error_save_failed))
            try {
                resolver.openOutputStream(uri)?.use { output ->
                    sourceFile.inputStream().use { it.copyTo(output) }
                } ?: throw IOException(context.getString(R.string.error_save_failed))
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
                throw e
            }
        } else {
            // Android 8-9: legacy storage (needs WRITE_EXTERNAL_STORAGE)
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val destFile = File(downloadsDir, fileName)
            sourceFile.copyTo(destFile, overwrite = true)
            MediaScannerConnection.scanFile(
                context, arrayOf(destFile.absolutePath), arrayOf(mimeType), null
            )
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
                if (e is CancellationException) throw e
                _uiState.update { it.copy(error = e.message ?: context.getString(R.string.error_delete_failed)) }
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
