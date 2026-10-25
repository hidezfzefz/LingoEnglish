package com.lingo.app.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingo.app.data.Accent
import com.lingo.app.data.StickerKind
import com.lingo.app.data.Word
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class AccentPalette(
    val main: Color,
    val soft: Color,
    val deep: Color,
)

fun paletteFor(accent: Accent): AccentPalette = when (accent) {
    Accent.GREEN -> AccentPalette(Emerald, Mint, Forest)
    Accent.CORAL -> AccentPalette(Coral, Peach, Color(0xFF8C3B27))
    Accent.BLUE -> AccentPalette(Blue, Sky, Color(0xFF234A6E))
    Accent.PURPLE -> AccentPalette(Purple, Lavender, Color(0xFF453572))
    Accent.TEAL -> AccentPalette(Teal, Seafoam, DeepTeal)
}

@Composable
fun LingoLogo(size: Dp = 42.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.34f))
            .background(Forest),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "L",
            color = Lime,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(size * 0.16f)
                .size(size * 0.13f)
                .clip(CircleShape)
                .background(Coral),
        )
    }
}

@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    leading: String? = null,
    trailingArrow: Boolean = false,
    container: Color = Forest,
    contentColor: Color = Cream,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = contentColor,
            disabledContainerColor = container.copy(alpha = 0.4f),
            disabledContentColor = contentColor.copy(alpha = 0.7f),
        ),
        contentPadding = PaddingValues(horizontal = 22.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
    ) {
        if (leading != null) {
            Text(leading, fontSize = 17.sp)
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge, fontSize = 15.sp)
        if (trailingArrow) {
            Spacer(Modifier.width(10.dp))
            Text("→", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    container: Color = Color.Transparent,
    contentColor: Color = Forest,
    borderColor: Color = Line,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = container,
            contentColor = contentColor,
        ),
        contentPadding = PaddingValues(horizontal = 20.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge, fontSize = 15.sp)
    }
}

@Composable
fun SpeakButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    background: Color = Forest,
    size: Dp = 40.dp,
    tint: Color = Cream,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
            contentDescription = "Play audio",
            tint = tint,
            modifier = Modifier.size(size * 0.46f),
        )
    }
}

@Composable
fun ProgressLine(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    track: Color = Sand,
    color: Color = Emerald,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "lineProgress",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(track),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)
                .clip(CircleShape)
                .background(color),
        )
    }
}

