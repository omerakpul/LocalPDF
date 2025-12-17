package com.omerakpul.localpdf.data.repository

import com.omerakpul.localpdf.data.local.dao.PdfDao
import com.omerakpul.localpdf.data.local.entity.PdfEntity
import com.omerakpul.localpdf.domain.repository.PdfRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PdfRepositoryImpl @Inject constructor(
    private val pdfDao : PdfDao
) : PdfRepository {

    override fun getAllPdfs(): Flow<List<PdfEntity>> {
        return pdfDao.getAllPdfs()
    }

    override suspend fun getPdfById(id: Int): PdfEntity? {
        return pdfDao.getPdfById(id)
    }

    override fun searchPdfs(query: String): Flow<List<PdfEntity>> {
        return pdfDao.searchPdfs(query)
    }

    override suspend fun insertPdf(pdf: PdfEntity) {
        pdfDao.insertPdf(pdf)
    }

    override suspend fun insertAllPdfs(pdfs: List<PdfEntity>): List<Long> {
        return pdfDao.insertAllPdfs(pdfs)
    }

    override suspend fun updatePdf(pdf: PdfEntity) {
        pdfDao.updatePdf(pdf)
    }

    override suspend fun deletePdfById(id: Int) {
        pdfDao.deletePdfById(id)
    }

    override suspend fun deleteAllPdfs() {
        pdfDao.deleteAllPdfs()
    }
}