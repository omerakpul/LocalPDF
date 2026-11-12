package com.omerakpul.localpdf.domain.util

object ImageExportNaming {

    /** "report", page 3 of 12 -> "report_page_03.png" */
    fun pageFileName(baseName: String, page: Int, totalPages: Int, extension: String = "png"): String {
        val safeBase = baseName
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .trim()
            .ifEmpty { "document" }
        val digits = maxOf(2, totalPages.toString().length)
        return "${safeBase}_page_${page.toString().padStart(digits, '0')}.$extension"
    }
}
