@file:OptIn(ExperimentalFoundationStyleApi::class)

package com.omerakpul.localpdf.presentation.feature.edit.screen

import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.RotateRight
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnIcon
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnChip
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Delete
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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

    AppScreen(
        title = stringResource(R.string.edit_pdf),
        onBack = onBack,
        snackbarHostState = snackbarHostState
    ) {
            if (uiState.selectedPdf == null) {
                // Empty state
                EmptyState(
                    icon = Icons.Rounded.Edit,
                    title = stringResource(R.string.edit_empty),
                    description = stringResource(R.string.edit_empty_subtitle),
                    modifier = Modifier.weight(1f)
                )

                PrimaryButton(
                    text = stringResource(R.string.edit_select_pdf),
                    onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                    icon = Icons.Rounded.PictureAsPdf,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Selected PDF card
                    FileSummaryCard(
                        name = uiState.pdfName,
                        detail = stringResource(R.string.pages_count, uiState.pageCount),
                        icon = Icons.Rounded.PictureAsPdf,
                        onRemove = { viewModel.clearSelection() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Edit action selection
                    Text(
                        text = stringResource(R.string.edit_action),
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
                            label = stringResource(R.string.edit_delete_pages),
                            selected = uiState.editAction == EditAction.DELETE_PAGES,
                            onClick = { viewModel.setEditAction(EditAction.DELETE_PAGES) },
                            leadingIcon = { mod -> ShadcnIcon(imageVector = Icons.Rounded.Delete, modifier = mod, small = true, contentDescription = null) }
                        )
                        ShadcnChip(
                            label = stringResource(R.string.edit_rotate_pages),
                            selected = uiState.editAction == EditAction.ROTATE_PAGES,
                            onClick = { viewModel.setEditAction(EditAction.ROTATE_PAGES) },
                            leadingIcon = { mod -> ShadcnIcon(imageVector = Icons.Rounded.RotateRight, modifier = mod, small = true, contentDescription = null) }
                        )
                        ShadcnChip(
                            label = stringResource(R.string.edit_reorder_pages),
                            selected = uiState.editAction == EditAction.REORDER_PAGES,
                            onClick = { viewModel.setEditAction(EditAction.REORDER_PAGES) },
                            leadingIcon = { mod -> ShadcnIcon(imageVector = Icons.Rounded.SwapVert, modifier = mod, small = true, contentDescription = null) }
                        )
                        ShadcnChip(
                            label = stringResource(R.string.edit_duplicate_pages),
                            selected = uiState.editAction == EditAction.DUPLICATE_PAGES,
                            onClick = { viewModel.setEditAction(EditAction.DUPLICATE_PAGES) },
                            leadingIcon = { mod -> ShadcnIcon(imageVector = Icons.Rounded.ContentCopy, modifier = mod, small = true, contentDescription = null) }
                        )
                        ShadcnChip(
                            label = stringResource(R.string.edit_protect),
                            selected = uiState.editAction == EditAction.PROTECT,
                            onClick = { viewModel.setEditAction(EditAction.PROTECT) },
                            leadingIcon = { mod -> ShadcnIcon(imageVector = Icons.Rounded.Lock, modifier = mod, small = true, contentDescription = null) }
                        )
                        ShadcnChip(
                            label = stringResource(R.string.edit_unlock),
                            selected = uiState.editAction == EditAction.UNLOCK,
                            onClick = { viewModel.setEditAction(EditAction.UNLOCK) },
                            leadingIcon = { mod -> ShadcnIcon(imageVector = Icons.Rounded.LockOpen, modifier = mod, small = true, contentDescription = null) }
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
                                    ShadcnChip(
                                        label = "${angle}°",
                                        selected = uiState.rotationAngle == angle,
                                        onClick = { viewModel.setRotationAngle(angle) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        if (uiState.editAction == EditAction.PROTECT || uiState.editAction == EditAction.UNLOCK) {
                            Text(
                                text = stringResource(
                                    if (uiState.editAction == EditAction.PROTECT) R.string.edit_protect_hint
                                    else R.string.edit_unlock_hint
                                ),
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = uiState.password,
                                onValueChange = { viewModel.setPassword(it) },
                                label = { Text(stringResource(R.string.edit_password)) },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (uiState.editAction == EditAction.PROTECT) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = uiState.confirmPassword,
                                    onValueChange = { viewModel.setConfirmPassword(it) },
                                    label = { Text(stringResource(R.string.edit_password_confirm)) },
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    isError = uiState.confirmPassword.isNotEmpty() && uiState.confirmPassword != uiState.password,
                                    supportingText = {
                                        if (uiState.confirmPassword.isNotEmpty() && uiState.confirmPassword != uiState.password) {
                                            Text(stringResource(R.string.edit_password_mismatch))
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = stringResource(R.string.edit_password_warning),
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        } else if (uiState.editAction == EditAction.REORDER_PAGES) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.edit_reorder_hint),
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { viewModel.resetPageOrder() }) {
                                    Text(stringResource(R.string.edit_reset_order), color = PrimaryRed, fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            uiState.pageOrder.forEachIndexed { index, originalPage ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(CardBackground)
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 13.sp,
                                        color = TextSecondary,
                                        modifier = Modifier.width(28.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.edit_page_n, originalPage),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.movePage(index, -1) },
                                        enabled = index > 0
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = TextPrimary)
                                    }
                                    IconButton(
                                        onClick = { viewModel.movePage(index, 1) },
                                        enabled = index < uiState.pageOrder.size - 1
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextPrimary)
                                    }
                                }
                            }
                        } else {
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

                            val actionText = when (uiState.editAction) {
                                EditAction.DELETE_PAGES -> stringResource(R.string.edit_delete_info, uiState.selectedPages.size)
                                EditAction.DUPLICATE_PAGES -> stringResource(R.string.edit_duplicate_info, uiState.selectedPages.size)
                                else -> stringResource(R.string.edit_rotate_info, uiState.selectedPages.size, uiState.rotationAngle)
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
                }



                // Edit button
                PrimaryButton(
                    text = stringResource(R.string.edit_button),
                    onClick = { viewModel.editPdf() },
                    icon = when (uiState.editAction) {
                        EditAction.DELETE_PAGES -> Icons.Rounded.Delete
                        EditAction.REORDER_PAGES -> Icons.Rounded.SwapVert
                        EditAction.DUPLICATE_PAGES -> Icons.Rounded.ContentCopy
                        EditAction.PROTECT -> Icons.Rounded.Lock
                        EditAction.UNLOCK -> Icons.Rounded.LockOpen
                        else -> Icons.Rounded.RotateRight
                    },
                    enabled = uiState.canEdit,
                    loading = uiState.isLoading,
                    modifier = Modifier.padding(16.dp)
                )
            }
            }
}
