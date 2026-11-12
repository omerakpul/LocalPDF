package com.omerakpul.localpdf.domain.util

/** Builds the page ranges (1-based, inclusive) that the split operation works with. */
object PageRanges {

    /** Every page becomes its own range: 1..1, 2..2, ... */
    fun eachPage(pageCount: Int): List<IntRange> = (1..pageCount).map { it..it }

    /** Each selected page becomes its own range, in page order. */
    fun selectedPages(pages: Set<Int>): List<IntRange> = pages.sorted().map { it..it }

    /**
     * One range from [start] to [end]. Swapped bounds are corrected and the range is clamped
     * to the document; an empty list means nothing valid is left.
     */
    fun single(start: Int, end: Int, pageCount: Int): List<IntRange> {
        if (pageCount <= 0) return emptyList()
        val low = minOf(start, end).coerceAtLeast(1)
        val high = maxOf(start, end).coerceAtMost(pageCount)
        return if (low <= high) listOf(low..high) else emptyList()
    }
}
