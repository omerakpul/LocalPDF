package com.omerakpul.localpdf.presentation.feature.splash.screen

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.omerakpul.localpdf.presentation.feature.splash.viewmodel.SplashViewModel
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.omerakpul.localpdf.R

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Animasyon değerleri
    val pdf1OffsetX = remember { Animatable(-300f) }  // Sol PDF, soldan gelecek
    val pdf2OffsetY = remember { Animatable(-300f) }  // Üst PDF, üstten gelecek
    val pdf3OffsetX = remember { Animatable(300f) }   // Sağ PDF, sağdan gelecek
    val logoScale = remember { Animatable(0f) }        // Logo büyüyecek
    val logoAlpha = remember { Animatable(0f) }        // Logo belirginleşecek

    // Faz 1: PDF'ler ortaya gelir
    LaunchedEffect(uiState.animationPhase) {
        if (uiState.animationPhase >= 1) {
            pdf1OffsetX.animateTo(-60f, tween(600, easing = FastOutSlowInEasing))
        }
    }
    LaunchedEffect(uiState.animationPhase) {
        if (uiState.animationPhase >= 1) {
            pdf2OffsetY.animateTo(-60f, tween(600, easing = FastOutSlowInEasing))
        }
    }
    LaunchedEffect(uiState.animationPhase) {
        if (uiState.animationPhase >= 1) {
            pdf3OffsetX.animateTo(60f, tween(600, easing = FastOutSlowInEasing))
        }
    }
// Faz 2: Logo görünür
    LaunchedEffect(uiState.animationPhase) {
        if (uiState.animationPhase >= 2) {
            pdf1OffsetX.animateTo(0f, tween(400))
            pdf2OffsetY.animateTo(0f, tween(400))
            pdf3OffsetX.animateTo(0f, tween(400))
            logoAlpha.animateTo(1f, tween(500))
            logoScale.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        }
    }
// Home'a git
    LaunchedEffect(uiState.navigateToHome) {
        if (uiState.navigateToHome) {
            onNavigateToHome()
        }
    }

    // Renk tanımı
    val PdfRed = Color(0xFFE53935)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        // PDF ikonları (animasyonlu)
        if (uiState.animationPhase >= 1 && logoAlpha.value < 1f) {
            // Sol PDF
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .offset(x = pdf1OffsetX.value.dp, y = 0.dp)
                    .alpha(1f - logoAlpha.value),
                tint = PdfRed
            )
            // Üst PDF
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .offset(x = 0.dp, y = pdf2OffsetY.value.dp)
                    .alpha(1f - logoAlpha.value),
                tint = PdfRed
            )
            // Sağ PDF
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .offset(x = pdf3OffsetX.value.dp, y = 0.dp)
                    .alpha(1f - logoAlpha.value),
                tint = PdfRed
            )
        }
        // Logo (animasyonlu)
        Image(
            painter = painterResource(id = R.drawable.localpdfpng),
            contentDescription = "LocalPDF Logo",
            modifier = Modifier
                .size(200.dp)
                .scale(logoScale.value)
                .alpha(logoAlpha.value)
        )
    }

}