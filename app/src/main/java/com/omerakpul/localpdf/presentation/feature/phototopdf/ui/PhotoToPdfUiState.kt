package com.omerakpul.localpdf.presentation.feature.phototopdf.ui

import android.net.Uri

data class PhotoToPdfUiState(
    val selectedPhotos: List<Uri> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val createSuccess: Boolean = false,
    val createdPdfUri: Uri? = null
) {
    val canCreate: Boolean
        get() = selectedPhotos.isNotEmpty() && !isLoading
}
