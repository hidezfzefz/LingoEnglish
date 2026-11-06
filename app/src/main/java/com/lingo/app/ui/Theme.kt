package com.lingo.app.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingo.app.data.ThemeMode

// Dynamic aliases for the active appearance. The values come from
// LingoPalettes.Paper or LingoPalettes.Night through LingoTheme, so existing
// screens keep their call sites while following the learner's choice.
val Cream: Color @Composable get() = LocalLingoPalette.current.cream
val Paper: Color @Composable get() = LocalLingoPalette.current.paper
val Ink: Color @Composable get() = LocalLingoPalette.current.ink
val InkSoft: Color @Composable get() = LocalLingoPalette.current.inkSoft
val Forest: Color @Composable get() = LocalLingoPalette.current.forest
val Emerald: Color @Composable get() = LocalLingoPalette.current.emerald
val Mint: Color @Composable get() = LocalLingoPalette.current.mint
val MintDeep: Color @Composable get() = LocalLingoPalette.current.mintDeep
val Lime: Color @Composable get() = LocalLingoPalette.current.lime
val LimeSoft: Color @Composable get() = LocalLingoPalette.current.limeSoft
val Coral: Color @Composable get() = LocalLingoPalette.current.coral
val CoralDeep: Color @Composable get() = LocalLingoPalette.current.coralDeep
val Peach: Color @Composable get() = LocalLingoPalette.current.peach
val Blue: Color @Composable get() = LocalLingoPalette.current.blue
val Sky: Color @Composable get() = LocalLingoPalette.current.sky
val Purple: Color @Composable get() = LocalLingoPalette.current.purple
val Lavender: Color @Composable get() = LocalLingoPalette.current.lavender
val Teal: Color @Composable get() = LocalLingoPalette.current.teal
val Seafoam: Color @Composable get() = LocalLingoPalette.current.seafoam
val DeepTeal: Color @Composable get() = LocalLingoPalette.current.deepTeal
val Gold: Color @Composable get() = LocalLingoPalette.current.gold
val Sand: Color @Composable get() = LocalLingoPalette.current.sand
val Line: Color @Composable get() = LocalLingoPalette.current.line
// Paper uses #59685F so small secondary text clears 4.5:1 on cream, paper,
// and sand; Night uses a light muted that clears the same bar on dark surfaces.
val Muted: Color @Composable get() = LocalLingoPalette.current.muted
val Danger: Color @Composable get() = LocalLingoPalette.current.danger
val PureWhite: Color @Composable get() = LocalLingoPalette.current.pureWhite

// New semantic tokens. OnCoral replaces white text on coral fills, which was
// below 4.5:1 in the paper palette.
val OnCoral: Color @Composable get() = LocalLingoPalette.current.onCoral
val OnBrandMuted: Color @Composable get() = LocalLingoPalette.current.onBrandMuted
val FocusRing: Color @Composable get() = LocalLingoPalette.current.focus

private fun lingoColorScheme(p: LingoPalette) = if (p.dark) {
    androidx.compose.material3.darkColorScheme(
        primary = p.forest,
        onPrimary = p.cream,
        primaryContainer = p.mint,
        onPrimaryContainer = p.forest,
        secondary = p.lime,
        onSecondary = p.ink,
        secondaryContainer = p.limeSoft,
        onSecondaryContainer = p.ink,
        tertiary = p.coral,
        onTertiary = p.onCoral,
        tertiaryContainer = p.peach,
        onTertiaryContainer = p.ink,
        background = p.cream,
        onBackground = p.ink,
        surface = p.paper,
        onSurface = p.ink,
        surfaceVariant = p.sand,
        onSurfaceVariant = p.muted,
        outline = p.line,
        outlineVariant = p.line,
        error = p.danger,
        onError = p.pureWhite,
    )
} else {
    lightColorScheme(
        primary = p.forest,
        onPrimary = p.cream,
        primaryContainer = p.mint,
        onPrimaryContainer = p.forest,
        secondary = p.lime,
        onSecondary = p.ink,
        secondaryContainer = p.limeSoft,
        onSecondaryContainer = p.ink,
        tertiary = p.coral,
        onTertiary = p.onCoral,
        tertiaryContainer = p.peach,
        onTertiaryContainer = p.ink,
        background = p.cream,
        onBackground = p.ink,
        surface = p.paper,
        onSurface = p.ink,
        surfaceVariant = p.sand,
        onSurfaceVariant = p.muted,
        outline = p.line,
        outlineVariant = p.line,
        error = p.danger,
        onError = p.pureWhite,
    )
}

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
fun LingoTheme(
    mode: ThemeMode = ThemeMode.PAPER,
    content: @Composable () -> Unit,
) {
    val palette = LingoPalettes.of(mode)
    CompositionLocalProvider(LocalLingoPalette provides palette) {
        MaterialTheme(
            colorScheme = lingoColorScheme(palette),
            typography = LingoTypography,
            shapes = LingoShapes,
            content = content,
        )
    }
}
