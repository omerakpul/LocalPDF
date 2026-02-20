package com.omerakpul.localpdf.presentation.feature.convert.ui

import android.net.Uri

data class ConvertUiState(
    val selectedFileUri: Uri? = null,
    val fileName: String = "",
    val fileSize: Long = 0,
    val conversionMode: ConversionMode = ConversionMode.WORD_TO_PDF,
    val isLoading: Boolean = false,
    val error: String? = null,
    val convertSuccess: Boolean = false,
    val convertedFileUri: Uri? = null
) {
    val canConvert: Boolean
        get() = selectedFileUri != null && !isLoading
}

enum class ConversionMode {
    WORD_TO_PDF,
    PDF_TO_WORD
}
