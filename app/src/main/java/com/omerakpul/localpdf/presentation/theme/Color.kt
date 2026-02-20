package com.omerakpul.localpdf.presentation.theme

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Primary
val PrimaryRed = Color(0xFFD32F2F)
val SuccessGreen = Color(0xFF4CAF50)

// Feature Card Colors (same in both themes)
val MergeColor = Color(0xFFE8F0FE)
val MergeIconColor = Color(0xFF4285F4)
val SplitColor = Color(0xFFFCE8E6)
val SplitIconColor = Color(0xFFEA4335)
val CompressColor = Color(0xFFE6F4EA)
val CompressIconColor = Color(0xFF34A853)
val ConvertColor = Color(0xFFE8F0FE)
val ConvertIconColor = Color(0xFF4285F4)
val ScanColor = Color(0xFFFCE8E6)
val ScanIconColor = Color(0xFFEA4335)
val SignColor = Color(0xFFE6F4EA)
val SignIconColor = Color(0xFF34A853)

// Internal dark mode state – set by Theme.kt
internal val _isDark = mutableStateOf(false)

// Theme-aware colors (reactive via Compose State)
val BackgroundColor: Color get() = if (_isDark.value) Color(0xFF121212) else Color(0xFFF5F5F5)
val CardBackground: Color get() = if (_isDark.value) Color(0xFF1E1E1E) else Color.White
val TextPrimary: Color get() = if (_isDark.value) Color.White else Color.Black
val TextSecondary: Color get() = if (_isDark.value) Color(0xFFB0B0B0) else Color.Gray