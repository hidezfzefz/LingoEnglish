package com.lingo.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingo.app.data.Exercise
import com.lingo.app.data.LingoContent
import com.lingo.app.data.PracticeMode
import com.lingo.app.data.ProgressStore
import com.lingo.app.data.StickerKind

@Composable
fun QuizScreen(
    mode: PracticeMode,
    store: ProgressStore,
    speaker: (String) -> Unit,
    onClose: () -> Unit,
) {
    var attempt by rememberSaveable(mode.name) { mutableIntStateOf(0) }
    val questions = remember(mode, attempt) { LingoContent.questionsFor(mode, 5) }
    var index by rememberSaveable(mode.name, attempt) { mutableIntStateOf(0) }
    var selected by rememberSaveable(mode.name, attempt) { mutableStateOf<Int?>(null) }
    var checked by rememberSaveable(mode.name, attempt) { mutableStateOf(false) }
    var correctCount by rememberSaveable(mode.name, attempt) { mutableIntStateOf(0) }
    var finished by rememberSaveable(mode.name, attempt) { mutableStateOf(false) }
    var awardedXp by rememberSaveable(mode.name, attempt) { mutableIntStateOf(0) }

    val palette = when (mode) {
        PracticeMode.DAILY -> AccentPalette(Coral, Peach, CoralDeep)
        PracticeMode.LISTENING -> AccentPalette(Blue, Sky, Color(0xFF234A6E))
        PracticeMode.GRAMMAR -> AccentPalette(Purple, Lavender, Color(0xFF453572))
        PracticeMode.VOCABULARY -> AccentPalette(Emerald, Mint, Forest)
    }

    if (questions.isEmpty()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Cream)
                .statusBarsPadding(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Practice is taking a nap.", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                SecondaryButton(text = "Go back", onClick = onClose)
            }
        }
        return
    }

    if (finished) {
        QuizResult(
            mode = mode,
            correct = correctCount,
            total = questions.size,
            awardedXp = awardedXp,
            palette = palette,
            onTryAgain = {
                attempt += 1
                index = 0
                selected = null
                checked = false
                correctCount = 0
                finished = false
                awardedXp = 0
            },
            onClose = onClose,
        )
        return
    }

    val question = questions[index]

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
                    .size(48.dp)
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
                        text = mode.title.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.deep,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${index + 1} / ${questions.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                }
                Spacer(Modifier.height(7.dp))
                ProgressLine(
                    progress = (index + 1f) / questions.size,
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
                targetState = index,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) },
                label = "quizQuestion",
                modifier = Modifier.fillMaxSize(),
            ) { current ->
                val exercise = questions[current]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp)
                        .padding(top = 12.dp, bottom = 20.dp),
                ) {
                    Text(
                        text = when {
                            exercise.audioText != null -> "Listen closely."
                            exercise.options.size == 4 && exercise.question.contains("___") -> "Fill the gap."
                            else -> "What do you think?"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        color = Ink,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Trust your instincts, then check the answer.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                    Spacer(Modifier.height(20.dp))
                    ExerciseView(
                        exercise = exercise,
                        selectedIndex = selected,
                        checked = checked,
                        onSelect = { selected = it },
                        onSpeak = speaker,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Cream)
                .padding(horizontal = 22.dp, vertical = 12.dp),
        ) {
            if (!checked) {
                PrimaryButton(
                    text = "Check answer",
                    onClick = {
                        checked = true
                        if (selected == question.correctIndex) correctCount += 1
                    },
                    enabled = selected != null,
                    modifier = Modifier.fillMaxWidth(),
                    container = palette.deep,
                )
            } else {
                PrimaryButton(
                    text = if (index == questions.lastIndex) "See results" else "Continue",
                    onClick = {
                        if (index == questions.lastIndex) {
                            awardedXp = store.recordQuiz(correctCount, questions.size)
                            finished = true
                        } else {
                            index += 1
                            selected = null
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
private fun QuizResult(
    mode: PracticeMode,
    correct: Int,
    total: Int,
    awardedXp: Int,
    palette: AccentPalette,
    onTryAgain: () -> Unit,
    onClose: () -> Unit,
) {
    val ratio = if (total == 0) 0f else correct / total.toFloat()
    val percent = (ratio * 100).toInt()
    val title = when {
        ratio >= 1f -> "A perfect run!"
        ratio >= 0.8f -> "Strong finish!"
        ratio >= 0.5f -> "Good progress!"
        else -> "A useful start."
    }
    val resultSticker = when {
        ratio >= 1f -> StickerKind.CELEBRATION
        ratio >= 0.8f -> StickerKind.SPARKLE
        ratio >= 0.5f -> StickerKind.SPROUT
        else -> StickerKind.TARGET
    }
    val subtitle = when {
        ratio >= 0.8f -> "Your instincts are getting sharper. Keep this momentum."
        ratio >= 0.5f -> "You're building real understanding. A little more practice will lock it in."
        else -> "Mistakes are where the learning happens. Try the same set once more."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        if (ratio >= 0.8f) {
            Confetti(Modifier.fillMaxSize(), particleCount = 40, seed = mode.ordinal + 11)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))
            GoalRing(
                progress = ratio,
                size = 136.dp,
                strokeWidth = 10.dp,
                track = Sand,
                color = palette.main,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$percent%",
                        style = MaterialTheme.typography.displayMedium,
                        color = Ink,
                    )
                    Text(
                        text = "SCORE",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                        color = Muted,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Sticker(kind = resultSticker, size = 54.dp)
            Spacer(Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                color = Ink,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = Muted,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Tag(text = mode.title.uppercase(), background = palette.soft, color = palette.deep)
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    color = palette.soft,
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("$correct / $total", style = MaterialTheme.typography.headlineSmall, color = palette.deep)
                        Text(
                            "CORRECT",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = palette.deep.copy(alpha = 0.65f),
                        )
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    color = LimeSoft,
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("+$awardedXp", style = MaterialTheme.typography.headlineSmall, color = Ink)
                        Text(
                            "XP EARNED",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = InkSoft.copy(alpha = 0.65f),
                        )
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
            Text(
                text = "“Practice makes fluent.”",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
                fontStyle = FontStyle.Italic,
            )
            Spacer(Modifier.height(28.dp))
            PrimaryButton(
                text = "Try another round",
                onClick = onTryAgain,
                modifier = Modifier.fillMaxWidth(),
                trailingArrow = true,
            )
            Spacer(Modifier.height(10.dp))
            SecondaryButton(
                text = "Back to practice",
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(28.dp))
        }
    }
}
