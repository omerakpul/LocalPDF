package com.omerakpul.localpdf.domain.usecase

import android.net.Uri
import com.omerakpul.localpdf.data.service.PdfService
import java.io.File
import javax.inject.Inject

class CreatePdfFromPhotosUseCase @Inject constructor(
    private val pdfService: PdfService
) {
    suspend operator fun invoke(photos: List<Uri>) : File {
        return pdfService.createPdfFromPhotos(photos)
    }
}

