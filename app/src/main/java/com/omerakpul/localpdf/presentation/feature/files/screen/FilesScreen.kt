@file:OptIn(ExperimentalFoundationStyleApi::class)

package com.omerakpul.localpdf.presentation.feature.files.screen

import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonSize
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextField
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnIcon
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialogTitle
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialogHeader
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialogFooter
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialog
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnChip
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import com.omerakpul.localpdf.presentation.components.SearchField
import com.omerakpul.localpdf.presentation.components.LoadingState
import com.omerakpul.localpdf.presentation.components.EmptyState
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.omerakpul.localpdf.R
import com.omerakpul.localpdf.presentation.components.BottomNavBar
import com.omerakpul.localpdf.presentation.components.LocalPdfNavigationRail
import com.omerakpul.localpdf.presentation.components.PdfListItem
import com.omerakpul.localpdf.presentation.feature.files.ui.FileFilter
import com.omerakpul.localpdf.presentation.feature.files.ui.FileSort
import com.omerakpul.localpdf.presentation.feature.files.ui.FilesUiState
import com.omerakpul.localpdf.presentation.feature.files.viewmodel.FilesViewModel
import com.omerakpul.localpdf.presentation.theme.*

@Composable
fun FilesScreen(
    windowSizeClass: WindowWidthSizeClass,
    onNavigateToHome: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onOpenPdf: (String) -> Unit,
    onSharePdf: (String) -> Unit,
    viewModel: FilesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isCompact = windowSizeClass == WindowWidthSizeClass.Compact

    if (isCompact) {
        // Standard Phone Portrait Layout
        Scaffold(
            bottomBar = {
                BottomNavBar(
                    currentRoute = "files",
                    onNavigate = { route ->
                        when (route) {
                            "home" -> onNavigateToHome()
                            "settings" -> onNavigateToSettings()
                        }
                    }
                )
            }
        ) { paddingValues ->
            FilesContent(
                uiState = uiState,
                viewModel = viewModel,
                isCompact = true,
                onOpenPdf = onOpenPdf,
                onSharePdf = onSharePdf,
                modifier = Modifier.padding(paddingValues)
            )
        }
    } else {
        // Tablet / Landscape Layout with NavigationRail
        Row(modifier = Modifier.fillMaxSize()) {
            LocalPdfNavigationRail(
                currentRoute = "files",
                onNavigate = { route ->
                    when (route) {
                        "home" -> onNavigateToHome()
                        "settings" -> onNavigateToSettings()
                    }
                }
            )

            FilesContent(
                uiState = uiState,
                viewModel = viewModel,
                isCompact = false,
                onOpenPdf = onOpenPdf,
                onSharePdf = onSharePdf,
                modifier = Modifier.weight(1f) // Take up remaining space
            )
        }
    }

    // Rename dialog (can live outside the layout structure)
    val renameTarget = uiState.pdfToRename
    if (uiState.isRenameDialogVisible && renameTarget != null) {
        val extension = renameTarget.name.substringAfterLast(".", "pdf")
        var newName by remember(renameTarget) {
            mutableStateOf(renameTarget.name.substringBeforeLast("."))
        }

        ShadcnDialog(
            visible = true,
            onDismissRequest = { viewModel.hideRenameDialog() }
        ) {
            ShadcnDialogHeader {
                ShadcnDialogTitle(stringResource(R.string.action_rename))
            }
            ShadcnTextField(
                value = newName,
                onValueChange = { newName = it },
                label = stringResource(R.string.rename_hint),
                trailingIcon = { ShadcnText(text = ".$extension", muted = true) },
                singleLine = true
            )
            ShadcnDialogFooter {
                ShadcnButton(
                    onClick = { viewModel.hideRenameDialog() },
                    variant = ButtonVariant.Outline
                ) {
                    ShadcnText(stringResource(R.string.action_cancel))
                }
                ShadcnButton(
                    onClick = {
                        if (newName.isNotBlank()) viewModel.renamePdf(renameTarget, newName)
                    }
                ) {
                    ShadcnText(stringResource(R.string.action_save))
                }
            }
        }
    }
}

