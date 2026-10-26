package com.lingo.app.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.lingo.app.data.ProgressStore
import com.lingo.app.data.RoleplayScenario
import com.lingo.app.data.RoleplayStep
import com.lingo.app.data.StickerKind

@Composable
fun RoleplayScreen(
    store: ProgressStore,
    speaker: (String) -> Unit,
    onClose: () -> Unit,
) {
    var activeId by rememberSaveable { mutableStateOf<String?>(null) }
    val active = activeId?.let { id -> LingoContent.roleplays.firstOrNull { it.id == id } }

    BackHandler(enabled = active != null) {
        activeId = null
    }

    if (active == null) {
        RoleplayHub(
            store = store,
            onClose = onClose,
            onOpenScene = { activeId = it.id },
        )
    } else {
        key(active.id) {
            RoleplayScene(
                scenario = active,
                store = store,
                speaker = speaker,
                onBack = { activeId = null },
            )
        }
    }
}

@Composable
private fun RoleplayHub(
    store: ProgressStore,
    onClose: () -> Unit,
    onOpenScene: (RoleplayScenario) -> Unit,
) {
    val completed = LingoContent.roleplays.count { it.id in store.completedRoleplays }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        RoleplayTopBar(
            label = "REAL TALK",
            trailing = "$completed / ${LingoContent.roleplays.size}",
            onBack = onClose,
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Column {
                    Text(
                        text = "English lives\nin the moment.",
                        style = MaterialTheme.typography.displaySmall,
                        color = Ink,
                    )
                    Spacer(Modifier.height(7.dp))
                    Text(
                        text = "Step into a tiny scene. Choose what you would say, then see how the conversation responds.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Muted,
                    )
                }
            }
            item {
                SceneProgressCard(completed = completed, total = LingoContent.roleplays.size)
            }
            itemsIndexed(LingoContent.roleplays) { index, scenario ->
                ScenarioCard(
                    scenario = scenario,
                    index = index,
                    completed = scenario.id in store.completedRoleplays,
                    onClick = { onOpenScene(scenario) },
                )
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = LimeSoft,
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                        Sticker(kind = StickerKind.SPROUT, size = 24.dp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "There is no perfect reply.",
                                style = MaterialTheme.typography.titleSmall,
                                color = Ink,
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                "Choose the tone that fits the moment. Wrong turns are just clues — try the scene again and hear the difference.",
                                style = MaterialTheme.typography.bodySmall,
                                color = InkSoft,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SceneProgressCard(completed: Int, total: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Seafoam,
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Sticker(kind = StickerKind.THEATER, size = 28.dp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Your scene shelf", style = MaterialTheme.typography.titleMedium, color = DeepTeal)
                    Text(
                        if (completed == total) "Every scene explored. Bravo!" else "$completed of $total scenes explored",
                        style = MaterialTheme.typography.bodySmall,
                        color = DeepTeal.copy(alpha = 0.7f),
                    )
                }
                Tag(
                    text = if (completed == total) "COMPLETE" else "PLAY",
                    background = Paper.copy(alpha = 0.8f),
                    color = DeepTeal,
                )
            }
            Spacer(Modifier.height(12.dp))
            ProgressLine(
                progress = if (total == 0) 0f else completed / total.toFloat(),
                color = Teal,
                track = Paper.copy(alpha = 0.85f),
                height = 6.dp,
            )
        }
    }
}

