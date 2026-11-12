package com.omerakpul.localpdf.data.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDFont
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.PDResources
import com.tom_roush.pdfbox.pdmodel.graphics.form.PDFormXObject
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
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

    private companion object {
        // Used only when no system Unicode font is available (Helvetica is WinAnsi-only)
        val TURKISH_FALLBACK = mapOf(
            'ğ' to 'g', 'Ğ' to 'G', 'ş' to 's', 'Ş' to 'S',
            'ı' to 'i', 'İ' to 'I'
        )
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Dosya açılamadı")
        val file = File.createTempFile("temp_", ".pdf", context.cacheDir)
        try {
            inputStream.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            file.delete()
            throw e
        }
        return file
    }

    /**
     * Decodes an image with downsampling (to avoid OOM on large photos)
     * and applies the EXIF orientation so gallery photos are not rotated.
     */
    private fun uriToBitmap(uri: Uri, maxDimension: Int = 3000): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / sample > maxDimension || bounds.outHeight / sample > maxDimension) {
            sample *= 2
        }

        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val decoded = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: throw Exception("Bitmap oluşturulamadı")

        val orientation = try {
            context.contentResolver.openInputStream(uri)?.use {
                ExifInterface(it).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (e: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (degrees == 0f) return decoded
        val rotated = Bitmap.createBitmap(
            decoded, 0, 0, decoded.width, decoded.height,
            Matrix().apply { postRotate(degrees) }, true
        )
        if (rotated !== decoded) decoded.recycle()
        return rotated
    }

    private fun bitmapToJpeg(bitmap: Bitmap, quality: Int): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
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
                                newDocument.importPage(page)
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

    /**
     * Compresses a PDF by recompressing and downscaling its embedded images.
     * Text, vectors and fonts are untouched, so text stays selectable/searchable.
     * If the result is not smaller than the source, the original is returned unchanged.
     */
    suspend fun compressPdf(pdfUri: Uri, quality: Float): File = withContext(Dispatchers.IO) {
        val sourceFile = uriToFile(pdfUri)
        val document = PDDocument.load(sourceFile)
        val outputFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "compressed_${System.currentTimeMillis()}.pdf"
        )

        try {
            val q = quality.coerceIn(0.1f, 1f)
            val jpegQuality = (q * 100).toInt().coerceIn(30, 90)
            val scale = (0.25f + q * 0.75f).coerceAtMost(1f)

            val replaced = HashMap<Any, PDImageXObject>()
            val visited = HashSet<Any>()

            fun recompress(image: PDImageXObject): PDImageXObject? {
                if (image.isStencil || image.softMask != null || image.mask != null) return null
                val key = image.cosObject
                if (replaced.containsKey(key)) return replaced[key]

                val oldSize = image.stream.length
                var bitmap: Bitmap? = null
                var scaled: Bitmap? = null
                return try {
                    bitmap = image.image
                    scaled = if (scale < 1f && bitmap.width > 800) {
                        Bitmap.createScaledBitmap(
                            bitmap,
                            (bitmap.width * scale).toInt().coerceAtLeast(1),
                            (bitmap.height * scale).toInt().coerceAtLeast(1),
                            true
                        )
                    } else bitmap
                    val jpeg = bitmapToJpeg(scaled, jpegQuality)
                    if (jpeg.size.toLong() >= oldSize) {
                        null
                    } else {
                        JPEGFactory.createFromByteArray(document, jpeg)
                    }
                } catch (e: Exception) {
                    null
                } catch (e: OutOfMemoryError) {
                    null
                } finally {
                    if (scaled != null && scaled !== bitmap) scaled.recycle()
                    bitmap?.recycle()
                }.also { replaced[key] = it ?: image }
            }

            fun processResources(resources: PDResources?) {
                if (resources == null) return
                for (name in resources.xObjectNames.toList()) {
                    when (val xObject = resources.getXObject(name)) {
                        is PDImageXObject -> {
                            val newImage = recompress(xObject)
                            if (newImage != null && newImage !== xObject) {
                                resources.put(name, newImage)
                            }
                        }
                        is PDFormXObject -> {
                            if (visited.add(xObject.cosObject)) processResources(xObject.resources)
                        }
                        else -> {}
                    }
                }
            }

            document.pages.forEach { processResources(it.resources) }
            document.save(outputFile)

            if (outputFile.length() >= sourceFile.length()) {
                sourceFile.copyTo(outputFile, overwrite = true)
            }
            outputFile
        } catch (e: Exception) {
            outputFile.delete()
            throw e
        } finally {
            document.close()
            sourceFile.delete()
        }
    }

    suspend fun createPdfFromPhotos(photos: List<Uri>): File = withContext(Dispatchers.IO) {
        val document = PDDocument()

        try {
            val margin = 20f
            photos.forEachIndexed { index, photoUri ->
                val bitmap = uriToBitmap(photoUri)
                try {
                    // Landscape photos get a landscape page
                    val pageSize = if (bitmap.width > bitmap.height) {
                        PDRectangle(PDRectangle.A4.height, PDRectangle.A4.width)
                    } else {
                        PDRectangle.A4
                    }
                    val page = PDPage(pageSize)
                    document.addPage(page)

                    val image = PDImageXObject.createFromByteArray(
                        document, bitmapToJpeg(bitmap, 90), "photo_$index"
                    )

                    // Fit inside the page keeping the aspect ratio, centered
                    val availW = pageSize.width - 2 * margin
                    val availH = pageSize.height - 2 * margin
                    val scale = minOf(availW / bitmap.width, availH / bitmap.height)
                    val drawW = bitmap.width * scale
                    val drawH = bitmap.height * scale
                    val x = (pageSize.width - drawW) / 2f
                    val y = (pageSize.height - drawH) / 2f

                    PDPageContentStream(document, page).use { contentStream ->
                        contentStream.drawImage(image, x, y, drawW, drawH)
                    }
                } finally {
                    bitmap.recycle()
                }
            }

            val outputFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "photos_${System.currentTimeMillis()}.pdf"
            )

            document.save(outputFile)
            outputFile
        } finally {
            document.close()
        }
    }

    suspend fun convertWordToPdf(wordUri: Uri): File = withContext(Dispatchers.IO) {
        val wordFile = uriToFile(wordUri)
        val document = PDDocument()

        try {
            val wordDocument = try {
                FileInputStream(wordFile).use { XWPFDocument(it) }
            } catch (e: Exception) {
                throw Exception("Sadece geçerli .docx dosyaları destekleniyor")
            }

            val unicodeFont = loadUnicodeFont(document)
            val font: PDFont = unicodeFont ?: PDType1Font.HELVETICA
            val renderable = HashMap<Char, Boolean>()
            val fontSize = 12f
            val leading = 15f
            val margin = 50f
            val maxWidth = PDRectangle.A4.width - 2 * margin

            fun sanitize(raw: String): String {
                val sb = StringBuilder()
                for (c in raw) {
                    val ch = when {
                        c == '\t' -> ' '
                        unicodeFont == null -> TURKISH_FALLBACK[c] ?: c
                        else -> c
                    }
                    val ok = renderable.getOrPut(ch) {
                        try { font.getStringWidth(ch.toString()); true } catch (e: Exception) { false }
                    }
                    sb.append(if (ok) ch else '?')
                }
                return sb.toString()
            }

            fun textWidth(text: String) = font.getStringWidth(text) / 1000f * fontSize

            // Break a paragraph into lines that fit the page width
            fun wrap(text: String): List<String> {
                val lines = mutableListOf<String>()
                var current = StringBuilder()
                for (word in text.split(' ')) {
                    var w = word
                    val candidate = if (current.isEmpty()) w else "$current $w"
                    if (textWidth(candidate) <= maxWidth) {
                        current = StringBuilder(candidate)
                        continue
                    }
                    if (current.isNotEmpty()) {
                        lines.add(current.toString())
                        current = StringBuilder()
                    }
                    // Word longer than a full line: split by characters
                    while (textWidth(w) > maxWidth && w.length > 1) {
                        var cut = w.length - 1
                        while (cut > 1 && textWidth(w.substring(0, cut)) > maxWidth) cut--
                        lines.add(w.substring(0, cut))
                        w = w.substring(cut)
                    }
                    current.append(w)
                }
                lines.add(current.toString())
                return lines
            }

            var page = PDPage(PDRectangle.A4)
            document.addPage(page)
            var contentStream = PDPageContentStream(document, page)
            var y = page.mediaBox.height - margin

            fun drawLine(line: String) {
                if (y < margin) {
                    contentStream.close()
                    page = PDPage(PDRectangle.A4)
                    document.addPage(page)
                    contentStream = PDPageContentStream(document, page)
                    y = page.mediaBox.height - margin
                }
                if (line.isNotEmpty()) {
                    contentStream.beginText()
                    contentStream.setFont(font, fontSize)
                    contentStream.newLineAtOffset(margin, y)
                    contentStream.showText(line)
                    contentStream.endText()
                }
                y -= leading
            }

            wordDocument.paragraphs.forEach { paragraph ->
                val text = paragraph.text
                if (text.isBlank()) {
                    drawLine("") // keep paragraph spacing
                } else {
                    text.split('\n', '\r', '\u000B').forEach { part ->
                        wrap(sanitize(part)).forEach { drawLine(it) }
                    }
                }
            }
            contentStream.close()
            wordDocument.close()

            val outputFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "word_${System.currentTimeMillis()}.pdf"
            )

            document.save(outputFile)
            outputFile
        } finally {
            document.close()
            wordFile.delete()
        }
    }

    /** Tries common system fonts that cover Turkish/Latin-extended glyphs. */
    private fun loadUnicodeFont(document: PDDocument): PDFont? {
        val candidates = listOf(
            "/system/fonts/Roboto-Regular.ttf",
            "/system/fonts/NotoSans-Regular.ttf",
            "/system/fonts/DroidSans.ttf"
        )
        for (path in candidates) {
            val file = File(path)
            if (!file.exists()) continue
            try {
                return PDType0Font.load(document, file)
            } catch (e: Exception) {
                // try next candidate
            }
        }
        return null
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
