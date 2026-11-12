package com.omerakpul.localpdf.presentation.feature.home.ui

import com.omerakpul.localpdf.domain.model.Pdf

data class HomeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val recentFiles: List<Pdf> = emptyList()
)
