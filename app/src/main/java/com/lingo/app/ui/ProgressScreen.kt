package com.lingo.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingo.app.data.LingoContent
import com.lingo.app.data.LingoLevels
import com.lingo.app.data.ProgressStore
import com.lingo.app.data.StickerKind
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ProgressScreen(store: ProgressStore, onOpenLevelPath: () -> Unit = {}) {
    val level = store.level
    val week = store.weeklyActivity()
    val weekTotal = week.sumOf { it.xp }
    val weekMax = maxOf(week.maxOf { it.xp }, store.dailyGoal, 1)
    val completedCount = LingoContent.lessons.count { it.id in store.completedLessons }
    val anyUnitComplete = LingoContent.units.any { unit ->
        unit.lessons.all { it.id in store.completedLessons }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Coral),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("YOUR PROGRESS", style = MaterialTheme.typography.labelSmall, color = Muted)
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Look how far\nyou've come.",
                    style = MaterialTheme.typography.displaySmall,
                    color = Ink,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Every lesson, word, and quiz adds up. Here's the proof.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Muted,
                )
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                color = Forest,
            ) {
                Row(
                    modifier = Modifier.padding(22.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GoalRing(
                        progress = level.progress,
                        size = 104.dp,
                        strokeWidth = 8.dp,
                        track = Cream.copy(alpha = 0.13f),
                        color = Lime,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "LV",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                color = Cream.copy(alpha = 0.6f),
                            )
                            Text(
                                text = "${level.level}",
                                style = MaterialTheme.typography.displaySmall,
                                color = Cream,
                            )
                        }
                    }
                    Spacer(Modifier.width(20.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = level.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Cream,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "${store.totalXp} XP earned",
                            style = MaterialTheme.typography.bodySmall,
                            color = Cream.copy(alpha = 0.65f),
                        )
                        Spacer(Modifier.height(12.dp))
                        ProgressLine(
                            progress = level.progress,
                            color = Lime,
                            track = Cream.copy(alpha = 0.14f),
                            height = 6.dp,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "${level.xpForNext - level.xpInLevel} XP to level ${level.level + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Cream.copy(alpha = 0.55f),
                        )
                    }
                }
            }
        }

        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable(onClick = onOpenLevelPath),
                shape = RoundedCornerShape(24.dp),
                color = Paper,
                border = BorderStroke(1.dp, Line),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CefrBadge(level = store.cefrLevel, size = 48.dp, outlined = true)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("ENGLISH LEVEL", style = MaterialTheme.typography.labelSmall, color = Muted)
                        Text(
                            "${store.cefrLevel.label} · ${store.cefrLevel.title}",
                            style = MaterialTheme.typography.titleLarge,
                            color = Ink,
                        )
                        Text(
                            if (store.weeklyAttempted) "Weekly test ${store.weeklyScore}/${LingoLevels.WEEKLY_QUESTION_COUNT} · View level path"
                            else "Level path, exams, and weekly test",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                    }
                    Text("→", fontSize = 20.sp, color = Forest, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }
        }

        item {
            Column {
                SectionHeader(title = "At a glance")
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(
                        sticker = StickerKind.BOOK,
                        value = "$completedCount",
                        label = "lessons finished",
                        background = Mint,
                        modifier = Modifier.weight(1f),
                    )
                    StatTile(
                        sticker = StickerKind.BULB,
                        value = "${store.masteredWords.size}",
                        label = "words mastered",
                        background = LimeSoft,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(
                        sticker = StickerKind.TARGET,
                        value = "${store.accuracy}%",
                        label = "quiz accuracy",
                        background = Sky,
                        modifier = Modifier.weight(1f),
                    )
                    StatTile(
                        sticker = StickerKind.FLAME,
                        value = "${store.bestStreak}",
                        label = "best streak",
                        background = Peach,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                color = Paper,
                border = BorderStroke(1.dp, Line),
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("This week", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.weight(1f))
                        Tag(text = "$weekTotal XP", background = Mint, color = Forest)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Daily XP, last seven days",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        week.forEach { day ->
                            val isToday = day.date == LocalDate.now()
                            val fraction = day.xp / weekMax.toFloat()
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = if (day.xp > 0) "${day.xp}" else "",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = if (isToday) Coral else Muted,
                                    modifier = Modifier.height(14.dp),
                                )
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .height(92.dp)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.BottomCenter,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.56f)
                                            .height((8 + 84 * fraction).dp.coerceAtMost(92.dp))
                                            .clip(RoundedCornerShape(7.dp))
                                            .background(
                                                when {
                                                    day.xp == 0 -> Sand
                                                    isToday -> Coral
                                                    else -> Emerald
                                                },
                                            ),
                                    )
                                }
                                Spacer(Modifier.height(7.dp))
                                Text(
                                    text = day.date.format(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        letterSpacing = 0.sp,
                                    ),
                                    color = if (isToday) Ink else Muted,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                color = Peach,
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Sticker(kind = StickerKind.FLAME, size = 58.dp)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = if (store.streak > 0) "You're on a roll" else "Start your streak",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF8C3B27),
                        )
                        Text(
                            text = if (store.streak > 0) {
                                "${store.streak} day${if (store.streak == 1) "" else "s"} in a row · best ${store.bestStreak}"
                            } else {
                                "One lesson today is all it takes."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8C3B27).copy(alpha = 0.72f),
                        )
                    }
                    Text(
                        text = if (store.streak > 0) "${store.streak}" else "0",
                        style = MaterialTheme.typography.displaySmall,
                        color = Color(0xFF8C3B27),
                    )
                }
            }
        }

        item {
            Column {
                SectionHeader(title = "Achievements")
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AchievementRow(
                        sticker = StickerKind.SPROUT,
                        title = "First Steps",
                        description = "Finish your first lesson",
                        unlocked = completedCount >= 1,
                        progress = "${completedCount.coerceAtMost(1)} / 1",
                    )
                    AchievementRow(
                        sticker = StickerKind.BOOK,
                        title = "Word Collector",
                        description = "Master 10 new words",
                        unlocked = store.masteredWords.size >= 10,
                        progress = "${store.masteredWords.size.coerceAtMost(10)} / 10",
                    )
                    AchievementRow(
                        sticker = StickerKind.FLAME,
                        title = "On Fire",
                        description = "Reach a 3-day streak",
                        unlocked = store.bestStreak >= 3,
                        progress = "${store.bestStreak.coerceAtMost(3)} / 3",
                    )
                    AchievementRow(
                        sticker = StickerKind.TARGET,
                        title = "Quiz Whiz",
                        description = "Answer 20 questions correctly",
                        unlocked = store.quizCorrect >= 20,
                        progress = "${store.quizCorrect.coerceAtMost(20)} / 20",
                    )
                    AchievementRow(
                        sticker = StickerKind.TROPHY,
                        title = "Unit Champion",
                        description = "Complete an entire unit",
                        unlocked = anyUnitComplete,
                        progress = if (anyUnitComplete) "1 / 1" else "0 / 1",
                    )
                    AchievementRow(
                        sticker = StickerKind.THEATER,
                        title = "Scene Stealer",
                        description = "Complete a Real Talk scene",
                        unlocked = store.completedRoleplays.isNotEmpty(),
                        progress = "${store.completedRoleplays.size.coerceAtMost(1)} / 1",
                    )
                    AchievementRow(
                        sticker = StickerKind.PUZZLE,
                        title = "Sentence Architect",
                        description = "Build 5 word-tile sentences",
                        unlocked = store.solvedSentences.size >= 5,
                        progress = "${store.solvedSentences.size.coerceAtMost(5)} / 5",
                    )
                    AchievementRow(
                        sticker = StickerKind.SPARKLE,
                        title = "Perfectionist",
                        description = "Ace a practice quiz",
                        unlocked = store.perfectQuizzes >= 1,
                        progress = if (store.perfectQuizzes >= 1) "1 / 1" else "0 / 1",
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "“The limits of my language mean the limits of my world.”",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "— Ludwig Wittgenstein",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted.copy(alpha = 0.75f),
                )
            }
        }
    }
}

@Composable
private fun StatTile(
    sticker: StickerKind,
    value: String,
    label: String,
    background: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = background,
    ) {
        Column(Modifier.padding(16.dp)) {
            Sticker(kind = sticker, size = 32.dp)
            Spacer(Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                color = Ink,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = InkSoft.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AchievementRow(
    sticker: StickerKind,
    title: String,
    description: String,
    unlocked: Boolean,
    progress: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (unlocked) Paper else Cream,
        border = BorderStroke(1.dp, if (unlocked) MintDeep else Line.copy(alpha = 0.6f)),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Sticker(
                kind = sticker,
                size = 42.dp,
                modifier = Modifier.alpha(if (unlocked) 1f else 0.55f),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (unlocked) Ink else Muted,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            if (unlocked) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Emerald),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("✓", color = Cream, fontSize = 12.sp)
                }
            } else {
                Text(
                    text = progress,
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
            }
        }
    }
}
