package com.omerakpul.localpdf.domain.usecase

import com.omerakpul.localpdf.domain.model.Pdf
import com.omerakpul.localpdf.domain.repository.PdfRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchPdfsUseCase @Inject constructor(
    private val repository: PdfRepository
) {
    operator fun invoke(query: String): Flow<List<Pdf>> {
        return repository.searchPdfs(query)
    }
}
