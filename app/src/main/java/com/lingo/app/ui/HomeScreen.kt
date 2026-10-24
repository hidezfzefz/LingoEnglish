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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingo.app.data.LingoContent
import com.lingo.app.data.Lesson
import com.lingo.app.data.PracticeMode
import com.lingo.app.data.ProgressStore
import com.lingo.app.data.StickerKind
import java.time.LocalTime

@Composable
fun HomeScreen(
    store: ProgressStore,
    speaker: (String) -> Unit,
    onOpenLesson: (Lesson) -> Unit,
    onOpenLearn: () -> Unit,
    onOpenPractice: () -> Unit,
    onOpenQuiz: (PracticeMode) -> Unit,
    onOpenFlashcards: () -> Unit,
    onOpenWordBank: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val next = LingoContent.nextLesson(store.completedLessons)
    val allComplete = LingoContent.lessons.all { it.id in store.completedLessons }
    val word = LingoContent.wordOfTheDay()
    val greeting = when (LocalTime.now().hour) {
        in 0..11 -> "Good morning."
        in 12..17 -> "Good afternoon."
        else -> "Good evening."
    }
    val planDone = listOf(
        store.todayLessons > 0,
        store.todayWords >= 5,
        store.todayQuizzes > 0,
    ).count { it }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LingoLogo()
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "LINGO",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                            color = Forest,
                        )
                        Text(
                            text = "English, every day",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                    }
                    CircleStickerButton(
                        kind = StickerKind.SETTINGS,
                        contentDescription = "Settings",
                        onClick = onOpenSettings,
                        size = 42.dp,
                    )
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.displayMedium,
                    color = Ink,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "A little English goes a long way. Here's what's waiting for you.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Muted,
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatPill(
                        sticker = StickerKind.FLAME,
                        text = if (store.streak > 0) "${store.streak}-day streak" else "Start a streak",
                    )
                    StatPill(sticker = StickerKind.BOLT, text = "${store.totalXp} XP")
                    StatPill(sticker = StickerKind.SPARKLE, text = "XP lv ${store.level.level}")
                }
            }
        }

        item {
            DailyHeroCard(
                lesson = next,
                allComplete = allComplete,
                streak = store.streak,
                todayXp = store.todayXp,
                dailyGoal = store.dailyGoal,
                goalProgress = store.goalProgress,
                onContinue = {
                    if (allComplete) onOpenPractice() else onOpenLesson(next)
                },
            )
        }

        item {
            TodayPlanCard(
                next = next,
                todayLessons = store.todayLessons,
                todayWords = store.todayWords,
                todayQuizzes = store.todayQuizzes,
                doneCount = planDone,
                onLesson = { onOpenLesson(next) },
                onWords = onOpenWordBank,
                onQuiz = { onOpenQuiz(PracticeMode.DAILY) },
            )
        }

        item {
            WordOfTheDayCard(
                word = word,
                saved = word.id in store.savedWords,
                onSpeak = { speaker("${word.word}. ${word.definition} ${word.example}") },
                onToggleSave = { store.toggleSaved(word.id) },
                onOpenBank = onOpenWordBank,
            )
        }

        item {
            Column {
                SectionHeader(
                    title = "Your courses",
                    action = "See all",
                    onAction = onOpenLearn,
                )
                Spacer(Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(LingoContent.units) { unit ->
                        UnitPreviewCard(
                            unit = unit,
                            completed = unit.lessons.count { it.id in store.completedLessons },
                            onClick = onOpenLearn,
                        )
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "“One language sets you in a corridor for life.\nTwo languages open every door along the way.”",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "— Frank Smith",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted.copy(alpha = 0.8f),
                )
            }
        }
    }
}

