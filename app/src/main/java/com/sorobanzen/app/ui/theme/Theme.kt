package com.sorobanzen.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = DarkAccentMoss,
    secondary = DarkAccentIndigo,
    tertiary = DarkAccentSakura,
    error = DarkAccentSakura,
    background = DarkBg,
    surface = DarkSurface,
    onPrimary = DarkBg,
    onSecondary = DarkBg,
    onTertiary = DarkBg,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary,
    surfaceVariant = DarkSurfaceMuted,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceRaised,
    outline = DarkBorder,
    outlineVariant = DarkBorder.copy(alpha = 0.72f),
    inversePrimary = LightAccentMoss
)

private val LightColorScheme = lightColorScheme(
    primary = LightAccentMoss,
    secondary = LightAccentIndigo,
    tertiary = LightAccentSakura,
    error = LightAccentSakura,
    background = LightBg,
    surface = LightSurface,
    onPrimary = LightBg,
    onSecondary = LightBg,
    onTertiary = LightBg,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary,
    surfaceVariant = LightSurfaceMuted,
    surfaceContainer = LightSurface,
    surfaceContainerHigh = LightSurfaceRaised,
    outline = LightBorder,
    outlineVariant = LightBorder.copy(alpha = 0.76f),
    inversePrimary = DarkAccentMoss
)

private val ZenShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun SorobanZenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ZenShapes,
        content = content
    )
}
