package com.lingo.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingo.app.data.Lesson
import com.lingo.app.data.LingoContent
import com.lingo.app.data.StickerKind
import com.lingo.app.data.ProgressStore
import com.lingo.app.data.Slide

@Composable
fun LessonScreen(
    lesson: Lesson,
    store: ProgressStore,
    speaker: (String) -> Unit,
    nextLesson: Lesson?,
    onClose: () -> Unit,
    onBackToPath: () -> Unit,
    onNextLesson: (Lesson) -> Unit,
    onPracticeMore: () -> Unit,
) {
    var step by rememberSaveable(lesson.id) { mutableIntStateOf(0) }
    var selected by rememberSaveable(lesson.id) { mutableStateOf<Int?>(null) }
    var checked by rememberSaveable(lesson.id) { mutableStateOf(false) }
    var correctCount by rememberSaveable(lesson.id) { mutableIntStateOf(0) }
    var complete by rememberSaveable(lesson.id) { mutableStateOf(false) }
    var awardedXp by rememberSaveable(lesson.id) { mutableIntStateOf(0) }

    val totalSteps = lesson.stepCount
    val slideCount = lesson.slides.size
    val exercise = if (step >= slideCount) lesson.exercises.getOrNull(step - slideCount) else null
    // Restore any saved writing when its exercise appears, and keep text per exercise.
    var response by rememberSaveable(lesson.id, exercise?.id) {
        mutableStateOf(if (exercise?.isCreative == true) store.creativeDraft(exercise.id) else "")
    }
    val palette = LingoContent.unitFor(lesson)?.let { paletteFor(it.accent) }
        ?: paletteFor(com.lingo.app.data.Accent.GREEN)

    if (complete) {
        LessonComplete(
            lesson = lesson,
            nextLesson = nextLesson,
            awardedXp = awardedXp,
            correctCount = correctCount,
            onBackToPath = onBackToPath,
            onNextLesson = onNextLesson,
            onPracticeMore = onPracticeMore,
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Paper)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                Text("←", fontSize = 18.sp, color = Ink)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (exercise == null) "LESSON" else "PRACTICE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${(step + 1).coerceAtMost(totalSteps)} / $totalSteps",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                }
                Spacer(Modifier.height(7.dp))
                ProgressLine(
                    progress = (step + 1f) / totalSteps,
                    color = palette.main,
                    track = Sand,
                    height = 7.dp,
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn(tween(240)) togetherWith fadeOut(tween(120)) },
                label = "lessonStep",
                modifier = Modifier.fillMaxSize(),
            ) { currentStep ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp)
                        .padding(top = 10.dp, bottom = 20.dp),
                ) {
                    if (currentStep < slideCount) {
                        SlideContent(
                            slide = lesson.slides[currentStep],
                            lesson = lesson,
                            palette = palette,
                            speaker = speaker,
                            slideNumber = currentStep + 1,
                            slideCount = slideCount,
                        )
                    } else {
                        val currentExercise = lesson.exercises[currentStep - slideCount]
                        Column {
                            Tag(
                                text = "CHECK YOURSELF · ${currentStep - slideCount + 1} OF ${lesson.exercises.size}",
                                background = palette.soft,
                                color = palette.deep,
                            )
                            Spacer(Modifier.height(14.dp))
                            ExerciseView(
                                exercise = currentExercise,
                                selectedIndex = selected,
                                checked = checked,
                                onSelect = { selected = it },
                                onSpeak = speaker,
                                text = response,
                                onTextChange = { updated ->
                                    response = updated
                                    if (currentExercise.isCreative) {
                                        store.saveCreativeDraft(currentExercise.id, updated)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Cream)
                .padding(horizontal = 22.dp, vertical = 12.dp),
        ) {
            if (exercise != null && !checked) {
                PrimaryButton(
                    text = if (exercise.isCreative) "Check my writing" else "Check answer",
                    onClick = {
                        checked = true
                        if (exercise.isAnswerCorrect(selected, response)) correctCount += 1
                    },
                    enabled = if (exercise.isCreative) {
                        exercise.wordCount(response) >= exercise.minWords
                    } else {
                        selected != null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    container = palette.deep,
                )
            } else {
                PrimaryButton(
                    text = if (step == totalSteps - 1) "Finish lesson" else "Continue",
                    onClick = {
                        if (step == totalSteps - 1) {
                            awardedXp = store.completeLesson(lesson)
                            complete = true
                        } else {
                            step += 1
                            selected = null
                            response = ""
                            checked = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    container = palette.deep,
                    trailingArrow = true,
                )
            }
        }
    }
}

@Composable
private fun SlideContent(
    slide: Slide,
    lesson: Lesson,
    palette: AccentPalette,
    speaker: (String) -> Unit,
    slideNumber: Int,
    slideCount: Int,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Sticker(kind = lesson.icon, size = 54.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = slide.eyebrow.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.main,
                )
                Text(
                    text = "Part $slideNumber of $slideCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = slide.title,
            style = MaterialTheme.typography.displaySmall.copy(fontSize = 34.sp, lineHeight = 39.sp),
            color = Ink,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = slide.body,
            style = MaterialTheme.typography.bodyLarge,
            color = InkSoft,
        )
        Spacer(Modifier.height(24.dp))
        Text("SAY IT OUT LOUD", style = MaterialTheme.typography.labelSmall, color = Muted)
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            slide.examples.forEach { example ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Paper,
                    border = BorderStroke(1.dp, Line),
                ) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "“${example.phrase}”",
                                style = MaterialTheme.typography.titleMedium,
                                color = Ink,
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = example.note,
                                style = MaterialTheme.typography.bodySmall,
                                color = Muted,
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        SpeakButton(
                            onClick = { speaker(example.phrase) },
                            size = 38.dp,
                            background = palette.soft,
                        )
                    }
                }
            }
        }
        if (slide.tip != null) {
            Spacer(Modifier.height(18.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = LimeSoft,
            ) {
                Row(Modifier.padding(16.dp)) {
                    Sticker(kind = StickerKind.BULB, size = 23.dp, elevated = false)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "A little tip",
                            style = MaterialTheme.typography.labelSmall,
                            color = Ink.copy(alpha = 0.7f),
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = slide.tip,
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSoft,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonComplete(
    lesson: Lesson,
    nextLesson: Lesson?,
    awardedXp: Int,
    correctCount: Int,
    onBackToPath: () -> Unit,
    onNextLesson: (Lesson) -> Unit,
    onPracticeMore: () -> Unit,
) {
    val celebration = when {
        lesson.exercises.isNotEmpty() && correctCount == lesson.exercises.size -> StickerKind.CELEBRATION
        correctCount >= 2 -> StickerKind.SPARKLE
        else -> StickerKind.TARGET
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Confetti(Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(46.dp))
            Sticker(kind = celebration, size = 118.dp)
            Spacer(Modifier.height(22.dp))
            Text(
                text = "Lesson complete!",
                style = MaterialTheme.typography.displaySmall,
                color = Ink,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = lesson.title,
                style = MaterialTheme.typography.bodyLarge,
                color = Muted,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ResultTile(
                    value = "+$awardedXp",
                    label = "XP EARNED",
                    background = Mint,
                    modifier = Modifier.weight(1f),
                )
                ResultTile(
                    value = "$correctCount/${lesson.exercises.size}",
                    label = "TASKS MET",
                    background = Sky,
                    modifier = Modifier.weight(1f),
                )
                ResultTile(
                    value = "${lesson.minutes}",
                    label = "MINUTES",
                    background = LimeSoft,
                    modifier = Modifier.weight(1f),
                )
            }
            if (nextLesson != null) {
                Spacer(Modifier.height(24.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNextLesson(nextLesson) },
                    shape = RoundedCornerShape(22.dp),
                    color = Paper,
                    border = BorderStroke(1.dp, Line),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Sticker(kind = nextLesson.icon, size = 42.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "NEXT UP",
                                style = MaterialTheme.typography.labelSmall,
                                color = Muted,
                            )
                            Text(
                                nextLesson.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = Ink,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                nextLesson.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Muted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text("→", style = MaterialTheme.typography.titleMedium, color = Emerald)
                    }
                }
            }
            Spacer(Modifier.height(26.dp))
            PrimaryButton(
                text = if (nextLesson != null) "Next lesson" else "Back to the path",
                onClick = {
                    if (nextLesson != null) onNextLesson(nextLesson) else onBackToPath()
                },
                modifier = Modifier.fillMaxWidth(),
                trailingArrow = true,
            )
            Spacer(Modifier.height(10.dp))
            SecondaryButton(
                text = "Practice more",
                onClick = onPracticeMore,
                modifier = Modifier.fillMaxWidth(),
            )
            if (nextLesson != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Back to the path",
                    style = MaterialTheme.typography.labelMedium,
                    color = Muted,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onBackToPath)
                        .padding(8.dp),
                )
            }
            Spacer(Modifier.height(26.dp))
        }
    }
}

@Composable
private fun ResultTile(
    value: String,
    label: String,
    background: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = background,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = Ink)
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = InkSoft.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}
