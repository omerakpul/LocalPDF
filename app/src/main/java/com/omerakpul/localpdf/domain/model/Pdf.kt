package com.omerakpul.localpdf.domain.model

data class Pdf(
    val id: Int = 0,
    val name: String,
    val filePath: String,
    val fileSize: Long,
    val createdAt: Long,
    val pageCount: Int? = null,
    val thumbnailPath: String? = null
)
