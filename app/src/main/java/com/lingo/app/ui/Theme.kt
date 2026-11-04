package com.lingo.app.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// A warm, editorial palette: cream paper, forest ink, and a fresh lime accent.
// The canonical values live in LingoPalettes.Paper; these aliases keep the
// existing screens source-compatible until the Night migration wires every
// call site to LocalLingoPalette.
val Cream = LingoPalettes.Paper.cream
val Paper = LingoPalettes.Paper.paper
val Ink = LingoPalettes.Paper.ink
val InkSoft = LingoPalettes.Paper.inkSoft
val Forest = LingoPalettes.Paper.forest
val Emerald = LingoPalettes.Paper.emerald
val Mint = LingoPalettes.Paper.mint
val MintDeep = LingoPalettes.Paper.mintDeep
val Lime = LingoPalettes.Paper.lime
val LimeSoft = LingoPalettes.Paper.limeSoft
val Coral = LingoPalettes.Paper.coral
val CoralDeep = LingoPalettes.Paper.coralDeep
val Peach = LingoPalettes.Paper.peach
val Blue = LingoPalettes.Paper.blue
val Sky = LingoPalettes.Paper.sky
val Purple = LingoPalettes.Paper.purple
val Lavender = LingoPalettes.Paper.lavender
val Teal = LingoPalettes.Paper.teal
val Seafoam = LingoPalettes.Paper.seafoam
val DeepTeal = LingoPalettes.Paper.deepTeal
val Gold = LingoPalettes.Paper.gold
val Sand = LingoPalettes.Paper.sand
val Line = LingoPalettes.Paper.line
// Darkened from #6E7D75 so small secondary text clears 4.5:1 on cream,
// paper, and sand surfaces (5.46:1, 5.74:1, and 4.88:1).
val Muted = LingoPalettes.Paper.muted
val Danger = LingoPalettes.Paper.danger
val PureWhite = LingoPalettes.Paper.pureWhite

// New semantic tokens. OnCoral replaces white text on coral fills, which was
// below 4.5:1 in the paper palette.
val OnCoral = LingoPalettes.Paper.onCoral
val OnBrandMuted = LingoPalettes.Paper.onBrandMuted
val FocusRing = LingoPalettes.Paper.focus

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
    onTertiary = OnCoral,
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

/** Shared motion timings. Compose still honours the system animation scale. */
object LingoMotion {
    const val quick = 150
    const val standard = 240
    const val expressive = 600
    const val celebration = 800
}

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
