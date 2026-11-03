package com.lingo.app.ui

import android.animation.ValueAnimator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingo.app.data.LaunchCutsceneMode
import com.lingo.app.data.Lesson
import com.lingo.app.data.LessonSpark
import com.lingo.app.data.Slide
import com.lingo.app.data.StickerKind
import kotlinx.coroutines.delay

private const val FullLaunchMillis = 3200
private const val StingLaunchMillis = 1200

/**
 * The branded launch sequence. It is deliberately local and one-shot: no
 * network, no sound, and no learner data. A system animator scale of zero
 * renders the finished frame and moves on quickly instead of animating.
 */
@Composable
fun LaunchCutscene(
    mode: LaunchCutsceneMode,
    onFinished: () -> Unit,
) {
    if (mode == LaunchCutsceneMode.NONE) {
        LaunchedEffect(Unit) { onFinished() }
        return
    }

    val reducedMotion = remember { !ValueAnimator.areAnimatorsEnabled() }
    val full = mode == LaunchCutsceneMode.FULL
    val progress = remember { Animatable(if (reducedMotion) 1f else 0f) }

    LaunchedEffect(mode, reducedMotion) {
        if (reducedMotion) {
            delay(if (full) 1100L else 500L)
            onFinished()
            return@LaunchedEffect
        }
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = if (full) FullLaunchMillis else StingLaunchMillis,
                easing = FastOutSlowInEasing,
            ),
        )
        onFinished()
    }

    val value = progress.value
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .pointerInput(Unit) {
                // Tapping anywhere is a quick escape for sighted learners.
                // TalkBack gets the explicit Skip button below instead.
                detectTapGestures(onTap = { onFinished() })
            },
    ) {
        LaunchBackdrop(Modifier.fillMaxSize())

        if (full) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(72.dp))
                CutsceneMark(
                    progress = value,
                    compact = false,
                    modifier = Modifier.widthIn(max = 340.dp),
                )
                Spacer(Modifier.height(30.dp))
                Text(
                    text = "A little English,\nevery day.",
                    style = MaterialTheme.typography.displaySmall,
                    color = Ink,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Warm up. Say it. Make it yours.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(
                        if (reducedMotion) 1f else ((value - 0.55f) / 0.3f).coerceIn(0f, 1f),
                    ),
                )
                Spacer(Modifier.height(64.dp))
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CutsceneMark(
                        progress = value,
                        compact = true,
                        modifier = Modifier.widthIn(max = 260.dp),
                    )
                    Spacer(Modifier.height(18.dp))
                    Text(
                        text = "A little English, every day.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Muted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.alpha(
                            if (reducedMotion) 1f else ((value - 0.3f) / 0.4f).coerceIn(0f, 1f),
                        ),
                    )
                }
            }
        }

        TextButton(
            onClick = onFinished,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .heightIn(min = 48.dp),
        ) {
            Text(
                text = "Skip intro",
                style = MaterialTheme.typography.labelMedium,
                color = InkSoft,
            )
        }
    }
}

@Composable
private fun LaunchBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        drawCircle(
            color = Mint.copy(alpha = 0.55f),
            radius = size.width * 0.42f,
            center = Offset(size.width * 0.08f, size.height * 0.08f),
        )
        drawCircle(
            color = Peach.copy(alpha = 0.32f),
            radius = size.width * 0.38f,
            center = Offset(size.width * 0.95f, size.height * 0.9f),
        )
        drawCircle(
            color = LimeSoft.copy(alpha = 0.45f),
            radius = size.width * 0.22f,
            center = Offset(size.width * 0.9f, size.height * 0.12f),
        )
    }
}

