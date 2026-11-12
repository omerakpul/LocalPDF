@file:OptIn(ExperimentalFoundationStyleApi::class)

package com.omerakpul.localpdf.presentation.feature.convert.screen

import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnIcon
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnChip
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Description
import com.omerakpul.localpdf.presentation.components.SecondaryButton
import com.omerakpul.localpdf.presentation.components.PrimaryButton
import com.omerakpul.localpdf.presentation.components.FileSummaryCard
import com.omerakpul.localpdf.presentation.components.EmptyState
import com.omerakpul.localpdf.presentation.components.AppScreen
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.foundation.layout.PaddingValues
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.omerakpul.localpdf.R
import com.omerakpul.localpdf.presentation.feature.convert.ui.ConversionMode
import com.omerakpul.localpdf.presentation.feature.convert.viewmodel.ConvertViewModel
import com.omerakpul.localpdf.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ConvertScreen(
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: ConvertViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.selectFile(it) }
    }

    LaunchedEffect(uiState.convertSuccess, uiState.convertedFileUri) {
        if (uiState.convertSuccess && uiState.convertedFileUri != null) {
            val path = uiState.convertedFileUri!!.path!!
            viewModel.resetSuccess()
            onNavigateToDetail(path)
        }
    }

    // Report finished image export
    val savedMessage = stringResource(R.string.convert_images_saved, uiState.imagesExported)
    LaunchedEffect(uiState.imagesExported) {
        if (uiState.imagesExported > 0) {
            snackbarHostState.showSnackbar(savedMessage)
            viewModel.resetImagesExported()
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
        title = stringResource(R.string.convert_pdf),
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        contentPadding = PaddingValues(16.dp)
    ) {
            // Mode Selection
            Text(
                text = stringResource(R.string.convert_mode),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShadcnChip(
                    label = stringResource(R.string.convert_word_to_pdf),
                    selected = uiState.conversionMode == ConversionMode.WORD_TO_PDF,
                    onClick = { viewModel.setConversionMode(ConversionMode.WORD_TO_PDF) }
                )
                ShadcnChip(
                    label = stringResource(R.string.convert_pdf_to_word),
                    selected = uiState.conversionMode == ConversionMode.PDF_TO_WORD,
                    onClick = { viewModel.setConversionMode(ConversionMode.PDF_TO_WORD) }
                )
                ShadcnChip(
                    label = stringResource(R.string.convert_pdf_to_images),
                    selected = uiState.conversionMode == ConversionMode.PDF_TO_IMAGES,
                    onClick = { viewModel.setConversionMode(ConversionMode.PDF_TO_IMAGES) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.selectedFileUri == null) {
                // Empty state
                EmptyState(
                    icon = Icons.Rounded.SwapHoriz,
                    title = stringResource(R.string.convert_empty),
                    description = stringResource(R.string.convert_empty_word_subtitle),
                    modifier = Modifier.weight(1f)
                )

                PrimaryButton(
                    text = stringResource(R.string.convert_select_file),
                    onClick = {
                        val mimeType = if (uiState.conversionMode == ConversionMode.WORD_TO_PDF)
                            arrayOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                        else
                            arrayOf("application/pdf")
                        filePickerLauncher.launch(mimeType)
                    },
                    icon = Icons.Rounded.Description
                )
            } else {
                // File selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (uiState.conversionMode == ConversionMode.WORD_TO_PDF)
                            Icons.Default.Description else Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = if (uiState.conversionMode == ConversionMode.WORD_TO_PDF)
                            ConvertIconColor else PrimaryRed,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.fileName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = formatFileSize(uiState.fileSize),
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = { viewModel.clearSelection() }) {
                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Conversion info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val fromExt = if (uiState.conversionMode == ConversionMode.WORD_TO_PDF) ".docx" else ".pdf"
                    val toExt = when (uiState.conversionMode) {
                        ConversionMode.WORD_TO_PDF -> ".pdf"
                        ConversionMode.PDF_TO_WORD -> ".docx"
                        ConversionMode.PDF_TO_IMAGES -> ".png"
                    }

                    Text(text = fromExt, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ConvertIconColor)
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = PrimaryRed, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = toExt, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryRed)
                }

                Spacer(modifier = Modifier.weight(1f))

                if (uiState.isLoading && uiState.conversionMode == ConversionMode.PDF_TO_IMAGES) {
                    LinearProgressIndicator(
                        progress = { uiState.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        color = PrimaryRed
                    )
                }



                PrimaryButton(
                    text = stringResource(R.string.convert_button),
                    onClick = { viewModel.convert() },
                    icon = Icons.Rounded.SwapHoriz,
                    enabled = uiState.canConvert,
                    loading = uiState.isLoading
                )
            }
            }
}

private fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> String.format("%.1f KB", size / 1024.0)
        else -> String.format("%.1f MB", size / (1024.0 * 1024.0))
    }
}
