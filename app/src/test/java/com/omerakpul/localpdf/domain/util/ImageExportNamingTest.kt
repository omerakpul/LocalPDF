package com.omerakpul.localpdf.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ImageExportNamingTest {

    @Test
    fun padsPageNumbers() {
        assertEquals("report_page_03.png", ImageExportNaming.pageFileName("report", 3, 12))
        assertEquals("report_page_003.png", ImageExportNaming.pageFileName("report", 3, 120))
    }

    @Test
    fun replacesIllegalCharactersAndFallsBack() {
        assertEquals("a_b_page_01.png", ImageExportNaming.pageFileName("a/b", 1, 5))
        assertEquals("document_page_01.png", ImageExportNaming.pageFileName("  ", 1, 5))
    }
}
