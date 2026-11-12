@file:OptIn(ExperimentalFoundationStyleApi::class)

package com.omerakpul.localpdf.presentation.feature.detail.screen

import com.omerakpul.localpdf.presentation.components.rememberLegacyStorageGate
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextField
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSeparator
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnIcon
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialogTitle
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialogHeader
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialogFooter
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialogDescription
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAlertDialog
import com.omerakpul.localpdf.presentation.theme.OutlineColor
import com.omerakpul.localpdf.presentation.components.SecondaryButton
import com.omerakpul.localpdf.presentation.components.PrimaryButton
import com.omerakpul.localpdf.presentation.components.LoadingState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import com.omerakpul.localpdf.presentation.components.AppTopBar
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import android.widget.Toast
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.omerakpul.localpdf.presentation.feature.detail.viewmodel.PdfDetailViewModel
import com.omerakpul.localpdf.presentation.theme.*
import com.omerakpul.localpdf.R
import kotlinx.coroutines.delay
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfDetailScreen(
    onBack: () -> Unit,
    onNavigateToFiles: () -> Unit,
    onViewPdf: () -> Unit = {},
    viewModel: PdfDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSuccessBanner by remember { mutableStateOf(false) }

    // Navigate back after delete
    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onBack()
        }
    }

    // Navigate to Files after save
    LaunchedEffect(uiState.savedFilePath) {
        uiState.savedFilePath?.let { _ ->
            showSuccessBanner = true
            delay(1500) // Show success banner for 1.5 seconds
            viewModel.resetSavedState()
            onNavigateToFiles()
        }
    }

    // Show error
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    // Delete confirmation dialog
    ShadcnAlertDialog(
        visible = showDeleteDialog,
        onDismissRequest = { showDeleteDialog = false }
    ) {
        ShadcnDialogHeader {
            ShadcnDialogTitle(stringResource(R.string.detail_delete_title))
            ShadcnDialogDescription(stringResource(R.string.detail_delete_message))
        }
        ShadcnDialogFooter {
            ShadcnButton(
                onClick = { showDeleteDialog = false },
                variant = ButtonVariant.Outline
            ) {
                ShadcnText(stringResource(R.string.action_cancel))
            }
            ShadcnButton(
                onClick = {
                    showDeleteDialog = false
                    viewModel.deleteFile()
                },
                variant = ButtonVariant.Destructive
            ) {
                ShadcnText(stringResource(R.string.action_delete))
            }
        }
    }

    val requestStorage = rememberLegacyStorageGate()
    val isPdf = uiState.filePath.lowercase().endsWith(".pdf")
    val fileLabel = if (isPdf) "PDF" else "Word"

    Scaffold(
        topBar = {
            AppTopBar(title = stringResource(R.string.detail_title, fileLabel), onBack = onBack)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                LoadingState()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Success mark
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        ShadcnIcon(
                            imageVector = Icons.Rounded.CheckCircle,
                            tint = SuccessGreen,
                            modifier = Modifier.size(48.dp),
                            contentDescription = null
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ShadcnText(
                        text = stringResource(R.string.detail_created, fileLabel),
                        style = ShadcnTextStyle.TitleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    ShadcnTextField(
                        value = uiState.fileName,
                        onValueChange = { viewModel.updateFileName(it) },
                        label = stringResource(R.string.detail_file_name),
                        trailingIcon = {
                            ShadcnIcon(imageVector = Icons.Rounded.Edit, contentDescription = null, small = true)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // File information
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardBackground)
                            .border(1.dp, OutlineColor, RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        InfoRow(stringResource(R.string.detail_size), formatFileSize(uiState.fileSize))
                        ShadcnSeparator()
                        if (isPdf && uiState.isEncrypted) {
                            InfoRow(stringResource(R.string.detail_protected), stringResource(R.string.detail_protected_value))
                        } else if (isPdf) {
                            InfoRow(stringResource(R.string.detail_pages), "${uiState.pageCount}")
                        }
                        if (isPdf) ShadcnSeparator()
                        InfoRow(stringResource(R.string.detail_created_date), uiState.createdDate)
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    if (isPdf && !uiState.isEncrypted) {
                        SecondaryButton(
                            text = stringResource(R.string.action_view),
                            onClick = onViewPdf,
                            icon = Icons.Rounded.Visibility
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    PrimaryButton(
                        text = stringResource(R.string.detail_save_downloads),
                        onClick = { requestStorage { viewModel.saveToDownloads() } },
                        icon = Icons.Rounded.Save
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SecondaryButton(
                        text = stringResource(R.string.action_share),
                        onClick = {
                            val file = File(uiState.filePath)
                            val contentUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                file
                            )
                            val extension = file.extension.lowercase()
                            val mimeType = if (extension == "pdf") "application/pdf" else "application/vnd.openxmlformats-officedocument.wordprocessingml.document"

                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = mimeType
                                putExtra(Intent.EXTRA_STREAM, contentUri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, context.getString(R.string.detail_share_title, fileLabel)))
                        },
                        icon = Icons.Rounded.Share
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PrimaryButton(
                        text = stringResource(R.string.action_delete),
                        onClick = { showDeleteDialog = true },
                        icon = Icons.Rounded.Delete,
                        variant = ButtonVariant.Destructive
                    )
                }
            }

            // Success Banner Overlay
            AnimatedVisibility(
                visible = showSuccessBanner,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SuccessGreen)
                        .padding(horizontal = 28.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = stringResource(R.string.detail_saved),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShadcnText(text = label, muted = true)
        ShadcnText(text = value, fontWeight = FontWeight.Medium)
    }
}

private fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> String.format("%.1f KB", size / 1024.0)
        else -> String.format("%.1f MB", size / (1024.0 * 1024.0))
    }
}

