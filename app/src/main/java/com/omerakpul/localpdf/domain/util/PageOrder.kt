package com.omerakpul.localpdf.domain.util

object PageOrder {

    /** Returns a copy of [order] with the item at [index] moved by [delta] positions. */
    fun move(order: List<Int>, index: Int, delta: Int): List<Int> {
        val target = index + delta
        if (index !in order.indices || target !in order.indices) return order
        val result = order.toMutableList()
        val item = result.removeAt(index)
        result.add(target, item)
        return result
    }

    fun identity(pageCount: Int): List<Int> = (1..pageCount).toList()

    fun isChanged(order: List<Int>): Boolean = order != identity(order.size)
}
