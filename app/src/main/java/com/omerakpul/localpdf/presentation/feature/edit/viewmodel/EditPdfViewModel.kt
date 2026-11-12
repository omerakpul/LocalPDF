package com.omerakpul.localpdf.presentation.feature.edit.viewmodel

import kotlinx.coroutines.CancellationException
import com.omerakpul.localpdf.R
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import com.omerakpul.localpdf.domain.util.PageOrder
import com.omerakpul.localpdf.data.service.PdfService
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
    private val pdfService: PdfService,
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
                        fileSize = size,
                        pageOrder = PageOrder.identity(pageCount)
                    )
                }
            }
        }
    }

    fun clearSelection() {
        _uiState.update { EditPdfUiState() }
    }

    fun setEditAction(action: EditAction) {
        _uiState.update {
            it.copy(
                editAction = action,
                selectedPages = emptySet(),
                pageOrder = PageOrder.identity(it.pageCount),
                password = "",
                confirmPassword = ""
            )
        }
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

    fun setPassword(value: String) {
        _uiState.update { it.copy(password = value) }
    }

    fun setConfirmPassword(value: String) {
        _uiState.update { it.copy(confirmPassword = value) }
    }

    fun movePage(index: Int, delta: Int) {
        _uiState.update { it.copy(pageOrder = PageOrder.move(it.pageOrder, index, delta)) }
    }

    fun resetPageOrder() {
        _uiState.update { it.copy(pageOrder = PageOrder.identity(it.pageCount)) }
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
                        EditAction.DELETE_PAGES -> pdfService.deletePages(pdfUri, state.selectedPages.sorted())
                        EditAction.ROTATE_PAGES -> pdfService.rotatePages(pdfUri, state.selectedPages.sorted(), state.rotationAngle)
                        EditAction.REORDER_PAGES -> pdfService.reorderPages(pdfUri, state.pageOrder)
                        EditAction.DUPLICATE_PAGES -> pdfService.duplicatePages(pdfUri, state.selectedPages.sorted())
                        EditAction.PROTECT -> pdfService.protectPdf(pdfUri, state.password)
                        EditAction.UNLOCK -> pdfService.unlockPdf(pdfUri, state.password)
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
        _uiState.update { it.copy(editSuccess = false, editedPdfUri = null, password = "", confirmPassword = "") }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }





}
