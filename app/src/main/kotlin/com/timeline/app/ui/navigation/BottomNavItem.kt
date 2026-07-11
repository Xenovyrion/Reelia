package com.timeline.app.ui.navigation

import androidx.annotation.StringRes
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
import com.timeline.app.R

enum class BottomNavItem(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val outlineIcon: ImageVector,
) {
    SERIES(Routes.SERIES, R.string.nav_series, Icons.Filled.Tv, Icons.Outlined.Tv),
    FILMS(Routes.FILMS, R.string.nav_films, Icons.Filled.Movie, Icons.Outlined.Movie),
    SEARCH(Routes.SEARCH, R.string.nav_search, Icons.Filled.Search, Icons.Outlined.Search),
    STATS(Routes.STATS, R.string.nav_stats, Icons.Filled.BarChart, Icons.Outlined.BarChart),
    SETTINGS(Routes.SETTINGS, R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings),
}
