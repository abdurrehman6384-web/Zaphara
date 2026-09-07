package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.IrisCyan

sealed class BottomTab(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomTab("home", "RGS Core", Icons.Default.RadioButtonChecked)
    object Chat : BottomTab("chat", "Terminal", Icons.Default.Terminal)
    object Vision : BottomTab("vision", "Vision HUD", Icons.Default.CropFree)
    object Tools : BottomTab("agents", "Tools", Icons.Default.Widgets)
    object License : BottomTab("license", "Portal", Icons.Default.VpnKey)
    object Settings : BottomTab("settings", "Settings", Icons.Default.Tune)
    object System : BottomTab("system", "System", Icons.Default.Memory)
}

@Composable
fun BottomNavBar(
    currentRoute: String,
    onTabSelected: (String) -> Unit
) {
    val tabs = listOf(
        BottomTab.Home,
        BottomTab.Chat,
        BottomTab.Vision,
        BottomTab.Tools,
        BottomTab.System
    )

    Surface(
        color = Color(0xFF070B18),
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = GlassBorder)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            tonalElevation = 0.dp
        ) {
            tabs.forEach { tab ->
                val isSelected = currentRoute == tab.route
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(tab.route) },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = if (isSelected) IrisCyan else Color(0xFF64748B)
                        )
                    },
                    label = {
                        Text(
                            text = tab.title,
                            color = if (isSelected) IrisCyan else Color(0xFF64748B),
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = IrisCyan.copy(alpha = 0.15f)
                    )
                )
            }
        }
    }
}
