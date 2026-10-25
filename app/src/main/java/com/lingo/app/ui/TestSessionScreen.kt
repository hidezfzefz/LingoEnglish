package com.lingo.app.ui

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.lingo.app.data.CallStore
import com.lingo.app.data.CefrLevel
import com.lingo.app.data.LingoLevels
import com.lingo.app.data.PreparedQuestion
import com.lingo.app.data.ProgressStore
import com.lingo.app.data.StickerKind
import com.lingo.app.data.TestKind
import com.lingo.app.data.TestSkill
import com.lingo.app.data.WeeklyEntry
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class TestPhase { ONGOING, RESULT, INVALIDATED }

private data class SkillScore(
    val skill: TestSkill,
    val correct: Int,
    val total: Int,
)

@Composable
fun TestSessionScreen(
    store: ProgressStore,
    callStore: CallStore,
    kind: TestKind,
    target: CefrLevel,
    onClose: () -> Unit,
) {
    val weekly = kind == TestKind.WEEKLY
    val minutes = if (weekly) LingoLevels.WEEKLY_MINUTES else LingoLevels.LEVEL_MINUTES
    val passScore = LingoLevels.LEVEL_PASS_SCORE

    val source = remember(kind, target, store.cefrLevel) {
        if (weekly) LingoLevels.weeklyQuestions(store.cefrLevel) else LingoLevels.examFor(target)
    }
    val questions = remember(source) { LingoLevels.prepare(source) }
    val answers = remember(questions) {
        mutableStateListOf<Int>().apply { repeat(questions.size) { add(-1) } }
    }
    var index by rememberSaveable { mutableIntStateOf(0) }
    var phase by remember { mutableStateOf(TestPhase.ONGOING) }
    var score by remember { mutableIntStateOf(0) }
    var passed by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    var recorded by remember { mutableStateOf(false) }
    var showExit by remember { mutableStateOf(false) }
    var showFinishHint by remember { mutableStateOf(false) }
    var remaining by remember { mutableLongStateOf(minutes * 60L) }

    LaunchedEffect(Unit) {
        // Guard restored test state: after a process death the store has
        // already consumed the attempt, so this screen must not reopen it.
        if (weekly) {
            if (store.weeklyAttempted) {
                onClose()
                return@LaunchedEffect
            }
            store.beginWeeklyTest()
        } else {
            if (!store.canAttemptLevel(target)) {
                onClose()
                return@LaunchedEffect
            }
            store.beginLevelExam(target)
        }
    }

    fun recordAttempt(finalScore: Int) {
        if (recorded) return
        recorded = true
        if (weekly) {
            store.finishWeeklyTest(finalScore)
            callStore.submitWeeklyScore(finalScore)
        } else {
            store.finishLevelExam(target, finalScore >= passScore)
        }
    }

    fun complete(timeUp: Boolean = false) {
        if (phase != TestPhase.ONGOING) return
        val correct = questions.indices.count { answers[it] == questions[it].correctIndex }
        score = correct
        passed = !weekly && correct >= passScore
        recordAttempt(correct)
        reason = if (timeUp) "Time is up. Your answers were submitted automatically." else ""
        phase = TestPhase.RESULT
    }

    fun endAttempt(message: String) {
        if (phase != TestPhase.ONGOING) return
        score = 0
        passed = false
        reason = message
        recordAttempt(0)
        phase = TestPhase.INVALIDATED
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, phase) {
        if (phase != TestPhase.ONGOING) {
            onDispose { }
        } else {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_STOP && phase == TestPhase.ONGOING) {
                    endAttempt("You left the app. This attempt has ended.")
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
    }

    LaunchedEffect(phase) {
        if (phase != TestPhase.ONGOING) return@LaunchedEffect
        val started = SystemClock.elapsedRealtime()
        val limit = minutes * 60L
        while (phase == TestPhase.ONGOING) {
            val elapsed = (SystemClock.elapsedRealtime() - started) / 1000L
            remaining = (limit - elapsed).coerceAtLeast(0L)
            if (remaining == 0L) {
                complete(timeUp = true)
                break
            }
            delay(250)
        }
    }

    BackHandler(enabled = true) {
        if (phase == TestPhase.ONGOING) showExit = true else onClose()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            TestTopBar(
                weekly = weekly,
                remaining = remaining,
                onClose = {
                    if (phase == TestPhase.ONGOING) showExit = true else onClose()
                },
            )
            if (phase == TestPhase.ONGOING) {
                val question = questions.getOrNull(index)
                if (question == null) {
                    EmptyTest(onClose = onClose)
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                    ) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "${question.skill.label.uppercase()} · ${index + 1} OF ${questions.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Muted,
                            )
                            Text(
                                text = if (weekly) "Leaderboard only" else "Pass $passScore of ${questions.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Emerald,
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        ProgressLine(
                            progress = (index + 1) / questions.size.toFloat(),
                            height = 6.dp,
                            color = Emerald,
                        )
                        Spacer(Modifier.height(24.dp))
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                        ) {
                            Text(
                                text = question.prompt,
                                style = MaterialTheme.typography.headlineSmall,
                                color = Ink,
                            )
                            Spacer(Modifier.height(20.dp))
                            question.options.forEachIndexed { optionIndex, option ->
                                ChoiceOption(
                                    letter = ('A' + optionIndex).toString(),
                                    text = option,
                                    state = if (answers[index] == optionIndex) OptionState.SELECTED else OptionState.DEFAULT,
                                    onClick = {
                                        answers[index] = optionIndex
                                        showFinishHint = false
                                    },
                                )
                                Spacer(Modifier.height(10.dp))
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Sand.copy(alpha = 0.65f))
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = Muted,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(9.dp))
                                Text(
                                    "Leaving the app ends this attempt. Stay with it.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Muted,
                                )
                            }
                            Spacer(Modifier.height(20.dp))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (index > 0) {
                                SecondaryButton(
                                    text = "Back",
                                    onClick = { index -= 1 },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            val last = index == questions.lastIndex
                            PrimaryButton(
                                text = if (last) "Finish test" else "Next question",
                                onClick = {
                                    if (!last) {
                                        index += 1
                                    } else if (answers.any { it < 0 }) {
                                        showFinishHint = true
                                    } else {
                                        complete()
                                    }
                                },
                                modifier = Modifier.weight(if (index > 0) 1f else 2f),
                                trailingArrow = !last,
                                enabled = !last || answers.all { it >= 0 },
                            )
                        }
                        if (showFinishHint) {
                            Text(
                                "Answer every question before finishing.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Danger,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            } else {
                val breakdown = if (phase == TestPhase.INVALIDATED) {
                    emptyList()
                } else {
                    TestSkill.entries.mapNotNull { skill ->
                        val items = questions.withIndex().filter { it.value.skill == skill }
                        if (items.isEmpty()) {
                            null
                        } else {
                            SkillScore(
                                skill = skill,
                                correct = items.count { answers[it.index] == it.value.correctIndex },
                                total = items.size,
                            )
                        }
                    }
                }
                TestResult(
                    callStore = callStore,
                    weekly = weekly,
                    phase = phase,
                    target = target,
                    score = score,
                    total = questions.size,
                    passed = passed,
                    reason = reason,
                    retryDate = store.examRetryDate,
                    level = store.cefrLevel,
                    weeklyScore = store.weeklyScore,
                    breakdown = breakdown,
                    questions = questions,
                    answers = answers,
                    onClose = onClose,
                )
            }
        }
    }

    if (showExit) {
        AlertDialog(
            onDismissRequest = { showExit = false },
            containerColor = Paper,
            titleContentColor = Ink,
            textContentColor = InkSoft,
            title = { Text("Leave this attempt?", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Text(
                    if (weekly) {
                        "This week's test will be marked as complete with a score of zero. You can try again next week."
                    } else {
                        "The attempt will end and your next try will be locked for seven days."
                    },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExit = false
                        endAttempt("You ended the attempt before finishing.")
                    },
                ) {
                    Text("End attempt", color = Danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExit = false }) {
                    Text("Keep going", color = Forest, fontWeight = FontWeight.Bold)
                }
            },
        )
    }
}

@Composable
private fun TestTopBar(
    weekly: Boolean,
    remaining: Long,
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Paper)
                .border(1.dp, Line, CircleShape)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Close test",
                tint = Ink,
                modifier = Modifier.size(19.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = if (weekly) "WEEKLY TEST" else "LEVEL EXAM",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
            Text(
                text = if (weekly) "This week's entry" else "Focused attempt",
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
            )
        }
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(if (remaining <= 60) Peach else Mint)
                .padding(horizontal = 12.dp, vertical = 7.dp),
        ) {
            Text(
                text = formatTime(remaining),
                style = MaterialTheme.typography.labelMedium,
                color = if (remaining <= 60) Danger else Forest,
            )
        }
    }
}

