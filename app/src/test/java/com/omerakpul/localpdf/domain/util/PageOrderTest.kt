package com.omerakpul.localpdf.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PageOrderTest {

    @Test
    fun movesItemDownAndUp() {
        val order = PageOrder.identity(4)
        assertEquals(listOf(2, 1, 3, 4), PageOrder.move(order, 0, 1))
        assertEquals(listOf(1, 2, 4, 3), PageOrder.move(order, 3, -1))
    }

    @Test
    fun movingOutOfRangeKeepsOrder() {
        val order = PageOrder.identity(3)
        assertEquals(order, PageOrder.move(order, 0, -1))
        assertEquals(order, PageOrder.move(order, 2, 1))
        assertEquals(order, PageOrder.move(order, 5, 1))
    }

    @Test
    fun detectsChange() {
        assertFalse(PageOrder.isChanged(PageOrder.identity(5)))
        assertTrue(PageOrder.isChanged(PageOrder.move(PageOrder.identity(5), 1, 1)))
    }
}
