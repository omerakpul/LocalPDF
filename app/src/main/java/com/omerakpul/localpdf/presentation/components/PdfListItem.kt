@file:OptIn(ExperimentalFoundationStyleApi::class)

package com.omerakpul.localpdf.presentation.components

import com.omerakpul.localpdf.domain.util.formatFileSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.omerakpul.localpdf.R
import com.omerakpul.localpdf.domain.model.Pdf
import com.omerakpul.localpdf.presentation.theme.ConvertColor
import com.omerakpul.localpdf.presentation.theme.ConvertIconColor
import com.omerakpul.localpdf.presentation.theme.SplitColor
import com.omerakpul.localpdf.presentation.theme.SplitIconColor
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnIcon
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItem
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItemActions
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItemContent
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItemDescription
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItemTitle
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItemVariant
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonSize
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant

/** Row for a stored PDF/Word file with a colour-coded type icon and an overflow menu. */
@Composable
fun PdfListItem(
    pdf: Pdf,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val isPdf = pdf.name.lowercase().endsWith(".pdf")
    val icon = if (isPdf) Icons.Rounded.PictureAsPdf else Icons.Rounded.Description
    val iconColor = if (isPdf) SplitIconColor else ConvertIconColor
    val tileColor = if (isPdf) SplitColor else ConvertColor

    ShadcnItem(
        modifier = modifier.fillMaxWidth(),
        variant = ShadcnItemVariant.Outline,
        onClick = onClick
    ) {
        // Own icon tile so the type colour (red for PDF, blue for Word) is easy to scan
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(tileColor),
            contentAlignment = Alignment.Center
        ) {
            ShadcnIcon(imageVector = icon, tint = iconColor, contentDescription = null)
        }
        ShadcnItemContent {
            ShadcnItemTitle(text = pdf.name)
            ShadcnItemDescription(text = formatFileSize(pdf.fileSize) + " • " + formatDate(pdf.createdAt))
        }
        ShadcnItemActions {
            Box {
                ShadcnButton(
                    onClick = { menuExpanded = true },
                    variant = ButtonVariant.Ghost,
                    size = ButtonSize.Icon
                ) {
                    ShadcnIcon(imageVector = Icons.Rounded.MoreVert, contentDescription = null)
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_share)) },
                        onClick = {
                            menuExpanded = false
                            onShare()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_rename)) },
                        onClick = {
                            menuExpanded = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_delete)) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

// Format timestamp to readable date
private fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
