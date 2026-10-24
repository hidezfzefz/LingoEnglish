package com.lingo.app.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// A warm, editorial palette: cream paper, forest ink, and a fresh lime accent.
val Cream = Color(0xFFFAF6EE)
val Paper = Color(0xFFFFFCF6)
val Ink = Color(0xFF17251F)
val InkSoft = Color(0xFF32443B)
val Forest = Color(0xFF143D31)
val Emerald = Color(0xFF2A7D5F)
val Mint = Color(0xFFE3F1E7)
val MintDeep = Color(0xFFCFE7D7)
val Lime = Color(0xFFD9F27C)
val LimeSoft = Color(0xFFEEF7CD)
val Coral = Color(0xFFEC7256)
val Peach = Color(0xFFFCE5DB)
val Blue = Color(0xFF3B6E9E)
val Sky = Color(0xFFE3EDF8)
val Purple = Color(0xFF6E5AA6)
val Lavender = Color(0xFFECE7F8)
val Teal = Color(0xFF2F7D7A)
val Seafoam = Color(0xFFDFF3EE)
val DeepTeal = Color(0xFF1F504C)
val Gold = Color(0xFFE9B949)
val Sand = Color(0xFFF1E9DA)
val Line = Color(0xFFE8E0D2)
val Muted = Color(0xFF6E7D75)
val Danger = Color(0xFFC2493D)
val PureWhite = Color(0xFFFFFFFF)

private val LingoColors = lightColorScheme(
    primary = Forest,
    onPrimary = Cream,
    primaryContainer = Mint,
    onPrimaryContainer = Forest,
    secondary = Lime,
    onSecondary = Ink,
    secondaryContainer = LimeSoft,
    onSecondaryContainer = Ink,
    tertiary = Coral,
    onTertiary = PureWhite,
    tertiaryContainer = Peach,
    onTertiaryContainer = Ink,
    background = Cream,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Sand,
    onSurfaceVariant = Muted,
    outline = Line,
    outlineVariant = Line,
    error = Danger,
    onError = PureWhite,
)

private val Serif = FontFamily.Serif
private val Sans = FontFamily.SansSerif

val LingoTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 46.sp,
        lineHeight = 50.sp,
        letterSpacing = (-1.2).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 38.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.8).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 37.sp,
        letterSpacing = (-0.4).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.3).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 25.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.3.sp,
    ),
)

val LingoShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun LingoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LingoColors,
        typography = LingoTypography,
        shapes = LingoShapes,
        content = content,
    )
}
