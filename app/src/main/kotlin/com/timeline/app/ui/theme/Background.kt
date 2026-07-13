package com.timeline.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush

/** The signature radial wash first introduced on the login screen — applied once at the app
 * root (see MainActivity) behind every screen's transparent Scaffold container, so the whole
 * app shares the same atmosphere instead of just the login screen. */
@Composable
fun appBackgroundBrush(): Brush = Brush.radialGradient(
    colors = listOf(
        StatusWantToWatch.copy(alpha = 0.18f),
        StatusFavorite.copy(alpha = 0.10f),
        AppBackground,
    ),
)
