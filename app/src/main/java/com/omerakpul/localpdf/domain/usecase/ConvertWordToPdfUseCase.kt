package com.omerakpul.localpdf.domain.usecase

import android.net.Uri
import com.omerakpul.localpdf.data.service.PdfService
import java.io.File
import javax.inject.Inject

class ConvertWordToPdfUseCase @Inject constructor(
    private val pdfService: PdfService
) {
    suspend operator fun invoke(wordUri: Uri): File {
        return pdfService.convertWordToPdf(wordUri)
    }
}

