package com.omerakpul.localpdf.presentation.feature.edit.ui

import android.net.Uri
import com.omerakpul.localpdf.domain.util.PageOrder

data class EditPdfUiState(
    val selectedPdf: Uri? = null,
    val pdfName: String = "",
    val pageCount: Int = 0,
    val fileSize: Long = 0,
    val selectedPages: Set<Int> = emptySet(),
    // Current page order for the reorder action (original 1-based page numbers)
    val pageOrder: List<Int> = emptyList(),
    val editAction: EditAction = EditAction.NONE,
    val rotationAngle: Int = 90,
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val editSuccess: Boolean = false,
    val editedPdfUri: Uri? = null
) {
    val canEdit: Boolean
        get() = selectedPdf != null && !isLoading && when (editAction) {
            EditAction.NONE -> false
            EditAction.REORDER_PAGES -> PageOrder.isChanged(pageOrder)
            EditAction.PROTECT -> password.isNotEmpty() && password == confirmPassword
            EditAction.UNLOCK -> password.isNotEmpty()
            else -> selectedPages.isNotEmpty()
        }
}

enum class EditAction {
    NONE,
    DELETE_PAGES,
    ROTATE_PAGES,
    REORDER_PAGES,
    DUPLICATE_PAGES,
    PROTECT,
    UNLOCK
}