private fun formatTime(seconds: Long): String {
    val safe = seconds.coerceAtLeast(0L)
    return "%02d:%02d".format(safe / 60, safe % 60)
}

@Composable
private fun EmptyTest(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Sticker(kind = StickerKind.COMPASS, size = 72.dp)
        Spacer(Modifier.height(16.dp))
        Text("This test is not ready yet.", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            "Keep practising while new questions are prepared.",
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        PrimaryButton(text = "Back to level path", onClick = onClose)
    }
}

@Composable
private fun TestResult(
    callStore: CallStore,
    weekly: Boolean,
    phase: TestPhase,
    target: CefrLevel,
    score: Int,
    total: Int,
    passed: Boolean,
    reason: String,
    retryDate: LocalDate?,
    level: CefrLevel,
    weeklyScore: Int,
    breakdown: List<SkillScore>,
    questions: List<PreparedQuestion>,
    answers: List<Int>,
    onClose: () -> Unit,
) {
    val invalid = phase == TestPhase.INVALIDATED
    val title = when {
        weekly -> if (invalid) "Entry closed." else "Your week is in the books."
        invalid -> "Attempt ended."
        passed -> "${target.label} unlocked."
        else -> "Not this time."
    }
    val sticker = when {
        weekly -> StickerKind.TROPHY
        invalid -> StickerKind.TARGET
        passed -> StickerKind.CELEBRATION
        else -> StickerKind.COMPASS
    }
    val dateLabel = retryDate?.format(DateTimeFormatter.ofPattern("MMM d", Locale.US))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(14.dp))
        Sticker(kind = sticker, size = 96.dp)
        Spacer(Modifier.height(18.dp))
        Text(
            text = when {
                weekly -> "WEEKLY TEST"
                invalid -> "ATTEMPT ENDED"
                passed -> "LEVEL UP"
                else -> "KEEP GOING"
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (passed || weekly) Emerald else Muted,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            color = Ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = when {
                weekly -> "You scored $score out of $total. Only your first attempt each week counts."
                invalid && weekly -> "This week's entry is now used. A new test opens next week."
                invalid -> "This attempt has been recorded as incomplete."
                else -> "You answered $score out of $total correctly${if (passed) " · +50 XP" else ""}."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = Muted,
            textAlign = TextAlign.Center,
        )
        if (reason.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = reason,
                style = MaterialTheme.typography.bodyMedium,
                color = if (invalid) Danger else Muted,
                textAlign = TextAlign.Center,
            )
        }
        if (breakdown.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            SkillBreakdown(breakdown)
        }
        if (!weekly && phase == TestPhase.RESULT) {
            Spacer(Modifier.height(14.dp))
            AnswerReview(questions = questions, answers = answers)
        }
        if (!weekly && !passed) {
            Spacer(Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Peach.copy(alpha = 0.65f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Pass mark: ${LingoLevels.LEVEL_PASS_SCORE} of ${LingoLevels.LEVEL_QUESTION_COUNT} correct",
                        style = MaterialTheme.typography.titleSmall,
                        color = Ink,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (dateLabel != null) "Your next attempt opens on $dateLabel." else "You can try again next week.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                }
            }
        }
        if (weekly) {
            Spacer(Modifier.height(18.dp))
            WeeklyLiveBoard(
                callStore = callStore,
                level = level,
                yourScore = if (invalid) 0 else weeklyScore,
            )
        }
        if (passed) {
            Spacer(Modifier.height(18.dp))
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Mint,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Sticker(kind = target.sticker, size = 38.dp, elevated = false)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Lingo Live now matches you with ${target.label} learners.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Forest,
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            text = "Back to level path",
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SkillBreakdown(breakdown: List<SkillScore>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Paper,
        border = androidx.compose.foundation.BorderStroke(1.dp, Line),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "SKILL BREAKDOWN",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
            Spacer(Modifier.height(10.dp))
            breakdown.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        row.skill.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSoft,
                    )
                    Text(
                        "${row.correct} / ${row.total}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (row.correct == row.total) Emerald else Ink,
                    )
                }
                Spacer(Modifier.height(6.dp))
                ProgressLine(
                    progress = if (row.total == 0) 0f else row.correct / row.total.toFloat(),
                    height = 5.dp,
                    color = if (row.correct == row.total) Emerald else Coral,
                )
                Spacer(Modifier.height(10.dp))
            }
            Text(
                "Every test samples all four skills, so the result reflects range rather than one lucky topic.",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
        }
    }
}

