package com.lingo.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lingo.app.data.ThemeMode
import com.lingo.app.ui.LingoApp
import com.lingo.app.ui.LingoTheme

class MainActivity : ComponentActivity() {
    private var recoveryIntent by mutableStateOf<Intent?>(null)
    private var openPracticeRequest by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recoveryIntent = intent
        openPracticeRequest = intent.wantsPractice()
        val app = application as LingoApplication
        applySystemBars(app.progressStore.themeMode)
        setContent {
            val mode = app.progressStore.themeMode
            LaunchedEffect(mode) {
                applySystemBars(mode)
            }
            LingoTheme(mode = mode) {
                LingoApp(
                    store = app.progressStore,
                    callStore = app.callStore,
                    recoveryIntent = recoveryIntent,
                    onRecoveryIntentHandled = {
                        recoveryIntent = null
                        // Never keep a consumed recovery URI as the activity's
                        // base intent. Otherwise a task restore after an update
                        // or process death can reopen the reset screen.
                        if (intent?.data != null) {
                            setIntent(Intent(this, MainActivity::class.java))
                        }
                    },
                    openPracticeRequest = openPracticeRequest,
                    onOpenPracticeHandled = {
                        openPracticeRequest = false
                        // Strip the one-shot extra so rotation or a task restore
                        // cannot send the learner to Practice a second time.
                        if (intent.wantsPractice()) {
                            setIntent(Intent(this, MainActivity::class.java))
                        }
                    },
                )
            }
        }
    }

    /** Night needs light status/navigation icons; Paper needs dark icons. */
    private fun applySystemBars(mode: ThemeMode) {
        enableEdgeToEdge(
            statusBarStyle = if (mode == ThemeMode.NIGHT) {
                SystemBarStyle.dark(Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            },
            navigationBarStyle = if (mode == ThemeMode.NIGHT) {
                SystemBarStyle.dark(Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            },
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        recoveryIntent = intent
        openPracticeRequest = intent.wantsPractice()
    }

    private fun Intent?.wantsPractice(): Boolean =
        this?.getBooleanExtra(EXTRA_OPEN_PRACTICE, false) == true

    companion object {
        const val EXTRA_OPEN_PRACTICE = "com.lingo.app.extra.OPEN_PRACTICE"
    }
}
