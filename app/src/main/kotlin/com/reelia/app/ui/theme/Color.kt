package com.reelia.app.ui.theme

import androidx.compose.ui.graphics.Color

// Aubergine dark base — deep warm-purple, not pure black, flatters poster art.
val AppBackground = Color(0xFF13111C)
val AppSurface = Color(0xFF1D1B29)
val AppSurfaceContainer = Color(0xFF252235)
val AppSurfaceContainerHigh = Color(0xFF2E2A42)
val AppSurfaceVariant = Color(0xFF252235)
val AppSurfaceContainerLow = Color(0xFF191724)
val AppOutline = Color(0xFF3A3650)
val OnAppSurface = Color(0xFFF4F2F9)
val OnAppSurfaceVariant = Color(0xFF948FA8)

// Functional status-color system — every one of these means the same thing everywhere
// in the app (poster dots, progress rings, badges, stat cards), per the design brief.
val StatusWatchingCompleted = Color(0xFF33E4C6) // teal — watching / completed
val StatusPlanned = Color(0xFFFFB84D) // amber — planned / on hold
val StatusWantToWatch = Color(0xFF8C8FFF) // periwinkle — plan to watch
val StatusFavorite = Color(0xFFFF6B8B) // coral — favorite flag (independent of watch status)

// Text/icon color for use on top of any of the bright status colors above — same tone as
// the app background, so contrast stays consistent across all four badge colors.
val OnStatusColor = AppBackground

// Teal (the most universally "primary" interactive color per the brief — active nav,
// watched checkmarks, chart highlights) mapped into Material3's primary/primaryContainer slots.
val TealContainer = Color(0xFF1B3D38)
val OnTealContainer = Color(0xFFB8FFF2)

// Light scheme (not a design priority — app is dark-first, kept for completeness only)
val LightBackground = Color(0xFFFFFBF9)
val LightSurfaceVariant = Color(0xFFF0DED8)
val OnLightSurfaceVariant = Color(0xFF52443F)
val PrimaryLight = Color(0xFF6B4EFF)
