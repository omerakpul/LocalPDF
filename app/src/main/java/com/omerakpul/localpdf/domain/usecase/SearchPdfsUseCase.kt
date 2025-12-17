package com.omerakpul.localpdf.domain.usecase

import com.omerakpul.localpdf.data.local.entity.PdfEntity
import com.omerakpul.localpdf.domain.repository.PdfRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchPdfsUseCase @Inject constructor(
    private val repository: PdfRepository
) {
    operator fun invoke(query: String): Flow<List<PdfEntity>> {
        return repository.searchPdfs(query)
    }
}
