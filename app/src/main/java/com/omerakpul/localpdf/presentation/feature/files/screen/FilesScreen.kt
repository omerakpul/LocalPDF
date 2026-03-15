package com.omerakpul.localpdf.presentation.feature.files.screen

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

    // Rename Dialog (can live outside the layout structure)
    if (uiState.isRenameDialogVisible && uiState.pdfToRename != null) {
        val extension = uiState.pdfToRename!!.name.substringAfterLast(".", "pdf")
        var newName by remember(uiState.pdfToRename) {
            mutableStateOf(uiState.pdfToRename!!.name.substringBeforeLast("."))
        }

        AlertDialog(
            onDismissRequest = { viewModel.hideRenameDialog() },
            title = { Text(stringResource(R.string.action_rename)) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text(stringResource(R.string.rename_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    suffix = { Text(".$extension") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.renamePdf(uiState.pdfToRename!!, newName)
                        }
                    }
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideRenameDialog() }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
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
        Spacer(modifier = Modifier.height(16.dp))

        // Title with underline - centered like Home
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.files_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(0.15f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(PrimaryRed)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Modern search bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    if (uiState.searchQuery.isEmpty()) {
                        Text(
                            text = stringResource(R.string.files_search_hint),
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                    BasicTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 14.sp,
                            color = TextPrimary
                        ),
                        singleLine = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Modern filter and sort chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Filter chip
            Box {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .clickable { viewModel.toggleFilterMenu() }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = PrimaryRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.files_filter),
                            fontSize = 14.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                DropdownMenu(
                    expanded = uiState.isFilterMenuVisible,
                    onDismissRequest = { viewModel.toggleFilterMenu() }
                ) {
                    FileFilter.values().forEach { filter ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = uiState.selectedFilter == filter,
                                        onClick = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(getFilterName(filter))
                                }
                            },
                            onClick = { viewModel.onFilterChange(filter) }
                        )
                    }
                }
            }

            // Sort chip
            Box {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .clickable { viewModel.toggleSortMenu() }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = null,
                            tint = PrimaryRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.files_sort),
                            fontSize = 14.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
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

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryRed)
                }
            }

            uiState.filteredFiles.isEmpty() -> {
                // Modern empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        // Circular background with icon
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(60.dp))
                                .background(CardBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = PrimaryRed.copy(alpha = 0.7f)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = stringResource(R.string.files_empty),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.files_empty_subtitle),
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            else -> {
                // File list
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.filteredFiles) { pdf ->
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