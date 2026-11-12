package com.omerakpul.localpdf.presentation.components

import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.getValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.border
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import com.omerakpul.localpdf.R
import com.omerakpul.localpdf.presentation.theme.CardBackground
import com.omerakpul.localpdf.presentation.theme.OutlineColor
import com.omerakpul.localpdf.presentation.theme.PrimaryRed
import com.omerakpul.localpdf.presentation.theme.TextSecondary

/** Shared tab definitions for the bottom bar and the navigation rail. */
val bottomNavItems: List<BottomNavItem>
    get() = listOf(
        BottomNavItem(
            route = "home",
            titleResId = R.string.nav_home,
            selectedIcon = Icons.Rounded.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        BottomNavItem(
            route = "files",
            titleResId = R.string.nav_files,
            selectedIcon = Icons.Rounded.Folder,
            unselectedIcon = Icons.Outlined.Folder
        ),
        BottomNavItem(
            route = "settings",
            titleResId = R.string.nav_settings,
            selectedIcon = Icons.Rounded.Settings,
            unselectedIcon = Icons.Outlined.Settings
        )
    )

/** Navigation colours: crimson for the selected tab inside a soft crimson pill. */
@Composable
internal fun navBarItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = PrimaryRed,
    selectedTextColor = PrimaryRed,
    indicatorColor = PrimaryRed.copy(alpha = 0.14f),
    unselectedIconColor = TextSecondary,
    unselectedTextColor = TextSecondary
)

@Composable
internal fun navRailItemColors() = NavigationRailItemDefaults.colors(
    selectedIconColor = PrimaryRed,
    selectedTextColor = PrimaryRed,
    indicatorColor = PrimaryRed.copy(alpha = 0.14f),
    unselectedIconColor = TextSecondary,
    unselectedTextColor = TextSecondary
)

/**
 * Compact floating navigation dock with Home, Files and Settings.
 * Only icons are shown; the selected tab expands into a crimson pill that also shows its label.
 */
@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val shape = RoundedCornerShape(32.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 10.dp,
                    shape = shape,
                    ambientColor = Color.Black.copy(alpha = 0.25f),
                    spotColor = Color.Black.copy(alpha = 0.25f)
                )
                .clip(shape)
                .background(CardBackground)
                .border(1.dp, OutlineColor, shape)
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                NavPill(
                    item = item,
                    selected = currentRoute == item.route,
                    onClick = { onNavigate(item.route) }
                )
            }
        }
    }
}

@Composable
private fun NavPill(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background by animateColorAsState(
        targetValue = if (selected) PrimaryRed else Color.Transparent,
        animationSpec = tween(150),
        label = "navPillBackground"
    )
    val content by animateColorAsState(
        targetValue = if (selected) Color.White else TextSecondary,
        animationSpec = tween(150),
        label = "navPillContent"
    )
    val title = stringResource(item.titleResId)

    Row(
        modifier = Modifier
            .height(44.dp)
            .animateContentSize(tween(180))
            .clip(RoundedCornerShape(22.dp))
            .background(background)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = if (selected) 18.dp else 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
            contentDescription = if (selected) null else title,
            tint = content,
            modifier = Modifier.size(24.dp)
        )
        if (selected) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = content,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

/**
 * Data class for bottom navigation items.
 */
data class BottomNavItem(
    val route: String,
    val titleResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)
