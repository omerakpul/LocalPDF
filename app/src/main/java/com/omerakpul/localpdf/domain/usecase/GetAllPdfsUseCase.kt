package com.omerakpul.localpdf.domain.usecase

import com.omerakpul.localpdf.data.local.entity.PdfEntity
import com.omerakpul.localpdf.domain.repository.PdfRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllPdfsUseCase @Inject constructor(
    private val repository: PdfRepository
) {
    operator fun invoke(): Flow<List<PdfEntity>> {
        return repository.getAllPdfs()
    }
}
