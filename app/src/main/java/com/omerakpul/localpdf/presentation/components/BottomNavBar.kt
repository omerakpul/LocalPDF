package com.omerakpul.localpdf.presentation.components

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
 * Bottom navigation bar with Home, Files, and Settings tabs.
 * Flat surface separated from the content by a hairline instead of a purple tonal overlay.
 */
@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(OutlineColor)
        )
        NavigationBar(
            containerColor = CardBackground,
            tonalElevation = 0.dp
        ) {
            bottomNavItems.forEach { item ->
                val isSelected = currentRoute == item.route
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onNavigate(item.route) },
                    colors = navBarItemColors(),
                    icon = {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(item.titleResId),
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    }
                )
            }
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
