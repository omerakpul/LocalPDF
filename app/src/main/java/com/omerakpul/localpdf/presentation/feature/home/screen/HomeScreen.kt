package com.omerakpul.localpdf.presentation.feature.home.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.CallMerge
import androidx.compose.foundation.border
import java.util.Locale
import java.util.Date
import java.text.SimpleDateFormat
import com.omerakpul.localpdf.domain.model.Pdf
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Description
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.omerakpul.localpdf.R
import com.omerakpul.localpdf.presentation.components.BottomNavBar
import com.omerakpul.localpdf.presentation.components.FeatureCard
import com.omerakpul.localpdf.presentation.components.LocalPdfNavigationRail
import com.omerakpul.localpdf.presentation.feature.home.viewmodel.HomeViewModel
import com.omerakpul.localpdf.presentation.theme.*

@Composable
fun HomeScreen(
    windowSizeClass: WindowWidthSizeClass,
    onNavigateToMerge: () -> Unit,
    onNavigateToSplit: () -> Unit,
    onNavigateToCompress: () -> Unit,
    onNavigateToConvert: () -> Unit,
    onNavigateToPhotoToPdf: () -> Unit,
    onNavigateToSign: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onNavigateToFiles: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onOpenPdf: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Keyed on the theme: the tile colours are theme dependent and must be rebuilt when it changes
    val isDarkTheme = com.omerakpul.localpdf.presentation.theme.isDarkThemeActive
    val features = remember(isDarkTheme) {
        listOf(
            FeatureItem(
                titleResId = R.string.merge_pdf,
                descResId = R.string.merge_pdf_desc,
                icon = Icons.Rounded.CallMerge,
                bgColor = MergeColor,
                iconColor = MergeIconColor,
                onClick = onNavigateToMerge
            ),
            FeatureItem(
                titleResId = R.string.split_pdf,
                descResId = R.string.split_pdf_desc,
                icon = Icons.Rounded.ContentCut,
                bgColor = SplitColor,
                iconColor = SplitIconColor,
                onClick = onNavigateToSplit
            ),
            FeatureItem(
                titleResId = R.string.compress_pdf,
                descResId = R.string.compress_pdf_desc,
                icon = Icons.Rounded.Compress,
                bgColor = CompressColor,
                iconColor = CompressIconColor,
                onClick = onNavigateToCompress
            ),
            FeatureItem(
                titleResId = R.string.convert_pdf,
                descResId = R.string.convert_pdf_desc,
                icon = Icons.Rounded.SwapHoriz,
                bgColor = ConvertColor,
                iconColor = ConvertIconColor,
                onClick = onNavigateToConvert
            ),
            FeatureItem(
                titleResId = R.string.photo_to_pdf,
                descResId = R.string.photo_to_pdf_desc,
                icon = Icons.Rounded.DocumentScanner,
                bgColor = ScanColor,
                iconColor = ScanIconColor,
                onClick = onNavigateToPhotoToPdf
            ),
            FeatureItem(
                titleResId = R.string.sign_pdf,
                descResId = R.string.sign_pdf_desc,
                icon = Icons.Rounded.Draw,
                bgColor = SignColor,
                iconColor = SignIconColor,
                onClick = onNavigateToSign
            ),
            FeatureItem(
                titleResId = R.string.edit_pdf,
                descResId = R.string.edit_pdf_desc,
                icon = Icons.Rounded.EditNote,
                bgColor = EditColor,
                iconColor = EditIconColor,
                onClick = onNavigateToEdit
            )
        )
    }

    val isCompact = windowSizeClass == WindowWidthSizeClass.Compact

    if (isCompact) {
        // Standard Phone Portrait Layout
        Scaffold(
            bottomBar = {
                BottomNavBar(
                    currentRoute = "home",
                    onNavigate = { route ->
                        when (route) {
                            "files" -> onNavigateToFiles()
                            "settings" -> onNavigateToSettings()
                        }
                    }
                )
            }
        ) { paddingValues ->
            HomeContent(
                features = features,
                isCompact = true,
                recentFiles = uiState.recentFiles,
                onOpenPdf = onOpenPdf,
                modifier = Modifier.padding(paddingValues)
            )
        }
    } else {
        // Tablet / Landscape Layout with NavigationRail
        Row(modifier = Modifier.fillMaxSize()) {
            LocalPdfNavigationRail(
                currentRoute = "home",
                onNavigate = { route ->
                    when (route) {
                        "files" -> onNavigateToFiles()
                        "settings" -> onNavigateToSettings()
                    }
                }
            )

            Scaffold { paddingValues ->
                HomeContent(
                    features = features,
                    isCompact = false,
                    recentFiles = uiState.recentFiles,
                    onOpenPdf = onOpenPdf,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    features: List<FeatureItem>,
    isCompact: Boolean,
    recentFiles: List<Pdf>,
    onOpenPdf: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = if (isCompact) 16.dp else 32.dp)
    ) {
        // Brand header: logo tile, app name and an "offline" tagline
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(PrimaryRed.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.lpdflogo),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp).scale(1.9f)
                )
            }

            Column(modifier = Modifier.padding(start = 14.dp)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(R.string.home_tagline),
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.quick_actions),
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )

        // Adaptive Feature cards grid
        LazyVerticalGrid(
            columns = if (isCompact) GridCells.Fixed(2) else GridCells.Adaptive(minSize = 180.dp),
            contentPadding = PaddingValues(start = 0.dp, end = 0.dp, top = 8.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(
                items = features,
                key = { it.titleResId }
            ) { feature ->
                FeatureCard(
                    title = stringResource(feature.titleResId),
                    description = stringResource(feature.descResId),
                    icon = feature.icon,
                    iconBackgroundColor = feature.bgColor,
                    iconTint = feature.iconColor,
                    onClick = feature.onClick
                )
            }

            if (recentFiles.isNotEmpty()) {
                item(key = "recent_header", span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = stringResource(R.string.recent_files),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(
                    items = recentFiles,
                    key = { "recent_${it.id}" },
                    span = { GridItemSpan(maxLineSpan) }
                ) { pdf ->
                    RecentFileRow(pdf = pdf, onClick = { onOpenPdf(pdf.filePath) })
                }
            }
        }
    }
}

@Composable
private fun RecentFileRow(pdf: Pdf, onClick: () -> Unit) {
    val isPdf = pdf.name.lowercase().endsWith(".pdf")
    val date = remember(pdf.createdAt) {
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(pdf.createdAt))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(1.dp, OutlineColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isPdf) Icons.Default.PictureAsPdf else Icons.Default.Description,
            contentDescription = null,
            tint = PrimaryRed,
            modifier = Modifier.size(32.dp)
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = pdf.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$date · ${formatSize(pdf.fileSize)}",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

private fun formatSize(size: Long): String = when {
    size < 1024 -> "$size B"
    size < 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f KB", size / 1024.0)
    else -> String.format(Locale.getDefault(), "%.1f MB", size / (1024.0 * 1024.0))
}

/**
 * Data class for feature items in the grid.
 */
private data class FeatureItem(
    val titleResId: Int,
    val descResId: Int,
    val icon: ImageVector,
    val bgColor: Color,
    val iconColor: Color,
    val onClick: () -> Unit
)