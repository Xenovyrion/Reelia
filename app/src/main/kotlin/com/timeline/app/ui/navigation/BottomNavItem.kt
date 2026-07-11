package com.timeline.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val outlineIcon: ImageVector,
) {
    LIBRARY(Routes.LIBRARY, "Bibliothèque", Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary),
    UP_NEXT(Routes.UP_NEXT, "À venir", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    STATS(Routes.STATS, "Stats", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    SETTINGS(Routes.SETTINGS, "Réglages", Icons.Filled.Settings, Icons.Outlined.Settings),
}
