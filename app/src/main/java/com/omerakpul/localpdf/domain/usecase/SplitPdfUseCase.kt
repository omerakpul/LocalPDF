package com.omerakpul.localpdf.domain.usecase

import android.net.Uri
import com.omerakpul.localpdf.data.service.PdfService
import java.io.File
import javax.inject.Inject

class SplitPdfUseCase @Inject constructor(
    private val pdfService: PdfService
) {
    suspend operator fun invoke(pdfUri: Uri, pageRanges: List<IntRange>) : List<File> {
        return pdfService.splitPdfs(pdfUri, pageRanges)
    }
}

