package com.omerakpul.localpdf.presentation.feature.files.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omerakpul.localpdf.domain.model.Pdf
import com.omerakpul.localpdf.domain.usecase.GetAllPdfsUseCase
import com.omerakpul.localpdf.presentation.feature.files.ui.FileFilter
import com.omerakpul.localpdf.presentation.feature.files.ui.FileSort
import com.omerakpul.localpdf.presentation.feature.files.ui.FilesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.filter

@HiltViewModel
class FilesViewModel @Inject constructor(
    private val getAllPdfsUseCase: GetAllPdfsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(FilesUiState())
    val uiState: StateFlow<FilesUiState> = _uiState.asStateFlow()
    init {
        loadFiles()
    }
    /**
     * Load all PDF files from the repository.
     */
    private fun loadFiles() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            getAllPdfsUseCase().collect { files ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    files = files,
                    filteredFiles = applyFiltersAndSort(files)
                )
            }
        }
    }
    /**
     * Update search query and filter results.
     */
    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredFiles = applyFiltersAndSort(_uiState.value.files)
        )
    }
    /**
     * Update selected filter and refresh list.
     */
    fun onFilterChange(filter: FileFilter) {
        _uiState.value = _uiState.value.copy(
            selectedFilter = filter,
            isFilterMenuVisible = false,
            filteredFiles = applyFiltersAndSort(_uiState.value.files)
        )
    }
    /**
     * Update selected sort and refresh list.
     */
    fun onSortChange(sort: FileSort) {
        _uiState.value = _uiState.value.copy(
            selectedSort = sort,
            isSortMenuVisible = false,
            filteredFiles = applyFiltersAndSort(_uiState.value.files)
        )
    }
    /**
     * Toggle filter menu visibility.
     */
    fun toggleFilterMenu() {
        _uiState.value = _uiState.value.copy(
            isFilterMenuVisible = !_uiState.value.isFilterMenuVisible,
            isSortMenuVisible = false
        )
    }
    /**
     * Toggle sort menu visibility.
     */
    fun toggleSortMenu() {
        _uiState.value = _uiState.value.copy(
            isSortMenuVisible = !_uiState.value.isSortMenuVisible,
            isFilterMenuVisible = false
        )
    }
    /**
     * Apply filters and sorting to the file list.
     */
    private fun applyFiltersAndSort(files: List<Pdf>): List<Pdf> {
        val state = _uiState.value

        // Apply search filter
        var result = if (state.searchQuery.isBlank()) {
            files
        } else {
            files.filter { it.name.contains(state.searchQuery, ignoreCase = true) }
        }

        // Apply type filter (TODO: When PdfSourceType is added to model)
        // For now, filter is placeholder

        // Apply sorting
        result = when (state.selectedSort) {
            FileSort.RECENT -> result.sortedByDescending { it.createdAt }
            FileSort.NAME_ASC -> result.sortedBy { it.name.lowercase() }
            FileSort.NAME_DESC -> result.sortedByDescending { it.name.lowercase() }
            FileSort.SIZE_ASC -> result.sortedBy { it.fileSize }
            FileSort.SIZE_DESC -> result.sortedByDescending { it.fileSize }
        }

        return result
    }
}

