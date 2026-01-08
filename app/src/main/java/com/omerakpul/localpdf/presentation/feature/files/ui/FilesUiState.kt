package com.omerakpul.localpdf.presentation.feature.files.ui

import com.omerakpul.localpdf.domain.model.Pdf

data class FilesUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val files: List<Pdf> = emptyList(),
    val filteredFiles: List<Pdf> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: FileFilter = FileFilter.ALL,
    val selectedSort: FileSort = FileSort.RECENT,
    val isFilterMenuVisible: Boolean = false,
    val isSortMenuVisible: Boolean = false
)
