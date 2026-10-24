package com.lingo.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingo.app.data.CallPhase
import com.lingo.app.data.CallStore
import com.lingo.app.data.MyProfile
import com.lingo.app.data.formatFriendCode
import com.lingo.app.data.normalizeFriendCode
import com.lingo.app.data.ProgressStore
import com.lingo.app.data.StickerKind

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallHubScreen(
    store: ProgressStore,
    callStore: CallStore,
    onOpenLevelPath: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showFriendSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(callStore.signedIn, callStore.passwordRecoveryActive) {
        if (callStore.signedIn && !callStore.passwordRecoveryActive) callStore.refreshHub()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 18.dp, bottom = 26.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "CALL",
                    style = MaterialTheme.typography.labelSmall,
                    color = Emerald,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Speak with a real learner.",
                    style = MaterialTheme.typography.headlineMedium,
                    lineHeight = 30.sp,
                )
            }
            LingoLogo(size = 42.dp)
        }

        Spacer(Modifier.height(18.dp))

        when {
            !callStore.isConfigured -> NotConfiguredCard()
            !callStore.authReady -> LoadingCard("Opening the call service…")
            callStore.passwordRecoveryActive -> PasswordResetCard(callStore)
            !callStore.signedIn -> AuthCard(callStore)
            else -> {
                val profile = callStore.profile
                if (profile == null) {
                    val message = callStore.actionError
                    if (message != null) {
                        ProfileErrorCard(
                            message = message,
                            onRetry = {
                                callStore.clearActionError()
                                callStore.refreshHub()
                            },
                        )
                    } else {
                        LoadingCard("Loading your call profile…")
                    }
                } else {
                    ConnectedHub(
                        profile = profile,
                        callStore = callStore,
                        onFindPartner = { callStore.startMatch() },
                        onCallFriend = { showFriendSheet = true },
                        onOpenLevelPath = onOpenLevelPath,
                    )
                }
            }
        }

        Spacer(Modifier.height(26.dp))
        SafetyCard()
        Spacer(Modifier.height(14.dp))
        if (callStore.signedIn) {
            Text(
                "Sign out",
                style = MaterialTheme.typography.labelMedium,
                color = Muted,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { callStore.signOut() }
                    .padding(8.dp),
            )
        }
    }

    if (showFriendSheet) {
        FriendCallSheet(
            callStore = callStore,
            onDismiss = {
                showFriendSheet = false
                callStore.clearActionError()
            },
        )
    }
}

