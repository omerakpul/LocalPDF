package com.omerakpul.localpdf.domain.usecase

import android.net.Uri
import com.omerakpul.localpdf.data.service.PdfService
import java.io.File
import javax.inject.Inject

class CompressPdfUseCase @Inject constructor(
    private val pdfService: PdfService
) {
    suspend operator fun invoke(pdfUri: Uri, quality: Float = 0.5f): File {
        return pdfService.compressPdf(pdfUri, quality)
    }
}

