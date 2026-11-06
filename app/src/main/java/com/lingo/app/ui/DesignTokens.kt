package com.lingo.app.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lingo.app.data.ThemeMode

/**
 * One complete set of colour tokens. The names match the existing palette so
 * the v1.10 migration can replace a raw colour with a palette getter without
 * renaming every call site.
 *
 * [onCoral] is new: white text on the coral fill was below 4.5:1. Coral-filled
 * buttons now use ink text in both appearances.
 * [onBrandMuted] replaces low-alpha cream text on forest/lime fills, where the
 * alpha blend could fall below 4.5:1 in Night.
 * [focus] is the 3:1 focus-ring colour, not the decorative hairline colour.
 */
data class LingoPalette(
    val dark: Boolean,
    val cream: Color,
    val paper: Color,
    val ink: Color,
    val inkSoft: Color,
    val forest: Color,
    val emerald: Color,
    val mint: Color,
    val mintDeep: Color,
    val lime: Color,
    val limeSoft: Color,
    val coral: Color,
    val coralDeep: Color,
    val peach: Color,
    val blue: Color,
    val sky: Color,
    val purple: Color,
    val lavender: Color,
    val teal: Color,
    val seafoam: Color,
    val deepTeal: Color,
    val gold: Color,
    val sand: Color,
    val line: Color,
    val muted: Color,
    val danger: Color,
    val pureWhite: Color,
    val onCoral: Color,
    val onBrandMuted: Color,
    val focus: Color,
)

object LingoPalettes {

    /** The existing warm paper/forest identity. Values are unchanged. */
    val Paper = LingoPalette(
        dark = false,
        cream = Color(0xFFFAF6EE),
        paper = Color(0xFFFFFCF6),
        ink = Color(0xFF17251F),
        inkSoft = Color(0xFF32443B),
        forest = Color(0xFF143D31),
        emerald = Color(0xFF2A7D5F),
        mint = Color(0xFFE3F1E7),
        mintDeep = Color(0xFFCFE7D7),
        lime = Color(0xFFD9F27C),
        limeSoft = Color(0xFFEEF7CD),
        coral = Color(0xFFEC7256),
        coralDeep = Color(0xFF8C3B27),
        peach = Color(0xFFFCE5DB),
        blue = Color(0xFF3B6E9E),
        sky = Color(0xFFE3EDF8),
        purple = Color(0xFF6E5AA6),
        lavender = Color(0xFFECE7F8),
        teal = Color(0xFF2F7D7A),
        seafoam = Color(0xFFDFF3EE),
        deepTeal = Color(0xFF1F504C),
        gold = Color(0xFFE9B949),
        sand = Color(0xFFF1E9DA),
        line = Color(0xFFE8E0D2),
        muted = Color(0xFF59685F),
        danger = Color(0xFFC2493D),
        pureWhite = Color(0xFFFFFFFF),
        onCoral = Color(0xFF17251F),
        onBrandMuted = Color(0xFFBAC2B9),
        focus = Color(0xFF143D31),
    )

    /**
     * A calm, Convio-inspired night palette. The brand fill inverts to a light
     * lime so existing Forest/Cream pairs (hero cards, primary buttons, the
     * logo) remain high-contrast without changing every call site. Lime becomes
     * the dark accent container, so Ink-on-Lime pairs also stay readable.
     *
     * Contrast pairs checked in DESIGN.md; focus rings use the bright lime.
     */
    val Night = LingoPalette(
        dark = true,
        cream = Color(0xFF0E1713),
        paper = Color(0xFF16221C),
        ink = Color(0xFFF3EEE2),
        inkSoft = Color(0xFFD5CFC1),
        forest = Color(0xFFD9F27C),
        emerald = Color(0xFF86D3AB),
        mint = Color(0xFF1E3329),
        mintDeep = Color(0xFF2C4637),
        lime = Color(0xFF33421F),
        limeSoft = Color(0xFF2A351C),
        coral = Color(0xFFF08A70),
        coralDeep = Color(0xFFFFB4A1),
        peach = Color(0xFF3A211A),
        blue = Color(0xFF8CB8E0),
        sky = Color(0xFF1C2C3A),
        purple = Color(0xFFB9A7E8),
        lavender = Color(0xFF2A2338),
        teal = Color(0xFF7FD1CC),
        seafoam = Color(0xFF17302E),
        deepTeal = Color(0xFFA9E3DF),
        gold = Color(0xFFF2C766),
        sand = Color(0xFF242019),
        line = Color(0xFF33453C),
        muted = Color(0xFFA8B5AC),
        danger = Color(0xFFFF8A7A),
        pureWhite = Color(0xFF0E1713),
        onCoral = Color(0xFF0E1713),
        onBrandMuted = Color(0xFF556438),
        focus = Color(0xFFD9F27C),
    )

    fun of(mode: ThemeMode): LingoPalette = when (mode) {
        ThemeMode.PAPER -> Paper
        ThemeMode.NIGHT -> Night
    }
}

/** Screens read the active palette through this local in the v1.10 build. */
val LocalLingoPalette = staticCompositionLocalOf { LingoPalettes.Paper }

/**
 * Shared spacing scale. New v1.10 surfaces use these; existing screens keep
 * their current values until the palette migration touches them.
 */
object LingoSpacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 24.dp
    val xxl = 32.dp
    val screen = 20.dp
    val card = 18.dp
    val section = 22.dp
}

/**
 * Shared corner radii. These mirror [LingoShapes] and add named values for the
 * review surfaces so the new screens do not invent one-off radii.
 */
object LingoRadii {
    val button = 18.dp
    val panel = 20.dp
    val card = 24.dp
    val cardLarge = 26.dp
    val hero = 30.dp
    val sheet = 28.dp
}
