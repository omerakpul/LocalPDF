package com.omerakpul.localpdf.presentation.feature.compress.ui

import android.net.Uri

data class CompressPdfUiState(
    val selectedPdf: Uri? = null,
    val pdfName: String = "",
    val originalSize: Long = 0,
    val pageCount: Int = 0,
    val quality: Float = 0.7f, // 0.3 = low, 0.5 = medium, 0.7 = high
    val isLoading: Boolean = false,
    val error: String? = null,
    val compressSuccess: Boolean = false,
    val compressedPdfUri: Uri? = null,
    val estimatedSize: Long = 0,
    // Bytes used by recompressible images; negative = not analysed (yet)
    val imageBytes: Long = -1,
    val progress: Float = 0f
) {
    val canCompress: Boolean
        get() = selectedPdf != null && !isLoading
}