@Composable
private fun ScenarioCard(
    scenario: RoleplayScenario,
    index: Int,
    completed: Boolean,
    onClick: () -> Unit,
) {
    val background = listOf(Seafoam, Peach, Sky)[index % 3]
    val accent = listOf(DeepTeal, CoralDeep, Color(0xFF234A6E))[index % 3]

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        color = background,
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Sticker(kind = scenario.sticker, size = 48.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        scenario.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = accent,
                    )
                    Text(
                        scenario.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = accent.copy(alpha = 0.7f),
                    )
                }
                if (completed) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Emerald),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("✓", color = Cream, fontSize = 14.sp)
                    }
                } else {
                    Text("↗", color = accent.copy(alpha = 0.55f), fontSize = 18.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                scenario.setting,
                style = MaterialTheme.typography.bodySmall,
                color = accent.copy(alpha = 0.65f),
                fontStyle = FontStyle.Italic,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Tag(text = scenario.level.substringBefore(" ·"), background = Paper.copy(alpha = 0.8f), color = accent)
                Tag(text = "${scenario.steps.size} decisions", background = Paper.copy(alpha = 0.8f), color = accent)
                Tag(text = "+${scenario.xp} XP", background = Paper.copy(alpha = 0.8f), color = accent)
            }
        }
    }
}

@Composable
private fun RoleplayScene(
    scenario: RoleplayScenario,
    store: ProgressStore,
    speaker: (String) -> Unit,
    onBack: () -> Unit,
) {
    var stepIndex by rememberSaveable(scenario.id) { mutableIntStateOf(0) }
    var selected by rememberSaveable(scenario.id) { mutableStateOf<Int?>(null) }
    var checked by rememberSaveable(scenario.id) { mutableStateOf(false) }
    var lastReply by rememberSaveable(scenario.id) { mutableStateOf<String?>(null) }
    var complete by rememberSaveable(scenario.id) { mutableStateOf(false) }
    var awardedXp by rememberSaveable(scenario.id) { mutableIntStateOf(0) }

    if (complete) {
        RoleplayComplete(
            scenario = scenario,
            awardedXp = awardedXp,
            onBackToScenes = onBack,
            onReplay = {
                stepIndex = 0
                selected = null
                checked = false
                lastReply = null
                awardedXp = 0
                complete = false
            },
        )
        return
    }

    val step = scenario.steps[stepIndex.coerceIn(0, scenario.steps.lastIndex)]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        RoleplayTopBar(
            label = "REAL TALK",
            trailing = "${stepIndex + 1} / ${scenario.steps.size}",
            onBack = onBack,
        )
        Column(Modifier.padding(horizontal = 22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Sticker(kind = scenario.sticker, size = 24.dp, elevated = false)
                Spacer(Modifier.width(7.dp))
                Text(
                    scenario.title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Teal,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    scenario.setting,
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(8.dp))
            ProgressLine(
                progress = (stepIndex + 1f) / scenario.steps.size,
                color = Teal,
                track = Sand,
                height = 7.dp,
            )
        }
        Spacer(Modifier.height(10.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(bottom = 18.dp),
        ) {
            Spacer(Modifier.height(10.dp))
            if (lastReply != null) {
                UserBubble(lastReply!!)
                Spacer(Modifier.height(14.dp))
            }
            NpcBubble(step = step, speaker = speaker)
            Spacer(Modifier.height(20.dp))
            Text(
                "YOUR LINE",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                step.prompt,
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
            )
            Spacer(Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                step.options.forEachIndexed { index, option ->
                    val state = when {
                        !checked && selected == index -> OptionState.SELECTED
                        !checked -> OptionState.DEFAULT
                        index == step.correctIndex -> OptionState.CORRECT
                        selected == index -> OptionState.WRONG
                        else -> OptionState.MUTED
                    }
                    ChoiceOption(
                        letter = ('A' + index).toString(),
                        text = option,
                        state = state,
                        onClick = { if (!checked) selected = index },
                    )
                }
            }
            if (checked) {
                Spacer(Modifier.height(16.dp))
                FeedbackBanner(
                    correct = selected == step.correctIndex,
                    explanation = step.explanation,
                )
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
                    text = "Say this",
                    onClick = { checked = true },
                    enabled = selected != null,
                    modifier = Modifier.fillMaxWidth(),
                    container = DeepTeal,
                )
            } else if (selected != step.correctIndex) {
                PrimaryButton(
                    text = "Try another reply",
                    onClick = {
                        checked = false
                        selected = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    container = DeepTeal,
                )
            } else {
                PrimaryButton(
                    text = if (stepIndex == scenario.steps.lastIndex) "Finish scene" else "Continue",
                    onClick = {
                        if (stepIndex == scenario.steps.lastIndex) {
                            awardedXp = store.completeRoleplay(scenario.id, scenario.xp)
                            complete = true
                        } else {
                            lastReply = step.options[step.correctIndex]
                            stepIndex += 1
                            selected = null
                            checked = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    container = DeepTeal,
                    trailingArrow = true,
                )
            }
        }
    }
}

@Composable
private fun NpcBubble(
    step: RoleplayStep,
    speaker: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
        Sticker(kind = step.avatar, size = 38.dp, elevated = false)
        Spacer(Modifier.width(10.dp))
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp),
            color = Paper,
            border = BorderStroke(1.dp, Line),
        ) {
            Column(Modifier.padding(start = 15.dp, end = 10.dp, top = 12.dp, bottom = 14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        step.speaker,
                        style = MaterialTheme.typography.labelMedium,
                        color = Teal,
                    )
                    Spacer(Modifier.weight(1f))
                    SpeakButton(
                        onClick = { speaker(step.line) },
                        size = 30.dp,
                        background = Seafoam,
                        tint = DeepTeal,
                    )
                }
                Spacer(Modifier.height(5.dp))
                Text(
                    step.line,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Ink,
                    modifier = Modifier.padding(end = 5.dp),
                )
            }
        }
    }
}

