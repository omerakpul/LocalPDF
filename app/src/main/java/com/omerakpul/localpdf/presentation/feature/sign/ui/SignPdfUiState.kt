package com.omerakpul.localpdf.presentation.feature.sign.ui

import android.net.Uri
import androidx.compose.ui.geometry.Offset

data class SignPdfUiState(
    val selectedPdf: Uri? = null,
    val pdfName: String = "",
    val pageCount: Int = 0,
    val signaturePoints: List<List<Offset>> = emptyList(),
    val currentStroke: List<Offset> = emptyList(),
    val signaturePage: Int = 1,
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
