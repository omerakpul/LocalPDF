package com.omerakpul.localpdf.data.mapper

import com.omerakpul.localpdf.data.local.entity.PdfEntity
import com.omerakpul.localpdf.domain.model.Pdf
import com.omerakpul.localpdf.domain.model.PdfSourceType

fun PdfEntity.toDomain(): Pdf = Pdf(
    id = id,
    name = name,
    filePath = filePath,
    fileSize = fileSize,
    createdAt = createdAt,
    pageCount = pageCount,
    thumbnailPath = thumbnailPath,
    sourceType = try { PdfSourceType.valueOf(sourceType) } catch (e: Exception) { PdfSourceType.UNKNOWN }
)

fun Pdf.toEntity(): PdfEntity = PdfEntity(
    id = id,
    name = name,
    filePath = filePath,
    fileSize = fileSize,
    createdAt = createdAt,
    pageCount = pageCount,
    thumbnailPath = thumbnailPath,
    sourceType = sourceType.name
)

fun List<PdfEntity>.toDomainList(): List<Pdf> = map { it.toDomain() }

fun List<Pdf>.toEntityList(): List<PdfEntity> = map { it.toEntity() }