@Composable
private fun AnswerReview(
    questions: List<PreparedQuestion>,
    answers: List<Int>,
) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Paper,
        border = androidx.compose.foundation.BorderStroke(1.dp, Line),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "ANSWER REVIEW",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        if (expanded) "Every question, with the reason." else "See what you missed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSoft,
                    )
                }
                TextButton(onClick = { expanded = !expanded }) {
                    Text(
                        if (expanded) "Hide" else "Review",
                        color = Forest,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            if (expanded) {
                Spacer(Modifier.height(10.dp))
                questions.forEachIndexed { index, question ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = Line,
                            modifier = Modifier.padding(vertical = 12.dp),
                        )
                    }
                    val chosenIndex = answers.getOrNull(index) ?: -1
                    val chosen = question.options.getOrNull(chosenIndex)
                    val correct = chosenIndex == question.correctIndex
                    Text(
                        "${index + 1}. ${question.prompt}",
                        style = MaterialTheme.typography.titleSmall,
                        color = Ink,
                    )
                    Spacer(Modifier.height(5.dp))
                    if (correct) {
                        Text(
                            "Correct — ${question.options[question.correctIndex]}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Emerald,
                        )
                    } else {
                        Text(
                            if (chosen == null) "No answer" else "Your answer: $chosen",
                            style = MaterialTheme.typography.bodySmall,
                            color = Danger,
                        )
                        Text(
                            "Correct answer: ${question.options[question.correctIndex]}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Emerald,
                        )
                    }
                    if (question.explanation.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            question.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyLiveBoard(
    callStore: CallStore,
    level: CefrLevel,
    yourScore: Int,
) {
    LaunchedEffect(level, yourScore, callStore.signedIn) {
        callStore.refreshWeeklyLeaderboard()
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Paper,
        border = androidx.compose.foundation.BorderStroke(1.dp, Line),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("${level.label} this week", style = MaterialTheme.typography.titleSmall)
                Tag(
                    text = if (callStore.signedIn) "LIVE COHORT" else "SIGN IN",
                    background = if (callStore.signedIn) Mint else Lavender,
                    color = if (callStore.signedIn) Emerald else Purple,
                )
            }
            Spacer(Modifier.height(4.dp))
            if (!callStore.signedIn) {
                Text(
                    "Sign in on the Call tab to join the live weekly board. Your local weekly test still saves normally.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            } else if (callStore.weeklyLoading) {
                Text("Loading this week's cohort…", style = MaterialTheme.typography.bodySmall, color = Muted)
            } else if (callStore.weeklyEntries.isEmpty()) {
                Text(
                    "Your $yourScore/${LingoLevels.WEEKLY_QUESTION_COUNT} is saved. The board appears when other learners submit this week.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            } else {
                callStore.weeklyEntries.take(5).forEachIndexed { position, entry ->
                    LeaderRow(position + 1, entry, LingoLevels.WEEKLY_QUESTION_COUNT)
                    if (position < callStore.weeklyEntries.take(5).lastIndex) {
                        Spacer(Modifier.height(7.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Same-level cohort · one attempt per week",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
        }
    }
}

@Composable
private fun LeaderRow(rank: Int, entry: WeeklyEntry, total: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (entry.isYou) Mint else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "$rank",
            style = MaterialTheme.typography.labelMedium,
            color = if (entry.isYou) Forest else Muted,
            modifier = Modifier.width(24.dp),
        )
        Text(
            if (entry.isYou) "You" else entry.displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = Ink,
            fontWeight = if (entry.isYou) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )
        Text(
            "${entry.score}/$total",
            style = MaterialTheme.typography.labelMedium,
            color = if (entry.isYou) Forest else Muted,
        )
    }
}
