package com.reelia.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.ui.graphics.vector.ImageVector
import com.reelia.app.R

enum class BottomNavItem(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val outlineIcon: ImageVector,
) {
    HOME(Routes.HOME, R.string.home_title, Icons.Filled.Home, Icons.Outlined.Home),
    SERIES(Routes.SERIES, R.string.nav_series, Icons.Filled.Tv, Icons.Outlined.Tv),
    FILMS(Routes.FILMS, R.string.nav_films, Icons.Filled.Movie, Icons.Outlined.Movie),
    PROFILE(Routes.PROFILE, R.string.nav_profile, Icons.Filled.Person, Icons.Outlined.Person),
}
