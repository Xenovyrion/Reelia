package com.timeline.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector
import com.timeline.app.R

enum class BottomNavItem(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val outlineIcon: ImageVector,
) {
    HOME(Routes.HOME, R.string.home_title, Icons.Filled.Home, Icons.Outlined.Home),
    LIBRARY(Routes.LIBRARY, R.string.library_title, Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary),
    PROFILE(Routes.PROFILE, R.string.nav_profile, Icons.Filled.Person, Icons.Outlined.Person),
}
