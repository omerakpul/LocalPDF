package com.omerakpul.localpdf.data.service

import android.content.Context
import com.omerakpul.localpdf.R
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.content.ContentValues
import android.graphics.pdf.PdfRenderer
import android.media.MediaScannerConnection
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import com.omerakpul.localpdf.domain.util.ImageExportNaming
import android.os.Environment
import com.omerakpul.localpdf.domain.util.CompressionEstimator
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.cos.COSDictionary
import com.tom_roush.pdfbox.cos.COSName
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
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission
import com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import com.tom_roush.pdfbox.rendering.PDFRenderer
import com.tom_roush.pdfbox.util.Matrix as PdfMatrix
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
            ?: throw Exception(context.getString(R.string.error_cannot_open_file))
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
        } ?: throw Exception(context.getString(R.string.error_cannot_open_file))

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

    /** Visits every image XObject of the document, including images inside form XObjects. */
    private fun walkImages(
        document: PDDocument,
        visit: (PDResources, COSName, PDImageXObject) -> Unit
    ) {
        val visitedForms = HashSet<Any>()
        fun walk(resources: PDResources?) {
            if (resources == null) return
            for (name in resources.xObjectNames.toList()) {
                when (val xObject = resources.getXObject(name)) {
                    is PDImageXObject -> visit(resources, name, xObject)
                    is PDFormXObject -> if (visitedForms.add(xObject.cosObject)) walk(xObject.resources)
                    else -> {}
                }
            }
        }
        document.pages.forEach { walk(it.resources) }
    }

    private fun isRecompressible(image: PDImageXObject): Boolean =
        !(image.isStencil || image.softMask != null || image.mask != null)

    /** Total bytes used by images that [compressPdf] is able to recompress. */
    suspend fun analyzeImageBytes(pdfUri: Uri): Long = withContext(Dispatchers.IO) {
        val sourceFile = uriToFile(pdfUri)
        val document = PDDocument.load(sourceFile)
        try {
            val seen = HashSet<Any>()
            var total = 0L
            walkImages(document) { _, _, image ->
                if (isRecompressible(image) && seen.add(image.cosObject)) {
                    total += image.stream.length
                }
            }
            total
        } finally {
            document.close()
            sourceFile.delete()
        }
    }

    /**
     * Compresses a PDF by recompressing and downscaling its embedded images.
     * Text, vectors and fonts are untouched, so text stays selectable/searchable.
     * If the result is not smaller than the source, the original is returned unchanged.
     *
     * @param onProgress called with a value between 0 and 1 as images are processed
     */
    suspend fun compressPdf(
        pdfUri: Uri,
        quality: Float,
        onProgress: (Float) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {
        val sourceFile = uriToFile(pdfUri)
        val document = PDDocument.load(sourceFile)
        val outputFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "compressed_${System.currentTimeMillis()}.pdf"
        )

        try {
            val q = quality.coerceIn(0.1f, 1f)
            val jpegQuality = (q * 100).toInt().coerceIn(30, 90)
            val scale = CompressionEstimator.imageScale(q)

            // First pass: how many distinct images will be processed (for progress)
            val countedImages = HashSet<Any>()
            walkImages(document) { _, _, image ->
                if (isRecompressible(image)) countedImages.add(image.cosObject)
            }
            val total = countedImages.size.coerceAtLeast(1)
            var done = 0
            onProgress(0f)

            val replaced = HashMap<Any, PDImageXObject>()

            fun recompress(image: PDImageXObject): PDImageXObject? {
                val key = image.cosObject
                if (replaced.containsKey(key)) return replaced[key]

                val oldSize = image.stream.length
                var bitmap: Bitmap? = null
                var scaled: Bitmap? = null
                val result: PDImageXObject? = try {
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
                    if (jpeg.size.toLong() >= oldSize) null else JPEGFactory.createFromByteArray(document, jpeg)
                } catch (e: Exception) {
                    null
                } catch (e: OutOfMemoryError) {
                    null
                } finally {
                    if (scaled != null && scaled !== bitmap) scaled.recycle()
                    bitmap?.recycle()
                }
                replaced[key] = result ?: image
                done++
                onProgress((done.toFloat() / total).coerceAtMost(1f))
                return result
            }

            walkImages(document) { resources, name, image ->
                if (isRecompressible(image)) {
                    val newImage = recompress(image)
                    if (newImage != null) resources.put(name, newImage)
                }
            }

            document.save(outputFile)

            if (outputFile.length() >= sourceFile.length()) {
                sourceFile.copyTo(outputFile, overwrite = true)
            }
            onProgress(1f)
            outputFile
        } catch (e: Exception) {
            outputFile.delete()
            throw e
        } finally {
            document.close()
            sourceFile.delete()
        }
    }

    suspend fun deletePages(pdfUri: Uri, pagesToDelete: List<Int>): File = withContext(Dispatchers.IO) {
        val sourceFile = uriToFile(pdfUri)
        val document = PDDocument.load(sourceFile)
        try {
            // Delete in reverse order to avoid index shifting
            pagesToDelete.sortedDescending().forEach { page ->
                val pageIndex = page - 1
                if (pageIndex in 0 until document.numberOfPages) {
                    document.removePage(pageIndex)
                }
            }

            val outputFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "edited_${System.currentTimeMillis()}.pdf"
            )
            document.save(outputFile)
            outputFile
        } finally {
            document.close()
            sourceFile.delete()
        }
    }

    /** Rebuilds the page tree in [newOrder] (1-based original page numbers). */
    suspend fun reorderPages(pdfUri: Uri, newOrder: List<Int>): File = withContext(Dispatchers.IO) {
        val sourceFile = uriToFile(pdfUri)
        val document = PDDocument.load(sourceFile)
        try {
            val count = document.numberOfPages
            val pages = (0 until count).map { document.getPage(it) }
            val order = newOrder.filter { it in 1..count }
            if (order.isEmpty()) throw IllegalArgumentException("Empty page order")

            for (i in count - 1 downTo 0) document.pages.remove(i)
            order.forEach { document.pages.add(pages[it - 1]) }

            val outputFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "reordered_${System.currentTimeMillis()}.pdf"
            )
            document.save(outputFile)
            outputFile
        } finally {
            document.close()
            sourceFile.delete()
        }
    }

    /** Inserts a copy of every selected (1-based) page directly after the original. */
    suspend fun duplicatePages(pdfUri: Uri, pagesToDuplicate: List<Int>): File =
        withContext(Dispatchers.IO) {
            val sourceFile = uriToFile(pdfUri)
            val document = PDDocument.load(sourceFile)
            try {
                // Work from the back so earlier indices stay valid
                pagesToDuplicate.sortedDescending().forEach { number ->
                    val index = number - 1
                    if (index in 0 until document.numberOfPages) {
                        val original = document.getPage(index)
                        val copy = PDPage(COSDictionary(original.cosObject))
                        document.pages.insertAfter(copy, original)
                    }
                }

                val outputFile = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    "edited_${System.currentTimeMillis()}.pdf"
                )
                document.save(outputFile)
                outputFile
            } finally {
                document.close()
                sourceFile.delete()
            }
        }

    /** Encrypts the PDF (128-bit) so it asks for [password] when opened. */
    suspend fun protectPdf(pdfUri: Uri, password: String): File = withContext(Dispatchers.IO) {
        val sourceFile = uriToFile(pdfUri)
        try {
            val document = try {
                PDDocument.load(sourceFile)
            } catch (e: InvalidPasswordException) {
                throw Exception(context.getString(R.string.error_pdf_already_encrypted))
            }
            document.use {
                val policy = StandardProtectionPolicy(password, password, AccessPermission())
                policy.encryptionKeyLength = 128
                it.protect(policy)

                val outputFile = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    "protected_${System.currentTimeMillis()}.pdf"
                )
                it.save(outputFile)
                outputFile
            }
        } finally {
            sourceFile.delete()
        }
    }

    /** Removes the password protection from a PDF, given its current [password]. */
    suspend fun unlockPdf(pdfUri: Uri, password: String): File = withContext(Dispatchers.IO) {
        val sourceFile = uriToFile(pdfUri)
        try {
            val document = try {
                PDDocument.load(sourceFile, password)
            } catch (e: InvalidPasswordException) {
                throw Exception(context.getString(R.string.error_wrong_password))
            }
            document.use {
                it.isAllSecurityToBeRemoved = true

                val outputFile = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    "unlocked_${System.currentTimeMillis()}.pdf"
                )
                it.save(outputFile)
                outputFile
            }
        } finally {
            sourceFile.delete()
        }
    }

    suspend fun rotatePages(pdfUri: Uri, pagesToRotate: List<Int>, angle: Int): File =
        withContext(Dispatchers.IO) {
            val sourceFile = uriToFile(pdfUri)
            val document = PDDocument.load(sourceFile)
            try {
                pagesToRotate.forEach { page ->
                    val pageIndex = page - 1
                    if (pageIndex in 0 until document.numberOfPages) {
                        val pdfPage = document.getPage(pageIndex)
                        pdfPage.rotation = ((pdfPage.rotation + angle) % 360 + 360) % 360
                    }
                }

                val outputFile = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    "edited_${System.currentTimeMillis()}.pdf"
                )
                document.save(outputFile)
                outputFile
            } finally {
                document.close()
                sourceFile.delete()
            }
        }

    /**
     * Draws a PNG signature on the given (1-based) page.
     * [xFraction]/[yFraction] are the signature's top-left corner and [widthFraction] its width,
     * all relative to the page as displayed (rotation applied). The signature keeps a 2:1 ratio.
     */
    suspend fun addSignature(
        pdfUri: Uri,
        pageNumber: Int,
        signaturePng: ByteArray,
        xFraction: Float,
        yFraction: Float,
        widthFraction: Float
    ): File = withContext(Dispatchers.IO) {
        val sourceFile = uriToFile(pdfUri)
        val document = PDDocument.load(sourceFile)
        try {
            val pageIndex = (pageNumber - 1).coerceIn(0, document.numberOfPages - 1)
            val page = document.getPage(pageIndex)
            val sigImage = PDImageXObject.createFromByteArray(document, signaturePng, "signature")

            val box = page.cropBox
            val rotation = ((page.rotation % 360) + 360) % 360
            val userW = box.width
            val userH = box.height
            val dispW = if (rotation == 90 || rotation == 270) userH else userW
            val dispH = if (rotation == 90 || rotation == 270) userW else userH

            // Signature rectangle in display space (origin top-left, y down)
            val dw = (widthFraction * dispW).coerceAtLeast(1f)
            val dh = dw / 2f
            val left = (xFraction * dispW).coerceIn(0f, (dispW - dw).coerceAtLeast(0f))
            val top = (yFraction * dispH).coerceIn(0f, (dispH - dh).coerceAtLeast(0f))

            val m = SignaturePlacement.matrixFor(
                rotation, userW, userH, left, top, dw, dh, box.lowerLeftX, box.lowerLeftY
            )
            val matrix = PdfMatrix(m.a, m.b, m.c, m.d, m.e, m.f)

            PDPageContentStream(
                document, page, PDPageContentStream.AppendMode.APPEND, true, true
            ).use { it.drawImage(sigImage, matrix) }

            val outputFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "signed_${System.currentTimeMillis()}.pdf"
            )
            document.save(outputFile)
            outputFile
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
                throw Exception(context.getString(R.string.error_invalid_docx))
            }

            wordDocument.use {
                WordToPdfRenderer(document, loadFonts(document)).render(it)
            }

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

    /**
     * Loads Unicode system fonts (regular/bold/italic/bold-italic) so Turkish characters and
     * text styles work. Falls back to the standard Helvetica family when none is available.
     */
    private fun loadFonts(document: PDDocument): WordToPdfRenderer.Fonts {
        fun load(vararg names: String): PDFont? {
            for (name in names) {
                val file = File("/system/fonts/$name")
                if (!file.exists()) continue
                try {
                    return PDType0Font.load(document, file)
                } catch (e: Exception) {
                    // try the next candidate
                }
            }
            return null
        }

        val regular = load("Roboto-Regular.ttf", "NotoSans-Regular.ttf", "DroidSans.ttf")
            ?: return WordToPdfRenderer.Fonts(
                PDType1Font.HELVETICA,
                PDType1Font.HELVETICA_BOLD,
                PDType1Font.HELVETICA_OBLIQUE,
                PDType1Font.HELVETICA_BOLD_OBLIQUE,
                isUnicode = false
            )

        val bold = load("Roboto-Bold.ttf", "NotoSans-Bold.ttf", "DroidSans-Bold.ttf") ?: regular
        val italic = load("Roboto-Italic.ttf", "NotoSans-Italic.ttf") ?: regular
        val boldItalic = load("Roboto-BoldItalic.ttf", "NotoSans-BoldItalic.ttf") ?: bold
        return WordToPdfRenderer.Fonts(regular, bold, italic, boldItalic, isUnicode = true)
    }

    /**
     * Renders every page of a PDF to a PNG and stores it in Pictures/LocalPDF.
     * @return the number of images saved
     */
    suspend fun exportPdfToImages(
        pdfUri: Uri,
        baseName: String,
        onProgress: (Float) -> Unit = {}
    ): Int = withContext(Dispatchers.IO) {
        val sourceFile = uriToFile(pdfUri)
        try {
            val pfd = ParcelFileDescriptor.open(sourceFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = try {
                PdfRenderer(pfd)
            } catch (e: SecurityException) {
                pfd.close()
                throw Exception(context.getString(R.string.error_pdf_already_encrypted))
            }
            renderer.use { r ->
                val total = r.pageCount
                for (index in 0 until total) {
                    r.openPage(index).use { page ->
                        // ~150 DPI, capped so very large pages do not exhaust memory
                        var scale = 150f / 72f
                        val maxSide = maxOf(page.width, page.height) * scale
                        if (maxSide > 3000f) scale *= 3000f / maxSide
                        val bitmap = Bitmap.createBitmap(
                            (page.width * scale).toInt().coerceAtLeast(1),
                            (page.height * scale).toInt().coerceAtLeast(1),
                            Bitmap.Config.ARGB_8888
                        )
                        try {
                            bitmap.eraseColor(android.graphics.Color.WHITE)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            saveImage(bitmap, ImageExportNaming.pageFileName(baseName, index + 1, total))
                        } finally {
                            bitmap.recycle()
                        }
                    }
                    onProgress((index + 1).toFloat() / total)
                }
                total
            }
        } finally {
            sourceFile.delete()
        }
    }

    /** Saves a bitmap as PNG into Pictures/LocalPDF (MediaStore on Android 10+). */
    private fun saveImage(bitmap: Bitmap, fileName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/LocalPDF")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: throw java.io.IOException(context.getString(R.string.error_save_failed))
            try {
                resolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                } ?: throw java.io.IOException(context.getString(R.string.error_save_failed))
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
                throw e
            }
        } else {
            // Android 8-9: legacy storage (needs WRITE_EXTERNAL_STORAGE)
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "LocalPDF"
            ).apply { mkdirs() }
            val file = File(dir, fileName)
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("image/png"), null)
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
