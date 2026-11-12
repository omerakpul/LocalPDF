package com.omerakpul.localpdf.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompressionEstimatorTest {

    @Test
    fun pdfWithoutImagesDoesNotShrink() {
        assertEquals(1_000_000L, CompressionEstimator.estimate(1_000_000L, 0L, 0.3f))
    }

    @Test
    fun lowerQualityGivesSmallerEstimate() {
        val low = CompressionEstimator.estimate(10_000_000L, 9_000_000L, 0.3f)
        val high = CompressionEstimator.estimate(10_000_000L, 9_000_000L, 0.9f)
        assertTrue(low < high)
    }

    @Test
    fun estimateNeverExceedsOriginalAndKeepsNonImageBytes() {
        val original = 5_000_000L
        val images = 3_000_000L
        val estimate = CompressionEstimator.estimate(original, images, 1.0f)
        assertTrue(estimate <= original)
        assertTrue(estimate >= original - images)
    }

    @Test
    fun unknownCompositionFallsBackToQualityRatio() {
        assertEquals(700_000L, CompressionEstimator.estimate(1_000_000L, -1L, 0.7f))
    }

    @Test
    fun emptyFileGivesZero() {
        assertEquals(0L, CompressionEstimator.estimate(0L, 0L, 0.5f))
    }
}
