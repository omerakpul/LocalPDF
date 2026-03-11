package com.omerakpul.localpdf.presentation.feature.splash.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omerakpul.localpdf.presentation.feature.splash.ui.SplashUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class SplashViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()
    init {
        startAnimation()
    }
    private fun startAnimation() {
        viewModelScope.launch {
            delay(300)
            _uiState.value = _uiState.value.copy(animationPhase = 1)

            delay(1200)
            _uiState.value = _uiState.value.copy(animationPhase = 2)

            delay(1500)
            _uiState.value = _uiState.value.copy(
                animationPhase = 3,
                isLoading = false,
                navigateToHome = true
            )
        }
    }
}
