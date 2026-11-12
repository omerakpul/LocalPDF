@file:OptIn(ExperimentalFoundationStyleApi::class)

package com.omerakpul.localpdf.presentation.components

import androidx.compose.foundation.style.ExperimentalFoundationStyleApi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonSize
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnEmpty
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnEmptyContent
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnEmptyDescription
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnEmptyHeader
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnEmptyMedia
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnEmptyTitle
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnIcon
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItem
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItemActions
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItemContent
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItemDescription
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItemMedia
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItemTitle
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItemVariant
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSpinner
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextField
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
import com.omerakpul.localpdf.presentation.theme.BackgroundColor
import com.omerakpul.localpdf.presentation.theme.OutlineColor
import com.omerakpul.localpdf.presentation.theme.TextPrimary

/**
 * Shared building blocks used by every screen, so the whole app has one visual language.
 */

/** Top bar with an optional back button, a bold title and a hairline underneath. */
@Composable
fun AppTopBar(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BackgroundColor)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                ShadcnButton(
                    onClick = onBack,
                    variant = ButtonVariant.Ghost,
                    size = ButtonSize.Icon
                ) {
                    ShadcnIcon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }
            ShadcnText(
                text = title,
                style = ShadcnTextStyle.TitleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            actions()
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(OutlineColor)
        )
    }
}

/** Standard page for a tool: top bar, snackbar host and a content column on the app background. */
@Composable
fun AppScreen(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
    actions: @Composable RowScope.() -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        modifier = modifier,
        containerColor = BackgroundColor,
        topBar = { AppTopBar(title = title, onBack = onBack, actions = actions) },
        snackbarHost = { snackbarHostState?.let { SnackbarHost(it) } }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(paddingValues)
                .padding(contentPadding),
            content = content
        )
    }
}

/** Empty-state placeholder: icon, title, description and an optional action. */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    action: (@Composable ColumnScope.() -> Unit)? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ShadcnEmpty(modifier = Modifier.padding(24.dp)) {
            ShadcnEmptyHeader {
                ShadcnEmptyMedia {
                    ShadcnIcon(imageVector = icon, contentDescription = null)
                }
                ShadcnEmptyTitle(text = title)
                ShadcnEmptyDescription(text = description)
            }
            if (action != null) {
                ShadcnEmptyContent(content = action)
            }
        }
    }
}

/** Full-width primary action. Shows a spinner instead of the icon while [loading]. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    variant: ButtonVariant = ButtonVariant.Default,
    contentColor: Color = Color.Unspecified
) {
    ShadcnButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled && !loading,
        variant = variant,
        size = ButtonSize.Lg
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (loading) {
                ShadcnSpinner(modifier = Modifier.size(18.dp))
            } else if (icon != null) {
                ShadcnIcon(imageVector = icon, tint = contentColor, contentDescription = null)
            }
            ShadcnText(text = text, fontWeight = FontWeight.SemiBold, color = contentColor)
        }
    }
}

/** Outlined, full-width secondary action. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    PrimaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        icon = icon,
        enabled = enabled,
        variant = ButtonVariant.Outline
    )
}

/** Search input with a leading magnifier and a clear button when it has text. */
@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    ShadcnTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = placeholder,
        leadingIcon = { ShadcnIcon(imageVector = Icons.Rounded.Search, contentDescription = null) },
        trailingIcon = if (value.isNotEmpty()) {
            {
                ShadcnButton(
                    onClick = { onValueChange("") },
                    variant = ButtonVariant.Ghost,
                    size = ButtonSize.Icon
                ) {
                    ShadcnIcon(imageVector = Icons.Rounded.Close, contentDescription = null, small = true)
                }
            }
        } else null,
        singleLine = true
    )
}

/** Card describing the currently selected file, with a button to remove it. */
@Composable
fun FileSummaryCard(
    name: String,
    detail: String,
    icon: ImageVector,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    ShadcnItem(
        modifier = modifier.fillMaxWidth(),
        variant = ShadcnItemVariant.Outline
    ) {
        ShadcnItemMedia {
            ShadcnIcon(imageVector = icon, contentDescription = null)
        }
        ShadcnItemContent {
            ShadcnItemTitle(text = name)
            ShadcnItemDescription(text = detail)
        }
        ShadcnItemActions {
            ShadcnButton(
                onClick = onRemove,
                variant = ButtonVariant.Ghost,
                size = ButtonSize.Icon
            ) {
                ShadcnIcon(imageVector = Icons.Rounded.Close, contentDescription = null)
            }
        }
    }
}

/** Centered loading indicator. */
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        ShadcnSpinner()
    }
}

/** Small section label used above groups of controls. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    ShadcnText(
        text = text,
        style = ShadcnTextStyle.LabelLarge,
        fontWeight = FontWeight.SemiBold,
        color = TextPrimary,
        textAlign = TextAlign.Start,
        modifier = modifier
    )
}
