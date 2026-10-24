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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingo.app.data.LingoContent
import com.lingo.app.data.PracticeMode
import com.lingo.app.data.ProgressStore
import com.lingo.app.data.StickerKind
import com.lingo.app.data.Topic
import com.lingo.app.data.ExerciseKind
import com.lingo.app.data.Word

@Composable
fun PracticeScreen(
    store: ProgressStore,
    speaker: (String) -> Unit,
    onOpenQuiz: (PracticeMode) -> Unit,
    onOpenFlashcards: () -> Unit,
    onOpenWordBank: () -> Unit,
    onOpenRoleplay: () -> Unit,
    onOpenSentenceTiles: () -> Unit,
) {
    val featuredWords = remember { LingoContent.words.shuffled().take(6) }

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
                        "PRACTICE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Make it stick.",
                    style = MaterialTheme.typography.displaySmall,
                    color = Ink,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "A few minutes of active recall beats an hour of re-reading.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Muted,
                )
            }
        }

        item {
            DailyChallengeCard(
                streak = store.streak,
                todayDone = store.todayQuizzes > 0,
                onClick = { onOpenQuiz(PracticeMode.DAILY) },
            )
        }

        item {
            Column {
                SectionHeader(title = "Learning lab")
                Spacer(Modifier.height(3.dp))
                Text(
                    "Two more ways to make English stick.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
                Spacer(Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = Paper,
                    border = BorderStroke(1.dp, Line),
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Sticker(kind = StickerKind.LAB, size = 38.dp)
                            Spacer(Modifier.width(11.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Learn by doing.",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Ink,
                                )
                                Text(
                                    "Choose a reply. Rebuild a sentence. Notice what fits.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Muted,
                                )
                            }
                            Tag("NEW", background = LimeSoft, color = Forest)
                        }
                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            LabCard(
                                sticker = StickerKind.THEATER,
                                title = "Real Talk",
                                subtitle = if (store.completedRoleplays.isEmpty()) {
                                    "Step into a scene"
                                } else {
                                    "${store.completedRoleplays.size} of ${LingoContent.roleplays.size} scenes"
                                },
                                background = Seafoam,
                                accent = DeepTeal,
                                modifier = Modifier.weight(1f),
                                onClick = onOpenRoleplay,
                            )
                            LabCard(
                                sticker = StickerKind.PUZZLE,
                                title = "Word Tiles",
                                subtitle = if (store.solvedSentences.isEmpty()) {
                                    "Build a sentence"
                                } else {
                                    "${store.solvedSentences.size} of ${LingoContent.sentencePuzzles.size} built"
                                },
                                background = Lavender,
                                accent = Color(0xFF453572),
                                modifier = Modifier.weight(1f),
                                onClick = onOpenSentenceTiles,
                            )
                        }
                    }
                }
            }
        }

        item {
            Column {
                SectionHeader(title = "Choose a workout")
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ModeCard(
                        sticker = StickerKind.CARDS,
                        title = "Flashcards",
                        subtitle = if (store.masteredWords.isEmpty()) {
                            "Flip, reveal, remember"
                        } else {
                            "${store.masteredWords.size} words mastered"
                        },
                        background = Mint,
                        accent = Forest,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenFlashcards,
                    )
                    ModeCard(
                        sticker = StickerKind.HEADPHONES,
                        title = "Listening",
                        subtitle = "Train your ear",
                        background = Sky,
                        accent = Color(0xFF234A6E),
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenQuiz(PracticeMode.LISTENING) },
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ModeCard(
                        sticker = StickerKind.PENCIL,
                        title = "Grammar",
                        subtitle = "Smooth out the tricky bits",
                        background = Peach,
                        accent = Color(0xFF8C3B27),
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenQuiz(PracticeMode.GRAMMAR) },
                    )
                    ModeCard(
                        sticker = StickerKind.BULB,
                        title = "Word bank",
                        subtitle = if (store.savedWords.isEmpty()) {
                            "${LingoContent.words.size} words to explore"
                        } else {
                            "${store.savedWords.size} saved for later"
                        },
                        background = Lavender,
                        accent = Color(0xFF453572),
                        modifier = Modifier.weight(1f),
                        onClick = onOpenWordBank,
                    )
                }
            }
        }

        item {
            Column {
                SectionHeader(
                    title = "Pick up a word",
                    action = "Open word bank",
                    onAction = onOpenWordBank,
                )
                Spacer(Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    itemsIndexed(featuredWords) { index, word ->
                        MiniWordCard(
                            word = word,
                            background = listOf(Mint, Sky, LimeSoft, Lavender, Peach, Sand)[index % 6],
                            saved = word.id in store.savedWords,
                            onSpeak = { speaker("${word.word}. ${word.definition}") },
                            onToggleSave = { store.toggleSaved(word.id) },
                            onClick = onOpenWordBank,
                        )
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Cream,
                border = BorderStroke(1.dp, Line),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Sticker(kind = StickerKind.SPROUT, size = 24.dp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Practice out loud, not just in your head.",
                            style = MaterialTheme.typography.titleSmall,
                            color = Ink,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "Say every answer before you tap it. Your mouth needs the reps, too.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatPill(StickerKind.TARGET, "${store.accuracy}% accuracy")
                StatPill(
                    StickerKind.FLAME,
                    if (store.streak > 0) "${store.streak}-day streak" else "New streak",
                )
                StatPill(StickerKind.BULB, "${store.masteredWords.size} words")
            }
        }
    }
}

@Composable
private fun DailyChallengeCard(
    streak: Int,
    todayDone: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(Brush.linearGradient(listOf(Coral, Color(0xFFD75B45)))),
    ) {
        DecorativeDots(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(180.dp),
            color = Cream.copy(alpha = 0.14f),
            count = 7,
        )
        Column(Modifier.padding(22.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Tag(
                    text = if (todayDone) "DONE FOR TODAY" else "DAILY CHALLENGE",
                    background = Cream.copy(alpha = 0.18f),
                    color = Cream,
                )
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Sticker(
                        kind = StickerKind.FLAME,
                        size = 21.dp,
                        elevated = false,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "$streak",
                        style = MaterialTheme.typography.labelMedium,
                        color = Cream,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Five questions.\nA sharper you.",
                style = MaterialTheme.typography.headlineLarge.copy(lineHeight = 34.sp),
                color = Cream,
            )
            Spacer(Modifier.height(7.dp))
            Text(
                text = "A fresh mix of grammar, vocabulary, and listening.",
                style = MaterialTheme.typography.bodyMedium,
                color = Cream.copy(alpha = 0.75f),
            )
            Spacer(Modifier.height(18.dp))
            PrimaryButton(
                text = if (todayDone) "Do it again" else "Start challenge",
                onClick = onClick,
                container = Paper,
                contentColor = Color(0xFF8C3B27),
                trailingArrow = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "5 questions · about 3 min · up to 30 XP",
                style = MaterialTheme.typography.labelSmall,
                color = Cream.copy(alpha = 0.65f),
            )
        }
    }
}

@Composable
private fun LabCard(
    sticker: StickerKind,
    title: String,
    subtitle: String,
    background: Color,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .height(128.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Sticker(kind = sticker, size = 32.dp, elevated = false)
            Spacer(Modifier.weight(1f))
            Text("↗", fontSize = 15.sp, color = accent.copy(alpha = 0.55f))
        }
        Spacer(Modifier.weight(1f))
        Text(title, style = MaterialTheme.typography.titleMedium, color = accent)
        Spacer(Modifier.height(2.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = accent.copy(alpha = 0.7f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ModeCard(
    sticker: StickerKind,
    title: String,
    subtitle: String,
    background: Color,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(146.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(background)
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Sticker(kind = sticker, size = 37.dp, elevated = false)
                Spacer(Modifier.weight(1f))
                Text("↗", fontSize = 16.sp, color = accent.copy(alpha = 0.55f))
            }
            Spacer(Modifier.weight(1f))
            Text(title, style = MaterialTheme.typography.titleMedium, color = accent)
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = accent.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MiniWordCard(
    word: Word,
    background: Color,
    saved: Boolean,
    onSpeak: () -> Unit,
    onToggleSave: () -> Unit,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .width(250.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = background,
    ) {
        Column(Modifier.padding(17.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = word.word,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Ink,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                SpeakButton(
                    onClick = onSpeak,
                    size = 34.dp,
                    background = Paper.copy(alpha = 0.9f),
                    tint = Forest,
                )
            }
            Text(
                text = "${word.phonetic} · ${word.partOfSpeech}",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = word.definition,
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onToggleSave)
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (saved) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                    contentDescription = if (saved) "Remove saved word" else "Save word",
                    tint = if (saved) Gold else Muted,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (saved) "Saved" else "Save for later",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
            }
        }
    }
}
