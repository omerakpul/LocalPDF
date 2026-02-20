package com.omerakpul.localpdf.presentation.feature.edit.ui

import android.net.Uri

data class EditPdfUiState(
    val selectedPdf: Uri? = null,
    val pdfName: String = "",
    val pageCount: Int = 0,
    val fileSize: Long = 0,
    val selectedPages: Set<Int> = emptySet(),
    val editAction: EditAction = EditAction.NONE,
    val rotationAngle: Int = 90,
    val isLoading: Boolean = false,
    val error: String? = null,
    val editSuccess: Boolean = false,
    val editedPdfUri: Uri? = null
) {
    val canEdit: Boolean
        get() = selectedPdf != null && !isLoading && editAction != EditAction.NONE && selectedPages.isNotEmpty()
}

enum class EditAction {
    NONE,
    DELETE_PAGES,
    ROTATE_PAGES
}
