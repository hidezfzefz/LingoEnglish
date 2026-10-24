package com.lingo.app.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingo.app.data.CallStore
import com.lingo.app.data.CefrLevel
import com.lingo.app.data.LingoLevels
import com.lingo.app.data.ProgressStore
import com.lingo.app.data.WeeklyEntry
import com.lingo.app.data.StickerKind
import java.time.format.DateTimeFormatter
import java.util.Locale

fun levelAccent(level: CefrLevel): Color = when (level) {
    CefrLevel.A1 -> Mint
    CefrLevel.A2 -> Sand
    CefrLevel.B1 -> Sky
    CefrLevel.B2 -> Lavender
    CefrLevel.C1 -> Peach
    CefrLevel.C2 -> Seafoam
    CefrLevel.TOP -> LimeSoft
}

fun levelInk(level: CefrLevel): Color = when (level) {
    CefrLevel.B2 -> Purple
    CefrLevel.C1 -> Coral
    CefrLevel.C2 -> Teal
    else -> Forest
}

@Composable
fun CefrBadge(
    level: CefrLevel,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    outlined: Boolean = false,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(levelAccent(level))
            .then(if (outlined) Modifier.border(1.dp, levelInk(level).copy(alpha = 0.22f), CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = level.label,
            color = levelInk(level),
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = (size.value * 0.28f).sp,
                letterSpacing = 0.sp,
            ),
        )
    }
}

