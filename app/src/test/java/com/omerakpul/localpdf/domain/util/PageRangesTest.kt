package com.omerakpul.localpdf.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PageRangesTest {

    @Test
    fun eachPageMakesOneRangePerPage() {
        assertEquals(listOf(1..1, 2..2, 3..3), PageRanges.eachPage(3))
        assertTrue(PageRanges.eachPage(0).isEmpty())
    }

    @Test
    fun selectedPagesAreSortedAndSplitIndividually() {
        assertEquals(listOf(2..2, 5..5, 9..9), PageRanges.selectedPages(setOf(9, 2, 5)))
        assertTrue(PageRanges.selectedPages(emptySet()).isEmpty())
    }

    @Test
    fun singleRangeIsClampedToTheDocument() {
        assertEquals(listOf(2..4), PageRanges.single(2, 4, 10))
        assertEquals(listOf(1..10), PageRanges.single(-3, 99, 10))
    }

    @Test
    fun singleRangeFixesSwappedBounds() {
        assertEquals(listOf(3..7), PageRanges.single(7, 3, 10))
    }

    @Test
    fun singleRangeOutsideTheDocumentIsEmpty() {
        assertTrue(PageRanges.single(20, 30, 10).isEmpty())
        assertTrue(PageRanges.single(1, 2, 0).isEmpty())
    }
}
