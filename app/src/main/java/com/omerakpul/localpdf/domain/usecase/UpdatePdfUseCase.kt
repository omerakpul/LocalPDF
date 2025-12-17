package com.omerakpul.localpdf.domain.usecase

import com.omerakpul.localpdf.data.local.entity.PdfEntity
import com.omerakpul.localpdf.domain.repository.PdfRepository
import javax.inject.Inject

class UpdatePdfUseCase @Inject constructor(
    private val repository: PdfRepository
) {
    suspend operator fun invoke(pdf: PdfEntity) {
        repository.updatePdf(pdf)
    }
}
