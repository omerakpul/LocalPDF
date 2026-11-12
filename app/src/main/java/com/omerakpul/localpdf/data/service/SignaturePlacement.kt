package com.omerakpul.localpdf.data.service

/**
 * PDF transformation matrix values (a, b, c, d, e, f) that draw a unit-square image so that it
 * appears upright inside a rectangle given in *display* space (origin top-left, y down) of a page
 * that is displayed with the given /Rotate value.
 */
data class PlacementMatrix(
    val a: Float, val b: Float, val c: Float, val d: Float,
    val e: Float, val f: Float
)

object SignaturePlacement {

    /**
     * @param rotation page rotation in degrees (0, 90, 180, 270)
     * @param userW/userH unrotated page size in user space
     * @param left/top display-space top-left corner of the image
     * @param dw/dh display-space image size
     * @param originX/originY lower-left corner of the page box in user space
     */
    fun matrixFor(
        rotation: Int,
        userW: Float,
        userH: Float,
        left: Float,
        top: Float,
        dw: Float,
        dh: Float,
        originX: Float = 0f,
        originY: Float = 0f
    ): PlacementMatrix {
        // Image lower-left corner in display space
        val ox = left
        val oy = top + dh
        return when (((rotation % 360) + 360) % 360) {
            90 -> PlacementMatrix(0f, dw, -dh, 0f, originX + oy, originY + ox)
            180 -> PlacementMatrix(-dw, 0f, 0f, -dh, originX + userW - ox, originY + oy)
            270 -> PlacementMatrix(0f, -dw, dh, 0f, originX + userW - oy, originY + userH - ox)
            else -> PlacementMatrix(dw, 0f, 0f, dh, originX + ox, originY + userH - oy)
        }
    }
}
