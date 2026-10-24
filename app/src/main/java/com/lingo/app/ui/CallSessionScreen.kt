package com.lingo.app.ui

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.lingo.app.data.CallPhase
import com.lingo.app.data.CallReaction
import com.lingo.app.data.CallReportReason
import com.lingo.app.data.CallStore
import com.lingo.app.data.CallSummary
import com.lingo.app.data.CefrLevel
import com.lingo.app.data.LivePeer
import com.lingo.app.data.StickerKind
import java.util.Locale

@Composable
fun CallSessionScreen(
    callStore: CallStore,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val view = LocalView.current
    var endSessionDialog by rememberSaveable { mutableStateOf(false) }
    var reportDialog by rememberSaveable { mutableStateOf(false) }
    var permissionRequested by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        permissionRequested = true
        if (granted) {
            callStore.connectRoom()
        } else {
            callStore.microphoneDenied()
        }
    }

    val micGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO,
    ) == PackageManager.PERMISSION_GRANTED

    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    DisposableEffect(view) {
        val window = (context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val previousStatus = controller?.isAppearanceLightStatusBars
        val previousNavigation = controller?.isAppearanceLightNavigationBars
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false
        onDispose {
            controller?.let { bars ->
                if (previousStatus != null) bars.isAppearanceLightStatusBars = previousStatus
                if (previousNavigation != null) bars.isAppearanceLightNavigationBars = previousNavigation
            }
        }
    }

    LaunchedEffect(callStore.phase, micGranted) {
        if (callStore.phase == CallPhase.CONNECTING && !callStore.connected) {
            if (micGranted) {
                callStore.connectRoom()
            } else if (!permissionRequested) {
                permissionRequested = true
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    LaunchedEffect(callStore.phase) {
        if (callStore.phase == CallPhase.IDLE) onExit()
    }

    fun requestEndSession() {
        if (callStore.phase == CallPhase.SUMMARY || callStore.phase == CallPhase.ERROR) {
            callStore.endSessionAndReset()
        } else {
            endSessionDialog = true
        }
    }

    BackHandler {
        when (callStore.phase) {
            CallPhase.SEARCHING -> callStore.cancelSearch()
            CallPhase.RINGING -> {
                if (callStore.session?.isCaller == true) {
                    callStore.cancelOutgoing()
                } else {
                    callStore.declineIncoming()
                }
            }
            CallPhase.CONNECTING, CallPhase.CONNECTED -> endSessionDialog = true
            CallPhase.SUMMARY, CallPhase.ERROR -> callStore.endSessionAndReset()
            CallPhase.IDLE -> onExit()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Forest, DeepTeal, Ink),
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp)
                .padding(top = 10.dp, bottom = 18.dp),
        ) {
            CallHeader(
                phase = callStore.phase,
                isCaller = callStore.session?.isCaller,
                onEnd = ::requestEndSession,
            )

            Spacer(Modifier.height(8.dp))

            when (callStore.phase) {
                CallPhase.SEARCHING -> SearchingStage(callStore)
                CallPhase.RINGING -> RingingStage(
                    callStore = callStore,
                    onCancel = {
                        if (callStore.session?.isCaller == true) {
                            callStore.cancelOutgoing()
                        } else {
                            callStore.declineIncoming()
                        }
                    },
                    onAccept = { callStore.acceptIncoming() },
                )
                CallPhase.CONNECTING -> ConnectingStage(callStore)
                CallPhase.CONNECTED -> ConnectedStage(
                    callStore = callStore,
                    onReport = { reportDialog = true },
                )
                CallPhase.SUMMARY -> {
                    val summary = callStore.lastSummary
                    if (summary != null) {
                        SummaryStage(
                            callStore = callStore,
                            summary = summary,
                            onReport = { reportDialog = true },
                            onFindAnother = {
                                callStore.endSessionAndReset()
                                callStore.startMatch()
                            },
                            onDone = { callStore.endSessionAndReset() },
                        )
                    }
                }
                CallPhase.ERROR -> ErrorStage(
                    callStore = callStore,
                    onBack = { callStore.endSessionAndReset() },
                )
                CallPhase.IDLE -> Spacer(Modifier.height(1.dp))
            }
        }
    }

    if (endSessionDialog) {
        AlertDialog(
            onDismissRequest = { endSessionDialog = false },
            containerColor = Paper,
            titleContentColor = Ink,
            textContentColor = InkSoft,
            title = { Text("End this session?", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Text(
                    "This leaves the call and closes the live-call screen. Your lesson progress is untouched.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        endSessionDialog = false
                        callStore.endSessionAndReset()
                    },
                ) {
                    Text("End session", color = Danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { endSessionDialog = false }) {
                    Text("Keep talking", color = Forest, fontWeight = FontWeight.Bold)
                }
            },
        )
    }

    if (reportDialog) {
        ReportDialog(
            onDismiss = { reportDialog = false },
            onReport = { reason ->
                reportDialog = false
                callStore.reportCall(reason)
            },
        )
    }
}

@Composable
private fun CallHeader(
    phase: CallPhase,
    isCaller: Boolean?,
    onEnd: () -> Unit,
) {
    val title = when (phase) {
        CallPhase.SEARCHING -> "FINDING A PARTNER"
        CallPhase.RINGING -> if (isCaller == true) "CALLING" else "INCOMING CALL"
        CallPhase.CONNECTING -> "CONNECTING AUDIO"
        CallPhase.CONNECTED -> "LIVE CALL"
        CallPhase.SUMMARY -> "CALL ENDED"
        CallPhase.ERROR -> "CALL INTERRUPTED"
        CallPhase.IDLE -> ""
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (phase == CallPhase.CONNECTED) Lime else Cream.copy(alpha = 0.45f)),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = Cream.copy(alpha = 0.74f),
                letterSpacing = 1.6.sp,
            )
        }
        Text(
            text = if (phase == CallPhase.SUMMARY || phase == CallPhase.ERROR) "Close" else "End session",
            style = MaterialTheme.typography.labelMedium,
            color = if (phase == CallPhase.SUMMARY || phase == CallPhase.ERROR) Lime else Cream.copy(alpha = 0.72f),
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onEnd)
                .padding(horizontal = 8.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun SearchingStage(callStore: CallStore) {
    val level = callStore.profile?.level ?: CefrLevel.A1
    val transition = rememberInfiniteTransition(label = "searchPulse")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1_700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulse",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier.size(236.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size((180 + (pulse * 56)).dp)
                    .clip(CircleShape)
                    .border(1.dp, Lime.copy(alpha = (1f - pulse) * 0.34f), CircleShape),
            )
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Cream.copy(alpha = 0.08f))
                    .border(1.dp, Lime.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Sticker(kind = StickerKind.HEADPHONES, size = 64.dp, background = Lime, tint = Ink)
            }
        }
        Spacer(Modifier.height(22.dp))
        Text(
            "Listening for a ${level.label} voice",
            style = MaterialTheme.typography.headlineMedium,
            color = Cream,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "We only match you with another ${level.label} learner. Keep this screen open while we look.",
            style = MaterialTheme.typography.bodyMedium,
            color = Cream.copy(alpha = 0.68f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 14.dp),
        )
        Spacer(Modifier.height(18.dp))
        QueueHint(callStore.queueCount)
        callStore.actionError?.let { message ->
            Spacer(Modifier.height(12.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = Coral,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 18.dp),
            )
        }
        Spacer(Modifier.weight(1f))
        SecondaryButton(
            text = "Leave the queue",
            modifier = Modifier.fillMaxWidth(),
            container = Color.Transparent,
            contentColor = Cream,
            borderColor = Cream.copy(alpha = 0.25f),
            onClick = { callStore.cancelSearch() },
        )
    }
}

