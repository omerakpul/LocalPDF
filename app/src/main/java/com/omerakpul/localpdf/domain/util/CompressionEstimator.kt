package com.omerakpul.localpdf.domain.util

/**
 * Rough estimate of the compressed size. Only embedded images shrink; text, fonts and
 * vector content stay as they are, so PDFs without images do not get smaller.
 */
object CompressionEstimator {

    /** Same mapping that PdfService.compressPdf uses for image downscaling. */
    fun imageScale(quality: Float): Float = (0.25f + quality.coerceIn(0.1f, 1f) * 0.75f).coerceAtMost(1f)

    /**
     * @param originalSize size of the PDF in bytes
     * @param imageBytes bytes used by recompressible images, or a negative value if unknown
     */
    fun estimate(originalSize: Long, imageBytes: Long, quality: Float): Long {
        if (originalSize <= 0) return 0
        val q = quality.coerceIn(0.1f, 1f)
        if (imageBytes < 0) {
            // Unknown composition: fall back to the previous simple guess
            return (originalSize * q).toLong()
        }
        val images = imageBytes.coerceAtMost(originalSize)
        val scale = imageScale(q)
        val ratio = (scale * scale * (0.2f + 0.8f * q)).coerceIn(0.05f, 1f)
        val estimate = (originalSize - images) + (images * ratio).toLong()
        return estimate.coerceIn(0L, originalSize)
    }
}
