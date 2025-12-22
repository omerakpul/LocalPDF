package com.omerakpul.localpdf.domain.repository

import com.omerakpul.localpdf.domain.model.Pdf
import kotlinx.coroutines.flow.Flow

interface PdfRepository {
    fun getAllPdfs(): Flow<List<Pdf>>
    suspend fun getPdfById(id: Int): Pdf?
    fun searchPdfs(query: String): Flow<List<Pdf>>
    suspend fun insertPdf(pdf: Pdf)
    suspend fun insertAllPdfs(pdfs: List<Pdf>): List<Long>
    suspend fun updatePdf(pdf: Pdf)
    suspend fun deletePdfById(id: Int)
    suspend fun deleteAllPdfs()
}