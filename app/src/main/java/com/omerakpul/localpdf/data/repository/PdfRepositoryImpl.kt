package com.omerakpul.localpdf.data.repository

import com.omerakpul.localpdf.data.local.dao.PdfDao
import com.omerakpul.localpdf.data.mapper.toDomain
import com.omerakpul.localpdf.data.mapper.toDomainList
import com.omerakpul.localpdf.data.mapper.toEntity
import com.omerakpul.localpdf.data.mapper.toEntityList
import com.omerakpul.localpdf.domain.model.Pdf
import com.omerakpul.localpdf.domain.repository.PdfRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PdfRepositoryImpl @Inject constructor(
    private val pdfDao: PdfDao
) : PdfRepository {

    override fun getAllPdfs(): Flow<List<Pdf>> {
        return pdfDao.getAllPdfs().map { it.toDomainList() }
    }

    override suspend fun getPdfById(id: Int): Pdf? {
        return pdfDao.getPdfById(id)?.toDomain()
    }

    override fun searchPdfs(query: String): Flow<List<Pdf>> {
        return pdfDao.searchPdfs(query).map { it.toDomainList() }
    }

    override suspend fun insertPdf(pdf: Pdf) {
        pdfDao.insertPdf(pdf.toEntity())
    }

    override suspend fun insertAllPdfs(pdfs: List<Pdf>): List<Long> {
        return pdfDao.insertAllPdfs(pdfs.toEntityList())
    }

    override suspend fun updatePdf(pdf: Pdf) {
        pdfDao.updatePdf(pdf.toEntity())
    }

    override suspend fun deletePdfById(id: Int) {
        pdfDao.deletePdfById(id)
    }

    override suspend fun deleteAllPdfs() {
        pdfDao.deleteAllPdfs()
    }
}