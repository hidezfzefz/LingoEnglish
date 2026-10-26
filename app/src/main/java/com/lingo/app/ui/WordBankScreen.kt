package com.lingo.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingo.app.data.LingoContent
import com.lingo.app.data.ProgressStore
import com.lingo.app.data.StickerKind
import com.lingo.app.data.Word

private enum class WordFilter(val label: String) {
    ALL("All words"),
    SAVED("Saved"),
    MASTERED("Mastered"),
}

@Composable
fun WordBankScreen(
    store: ProgressStore,
    speaker: (String) -> Unit,
    onClose: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var filterName by rememberSaveable { mutableStateOf(WordFilter.ALL.name) }
    var expandedId by rememberSaveable { mutableStateOf<String?>(null) }
    val filter = runCatching { WordFilter.valueOf(filterName) }.getOrDefault(WordFilter.ALL)

    val filtered = LingoContent.words.filter { word ->
        val matchesFilter = when (filter) {
            WordFilter.ALL -> true
            WordFilter.SAVED -> word.id in store.savedWords
            WordFilter.MASTERED -> word.id in store.masteredWords
        }
        val matchesQuery = query.isBlank() ||
            word.word.contains(query.trim(), ignoreCase = true) ||
            word.definition.contains(query.trim(), ignoreCase = true)
        matchesFilter && matchesQuery
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(10.dp))
            BackHeader(
                onBack = onClose,
                title = "Word bank",
                trailing = {
                    Tag(
                        text = "${store.masteredWords.size} learned",
                        background = Mint,
                        color = Forest,
                    )
                },
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Words worth keeping.",
                style = MaterialTheme.typography.displaySmall,
                color = Ink,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Tap a word to see it in context, hear it, or save it.",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )
            Spacer(Modifier.height(16.dp))
            SearchField(
                query = query,
                onQueryChange = { query = it },
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WordFilter.entries.forEach { item ->
                    val count = when (item) {
                        WordFilter.ALL -> LingoContent.words.size
                        WordFilter.SAVED -> store.savedWords.size
                        WordFilter.MASTERED -> store.masteredWords.size
                    }
                    FilterPill(
                        label = item.label,
                        count = count,
                        active = filter == item,
                        onClick = { filterName = item.name },
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = "${filtered.size} word${if (filtered.size == 1) "" else "s"}",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
            Spacer(Modifier.height(8.dp))
        }

        if (filtered.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Sticker(kind = StickerKind.SEARCH, size = 58.dp)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "No words here yet",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Ink,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when (filter) {
                        WordFilter.SAVED -> "Tap the star on any word to keep it here."
                        WordFilter.MASTERED -> "Mark a word as learned from a flashcard to see it here."
                        WordFilter.ALL -> "Try a different search."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(filtered, key = { it.id }) { word ->
                    WordBankRow(
                        word = word,
                        expanded = expandedId == word.id,
                        saved = word.id in store.savedWords,
                        mastered = word.id in store.masteredWords,
                        onToggleExpand = {
                            expandedId = if (expandedId == word.id) null else word.id
                        },
                        onSpeak = { speaker("${word.word}. ${word.definition}. ${word.example}") },
                        onToggleSave = { store.toggleSaved(word.id) },
                        onLearn = { store.learnWord(word.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Paper,
        border = BorderStroke(1.dp, Line),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Sticker(kind = StickerKind.SEARCH, size = 24.dp, elevated = false)
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search words or meanings",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Ink),
                    cursorBrush = SolidColor(Emerald),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (query.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onQueryChange("") },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Sand),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("×", fontSize = 13.sp, color = Muted)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    count: Int,
    active: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (active) Forest else Paper)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (active) Cream else InkSoft,
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = if (active) Lime else Muted,
        )
    }
}

@Composable
private fun WordBankRow(
    word: Word,
    expanded: Boolean,
    saved: Boolean,
    mastered: Boolean,
    onToggleExpand: () -> Unit,
    onSpeak: () -> Unit,
    onToggleSave: () -> Unit,
    onLearn: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleExpand),
        shape = RoundedCornerShape(20.dp),
        color = if (expanded) Paper else Paper.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, if (expanded) MintDeep else Line),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = word.word,
                            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 21.sp),
                            color = Ink,
                        )
                        if (mastered) {
                            Spacer(Modifier.width(7.dp))
                            Tag(text = "LEARNED", background = Mint, color = Emerald)
                        }
                    }
                    Text(
                        text = "${word.phonetic} · ${word.partOfSpeech}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                }
                SpeakButton(
                    onClick = onSpeak,
                    size = 36.dp,
                    background = Cream,
                )
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onToggleSave),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = if (saved) "Remove saved word" else "Save word",
                        tint = if (saved) Gold else Line,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Spacer(Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Cream),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (expanded) "−" else "+",
                        fontSize = 14.sp,
                        color = Muted,
                    )
                }
            }
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Line.copy(alpha = 0.7f)),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = word.definition,
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSoft,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "“${word.example}”",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSoft,
                        fontStyle = FontStyle.Italic,
                    )
                    Spacer(Modifier.height(14.dp))
                    if (mastered) {
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Mint)
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("✓", color = Emerald, fontSize = 12.sp)
                            Spacer(Modifier.width(5.dp))
                            Text(
                                "In your mastered words",
                                style = MaterialTheme.typography.labelSmall,
                                color = Forest,
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Forest)
                                .clickable(onClick = onLearn)
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "I know this word",
                                style = MaterialTheme.typography.labelMedium,
                                color = Cream,
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "+2 XP",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = Lime,
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                }
            }
        }
    }
}
