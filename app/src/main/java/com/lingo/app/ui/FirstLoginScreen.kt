package com.lingo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingo.app.data.CallStore
import com.lingo.app.data.ProgressStore
import java.time.LocalTime

/**
 * Mandatory entry screen for the app. A signed-out learner stays here until
 * they sign in or create an account, so lessons and calls always have a real
 * account behind them. Existing on-phone progress is never cleared.
 */
@Composable
fun FirstLoginScreen(
    store: ProgressStore,
    callStore: CallStore,
    modifier: Modifier = Modifier,
) {
    val greeting = greetingFor(LocalTime.now().hour)
    val earned = buildList {
        if (store.totalXp > 0) add("${store.totalXp} XP")
        if (store.streak > 0) add("${store.streak}-day streak")
    }
    val progressLine = if (earned.isEmpty()) {
        "Sign in once to open your lessons, live calls, and progress. It only takes a moment."
    } else {
        val verb = if (earned.size == 1) "stays" else "stay"
        "Sign in once to open your lessons, live calls, and progress. " +
            "Your ${earned.joinToString(" and ")} $verb safe on this phone."
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 26.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LingoLogo(size = 46.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("LINGO", style = MaterialTheme.typography.titleLarge, letterSpacing = 2.sp)
                Text(
                    "English, every day",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        SurfaceCard(background = Forest, border = Forest) {
            Tag(
                text = "YOUR FIRST STEP",
                background = Lime.copy(alpha = 0.18f),
                color = Lime,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "$greeting\nWelcome to Lingo.",
                style = MaterialTheme.typography.displaySmall,
                color = Cream,
                lineHeight = 37.sp,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                progressLine,
                style = MaterialTheme.typography.bodyMedium,
                color = Cream.copy(alpha = 0.74f),
            )
            if (earned.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (store.totalXp > 0) {
                        Tag(
                            text = "${store.totalXp} XP",
                            background = Cream.copy(alpha = 0.1f),
                            color = Cream,
                        )
                    }
                    if (store.streak > 0) {
                        Tag(
                            text = "${store.streak}-day streak",
                            background = Cream.copy(alpha = 0.1f),
                            color = Cream,
                        )
                    }
                    Tag(
                        text = store.cefrLevel.label,
                        background = Lime.copy(alpha = 0.18f),
                        color = Lime,
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        when {
            !callStore.authReady -> OpeningCard()
            callStore.passwordRecoveryActive -> PasswordResetCard(callStore)
            else -> AuthCard(
                callStore = callStore,
                signInTitle = "Sign in to start learning",
                signInBody = "Use your account to open your lessons, live calls, and progress on this phone.",
                showIntro = false,
            )
        }

        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                Icons.Rounded.Lock,
                contentDescription = null,
                tint = Muted,
                modifier = Modifier.size(15.dp),
            )
            Spacer(Modifier.width(7.dp))
            Text(
                "Your password is handled by Supabase and never stored on this phone. " +
                    "Progress already earned here stays on this device.",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Composable
private fun OpeningCard() {
    SurfaceCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Emerald,
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Opening Lingo…", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(
                    "Checking for an existing account on this phone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
        }
    }
}

private fun greetingFor(hour: Int): String = when (hour) {
    in 0..11 -> "Good morning."
    in 12..17 -> "Good afternoon."
    else -> "Good evening."
}
