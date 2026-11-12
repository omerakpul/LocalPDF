package com.omerakpul.localpdf.presentation.feature.phototopdf.screen

import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CameraAlt
import com.omerakpul.localpdf.presentation.components.SecondaryButton
import com.omerakpul.localpdf.presentation.components.PrimaryButton
import com.omerakpul.localpdf.presentation.components.FileSummaryCard
import com.omerakpul.localpdf.presentation.components.EmptyState
import com.omerakpul.localpdf.presentation.components.AppScreen
import androidx.compose.material.icons.rounded.PictureAsPdf
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.omerakpul.localpdf.R
import com.omerakpul.localpdf.presentation.feature.phototopdf.viewmodel.PhotoToPdfViewModel
import com.omerakpul.localpdf.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoToPdfScreen(
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: PhotoToPdfViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.addPhotos(uris)
        }
    }

    // Navigate to detail when creation completes
    LaunchedEffect(uiState.createSuccess, uiState.createdPdfUri) {
        if (uiState.createSuccess && uiState.createdPdfUri != null) {
            val path = uiState.createdPdfUri!!.path!!
            viewModel.resetSuccess()
            onNavigateToDetail(path)
        }
    }

    // Show error SnackBar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    AppScreen(
        title = stringResource(R.string.photo_to_pdf),
        onBack = onBack,
        snackbarHostState = snackbarHostState
    ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.photo_count, uiState.selectedPhotos.size),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                if (uiState.selectedPhotos.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearAll() }) {
                        Text(
                            text = stringResource(R.string.merge_clear_all),
                            color = PrimaryRed,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            if (uiState.selectedPhotos.isEmpty()) {
                // Empty state
                EmptyState(
                    icon = Icons.Rounded.CameraAlt,
                    title = stringResource(R.string.photo_empty),
                    description = stringResource(R.string.photo_empty_subtitle),
                    modifier = Modifier.weight(1f)
                )
            } else {
                // Photo Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(uiState.selectedPhotos) { index, uri ->
                        PhotoItem(
                            uri = uri,
                            index = index + 1,
                            onRemove = { viewModel.removePhoto(uri) }
                        )
                    }
                }
            }

            // Add more button
            SecondaryButton(
                text = stringResource(R.string.photo_add_more),
                onClick = { photoPickerLauncher.launch(arrayOf("image/*")) },
                icon = Icons.Rounded.Add,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Create PDF Button
            PrimaryButton(
                text = stringResource(R.string.photo_create_pdf),
                onClick = { viewModel.createPdf() },
                icon = Icons.Rounded.PictureAsPdf,
                enabled = uiState.canCreate,
                loading = uiState.isLoading,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            }
}

@Composable
private fun PhotoItem(
    uri: Uri,
    index: Int,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(uri)
                .crossfade(true)
                .build(),
            contentDescription = "Photo $index",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Index Badge
        Box(
            modifier = Modifier
                .padding(4.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(PrimaryRed)
                .align(Alignment.TopStart),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index.toString(),
                color = CardBackground,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Remove Button
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp)
                .padding(4.dp)
                .clip(CircleShape)
                .background(BackgroundColor.copy(alpha = 0.7f))
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = PrimaryRed,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