@Composable
private fun QueueHint(count: Int) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(Cream.copy(alpha = 0.08f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (count > 0) Lime else Cream.copy(alpha = 0.35f)),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            if (count <= 1) "You are in the queue" else "$count learners are looking too",
            style = MaterialTheme.typography.labelMedium,
            color = Cream.copy(alpha = 0.72f),
        )
    }
}

@Composable
private fun RingingStage(
    callStore: CallStore,
    onCancel: () -> Unit,
    onAccept: () -> Unit,
) {
    val peer = callStore.session?.peer ?: return
    val incoming = callStore.session?.isCaller == false
    val transition = rememberInfiniteTransition(label = "ringPulse")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1_300), RepeatMode.Restart),
        label = "ring",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier.size(230.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size((180 + pulse * 45).dp)
                    .clip(CircleShape)
                    .border(1.dp, Lime.copy(alpha = (1f - pulse) * 0.4f), CircleShape),
            )
            PeerOrb(peer = peer, size = 150)
        }
        Spacer(Modifier.height(22.dp))
        Text(
            if (incoming) "Incoming call" else "Calling ${peer.name}",
            style = MaterialTheme.typography.labelSmall,
            color = Lime,
            letterSpacing = 1.4.sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            peer.name,
            style = MaterialTheme.typography.displaySmall,
            color = Cream,
        )
        Spacer(Modifier.height(5.dp))
        Text(
            "${peer.level.label} · ${peer.detail} · ★ ${peer.rating.label}",
            style = MaterialTheme.typography.bodyMedium,
            color = Cream.copy(alpha = 0.68f),
        )
        Spacer(Modifier.height(20.dp))
        if (incoming) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                RoundAction(
                    label = "Decline",
                    symbol = "✕",
                    background = Coral,
                    contentColor = Cream,
                    onClick = onCancel,
                )
                RoundAction(
                    label = "Accept",
                    symbol = "✓",
                    background = Lime,
                    contentColor = Ink,
                    onClick = onAccept,
                )
            }
        } else {
            Text(
                "Ringing…",
                style = MaterialTheme.typography.titleMedium,
                color = Cream.copy(alpha = 0.8f),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "They need to accept before either microphone opens.",
                style = MaterialTheme.typography.bodySmall,
                color = Cream.copy(alpha = 0.56f),
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.weight(1f))
        if (!incoming) {
            SecondaryButton(
                text = "Cancel call",
                modifier = Modifier.fillMaxWidth(),
                container = Color.Transparent,
                contentColor = Cream,
                borderColor = Cream.copy(alpha = 0.25f),
                onClick = onCancel,
            )
        }
    }
}