@Composable
private fun DailyHeroCard(
    lesson: Lesson,
    allComplete: Boolean,
    streak: Int,
    todayXp: Int,
    dailyGoal: Int,
    goalProgress: Float,
    onContinue: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = Forest,
    ) {
        Box {
            DecorativeDots(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(190.dp)
                    .padding(4.dp),
                color = Lime.copy(alpha = 0.13f),
                count = 8,
            )
            Column(Modifier.padding(22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Tag(
                        text = if (allComplete) "PATH COMPLETE" else "TODAY'S LESSON",
                        background = Lime.copy(alpha = 0.16f),
                        color = Lime,
                    )
                    Spacer(Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Sticker(
                            kind = StickerKind.FLAME,
                            size = 22.dp,
                            elevated = false,
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = if (streak > 0) "$streak" else "Begin",
                            style = MaterialTheme.typography.labelMedium,
                            color = Cream.copy(alpha = 0.8f),
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = if (allComplete) "You've finished the path." else lesson.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Cream,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = if (allComplete) {
                        "Keep the words warm with a quick practice session."
                    } else {
                        lesson.subtitle
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Cream.copy(alpha = 0.68f),
                )
                Spacer(Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GoalRing(
                        progress = goalProgress,
                        size = 86.dp,
                        strokeWidth = 7.dp,
                        track = Cream.copy(alpha = 0.14f),
                        color = Lime,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$todayXp",
                                style = MaterialTheme.typography.titleLarge,
                                color = Cream,
                            )
                            Text(
                                text = "/ $dailyGoal XP",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.sp),
                                color = Cream.copy(alpha = 0.55f),
                            )
                        }
                    }
                    Spacer(Modifier.width(18.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = if (goalProgress >= 1f) "Daily goal complete" else "${(dailyGoal - todayXp).coerceAtLeast(0)} XP to go",
                            style = MaterialTheme.typography.titleMedium,
                            color = Cream,
                        )
                        Spacer(Modifier.height(3.dp))
                        if (allComplete) {
                            Text(
                                text = "Every lesson complete · keep practicing",
                                style = MaterialTheme.typography.bodySmall,
                                color = Cream.copy(alpha = 0.6f),
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Sticker(
                                    kind = lesson.icon,
                                    size = 21.dp,
                                    elevated = false,
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = "${lesson.minutes} min · +${lesson.xp} XP",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Cream.copy(alpha = 0.6f),
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                PrimaryButton(
                    text = if (allComplete) "Keep practicing" else "Continue lesson",
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth(),
                    container = Lime,
                    contentColor = Forest,
                    trailingArrow = true,
                )
            }
        }
    }
}

@Composable
private fun TodayPlanCard(
    next: Lesson,
    todayLessons: Int,
    todayWords: Int,
    todayQuizzes: Int,
    doneCount: Int,
    onLesson: () -> Unit,
    onWords: () -> Unit,
    onQuiz: () -> Unit,
) {
    Column {
        SectionHeader(
            title = "Today's plan",
            action = "$doneCount of 3 done",
        )
        Spacer(Modifier.height(12.dp))
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Paper,
            border = BorderStroke(1.dp, Line),
        ) {
            Column {
                PlanRow(
                    sticker = StickerKind.BOOK,
                    title = if (todayLessons > 0) "Lesson complete" else "One focused lesson",
                    subtitle = if (todayLessons > 0) "Lovely work — keep the momentum" else "${next.title} · ${next.minutes} min",
                    done = todayLessons > 0,
                    tint = Mint,
                    onClick = onLesson,
                )
                PlanDivider()
                PlanRow(
                    sticker = StickerKind.BULB,
                    title = "Five new words",
                    subtitle = if (todayWords >= 5) "Five new words learned" else "$todayWords of 5 words learned",
                    done = todayWords >= 5,
                    tint = LimeSoft,
                    onClick = onWords,
                )
                PlanDivider()
                PlanRow(
                    sticker = StickerKind.BOLT,
                    title = "A quick challenge",
                    subtitle = if (todayQuizzes > 0) "Challenge complete" else "Five questions to warm up",
                    done = todayQuizzes > 0,
                    tint = Sky,
                    onClick = onQuiz,
                )
            }
        }
    }
}

@Composable
private fun PlanRow(
    sticker: StickerKind,
    title: String,
    subtitle: String,
    done: Boolean,
    tint: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Sticker(
            kind = sticker,
            size = 42.dp,
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = Ink,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        if (done) {
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
            Text("→", style = MaterialTheme.typography.titleMedium, color = Muted)
        }
    }
}

@Composable
private fun PlanDivider() {
    Box(
        modifier = Modifier
            .padding(start = 68.dp, end = 14.dp)
            .height(1.dp)
            .fillMaxWidth()
            .background(Line.copy(alpha = 0.7f)),
    )
}

@Composable
private fun UnitPreviewCard(
    unit: com.lingo.app.data.Unit,
    completed: Int,
    onClick: () -> Unit,
) {
    val palette = paletteFor(unit.accent)
    Surface(
        modifier = Modifier
            .width(252.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = palette.soft,
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Sticker(kind = unit.icon, size = 38.dp)
                Spacer(Modifier.weight(1f))
                Tag(
                    text = unit.level.substringBefore(" ·"),
                    background = Paper.copy(alpha = 0.8f),
                    color = palette.deep,
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = unit.title,
                style = MaterialTheme.typography.headlineSmall,
                color = palette.deep,
                maxLines = 1,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = unit.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = palette.deep.copy(alpha = 0.68f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(16.dp))
            ProgressLine(
                progress = completed / unit.lessons.size.toFloat(),
                color = palette.main,
                track = Paper.copy(alpha = 0.8f),
                height = 6.dp,
            )
            Spacer(Modifier.height(7.dp))
            Text(
                text = "$completed of ${unit.lessons.size} lessons",
                style = MaterialTheme.typography.labelSmall,
                color = palette.deep.copy(alpha = 0.7f),
            )
        }
    }
}