@Composable
fun LevelPathScreen(
    store: ProgressStore,
    callStore: CallStore,
    onClose: () -> Unit,
    onStartExam: (CefrLevel) -> Unit,
    onStartWeekly: () -> Unit,
) {
    val current = store.cefrLevel
    val next = store.nextCefrLevel
    val ready = next?.let { store.canAttemptLevel(it) } == true
    val retry = store.examRetryDate

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            BackHeader(onBack = onClose, title = "English level")
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Coral),
                )
                Spacer(Modifier.width(8.dp))
                Text("YOUR LEVEL PATH", style = MaterialTheme.typography.labelSmall, color = Muted)
            }
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                color = Forest,
            ) {
                Column(Modifier.padding(22.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Sticker(
                            kind = current.sticker,
                            size = 72.dp,
                            background = Lime,
                            tint = Forest,
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "YOU ARE HERE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Lime,
                            )
                            Spacer(Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    current.label,
                                    style = MaterialTheme.typography.displayMedium,
                                    color = Cream,
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    current.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Cream.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(bottom = 7.dp),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(17.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HeroTag("${store.totalXp} XP earned")
                        HeroTag("${store.activeDayCount} active day${if (store.activeDayCount == 1) "" else "s"}")
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "A ladder, not a race. Each step takes practice, time, and a focused exam.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Cream.copy(alpha = 0.72f),
                    )
                }
            }
        }
        if (next != null) {
            item {
                NextLevelCard(
                    store = store,
                    next = next,
                    ready = ready,
                    retry = retry,
                    onStartExam = { onStartExam(next) },
                )
            }
        } else {
            item {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = Paper,
                    border = BorderStroke(1.dp, Line),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("TOP FLIGHT", style = MaterialTheme.typography.labelSmall, color = Muted)
                        Spacer(Modifier.height(4.dp))
                        Text("You have reached the highest rung.", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Keep your edge with the weekly test and conversations with other advanced learners.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Muted,
                        )
                    }
                }
            }
        }
        item {
            WeeklyTestCard(
                store = store,
                callStore = callStore,
                onStartWeekly = onStartWeekly,
            )
        }
        item {
            SectionHeader(title = "The whole ladder")
        }
        items(CefrLevel.entries.size) { index ->
            val level = CefrLevel.entries[index]
            LadderRow(
                level = level,
                current = current,
                canStartNext = ready && level == next,
                onStart = { if (level == next && ready) onStartExam(level) },
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Paper)
                    .border(1.dp, Line, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Sticker(kind = StickerKind.BALANCE, size = 34.dp, elevated = false)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("How promotion works", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "Only a level exam moves you up. The weekly test is a leaderboard challenge, not a promotion. Leaving the app during an exam ends the attempt.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroTag(text: String) {
    Box(
        Modifier
            .clip(CircleShape)
            .background(Cream.copy(alpha = 0.11f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = Cream.copy(alpha = 0.8f))
    }
}

@Composable
private fun NextLevelCard(
    store: ProgressStore,
    next: CefrLevel,
    ready: Boolean,
    retry: java.time.LocalDate?,
    onStartExam: () -> Unit,
) {
    val xpProgress = if (next.xpGate == 0) 1f else (store.totalXp.toFloat() / next.xpGate).coerceIn(0f, 1f)
    val dayProgress = if (next.activeDaysGate == 0) 1f else (store.activeDayCount.toFloat() / next.activeDaysGate).coerceIn(0f, 1f)
    val cooldown = retry != null && java.time.LocalDate.now().isBefore(retry)
    val retryLabel = retry?.format(DateTimeFormatter.ofPattern("MMM d", Locale.US))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Paper,
        border = BorderStroke(1.dp, Line),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CefrBadge(level = next, size = 50.dp, outlined = true)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("NEXT CHALLENGE", style = MaterialTheme.typography.labelSmall, color = Muted)
                    Text(
                        "${next.label} · ${next.title}",
                        style = MaterialTheme.typography.titleLarge,
                        color = Ink,
                    )
                }
                if (ready) {
                    Tag(text = "READY", background = Mint, color = Emerald)
                }
            }
            Spacer(Modifier.height(16.dp))
            GateLine(
                label = "Experience",
                value = "${store.totalXp.coerceAtMost(next.xpGate)} / ${next.xpGate} XP",
                progress = xpProgress,
                color = Emerald,
            )
            Spacer(Modifier.height(10.dp))
            GateLine(
                label = "Study days",
                value = "${store.activeDayCount.coerceAtMost(next.activeDaysGate)} / ${next.activeDaysGate} days",
                progress = dayProgress,
                color = Coral,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "${LingoLevels.LEVEL_QUESTION_COUNT} questions across grammar, vocabulary, reading and use of English · ${LingoLevels.LEVEL_MINUTES} minutes · pass ${LingoLevels.LEVEL_PASS_SCORE} of ${LingoLevels.LEVEL_QUESTION_COUNT} to move up",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
            Spacer(Modifier.height(12.dp))
            PrimaryButton(
                text = when {
                    ready -> "Start ${next.label} exam"
                    cooldown -> "Try again $retryLabel"
                    xpProgress < 1f || dayProgress < 1f -> "Keep learning to unlock"
                    else -> "Try again next week"
                },
                onClick = onStartExam,
                enabled = ready,
                modifier = Modifier.fillMaxWidth(),
                trailingArrow = ready,
                container = if (ready) Forest else Sand,
                contentColor = if (ready) Cream else Muted,
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = Muted,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    if (cooldown) "A failed attempt waits seven days before another try."
                    else "Leaving the app during the exam ends the attempt.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
        }
    }
}

@Composable
private fun GateLine(
    label: String,
    value: String,
    progress: Float,
    color: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = InkSoft)
        Text(value, style = MaterialTheme.typography.labelSmall, color = Muted)
    }
    Spacer(Modifier.height(5.dp))
    ProgressLine(progress = progress, height = 7.dp, color = color)
}