@Composable
private fun ConnectingStage(callStore: CallStore) {
    val peer = callStore.session?.peer
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        if (peer != null) {
            PeerOrb(peer = peer, size = 148)
        } else {
            CircularProgressIndicator(color = Lime)
        }
        Spacer(Modifier.height(22.dp))
        Text(
            "Connecting audio",
            style = MaterialTheme.typography.headlineMedium,
            color = Cream,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (peer != null) {
                "Opening a private audio room with ${peer.name}."
            } else {
                "Preparing your microphone."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = Cream.copy(alpha = 0.68f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(18.dp))
        CircularProgressIndicator(
            modifier = Modifier.size(22.dp),
            color = Lime,
            strokeWidth = 2.dp,
        )
        Spacer(Modifier.weight(1f))
        Text(
            "Keep the app open while the room connects.",
            style = MaterialTheme.typography.bodySmall,
            color = Cream.copy(alpha = 0.48f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ConnectedStage(
    callStore: CallStore,
    onReport: () -> Unit,
) {
    val peer = callStore.session?.peer ?: return
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(0.7f))
        Box(contentAlignment = Alignment.Center) {
            PeerOrb(peer = peer, size = 154)
            if (callStore.partnerReaction != null) {
                Box(modifier = Modifier.align(Alignment.TopEnd)) {
                    ReactionBubble(emoji = callStore.partnerReaction?.emoji.orEmpty())
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(
            peer.name,
            style = MaterialTheme.typography.displaySmall,
            color = Cream,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            "${peer.level.label} · ${peer.detail} · ★ ${peer.rating.label}",
            style = MaterialTheme.typography.bodyMedium,
            color = Cream.copy(alpha = 0.68f),
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(Cream.copy(alpha = 0.09f))
                .padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Lime),
            )
            Spacer(Modifier.width(7.dp))
            Text(
                "LIVE · ${formatDuration(callStore.callSeconds)}",
                style = MaterialTheme.typography.labelMedium,
                color = Cream.copy(alpha = 0.82f),
            )
        }

        Spacer(Modifier.weight(0.8f))

        AnimatedVisibility(
            visible = callStore.myReaction != null,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.85f),
        ) {
            ReactionBubble(emoji = callStore.myReaction?.emoji.orEmpty(), mine = true)
        }
        Spacer(Modifier.height(10.dp))

        ReactionTray(
            onReaction = { callStore.sendReaction(it) },
        )

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CallControl(
                icon = if (callStore.micOn) Icons.Rounded.Mic else Icons.Rounded.MicOff,
                label = if (callStore.micOn) "Mic on" else "Mic off",
                active = callStore.micOn,
                onClick = { callStore.setMicEnabled(!callStore.micOn) },
            )
            CallControl(
                icon = Icons.Rounded.Mic, // replaced by a flag below for report
                label = "Report",
                active = false,
                iconText = "!",
                onClick = onReport,
            )
            CallControl(
                icon = Icons.Rounded.Mic,
                label = "Skip",
                active = false,
                iconText = "→",
                onClick = { callStore.skipAndFindNext() },
            )
        }

        Spacer(Modifier.height(18.dp))
        PrimaryButton(
            text = "End call",
            modifier = Modifier.fillMaxWidth(),
            container = Coral,
            contentColor = Cream,
            onClick = { callStore.endCall() },
        )
    }
}

@Composable
private fun SummaryStage(
    callStore: CallStore,
    summary: CallSummary,
    onReport: () -> Unit,
    onFindAnother: () -> Unit,
    onDone: () -> Unit,
) {
    val peer = summary.session.peer
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(0.45f))
        PeerOrb(peer = peer, size = 104)
        Spacer(Modifier.height(16.dp))
        Text(
            if (summary.reported) "Report sent" else "Good conversation",
            style = MaterialTheme.typography.headlineMedium,
            color = Cream,
        )
        Spacer(Modifier.height(5.dp))
        Text(
            "${peer.name} · ${formatDuration(summary.seconds)}",
            style = MaterialTheme.typography.bodyMedium,
            color = Cream.copy(alpha = 0.66f),
        )
        if (summary.reported) {
            Spacer(Modifier.height(10.dp))
            Text(
                "This player is now blocked for you and will not appear in future matching.",
                style = MaterialTheme.typography.bodySmall,
                color = Lime,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 18.dp),
            )
        }

        Spacer(Modifier.height(24.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Cream.copy(alpha = 0.08f))
                .border(1.dp, Cream.copy(alpha = 0.1f), RoundedCornerShape(22.dp))
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                if (callStore.ratedStars > 0) "Thanks for the rating" else "How was ${peer.name}?",
                style = MaterialTheme.typography.titleMedium,
                color = Cream,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (callStore.ratedStars > 0) {
                    "Your rating helps the next learner choose a good conversation."
                } else {
                    "Every player gets a 1–5 star rating after the call."
                },
                style = MaterialTheme.typography.bodySmall,
                color = Cream.copy(alpha = 0.58f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            StarRating(
                selected = callStore.ratedStars,
                onSelect = { callStore.rateCall(it) },
            )
            if (callStore.actionError != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    callStore.actionError.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Coral,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(Modifier.weight(0.7f))

        PrimaryButton(
            text = "Find another partner",
            modifier = Modifier.fillMaxWidth(),
            leading = "◉",
            onClick = onFindAnother,
        )
        Spacer(Modifier.height(9.dp))
        if (!summary.reported) {
            SecondaryButton(
                text = "Report ${peer.name}",
                modifier = Modifier.fillMaxWidth(),
                container = Color.Transparent,
                contentColor = Coral,
                borderColor = Coral.copy(alpha = 0.42f),
                onClick = onReport,
            )
            Spacer(Modifier.height(9.dp))
        }
        Text(
            "Done",
            style = MaterialTheme.typography.labelMedium,
            color = Cream.copy(alpha = 0.7f),
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onDone)
                .padding(horizontal = 18.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun ErrorStage(
    callStore: CallStore,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(Cream.copy(alpha = 0.08f))
                .border(1.dp, Coral.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("!", fontSize = 42.sp, color = Coral, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(20.dp))
        Text(
            callStore.callError ?: "The call could not start.",
            style = MaterialTheme.typography.headlineSmall,
            color = Cream,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "No microphone audio was sent. You can go back and try again.",
            style = MaterialTheme.typography.bodySmall,
            color = Cream.copy(alpha = 0.58f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(1f))
        PrimaryButton(
            text = "Back to calls",
            modifier = Modifier.fillMaxWidth(),
            container = Lime,
            contentColor = Ink,
            onClick = onBack,
        )
    }
}

@Composable
private fun PeerOrb(peer: LivePeer, size: Int) {
    Box(
        modifier = Modifier.size(size.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Lime.copy(alpha = 0.16f)),
        )
        Box(
            modifier = Modifier
                .size((size * 0.82f).dp)
                .clip(CircleShape)
                .background(Cream),
        )
        Sticker(
            kind = peer.sticker,
            size = (size * 0.54f).dp,
            elevated = false,
        )
    }
}

@Composable
private fun ReactionBubble(emoji: String, mine: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (mine) Lime else Cream)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Text(emoji, fontSize = 26.sp)
    }
}

@Composable
private fun ReactionTray(onReaction: (CallReaction) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Cream.copy(alpha = 0.08f))
            .border(1.dp, Cream.copy(alpha = 0.1f), RoundedCornerShape(22.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        CallReaction.entries.forEach { reaction ->
            Text(
                text = reaction.emoji,
                fontSize = 25.sp,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onReaction(reaction) }
                    .padding(horizontal = 7.dp, vertical = 5.dp),
            )
        }
    }
}

@Composable
private fun CallControl(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    iconText: String? = null,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(if (active) Cream else Cream.copy(alpha = 0.09f))
                .border(1.dp, Cream.copy(alpha = 0.12f), CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (iconText != null) {
                Text(
                    iconText,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (active) Ink else Cream,
                )
            } else {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = if (active) Ink else Cream,
                    modifier = Modifier.size(23.dp),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Cream.copy(alpha = 0.66f),
        )
    }
}

@Composable
private fun RoundAction(
    label: String,
    symbol: String,
    background: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(background)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(symbol, fontSize = 25.sp, color = contentColor, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(7.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = Cream.copy(alpha = 0.72f))
    }
}

@Composable
private fun StarRating(
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        (1..5).forEach { star ->
            val active = star <= selected
            Text(
                if (active) "★" else "☆",
                fontSize = 32.sp,
                color = if (active) Lime else Cream.copy(alpha = 0.42f),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { if (selected == 0) onSelect(star) }
                    .padding(2.dp),
            )
        }
    }
}

@Composable
private fun ReportDialog(
    onDismiss: () -> Unit,
    onReport: (CallReportReason) -> Unit,
) {
    var selected by remember { mutableStateOf<CallReportReason?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Paper,
        titleContentColor = Ink,
        textContentColor = InkSoft,
        title = { Text("Report this player", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                Text(
                    "Choose the reason. Reporting also ends the call and blocks the player immediately.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(12.dp))
                CallReportReason.entries.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected == reason) Mint else Color.Transparent)
                            .clickable { selected = reason }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .border(1.dp, if (selected == reason) Emerald else Line, CircleShape)
                                .background(if (selected == reason) Emerald else Color.Transparent),
                        )
                        Spacer(Modifier.width(9.dp))
                        Text(reason.label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { selected?.let(onReport) },
                enabled = selected != null,
            ) {
                Text("Report and block", color = Danger, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Forest, fontWeight = FontWeight.Bold)
            }
        },
    )
}

private fun formatDuration(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    val minutes = safe / 60
    val remainder = safe % 60
    return String.format(Locale.US, "%02d:%02d", minutes, remainder)
}
