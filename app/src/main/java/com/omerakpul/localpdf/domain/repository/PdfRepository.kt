package com.omerakpul.localpdf.domain.repository

import com.omerakpul.localpdf.data.local.entity.PdfEntity
import kotlinx.coroutines.flow.Flow

interface PdfRepository {
    fun getAllPdfs(): Flow<List<PdfEntity>>
    suspend fun getPdfById(id: Int): PdfEntity?
    fun searchPdfs(query: String): Flow<List<PdfEntity>>
    suspend fun insertPdf(pdf: PdfEntity)
    suspend fun insertAllPdfs(pdfs: List<PdfEntity>) : List<Long>
    suspend fun updatePdf(pdf: PdfEntity)
    suspend fun deletePdfById(id: Int)
    suspend fun deleteAllPdfs()
}