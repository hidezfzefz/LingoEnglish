package com.lingo.app

import androidx.compose.ui.graphics.Color
import com.lingo.app.ui.LingoPalettes
import com.lingo.app.ui.ThemeMode
import kotlin.math.pow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the v1.10 palette against the contrast regressions that are easy to
 * introduce while adding Night. The pairs below are the ones the new review
 * surfaces and the existing brand surfaces rely on.
 */
class DesignTokensTest {

    private fun channel(value: Float): Double {
        val v = value.toDouble()
        return if (v <= 0.04045) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
    }

    private fun luminance(color: Color): Double =
        0.2126 * channel(color.red) +
            0.7152 * channel(color.green) +
            0.0722 * channel(color.blue)

    private fun contrast(a: Color, b: Color): Double {
        val first = luminance(a)
        val second = luminance(b)
        val lighter = maxOf(first, second)
        val darker = minOf(first, second)
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun assertContrast(
        minimum: Double,
        foreground: Color,
        background: Color,
        label: String,
    ) {
        val ratio = contrast(foreground, background)
        assertTrue(
            "$label is %.2f:1, expected at least %.1f:1".format(ratio, minimum),
            ratio >= minimum,
        )
    }

    @Test
    fun `paper remains the default and keeps its original hex values`() {
        assertEquals(LingoPalettes.Paper, LingoPalettes.of(ThemeMode.PAPER))
        assertEquals(LingoPalettes.Night, LingoPalettes.of(ThemeMode.NIGHT))
        assertEquals(Color(0xFFFAF6EE), LingoPalettes.Paper.cream)
        assertEquals(Color(0xFFFFFCF6), LingoPalettes.Paper.paper)
        assertEquals(Color(0xFF143D31), LingoPalettes.Paper.forest)
        assertEquals(Color(0xFFEC7256), LingoPalettes.Paper.coral)
        assertTrue(!LingoPalettes.Paper.dark)
        assertTrue(LingoPalettes.Night.dark)
    }

    @Test
    fun `paper text pairs clear wcag aa`() {
        val p = LingoPalettes.Paper
        assertContrast(4.5, p.ink, p.cream, "ink on cream")
        assertContrast(4.5, p.ink, p.paper, "ink on paper")
        assertContrast(4.5, p.muted, p.cream, "muted on cream")
        assertContrast(4.5, p.muted, p.paper, "muted on paper")
        assertContrast(4.5, p.forest, p.paper, "forest on paper")
        assertContrast(4.5, p.cream, p.forest, "cream on forest")
        assertContrast(4.5, p.ink, p.lime, "ink on lime")
        assertContrast(4.5, p.onCoral, p.coral, "onCoral on coral")
        assertContrast(4.5, p.coralDeep, p.paper, "coralDeep on paper")
        assertContrast(4.5, p.coralDeep, p.peach, "coralDeep on peach")
        assertContrast(4.5, p.onBrandMuted, p.forest, "onBrandMuted on forest")
        assertContrast(3.0, p.focus, p.paper, "focus on paper")
    }

    @Test
    fun `night text pairs clear wcag aa`() {
        val n = LingoPalettes.Night
        assertContrast(4.5, n.ink, n.cream, "ink on cream")
        assertContrast(4.5, n.ink, n.paper, "ink on paper")
        assertContrast(4.5, n.muted, n.cream, "muted on cream")
        assertContrast(4.5, n.muted, n.paper, "muted on paper")
        assertContrast(4.5, n.forest, n.paper, "forest on paper")
        assertContrast(4.5, n.cream, n.forest, "cream on forest")
        assertContrast(4.5, n.ink, n.lime, "ink on lime")
        assertContrast(4.5, n.onCoral, n.coral, "onCoral on coral")
        assertContrast(4.5, n.coralDeep, n.paper, "coralDeep on paper")
        assertContrast(4.5, n.coralDeep, n.peach, "coralDeep on peach")
        assertContrast(4.5, n.onBrandMuted, n.forest, "onBrandMuted on forest")
        assertContrast(4.5, n.danger, n.cream, "danger on cream")
        assertContrast(3.0, n.focus, n.paper, "focus on paper")
    }

    @Test
    fun `accent containers keep readable text`() {
        val p = LingoPalettes.Paper
        val n = LingoPalettes.Night
        assertContrast(4.5, p.blue, p.sky, "paper blue on sky")
        assertContrast(4.5, p.purple, p.lavender, "paper purple on lavender")
        assertContrast(4.5, p.deepTeal, p.seafoam, "paper deepTeal on seafoam")
        assertContrast(4.5, n.blue, n.sky, "night blue on sky")
        assertContrast(4.5, n.purple, n.lavender, "night purple on lavender")
        assertContrast(4.5, n.deepTeal, n.seafoam, "night deepTeal on seafoam")
    }
}
