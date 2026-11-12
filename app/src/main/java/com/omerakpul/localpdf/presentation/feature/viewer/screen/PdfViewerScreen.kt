package com.omerakpul.localpdf.presentation.feature.viewer.screen

import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitEachGesture
import kotlin.math.abs
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omerakpul.localpdf.R
import com.omerakpul.localpdf.presentation.theme.BackgroundColor
import com.omerakpul.localpdf.presentation.theme.CardBackground
import com.omerakpul.localpdf.presentation.theme.PrimaryRed
import com.omerakpul.localpdf.presentation.theme.TextPrimary
import com.omerakpul.localpdf.presentation.theme.TextSecondary
import java.io.File

private const val MAX_RENDER_SIDE = 2048f
private const val CACHE_RADIUS = 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    pdfPath: String,
    onBack: () -> Unit
) {
    var pdfRenderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var pageCount by remember { mutableStateOf(0) }
    val pageBitmaps = remember { mutableStateListOf<Bitmap?>() }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    // PdfRenderer allows only one open page at a time, so all rendering goes through this lock
    val renderLock = remember { Mutex() }

    // Open PDF
    DisposableEffect(pdfPath) {
        try {
            val file = File(pdfPath)
            val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fd)
            pdfRenderer = renderer
            pageCount = renderer.pageCount
            // Initialize with nulls
            pageBitmaps.clear()
            repeat(pageCount) { pageBitmaps.add(null) }
            isLoading = false
        } catch (e: Exception) {
            error = e.message
            isLoading = false
        }

        onDispose {
            val renderer = pdfRenderer
            if (renderer != null) {
                // Wait for a page that may still be rendering before closing the renderer
                CoroutineScope(Dispatchers.Default).launch {
                    renderLock.withLock { runCatching { renderer.close() } }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = File(pdfPath).nameWithoutExtension,
                        color = TextPrimary,
                        maxLines = 1,
                        fontSize = 16.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryRed)
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = error ?: "Unknown error",
                            color = TextSecondary,
                            fontSize = 16.sp
                        )
                    }
                }
                pageCount > 0 -> {
                    val pagerState = rememberPagerState(pageCount = { pageCount })

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Page viewer
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) { pageIndex ->
                            PdfPage(
                                renderer = pdfRenderer,
                                renderLock = renderLock,
                                pageIndex = pageIndex,
                                cachedBitmaps = pageBitmaps
                            )
                        }

                        // Bottom page indicator
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardBackground)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.viewer_page,
                                    pagerState.currentPage + 1,
                                    pageCount
                                ),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfPage(
    renderer: PdfRenderer?,
    renderLock: Mutex,
    pageIndex: Int,
    cachedBitmaps: MutableList<Bitmap?>
) {
    var bitmap by remember(pageIndex) { mutableStateOf(cachedBitmaps.getOrNull(pageIndex)) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Render page on demand, off the main thread so swiping stays smooth
    LaunchedEffect(pageIndex, renderer) {
        if (bitmap == null && renderer != null) {
            try {
                val bmp = withContext(Dispatchers.Default) {
                    renderLock.withLock {
                        renderer.openPage(pageIndex).use { page ->
                            // About 2048 px on the long side, never more than 3x the page size
                            val longest = maxOf(page.width, page.height).toFloat()
                            val renderScale = (MAX_RENDER_SIDE / longest).coerceIn(0.5f, 3f)
                            val result = Bitmap.createBitmap(
                                (page.width * renderScale).toInt().coerceAtLeast(1),
                                (page.height * renderScale).toInt().coerceAtLeast(1),
                                Bitmap.Config.ARGB_8888
                            )
                            result.eraseColor(android.graphics.Color.WHITE)
                            page.render(result, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            result
                        }
                    }
                }
                bitmap = bmp
                if (pageIndex < cachedBitmaps.size) {
                    cachedBitmaps[pageIndex] = bmp
                    // Keep only the pages around the current one; the rest are re-rendered on demand
                    for (i in cachedBitmaps.indices) {
                        if (abs(i - pageIndex) > CACHE_RADIUS) cachedBitmaps[i] = null
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Renderer closed or page failed to render: keep the spinner instead of crashing
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "Page ${pageIndex + 1}",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
                    // Only take over gestures when pinching or already zoomed in. A single finger at
                    // normal zoom must reach the pager so swiping turns the page.
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            do {
                                val event = awaitPointerEvent()
                                val zoom = event.calculateZoom()
                                val pan = event.calculatePan()
                                val multiTouch = event.changes.size > 1
                                if (multiTouch || scale > 1f) {
                                    scale = (scale * zoom).coerceIn(1f, 4f)
                                    if (scale > 1f) {
                                        // Keep the zoomed page from being dragged out of view
                                        val maxX = size.width * (scale - 1f) / 2f
                                        val maxY = size.height * (scale - 1f) / 2f
                                        offsetX = (offsetX + pan.x).coerceIn(-maxX, maxX)
                                        offsetY = (offsetY + pan.y).coerceIn(-maxY, maxY)
                                    } else {
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                                }
                            } while (event.changes.any { it.pressed })
                        }
                    }
            )
        } else {
            CircularProgressIndicator(
                color = PrimaryRed,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
