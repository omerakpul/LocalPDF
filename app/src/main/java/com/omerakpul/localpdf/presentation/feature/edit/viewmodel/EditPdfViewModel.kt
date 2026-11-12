package com.omerakpul.localpdf.presentation.feature.edit.viewmodel

import kotlinx.coroutines.CancellationException
import com.omerakpul.localpdf.R
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import com.omerakpul.localpdf.data.service.FileInfoProvider
import androidx.lifecycle.viewModelScope
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.omerakpul.localpdf.presentation.feature.edit.ui.EditAction
import com.omerakpul.localpdf.presentation.feature.edit.ui.EditPdfUiState
import com.omerakpul.localpdf.domain.util.MemoryUtil
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
class EditPdfViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val memoryUtil: MemoryUtil,
    private val fileInfo: FileInfoProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditPdfUiState())
    val uiState: StateFlow<EditPdfUiState> = _uiState.asStateFlow()

    fun selectPdf(uri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val size = fileInfo.getFileSize(uri)
                val maxSize = memoryUtil.getMaxAllowedFileSizeBytes()

                if (size > maxSize) {
                    _uiState.update { current ->
                        current.copy(
                            error = context.getString(R.string.error_file_too_large, memoryUtil.formatSize(size), memoryUtil.formatSize(maxSize))
                        )
                    }
                    return@withContext
                }

                val name = fileInfo.getFileName(uri) ?: "unknown.pdf"
                val pageCount = fileInfo.getPageCount(uri)
                
                _uiState.update {
                    it.copy(
                        selectedPdf = uri,
                        pdfName = name,
                        pageCount = pageCount,
                        fileSize = size
                    )
                }
            }
        }
    }

    fun clearSelection() {
        _uiState.update { EditPdfUiState() }
    }

    fun setEditAction(action: EditAction) {
        _uiState.update { it.copy(editAction = action, selectedPages = emptySet()) }
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

    fun setRotationAngle(angle: Int) {
        _uiState.update { it.copy(rotationAngle = angle) }
    }

    fun editPdf() {
        val pdfUri = _uiState.value.selectedPdf ?: return
        val state = _uiState.value

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val resultFile = withContext(Dispatchers.IO) {
                    when (state.editAction) {
                        EditAction.DELETE_PAGES -> deletePages(pdfUri, state.selectedPages.sorted())
                        EditAction.ROTATE_PAGES -> rotatePages(pdfUri, state.selectedPages.sorted(), state.rotationAngle)
                        EditAction.NONE -> throw IllegalStateException("No action selected")
                    }
                }
                val resultUri = Uri.fromFile(resultFile)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        editSuccess = true,
                        editedPdfUri = resultUri
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: context.getString(R.string.error_edit_failed)
                    )
                }
            }
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(editSuccess = false, editedPdfUri = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun deletePages(pdfUri: Uri, pagesToDelete: List<Int>): File {
        val tempFile = File.createTempFile("temp_", ".pdf", context.cacheDir)
        context.contentResolver.openInputStream(pdfUri)?.use { input ->
            tempFile.outputStream().use { output -> input.copyTo(output) }
        }

        val document = PDDocument.load(tempFile)
        try {
            // Delete pages in reverse order to avoid index shifting
            pagesToDelete.sortedDescending().forEach { page ->
                val pageIndex = page - 1
                if (pageIndex in 0 until document.numberOfPages) {
                    document.removePage(pageIndex)
                }
            }

            val outputFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "edited_${System.currentTimeMillis()}.pdf"
            )
            document.save(outputFile)
            return outputFile
        } finally {
            document.close()
            tempFile.delete()
        }
    }

    private fun rotatePages(pdfUri: Uri, pagesToRotate: List<Int>, angle: Int): File {
        val tempFile = File.createTempFile("temp_", ".pdf", context.cacheDir)
        context.contentResolver.openInputStream(pdfUri)?.use { input ->
            tempFile.outputStream().use { output -> input.copyTo(output) }
        }

        val document = PDDocument.load(tempFile)
        try {
            pagesToRotate.forEach { page ->
                val pageIndex = page - 1
                if (pageIndex in 0 until document.numberOfPages) {
                    val pdfPage = document.getPage(pageIndex)
                    pdfPage.rotation = (pdfPage.rotation + angle) % 360
                }
            }

            val outputFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "edited_${System.currentTimeMillis()}.pdf"
            )
            document.save(outputFile)
            return outputFile
        } finally {
            document.close()
            tempFile.delete()
        }
    }



}
