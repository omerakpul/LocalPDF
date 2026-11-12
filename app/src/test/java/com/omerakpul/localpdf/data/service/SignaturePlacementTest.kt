package com.omerakpul.localpdf.data.service

import org.junit.Assert.assertEquals
import org.junit.Test

class SignaturePlacementTest {

    private val userW = 600f
    private val userH = 800f
    private val delta = 0.001f

    /** Independent user-space -> display-space mapping for a page displayed with [rotation]. */
    private fun toDisplay(rotation: Int, ux: Float, uy: Float): Pair<Float, Float> = when (rotation) {
        90 -> Pair(uy, ux)
        180 -> Pair(userW - ux, uy)
        270 -> Pair(userH - uy, userW - ux)
        else -> Pair(ux, userH - uy)
    }

    private fun applyMatrix(m: PlacementMatrix, x: Float, y: Float): Pair<Float, Float> =
        Pair(m.a * x + m.c * y + m.e, m.b * x + m.d * y + m.f)

    @Test
    fun imageCornersLandOnDisplayRectangleForEveryRotation() {
        val left = 120f
        val top = 200f
        val dw = 150f
        val dh = 75f

        for (rotation in listOf(0, 90, 180, 270)) {
            val m = SignaturePlacement.matrixFor(rotation, userW, userH, left, top, dw, dh)

            // Unit-square corners: lower-left, lower-right, upper-left, upper-right
            val expected = listOf(
                Pair(left, top + dh),
                Pair(left + dw, top + dh),
                Pair(left, top),
                Pair(left + dw, top)
            )
            val corners = listOf(Pair(0f, 0f), Pair(1f, 0f), Pair(0f, 1f), Pair(1f, 1f))

            corners.zip(expected).forEach { (corner, want) ->
                val (ux, uy) = applyMatrix(m, corner.first, corner.second)
                val (dx, dy) = toDisplay(rotation, ux, uy)
                assertEquals("rotation $rotation x", want.first, dx, delta)
                assertEquals("rotation $rotation y", want.second, dy, delta)
            }
        }
    }

    @Test
    fun pageBoxOriginIsAddedToTheTranslation() {
        val plain = SignaturePlacement.matrixFor(0, userW, userH, 10f, 10f, 100f, 50f)
        val shifted = SignaturePlacement.matrixFor(0, userW, userH, 10f, 10f, 100f, 50f, 25f, 40f)
        assertEquals(plain.e + 25f, shifted.e, delta)
        assertEquals(plain.f + 40f, shifted.f, delta)
    }
}