@Composable
private fun FilesContent(
    uiState: FilesUiState,
    viewModel: FilesViewModel,
    isCompact: Boolean,
    onOpenPdf: (String) -> Unit,
    onSharePdf: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = if (isCompact) 16.dp else 32.dp)
    ) {
        // Title, file count and the sort control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.files_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
                ShadcnText(
                    text = stringResource(R.string.files_count, uiState.files.size),
                    style = ShadcnTextStyle.BodySmall,
                    muted = true
                )
            }

            Box {
                ShadcnButton(
                    onClick = { viewModel.toggleSortMenu() },
                    variant = ButtonVariant.Outline,
                    size = ButtonSize.Sm
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ShadcnIcon(
                            imageVector = Icons.AutoMirrored.Rounded.Sort,
                            contentDescription = null,
                            small = true
                        )
                        ShadcnText(stringResource(R.string.files_sort))
                    }
                }

                DropdownMenu(
                    expanded = uiState.isSortMenuVisible,
                    onDismissRequest = { viewModel.toggleSortMenu() }
                ) {
                    FileSort.values().forEach { sort ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = uiState.selectedSort == sort,
                                        onClick = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(getSortName(sort))
                                }
                            },
                            onClick = { viewModel.onSortChange(sort) }
                        )
                    }
                }
            }
        }

        SearchField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            placeholder = stringResource(R.string.files_search_hint)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips (one tap instead of a menu)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(FileFilter.values().toList()) { filter ->
                ShadcnChip(
                    label = getFilterName(filter),
                    selected = uiState.selectedFilter == filter,
                    onClick = { viewModel.onFilterChange(filter) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        when {
            uiState.isLoading -> LoadingState()

            uiState.filteredFiles.isEmpty() -> {
                EmptyState(
                    icon = Icons.Outlined.FolderOpen,
                    title = stringResource(R.string.files_empty),
                    description = stringResource(R.string.files_empty_subtitle)
                )
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.filteredFiles, key = { it.id }) { pdf ->
                        PdfListItem(
                            pdf = pdf,
                            onClick = { onOpenPdf(pdf.filePath) },
                            onLongClick = { /* TODO: Select mode */ },
                            onShare = { onSharePdf(pdf.filePath) },
                            onDelete = { viewModel.deletePdf(pdf.id, pdf.filePath) },
                            onRename = { viewModel.showRenameDialog(pdf) }
                        )
                    }
                }
            }
        }
    }
}

// Helper function to get filter display name
@Composable
private fun getFilterName(filter: FileFilter): String {
    return when (filter) {
        FileFilter.ALL -> stringResource(R.string.filter_all)
        FileFilter.MERGED -> stringResource(R.string.filter_merged)
        FileFilter.SPLIT -> stringResource(R.string.filter_split)
        FileFilter.COMPRESSED -> stringResource(R.string.filter_compressed)
        FileFilter.SIGNED -> stringResource(R.string.filter_signed)
        FileFilter.SCANNED -> stringResource(R.string.filter_scanned)
        FileFilter.CONVERTED -> stringResource(R.string.filter_converted)
    }
}

// Helper function to get sort display name
@Composable
private fun getSortName(sort: FileSort): String {
    return when (sort) {
        FileSort.RECENT -> stringResource(R.string.sort_recent)
        FileSort.NAME_ASC -> stringResource(R.string.sort_name_asc)
        FileSort.NAME_DESC -> stringResource(R.string.sort_name_desc)
        FileSort.SIZE_ASC -> stringResource(R.string.sort_size_asc)
        FileSort.SIZE_DESC -> stringResource(R.string.sort_size_desc)
    }
}