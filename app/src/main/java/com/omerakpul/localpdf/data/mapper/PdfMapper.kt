package com.omerakpul.localpdf.data.mapper

import com.omerakpul.localpdf.data.local.entity.PdfEntity
import com.omerakpul.localpdf.domain.model.Pdf

fun PdfEntity.toDomain(): Pdf = Pdf(
    id = id,
    name = name,
    filePath = filePath,
    fileSize = fileSize,
    createdAt = createdAt,
    pageCount = pageCount,
    thumbnailPath = thumbnailPath
)

fun Pdf.toEntity(): PdfEntity = PdfEntity(
    id = id,
    name = name,
    filePath = filePath,
    fileSize = fileSize,
    createdAt = createdAt,
    pageCount = pageCount,
    thumbnailPath = thumbnailPath
)

fun List<PdfEntity>.toDomainList(): List<Pdf> = map { it.toDomain() }

fun List<Pdf>.toEntityList(): List<PdfEntity> = map { it.toEntity() }
