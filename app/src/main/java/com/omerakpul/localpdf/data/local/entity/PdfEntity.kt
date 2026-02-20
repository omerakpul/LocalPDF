package com.omerakpul.localpdf.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pdfs")
data class PdfEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val filePath: String,
    val fileSize: Long,
    val createdAt: Long,
    val pageCount: Int? = null,
    val thumbnailPath: String? = null,
    val sourceType: String = "UNKNOWN"
)
