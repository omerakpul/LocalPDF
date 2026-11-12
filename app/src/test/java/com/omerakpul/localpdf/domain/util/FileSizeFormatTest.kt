package com.omerakpul.localpdf.domain.util

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class FileSizeFormatTest {

    private val us = Locale.US

    @Test
    fun formatsBytes() {
        assertEquals("0 B", formatFileSize(0, us))
        assertEquals("1023 B", formatFileSize(1023, us))
    }

    @Test
    fun formatsKilobytesAndMegabytes() {
        assertEquals("1.0 KB", formatFileSize(1024, us))
        assertEquals("1.5 KB", formatFileSize(1536, us))
        assertEquals("1.0 MB", formatFileSize(1024L * 1024, us))
        assertEquals("2.3 MB", formatFileSize((2.25 * 1024 * 1024).toLong() + 1024, us))
    }

    @Test
    fun negativeSizesAreTreatedAsZero() {
        assertEquals("0 B", formatFileSize(-5, us))
    }

    @Test
    fun usesTheLocaleDecimalSeparator() {
        assertEquals("1,5 KB", formatFileSize(1536, Locale("tr", "TR")))
    }
}
