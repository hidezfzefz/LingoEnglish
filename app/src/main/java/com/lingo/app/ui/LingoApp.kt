package com.lingo.app.ui

import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingo.app.BuildConfig
import com.lingo.app.data.CefrLevel
import com.lingo.app.data.CallPhase
import com.lingo.app.data.CallStore
import com.lingo.app.data.LingoContent
import com.lingo.app.data.PracticeMode
import com.lingo.app.data.ProgressStore
import com.lingo.app.data.StickerKind
import com.lingo.app.data.TestKind
import java.util.Locale

enum class Tab(val label: String, val sticker: StickerKind) {
    HOME("Home", StickerKind.HOME),
    LEARN("Learn", StickerKind.BOOK),
    PRACTICE("Practice", StickerKind.BOLT),
    COMMUNITY("Call", StickerKind.HEADPHONES),
    PROGRESS("Progress", StickerKind.CHART),
}

@Composable
fun LingoApp(
    store: ProgressStore,
    recoveryIntent: Intent? = null,
    onRecoveryIntentHandled: () -> Unit = {},
) {
    val context = LocalContext.current
    val callStore = remember { CallStore(context, store) }
    DisposableEffect(callStore) {
        onDispose { callStore.dispose() }
    }
    val voice = rememberSpeaker()
    val speaker: (String) -> Unit = voice.speak
    var tabName by rememberSaveable { mutableStateOf(Tab.HOME.name) }
    val tab = runCatching { Tab.valueOf(tabName) }.getOrDefault(Tab.HOME)
    var lessonId by rememberSaveable { mutableStateOf<String?>(null) }
    var quizModeName by rememberSaveable { mutableStateOf<String?>(null) }
    var showFlashcards by rememberSaveable { mutableStateOf(false) }
    var showWordBank by rememberSaveable { mutableStateOf(false) }
    var showRoleplay by rememberSaveable { mutableStateOf(false) }
    var showSentenceTiles by rememberSaveable { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showLevelPath by rememberSaveable { mutableStateOf(false) }
    var testKindName by rememberSaveable { mutableStateOf<String?>(null) }
    var testTargetName by rememberSaveable { mutableStateOf<String?>(null) }

    val lesson = lessonId?.let { LingoContent.lessonById(it) }
    val quizMode = quizModeName?.let { name ->
        runCatching { PracticeMode.valueOf(name) }.getOrNull()
    }
    val testKind = testKindName?.let { name ->
        runCatching { TestKind.valueOf(name) }.getOrNull()
    }
    val testTarget = CefrLevel.fromKey(testTargetName)

    // The account gate is mandatory in a configured build. While the saved
    // session is still being restored we show a branded loading state instead
    // of flashing Home or the sign-in form.
    val showEntryGate = callStore.isConfigured && (
        !callStore.authReady ||
            !callStore.signedIn ||
            callStore.passwordRecoveryActive
        )

    LaunchedEffect(recoveryIntent) {
        val intent = recoveryIntent ?: return@LaunchedEffect
        if (!callStore.handlePasswordResetLink(intent)) return@LaunchedEffect
        // A recovery link should always land on the account screen, even if the
        // app was last left inside a lesson, test, or another tab.
        tabName = Tab.COMMUNITY.name
        lessonId = null
        quizModeName = null
        testKindName = null
        testTargetName = null
        showLevelPath = false
        showFlashcards = false
        showWordBank = false
        showRoleplay = false
        showSentenceTiles = false
        showSettings = false
        onRecoveryIntentHandled()
    }

    LaunchedEffect(showEntryGate, callStore.authReady) {
        // When a confirmed signed-out learner reaches the gate, leave any stale
        // lesson, test, sheet, or tab behind so a later sign-in starts cleanly.
        if (!showEntryGate || !callStore.authReady) return@LaunchedEffect
        tabName = Tab.HOME.name
        lessonId = null
        quizModeName = null
        testKindName = null
        testTargetName = null
        showLevelPath = false
        showFlashcards = false
        showWordBank = false
        showRoleplay = false
        showSentenceTiles = false
        showSettings = false
    }

    fun startWeekly() {
        if (!store.weeklyAttempted) {
            testKindName = TestKind.WEEKLY.name
            testTargetName = store.cefrLevel.name
        }
    }

    fun startLevelExam(target: CefrLevel) {
        if (store.canAttemptLevel(target)) {
            testKindName = TestKind.LEVEL.name
            testTargetName = target.name
        }
    }

    val hasOverlay = lesson != null || quizMode != null || showFlashcards || showWordBank ||
        showRoleplay || showSentenceTiles || callStore.phase != CallPhase.IDLE || showLevelPath || testKind != null
    BackHandler(enabled = !showEntryGate && (hasOverlay || tab != Tab.HOME)) {
        when {
            testKind != null -> Unit
            callStore.phase != CallPhase.IDLE -> Unit
            showLevelPath -> showLevelPath = false
            lesson != null -> lessonId = null
            quizMode != null -> quizModeName = null
            showFlashcards -> showFlashcards = false
            showWordBank -> showWordBank = false
            showRoleplay -> showRoleplay = false
            showSentenceTiles -> showSentenceTiles = false
            tab != Tab.HOME -> tabName = Tab.HOME.name
        }
    }

    when {
        showEntryGate -> {
            FirstLoginScreen(
                store = store,
                callStore = callStore,
            )
        }

        testKind != null -> {
            TestSessionScreen(
                store = store,
                callStore = callStore,
                kind = testKind,
                target = testTarget,
                onClose = {
                    testKindName = null
                    testTargetName = null
                    showLevelPath = true
                },
            )
        }

        callStore.phase != CallPhase.IDLE -> {
            CallSessionScreen(
                callStore = callStore,
                onExit = {},
            )
        }

        showLevelPath -> {
            LevelPathScreen(
                store = store,
                callStore = callStore,
                onClose = { showLevelPath = false },
                onStartExam = { target -> startLevelExam(target) },
                onStartWeekly = { startWeekly() },
            )
        }

        lesson != null -> {
            val index = LingoContent.lessons.indexOfFirst { it.id == lesson.id }
            val next = LingoContent.lessons.getOrNull(index + 1)
            key(lesson.id) {
                LessonScreen(
                    lesson = lesson,
                    store = store,
                    speaker = speaker,
                    nextLesson = next,
                    onClose = { lessonId = null },
                    onBackToPath = {
                        lessonId = null
                        tabName = Tab.LEARN.name
                    },
                    onNextLesson = { nextLesson -> lessonId = nextLesson.id },
                    onPracticeMore = {
                        lessonId = null
                        tabName = Tab.PRACTICE.name
                    },
                )
            }
        }

        quizMode != null -> {
            key(quizMode.name) {
                QuizScreen(
                    mode = quizMode,
                    store = store,
                    speaker = speaker,
                    onClose = { quizModeName = null },
                )
            }
        }

        showFlashcards -> {
            FlashcardScreen(
                store = store,
                speaker = speaker,
                onClose = { showFlashcards = false },
            )
        }

        showWordBank -> {
            WordBankScreen(
                store = store,
                speaker = speaker,
                onClose = { showWordBank = false },
            )
        }

        showRoleplay -> {
            RoleplayScreen(
                store = store,
                speaker = speaker,
                onClose = { showRoleplay = false },
            )
        }

        showSentenceTiles -> {
            SentenceTilesScreen(
                store = store,
                speaker = speaker,
                onClose = { showSentenceTiles = false },
            )
        }

        else -> {
            Scaffold(
                containerColor = Cream,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    LingoBottomBar(
                        selected = tab,
                        onSelect = { tabName = it.name },
                    )
                },
            ) { innerPadding ->
                AnimatedContent(
                    targetState = tab,
                    transitionSpec = {
                        fadeIn(tween(220)) togetherWith fadeOut(tween(140))
                    },
                    label = "tabSwitch",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) { active ->
                    when (active) {
                        Tab.HOME -> HomeScreen(
                            store = store,
                            speaker = speaker,
                            onOpenLesson = { lessonId = it.id },
                            onOpenLearn = { tabName = Tab.LEARN.name },
                            onOpenPractice = { tabName = Tab.PRACTICE.name },
                            onOpenQuiz = { quizModeName = it.name },
                            onOpenFlashcards = { showFlashcards = true },
                            onOpenWordBank = { showWordBank = true },
                            onOpenSettings = { showSettings = true },
                        )

                        Tab.LEARN -> LearnScreen(
                            store = store,
                            onOpenLesson = { lessonId = it.id },
                        )

                        Tab.PRACTICE -> PracticeScreen(
                            store = store,
                            speaker = speaker,
                            onOpenQuiz = { quizModeName = it.name },
                            onOpenFlashcards = { showFlashcards = true },
                            onOpenWordBank = { showWordBank = true },
                            onOpenRoleplay = { showRoleplay = true },
                            onOpenSentenceTiles = { showSentenceTiles = true },
                        )

                        Tab.COMMUNITY -> CallHubScreen(
                            store = store,
                            callStore = callStore,
                            onOpenLevelPath = { showLevelPath = true },
                        )

                        Tab.PROGRESS -> ProgressScreen(
                            store = store,
                            onOpenLevelPath = { showLevelPath = true },
                        )
                    }
                }
            }
            if (showSettings) {
                SettingsSheet(
                    store = store,
                    onDismiss = { showSettings = false },
                    onReset = {
                        callStore.resetLocalState()
                        showSettings = false
                        tabName = Tab.HOME.name
                    },
                )
            }
        }
    }
}

