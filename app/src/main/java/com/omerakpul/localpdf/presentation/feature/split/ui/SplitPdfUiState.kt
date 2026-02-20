package com.omerakpul.localpdf.presentation.feature.split.ui

import android.net.Uri

data class SplitPdfUiState(
    val selectedPdf: Uri? = null,
    val pdfName: String = "",
    val pageCount: Int = 0,
    val fileSize: Long = 0,
    val splitMode: SplitMode = SplitMode.EXTRACT_ALL,
    val selectedPages: Set<Int> = emptySet(), // 1-indexed pages
    val rangeStart: Int = 1,
    val rangeEnd: Int = 1,
    val isLoading: Boolean = false,
    val error: String? = null,
    val splitSuccess: Boolean = false,
    val splitPdfUris: List<Uri> = emptyList()
) {
    val canSplit: Boolean
        get() = selectedPdf != null && !isLoading && when (splitMode) {
            SplitMode.EXTRACT_ALL -> pageCount > 0
            SplitMode.RANGE -> rangeStart in 1..rangeEnd && rangeEnd <= pageCount
            SplitMode.SELECT_PAGES -> selectedPages.isNotEmpty()
        }
}

enum class SplitMode {
    EXTRACT_ALL,    // Her sayfayı ayrı PDF yap
    RANGE,          // Belirli sayfa aralığını çıkar
    SELECT_PAGES    // Seçilen sayfaları çıkar
}
