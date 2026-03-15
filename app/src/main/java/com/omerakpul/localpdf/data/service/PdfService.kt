package com.omerakpul.localpdf.data.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import com.tom_roush.pdfbox.rendering.PDFRenderer
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfService @Inject constructor(
    private val context: Context
) {
    init {
        PDFBoxResourceLoader.init(context)
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File.createTempFile("temp_", ".pdf", context.cacheDir)
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    private fun uriToBitmap(uri: Uri): Bitmap {
        val inputStream = context.contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream) ?: throw Exception("Bitmap oluşturulamadı")
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    suspend fun mergePdfs(pdfs: List<Uri>): File = withContext(Dispatchers.IO) {
        val mergedDocument = PDDocument()
        val sourceDocuments = mutableListOf<PDDocument>()
        val tempFiles = mutableListOf<File>()

        try {
            // Load all documents first and keep them open
            pdfs.forEach { uri ->
                val file = uriToFile(uri)
                tempFiles.add(file)
                val sourceDocument = PDDocument.load(file)
                sourceDocuments.add(sourceDocument)
            }

            // Import pages from all source documents
            sourceDocuments.forEach { sourceDocument ->
                for (i in 0 until sourceDocument.numberOfPages) {
                    mergedDocument.importPage(sourceDocument.getPage(i))
                }
            }

            val outputFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "merged_${System.currentTimeMillis()}.pdf"
            )

            mergedDocument.save(outputFile)
            outputFile
        } finally {
            // Close all documents after saving
            sourceDocuments.forEach { it.close() }
            mergedDocument.close()
            tempFiles.forEach { it.delete() }
        }
    }

    suspend fun splitPdfs(pdfUri: Uri, pageRanges: List<IntRange>): List<File> =
        withContext(Dispatchers.IO) {
            val sourceFile = uriToFile(pdfUri)
            val sourceDocument = PDDocument.load(sourceFile)

            try {
                val outputFiles = mutableListOf<File>()

                pageRanges.forEachIndexed { index, range ->
                    val newDocument = PDDocument()

                    try {
                        for (pageNum in range) {
                            val pageIndex = pageNum - 1 // 1.sayfa = index 0
                            if (pageIndex in 0 until sourceDocument.numberOfPages) {
                                val page = sourceDocument.getPage(pageIndex)
                                newDocument.addPage(page)
                            }
                        }
                        val outputFile = File(
                            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                            "split_${System.currentTimeMillis()}_$index.pdf"
                        )
                        newDocument.save(outputFile)
                        outputFiles.add(outputFile)
                    } finally {
                        newDocument.close()
                    }
                }

                outputFiles
            } finally {
                sourceDocument.close()
                sourceFile.delete()
            }
        }

    suspend fun compressPdf(pdfUri: Uri, quality: Float): File = withContext(Dispatchers.IO) {
        val sourceFile = uriToFile(pdfUri)
        val sourceDocument = PDDocument.load(sourceFile)

        try {
            val compressedDocument = PDDocument()
            val renderer = PDFRenderer(sourceDocument)
            sourceDocument.pages.forEachIndexed { index, page ->
                // sayfayı goruntuye cevir
                val bitmap = renderer.renderImageWithDPI(index, 150f * quality)

                //yeni sayfa olustur
                val newPage = PDPage(page.mediaBox)
                compressedDocument.addPage(newPage)

                //goruntuyu pdf'e ekle
                val contentStream = PDPageContentStream(compressedDocument, newPage)
                val image = PDImageXObject.createFromByteArray(
                    compressedDocument,
                    bitmapToByteArray(bitmap),
                    "page_$index"
                )
                contentStream.drawImage(
                    image,
                    0f,
                    0f,
                    newPage.mediaBox.width,
                    newPage.mediaBox.height
                )
                contentStream.close()
                bitmap.recycle()
            }
            val outputFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "compressed_${System.currentTimeMillis()}.pdf"
            )

            compressedDocument.save(outputFile)
            compressedDocument.close()
            sourceDocument.close()
            sourceFile.delete()

            outputFile
        } catch (e: Exception) {
            sourceDocument.close()
            sourceFile.delete()
            throw e
        }

    }

    suspend fun createPdfFromPhotos(photos: List<Uri>): File = withContext(Dispatchers.IO) {
        val document = PDDocument()

        try {
            photos.forEach { photoUri ->
                val bitmap = uriToBitmap(photoUri)
                val page = PDPage(PDRectangle.A4)
                document.addPage(page)

                val contentStream = PDPageContentStream(document, page)
                val image =
                    PDImageXObject.createFromByteArray(document, bitmapToByteArray(bitmap), "photo")

                contentStream.drawImage(
                    image,
                    0f,
                    0f,
                    page.mediaBox.width,
                    page.mediaBox.height
                )

                contentStream.close()
                bitmap.recycle()

            }

            val outputFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "photos_${System.currentTimeMillis()}.pdf"
            )

            document.save(outputFile)
            document.close()

            outputFile

        } catch (e: Exception) {
            document.close()
            throw e
        }
    }

    suspend fun convertWordToPdf(wordUri: Uri): File = withContext(Dispatchers.IO) {
        val wordFile = uriToFile(wordUri)
        val document = PDDocument()

        try {
            if (!wordFile.name.endsWith(".docx")) {
                throw Exception("Sadece .docx dosyaları destekleniyor")
            }

            val wordDocument = XWPFDocument(FileInputStream(wordFile))

            val page = PDPage(PDRectangle.A4)
            document.addPage(page)

            var contentStream = PDPageContentStream(document, page)
            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA, 12f)
            contentStream.newLineAtOffset(50f, page.mediaBox.height - 50f)
            var yPosition = page.mediaBox.height - 50f

            wordDocument.paragraphs.forEach { paragraph ->
                val text = paragraph.text
                if (text.isNotBlank()) {
                    if (yPosition < 50f) {
                        contentStream.endText()
                        contentStream.close()

                        val newPage = PDPage(PDRectangle.A4)
                        document.addPage(newPage)
                        val newContentStream = PDPageContentStream(document, newPage)
                        newContentStream.beginText()
                        newContentStream.setFont(PDType1Font.HELVETICA, 12f)
                        newContentStream.newLineAtOffset(50f, newPage.mediaBox.height - 50f)
                        yPosition = newPage.mediaBox.height - 50f
                        contentStream = newContentStream
                    }

                    contentStream.showText(text)
                    yPosition -= 15f
                    contentStream.newLineAtOffset(0f, -15f)
                }
            }
            contentStream.endText()
            contentStream.close()
            wordDocument.close()

            val outputFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "word_${System.currentTimeMillis()}.pdf"
            )

            document.save(outputFile)
            document.close()
            wordFile.delete()

            outputFile
        } catch (e: Exception) {
            document.close()
            wordFile.delete()
            throw e
        }
    }

    suspend fun convertPdfToWord(pdfUri: Uri): File = withContext(Dispatchers.IO) {
        val pdfFile = uriToFile(pdfUri)
        val pdfDocument = PDDocument.load(pdfFile)

        try {
            val wordDocument = XWPFDocument()
            val stripper = PDFTextStripper()
            stripper.sortByPosition = true

            for (i in 1..pdfDocument.numberOfPages) {
                stripper.startPage = i
                stripper.endPage = i
                
                val text = try {
                    stripper.getText(pdfDocument)
                } catch (e: Exception) {
                    ""
                }

                if (text.isNotBlank()) {
                    // Clean text from non-printable characters that might cause native issues
                    val cleanedText = text.replace(Regex("[^\\p{Print}\\r\\n\\t]"), "").trim()
                    if (cleanedText.isNotEmpty()) {
                        val paragraph = wordDocument.createParagraph()
                        val run = paragraph.createRun()
                        run.setText(cleanedText)
                    }
                }
            }

            val outputFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "pdf_to_word_${System.currentTimeMillis()}.docx"
            )

            wordDocument.write(FileOutputStream(outputFile))
            wordDocument.close()
            pdfDocument.close()
            pdfFile.delete()

            outputFile
        } catch (e: Exception) {
            pdfDocument.close()
            pdfFile.delete()
            throw e
        }
    }
}
