package com.lingo.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Balance
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.FlightTakeoff
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.RoomService
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.TheaterComedy
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.WavingHand
import androidx.compose.material.icons.rounded.Window
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lingo.app.data.StickerKind
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class StickerColors(
    val background: Color,
    val ink: Color,
)

/**
 * The app's illustrated icon system: a die-cut paper sticker with a softly
 * printed texture. It keeps the interface warm without relying on emoji.
 */
@Composable
fun Sticker(
    kind: StickerKind,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    contentDescription: String? = null,
    elevated: Boolean = true,
    background: Color? = null,
    tint: Color? = null,
) {
    val colors = stickerColors(kind)
    val base = background ?: colors.background
    val ink = tint ?: colors.ink
    val rim = (size.value * 0.055f).dp.coerceIn(1.dp, 3.dp)
    val seed = kind.ordinal * 79 + 23
    val flecks = remember(seed) {
        val random = Random(seed)
        List(26) {
            Triple(
                random.nextFloat(),
                random.nextFloat(),
                random.nextFloat(),
            )
        }
    }
    val blobs = remember(seed) {
        val random = Random(seed + 5)
        List(3) {
            Triple(
                random.nextFloat(),
                random.nextFloat(),
                random.nextFloat(),
            )
        }
    }
    val shadow = if (elevated) Modifier.shadow(2.dp, CircleShape, clip = false) else Modifier

    Box(
        modifier = modifier
            .size(size)
            .then(shadow)
            .clip(CircleShape)
            .background(Paper)
            .padding(rim),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
        ) {
            val shortest = this.size.minDimension
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        lerp(base, Color.White, 0.30f),
                        base,
                        lerp(base, Color.Black, 0.055f),
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(this.size.width, this.size.height),
                ),
                radius = shortest / 2f,
            )

            // Soft printed patches give each sticker a little paper texture.
            blobs.forEach { (x, y, strength) ->
                drawCircle(
                    color = lerp(base, Color.White, 0.38f).copy(alpha = 0.18f + strength * 0.08f),
                    radius = shortest * (0.13f + strength * 0.10f),
                    center = Offset(this.size.width * x, this.size.height * y),
                )
            }
            flecks.forEach { (x, y, strength) ->
                drawCircle(
                    color = if (strength > 0.62f) {
                        lerp(base, Color.Black, 0.30f).copy(alpha = 0.07f)
                    } else {
                        Color.White.copy(alpha = 0.12f + strength * 0.10f)
                    },
                    radius = shortest * (0.006f + strength * 0.009f),
                    center = Offset(this.size.width * x, this.size.height * y),
                )
            }

            // A faint stitched edge catches the light like a real paper sticker.
            drawCircle(
                color = Color.White.copy(alpha = 0.34f),
                radius = shortest * 0.465f,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = (shortest * 0.018f).coerceAtLeast(0.7f),
                ),
            )
            drawCircle(
                color = lerp(base, Color.Black, 0.16f).copy(alpha = 0.10f),
                radius = shortest * 0.465f,
                center = Offset(center.x + shortest * 0.012f, center.y + shortest * 0.016f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = (shortest * 0.012f).coerceAtLeast(0.5f),
                ),
            )
        }
        Icon(
            imageVector = stickerVector(kind),
            contentDescription = contentDescription,
            tint = ink,
            modifier = Modifier.size(size * 0.47f),
        )
    }
}

@Composable
fun CircleStickerButton(
    kind: StickerKind,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(onClick = onClick),
    ) {
        Sticker(
            kind = kind,
            size = size,
            contentDescription = contentDescription,
            modifier = Modifier.clip(CircleShape),
        )
    }
}

