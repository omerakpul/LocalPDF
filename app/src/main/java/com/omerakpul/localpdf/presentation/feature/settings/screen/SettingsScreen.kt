@file:OptIn(ExperimentalFoundationStyleApi::class)

package com.omerakpul.localpdf.presentation.feature.settings.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSeparator
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItemVariant
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItemTitle
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItemDescription
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItemContent
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItemActions
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItem
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnIcon
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialogTitle
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialogHeader
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialogFooter
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialog
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import com.omerakpul.localpdf.presentation.theme.OutlineColor
import androidx.compose.runtime.remember
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
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
    ChoiceDialog(
        title = stringResource(R.string.settings_theme),
        options = ThemeMode.entries.toList(),
        selected = themeMode,
        label = { mode ->
            when (mode) {
                ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                ThemeMode.DARK -> stringResource(R.string.theme_dark)
            }
        },
        onSelect = onThemeSelected,
        onDismiss = onDismiss
    )
}

@Composable
private fun LanguageDialog(
    language: AppLanguage,
    onDismiss: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    ChoiceDialog(
        title = stringResource(R.string.settings_language),
        options = AppLanguage.entries.toList(),
        selected = language,
        label = { it.displayName },
        onSelect = onLanguageSelected,
        onDismiss = onDismiss
    )
}

/** Dialog that lists a few options; the selected one gets an outline and a check mark. */
@Composable
private fun <T> ChoiceDialog(
    title: String,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit
) {
    ShadcnDialog(visible = true, onDismissRequest = onDismiss) {
        ShadcnDialogHeader {
            ShadcnDialogTitle(title)
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEach { option ->
                val isSelected = option == selected
                ShadcnItem(
                    variant = if (isSelected) ShadcnItemVariant.Outline else ShadcnItemVariant.Default,
                    onClick = { onSelect(option) }
                ) {
                    ShadcnItemContent {
                        ShadcnItemTitle(text = label(option))
                    }
                    if (isSelected) {
                        ShadcnItemActions {
                            ShadcnIcon(
                                imageVector = Icons.Rounded.Check,
                                tint = PrimaryRed,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
        ShadcnDialogFooter {
            ShadcnButton(onClick = onDismiss, variant = ButtonVariant.Outline) {
                ShadcnText(stringResource(R.string.action_cancel))
            }
        }
    }
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
    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = if (isCompact) 16.dp else 32.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            modifier = Modifier.padding(top = 20.dp, bottom = 20.dp)
        )

        SettingsGroup(title = stringResource(R.string.settings_appearance)) {
            SettingsRow(
                icon = Icons.Outlined.DarkMode,
                title = stringResource(R.string.settings_theme),
                subtitle = when (uiState.themeMode) {
                    ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                    ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                    ThemeMode.DARK -> stringResource(R.string.theme_dark)
                },
                onClick = { viewModel.showThemeDialog() }
            )
            ShadcnSeparator()
            SettingsRow(
                icon = Icons.Outlined.Language,
                title = stringResource(R.string.settings_language),
                subtitle = uiState.language.displayName,
                onClick = { viewModel.showLanguageDialog() }
            )
        }

        SettingsGroup(title = stringResource(R.string.settings_about)) {
            SettingsRow(
                icon = Icons.Outlined.Info,
                title = stringResource(R.string.settings_version),
                subtitle = versionName,
                onClick = null
            )
            ShadcnSeparator()
            SettingsRow(
                icon = Icons.Outlined.Policy,
                title = stringResource(R.string.settings_licenses),
                subtitle = stringResource(R.string.settings_licenses_desc),
                onClick = { onNavigateToLicenseDetails() }
            )
            ShadcnSeparator()
            SettingsRow(
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
        }

        // Privacy note
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PrimaryRed.copy(alpha = 0.06f))
                .border(1.dp, PrimaryRed.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            ShadcnIcon(
                imageVector = Icons.Outlined.Security,
                tint = PrimaryRed,
                contentDescription = null
            )
            ShadcnText(
                text = stringResource(R.string.about_local_info),
                style = ShadcnTextStyle.BodySmall,
                muted = true,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/** A titled group of rows inside one bordered card. */
@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    ShadcnText(
        text = title.uppercase(),
        style = ShadcnTextStyle.LabelSmall,
        muted = true,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(1.dp, OutlineColor, RoundedCornerShape(16.dp)),
        content = content
    )
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: (() -> Unit)?
) {
    ShadcnItem(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PrimaryRed.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            ShadcnIcon(imageVector = icon, tint = PrimaryRed, contentDescription = null)
        }
        ShadcnItemContent {
            ShadcnItemTitle(text = title)
            if (!subtitle.isNullOrEmpty()) {
                ShadcnItemDescription(text = subtitle)
            }
        }
        if (onClick != null) {
            ShadcnItemActions {
                ShadcnIcon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    small = true
                )
            }
        }
    }
}
