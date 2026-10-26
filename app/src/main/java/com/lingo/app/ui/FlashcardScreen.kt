package com.lingo.app.ui

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingo.app.data.LingoContent
import com.lingo.app.data.ProgressStore
import com.lingo.app.data.StickerKind
import com.lingo.app.data.Word
import kotlin.random.Random
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star

@Composable
fun FlashcardScreen(
    store: ProgressStore,
    speaker: (String) -> Unit,
    onClose: () -> Unit,
) {
    var round by rememberSaveable { mutableIntStateOf(0) }
    val deck = remember(round) { LingoContent.words.shuffled(Random(7 + round)) }
    var index by rememberSaveable(round) { mutableIntStateOf(0) }
    var revealed by rememberSaveable(round) { mutableStateOf(false) }
    var complete by rememberSaveable(round) { mutableStateOf(false) }
    var sessionLearned by rememberSaveable(round) { mutableIntStateOf(0) }

    if (complete) {
        FlashcardComplete(
            total = deck.size,
            sessionLearned = sessionLearned,
            totalMastered = store.masteredWords.size,
            onClose = onClose,
            onAgain = {
                round += 1
                index = 0
                revealed = false
                complete = false
                sessionLearned = 0
            },
        )
        return
    }

    val word = deck[index.coerceIn(0, deck.lastIndex)]
    val mastered = word.id in store.masteredWords

    val advance = {
        if (index >= deck.lastIndex) {
            complete = true
        } else {
            index += 1
            revealed = false
        }
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
                Text("Flashcards", style = MaterialTheme.typography.titleMedium, color = Ink)
                Text(
                    "Card ${index + 1} of ${deck.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable { store.toggleSaved(word.id) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = if (word.id in store.savedWords) "Remove saved word" else "Save word",
                    tint = if (word.id in store.savedWords) Gold else Line,
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        ProgressLine(
            progress = (index + 1f) / deck.size,
            color = Coral,
            track = Sand,
            height = 6.dp,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            FlipCard(
                word = word,
                revealed = revealed,
                saved = word.id in store.savedWords,
                mastered = mastered,
                onFlip = { revealed = !revealed },
                onSpeak = { speaker("${word.word}. ${word.definition}. ${word.example}") },
                onToggleSave = { store.toggleSaved(word.id) },
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 12.dp),
        ) {
            if (!revealed) {
                PrimaryButton(
                    text = "Reveal meaning",
                    onClick = { revealed = true },
                    modifier = Modifier.fillMaxWidth(),
                    container = Coral,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Say the word out loud before you flip the card.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryButton(
                        text = "Still learning",
                        onClick = advance,
                        modifier = Modifier.weight(1f),
                    )
                    PrimaryButton(
                        text = "I know it",
                        onClick = {
                            val earned = store.learnWord(word.id)
                            if (earned > 0) sessionLearned += 1
                            advance()
                        },
                        modifier = Modifier.weight(1f),
                        container = Coral,
                    )
                }
                Spacer(Modifier.height(7.dp))
                Text(
                    text = if (mastered) {
                        "Already in your mastered words · reviewing is still valuable."
                    } else {
                        "“I know it” adds the word to your word bank and earns +2 XP."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun FlipCard(
    word: Word,
    revealed: Boolean,
    saved: Boolean,
    mastered: Boolean,
    onFlip: () -> Unit,
    onSpeak: () -> Unit,
    onToggleSave: () -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (revealed) 180f else 0f,
        animationSpec = tween(500),
        label = "cardFlip",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(390.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
            }
            .clickable(onClick = onFlip),
    ) {
        if (rotation <= 90f) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(32.dp),
                color = Paper,
                border = BorderStroke(1.dp, Line),
            ) {
                Box {
                    DecorativeDots(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(170.dp),
                        color = Coral.copy(alpha = 0.09f),
                        count = 8,
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Tag(
                            text = if (mastered) "MASTERED" else "TAP TO FLIP",
                            background = if (mastered) Mint else Cream,
                            color = if (mastered) Emerald else Muted,
                        )
                        Spacer(Modifier.height(26.dp))
                        Text(
                            text = word.word,
                            style = MaterialTheme.typography.displayMedium.copy(fontSize = 44.sp, lineHeight = 50.sp),
                            color = Ink,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = word.phonetic,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Muted,
                        )
                        Text(
                            text = word.partOfSpeech,
                            style = MaterialTheme.typography.labelMedium,
                            color = Coral,
                        )
                        Spacer(Modifier.height(32.dp))
                        SpeakButton(
                            onClick = onSpeak,
                            size = 48.dp,
                            background = Peach,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Hear the pronunciation",
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted,
                        )
                    }
                }
            }
        } else {
            Box(Modifier.graphicsLayer { rotationY = 180f }) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(32.dp),
                    color = Forest,
                ) {
                    Box {
                        DecorativeDots(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(200.dp),
                            color = Lime.copy(alpha = 0.12f),
                            count = 7,
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(26.dp),
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                "MEANING",
                                style = MaterialTheme.typography.labelSmall,
                                color = Lime.copy(alpha = 0.7f),
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = word.definition,
                                style = MaterialTheme.typography.headlineSmall.copy(lineHeight = 30.sp),
                                color = Cream,
                            )
                            Spacer(Modifier.height(20.dp))
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Cream.copy(alpha = 0.14f)),
                            )
                            Spacer(Modifier.height(20.dp))
                            Text(
                                text = "IN A SENTENCE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Lime.copy(alpha = 0.7f),
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "“${word.example}”",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Cream.copy(alpha = 0.88f),
                            )
                            Spacer(Modifier.height(22.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SpeakButton(
                                    onClick = onSpeak,
                                    size = 40.dp,
                                    background = Lime,
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "Tap to flip back",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Cream.copy(alpha = 0.5f),
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(22.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onToggleSave),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = if (saved) "Remove saved word" else "Save word",
                                tint = if (saved) Gold else Cream.copy(alpha = 0.35f),
                                modifier = Modifier.size(26.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlashcardComplete(
    total: Int,
    sessionLearned: Int,
    totalMastered: Int,
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
        Confetti(Modifier.fillMaxSize(), particleCount = 38, seed = 23)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(64.dp))
            Sticker(kind = StickerKind.CARDS, size = 118.dp)
            Spacer(Modifier.height(22.dp))
            Text(
                text = "Deck complete!",
                style = MaterialTheme.typography.displaySmall,
                color = Ink,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "You reviewed all $total words. That's real repetition.",
                style = MaterialTheme.typography.bodyLarge,
                color = Muted,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(26.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    color = Mint,
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("+$sessionLearned", style = MaterialTheme.typography.headlineSmall, color = Forest)
                        Text(
                            "NEW WORDS",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = Forest.copy(alpha = 0.6f),
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
                        Text("$totalMastered", style = MaterialTheme.typography.headlineSmall, color = Ink)
                        Text(
                            "TOTAL MASTERED",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = InkSoft.copy(alpha = 0.6f),
                        )
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
            PrimaryButton(
                text = "Back to practice",
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
                trailingArrow = true,
            )
            Spacer(Modifier.height(10.dp))
            SecondaryButton(
                text = "Shuffle and review again",
                onClick = onAgain,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(28.dp))
        }
    }
}
