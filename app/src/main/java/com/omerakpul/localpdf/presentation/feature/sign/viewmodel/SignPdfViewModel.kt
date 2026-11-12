package com.omerakpul.localpdf.presentation.feature.sign.viewmodel

import kotlinx.coroutines.CancellationException
import com.omerakpul.localpdf.R
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import com.omerakpul.localpdf.data.service.PdfService
import com.omerakpul.localpdf.data.service.FileInfoProvider
import androidx.lifecycle.viewModelScope
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import com.omerakpul.localpdf.presentation.feature.sign.ui.SignPdfUiState
import com.omerakpul.localpdf.domain.util.MemoryUtil
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
    @ApplicationContext private val context: Context,
    private val pdfService: PdfService,
    private val memoryUtil: MemoryUtil,
    private val fileInfo: FileInfoProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignPdfUiState())
    val uiState: StateFlow<SignPdfUiState> = _uiState.asStateFlow()

    fun selectPdf(uri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val size = fileInfo.getFileSize(uri)
                val maxSize = memoryUtil.getMaxAllowedFileSizeBytes()

                if (size > maxSize) {
                    _uiState.update { current ->
                        current.copy(
                            error = context.getString(R.string.error_file_too_large, memoryUtil.formatSize(size), memoryUtil.formatSize(maxSize))
                        )
                    }
                    return@withContext
                }

                val name = fileInfo.getFileName(uri) ?: "unknown.pdf"
                val pageCount = fileInfo.getPageCount(uri)
                _uiState.update {
                    it.copy(
                        selectedPdf = uri,
                        pdfName = name,
                        pageCount = pageCount,
                        signaturePage = pageCount // default: last page
                    )
                }
                loadPagePreview()
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
                val strokes = current.signaturePoints + listOf(current.currentStroke)
                current.copy(
                    signaturePoints = strokes,
                    currentStroke = emptyList(),
                    signatureBitmap = createSignatureBitmap(strokes, 400, 200)
                )
            } else current
        }
    }

    fun clearSignature() {
        _uiState.update { it.copy(signaturePoints = emptyList(), currentStroke = emptyList(), signatureBitmap = null) }
    }

    fun setSignaturePage(page: Int) {
        _uiState.update { it.copy(signaturePage = page.coerceIn(1, it.pageCount.coerceAtLeast(1))) }
        loadPagePreview()
    }

    fun setSignaturePlacement(x: Float, y: Float, width: Float) {
        _uiState.update { it.copy(signatureX = x, signatureY = y, signatureWidth = width) }
    }

    /** Renders the selected page so the user can position the signature on it. */
    private fun loadPagePreview() {
        val uri = _uiState.value.selectedPdf ?: return
        val page = _uiState.value.signaturePage
        viewModelScope.launch {
            val bitmap = withContext(Dispatchers.IO) { renderPage(uri, page) }
            // Ignore stale results if the user changed page/file meanwhile
            if (_uiState.value.selectedPdf == uri && _uiState.value.signaturePage == page) {
                _uiState.update { it.copy(pagePreview = bitmap) }
            }
        }
    }

    private fun renderPage(uri: Uri, pageNumber: Int): Bitmap? {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    val index = (pageNumber - 1).coerceIn(0, renderer.pageCount - 1)
                    renderer.openPage(index).use { page ->
                        val targetWidth = 1000
                        val scale = targetWidth.toFloat() / page.width
                        val bitmap = Bitmap.createBitmap(
                            targetWidth,
                            (page.height * scale).toInt().coerceAtLeast(1),
                            Bitmap.Config.ARGB_8888
                        )
                        bitmap.eraseColor(android.graphics.Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        bitmap
                    }
                }
            }
        } catch (e: Exception) {
            null
        }
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
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: context.getString(R.string.error_sign_failed)
                    )
                }
            }
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(signSuccess = false, signedPdfUri = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private suspend fun addSignatureToPdf(pdfUri: Uri): File {
        val state = _uiState.value

        // Create signature bitmap
        val sigBitmap = createSignatureBitmap(state.signaturePoints, 400, 200)

        // Convert to byte array
        val baos = ByteArrayOutputStream()
        sigBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val sigBytes = baos.toByteArray()
        sigBitmap.recycle()

        return pdfService.addSignature(
            pdfUri, state.signaturePage, sigBytes,
            state.signatureX, state.signatureY, state.signatureWidth
        )
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



}
