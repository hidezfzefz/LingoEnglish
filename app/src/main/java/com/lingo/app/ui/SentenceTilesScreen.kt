@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.lingo.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import com.lingo.app.data.LingoContent
import com.lingo.app.data.ProgressStore
import com.lingo.app.data.SentencePuzzle
import com.lingo.app.data.StickerKind

private val WorkshopPurple = Color(0xFF453572)
private val WorkshopInk = Color(0xFF2E2352)

private fun freshSessionIds(): String =
    LingoContent.sentencePuzzles.shuffled().take(5).joinToString("|") { it.id }

@Composable
fun SentenceTilesScreen(
    store: ProgressStore,
    speaker: (String) -> Unit,
    onClose: () -> Unit,
) {
    var sessionIds by rememberSaveable { mutableStateOf(freshSessionIds()) }
    var sessionKey by rememberSaveable { mutableIntStateOf(0) }
    var round by rememberSaveable { mutableIntStateOf(0) }
    var finished by rememberSaveable { mutableStateOf(false) }
    var sessionXp by rememberSaveable { mutableIntStateOf(0) }

    val session = sessionIds
        .split("|")
        .mapNotNull { id -> LingoContent.sentencePuzzles.firstOrNull { it.id == id } }

    if (finished) {
        WordTilesComplete(
            count = session.size,
            earnedXp = sessionXp,
            onClose = onClose,
            onAgain = {
                sessionIds = freshSessionIds()
                sessionKey += 1
                round = 0
                finished = false
                sessionXp = 0
            },
        )
        return
    }

    val puzzle = session.getOrNull(round)
    if (puzzle == null) {
        onClose()
        return
    }

    key("$sessionKey-${puzzle.id}") {
        SentenceRound(
            puzzle = puzzle,
            round = round,
            total = session.size,
            speaker = speaker,
            onClose = onClose,
            onSolved = {
                val gained = store.recordSentenceBuild(puzzle.id)
                sessionXp += gained
                gained
            },
            onNext = {
                if (round >= session.lastIndex) {
                    finished = true
                } else {
                    round += 1
                }
            },
        )
    }
}

