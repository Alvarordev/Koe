package com.example.tracker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.tracker.data.preferences.ThemePreference

private val LightColorScheme = lightColorScheme(
    primary = Blue,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    secondaryContainer = SurfaceVariantLight,
    onSecondaryContainer = OnSurfaceLight
)

private val DarkColorScheme = darkColorScheme(
    primary = BlueDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = BlueDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = OnSurfaceDark
)

@Composable
fun TrackerTheme(
    themePreference: ThemePreference = ThemePreference.System,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themePreference) {
        ThemePreference.Light -> false
        ThemePreference.Dark -> true
        ThemePreference.System -> systemDark
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
