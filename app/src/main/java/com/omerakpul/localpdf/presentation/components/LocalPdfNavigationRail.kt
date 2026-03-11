package com.omerakpul.localpdf.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.omerakpul.localpdf.R

/**
 * Vertical navigation rail for Medium and Expanded screens (Tablets & Landscape).
 */
@Composable
fun LocalPdfNavigationRail(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(
            route = "home",
            titleResId = R.string.nav_home,
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        BottomNavItem(
            route = "files",
            titleResId = R.string.nav_files,
            selectedIcon = Icons.Filled.Folder,
            unselectedIcon = Icons.Outlined.Folder
        ),
        BottomNavItem(
            route = "settings",
            titleResId = R.string.nav_settings,
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings
        )
    )

    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        header = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.lpdflogo),
                    contentDescription = stringResource(id = R.string.app_name),
                    tint = MaterialTheme.colorScheme.primary, // Or use TextPrimary if you have it in scope
                    modifier = Modifier.height(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    ) {
        Spacer(modifier = Modifier.weight(1f)) // pushes items to center

        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationRailItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = stringResource(item.titleResId)
                    )
                },
                label = {
                    Text(text = stringResource(item.titleResId))
                }
            )
        }
        
        Spacer(modifier = Modifier.weight(1f)) // pushes items to center
    }
}