@Composable
private fun NotConfiguredCard() {
    SurfaceCard(background = Peach, border = Coral.copy(alpha = 0.25f)) {
        Text("Calls are offline in this build", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(
            "The live call service has not been connected yet. Lessons and progress still work normally.",
            style = MaterialTheme.typography.bodyMedium,
            color = InkSoft,
        )
    }
}

@Composable
private fun ProfileErrorCard(
    message: String,
    onRetry: () -> Unit,
) {
    SurfaceCard(background = Peach, border = Coral.copy(alpha = 0.25f)) {
        Text("Your call profile could not load", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(message, style = MaterialTheme.typography.bodySmall, color = InkSoft)
        Spacer(Modifier.height(14.dp))
        PrimaryButton(
            text = "Try again",
            modifier = Modifier.fillMaxWidth(),
            onClick = onRetry,
        )
    }
}

@Composable
private fun LoadingCard(message: String) {
    SurfaceCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Emerald,
            )
            Spacer(Modifier.width(12.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
    }
}

@Composable
internal fun AuthCard(
    callStore: CallStore,
    signInTitle: String = "Welcome back",
    signInBody: String = "Sign in to join the live call queue and keep your ratings across devices.",
    showIntro: Boolean = true,
) {
    var createAccount by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    if (callStore.passwordResetSuccess) {
        SurfaceCard {
            Text("Password updated", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(
                "Your old password no longer works. Sign in with the new password you just chose.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft,
            )
            Spacer(Modifier.height(18.dp))
            PrimaryButton(
                text = "Back to sign in",
                modifier = Modifier.fillMaxWidth(),
                onClick = { callStore.clearPasswordResetState() },
            )
        }
        return
    }

    if (callStore.passwordResetEmailSent) {
        SurfaceCard {
            Text("Check your email", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(
                "We sent a password reset link to ${callStore.passwordResetEmail}. Open it on this phone to choose a new password.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft,
            )
            Spacer(Modifier.height(18.dp))
            PrimaryButton(
                text = "Back to sign in",
                modifier = Modifier.fillMaxWidth(),
                onClick = { callStore.clearPasswordResetState() },
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (callStore.passwordResetBusy) "Sending…" else "Send another link",
                style = MaterialTheme.typography.labelMedium,
                color = Emerald,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        if (!callStore.passwordResetBusy) {
                            callStore.requestPasswordReset(callStore.passwordResetEmail)
                        }
                    }
                    .padding(8.dp),
            )
        }
        return
    }

    if (callStore.awaitingEmailConfirmation) {
        SurfaceCard {
            Text("Check your email", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(
                "We sent a confirmation link to $email. Open it once, then come back and sign in.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft,
            )
            Spacer(Modifier.height(18.dp))
            PrimaryButton(
                text = "I've confirmed — sign in",
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    callStore.clearEmailConfirmation()
                    createAccount = false
                },
            )
        }
        return
    }

    Column {
        if (showIntro) {
            SurfaceCard(background = Forest, border = Forest) {
                Tag(text = "LIVE AUDIO", background = Lime.copy(alpha = 0.18f), color = Lime)
                Spacer(Modifier.height(16.dp))
                Text(
                    "One conversation\nchanges everything.",
                    style = MaterialTheme.typography.displaySmall,
                    color = Cream,
                    lineHeight = 36.sp,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Meet learners at your English level, or call a friend with their code. No video. No chat. Just voices.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Cream.copy(alpha = 0.72f),
                )
            }

            Spacer(Modifier.height(14.dp))
        }

        SurfaceCard {
            Text(
                if (createAccount) "Create your account" else signInTitle,
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (createAccount) {
                    "Your level and progress stay on this phone. Sign-in keeps calls and ratings real."
                } else {
                    signInBody
                },
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
            Spacer(Modifier.height(16.dp))

            if (createAccount) {
                CallTextField(
                    value = name,
                    onValueChange = { name = it.take(32) },
                    label = "Your first name",
                    keyboardType = KeyboardType.Text,
                )
                Spacer(Modifier.height(10.dp))
            }
            CallTextField(
                value = email,
                onValueChange = { email = it.trim().take(120) },
                label = "Email",
                keyboardType = KeyboardType.Email,
            )
            Spacer(Modifier.height(10.dp))
            CallTextField(
                value = password,
                onValueChange = { password = it.take(80) },
                label = "Password",
                keyboardType = KeyboardType.Password,
                isPassword = true,
                imeAction = if (createAccount) ImeAction.Done else ImeAction.Done,
            )

            if (!createAccount) {
                Text(
                    "Forgot password?",
                    style = MaterialTheme.typography.labelMedium,
                    color = Emerald,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            callStore.requestPasswordReset(email)
                        }
                        .padding(top = 6.dp, bottom = 2.dp, start = 6.dp, end = 6.dp),
                )
            }

            (callStore.authError ?: callStore.passwordResetError)?.let { message ->
                Spacer(Modifier.height(12.dp))
                Text(
                    message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Danger,
                )
            }

            Spacer(Modifier.height(16.dp))
            PrimaryButton(
                text = if (createAccount) "Create account" else "Sign in",
                modifier = Modifier.fillMaxWidth(),
                enabled = !callStore.authBusy,
                onClick = {
                    if (createAccount) {
                        callStore.signUp(name, email, password)
                    } else {
                        callStore.signIn(email, password)
                    }
                },
            )
            Spacer(Modifier.height(10.dp))
            Text(
                if (createAccount) "Already have an account? Sign in" else "New here? Create a free account",
                style = MaterialTheme.typography.labelMedium,
                color = Emerald,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        createAccount = !createAccount
                        callStore.clearAuthError()
                        callStore.clearPasswordResetState()
                    }
                    .padding(8.dp),
            )
        }

        Spacer(Modifier.height(12.dp))
        if (showIntro) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(horizontal = 4.dp),
            ) {
                Icon(
                    Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = Muted,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    "Passwords are handled by Supabase. Lingo never stores your password on the phone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
        }
    }
}

@Composable
internal fun PasswordResetCard(callStore: CallStore) {
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    SurfaceCard {
        Tag(text = "ACCOUNT RECOVERY")
        Spacer(Modifier.height(14.dp))
        Text("Choose a new password", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(6.dp))
        Text(
            "Use at least six characters. This replaces the password for your call account.",
            style = MaterialTheme.typography.bodyMedium,
            color = InkSoft,
        )
        Spacer(Modifier.height(16.dp))
        CallTextField(
            value = newPassword,
            onValueChange = { newPassword = it.take(80) },
            label = "New password",
            keyboardType = KeyboardType.Password,
            isPassword = true,
        )
        Spacer(Modifier.height(10.dp))
        CallTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it.take(80) },
            label = "Confirm new password",
            keyboardType = KeyboardType.Password,
            isPassword = true,
            imeAction = ImeAction.Done,
        )
        callStore.passwordResetError?.let { message ->
            Spacer(Modifier.height(12.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = Danger,
            )
        }
        Spacer(Modifier.height(16.dp))
        PrimaryButton(
            text = if (callStore.passwordResetBusy) "Updating…" else "Update password",
            modifier = Modifier.fillMaxWidth(),
            enabled = !callStore.passwordResetBusy,
            onClick = { callStore.completePasswordReset(newPassword, confirmPassword) },
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Cancel",
            style = MaterialTheme.typography.labelMedium,
            color = Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { callStore.cancelPasswordRecovery() }
                .padding(8.dp),
        )
    }
}

@Composable
private fun ConnectedHub(
    profile: MyProfile,
    callStore: CallStore,
    onFindPartner: () -> Unit,
    onCallFriend: () -> Unit,
    onOpenLevelPath: () -> Unit,
) {
    val (copy, share) = rememberCodeActions(profile.displayCode)

    SurfaceCard(background = Forest, border = Forest) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Tag(
                text = "LIVE",
                background = Lime.copy(alpha = 0.18f),
                color = Lime,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Signed in as ${profile.displayName}",
                style = MaterialTheme.typography.bodySmall,
                color = Cream.copy(alpha = 0.7f),
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            "Ready when\nyou are.",
            style = MaterialTheme.typography.displayMedium,
            color = Cream,
            lineHeight = 40.sp,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Random calls match you with another ${profile.level.label} learner. Friend calls can cross levels.",
            style = MaterialTheme.typography.bodyMedium,
            color = Cream.copy(alpha = 0.72f),
        )
        Spacer(Modifier.height(20.dp))
        PrimaryButton(
            text = "Find a speaking partner",
            modifier = Modifier.fillMaxWidth(),
            leading = "◉",
            container = Lime,
            contentColor = Ink,
            onClick = onFindPartner,
        )
        Spacer(Modifier.height(10.dp))
        SecondaryButton(
            text = "Call a friend by code",
            modifier = Modifier.fillMaxWidth(),
            container = Color.Transparent,
            contentColor = Cream,
            borderColor = Cream.copy(alpha = 0.22f),
            onClick = onCallFriend,
        )
    }

    Spacer(Modifier.height(12.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CallStat(
            value = profile.ratingLabel,
            label = if (profile.rating.count == 0) "NEW" else "RATING",
            modifier = Modifier.weight(1f),
            sticker = StickerKind.STAR,
        )
        CallStat(
            value = "${profile.callsCompleted}",
            label = "CALLS",
            modifier = Modifier.weight(1f),
            sticker = StickerKind.HEADPHONES,
        )
        CallStat(
            value = "${profile.minutesTalked}",
            label = "MINUTES",
            modifier = Modifier.weight(1f),
            sticker = StickerKind.HOURGLASS,
        )
    }

    Spacer(Modifier.height(14.dp))

    SurfaceCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("YOUR LEVEL", style = MaterialTheme.typography.labelSmall, color = Muted)
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Sticker(kind = profile.level.sticker, size = 34.dp, elevated = false)
                    Spacer(Modifier.width(9.dp))
                    Text(
                        "${profile.level.label} · ${profile.level.title}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            Text(
                "Level path →",
                style = MaterialTheme.typography.labelMedium,
                color = Emerald,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onOpenLevelPath)
                    .padding(6.dp),
            )
        }
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Cream)
                .padding(13.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(if (callStore.queueCount > 0) Emerald else Muted),
                )
                Spacer(Modifier.width(9.dp))
                Text(
                    when {
                        callStore.hubLoading -> "Checking who is around…"
                        callStore.queueCount == 0 -> "No one is in the ${profile.level.label} queue yet."
                        callStore.queueCount == 1 -> "1 learner is waiting at ${profile.level.label}."
                        else -> "${callStore.queueCount} learners are waiting at ${profile.level.label}."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSoft,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Matching is always same-level. A friend code is the only way to call across levels.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )
    }

    Spacer(Modifier.height(14.dp))

    SurfaceCard {
        Text("FRIEND CODE", style = MaterialTheme.typography.labelSmall, color = Muted)
        Spacer(Modifier.height(4.dp))
        Text(
            profile.displayCode,
            style = MaterialTheme.typography.displaySmall,
            letterSpacing = 2.sp,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Share it so a friend can call you directly.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallAction(
                icon = Icons.Rounded.ContentCopy,
                label = "Copy",
                modifier = Modifier.weight(1f),
            ) {
                copy()
            }
            SmallAction(
                icon = Icons.Rounded.Share,
                label = "Share",
                modifier = Modifier.weight(1f),
            ) {
                share()
            }
        }
    }

    callStore.actionError?.let { message ->
        Spacer(Modifier.height(12.dp))
        SurfaceCard(background = Peach, border = Coral.copy(alpha = 0.25f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Danger,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "Dismiss",
                    style = MaterialTheme.typography.labelSmall,
                    color = Forest,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { callStore.clearActionError() }
                        .padding(6.dp),
                )
            }
        }
    }

    Spacer(Modifier.height(14.dp))

    SurfaceCard {
        Text("HOW LIVE CALLS WORK", style = MaterialTheme.typography.labelSmall, color = Muted)
        Spacer(Modifier.height(10.dp))
        HowStep("1", "Match at your level", "Random calls only pair learners at the same CEFR level.")
        HowStep("2", "Talk with real audio", "LiveKit carries the voice. There is no video and no in-call text.")
        HowStep("3", "Rate and stay safe", "Every call ends with a 1–5 star rating. Reports block the player instantly.")
    }
}