@Composable
private fun CutsceneMark(
    progress: Float,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    val cardHeight = if (compact) 154.dp else 252.dp
    val logoSize = if (compact) 54.dp else 58.dp
    val pathFraction = if (compact) {
        (progress / 0.85f).coerceIn(0f, 1f)
    } else {
        ((progress - 0.06f) / 0.62f).coerceIn(0f, 1f)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .clearAndSetSemantics {
                contentDescription = if (compact) {
                    "Lingo"
                } else {
                    "Lingo: warm up, say it, make it yours"
                }
            },
        shape = RoundedCornerShape(if (compact) 30.dp else 38.dp),
        color = Forest,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (compact) 18.dp else 22.dp),
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    color = Lime.copy(alpha = 0.08f),
                    radius = size.minDimension * 0.62f,
                    center = Offset(size.width * 0.92f, size.height * 0.08f),
                )
                val w = size.width
                val h = size.height
                val path = Path().apply {
                    moveTo(w * 0.06f, h * 0.68f)
                    cubicTo(w * 0.24f, h * 0.20f, w * 0.38f, h * 0.92f, w * 0.55f, h * 0.58f)
                    cubicTo(w * 0.70f, h * 0.28f, w * 0.78f, h * 0.44f, w * 0.94f, h * 0.34f)
                }
                drawPath(
                    path = path,
                    color = Lime.copy(alpha = 0.18f),
                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round),
                )
                if (pathFraction > 0f) {
                    val measure = PathMeasure().apply { setPath(path, false) }
                    val drawn = Path()
                    measure.getSegment(0f, measure.length * pathFraction, drawn, true)
                    drawPath(
                        path = drawn,
                        color = Lime,
                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round),
                    )
                    if (pathFraction >= 0.99f) {
                        drawCircle(
                            color = Coral,
                            radius = 5.dp.toPx(),
                            center = measure.getPosition(measure.length),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.align(Alignment.TopStart),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LingoLogo(size = logoSize)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "LINGO",
                    style = MaterialTheme.typography.labelLarge.copy(
                        letterSpacing = 2.sp,
                        fontSize = if (compact) 12.sp else 14.sp,
                    ),
                    color = Lime,
                )
            }

            if (!compact) {
                BeatSticker(
                    kind = StickerKind.SPROUT,
                    progress = progress,
                    start = 0.18f,
                    end = 0.34f,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 4.dp, y = (-6).dp),
                )
                BeatSticker(
                    kind = StickerKind.SPEAK,
                    progress = progress,
                    start = 0.38f,
                    end = 0.56f,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = 0.dp, y = 2.dp),
                )
                BeatSticker(
                    kind = StickerKind.SPARKLE,
                    progress = progress,
                    start = 0.58f,
                    end = 0.76f,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-2).dp, y = 18.dp),
                )
            }
        }
    }
}

@Composable
private fun BeatSticker(
    kind: StickerKind,
    progress: Float,
    start: Float,
    end: Float,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    val appear = ((progress - start) / (end - start)).coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .scale(0.82f + 0.18f * appear)
            .alpha(appear),
    ) {
        Sticker(kind = kind, size = size)
    }
}

/**
 * A once-per-lesson welcome. It previews the real first step and offers an
 * optional creative spark. Nothing here is a gate: Begin is always available,
 * and the screen records no answer.
 */
@Composable
fun LessonIntro(
    lesson: Lesson,
    spark: LessonSpark?,
    onBegin: () -> Unit,
) {
    val reducedMotion = remember { !ValueAnimator.areAnimatorsEnabled() }
    var selectedChip by rememberSaveable(lesson.id) { mutableStateOf<Int?>(null) }
    val firstSlide = lesson.slides.firstOrNull()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream),
    ) {
        LaunchBackdrop(Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "A NEW LESSON",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                    Text(
                        text = "Take it one step at a time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                }
                TextButton(
                    onClick = onBegin,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(
                        text = "Skip intro",
                        style = MaterialTheme.typography.labelMedium,
                        color = InkSoft,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Sticker(kind = lesson.icon, size = 62.dp)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 32.sp,
                            lineHeight = 36.sp,
                        ),
                        color = Ink,
                        modifier = Modifier.semantics { heading() },
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = lesson.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )
                }
            }
            Spacer(Modifier.height(22.dp))

            LessonPathRow()
            Spacer(Modifier.height(18.dp))

            FirstStepCard(
                lesson = lesson,
                firstSlide = firstSlide,
                modifier = Modifier.fillMaxWidth(),
            )

            if (spark != null) {
                Spacer(Modifier.height(24.dp))
                CreativeSpark(
                    spark = spark,
                    selectedChip = selectedChip,
                    reducedMotion = reducedMotion,
                    onChip = { index ->
                        selectedChip = if (selectedChip == index) null else index
                    },
                )
            }

            Spacer(Modifier.height(26.dp))
            PrimaryButton(
                text = "Begin first step",
                onClick = onBegin,
                modifier = Modifier.fillMaxWidth(),
                trailingArrow = true,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "You can leave and come back any time.",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(26.dp))
        }
    }
}

