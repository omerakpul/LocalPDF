package com.omerakpul.localpdf.presentation.feature.home.screen

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
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val features = remember {
        listOf(
            FeatureItem(
                titleResId = R.string.merge_pdf,
                descResId = R.string.merge_pdf_desc,
                icon = Icons.Default.CallMerge,
                bgColor = MergeColor,
                iconColor = MergeIconColor,
                onClick = onNavigateToMerge
            ),
            FeatureItem(
                titleResId = R.string.split_pdf,
                descResId = R.string.split_pdf_desc,
                icon = Icons.Default.ContentCut,
                bgColor = SplitColor,
                iconColor = SplitIconColor,
                onClick = onNavigateToSplit
            ),
            FeatureItem(
                titleResId = R.string.compress_pdf,
                descResId = R.string.compress_pdf_desc,
                icon = Icons.Default.Compress,
                bgColor = CompressColor,
                iconColor = CompressIconColor,
                onClick = onNavigateToCompress
            ),
            FeatureItem(
                titleResId = R.string.convert_pdf,
                descResId = R.string.convert_pdf_desc,
                icon = Icons.Default.SwapHoriz,
                bgColor = ConvertColor,
                iconColor = ConvertIconColor,
                onClick = onNavigateToConvert
            ),
            FeatureItem(
                titleResId = R.string.photo_to_pdf,
                descResId = R.string.photo_to_pdf_desc,
                icon = Icons.Default.PhotoCamera,
                bgColor = ScanColor,
                iconColor = ScanIconColor,
                onClick = onNavigateToPhotoToPdf
            ),
            FeatureItem(
                titleResId = R.string.sign_pdf,
                descResId = R.string.sign_pdf_desc,
                icon = Icons.Default.Draw,
                bgColor = SignColor,
                iconColor = SignIconColor,
                onClick = onNavigateToSign
            ),
            FeatureItem(
                titleResId = R.string.edit_pdf,
                descResId = R.string.edit_pdf_desc,
                icon = Icons.Default.Edit,
                bgColor = MergeColor,
                iconColor = MergeIconColor,
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = if (isCompact) 16.dp else 32.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.lpdflogo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(38.dp).scale(2.75f)
                )

                Text(
                    text = stringResource(R.string.app_name),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Red underline
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(0.3f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(PrimaryRed)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Actions header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.quick_actions),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = stringResource(R.string.quick_actions_subtitle),
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Adaptive Feature cards grid
        LazyVerticalGrid(
            columns = if (isCompact) GridCells.Fixed(2) else GridCells.Adaptive(minSize = 180.dp),
            contentPadding = PaddingValues(start = 0.dp, end = 0.dp, top = 8.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
        }
    }
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