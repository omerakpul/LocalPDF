package com.omerakpul.localpdf.presentation.theme

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Internal dark mode state – set by Theme.kt
internal val _isDark = mutableStateOf(false)

/** Current theme state; read it in composition to rebuild anything that caches theme colours. */
val isDarkThemeActive: Boolean get() = _isDark.value

private fun themed(light: Long, dark: Long): Color = Color(if (_isDark.value) dark else light)

// ---------------------------------------------------------------------------
// Brand
// ---------------------------------------------------------------------------
internal val LightPrimary = Color(0xFFDC2626)
internal val DarkPrimary = Color(0xFFEF4444)

/** Brand crimson. Slightly deeper in light mode, slightly brighter in dark mode. */
val PrimaryRed: Color get() = if (_isDark.value) DarkPrimary else LightPrimary
val SuccessGreen = Color(0xFF2E9E5B)

// ---------------------------------------------------------------------------
// Surfaces and text (warm "stone" neutrals, shared with the component library theme)
// ---------------------------------------------------------------------------
/** App background in ARGB; also used for the window so transitions never flash a different colour. */
const val BACKGROUND_LIGHT = 0xFFFAFAF9L
const val BACKGROUND_DARK = 0xFF0C0A09L

val BackgroundColor: Color get() = themed(BACKGROUND_LIGHT, BACKGROUND_DARK)
val CardBackground: Color get() = themed(0xFFFFFFFF, 0xFF1C1917)
val OutlineColor: Color get() = themed(0xFFE7E5E4, 0xFF292524)
val TextPrimary: Color get() = themed(0xFF0C0A09, 0xFFFAFAF9)
val TextSecondary: Color get() = themed(0xFF78716C, 0xFFA8A29E)

// ---------------------------------------------------------------------------
// Tool accents: a tinted tile (Color) with a stronger icon colour (IconColor).
// Each pair has a light and a dark variant so tiles never glare in dark mode.
// ---------------------------------------------------------------------------
val MergeColor: Color get() = themed(0xFFE8EAFE, 0xFF262B4D)
val MergeIconColor: Color get() = themed(0xFF4455D6, 0xFF9FA8FF)

val SplitColor: Color get() = themed(0xFFFDE7EC, 0xFF4A2030)
val SplitIconColor: Color get() = themed(0xFFD81B60, 0xFFFF8FB1)

val CompressColor: Color get() = themed(0xFFDDF4E7, 0xFF1F3D2E)
val CompressIconColor: Color get() = themed(0xFF1E8E5A, 0xFF6FD3A0)

val ConvertColor: Color get() = themed(0xFFDDEFFC, 0xFF1E3550)
val ConvertIconColor: Color get() = themed(0xFF1B73C8, 0xFF7FBFFF)

val ScanColor: Color get() = themed(0xFFFFF0D6, 0xFF4A3818)
val ScanIconColor: Color get() = themed(0xFFC26A00, 0xFFFFC46B)

val SignColor: Color get() = themed(0xFFEEE4FB, 0xFF35264F)
val SignIconColor: Color get() = themed(0xFF7B3FC4, 0xFFC9A2FF)

val EditColor: Color get() = themed(0xFFD9F2F0, 0xFF1B3F3C)
val EditIconColor: Color get() = themed(0xFF14857C, 0xFF6FD6CC)
