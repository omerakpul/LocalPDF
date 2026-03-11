package com.omerakpul.localpdf.presentation.feature.edit.screen

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.omerakpul.localpdf.R
import com.omerakpul.localpdf.presentation.feature.edit.ui.EditAction
import com.omerakpul.localpdf.presentation.feature.edit.viewmodel.EditPdfViewModel
import com.omerakpul.localpdf.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditPdfScreen(
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: EditPdfViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.selectPdf(it) }
    }

    LaunchedEffect(uiState.editSuccess, uiState.editedPdfUri) {
        if (uiState.editSuccess && uiState.editedPdfUri != null) {
            val path = uiState.editedPdfUri!!.path!!
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_pdf), color = TextPrimary) },
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
        ) {
            if (uiState.selectedPdf == null) {
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
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MergeIconColor
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.edit_empty),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = stringResource(R.string.edit_empty_subtitle),
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }

                Button(
                    onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.edit_select_pdf))
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Selected PDF card
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
                                text = "${uiState.pageCount} pages",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Edit action selection
                    Text(
                        text = stringResource(R.string.edit_action),
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
                            selected = uiState.editAction == EditAction.DELETE_PAGES,
                            onClick = { viewModel.setEditAction(EditAction.DELETE_PAGES) },
                            label = { Text(stringResource(R.string.edit_delete_pages)) },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryRed,
                                selectedLabelColor = CardBackground,
                                selectedLeadingIconColor = CardBackground
                            )
                        )
                        FilterChip(
                            selected = uiState.editAction == EditAction.ROTATE_PAGES,
                            onClick = { viewModel.setEditAction(EditAction.ROTATE_PAGES) },
                            label = { Text(stringResource(R.string.edit_rotate_pages)) },
                            leadingIcon = {
                                Icon(Icons.Default.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryRed,
                                selectedLabelColor = CardBackground,
                                selectedLeadingIconColor = CardBackground
                            )
                        )
                    }

                    if (uiState.editAction != EditAction.NONE) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Rotation angle selection (only for rotate action)
                        if (uiState.editAction == EditAction.ROTATE_PAGES) {
                            Text(
                                text = stringResource(R.string.edit_rotation_angle),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(90, 180, 270).forEach { angle ->
                                    FilterChip(
                                        selected = uiState.rotationAngle == angle,
                                        onClick = { viewModel.setRotationAngle(angle) },
                                        label = { Text("${angle}°") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PrimaryRed,
                                            selectedLabelColor = CardBackground
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Page selection header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.edit_select_pages),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Row {
                                TextButton(onClick = { viewModel.selectAllPages() }) {
                                    Text(stringResource(R.string.split_select_all), color = PrimaryRed, fontSize = 12.sp)
                                }
                                TextButton(onClick = { viewModel.deselectAllPages() }) {
                                    Text(stringResource(R.string.split_deselect_all), color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Page selection grid
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (page in 1..uiState.pageCount) {
                                val isSelected = page in uiState.selectedPages
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) PrimaryRed else CardBackground)
                                        .border(
                                            1.dp,
                                            if (isSelected) PrimaryRed else TextSecondary.copy(alpha = 0.3f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.togglePage(page) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$page",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) CardBackground else TextPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        if (uiState.selectedPages.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))

                            val actionText = if (uiState.editAction == EditAction.DELETE_PAGES) {
                                stringResource(R.string.edit_delete_info, uiState.selectedPages.size)
                            } else {
                                stringResource(R.string.edit_rotate_info, uiState.selectedPages.size, uiState.rotationAngle)
                            }
                            Text(
                                text = actionText,
                                fontSize = 13.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }



                // Edit button
                Button(
                    onClick = { viewModel.editPdf() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = uiState.canEdit,
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
                        Icon(
                            if (uiState.editAction == EditAction.DELETE_PAGES) Icons.Default.Delete
                            else Icons.Default.RotateRight,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.edit_button))
                    }
                }
            }
        }
    }
}