@Composable
fun GoalRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 88.dp,
    strokeWidth: Dp = 7.dp,
    track: Color = Cream.copy(alpha = 0.16f),
    color: Color = Lime,
    content: @Composable () -> Unit,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "goalRing",
    )
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            if (animated > 0.005f) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }
        content()
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        if (action != null && onAction != null) {
            Text(
                text = action,
                style = MaterialTheme.typography.labelLarge,
                color = Emerald,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier,
    background: Color = Mint,
    color: Color = Forest,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(background)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

@Composable
fun StatPill(
    sticker: StickerKind,
    text: String,
    modifier: Modifier = Modifier,
    background: Color = Paper,
    color: Color = Ink,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(background)
            .border(1.dp, Line, CircleShape)
            .padding(start = 7.dp, end = 11.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Sticker(kind = sticker, size = 21.dp, elevated = false)
        Spacer(Modifier.width(6.dp))
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

@Composable
fun BackHeader(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "",
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Paper)
                .border(1.dp, Line, CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Text("←", fontSize = 19.sp, color = Ink, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        trailing?.invoke()
    }
}

@Composable
fun WordOfTheDayCard(
    word: Word,
    saved: Boolean,
    onSpeak: () -> Unit,
    onToggleSave: () -> Unit,
    onOpenBank: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = Paper,
        border = BorderStroke(1.dp, Line),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Coral),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "WORD OF THE DAY",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = if (saved) "Remove from saved words" else "Save word",
                        tint = if (saved) Gold else Line,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onToggleSave)
                            .padding(2.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    SpeakButton(onClick = onSpeak, size = 36.dp)
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = word.word,
                    style = MaterialTheme.typography.displaySmall,
                    color = Ink,
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = word.partOfSpeech,
                    style = MaterialTheme.typography.labelMedium,
                    color = Emerald,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
            Text(
                text = word.phonetic,
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
                modifier = Modifier.padding(top = 1.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = word.definition,
                style = MaterialTheme.typography.bodyLarge,
                color = InkSoft,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Cream)
                    .padding(horizontal = 14.dp, vertical = 11.dp),
            ) {
                Text(
                    text = "“${word.example}”",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSoft,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Explore the word bank →",
                style = MaterialTheme.typography.labelMedium,
                color = Emerald,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onOpenBank)
                    .padding(vertical = 5.dp),
            )
        }
    }
}

enum class OptionState { DEFAULT, SELECTED, CORRECT, WRONG, MUTED }

@Composable
fun ChoiceOption(
    letter: String,
    text: String,
    state: OptionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = when (state) {
        OptionState.DEFAULT -> Line
        OptionState.SELECTED -> Forest
        OptionState.CORRECT -> Emerald
        OptionState.WRONG -> Danger
        OptionState.MUTED -> Line.copy(alpha = 0.7f)
    }
    val background = when (state) {
        OptionState.DEFAULT -> Paper
        OptionState.SELECTED -> MintSoft
        OptionState.CORRECT -> Color(0xFFE0F2E5)
        OptionState.WRONG -> Color(0xFFFBE7E2)
        OptionState.MUTED -> Paper
    }
    val letterBackground = when (state) {
        OptionState.DEFAULT -> Cream
        OptionState.SELECTED -> Forest
        OptionState.CORRECT -> Emerald
        OptionState.WRONG -> Danger
        OptionState.MUTED -> Cream
    }
    val letterColor = when (state) {
        OptionState.DEFAULT, OptionState.MUTED -> Muted
        else -> Cream
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(background)
            .border(1.5.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(
                enabled = state == OptionState.DEFAULT || state == OptionState.SELECTED,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(letterBackground),
            contentAlignment = Alignment.Center,
        ) {
            if (state == OptionState.CORRECT) {
                Text("✓", color = Cream, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            } else if (state == OptionState.WRONG) {
                Text("×", color = Cream, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            } else {
                Text(
                    text = letter,
                    color = letterColor,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (state == OptionState.MUTED) Muted else Ink,
            modifier = Modifier.weight(1f),
        )
    }
}

// Kept local so components don't depend on the theme's internal palette names.
private val MintSoft = Color(0xFFF0F7EF)

@Composable
fun Confetti(
    modifier: Modifier = Modifier,
    particleCount: Int = 34,
    seed: Int = 7,
) {
    val random = remember(seed) { Random(seed) }
    val particles = remember(seed) {
        List(particleCount) {
            ConfettiParticle(
                x = random.nextFloat(),
                delay = random.nextFloat() * 0.45f,
                size = 5f + random.nextFloat() * 7f,
                color = listOf(Lime, Coral, Gold, Emerald, Purple, Blue)[random.nextInt(6)],
                rotation = random.nextFloat() * 360f,
                spin = -220f + random.nextFloat() * 440f,
                drift = -0.12f + random.nextFloat() * 0.24f,
                round = random.nextBoolean(),
            )
        }
    }
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val progress by animateFloatAsState(
        targetValue = if (started) 1.25f else 0f,
        animationSpec = tween(1900, easing = LinearEasing),
        label = "confetti",
    )
    Canvas(modifier = modifier) {
        particles.forEach { particle ->
            val local = ((progress - particle.delay) / (1.05f - particle.delay)).coerceIn(0f, 1f)
            if (local <= 0f) return@forEach
            val startX = size.width * particle.x
            val x = startX + size.width * particle.drift * local
            val y = -30f + (size.height + 60f) * local
            val alpha = when {
                local < 0.12f -> local / 0.12f
                local > 0.85f -> (1f - local) / 0.15f
                else -> 1f
            }
            rotate(particle.rotation + particle.spin * local, pivot = Offset(x, y)) {
                if (particle.round) {
                    drawCircle(
                        color = particle.color.copy(alpha = alpha.coerceIn(0f, 1f)),
                        radius = particle.size / 2f,
                        center = Offset(x, y),
                    )
                } else {
                    drawRoundRect(
                        color = particle.color.copy(alpha = alpha.coerceIn(0f, 1f)),
                        topLeft = Offset(x, y),
                        size = Size(particle.size, particle.size * 0.55f),
                        cornerRadius = CornerRadius(2f, 2f),
                    )
                }
            }
        }
    }
}

private data class ConfettiParticle(
    val x: Float,
    val delay: Float,
    val size: Float,
    val color: Color,
    val rotation: Float,
    val spin: Float,
    val drift: Float,
    val round: Boolean,
)

@Composable
fun DecorativeDots(
    modifier: Modifier = Modifier,
    color: Color = Lime.copy(alpha = 0.12f),
    count: Int = 7,
) {
    Canvas(modifier = modifier) {
        repeat(count) { index ->
            val angle = (index * 360f / count) * (Math.PI / 180f)
            val radius = size.minDimension * 0.42f
            val center = Offset(
                x = size.width / 2 + (cos(angle) * radius).toFloat(),
                y = size.height / 2 + (sin(angle) * radius).toFloat(),
            )
            drawCircle(color = color, radius = size.minDimension * 0.035f, center = center)
        }
    }
}
