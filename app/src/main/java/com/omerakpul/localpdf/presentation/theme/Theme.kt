package com.omerakpul.localpdf.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// Warm, crimson-based schemes. Every neutral is tinted warm so nothing falls back to
// Material's default purple (navigation bar, chips, dialogs, status bar).
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5C1A1A),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFE7BDB6),
    onSecondary = Color(0xFF442925),
    secondaryContainer = Color(0xFF3A2724),
    onSecondaryContainer = Color(0xFFFFDAD6),
    tertiary = Color(0xFFE0C38C),
    background = Color(0xFF131010),
    onBackground = Color(0xFFF3EDEB),
    surface = Color(0xFF131010),
    onSurface = Color(0xFFF3EDEB),
    surfaceVariant = Color(0xFF2A2423),
    onSurfaceVariant = Color(0xFFB4A8A4),
    surfaceContainerLowest = Color(0xFF0E0B0B),
    surfaceContainerLow = Color(0xFF191514),
    surfaceContainer = Color(0xFF1E1A19),
    surfaceContainerHigh = Color(0xFF282322),
    surfaceContainerHighest = Color(0xFF332D2B),
    outline = Color(0xFF8C7F7B),
    outlineVariant = Color(0xFF332D2B)
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0DD),
    onPrimaryContainer = Color(0xFF410003),
    secondary = Color(0xFF77574F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE9E5),
    onSecondaryContainer = Color(0xFF2C1511),
    tertiary = Color(0xFF6E5D2E),
    background = Color(0xFFFAF7F5),
    onBackground = Color(0xFF211A19),
    surface = Color(0xFFFAF7F5),
    onSurface = Color(0xFF211A19),
    surfaceVariant = Color(0xFFF3EBE8),
    onSurfaceVariant = Color(0xFF6E6461),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFCF9F7),
    surfaceContainer = Color(0xFFF6F1EE),
    surfaceContainerHigh = Color(0xFFF0EAE6),
    surfaceContainerHighest = Color(0xFFEAE3DF),
    outline = Color(0xFF9A8E8A),
    outlineVariant = Color(0xFFEBE3DF)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun LocalPDFTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Update the global dark mode state so Color.kt vals react
    SideEffect {
        _isDark.value = darkTheme
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
