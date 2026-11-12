package com.omerakpul.localpdf.presentation.feature.convert.ui

import com.omerakpul.localpdf.domain.util.ExportImageFormat
import android.net.Uri

data class ConvertUiState(
    val selectedFileUri: Uri? = null,
    val fileName: String = "",
    val fileSize: Long = 0,
    val conversionMode: ConversionMode = ConversionMode.WORD_TO_PDF,
    val isLoading: Boolean = false,
    val error: String? = null,
    val convertSuccess: Boolean = false,
    val convertedFileUri: Uri? = null,
    val progress: Float = 0f,
    // Number of images saved by the last PDF -> images export (0 = none)
    val imagesExported: Int = 0,
    val imageFormat: ExportImageFormat = ExportImageFormat.PNG
) {
    val canConvert: Boolean
        get() = selectedFileUri != null && !isLoading
}

enum class ConversionMode {
    WORD_TO_PDF,
    PDF_TO_WORD,
    PDF_TO_IMAGES
}
