package com.lingo.app.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lingo.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.result.PostgrestResult
import io.livekit.android.LiveKit
import io.livekit.android.RoomOptions
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import io.livekit.android.room.track.DataPublishReliability
import io.livekit.android.room.track.LocalAudioTrackOptions
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

private const val REACTION_TOPIC = "lingo.reaction"
private const val PASSWORD_RESET_REDIRECT = "lingo://reset-password"

/**
 * Live call coordinator. Supabase is the source of truth for accounts,
 * matching, friend codes, safety, and ratings. LiveKit carries the audio.
 *
 * The store deliberately exposes one call phase to the UI. Search, ringing,
 * connecting, talking, and the post-call summary are all driven from here so
 * an incoming call can open over any tab.
 */
class CallStore(
    context: Context,
    private val progressStore: ProgressStore,
) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val configured = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    private val supabase: SupabaseClient? = if (configured) {
        createSupabaseClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY) {
            install(Auth)
            install(Postgrest)
        }
    } else {
        null
    }

    private var pollJob: Job? = null
    private var searchJob: Job? = null
    private var timerJob: Job? = null
    private var reactionClearJob: Job? = null
    private var partnerReactionClearJob: Job? = null
    private var connectTimeoutJob: Job? = null
    private var ringTimeoutJob: Job? = null
    private var authSetupJob: Job? = null
    private var refreshJob: Job? = null

    private var room: Room? = null
    private var roomEventsJob: Job? = null
    private var roomConnectInFlight = false
    private var connectGeneration = 0

    var authReady by mutableStateOf(!configured)
        private set
    var signedIn by mutableStateOf(false)
        private set
    var authBusy by mutableStateOf(false)
        private set
    var authError by mutableStateOf<String?>(null)
        private set
    var awaitingEmailConfirmation by mutableStateOf(false)
        private set
    var passwordResetEmailSent by mutableStateOf(false)
        private set
    var passwordResetEmail by mutableStateOf("")
        private set
    var passwordRecoveryActive by mutableStateOf(false)
        private set
    var passwordResetBusy by mutableStateOf(false)
        private set
    var passwordResetError by mutableStateOf<String?>(null)
        private set
    var passwordResetSuccess by mutableStateOf(false)
        private set
    private var recoveryAccessToken: String? = null
    var profile by mutableStateOf<MyProfile?>(null)
        private set
    var hubLoading by mutableStateOf(false)
        private set
    var queueCount by mutableIntStateOf(0)
        private set
    var weeklyEntries by mutableStateOf<List<WeeklyEntry>>(emptyList())
        private set
    var weeklyLoading by mutableStateOf(false)
        private set
    var actionError by mutableStateOf<String?>(null)
        private set
    var actionBusy by mutableStateOf(false)
        private set

    var phase by mutableStateOf(CallPhase.IDLE)
        private set
    var session by mutableStateOf<LiveSession?>(null)
        private set
    var lastSummary by mutableStateOf<CallSummary?>(null)
        private set
    var callSeconds by mutableIntStateOf(0)
        private set
    var micOn by mutableStateOf(true)
        private set
    var connected by mutableStateOf(false)
        private set
    var myReaction by mutableStateOf<CallReaction?>(null)
        private set
    var partnerReaction by mutableStateOf<CallReaction?>(null)
        private set
    var ratedStars by mutableIntStateOf(0)
        private set
    var reported by mutableStateOf(false)
        private set
    var callError by mutableStateOf<String?>(null)
        private set

    val isConfigured: Boolean get() = configured
    val callOpen: Boolean
        get() = phase != CallPhase.IDLE && phase != CallPhase.ERROR

    init {
        val client = supabase
        if (client == null) {
            authError = "Live calls are not configured in this build."
        } else {
            scope.launch {
                try {
                    client.auth.awaitInitialization()
                    authReady = true
                    client.auth.sessionStatus.collect { status ->
                        when (status) {
                            is SessionStatus.Authenticated -> onAuthenticated()
                            is SessionStatus.NotAuthenticated -> onSignedOut(null)
                            is SessionStatus.Initializing -> authReady = false
                            is SessionStatus.RefreshFailure -> onSignedOut(
                                "Your session expired. Please sign in again.",
                            )
                        }
                    }
                } catch (error: Throwable) {
                    authReady = true
                    authError = "Could not reach the account service. Check your connection."
                }
            }
        }
    }

    private fun onAuthenticated() {
        authReady = true
        authError = null
        awaitingEmailConfirmation = false
        passwordResetEmailSent = false
        passwordResetSuccess = false
        passwordResetError = null
        if (signedIn) return
        signedIn = true
        authSetupJob?.cancel()
        authSetupJob = scope.launch {
            refreshHub()
            startPolling()
        }
    }

    private fun onSignedOut(message: String?) {
        authReady = true
        signedIn = false
        profile = null
        queueCount = 0
        weeklyEntries = emptyList()
        actionError = null
        authError = message
        authSetupJob?.cancel()
        refreshJob?.cancel()
        refreshJob = null
        searchJob?.cancel()
        pollJob?.cancel()
        pollJob = null
        if (phase != CallPhase.IDLE) {
            finishLocalCall("Your session ended.")
        }
    }

    fun clearAuthError() {
        authError = null
    }

    fun clearEmailConfirmation() {
        awaitingEmailConfirmation = false
        authError = null
    }

    fun requestPasswordReset(email: String) {
        val client = supabase ?: return
        if (passwordResetBusy) return
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank()) {
            passwordResetError = "Enter the email for your account first."
            return
        }
        passwordResetBusy = true
        passwordResetError = null
        passwordResetSuccess = false
        authError = null
        scope.launch {
            try {
                client.auth.resetPasswordForEmail(
                    email = cleanEmail,
                    redirectUrl = PASSWORD_RESET_REDIRECT,
                )
                passwordResetEmail = cleanEmail
                passwordResetEmailSent = true
            } catch (error: Throwable) {
                val raw = error.message.orEmpty()
                if (raw.contains("User not found", true) ||
                    raw.contains("user_not_found", true)
                ) {
                    // Do not reveal whether an email is registered. A real
                    // account and an unknown address get the same screen.
                    passwordResetEmail = cleanEmail
                    passwordResetEmailSent = true
                } else {
                    passwordResetError = authMessage(error)
                }
            } finally {
                passwordResetBusy = false
            }
        }
    }

    /**
     * Supabase redirects a recovery email to lingo://reset-password with an
     * implicit-flow fragment. The token is kept in memory only and is used for
     * one PUT to /auth/v1/user; no recovery session is persisted on the phone.
     */
    fun handlePasswordResetLink(intent: Intent): Boolean {
        val data = intent.data ?: return false
        if (!data.scheme.equals("lingo", ignoreCase = true) ||
            !data.host.equals("reset-password", ignoreCase = true)
        ) {
            return false
        }
        val fragment = data.fragment.orEmpty()
        val query = data.query.orEmpty()
        val params = Uri.parse("lingo://reset-password?" + fragment.ifBlank { query })
        val linkError = params.getQueryParameter("error_description")
            ?: params.getQueryParameter("error")
            ?: data.getQueryParameter("error_description")
            ?: data.getQueryParameter("error")
        val accessToken = params.getQueryParameter("access_token")
        val type = params.getQueryParameter("type")
        if (!linkError.isNullOrBlank() || accessToken.isNullOrBlank() || type != "recovery") {
            resetPasswordError("This reset link is invalid or expired. Request a new one.")
            return true
        }
        recoveryAccessToken = accessToken
        passwordRecoveryActive = true
        passwordResetEmailSent = false
        passwordResetSuccess = false
        passwordResetError = null
        authError = null
        return true
    }

    fun completePasswordReset(newPassword: String, confirmPassword: String) {
        val token = recoveryAccessToken
        if (token.isNullOrBlank()) {
            resetPasswordError("This reset link is invalid or expired. Request a new one.")
            return
        }
        if (newPassword.length < 6) {
            passwordResetError = "Use a password with at least six characters."
            return
        }
        if (newPassword != confirmPassword) {
            passwordResetError = "The new passwords do not match."
            return
        }
        passwordResetBusy = true
        passwordResetError = null
        scope.launch {
            try {
                updatePassword(token, newPassword)
                recoveryAccessToken = null
                passwordRecoveryActive = false
                passwordResetSuccess = true
                passwordResetEmailSent = false
                passwordResetError = null
                authError = null
                // The recovery token is not a normal app session. Clear any old
                // session so the next sign-in uses the new password.
                runCatching { supabase?.auth?.signOut() }
            } catch (error: Throwable) {
                passwordResetError = resetMessage(error)
            } finally {
                passwordResetBusy = false
            }
        }
    }

    fun cancelPasswordRecovery() {
        recoveryAccessToken = null
        passwordRecoveryActive = false
        passwordResetBusy = false
        passwordResetError = null
    }

    fun clearPasswordResetState() {
        passwordResetEmailSent = false
        passwordResetSuccess = false
        passwordResetError = null
        authError = null
    }

    private fun resetPasswordError(message: String) {
        recoveryAccessToken = null
        passwordRecoveryActive = false
        passwordResetEmailSent = false
        passwordResetSuccess = false
        passwordResetError = message
        authError = message
    }

    private suspend fun updatePassword(accessToken: String, newPassword: String) =
        withContext(Dispatchers.IO) {
            val connection = (
                URL(BuildConfig.SUPABASE_URL.trimEnd('/') + "/auth/v1/user")
                    .openConnection() as HttpURLConnection
                ).apply {
                requestMethod = "PUT"
                connectTimeout = 15_000
                readTimeout = 20_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("apikey", BuildConfig.SUPABASE_ANON_KEY)
                setRequestProperty("Authorization", "Bearer $accessToken")
            }
            try {
                connection.outputStream.use { output ->
                    output.write(
                        buildJsonObject { put("password", newPassword) }
                            .toString()
                            .toByteArray(Charsets.UTF_8),
                    )
                }
                val code = connection.responseCode
                if (code !in 200..299) {
                    val body = connection.errorStream
                        ?.bufferedReader()
                        ?.use { it.readText() }
                        .orEmpty()
                    val serverMessage = runCatching {
                        AppJson.parseToJsonElement(body).jsonObject["message"]
                            ?.jsonPrimitive
                            ?.content
                    }.getOrNull().orEmpty()
                    error(serverMessage.ifBlank { "reset_http_$code" })
                }
            } finally {
                connection.disconnect()
            }
        }

    private fun resetMessage(error: Throwable): String {
        val raw = error.message.orEmpty()
        return when {
            raw.contains("weak_password", true) ||
                raw.contains("Password should be", true) ->
                "Use a stronger password with at least six characters."
            raw.contains("expired", true) ||
                raw.contains("invalid", true) ||
                raw.contains("JWT", true) ||
                raw.contains("401", true) ->
                "This reset link is invalid or expired. Request a new one."
            raw.contains("timeout", true) || raw.contains("connect", true) ->
                "Can't reach the account service. Check your connection."
            else -> "Could not update your password. Request a new link and try again."
        }
    }

    fun signIn(email: String, password: String) {
        val client = supabase ?: return
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || password.isBlank()) {
            authError = "Enter your email and password."
            return
        }
        authBusy = true
        authError = null
        awaitingEmailConfirmation = false
        scope.launch {
            try {
                client.auth.signInWith(Email) {
                    this.email = cleanEmail
                    this.password = password
                }
            } catch (error: Throwable) {
                authError = authMessage(error)
            } finally {
                authBusy = false
            }
        }
    }

    fun signUp(name: String, email: String, password: String) {
        val client = supabase ?: return
        val cleanName = name.trim()
        val cleanEmail = email.trim()
        if (cleanName.length < 2) {
            authError = "Choose a name with at least two characters."
            return
        }
        if (cleanName.length > 32) {
            authError = "Keep your name to 32 characters or fewer."
            return
        }
        if (cleanEmail.isBlank() || password.length < 6) {
            authError = "Use a valid email and a password with at least six characters."
            return
        }
        authBusy = true
        authError = null
        awaitingEmailConfirmation = false
        scope.launch {
            try {
                client.auth.signUpWith(Email, redirectUrl = null) {
                    this.email = cleanEmail
                    this.password = password
                    data = buildJsonObject {
                        put("display_name", cleanName)
                    }
                }
                if (client.auth.currentSessionOrNull() == null) {
                    awaitingEmailConfirmation = true
                }
            } catch (error: Throwable) {
                authError = authMessage(error)
            } finally {
                authBusy = false
            }
        }
    }

    fun signOut() {
        val client = supabase ?: return
        endSessionAndReset()
        scope.launch {
            runCatching { client.auth.signOut() }
        }
    }

    fun refreshHub() {
        if (!signedIn || supabase == null) return
        refreshJob?.cancel()
        refreshJob = scope.launch {
            hubLoading = true
            try {
                syncProfile()
                if (!signedIn) return@launch
                loadProfile()
                updateQueueCount()
                if (progressStore.weeklyAttempted) {
                    submitWeeklyScore(progressStore.weeklyScore)
                }
            } catch (error: Throwable) {
                if (signedIn) actionError = friendlyError(error)
            } finally {
                if (signedIn) hubLoading = false
            }
        }
    }

    fun clearActionError() {
        actionError = null
    }

    fun refreshWeeklyLeaderboard() {
        if (!signedIn) {
            weeklyEntries = emptyList()
            return
        }
        scope.launch {
            weeklyLoading = true
            try {
                runCatching { syncProfile() }
                weeklyEntries = supabase?.postgrest?.rpc("weekly_leaderboard")
                    ?.decodeList<WeeklyDto>()
                    ?.map { it.toModel() }
                    .orEmpty()
            } catch (_: Throwable) {
                weeklyEntries = emptyList()
            } finally {
                weeklyLoading = false
            }
        }
    }

    fun submitWeeklyScore(score: Int) {
        if (!signedIn) return
        scope.launch {
            runCatching { syncProfile() }
            runCatching {
                supabase?.postgrest?.rpc(
                    "submit_weekly_score",
                    buildJsonObject { put("p_score", score.coerceIn(0, LingoLevels.WEEKLY_QUESTION_COUNT)) },
                )
            }
            refreshWeeklyLeaderboard()
        }
    }

    private suspend fun syncProfile() {
        val client = supabase ?: return
        client.postgrest.rpc(
            "sync_profile",
            buildJsonObject {
                put("p_level", progressStore.cefrLevel.name)
            },
        )
    }

    private suspend fun loadProfile() {
        val client = supabase ?: return
        profile = client.postgrest.rpc("my_profile").decodeSingleOrNull<ProfileDto>()?.toModel()
    }

    private suspend fun updateQueueCount() {
        val client = supabase ?: return
        queueCount = runCatching {
            client.postgrest.rpc("queue_size").decodeAs<Int>()
        }.getOrDefault(0)
    }

    // ---------------------------------------------------------------------
    // Matching and friend calls
    // ---------------------------------------------------------------------

    fun startMatch() {
        if (!signedIn) {
            actionError = "Sign in to find a speaking partner."
            return
        }
        if (phase == CallPhase.SEARCHING || phase == CallPhase.RINGING ||
            phase == CallPhase.CONNECTING || phase == CallPhase.CONNECTED
        ) return

        resetForNewCall()
        phase = CallPhase.SEARCHING
        actionError = null
        searchJob = scope.launch {
            try {
                syncProfile()
                updateQueueCount()
            } catch (_: Throwable) {
                // The matching loop below will surface a real connection error.
            }
            runSearchLoop()
        }
    }

    private suspend fun runSearchLoop() {
        val client = supabase ?: return
        while (scope.isActive && signedIn && phase == CallPhase.SEARCHING) {
            try {
                val dto = client.postgrest.rpc("claim_match").decodeSingleOrNull<CallDto>()
                if (dto != null) {
                    if (phase != CallPhase.SEARCHING) return
                    val found = dto.toModel()
                    session = found
                    if (found.isRinging && !found.isCaller) {
                        beginRinging()
                    } else if (found.isActive) {
                        beginConnecting()
                    } else {
                        beginRinging()
                    }
                    return
                }
                updateQueueCount()
            } catch (error: Throwable) {
                actionError = friendlyError(error)
            }
            delay(2_000)
        }
    }

    fun cancelSearch() {
        if (phase != CallPhase.SEARCHING) return
        searchJob?.cancel()
        phase = CallPhase.IDLE
        session = null
        scope.launch {
            runCatching {
                supabase?.postgrest?.rpc("leave_call_queue")
            }
        }
    }

    fun startFriendCall(rawCode: String) {
        val client = supabase ?: return
        if (!signedIn) {
            actionError = "Sign in to call a friend."
            return
        }
        if (phase == CallPhase.SEARCHING || phase == CallPhase.RINGING ||
            phase == CallPhase.CONNECTING || phase == CallPhase.CONNECTED
        ) return

        val code = normalizeFriendCode(rawCode)
        if (code.length != 6) {
            actionError = "Friend codes have six letters or numbers."
            return
        }

        actionBusy = true
        actionError = null
        scope.launch {
            try {
                val dto = client.postgrest.rpc(
                    "start_friend_call",
                    buildJsonObject { put("p_code", code) },
                ).decodeSingleOrNull<CallDto>() ?: error("The call could not be started.")
                resetForNewCall()
                session = dto.toModel()
                beginRinging()
            } catch (error: Throwable) {
                actionError = friendlyError(error)
            } finally {
                actionBusy = false
            }
        }
    }

    fun acceptIncoming() {
        val client = supabase ?: return
        val current = session ?: return
        if (phase != CallPhase.RINGING || current.isCaller || actionBusy) return
        actionBusy = true
        callError = null
        scope.launch {
            try {
                val dto = client.postgrest.rpc(
                    "accept_call",
                    buildJsonObject { put("p_session", current.id) },
                ).decodeSingleOrNull<CallDto>() ?: error("This call has already ended.")
                session = dto.toModel()
                beginConnecting()
            } catch (error: Throwable) {
                callError = friendlyError(error)
                phase = CallPhase.ERROR
                disconnectRoom()
                scope.launch {
                    runCatching {
                        client.postgrest.rpc(
                            "end_call",
                            buildJsonObject {
                                put("p_session", current.id)
                                put("p_reason", "disconnected")
                            },
                        )
                    }
                }
            } finally {
                actionBusy = false
            }
        }
    }

    fun declineIncoming() {
        val client = supabase ?: return
        val current = session ?: return
        scope.launch {
            val declined = runCatching {
                client.postgrest.rpc(
                    "decline_call",
                    buildJsonObject { put("p_session", current.id) },
                )
            }.isSuccess
            if (!declined) {
                runCatching {
                    client.postgrest.rpc(
                        "end_call",
                        buildJsonObject {
                            put("p_session", current.id)
                            put("p_reason", "declined")
                        },
                    )
                }
            }
            resetForNewCall()
        }
    }

    private fun beginRinging() {
        phase = CallPhase.RINGING
        ringTimeoutJob?.cancel()
        ringTimeoutJob = scope.launch {
            delay(50_000)
            if (phase != CallPhase.RINGING) return@launch
            val current = session ?: return@launch
            val caller = current.isCaller
            scope.launch {
                runCatching {
                    supabase?.postgrest?.rpc(
                        "end_call",
                        buildJsonObject {
                            put("p_session", current.id)
                            put("p_reason", "no_answer")
                        },
                    )
                }
            }
            disconnectRoom()
            if (caller) {
                callError = "No answer. Try another partner."
                phase = CallPhase.ERROR
            } else {
                resetForNewCall()
            }
        }
    }

    private fun beginConnecting() {
        if (session == null) return
        if (phase == CallPhase.CONNECTING || phase == CallPhase.CONNECTED) return
        ringTimeoutJob?.cancel()
        phase = CallPhase.CONNECTING
        connected = false
        callError = null
    }

    /**
     * Called by the call screen once RECORD_AUDIO has been granted. Keeping
     * the permission prompt in the UI lets the store stay free of Activity
     * plumbing while still never opening a microphone without permission.
     */
    fun connectRoom() {
        val current = session ?: return
        if (phase != CallPhase.CONNECTING || room != null || roomConnectInFlight) return
        val generation = ++connectGeneration
        roomConnectInFlight = true
        scope.launch {
            try {
                val token = fetchLiveKitToken(current.id)
                if (generation != connectGeneration ||
                    phase != CallPhase.CONNECTING || session?.id != current.id
                ) return@launch
                val newRoom = LiveKit.create(
                    appContext,
                    RoomOptions(
                        audioTrackCaptureDefaults = LocalAudioTrackOptions(
                            echoCancellation = true,
                            noiseSuppression = true,
                            autoGainControl = true,
                        ),
                    ),
                )
                room = newRoom
                roomEventsJob?.cancel()
                roomEventsJob = scope.launch {
                    newRoom.events.collect { event -> handleRoomEvent(event) }
                }

                newRoom.connect(token.url, token.token)
                if (newRoom.remoteParticipants.isNotEmpty()) {
                    markConnected()
                } else {
                    startConnectTimeout()
                }

                val micPublished = newRoom.localParticipant.setMicrophoneEnabled(true)
                micOn = micPublished
                if (!micPublished && phase == CallPhase.CONNECTING) {
                    callError = "Your microphone could not be started."
                    phase = CallPhase.ERROR
                    disconnectRoom()
                }
            } catch (error: Throwable) {
                if (generation == connectGeneration &&
                    phase == CallPhase.CONNECTING && session?.id == current.id
                ) {
                    callError = friendlyError(error)
                    phase = CallPhase.ERROR
                    disconnectRoom()
                    scope.launch {
                        runCatching {
                            supabase?.postgrest?.rpc(
                                "end_call",
                                buildJsonObject {
                                    put("p_session", current.id)
                                    put("p_reason", "disconnected")
                                },
                            )
                        }
                    }
                }
            } finally {
                if (generation == connectGeneration) roomConnectInFlight = false
            }
        }
    }

    fun microphoneDenied() {
        if (phase != CallPhase.CONNECTING && phase != CallPhase.CONNECTED) return
        val current = session
        callError = "Microphone access is needed for a live call."
        phase = CallPhase.ERROR
        disconnectRoom()
        if (current != null) {
            scope.launch {
                runCatching {
                    supabase?.postgrest?.rpc(
                        "end_call",
                        buildJsonObject {
                            put("p_session", current.id)
                            put("p_reason", "disconnected")
                        },
                    )
                }
            }
        }
    }

    private fun markConnected() {
        if (phase != CallPhase.CONNECTING && phase != CallPhase.CONNECTED) return
        if (phase == CallPhase.CONNECTED) return
        connectTimeoutJob?.cancel()
        connected = true
        phase = CallPhase.CONNECTED
        startTimer()
    }

    private fun startConnectTimeout() {
        connectTimeoutJob?.cancel()
        connectTimeoutJob = scope.launch {
            delay(30_000)
            if (phase == CallPhase.CONNECTING) {
                val current = session
                callError = "Your partner did not join. Please try another call."
                phase = CallPhase.ERROR
                disconnectRoom()
                if (current != null) {
                    runCatching {
                        supabase?.postgrest?.rpc(
                            "end_call",
                            buildJsonObject {
                                put("p_session", current.id)
                                put("p_reason", "disconnected")
                            },
                        )
                    }
                }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        callSeconds = 0
        timerJob = scope.launch {
            while (isActive) {
                delay(1_000)
                callSeconds += 1
            }
        }
    }

    fun setMicEnabled(enabled: Boolean) {
        val activeRoom = room ?: return
        scope.launch {
            try {
                val published = activeRoom.localParticipant.setMicrophoneEnabled(enabled)
                micOn = if (enabled) published else false
            } catch (error: Throwable) {
                callError = "Could not change the microphone."
            }
        }
    }

    fun sendReaction(reaction: CallReaction) {
        val activeRoom = room ?: return
        myReaction = reaction
        reactionClearJob?.cancel()
        reactionClearJob = scope.launch {
            delay(1_600)
            if (myReaction == reaction) myReaction = null
        }
        scope.launch {
            activeRoom.localParticipant.publishData(
                data = reaction.name.toByteArray(Charsets.UTF_8),
                reliability = DataPublishReliability.RELIABLE,
                topic = REACTION_TOPIC,
            )
        }
    }

    private fun handleRoomEvent(event: RoomEvent) {
        when (event) {
            is RoomEvent.ParticipantConnected -> markConnected()
            is RoomEvent.ParticipantDisconnected -> {
                if (phase == CallPhase.CONNECTED) completeFromRemoteEnd()
            }
            is RoomEvent.DataReceived -> {
                if (event.topic == REACTION_TOPIC) {
                    val name = runCatching { event.data.toString(Charsets.UTF_8) }.getOrNull()
                    val incoming = CallReaction.entries.firstOrNull { it.name == name } ?: return
                    partnerReaction = incoming
                    partnerReactionClearJob?.cancel()
                    partnerReactionClearJob = scope.launch {
                        delay(2_400)
                        if (partnerReaction == incoming) partnerReaction = null
                    }
                }
            }
            is RoomEvent.Disconnected -> {
                if (phase == CallPhase.CONNECTING || phase == CallPhase.CONNECTED) {
                    completeFromRemoteEnd()
                }
            }
            is RoomEvent.FailedToConnect -> {
                if (phase == CallPhase.CONNECTING || phase == CallPhase.CONNECTED) {
                    val current = session
                    callError = "The audio connection failed. Please try again."
                    phase = CallPhase.ERROR
                    disconnectRoom()
                    if (current != null) {
                        scope.launch {
                            runCatching {
                                supabase?.postgrest?.rpc(
                                    "end_call",
                                    buildJsonObject {
                                        put("p_session", current.id)
                                        put("p_reason", "disconnected")
                                    },
                                )
                            }
                        }
                    }
                }
            }
            else -> Unit
        }
    }

    // ---------------------------------------------------------------------
    // Ending, safety, ratings
    // ---------------------------------------------------------------------

    private suspend fun endOnServer(sessionId: String, reason: String) {
        val client = supabase ?: error("Sign in again.")
        var lastError: Throwable? = null
        repeat(3) { attempt ->
            try {
                client.postgrest.rpc(
                    "end_call",
                    buildJsonObject {
                        put("p_session", sessionId)
                        put("p_reason", reason)
                    },
                )
                return
            } catch (error: Throwable) {
                lastError = error
                if (attempt < 2) delay(500L * (attempt + 1))
            }
        }
        throw lastError ?: error("The call could not be ended.")
    }

    fun endCall() {
        val current = session ?: return
        if (phase == CallPhase.SUMMARY || phase == CallPhase.ERROR) return
        val duration = callSeconds
        val ended = current.copy(status = "ended")
        lastSummary = CallSummary(ended, duration, reported = false)
        session = ended
        phase = CallPhase.SUMMARY
        disconnectRoom()
        scope.launch {
            runCatching {
                endOnServer(current.id, "ended")
            }.onFailure {
                actionError = "The call is ending. Tap a star or Done to retry."
            }
        }
    }

    fun cancelOutgoing() {
        val current = session ?: return
        scope.launch {
            runCatching {
                supabase?.postgrest?.rpc(
                    "end_call",
                    buildJsonObject {
                        put("p_session", current.id)
                        put("p_reason", "ended")
                    },
                )
            }
            resetForNewCall()
        }
    }

    fun skipAndFindNext() {
        val current = session ?: return
        if (phase == CallPhase.SUMMARY || phase == CallPhase.ERROR) {
            startMatch()
            return
        }
        disconnectRoom()
        lastSummary = null
        session = null
        callSeconds = 0
        ratedStars = 0
        reported = false
        callError = null
        phase = CallPhase.SEARCHING
        searchJob = scope.launch {
            runCatching {
                supabase?.postgrest?.rpc(
                    "end_call",
                    buildJsonObject {
                        put("p_session", current.id)
                        put("p_reason", "skipped")
                    },
                )
            }
            runSearchLoop()
        }
    }

    fun reportCall(reason: CallReportReason) {
        val current = session ?: return
        if (phase == CallPhase.ERROR) return
        scope.launch {
            actionBusy = true
            var sent = false
            try {
                supabase?.postgrest?.rpc(
                    "report_call",
                    buildJsonObject {
                        put("p_session", current.id)
                        put("p_reason", reason.wire)
                    },
                )
                sent = true
            } catch (error: Throwable) {
                actionError = friendlyError(error)
                // A report must never strand the other learner in a live room.
                // If the report request failed, end the session anyway.
                runCatching {
                    supabase?.postgrest?.rpc(
                        "end_call",
                        buildJsonObject {
                            put("p_session", current.id)
                            put("p_reason", "ended")
                        },
                    )
                }
            } finally {
                val ended = current.copy(status = "ended")
                lastSummary = CallSummary(ended, callSeconds, reported = sent)
                session = ended
                reported = sent
                phase = CallPhase.SUMMARY
                disconnectRoom()
                actionBusy = false
            }
        }
    }

    fun rateCall(stars: Int) {
        val current = lastSummary?.session ?: return
        if (stars !in 1..5 || ratedStars > 0) return
        ratedStars = stars
        actionError = null
        scope.launch {
            try {
                val client = supabase ?: error("Sign in again.")
                val rate = suspend {
                    client.postgrest.rpc(
                        "rate_call",
                        buildJsonObject {
                            put("p_session", current.id)
                            put("p_stars", stars)
                        },
                    ).decodeSingleOrNull<RateDto>()
                }
                val dto = try {
                    rate()
                } catch (error: Throwable) {
                    if (error.message.orEmpty().contains("finished call", ignoreCase = true)) {
                        // The End call request can still be in flight when the
                        // summary appears. Make the server state final, then
                        // retry the rating once.
                        endOnServer(current.id, "ended")
                        rate()
                    } else {
                        throw error
                    }
                }
                if (dto != null) {
                    val updatedPeer = current.peer.copy(
                        rating = PeerRating(dto.average, dto.ratingCount),
                    )
                    lastSummary = lastSummary?.copy(session = current.copy(peer = updatedPeer))
                }
            } catch (error: Throwable) {
                ratedStars = 0
                actionError = friendlyError(error)
            }
        }
    }

    fun endSessionAndReset() {
        val current = session
        if (phase == CallPhase.SEARCHING) {
            scope.launch {
                runCatching {
                    supabase?.postgrest?.rpc("leave_call_queue")
                }
            }
        }
        if (current != null) {
            scope.launch {
                // Idempotent: retry is safe when the call already finished, and
                // repairs the server state if the original End call request was
                // lost while the user was looking at the rating summary.
                runCatching { endOnServer(current.id, "ended") }
            }
        }
        disconnectRoom()
        resetForNewCall()
    }

    private fun completeFromRemoteEnd() {
        val current = session ?: return
        if (phase == CallPhase.SUMMARY || phase == CallPhase.ERROR) return
        val ended = current.copy(status = "ended")
        lastSummary = CallSummary(ended, callSeconds, reported = false)
        session = ended
        phase = CallPhase.SUMMARY
        disconnectRoom()
        scope.launch {
            runCatching {
                supabase?.postgrest?.rpc(
                    "end_call",
                    buildJsonObject {
                        put("p_session", current.id)
                        put("p_reason", "disconnected")
                    },
                )
            }
        }
    }

    private fun finishLocalCall(message: String?) {
        callError = message
        phase = CallPhase.ERROR
        disconnectRoom()
    }

    private fun resetForNewCall() {
        searchJob?.cancel()
        timerJob?.cancel()
        connectTimeoutJob?.cancel()
        ringTimeoutJob?.cancel()
        timerJob = null
        connectTimeoutJob = null
        ringTimeoutJob = null
        callSeconds = 0
        connected = false
        session = null
        lastSummary = null
        phase = CallPhase.IDLE
        ratedStars = 0
        reported = false
        callError = null
        reactionClearJob?.cancel()
        partnerReactionClearJob?.cancel()
        reactionClearJob = null
        partnerReactionClearJob = null
        myReaction = null
        partnerReaction = null
        micOn = true
    }

    fun resetLocalState() {
        endSessionAndReset()
        actionError = null
    }

    private fun disconnectRoom() {
        connectGeneration += 1
        roomEventsJob?.cancel()
        roomEventsJob = null
        roomConnectInFlight = false
        val activeRoom = room
        room = null
        runCatching { activeRoom?.disconnect() }
        runCatching { activeRoom?.release() }
        timerJob?.cancel()
        timerJob = null
        connectTimeoutJob?.cancel()
        connectTimeoutJob = null
        ringTimeoutJob?.cancel()
        ringTimeoutJob = null
        connected = false
    }

    // ---------------------------------------------------------------------
    // Polling
    // ---------------------------------------------------------------------

    private fun startPolling() {
        if (pollJob?.isActive == true) return
        pollJob = scope.launch {
            while (isActive && signedIn) {
                runCatching { pollCallState() }
                delay(2_000)
            }
        }
    }

    private suspend fun pollCallState() {
        val dto = supabase?.postgrest?.rpc("my_call_state")
            ?.decodeSingleOrNull<CallDto>()
        if (dto == null) {
            onNoRemoteCall()
        } else {
            onRemoteCall(dto.toModel())
        }
    }

    private fun onRemoteCall(remote: LiveSession) {
        when (phase) {
            CallPhase.IDLE -> {
                session = remote
                if (remote.isRinging) {
                    beginRinging()
                } else if (remote.isActive) {
                    beginConnecting()
                }
            }
            CallPhase.SEARCHING -> {
                if (remote.isRinging && !remote.isCaller) {
                    searchJob?.cancel()
                    session = remote
                    beginRinging()
                } else if (remote.isActive) {
                    searchJob?.cancel()
                    session = remote
                    beginConnecting()
                }
            }
            CallPhase.RINGING -> {
                session = remote
                if (remote.isActive) beginConnecting()
            }
            CallPhase.CONNECTING, CallPhase.CONNECTED -> {
                session = remote
                if (!remote.isActive) completeFromRemoteEnd()
            }
            CallPhase.SUMMARY, CallPhase.ERROR -> Unit
        }
    }

    private fun onNoRemoteCall() {
        when (phase) {
            CallPhase.RINGING -> {
                if (session?.isCaller == true) {
                    val current = session
                    session = null
                    callError = "The call ended before it connected."
                    phase = CallPhase.ERROR
                    disconnectRoom()
                } else {
                    session = null
                    phase = CallPhase.IDLE
                }
            }
            CallPhase.CONNECTING, CallPhase.CONNECTED -> completeFromRemoteEnd()
            else -> Unit
        }
    }

    // ---------------------------------------------------------------------
    // Networking helpers
    // ---------------------------------------------------------------------

    private suspend fun fetchLiveKitToken(sessionId: String): TokenDto = withContext(Dispatchers.IO) {
        val token = supabase?.auth?.currentAccessTokenOrNull()
            ?: error("Your session expired. Sign in again.")
        val url = URL(BuildConfig.SUPABASE_URL.trimEnd('/') + "/functions/v1/livekit-token")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 20_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("apikey", BuildConfig.SUPABASE_ANON_KEY)
            setRequestProperty("Authorization", "Bearer $token")
        }
        try {
            connection.outputStream.use { output ->
                output.write(
                    buildJsonObject { put("session_id", sessionId) }
                        .toString()
                        .toByteArray(Charsets.UTF_8),
                )
            }
            val code = connection.responseCode
            val body = (if (code in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()
            if (code !in 200..299) {
                val message = runCatching {
                    AppJson.parseToJsonElement(body).jsonObject["error"]?.jsonPrimitive?.content
                }.getOrNull()
                error(message ?: "Could not reach the call service.")
            }
            AppJson.decodeFromString<TokenDto>(body)
        } finally {
            connection.disconnect()
        }
    }

    private fun authMessage(error: Throwable): String {
        val raw = error.message.orEmpty()
        return when {
            raw.contains("invalid_credentials", true) ||
                raw.contains("Invalid login credentials", true) ->
                "Email or password is incorrect."
            raw.contains("Email not confirmed", true) ||
                raw.contains("email_not_confirmed", true) ->
                "Confirm your email, then sign in."
            raw.contains("already registered", true) ||
                raw.contains("user_already_exists", true) ->
                "That email already has an account. Sign in instead."
            raw.contains("weak_password", true) ||
                raw.contains("Password should be", true) ->
                "Use a stronger password with at least six characters."
            raw.contains("Unable to validate email", true) ->
                "Enter a valid email address."
            raw.contains("For security purposes", true) ||
                raw.contains("rate limit", true) ||
                raw.contains("429", true) ->
                "Please wait a minute before requesting another email."
            raw.contains("User not found", true) ||
                raw.contains("user_not_found", true) ->
                "If that email has an account, check your inbox."
            raw.contains("timeout", true) ->
                "The account service took too long. Try again."
            raw.isBlank() -> "Something went wrong. Please try again."
            else -> "Something went wrong. Please try again."
        }
    }

    private fun friendlyError(error: Throwable): String {
        val raw = error.message.orEmpty().trim()
        return when {
            raw.contains("offline", true) -> "That learner is offline right now."
            raw.contains("another call", true) -> "That learner is on another call."
            raw.contains("Friend codes", true) -> raw
            raw.contains("No learner", true) -> raw
            raw.contains("blocked", true) -> raw
            raw.contains("already in a call", true) -> raw
            raw.contains("JWT", true) || raw.contains("session", true) ||
                raw.contains("401", true) || raw.contains("permission denied", true) ->
                "Your session expired. Sign in again."
            raw.contains("finished call", true) ->
                "The call is still finishing. Tap a star again."
            raw.contains("timeout", true) || raw.contains("connect", true) ->
                "Can't reach the call service. Check your connection."
            raw.isBlank() -> "Something went wrong. Please try again."
            // Never surface raw PostgREST text here: it can contain request
            // URLs and Authorization headers.
            else -> "Something went wrong. Please try again."
        }
    }

    fun dispose() {
        searchJob?.cancel()
        pollJob?.cancel()
        disconnectRoom()
        scope.cancel()
        supabase?.let { client ->
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                runCatching { client.close() }
            }
        }
    }
}

private val AppJson = Json { ignoreUnknownKeys = true }

@Serializable
private data class ProfileDto(
    @SerialName("display_name") val displayName: String = "Learner",
    @SerialName("friend_code") val friendCode: String = "",
    @SerialName("cefr_level") val level: String = "A1",
    val rating: Double = 0.0,
    @SerialName("rating_count") val ratingCount: Int = 0,
    @SerialName("calls_completed") val callsCompleted: Int = 0,
    @SerialName("minutes_talked") val minutesTalked: Int = 0,
    @SerialName("avatar_sticker") val avatar: String = "SPROUT",
    val country: String? = null,
) {
    fun toModel() = MyProfile(
        displayName = displayName,
        friendCode = friendCode,
        level = CefrLevel.fromKey(level),
        rating = PeerRating(rating, ratingCount),
        callsCompleted = callsCompleted,
        minutesTalked = minutesTalked,
        avatar = avatar,
        country = country,
    )
}

@Serializable
private data class CallDto(
    @SerialName("session_id") val sessionId: String = "",
    val status: String = "",
    @SerialName("call_kind") val callKind: String = "friend",
    @SerialName("is_caller") val isCaller: Boolean = false,
    @SerialName("partner_id") val partnerId: String = "",
    @SerialName("partner_name") val partnerName: String = "Learner",
    @SerialName("partner_level") val partnerLevel: String = "A1",
    @SerialName("partner_avatar") val partnerAvatar: String = "SPROUT",
    @SerialName("partner_rating") val partnerRating: Double = 0.0,
    @SerialName("partner_rating_count") val partnerRatingCount: Int = 0,
    @SerialName("partner_country") val partnerCountry: String? = null,
    @SerialName("room_name") val roomName: String = "",
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
) {
    fun toModel() = LiveSession(
        id = sessionId,
        roomName = roomName,
        status = status,
        callKind = callKind,
        isCaller = isCaller,
        peer = LivePeer(
            id = partnerId,
            name = partnerName,
            level = CefrLevel.fromKey(partnerLevel),
            avatar = partnerAvatar,
            country = partnerCountry,
            rating = PeerRating(partnerRating, partnerRatingCount),
        ),
    )
}

@Serializable
private data class WeeklyDto(
    @SerialName("user_id") val userId: String = "",
    @SerialName("display_name") val displayName: String = "Learner",
    val country: String? = null,
    @SerialName("cefr_level") val level: String = "A1",
    val score: Int = 0,
    @SerialName("is_you") val isYou: Boolean = false,
) {
    fun toModel() = WeeklyEntry(
        userId = userId,
        displayName = displayName,
        country = country,
        level = CefrLevel.fromKey(level),
        score = score,
        isYou = isYou,
    )
}

@Serializable
private data class RateDto(
    val average: Double = 0.0,
    @SerialName("rating_count") val ratingCount: Int = 0,
)

@Serializable
private data class TokenDto(
    val url: String,
    val token: String,
)
