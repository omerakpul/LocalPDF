package com.omerakpul.localpdf.data.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Renders small page previews of one PDF at a time. A single PdfRenderer is kept open and every
 * render goes through a lock (PdfRenderer allows only one open page). Results are cached, so
 * scrolling back and forth does not render the same page again.
 */
class PdfThumbnails(private val context: Context) : AutoCloseable {

    private val lock = Mutex()
    private var descriptor: ParcelFileDescriptor? = null
    private var renderer: PdfRenderer? = null
    private var openUri: Uri? = null
    private val cache = LruCache<Int, Bitmap>(48)

    /** Returns the preview for the 1-based [page], or null if it cannot be rendered. */
    suspend fun load(uri: Uri, page: Int, widthPx: Int = 200): Bitmap? =
        withContext(Dispatchers.Default) {
            lock.withLock {
                try {
                    if (openUri != uri) {
                        closeInternal()
                        val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return@withLock null
                        descriptor = pfd
                        renderer = PdfRenderer(pfd)
                        openUri = uri
                    }
                    cache.get(page)?.let { return@withLock it }

                    val pdf = renderer ?: return@withLock null
                    val index = (page - 1).coerceIn(0, pdf.pageCount - 1)
                    pdf.openPage(index).use { source ->
                        val scale = widthPx.toFloat() / source.width
                        val bitmap = Bitmap.createBitmap(
                            widthPx,
                            (source.height * scale).toInt().coerceAtLeast(1),
                            Bitmap.Config.ARGB_8888
                        )
                        bitmap.eraseColor(Color.WHITE)
                        source.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        cache.put(page, bitmap)
                        bitmap
                    }
                } catch (e: Exception) {
                    // Protected or damaged file: the caller just shows an empty placeholder
                    null
                }
            }
        }

    private fun closeInternal() {
        runCatching { renderer?.close() }
        runCatching { descriptor?.close() }
        renderer = null
        descriptor = null
        openUri = null
        cache.evictAll()
    }

    override fun close() {
        closeInternal()
    }
}