@Composable
private fun SentenceRound(
    puzzle: SentencePuzzle,
    round: Int,
    total: Int,
    speaker: (String) -> Unit,
    onClose: () -> Unit,
    onSolved: () -> Int,
    onNext: () -> Unit,
) {
    var placed by remember(puzzle.id) { mutableStateOf<List<Int>>(emptyList()) }
    var checked by remember(puzzle.id) { mutableStateOf(false) }
    var correct by remember(puzzle.id) { mutableStateOf(false) }
    var earned by remember(puzzle.id) { mutableIntStateOf(0) }
    var showHint by remember(puzzle.id) { mutableStateOf(false) }
    val bankOrder = remember(puzzle.id) { puzzle.tokens.indices.shuffled() }
    val builtSentence = placed.joinToString(" ") { puzzle.tokens[it] }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        WordTilesTopBar(
            label = "WORD TILES",
            trailing = "${round + 1} / $total",
            onBack = onClose,
        )
        Column(Modifier.padding(horizontal = 22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Sticker(kind = puzzle.sticker, size = 24.dp, elevated = false)
                Spacer(Modifier.width(7.dp))
                Text(
                    "BUILD A SENTENCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = WorkshopPurple,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "Tap · order · check",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
            Spacer(Modifier.height(8.dp))
            ProgressLine(
                progress = (round + 1f) / total.coerceAtLeast(1),
                color = Purple,
                track = Lavender,
                height = 7.dp,
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp),
        ) {
            Spacer(Modifier.height(18.dp))
            Text(
                puzzle.prompt,
                style = MaterialTheme.typography.headlineMedium.copy(lineHeight = 32.sp),
                color = Ink,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Tap the words in the right order. Tap a word in your sentence to take it back.",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )
            Spacer(Modifier.height(18.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 138.dp),
                shape = RoundedCornerShape(24.dp),
                color = Paper,
                border = BorderStroke(1.dp, Line),
            ) {
                if (placed.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(138.dp)
                            .padding(22.dp),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Sticker(
                                kind = StickerKind.SPROUT,
                                size = 40.dp,
                                elevated = false,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Your sentence will grow here",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Muted.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    FlowRow(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        placed.forEachIndexed { position, tokenIndex ->
                            PlacedWord(
                                text = puzzle.tokens[tokenIndex],
                                onClick = {
                                    if (!correct) {
                                        placed = placed.toMutableList().also { it.removeAt(position) }
                                        checked = false
                                    }
                                },
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (placed.isNotEmpty()) {
                    TextButton(onClick = {
                        if (!correct) {
                            placed = emptyList()
                            checked = false
                        }
                    }) {
                        Text(
                            "Clear all",
                            style = MaterialTheme.typography.labelMedium,
                            color = Muted,
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                if (placed.isNotEmpty()) {
                    SpeakButton(
                        onClick = { speaker(builtSentence) },
                        size = 34.dp,
                        background = Lavender,
                        tint = WorkshopPurple,
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "WORD BANK",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
            Spacer(Modifier.height(9.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                bankOrder.forEach { tokenIndex ->
                    val used = tokenIndex in placed
                    WordTile(
                        text = puzzle.tokens[tokenIndex],
                        used = used,
                        onClick = {
                            if (!used && !correct) {
                                placed = placed + tokenIndex
                                checked = false
                            }
                        },
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            if (!showHint && !checked) {
                Text(
                    "Need a nudge?",
                    style = MaterialTheme.typography.labelMedium,
                    color = WorkshopPurple,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showHint = true }
                        .padding(vertical = 4.dp),
                )
            }
            if (showHint) {
                HintCard(explanation = puzzle.explanation)
            }
            if (checked) {
                Spacer(Modifier.height(12.dp))
                if (correct) {
                    FeedbackBanner(
                        correct = true,
                        explanation = puzzle.explanation,
                    )
                } else {
                    WrongOrderCard()
                }
            }
            Spacer(Modifier.height(18.dp))
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Cream)
                .padding(horizontal = 22.dp, vertical = 12.dp),
        ) {
            when {
                !checked -> {
                    PrimaryButton(
                        text = "Check sentence",
                        onClick = {
                            checked = true
                            correct = builtSentence == puzzle.answer
                            if (correct && earned == 0) {
                                earned = onSolved()
                            }
                        },
                        enabled = placed.size == puzzle.tokens.size,
                        modifier = Modifier.fillMaxWidth(),
                        container = WorkshopPurple,
                    )
                    if (placed.size < puzzle.tokens.size) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Use every word to check your sentence.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                !correct -> {
                    PrimaryButton(
                        text = "Try again",
                        onClick = { checked = false },
                        modifier = Modifier.fillMaxWidth(),
                        container = WorkshopPurple,
                    )
                }
                else -> {
                    if (earned > 0) {
                        Text(
                            "Lovely work — +$earned XP",
                            style = MaterialTheme.typography.labelMedium,
                            color = WorkshopPurple,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                    PrimaryButton(
                        text = if (round + 1 >= total) "See results" else "Next sentence",
                        onClick = onNext,
                        modifier = Modifier.fillMaxWidth(),
                        container = WorkshopPurple,
                        trailingArrow = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlacedWord(
    text: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(WorkshopPurple)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleSmall,
            color = Cream,
        )
    }
}

@Composable
private fun WordTile(
    text: String,
    used: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(13.dp))
            .background(if (used) Sand.copy(alpha = 0.55f) else Paper)
            .border(1.dp, if (used) Color.Transparent else Line, RoundedCornerShape(13.dp))
            .clickable(enabled = !used, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleSmall,
            color = if (used) Muted.copy(alpha = 0.35f) else Ink,
            fontStyle = if (used) FontStyle.Italic else FontStyle.Normal,
        )
    }
}

@Composable
private fun HintCard(explanation: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = LimeSoft,
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Sticker(kind = StickerKind.SPROUT, size = 24.dp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    "A small nudge",
                    style = MaterialTheme.typography.titleSmall,
                    color = Ink,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSoft,
                )
            }
        }
    }
}

@Composable
private fun WrongOrderCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Peach,
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Coral),
                contentAlignment = Alignment.Center,
            ) {
                Text("!", color = Cream, fontSize = 13.sp)
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    "Not quite — read it through.",
                    style = MaterialTheme.typography.titleSmall,
                    color = Ink,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Try saying it aloud. Which word feels out of place?",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSoft,
                )
            }
        }
    }
}

@Composable
private fun WordTilesComplete(
    count: Int,
    earnedXp: Int,
    onClose: () -> Unit,
    onAgain: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Confetti(Modifier.fillMaxSize(), particleCount = 34, seed = count * 11 + 3)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(50.dp))
            Sticker(kind = StickerKind.PUZZLE, size = 118.dp)
            Spacer(Modifier.height(22.dp))
            Text(
                "Words, in their place.",
                style = MaterialTheme.typography.displaySmall,
                color = Ink,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                "You built $count sentences from scratch.",
                style = MaterialTheme.typography.bodyLarge,
                color = Muted,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                WordResultTile(
                    value = "$count",
                    label = "SENTENCES",
                    background = Lavender,
                    modifier = Modifier.weight(1f),
                )
                WordResultTile(
                    value = "+$earnedXp",
                    label = "XP EARNED",
                    background = LimeSoft,
                    modifier = Modifier.weight(1f),
                )
                WordResultTile(
                    value = "1",
                    label = "SET DONE",
                    background = Seafoam,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(22.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = Paper,
                border = BorderStroke(1.dp, Line),
            ) {
                Row(Modifier.padding(16.dp)) {
                    Sticker(kind = StickerKind.SPEAK, size = 24.dp, elevated = false)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "Now say it out loud.",
                            style = MaterialTheme.typography.titleSmall,
                            color = Ink,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            if (earnedXp > 0) {
                                "New sentences earn XP. Every replay earns something better: fluency."
                            } else {
                                "You have met these sentences before. Repetition is how they become yours."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSoft,
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                text = "Back to practice",
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
                container = WorkshopPurple,
                trailingArrow = true,
            )
            Spacer(Modifier.height(10.dp))
            SecondaryButton(
                text = "Build another set",
                onClick = onAgain,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun WordResultTile(
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
            Text(value, style = MaterialTheme.typography.headlineSmall, color = WorkshopInk)
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = WorkshopInk.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun WordTilesTopBar(
    label: String,
    trailing: String,
    onBack: () -> Unit,
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
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Text("←", fontSize = 18.sp, color = Ink)
        }
        Spacer(Modifier.width(14.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = WorkshopPurple,
        )
        Spacer(Modifier.weight(1f))
        Text(
            trailing,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
            color = Muted,
        )
    }
}
