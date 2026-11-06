package com.lingo.app.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingo.app.data.Exercise
import com.lingo.app.data.LingoContent
import com.lingo.app.data.ProgressStore
import com.lingo.app.data.ReviewQueue
import com.lingo.app.data.StickerKind

/**
 * A local-only review session over a snapshot of up to five queued questions.
 * The queue can change underneath (another surface could record an answer), so
 * the session resolves its IDs once and never swaps a question mid-answer.
 */
@Composable
fun ReviewScreen(
    store: ProgressStore,
    speaker: (String) -> Unit,
    backLabel: String,
    onClose: () -> Unit,
) {
    BackHandler(onBack = onClose)

    var attempt by rememberSaveable { mutableIntStateOf(0) }
    val sessionIds = rememberSaveable(attempt) {
        ArrayList(store.reviewExercises(ReviewQueue.SESSION_SIZE).map { it.id })
    }
    val session = remember(sessionIds) {
        sessionIds.mapNotNull { LingoContent.exerciseById(it) }
    }

    var index by rememberSaveable(attempt) { mutableIntStateOf(0) }
    var selected by rememberSaveable(attempt) { mutableStateOf<Int?>(null) }
    var checked by rememberSaveable(attempt) { mutableStateOf(false) }
    var correctCount by rememberSaveable(attempt) { mutableIntStateOf(0) }
    var finished by rememberSaveable(attempt) { mutableStateOf(false) }
    var awardedXp by rememberSaveable(attempt) { mutableIntStateOf(0) }

    if (session.isEmpty()) {
        ReviewEmptyState(backLabel = backLabel, onClose = onClose)
        return
    }

    if (finished) {
        ReviewResult(
            correct = correctCount,
            total = session.size,
            awardedXp = awardedXp,
            remaining = store.reviewDueCount,
            backLabel = backLabel,
            onReviewAgain = {
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

    val currentIndex = index.coerceIn(0, session.lastIndex)
    val exercise = session[currentIndex]

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
                    .clickable(onClickLabel = backLabel, onClick = onClose),
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
                        text = "SMART REVIEW",
                        style = MaterialTheme.typography.labelSmall,
                        color = CoralDeep,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${currentIndex + 1} of ${session.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                }
                Spacer(Modifier.height(7.dp))
                ProgressLine(
                    progress = (currentIndex + 1f) / session.size,
                    color = Forest,
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
                targetState = currentIndex,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) },
                label = "reviewQuestion",
                modifier = Modifier.fillMaxSize(),
            ) { current ->
                val question = session[current]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp)
                        .padding(top = 12.dp, bottom = 20.dp),
                ) {
                    Text(
                        text = "Question ${current + 1} of ${session.size}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Ink,
                        modifier = Modifier.semantics { heading() },
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "These are the ones that needed another pass. Take your time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                    Spacer(Modifier.height(20.dp))
                    ExerciseView(
                        exercise = question,
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
                        val correct = exercise.isAnswerCorrect(selected, "")
                        if (correct) {
                            correctCount += 1
                            store.recordReviewCorrect(exercise.id)
                        } else {
                            store.recordReviewMiss(exercise.id)
                        }
                    },
                    enabled = selected != null,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                PrimaryButton(
                    text = if (currentIndex == session.lastIndex) "See results" else "Next question",
                    onClick = {
                        if (currentIndex == session.lastIndex) {
                            awardedXp = store.recordReviewSession(correctCount, session.size)
                            finished = true
                        } else {
                            index = currentIndex + 1
                            selected = null
                            checked = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    trailingArrow = true,
                )
            }
        }
    }
}

@Composable
private fun ReviewEmptyState(
    backLabel: String,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Sticker(kind = StickerKind.TARGET, size = 64.dp)
        Spacer(Modifier.height(18.dp))
        Text(
            text = "Nothing tricky right now.",
            style = MaterialTheme.typography.displaySmall,
            color = Ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Missed questions will collect here for a quick review.",
            style = MaterialTheme.typography.bodyLarge,
            color = Muted,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        PrimaryButton(
            text = backLabel,
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ReviewResult(
    correct: Int,
    total: Int,
    awardedXp: Int,
    remaining: Int,
    backLabel: String,
    onReviewAgain: () -> Unit,
    onClose: () -> Unit,
) {
    val ratio = if (total == 0) 0f else correct / total.toFloat()
    val percent = (ratio * 100).toInt()
    val title = when {
        ratio >= 1f -> "Sharp work."
        ratio >= 0.6f -> "Progress, not perfection."
        else -> "These need another pass."
    }
    val subtitle = when {
        remaining == 0 -> "You cleared $correct of $total, and the queue is empty."
        remaining == 1 -> "You cleared $correct of $total. 1 question is still waiting."
        else -> "You cleared $correct of $total. $remaining questions are still waiting."
    }
    val ringColor = when {
        ratio >= 0.8f -> Emerald
        ratio >= 0.6f -> Gold
        else -> Coral
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
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
                color = ringColor,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$percent%",
                        style = MaterialTheme.typography.displayMedium,
                        color = Ink,
                    )
                    Text(
                        text = "REVIEWED",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                        color = Muted,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Sticker(kind = StickerKind.TARGET, size = 54.dp)
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
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ResultTile(
                    value = "$correct / $total",
                    label = "CORRECT",
                    background = Mint,
                    color = Forest,
                    modifier = Modifier.weight(1f),
                )
                ResultTile(
                    value = "+$awardedXp",
                    label = "XP EARNED",
                    background = LimeSoft,
                    color = Ink,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(26.dp))
            Text(
                text = "“A mistake repeated becomes a lesson learned.”",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(26.dp))
            if (remaining > 0) {
                PrimaryButton(
                    text = "Review again",
                    onClick = onReviewAgain,
                    modifier = Modifier.fillMaxWidth(),
                    trailingArrow = true,
                )
                Spacer(Modifier.height(10.dp))
                SecondaryButton(
                    text = backLabel,
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                PrimaryButton(
                    text = backLabel,
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                    trailingArrow = true,
                )
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ResultTile(
    value: String,
    label: String,
    background: androidx.compose.ui.graphics.Color,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = background,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = color)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = color.copy(alpha = 0.65f),
            )
        }
    }
}