@Composable
private fun LingoBottomBar(
    selected: Tab,
    onSelect: (Tab) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .navigationBarsPadding(),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = Paper,
            border = BorderStroke(1.dp, Line),
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Tab.entries.forEach { item ->
                    val active = item == selected
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (active) Mint else Color.Transparent)
                            .clickable { onSelect(item) }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Sticker(
                            kind = item.sticker,
                            size = 25.dp,
                            elevated = false,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                letterSpacing = 0.2.sp,
                            ),
                            color = if (active) Forest else Muted,
                        )
                    }
                }
            }
        }
    }
}

class Speaker(
    val speak: (String) -> Unit,
    val stop: () -> Unit,
)

@Composable
private fun rememberSpeaker(): Speaker {
    val context = LocalContext.current
    var ready by remember { mutableStateOf(false) }
    var engine by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            ready = status == TextToSpeech.SUCCESS
        }
        tts.language = Locale.US
        tts.setSpeechRate(0.92f)
        tts.setPitch(1.0f)
        engine = tts
        onDispose {
            tts.stop()
            tts.shutdown()
            engine = null
        }
    }

    return remember {
        Speaker(
            speak = { text ->
                if (ready) {
                    engine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "lingo-speech")
                }
            },
            stop = { engine?.stop() },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSheet(
    store: ProgressStore,
    onDismiss: () -> Unit,
    onReset: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var confirmReset by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Paper,
        dragHandle = {
            Box(
                Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 38.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Line),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
        ) {
            Text("Settings", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Make Lingo fit your day.",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )
            Spacer(Modifier.height(26.dp))
            Text("DAILY GOAL", style = MaterialTheme.typography.labelSmall, color = Muted)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(20, 30, 40, 60, 80).forEach { goal ->
                    val active = store.dailyGoal == goal
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (active) Forest else Cream)
                            .border(1.dp, if (active) Forest else Line, RoundedCornerShape(14.dp))
                            .clickable { store.updateDailyGoal(goal) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "$goal",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (active) Lime else InkSoft,
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${store.dailyGoal} XP a day · about ${(store.dailyGoal / 30f).coerceAtLeast(1f).toInt()} lesson${if (store.dailyGoal >= 60) "s" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Cream)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Sticker(kind = StickerKind.VOLUME, size = 30.dp, elevated = false)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Pronunciation", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Tap any speaker icon to hear words and phrases.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Reset progress",
                style = MaterialTheme.typography.labelLarge,
                color = Danger,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { confirmReset = true }
                    .padding(vertical = 6.dp),
            )
            Text(
                "This clears lessons, XP, streak, saved words, level progress, and weekly scores on this phone. Your call account and friend code stay with your login.",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Lingo · English, every day\nVersion ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            containerColor = Paper,
            titleContentColor = Ink,
            textContentColor = InkSoft,
            title = { Text("Start from scratch?", style = MaterialTheme.typography.headlineSmall) },
            text = { Text("Lessons, XP, streak, saved words, level progress, and weekly scores will be cleared. Your call account and friend code stay with your login. This can't be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmReset = false
                        store.reset()
                        onReset()
                    },
                ) {
                    Text("Reset", color = Danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) {
                    Text("Keep learning", color = Forest, fontWeight = FontWeight.Bold)
                }
            },
        )
    }
}
