@file:OptIn(ExperimentalFoundationStyleApi::class)

package com.omerakpul.localpdf.presentation.feature.split.screen

import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnIcon
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnChip
import androidx.compose.material.icons.rounded.ContentCut
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.omerakpul.localpdf.R
import com.omerakpul.localpdf.presentation.feature.split.ui.SplitMode
import com.omerakpul.localpdf.presentation.feature.split.viewmodel.SplitPdfViewModel
import com.omerakpul.localpdf.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SplitPdfScreen(
    onBack: () -> Unit,
    onNavigateToFiles: () -> Unit,
    viewModel: SplitPdfViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.selectPdf(it) }
    }

    // Navigate to files when split completes
    LaunchedEffect(uiState.splitSuccess) {
        if (uiState.splitSuccess) {
            viewModel.resetSuccess()
            onNavigateToFiles()
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
        title = stringResource(R.string.split_pdf),
        onBack = onBack,
        snackbarHostState = snackbarHostState
    ) {
            if (uiState.selectedPdf == null) {
                // Empty state
                EmptyState(
                    icon = Icons.Rounded.ContentCut,
                    title = stringResource(R.string.split_empty),
                    description = stringResource(R.string.split_empty_subtitle),
                    modifier = Modifier.weight(1f)
                )

                PrimaryButton(
                    text = stringResource(R.string.split_select_pdf),
                    onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                    icon = Icons.Rounded.PictureAsPdf,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                // PDF Selected
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Selected PDF Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardBackground)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = PrimaryRed,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.pdfName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stringResource(R.string.pages_count, uiState.pageCount),
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Split Mode Selection
                    Text(
                        text = stringResource(R.string.split_mode),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShadcnChip(
                            label = stringResource(R.string.split_mode_all),
                            selected = uiState.splitMode == SplitMode.EXTRACT_ALL,
                            onClick = { viewModel.setSplitMode(SplitMode.EXTRACT_ALL) }
                        )
                        ShadcnChip(
                            label = stringResource(R.string.split_mode_range),
                            selected = uiState.splitMode == SplitMode.RANGE,
                            onClick = { viewModel.setSplitMode(SplitMode.RANGE) }
                        )
                        ShadcnChip(
                            label = stringResource(R.string.split_mode_select),
                            selected = uiState.splitMode == SplitMode.SELECT_PAGES,
                            onClick = { viewModel.setSplitMode(SplitMode.SELECT_PAGES) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mode-specific content
                    when (uiState.splitMode) {
                        SplitMode.EXTRACT_ALL -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CardBackground)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.split_all_info, uiState.pageCount),
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        SplitMode.RANGE -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CardBackground)
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    OutlinedTextField(
                                        value = uiState.rangeStart.toString(),
                                        onValueChange = { 
                                            it.toIntOrNull()?.let { num -> viewModel.setRangeStart(num) }
                                        },
                                        label = { Text(stringResource(R.string.split_from)) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = uiState.rangeEnd.toString(),
                                        onValueChange = { 
                                            it.toIntOrNull()?.let { num -> viewModel.setRangeEnd(num) }
                                        },
                                        label = { Text(stringResource(R.string.split_to)) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.split_range_info, uiState.rangeStart, uiState.rangeEnd),
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        SplitMode.SELECT_PAGES -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CardBackground)
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stringResource(R.string.split_selected, uiState.selectedPages.size),
                                        fontSize = 14.sp,
                                        color = TextSecondary
                                    )
                                    Row {
                                        TextButton(onClick = { viewModel.selectAllPages() }) {
                                            Text(stringResource(R.string.split_select_all), color = PrimaryRed)
                                        }
                                        TextButton(onClick = { viewModel.deselectAllPages() }) {
                                            Text(stringResource(R.string.split_deselect_all), color = TextSecondary)
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    (1..uiState.pageCount).forEach { page ->
                                        val isSelected = page in uiState.selectedPages
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) PrimaryRed else BackgroundColor)
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelected) PrimaryRed else TextSecondary,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable { viewModel.togglePage(page) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = page.toString(),
                                                color = if (isSelected) CardBackground else TextPrimary,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Split Button
                PrimaryButton(
                    text = stringResource(R.string.split_button),
                    onClick = { viewModel.splitPdf() },
                    icon = Icons.Rounded.ContentCut,
                    enabled = uiState.canSplit,
                    loading = uiState.isLoading,
                    modifier = Modifier.padding(16.dp)
                )
            }
            }
}