@Composable
private fun UserBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.82f),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 6.dp, bottomEnd = 20.dp, bottomStart = 20.dp),
            color = Forest,
        ) {
            Column(Modifier.padding(horizontal = 15.dp, vertical = 12.dp)) {
                Text(
                    "YOU",
                    style = MaterialTheme.typography.labelSmall,
                    color = Lime.copy(alpha = 0.8f),
                )
                Spacer(Modifier.height(4.dp))
                Text(text, style = MaterialTheme.typography.bodyMedium, color = Cream)
            }
        }
    }
}

@Composable
private fun RoleplayComplete(
    scenario: RoleplayScenario,
    awardedXp: Int,
    onBackToScenes: () -> Unit,
    onReplay: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Confetti(Modifier.fillMaxSize(), particleCount = 36, seed = scenario.steps.size * 7)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))
            Sticker(kind = scenario.sticker, size = 118.dp)
            Spacer(Modifier.height(22.dp))
            Text(
                "A scene well played.",
                style = MaterialTheme.typography.displaySmall,
                color = Ink,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                scenario.title,
                style = MaterialTheme.typography.bodyLarge,
                color = Muted,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RoleplayResultTile(
                    value = "+$awardedXp",
                    label = "XP EARNED",
                    background = Seafoam,
                    modifier = Modifier.weight(1f),
                )
                RoleplayResultTile(
                    value = "${scenario.steps.size}",
                    label = "DECISIONS",
                    background = LimeSoft,
                    modifier = Modifier.weight(1f),
                )
                RoleplayResultTile(
                    value = "1",
                    label = "SCENE DONE",
                    background = Peach,
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
                    Sticker(kind = StickerKind.CHAT, size = 24.dp, elevated = false)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "One scene, one tone.",
                            style = MaterialTheme.typography.titleSmall,
                            color = Ink,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "Replay this scene and try a different path. The same idea can sound warm, direct, or playful.",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSoft,
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                text = "Back to Real Talk",
                onClick = onBackToScenes,
                modifier = Modifier.fillMaxWidth(),
                trailingArrow = true,
                container = DeepTeal,
            )
            Spacer(Modifier.height(10.dp))
            SecondaryButton(
                text = "Play this scene again",
                onClick = onReplay,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun RoleplayResultTile(
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
            Text(value, style = MaterialTheme.typography.headlineSmall, color = Ink)
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = InkSoft.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun RoleplayTopBar(
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
                .size(48.dp)
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
            color = DeepTeal,
        )
        Spacer(Modifier.weight(1f))
        Text(
            trailing,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
            color = Muted,
        )
    }
}