@Composable
private fun WeeklyTestCard(
    store: ProgressStore,
    callStore: CallStore,
    onStartWeekly: () -> Unit,
) {
    val attempted = store.weeklyAttempted
    LaunchedEffect(attempted, callStore.signedIn) {
        if (attempted) callStore.refreshWeeklyLeaderboard()
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = if (attempted) Paper else Lavender.copy(alpha = 0.52f),
        border = BorderStroke(1.dp, if (attempted) Line else Lavender),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Sticker(kind = StickerKind.TROPHY, size = 54.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("WEEKLY LEVEL TEST", style = MaterialTheme.typography.labelSmall, color = Muted)
                    Text("The weekly challenge", style = MaterialTheme.typography.titleLarge)
                }
                Tag(text = "LEADERBOARD", background = Lavender, color = Purple)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "A fresh ${LingoLevels.WEEKLY_QUESTION_COUNT}-question challenge at your level, mixing new material with earlier skills. One attempt each week; your score does not promote you.",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )
            Spacer(Modifier.height(12.dp))
            if (attempted) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Cream)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("THIS WEEK'S SCORE", style = MaterialTheme.typography.labelSmall, color = Muted)
                        Text(
                            "${store.weeklyScore} / ${LingoLevels.WEEKLY_QUESTION_COUNT}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Forest,
                        )
                    }
                    Tag(text = "ENTRY SAVED", background = Mint, color = Emerald)
                }
                Spacer(Modifier.height(12.dp))
                when {
                    !callStore.signedIn -> {
                        Text(
                            "Sign in on the Call tab to join the live weekly board.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                    }
                    callStore.weeklyLoading -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Purple,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Loading your cohort…", style = MaterialTheme.typography.bodySmall, color = Muted)
                        }
                    }
                    callStore.weeklyEntries.isEmpty() -> {
                        Text(
                            "Your score is saved. The board appears when other learners submit this week.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                    }
                    else -> {
                        callStore.weeklyEntries.take(3).forEachIndexed { index, entry ->
                            MiniLeaderRow(index + 1, entry, LingoLevels.WEEKLY_QUESTION_COUNT)
                            if (index < callStore.weeklyEntries.take(3).lastIndex) Spacer(Modifier.height(4.dp))
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Same-level cohort · resets every Monday",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                    }
                }
            } else {
                PrimaryButton(
                    text = "Take this week's test",
                    onClick = onStartWeekly,
                    modifier = Modifier.fillMaxWidth(),
                    trailingArrow = true,
                    container = Purple,
                    contentColor = Cream,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "${LingoLevels.WEEKLY_QUESTION_COUNT} questions · ${LingoLevels.WEEKLY_MINUTES} minutes · one attempt per week",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
        }
    }
}

@Composable
private fun MiniLeaderRow(rank: Int, entry: WeeklyEntry, total: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (entry.isYou) Mint else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "$rank",
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
            modifier = Modifier.width(20.dp),
        )
        Text(
            if (entry.isYou) "You" else entry.displayName,
            style = MaterialTheme.typography.bodySmall,
            color = Ink,
            fontWeight = if (entry.isYou) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )
        Text("${entry.score}/$total", style = MaterialTheme.typography.labelSmall, color = Muted)
    }
}

@Composable
private fun LadderRow(
    level: CefrLevel,
    current: CefrLevel,
    canStartNext: Boolean,
    onStart: () -> Unit,
) {
    val completed = level.ordinal < current.ordinal
    val isCurrent = level == current
    val next = level.ordinal == current.ordinal + 1
    val locked = level.ordinal > current.ordinal + 1
    val background = when {
        isCurrent -> Mint
        canStartNext -> Lavender.copy(alpha = 0.5f)
        else -> Paper
    }
    val status = when {
        completed -> "Exam passed"
        isCurrent -> "This is your level"
        canStartNext -> "Ready to challenge"
        next && !locked -> "Keep practising to unlock"
        else -> "Unlocks after ${CefrLevel.entries[level.ordinal - 1].label}"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .border(1.dp, if (isCurrent) MintDeep else Line, RoundedCornerShape(20.dp))
            .then(
                if (canStartNext) Modifier.clickable(onClick = onStart) else Modifier,
            )
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CefrBadge(level = level, size = 42.dp, outlined = !isCurrent)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    level.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (locked) Muted else Ink,
                )
                if (isCurrent) {
                    Spacer(Modifier.width(7.dp))
                    Tag(text = "CURRENT", background = Forest, color = Lime)
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(status, style = MaterialTheme.typography.bodySmall, color = Muted)
        }
        when {
            completed -> Icon(
                Icons.Rounded.Check,
                contentDescription = "Passed",
                tint = Emerald,
                modifier = Modifier.size(19.dp),
            )
            isCurrent -> Sticker(kind = current.sticker, size = 27.dp, elevated = false)
            canStartNext -> Icon(
                Icons.Rounded.ArrowForward,
                contentDescription = "Start exam",
                tint = Purple,
                modifier = Modifier.size(19.dp),
            )
            else -> Icon(
                Icons.Rounded.Lock,
                contentDescription = "Locked",
                tint = Muted.copy(alpha = 0.7f),
                modifier = Modifier.size(17.dp),
            )
        }
    }
}
