package com.omerakpul.localpdf.presentation.feature.sign.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.geometry.Offset

data class SignPdfUiState(
    val selectedPdf: Uri? = null,
    val pdfName: String = "",
    val pageCount: Int = 0,
    val signaturePoints: List<List<Offset>> = emptyList(),
    val currentStroke: List<Offset> = emptyList(),
    val signaturePage: Int = 1,
    // Placement as fractions of the (displayed) page: top-left corner and width
    val signatureX: Float = 0.66f,
    val signatureY: Float = 0.85f,
    val signatureWidth: Float = 0.25f,
    val pagePreview: Bitmap? = null,
    val signatureBitmap: Bitmap? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val signSuccess: Boolean = false,
    val signedPdfUri: Uri? = null
) {
    val canSign: Boolean
        get() = selectedPdf != null && signaturePoints.isNotEmpty() && !isLoading

    val hasSignature: Boolean
        get() = signaturePoints.isNotEmpty()
}
