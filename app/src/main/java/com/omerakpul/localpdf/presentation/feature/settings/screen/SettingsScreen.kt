package com.omerakpul.localpdf.presentation.feature.settings.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.omerakpul.localpdf.R
import com.omerakpul.localpdf.presentation.components.BottomNavBar
import com.omerakpul.localpdf.presentation.components.LocalPdfNavigationRail
import com.omerakpul.localpdf.presentation.feature.settings.ui.AppLanguage
import com.omerakpul.localpdf.presentation.feature.settings.ui.SettingsUiState
import com.omerakpul.localpdf.presentation.feature.settings.ui.ThemeMode
import com.omerakpul.localpdf.presentation.feature.settings.viewmodel.SettingsViewModel
import com.omerakpul.localpdf.presentation.theme.*

@Composable
fun SettingsScreen(
    windowSizeClass: WindowWidthSizeClass,
    onNavigateToHome: () -> Unit,
    onNavigateToFiles: () -> Unit,
    onNavigateToLicenseDetails: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isCompact = windowSizeClass == WindowWidthSizeClass.Compact

    // Theme Dialog
    if (uiState.showThemeDialog) {
        ThemeDialog(
            themeMode = uiState.themeMode,
            onDismiss = { viewModel.hideThemeDialog() },
            onThemeSelected = { viewModel.setThemeMode(it) }
        )
    }

    // Language Dialog
    if (uiState.showLanguageDialog) {
        LanguageDialog(
            language = uiState.language,
            onDismiss = { viewModel.hideLanguageDialog() },
            onLanguageSelected = { viewModel.setLanguage(it) }
        )
    }

    if (isCompact) {
        // Standard Phone Portrait Layout
        Scaffold(
            bottomBar = {
                BottomNavBar(
                    currentRoute = "settings",
                    onNavigate = { route ->
                        when (route) {
                            "home" -> onNavigateToHome()
                            "files" -> onNavigateToFiles()
                        }
                    }
                )
            }
        ) { paddingValues ->
            SettingsContent(
                uiState = uiState,
                viewModel = viewModel,
                isCompact = true,
                onNavigateToLicenseDetails = onNavigateToLicenseDetails,
                modifier = Modifier.padding(paddingValues)
            )
        }
    } else {
        // Tablet / Landscape Layout with NavigationRail
        Row(modifier = Modifier.fillMaxSize()) {
            LocalPdfNavigationRail(
                currentRoute = "settings",
                onNavigate = { route ->
                    when (route) {
                        "home" -> onNavigateToHome()
                        "files" -> onNavigateToFiles()
                    }
                }
            )

            SettingsContent(
                uiState = uiState,
                viewModel = viewModel,
                isCompact = false,
                onNavigateToLicenseDetails = onNavigateToLicenseDetails,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ThemeDialog(
    themeMode: ThemeMode,
    onDismiss: () -> Unit,
    onThemeSelected: (ThemeMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_theme), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(mode) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = themeMode == mode,
                            onClick = { onThemeSelected(mode) },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryRed)
                        )
                        Text(
                            text = when (mode) {
                                ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                                ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                                ThemeMode.DARK -> stringResource(R.string.theme_dark)
                            },
                            fontSize = 16.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = PrimaryRed)
            }
        }
    )
}

@Composable
private fun LanguageDialog(
    language: AppLanguage,
    onDismiss: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_language), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                AppLanguage.entries.forEach { lang ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(lang) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = language == lang,
                            onClick = { onLanguageSelected(lang) },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryRed)
                        )
                        Text(
                            text = lang.displayName,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = PrimaryRed)
            }
        }
    )
}


@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    isCompact: Boolean,
    onNavigateToLicenseDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = if (isCompact) 16.dp else 32.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(0.15f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(PrimaryRed)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(title = stringResource(R.string.settings_appearance))
        Spacer(modifier = Modifier.height(8.dp))

        SettingsItem(
            icon = Icons.Outlined.DarkMode,
            title = stringResource(R.string.settings_theme),
            subtitle = when (uiState.themeMode) {
                ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                ThemeMode.DARK -> stringResource(R.string.theme_dark)
            },
            onClick = { viewModel.showThemeDialog() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsItem(
            icon = Icons.Outlined.Language,
            title = stringResource(R.string.settings_language),
            subtitle = uiState.language.displayName,
            onClick = { viewModel.showLanguageDialog() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(title = stringResource(R.string.settings_about))
        Spacer(modifier = Modifier.height(8.dp))

        SettingsItem(
            icon = Icons.Outlined.Info,
            title = stringResource(R.string.settings_version),
            subtitle = "1.0.0",
            onClick = { }
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsItem(
            icon = Icons.Outlined.Policy,
            title = stringResource(R.string.settings_licenses),
            subtitle = "Open Source Libraries",
            onClick = { onNavigateToLicenseDetails() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsItem(
            icon = Icons.Outlined.Code,
            title = stringResource(R.string.settings_github),
            subtitle = "github.com/omerakpul/LocalPDF",
            onClick = {
                val intent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://github.com/omerakpul/LocalPDF")
                )
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // About info card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PrimaryRed.copy(alpha = 0.08f))
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Security,
                contentDescription = null,
                tint = PrimaryRed,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = stringResource(R.string.about_local_info),
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 19.sp,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary,
        letterSpacing = 1.sp
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryRed,
            modifier = Modifier.size(24.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}