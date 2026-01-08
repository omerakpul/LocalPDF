package com.omerakpul.localpdf.presentation.feature.merge.ui

import com.omerakpul.localpdf.domain.model.PdfFile

data class MergePdfUiState(
    val selectedPdfs: List<PdfFile> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val mergeSuccess: Boolean = false,
    val mergedPdfUri: android.net.Uri? = null
) {
    val totalSize: Long
        get() = selectedPdfs.sumOf { it.size }

    val totalPages: Int
        get() = selectedPdfs.sumOf { it.pageCount }

    val canMerge: Boolean
        get() = selectedPdfs.size >= 2 && !isLoading
}
