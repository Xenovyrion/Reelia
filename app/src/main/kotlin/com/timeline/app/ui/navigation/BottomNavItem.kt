package com.timeline.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val outlineIcon: ImageVector,
) {
    SERIES(Routes.SERIES, "Séries", Icons.Filled.Tv, Icons.Outlined.Tv),
    FILMS(Routes.FILMS, "Films", Icons.Filled.Movie, Icons.Outlined.Movie),
    SEARCH(Routes.SEARCH, "Rechercher", Icons.Filled.Search, Icons.Outlined.Search),
    STATS(Routes.STATS, "Stats", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    SETTINGS(Routes.SETTINGS, "Réglages", Icons.Filled.Settings, Icons.Outlined.Settings),
}