@Composable
private fun CallStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    sticker: StickerKind,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Paper)
            .border(1.dp, Line, RoundedCornerShape(20.dp))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Sticker(kind = sticker, size = 20.dp, elevated = false)
            Spacer(Modifier.width(5.dp))
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Muted)
    }
}

@Composable
private fun HowStep(number: String, title: String, body: String) {
    Row(modifier = Modifier.padding(vertical = 5.dp)) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Mint),
            contentAlignment = Alignment.Center,
        ) {
            Text(number, style = MaterialTheme.typography.labelSmall, color = Forest)
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(body, style = MaterialTheme.typography.bodySmall, color = Muted)
        }
    }
}

@Composable
private fun SafetyCard() {
    SurfaceCard(background = Lavender.copy(alpha = 0.55f), border = Purple.copy(alpha = 0.12f)) {
        Row(verticalAlignment = Alignment.Top) {
            Sticker(kind = StickerKind.SPEAK, size = 34.dp, elevated = false)
            Spacer(Modifier.width(11.dp))
            Column {
                Text("Safety is part of the call", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(3.dp))
                Text(
                    "Report a player from any call. The report is sent to the call service and that player is blocked immediately.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSoft,
                )
            }
        }
    }
}

@Composable
internal fun SurfaceCard(
    background: Color = Paper,
    border: Color = Line,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(background)
            .border(1.dp, border, RoundedCornerShape(24.dp))
            .padding(18.dp),
        content = content,
    )
}

