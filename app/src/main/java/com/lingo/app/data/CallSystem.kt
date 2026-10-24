package com.lingo.app.data

import java.util.Locale

/**
 * The public call model. There are no simulated partners in the release app:
 * every field here comes from a signed-in Supabase profile and a live
 * LiveKit-backed call session.
 */
data class LivePeer(
    val id: String,
    val name: String,
    val level: CefrLevel,
    val avatar: String,
    val country: String?,
    val rating: PeerRating,
) {
    val sticker: StickerKind
        get() = runCatching { StickerKind.valueOf(avatar.uppercase(Locale.US)) }
            .getOrDefault(StickerKind.HEADPHONES)

    val detail: String
        get() = country?.takeIf { it.isNotBlank() } ?: "Lingo learner"
}

data class PeerRating(
    val average: Double,
    val count: Int,
) {
    val label: String
        get() = if (count == 0) "New" else String.format(Locale.US, "%.1f", average)
}

data class MyProfile(
    val displayName: String,
    val friendCode: String,
    val level: CefrLevel,
    val rating: PeerRating,
    val callsCompleted: Int,
    val minutesTalked: Int,
    val avatar: String,
    val country: String?,
) {
    val ratingLabel: String get() = rating.label
    val displayCode: String get() = formatFriendCode(friendCode)
}

data class LiveSession(
    val id: String,
    val roomName: String,
    val status: String,
    val callKind: String,
    val isCaller: Boolean,
    val peer: LivePeer,
) {
    val isActive: Boolean get() = status == "active"
    val isRinging: Boolean get() = status == "ringing"
}

data class CallSummary(
    val session: LiveSession,
    val seconds: Int,
    val reported: Boolean,
)

data class WeeklyEntry(
    val userId: String,
    val displayName: String,
    val country: String?,
    val level: CefrLevel,
    val score: Int,
    val isYou: Boolean,
)

enum class CallPhase {
    IDLE,
    SEARCHING,
    RINGING,
    CONNECTING,
    CONNECTED,
    SUMMARY,
    ERROR,
}

enum class CallReaction(val label: String, val emoji: String) {
    WAVE("Hello", "👋"),
    LOVE("Love", "❤️"),
    FUN("Funny", "😂"),
    WOW("Wow", "😮"),
    NICE("Nice", "👍"),
    FIRE("Fire", "🔥"),
}

enum class CallReportReason(val label: String, val wire: String) {
    BAD_LANGUAGE("Bad language", "bad_language"),
    HARASSMENT("Harassment", "harassment"),
    SEXUAL("Sexual content", "sexual"),
    THREAT("Threats or violence", "threat"),
    OTHER("Something else", "other"),
}

fun normalizeFriendCode(raw: String): String =
    raw.uppercase(Locale.US).filter { it in 'A'..'Z' || it in '0'..'9' }.take(6)

fun formatFriendCode(raw: String): String {
    val code = normalizeFriendCode(raw)
    return if (code.length == 6) "${code.substring(0, 3)}-${code.substring(3)}" else code
}
