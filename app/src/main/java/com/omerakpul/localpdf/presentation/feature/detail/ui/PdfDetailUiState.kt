package com.omerakpul.localpdf.presentation.feature.detail.ui

data class PdfDetailUiState(
    val filePath: String = "",
    val fileName: String = "",
    val fileSize: Long = 0,
    val pageCount: Int = 0,
    val createdDate: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false
)
