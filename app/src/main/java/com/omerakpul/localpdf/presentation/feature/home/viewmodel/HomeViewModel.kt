package com.omerakpul.localpdf.presentation.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omerakpul.localpdf.domain.usecase.GetAllPdfsUseCase
import com.omerakpul.localpdf.presentation.feature.home.ui.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getAllPdfsUseCase: GetAllPdfsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getAllPdfsUseCase()
                .map { files ->
                    // Newest first (already sorted by the DAO); skip files that no longer exist
                    withContext(Dispatchers.IO) {
                        files.filter { File(it.filePath).exists() }.take(RECENT_COUNT)
                    }
                }
                .collect { recent -> _uiState.update { it.copy(recentFiles = recent) } }
        }
    }

    private companion object {
        const val RECENT_COUNT = 5
    }
}