@Composable
private fun CallTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction,
        ),
        visualTransformation = if (isPassword) {
            PasswordVisualTransformation()
        } else {
            androidx.compose.ui.text.input.VisualTransformation.None
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Emerald,
            unfocusedBorderColor = Line,
            focusedContainerColor = Cream,
            unfocusedContainerColor = Cream,
            cursorColor = Forest,
            focusedTextColor = Ink,
            unfocusedTextColor = Ink,
        ),
    )
}

@Composable
private fun SmallAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Cream)
            .border(1.dp, Line, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Forest, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = Forest)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FriendCallSheet(
    callStore: CallStore,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var code by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(callStore.phase) {
        if (callStore.phase != CallPhase.IDLE) onDismiss()
    }

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
                .imePadding()
                .padding(bottom = 26.dp),
        ) {
            Text("Call a friend", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                "Enter the six-character code they shared with you. Friend calls can cross English levels.",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
            Spacer(Modifier.height(18.dp))
            OutlinedTextField(
                value = code,
                onValueChange = { code = normalizeFriendCode(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Friend code") },
                placeholder = { Text("ABC123") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Done,
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald,
                    unfocusedBorderColor = Line,
                    focusedContainerColor = Cream,
                    unfocusedContainerColor = Cream,
                ),
            )
            if (code.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Format: ${formatFriendCode(code)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
            callStore.actionError?.let { message ->
                Spacer(Modifier.height(10.dp))
                Text(message, style = MaterialTheme.typography.bodySmall, color = Danger)
            }
            Spacer(Modifier.height(18.dp))
            PrimaryButton(
                text = if (callStore.actionBusy) "Calling…" else "Start call",
                modifier = Modifier.fillMaxWidth(),
                enabled = code.length == 6 && !callStore.actionBusy,
                onClick = { callStore.startFriendCall(code) },
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "They will see an incoming call and can accept or decline it.",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun rememberCodeActions(code: String): Pair<() -> Unit, () -> Unit> {
    val context = LocalContext.current
    val copy = {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Lingo friend code", code))
        Toast.makeText(context, "Friend code copied", Toast.LENGTH_SHORT).show()
    }
    val share = {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "Call me on Lingo. My friend code is $code.",
            )
        }
        context.startActivity(Intent.createChooser(intent, "Share your friend code"))
    }
    return copy to share
}
