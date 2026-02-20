package com.omerakpul.localpdf.presentation.feature.convert.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvertScreen(
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: ConvertViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.convert_pdf), color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Mode Selection
            Text(
                text = stringResource(R.string.convert_mode),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.conversionMode == ConversionMode.WORD_TO_PDF,
                    onClick = { viewModel.setConversionMode(ConversionMode.WORD_TO_PDF) },
                    label = { Text(stringResource(R.string.convert_word_to_pdf)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryRed,
                        selectedLabelColor = CardBackground
                    )
                )
                FilterChip(
                    selected = uiState.conversionMode == ConversionMode.PDF_TO_WORD,
                    onClick = { viewModel.setConversionMode(ConversionMode.PDF_TO_WORD) },
                    label = { Text(stringResource(R.string.convert_pdf_to_word)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryRed,
                        selectedLabelColor = CardBackground
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.selectedFileUri == null) {
                // Empty state
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(CardBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = ConvertIconColor
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.convert_empty),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (uiState.conversionMode == ConversionMode.WORD_TO_PDF)
                                stringResource(R.string.convert_empty_word_subtitle)
                            else
                                stringResource(R.string.convert_empty_pdf_subtitle),
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }

                Button(
                    onClick = {
                        val mimeType = if (uiState.conversionMode == ConversionMode.WORD_TO_PDF)
                            arrayOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                        else
                            arrayOf("application/pdf")
                        filePickerLauncher.launch(mimeType)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                ) {
                    Icon(
                        if (uiState.conversionMode == ConversionMode.WORD_TO_PDF)
                            Icons.Default.Description else Icons.Default.PictureAsPdf,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.convert_select_file))
                }
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
                    val toExt = if (uiState.conversionMode == ConversionMode.WORD_TO_PDF) ".pdf" else ".docx"

                    Text(text = fromExt, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ConvertIconColor)
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = PrimaryRed, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = toExt, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryRed)
                }

                Spacer(modifier = Modifier.weight(1f))

                // Error
                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = PrimaryRed,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = { viewModel.convert() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.canConvert,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = CardBackground,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.convert_button))
                    }
                }
            }
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