@Composable
private fun LessonPathRow() {
    val fontScale = LocalDensity.current.fontScale
    if (fontScale >= 1.5f) {
        // Three pills side by side stop fitting once the system font is large.
        // Stacking keeps every label whole instead of breaking mid-word.
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PathPill(
                number = "1",
                label = "First step",
                active = true,
                modifier = Modifier.fillMaxWidth(),
            )
            PathPill(
                number = "2",
                label = "Practise",
                active = false,
                modifier = Modifier.fillMaxWidth(),
            )
            PathPill(
                number = "3",
                label = "Finish",
                active = false,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PathPill(
                number = "1",
                label = "First step",
                active = true,
                modifier = Modifier.weight(1f),
            )
            PathPill(
                number = "2",
                label = "Practise",
                active = false,
                modifier = Modifier.weight(1f),
            )
            PathPill(
                number = "3",
                label = "Finish",
                active = false,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PathPill(
    number: String,
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.semantics {
            stateDescription = if (active) "Current step" else "Upcoming step"
        },
        shape = RoundedCornerShape(16.dp),
        color = if (active) Mint else Paper,
        border = BorderStroke(1.dp, if (active) MintDeep else Line),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (active) Forest else Sand)
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        letterSpacing = 0.sp,
                    ),
                    color = if (active) Lime else Muted,
                )
            }
            Spacer(Modifier.width(7.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.5.sp,
                    lineHeight = 12.sp,
                    letterSpacing = 0.2.sp,
                ),
                color = if (active) Forest else Muted,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun FirstStepCard(
    lesson: Lesson,
    firstSlide: Slide?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Paper,
        border = BorderStroke(1.dp, Line),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Tag(
                    text = "FIRST STEP",
                    background = Mint,
                    color = Forest,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "1 of ${lesson.stepCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = firstSlide?.title ?: "Start here",
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = firstSlide?.body ?: "Your first step is waiting inside.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft,
            )
            if (firstSlide?.examples?.isNotEmpty() == true) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "“${firstSlide.examples.first().phrase}”",
                    style = MaterialTheme.typography.bodySmall,
                    color = Emerald,
                )
            }
        }
    }
}

@Composable
private fun CreativeSpark(
    spark: LessonSpark,
    selectedChip: Int?,
    reducedMotion: Boolean,
    onChip: (Int) -> Unit,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Sticker(kind = StickerKind.BULB, size = 24.dp, elevated = false)
            Spacer(Modifier.width(8.dp))
            Text(
                text = "A CREATIVE SPARK",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = spark.prompt,
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Tap an idea for a little inspiration. There's no wrong answer.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            spark.chips.forEachIndexed { index, chip ->
                val selected = selectedChip == index
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            onClickLabel = if (selected) "Hide example" else "Show example",
                            role = Role.Button,
                        ) { onChip(index) }
                        .semantics(mergeDescendants = true) {
                            contentDescription = chip.label
                            stateDescription = if (selected) "Example shown" else "Example hidden"
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected) LimeSoft else Paper,
                    border = BorderStroke(1.dp, if (selected) Emerald else Line),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = chip.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) Forest else InkSoft,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (selected) "−" else "+",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (selected) Forest else Muted,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        val example = selectedChip?.let { spark.chips.getOrNull(it)?.example }
        if (reducedMotion) {
            if (example != null) {
                Spacer(Modifier.height(10.dp))
                SparkExample(example)
            }
        } else {
            AnimatedVisibility(
                visible = example != null,
                enter = fadeIn(tween(220)) + expandVertically(tween(220)),
                exit = fadeOut(tween(120)) + shrinkVertically(tween(160)),
            ) {
                if (example != null) {
                    Column {
                        Spacer(Modifier.height(10.dp))
                        SparkExample(example)
                    }
                }
            }
        }
    }
}

@Composable
private fun SparkExample(example: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        shape = RoundedCornerShape(18.dp),
        color = LimeSoft,
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = "ONE WAY TO SAY IT",
                style = MaterialTheme.typography.labelSmall,
                color = Ink.copy(alpha = 0.68f),
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "“$example”",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft,
            )
        }
    }
}
