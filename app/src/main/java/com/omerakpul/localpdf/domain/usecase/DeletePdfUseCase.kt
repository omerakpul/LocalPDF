package com.omerakpul.localpdf.domain.usecase

import com.omerakpul.localpdf.domain.repository.PdfRepository
import javax.inject.Inject

class DeletePdfUseCase @Inject constructor(
    private val repository: PdfRepository
) {
    suspend operator fun invoke(id: Int) {
        repository.deletePdfById(id)
    }
}
