package com.sorobanzen.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DarkAccentMoss,
    secondary = DarkAccentIndigo,
    tertiary = DarkAccentSakura,
    background = DarkBg,
    surface = DarkSurface,
    onPrimary = DarkBg,
    onSecondary = DarkBg,
    onTertiary = DarkBg,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = LightAccentMoss,
    secondary = LightAccentIndigo,
    tertiary = LightAccentSakura,
    background = LightBg,
    surface = LightSurface,
    onPrimary = LightBg,
    onSecondary = LightBg,
    onTertiary = LightBg,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    outline = LightBorder
)

@Composable
fun SorobanZenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            
            val windowInsetsController = WindowCompat.getInsetsController(window, view)
            windowInsetsController.isAppearanceLightStatusBars = !darkTheme
            windowInsetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