private fun stickerVector(kind: StickerKind): ImageVector = when (kind) {
    StickerKind.HOME -> Icons.Rounded.Home
    StickerKind.BOOK -> Icons.Rounded.AutoStories
    StickerKind.BOLT -> Icons.Rounded.Bolt
    StickerKind.CHART -> Icons.Rounded.BarChart
    StickerKind.SETTINGS -> Icons.Rounded.Settings
    StickerKind.SPROUT -> Icons.Rounded.Eco
    StickerKind.FLAME -> Icons.Rounded.LocalFireDepartment
    StickerKind.SPARKLE -> Icons.Rounded.AutoAwesome
    StickerKind.TARGET -> Icons.Rounded.GpsFixed
    StickerKind.TROPHY -> Icons.Rounded.EmojiEvents
    StickerKind.STAR -> Icons.Rounded.Star
    StickerKind.CARDS -> Icons.Rounded.Style
    StickerKind.HEADPHONES -> Icons.Rounded.Headphones
    StickerKind.PENCIL -> Icons.Rounded.Edit
    StickerKind.BULB -> Icons.Rounded.Lightbulb
    StickerKind.LAB -> Icons.Rounded.Science
    StickerKind.THEATER -> Icons.Rounded.TheaterComedy
    StickerKind.PUZZLE -> Icons.Rounded.Extension
    StickerKind.SEARCH -> Icons.Rounded.Search
    StickerKind.CELEBRATION -> Icons.Rounded.Celebration
    StickerKind.WAVE -> Icons.Rounded.WavingHand
    StickerKind.COFFEE -> Icons.Rounded.LocalCafe
    StickerKind.COMPASS -> Icons.Rounded.Explore
    StickerKind.PLANE -> Icons.Rounded.Flight
    StickerKind.TAKEOFF -> Icons.Rounded.FlightTakeoff
    StickerKind.BELL -> Icons.Rounded.RoomService
    StickerKind.CHAT -> Icons.Rounded.ChatBubble
    StickerKind.BRIEFCASE -> Icons.Rounded.Work
    StickerKind.EMAIL -> Icons.Rounded.Email
    StickerKind.SPEAK -> Icons.Rounded.RecordVoiceOver
    StickerKind.HOURGLASS -> Icons.Rounded.HourglassEmpty
    StickerKind.FLOWER -> Icons.Rounded.LocalFlorist
    StickerKind.PILL -> Icons.Rounded.Medication
    StickerKind.MEDITATION -> Icons.Rounded.SelfImprovement
    StickerKind.CALENDAR -> Icons.Rounded.CalendarMonth
    StickerKind.HOUSE -> Icons.Rounded.Home
    StickerKind.WINDOW -> Icons.Rounded.Window
    StickerKind.WALK -> Icons.Rounded.DirectionsWalk
    StickerKind.RESTAURANT -> Icons.Rounded.Restaurant
    StickerKind.TICKET -> Icons.Rounded.ConfirmationNumber
    StickerKind.MOVIE -> Icons.Rounded.Movie
    StickerKind.SMARTPHONE -> Icons.Rounded.Smartphone
    StickerKind.PERSON -> Icons.Rounded.Person
    StickerKind.BALANCE -> Icons.Rounded.Balance
    StickerKind.VOLUME -> Icons.Rounded.VolumeUp
}

private fun stickerColors(kind: StickerKind): StickerColors = when (kind) {
    StickerKind.COFFEE,
    StickerKind.PILL,
    StickerKind.RESTAURANT,
    StickerKind.TICKET,
    StickerKind.WAVE,
    StickerKind.CELEBRATION,
    StickerKind.FLAME,
    StickerKind.TROPHY,
    StickerKind.FLOWER,
    StickerKind.PUZZLE,
    -> StickerColors(Peach, Color(0xFF8C3B27))

    StickerKind.COMPASS,
    StickerKind.PLANE,
    StickerKind.TAKEOFF,
    StickerKind.CHAT,
    StickerKind.CHART,
    StickerKind.VOLUME,
    StickerKind.HOME,
    StickerKind.HOUSE,
    StickerKind.TARGET,
    StickerKind.SMARTPHONE,
    -> StickerColors(Sky, Color(0xFF234A6E))

    StickerKind.THEATER,
    StickerKind.MOVIE,
    StickerKind.MEDITATION,
    StickerKind.BALANCE,
    StickerKind.CALENDAR,
    StickerKind.SPEAK,
    -> StickerColors(Lavender, Color(0xFF453572))

    StickerKind.SETTINGS,
    StickerKind.BRIEFCASE,
    StickerKind.EMAIL,
    StickerKind.HOURGLASS,
    StickerKind.STAR,
    StickerKind.SPARKLE,
    StickerKind.BELL,
    -> StickerColors(Sand, Color(0xFF6A5836))

    else -> StickerColors(Mint, Forest)
}
