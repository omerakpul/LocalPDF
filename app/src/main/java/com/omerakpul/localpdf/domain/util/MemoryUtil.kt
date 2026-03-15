package com.omerakpul.localpdf.domain.util

import android.app.ActivityManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryUtil @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Calculates the safe maximum file size (in bytes) that this device can handle
     * based on its total physical RAM.
     *
     * - < 2GB RAM: Max 20 MB file
     * - 2GB - 3GB RAM: Max 40 MB file
     * - 3GB - 6GB RAM: Max 80 MB file
     * - > 6GB RAM: Max 150 MB file
     */
    fun getMaxAllowedFileSizeBytes(): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalMemoryGB = memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0)

        // Return limits in bytes
        return when {
            totalMemoryGB < 2.0 -> 20L * 1024 * 1024     // 20 MB
            totalMemoryGB < 3.5 -> 40L * 1024 * 1024     // 40 MB
            totalMemoryGB < 6.5 -> 80L * 1024 * 1024     // 80 MB
            else -> 150L * 1024 * 1024                  // 150 MB
        }
    }

    /**
     * Formats bytes into a human-readable string (e.g., "150 MB")
     */
    fun formatSize(sizeInBytes: Long): String {
        return "${sizeInBytes / (1024 * 1024)} MB"
    }
}
