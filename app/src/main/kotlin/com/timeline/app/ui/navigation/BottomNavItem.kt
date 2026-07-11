package com.timeline.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    LIBRARY(Routes.LIBRARY, "Bibliothèque", Icons.Filled.VideoLibrary),
    UP_NEXT(Routes.UP_NEXT, "À venir", Icons.Filled.CalendarMonth),
    STATS(Routes.STATS, "Stats", Icons.Filled.BarChart),
    SETTINGS(Routes.SETTINGS, "Réglages", Icons.Filled.Settings),
}
