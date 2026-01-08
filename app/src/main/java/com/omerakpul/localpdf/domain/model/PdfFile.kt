package com.omerakpul.localpdf.domain.model

import android.net.Uri
import java.util.UUID

/**
 * Represents a PDF file selected for merging.
 * This is a temporary/UI model, not persisted to database.
 */
data class PdfFile(
    val id: String = UUID.randomUUID().toString(),
    val uri: Uri,
    val name: String,
    val size: Long,
    val pageCount: Int
)
