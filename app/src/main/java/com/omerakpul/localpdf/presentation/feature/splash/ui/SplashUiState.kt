package com.omerakpul.localpdf.presentation.feature.splash.ui

data class SplashUiState(
    val isLoading: Boolean = true,
    val animationPhase: Int = 0,
    val navigateToHome: Boolean = false
)
