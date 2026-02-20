package com.omerakpul.localpdf.presentation.feature.files.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omerakpul.localpdf.domain.model.Pdf
import com.omerakpul.localpdf.domain.model.PdfSourceType
import com.omerakpul.localpdf.domain.repository.PdfRepository
import com.omerakpul.localpdf.domain.usecase.GetAllPdfsUseCase
import com.omerakpul.localpdf.presentation.feature.files.ui.FileFilter
import com.omerakpul.localpdf.presentation.feature.files.ui.FileSort
import com.omerakpul.localpdf.presentation.feature.files.ui.FilesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FilesViewModel @Inject constructor(
    private val getAllPdfsUseCase: GetAllPdfsUseCase,
    private val pdfRepository: PdfRepository
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
     * Delete a PDF file from storage and database.
     */
    fun deletePdf(pdfId: Int, filePath: String) {
        viewModelScope.launch {
            try {
                // Delete file from storage
                withContext(Dispatchers.IO) {
                    val file = File(filePath)
                    if (file.exists()) {
                        file.delete()
                    }
                }
                // Delete from database
                pdfRepository.deletePdfById(pdfId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Rename a PDF file in storage and database.
     */
    fun renamePdf(pdf: Pdf, newName: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val oldFile = File(pdf.filePath)
                    if (oldFile.exists()) {
                        val newFileName = if (newName.endsWith(".pdf", ignoreCase = true)) {
                            newName
                        } else {
                            "$newName.pdf"
                        }
                        val newFile = File(oldFile.parent, newFileName)
                        if (oldFile.renameTo(newFile)) {
                            // Update database using Pdf domain model
                            pdfRepository.updatePdf(
                                pdf.copy(
                                    name = newFileName,
                                    filePath = newFile.absolutePath,
                                    fileSize = newFile.length()
                                )
                            )
                        }
                    }
                }
                // Hide dialog after successful rename
                hideRenameDialog()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Show rename dialog for a PDF.
     */
    fun showRenameDialog(pdf: Pdf) {
        _uiState.value = _uiState.value.copy(
            pdfToRename = pdf,
            isRenameDialogVisible = true
        )
    }
    
    /**
     * Hide rename dialog.
     */
    fun hideRenameDialog() {
        _uiState.value = _uiState.value.copy(
            pdfToRename = null,
            isRenameDialogVisible = false
        )
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
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            selectedFilter = filter,
            isFilterMenuVisible = false,
            filteredFiles = applyFiltersAndSort(
                files = currentState.files,
                searchQuery = currentState.searchQuery,
                filter = filter,
                sort = currentState.selectedSort
            )
        )
    }
    
    /**
     * Update selected sort and refresh list.
     */
    fun onSortChange(sort: FileSort) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            selectedSort = sort,
            isSortMenuVisible = false,
            filteredFiles = applyFiltersAndSort(
                files = currentState.files,
                searchQuery = currentState.searchQuery,
                filter = currentState.selectedFilter,
                sort = sort
            )
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
    private fun applyFiltersAndSort(
        files: List<Pdf>,
        searchQuery: String = _uiState.value.searchQuery,
        filter: FileFilter = _uiState.value.selectedFilter,
        sort: FileSort = _uiState.value.selectedSort
    ): List<Pdf> {
        // Apply search filter
        var result = if (searchQuery.isBlank()) {
            files
        } else {
            files.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }

        // Apply type filter based on sourceType
        result = when (filter) {
            FileFilter.ALL -> result
            FileFilter.MERGED -> result.filter { it.sourceType == PdfSourceType.MERGED }
            FileFilter.SPLIT -> result.filter { it.sourceType == PdfSourceType.SPLIT }
            FileFilter.COMPRESSED -> result.filter { it.sourceType == PdfSourceType.COMPRESSED }
            FileFilter.SIGNED -> result.filter { it.sourceType == PdfSourceType.SIGNED }
            FileFilter.SCANNED -> result.filter { it.sourceType == PdfSourceType.SCANNED }
            FileFilter.CONVERTED -> result.filter { it.sourceType == PdfSourceType.CONVERTED }
        }

        // Apply sorting
        result = when (sort) {
            FileSort.RECENT -> result.sortedByDescending { it.createdAt }
            FileSort.NAME_ASC -> result.sortedBy { it.name.lowercase() }
            FileSort.NAME_DESC -> result.sortedByDescending { it.name.lowercase() }
            FileSort.SIZE_ASC -> result.sortedBy { it.fileSize }
            FileSort.SIZE_DESC -> result.sortedByDescending { it.fileSize }
        }

        return result
    }
}
