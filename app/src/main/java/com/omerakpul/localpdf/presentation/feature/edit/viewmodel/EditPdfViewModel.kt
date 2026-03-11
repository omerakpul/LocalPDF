package com.omerakpul.localpdf.presentation.feature.edit.viewmodel

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.omerakpul.localpdf.presentation.feature.edit.ui.EditAction
import com.omerakpul.localpdf.presentation.feature.edit.ui.EditPdfUiState
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
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditPdfUiState())
    val uiState: StateFlow<EditPdfUiState> = _uiState.asStateFlow()

    fun selectPdf(uri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val name = getFileName(uri) ?: "unknown.pdf"
                val pageCount = getPageCount(uri)
                val size = getFileSize(uri)
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
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Edit failed"
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
