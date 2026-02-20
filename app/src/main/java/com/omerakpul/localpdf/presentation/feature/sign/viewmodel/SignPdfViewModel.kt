package com.omerakpul.localpdf.presentation.feature.sign.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import com.omerakpul.localpdf.presentation.feature.sign.ui.SignPdfUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SignPdfViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignPdfUiState())
    val uiState: StateFlow<SignPdfUiState> = _uiState.asStateFlow()

    fun selectPdf(uri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val name = getFileName(uri) ?: "unknown.pdf"
                val pageCount = getPageCount(uri)
                _uiState.update {
                    it.copy(
                        selectedPdf = uri,
                        pdfName = name,
                        pageCount = pageCount,
                        signaturePage = pageCount // default: last page
                    )
                }
            }
        }
    }

    fun clearSelection() {
        _uiState.update { SignPdfUiState() }
    }

    fun addPointToStroke(point: Offset) {
        _uiState.update { current ->
            current.copy(currentStroke = current.currentStroke + point)
        }
    }

    fun finishStroke() {
        _uiState.update { current ->
            if (current.currentStroke.isNotEmpty()) {
                current.copy(
                    signaturePoints = current.signaturePoints + listOf(current.currentStroke),
                    currentStroke = emptyList()
                )
            } else current
        }
    }

    fun clearSignature() {
        _uiState.update { it.copy(signaturePoints = emptyList(), currentStroke = emptyList()) }
    }

    fun setSignaturePage(page: Int) {
        _uiState.update { it.copy(signaturePage = page.coerceIn(1, it.pageCount)) }
    }

    fun signPdf() {
        val pdfUri = _uiState.value.selectedPdf ?: return
        if (!_uiState.value.hasSignature) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val resultFile = withContext(Dispatchers.IO) {
                    addSignatureToPdf(pdfUri)
                }
                val resultUri = Uri.fromFile(resultFile)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        signSuccess = true,
                        signedPdfUri = resultUri
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Signing failed"
                    )
                }
            }
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(signSuccess = false, signedPdfUri = null) }
    }

    private fun addSignatureToPdf(pdfUri: Uri): File {
        val state = _uiState.value

        // Create signature bitmap
        val sigBitmap = createSignatureBitmap(state.signaturePoints, 400, 200)

        // Convert to byte array
        val baos = ByteArrayOutputStream()
        sigBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val sigBytes = baos.toByteArray()
        sigBitmap.recycle()

        // Copy source PDF
        val tempFile = File.createTempFile("temp_", ".pdf", context.cacheDir)
        context.contentResolver.openInputStream(pdfUri)?.use { input ->
            tempFile.outputStream().use { output -> input.copyTo(output) }
        }

        val document = PDDocument.load(tempFile)
        try {
            val pageIndex = (state.signaturePage - 1).coerceIn(0, document.numberOfPages - 1)
            val page = document.getPage(pageIndex)

            val sigImage = PDImageXObject.createFromByteArray(document, sigBytes, "signature")

            val sigWidth = 150f
            val sigHeight = 75f
            val xPos = page.mediaBox.width - sigWidth - 50f
            val yPos = 50f

            val contentStream = PDPageContentStream(
                document, page, PDPageContentStream.AppendMode.APPEND, true, true
            )
            contentStream.drawImage(sigImage, xPos, yPos, sigWidth, sigHeight)
            contentStream.close()

            val outputFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "signed_${System.currentTimeMillis()}.pdf"
            )
            document.save(outputFile)
            return outputFile
        } finally {
            document.close()
            tempFile.delete()
        }
    }

    private fun createSignatureBitmap(strokes: List<List<Offset>>, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint().apply {
            color = android.graphics.Color.BLACK
            strokeWidth = 4f
            style = Paint.Style.STROKE
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        // Find bounds of signature points to scale
        if (strokes.isEmpty()) return bitmap

        val allPoints = strokes.flatten()
        val minX = allPoints.minOf { it.x }
        val maxX = allPoints.maxOf { it.x }
        val minY = allPoints.minOf { it.y }
        val maxY = allPoints.maxOf { it.y }

        val sigWidth = (maxX - minX).coerceAtLeast(1f)
        val sigHeight = (maxY - minY).coerceAtLeast(1f)

        val scaleX = (width - 20f) / sigWidth
        val scaleY = (height - 20f) / sigHeight
        val scale = minOf(scaleX, scaleY)

        val offsetX = (width - sigWidth * scale) / 2f
        val offsetY = (height - sigHeight * scale) / 2f

        strokes.forEach { stroke ->
            if (stroke.size >= 2) {
                val path = android.graphics.Path()
                val first = stroke[0]
                path.moveTo(
                    (first.x - minX) * scale + offsetX,
                    (first.y - minY) * scale + offsetY
                )
                for (i in 1 until stroke.size) {
                    val point = stroke[i]
                    path.lineTo(
                        (point.x - minX) * scale + offsetX,
                        (point.y - minY) * scale + offsetY
                    )
                }
                canvas.drawPath(path, paint)
            }
        }

        return bitmap
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    private fun getPageCount(uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                PDDocument.load(inputStream).use { document ->
                    document.numberOfPages
                }
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }
}
