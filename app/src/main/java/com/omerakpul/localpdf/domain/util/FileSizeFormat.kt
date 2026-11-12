package com.omerakpul.localpdf.domain.util

import java.util.Locale

/** One place for turning a byte count into "812 B", "1.4 KB" or "2.3 MB". */
fun formatFileSize(bytes: Long, locale: Locale = Locale.getDefault()): String {
    val size = bytes.coerceAtLeast(0)
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> String.format(locale, "%.1f KB", size / 1024.0)
        else -> String.format(locale, "%.1f MB", size / (1024.0 * 1024.0))
    }
}
