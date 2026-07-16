package com.sorobanzen.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val DisplayFamily = FontFamily.Serif
private val InterfaceFamily = FontFamily.SansSerif

// Android's system CJK fonts keep the app lightweight and render Japanese reliably.
// Serif is reserved for a few expressive headings; controls and numerals stay sans-serif.
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Light,
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = (-0.8).sp
    ),
    displayMedium = TextStyle(
        fontFamily = InterfaceFamily,
        fontWeight = FontWeight.Light,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.4).sp
    ),
    displaySmall = TextStyle(
        fontFamily = InterfaceFamily,
        fontWeight = FontWeight.Light,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.2).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.1.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.1.sp
    ),
    titleLarge = TextStyle(
        fontFamily = InterfaceFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = InterfaceFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = InterfaceFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = InterfaceFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = InterfaceFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp
    ),
    bodySmall = TextStyle(
        fontFamily = InterfaceFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontFamily = InterfaceFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = InterfaceFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = InterfaceFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.2.sp
    )
)
