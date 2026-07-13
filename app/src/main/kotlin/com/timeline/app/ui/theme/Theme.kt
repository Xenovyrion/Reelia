package com.timeline.app.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = StatusWatchingCompleted,
    onPrimary = OnStatusColor,
    primaryContainer = TealContainer,
    onPrimaryContainer = OnTealContainer,
    secondary = StatusWantToWatch,
    onSecondary = OnStatusColor,
    tertiary = StatusPlanned,
    onTertiary = OnStatusColor,
    background = AppBackground,
    onBackground = OnAppSurface,
    surface = AppSurface,
    onSurface = OnAppSurface,
    surfaceVariant = AppSurfaceVariant,
    onSurfaceVariant = OnAppSurfaceVariant,
    surfaceContainer = AppSurfaceContainer,
    surfaceContainerHigh = AppSurfaceContainerHigh,
    surfaceContainerLow = AppSurfaceContainerLow,
    outline = AppOutline,
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = Color.White,
    tertiary = StatusPlanned,
    background = LightBackground,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = OnLightSurfaceVariant,
)

@Composable
fun TimeLineTheme(
    // Reelia has one deliberately-designed dark "Aubergine" look — it doesn't follow the
    // device's system light/dark setting, otherwise a phone in light mode falls back to the
    // much less finished LightColorScheme below and renders completely differently.
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}
