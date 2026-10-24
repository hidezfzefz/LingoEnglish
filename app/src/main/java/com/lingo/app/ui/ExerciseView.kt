package com.lingo.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingo.app.data.Exercise
import com.lingo.app.data.StickerKind
import com.lingo.app.data.ExerciseKind

@Composable
fun ExerciseView(
    exercise: Exercise,
    selectedIndex: Int?,
    checked: Boolean,
    onSelect: (Int) -> Unit,
    onSpeak: (String) -> Unit,
    text: String = "",
    onTextChange: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isCorrect = checked && exercise.isAnswerCorrect(selectedIndex, text)
    Column(modifier = modifier.fillMaxWidth()) {
        Tag(
            text = when (exercise.kind) {
                ExerciseKind.LISTENING -> "LISTEN"
                ExerciseKind.FILL_BLANK -> "FILL THE GAP"
                ExerciseKind.CHOICE -> "CHOOSE"
                ExerciseKind.WRITE -> "CREATE"
            },
            background = when (exercise.kind) {
                ExerciseKind.LISTENING -> Sky
                ExerciseKind.WRITE -> LimeSoft
                else -> Peach
            },
            color = when (exercise.kind) {
                ExerciseKind.LISTENING -> Blue
                ExerciseKind.WRITE -> Forest
                else -> Color(0xFF8C3B27)
            },
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = exercise.instruction,
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = exercise.question,
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 25.sp, lineHeight = 32.sp),
            color = Ink,
        )
        if (exercise.kind == ExerciseKind.LISTENING && exercise.audioText != null) {
            Spacer(Modifier.height(16.dp))
            AudioPrompt(
                audioText = exercise.audioText,
                playing = checked,
                onPlay = { onSpeak(exercise.audioText) },
            )
        }
        if (exercise.isCreative) {
            Spacer(Modifier.height(18.dp))
            CreativeResponse(
                exercise = exercise,
                text = text,
                checked = checked,
                onTextChange = onTextChange,
            )
        } else {
            Spacer(Modifier.height(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                exercise.options.forEachIndexed { index, option ->
                    val state = when {
                        !checked && selectedIndex == index -> OptionState.SELECTED
                        !checked -> OptionState.DEFAULT
                        index == exercise.correctIndex -> OptionState.CORRECT
                        selectedIndex == index -> OptionState.WRONG
                        else -> OptionState.MUTED
                    }
                    ChoiceOption(
                        letter = ('A' + index).toString(),
                        text = option,
                        state = state,
                        onClick = { if (!checked) onSelect(index) },
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = checked,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            if (exercise.isCreative) {
                CreativeFeedback(
                    exercise = exercise,
                    text = text,
                    correct = isCorrect,
                    modifier = Modifier.padding(top = 16.dp),
                )
            } else {
                FeedbackBanner(
                    correct = isCorrect,
                    explanation = exercise.explanation,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun CreativeResponse(
    exercise: Exercise,
    text: String,
    checked: Boolean,
    onTextChange: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = LimeSoft,
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "SUCCESS CRITERIA",
                style = MaterialTheme.typography.labelSmall,
                color = Ink.copy(alpha = 0.65f),
            )
            Spacer(Modifier.height(7.dp))
            exercise.checklist.forEach { item ->
                Row(Modifier.padding(vertical = 2.dp)) {
                    Text("•", color = Forest, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Text(item, style = MaterialTheme.typography.bodySmall, color = InkSoft)
                }
            }
            if (exercise.starters.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "NEED A FIRST LINE?",
                    style = MaterialTheme.typography.labelSmall,
                    color = Ink.copy(alpha = 0.65f),
                )
                Spacer(Modifier.height(5.dp))
                exercise.starters.forEach { opener ->
                    Text(
                        "“$opener”",
                        style = MaterialTheme.typography.bodySmall,
                        color = Forest,
                        fontStyle = FontStyle.Italic,
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(14.dp))
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 150.dp),
        enabled = !checked,
        placeholder = {
            Text(
                "Write your answer here. Try to include a surprising detail.",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )
        },
        shape = RoundedCornerShape(18.dp),
        keyboardOptions = KeyboardOptions.Default,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Emerald,
            unfocusedBorderColor = Line,
            disabledBorderColor = Line,
            focusedContainerColor = Paper,
            unfocusedContainerColor = Paper,
            disabledContainerColor = Paper,
            cursorColor = Forest,
            focusedTextColor = Ink,
            unfocusedTextColor = Ink,
            disabledTextColor = Ink,
        ),
    )
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            "${exercise.wordCount(text)} words",
            style = MaterialTheme.typography.labelSmall,
            color = if (exercise.wordCount(text) >= exercise.minWords) Emerald else Muted,
        )
        Text(
            "Target: ${exercise.minWords}+ words",
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
        )
    }
}

@Composable
private fun CreativeFeedback(
    exercise: Exercise,
    text: String,
    correct: Boolean,
    modifier: Modifier = Modifier,
) {
    val words = exercise.wordCount(text)
    Column(modifier = modifier.fillMaxWidth()) {
        FeedbackBanner(
            correct = correct,
            title = if (correct) "Idea captured" else "A little more detail",
            explanation = if (correct) {
                "You met the task. Now compare your version with the model and notice one phrase you could borrow."
            } else {
                "You have $words words. Add ${(exercise.minWords - words).coerceAtLeast(1)} more to meet the task target, then check again."
            },
        )
        if (exercise.modelAnswer != null) {
            Spacer(Modifier.height(12.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Paper,
                border = androidx.compose.foundation.BorderStroke(1.dp, Line),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "ONE STRONG ANSWER",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        exercise.modelAnswer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "This is not the only right answer. Read yours aloud and keep the part that sounds most like you.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                        fontStyle = FontStyle.Italic,
                    )
                }
            }
        }
    }
}

@Composable
private fun AudioPrompt(
    audioText: String,
    playing: Boolean,
    onPlay: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay),
        shape = RoundedCornerShape(20.dp),
        color = Sky,
        border = null,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Blue, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("▶", color = PureWhite, fontSize = 18.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = if (playing) "Tap to hear it again" else "Tap to listen",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF234A6E),
                )
                Text(
                    text = if (playing) "“$audioText”" else "Play the sentence, then choose an answer.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF416B8E),
                    fontStyle = if (playing) FontStyle.Italic else FontStyle.Normal,
                )
            }
            Sticker(kind = StickerKind.HEADPHONES, size = 28.dp, elevated = false)
        }
    }
}

@Composable
fun FeedbackBanner(
    correct: Boolean,
    explanation: String,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (correct) Color(0xFFE7F4EA) else Peach,
        border = null,
    ) {
        Row(Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(if (correct) Emerald else Coral, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (correct) "✓" else "!",
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = title ?: if (correct) "Exactly right" else "Not quite",
                    style = MaterialTheme.typography.titleSmall,
                    color = Ink,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSoft,
                )
            }
        }
    }
}
