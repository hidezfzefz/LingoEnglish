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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingo.app.data.Lesson
import com.lingo.app.data.LingoContent
import com.lingo.app.data.ProgressStore

@Composable
fun LearnScreen(
    store: ProgressStore,
    onOpenLesson: (Lesson) -> Unit,
) {
    val completedCount = LingoContent.lessons.count { it.id in store.completedLessons }
    val next = LingoContent.nextLesson(store.completedLessons)
    val totalMinutes = LingoContent.lessons.sumOf { it.minutes }
    val roundedMinutes = ((totalMinutes + 4) / 5) * 5

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
                    Text(
                        "YOUR PATH",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Choose your next step.",
                    style = MaterialTheme.typography.displaySmall,
                    color = Ink,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${LingoContent.units.size} friendly courses, from first hellos to real conversations.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Muted,
                )
            }
        }

        item {
            LevelOverviewCard(store = store, completedCount = completedCount)
        }

        items(LingoContent.units) { unit ->
            UnitSection(
                unit = unit,
                completed = unit.lessons.filter { it.id in store.completedLessons }.map { it.id }.toSet(),
                nextLessonId = next.id,
                onOpenLesson = onOpenLesson,
            )
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "${LingoContent.lessons.size} lessons · about $roundedMinutes minutes · ${LingoContent.lessons.sumOf { it.xp }} XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Sticker(
                        kind = com.lingo.app.data.StickerKind.SPROUT,
                        size = 19.dp,
                        elevated = false,
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "A lesson a day is enough. Truly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelOverviewCard(
    store: ProgressStore,
    completedCount: Int,
) {
    val level = store.level
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = Mint,
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Forest),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "LV",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = Lime.copy(alpha = 0.75f),
                        )
                        Text(
                            text = "${level.level}",
                            style = MaterialTheme.typography.titleLarge,
                            color = Lime,
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(level.title, style = MaterialTheme.typography.titleMedium, color = Forest)
                    Text(
                        "${store.totalXp} XP · $completedCount of ${LingoContent.lessons.size} lessons",
                        style = MaterialTheme.typography.bodySmall,
                        color = Forest.copy(alpha = 0.65f),
                    )
                }
                Tag(text = "${level.xpInLevel}/${level.xpForNext}", background = Paper, color = Forest)
            }
            Spacer(Modifier.height(14.dp))
            ProgressLine(
                progress = level.progress,
                color = Forest,
                track = Paper,
                height = 8.dp,
            )
            Spacer(Modifier.height(7.dp))
            Text(
                text = "${level.xpForNext - level.xpInLevel} XP to level ${level.level + 1}",
                style = MaterialTheme.typography.labelSmall,
                color = Forest.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun UnitSection(
    unit: com.lingo.app.data.Unit,
    completed: Set<String>,
    nextLessonId: String,
    onOpenLesson: (Lesson) -> Unit,
) {
    val palette = paletteFor(unit.accent)
    val completeCount = completed.size
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = Paper,
        border = BorderStroke(1.dp, Line),
    ) {
        Column {
            Row(
                modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Sticker(
                    kind = unit.icon,
                    size = 46.dp,
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = unit.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Ink,
                        )
                    }
                    Text(
                        text = "${unit.level} · ${unit.subtitle}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Tag(
                    text = "$completeCount/${unit.lessons.size}",
                    background = palette.soft,
                    color = palette.deep,
                )
            }
            Spacer(Modifier.height(14.dp))
            ProgressLine(
                progress = completeCount / unit.lessons.size.toFloat(),
                color = palette.main,
                track = palette.soft.copy(alpha = 0.7f),
                height = 6.dp,
                modifier = Modifier.padding(horizontal = 18.dp),
            )
            Spacer(Modifier.height(10.dp))
            unit.lessons.forEachIndexed { index, lesson ->
                if (index > 0) {
                    Box(
                        Modifier
                            .padding(start = 62.dp, end = 18.dp)
                            .height(1.dp)
                            .fillMaxWidth()
                            .background(Line.copy(alpha = 0.7f)),
                    )
                }
                LessonRow(
                    lesson = lesson,
                    index = index + 1,
                    completed = lesson.id in completed,
                    isNext = lesson.id == nextLessonId,
                    palette = palette,
                    onClick = { onOpenLesson(lesson) },
                )
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun LessonRow(
    lesson: Lesson,
    index: Int,
    completed: Boolean,
    isNext: Boolean,
    palette: AccentPalette,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(if (completed) Emerald else palette.soft),
            contentAlignment = Alignment.Center,
        ) {
            if (completed) {
                Text("✓", color = Cream, fontSize = 13.sp)
            } else {
                Text(
                    text = "$index",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.deep,
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (isNext && !completed) {
                    Spacer(Modifier.width(7.dp))
                    Tag(
                        text = "UP NEXT",
                        background = palette.soft,
                        color = palette.deep,
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${lesson.subtitle} · ${lesson.minutes} min",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(if (isNext && !completed) palette.main else Cream),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (completed) "↻" else "▶",
                color = if (isNext && !completed) Cream else Muted,
                fontSize = if (completed) 14.sp else 10.sp,
            )
        }
    }
}
