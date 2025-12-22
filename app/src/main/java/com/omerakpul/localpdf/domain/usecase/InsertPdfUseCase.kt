package com.omerakpul.localpdf.domain.usecase

import com.omerakpul.localpdf.domain.model.Pdf
import com.omerakpul.localpdf.domain.repository.PdfRepository
import javax.inject.Inject

class InsertPdfUseCase @Inject constructor(
    private val repository: PdfRepository
) {
    suspend operator fun invoke(pdf: Pdf) {
        repository.insertPdf(pdf)
    }
}
