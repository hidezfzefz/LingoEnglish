package com.lingo.app.data

/** Hand-drawn sticker motifs used instead of emoji in the interface. */
enum class StickerKind {
    HOME, BOOK, BOLT, CHART, SETTINGS,
    SPROUT, FLAME, SPARKLE, TARGET, TROPHY, STAR,
    CARDS, HEADPHONES, PENCIL, BULB, LAB, THEATER, PUZZLE, SEARCH, CELEBRATION,
    WAVE, COFFEE, COMPASS, PLANE, TAKEOFF, BELL, CHAT, BRIEFCASE, EMAIL, SPEAK,
    HOURGLASS, FLOWER, PILL, MEDITATION, CALENDAR, HOUSE, WINDOW, WALK, RESTAURANT,
    TICKET, MOVIE, SMARTPHONE, PERSON, BALANCE, VOLUME,
}

/** A themed course track shown in the learning path. */
data class Unit(
    val id: String,
    val title: String,
    val subtitle: String,
    val level: String,
    val icon: StickerKind,
    val accent: Accent,
    val lessons: List<Lesson>,
)

enum class Accent { GREEN, CORAL, BLUE, PURPLE, TEAL }

data class Lesson(
    val id: String,
    val unitId: String,
    val title: String,
    val subtitle: String,
    val minutes: Int,
    val xp: Int,
    val icon: StickerKind,
    val slides: List<Slide>,
    val exercises: List<Exercise>,
) {
    val stepCount: Int get() = slides.size + exercises.size
}

data class Slide(
    val eyebrow: String,
    val title: String,
    val body: String,
    val examples: List<Example>,
    val tip: String? = null,
)

data class Example(
    val phrase: String,
    val note: String,
)

enum class ExerciseKind { CHOICE, LISTENING, FILL_BLANK, WRITE }

enum class Topic { VOCABULARY, GRAMMAR, LISTENING, CONVERSATION }

data class Exercise(
    val id: String,
    val kind: ExerciseKind,
    val instruction: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val audioText: String? = null,
    val topic: Topic = Topic.CONVERSATION,
    /** Minimum production for a WRITE task. Other kinds leave this at zero. */
    val minWords: Int = 0,
    /** Success criteria shown before the learner writes. */
    val checklist: List<String> = emptyList(),
    /** A strong example answer revealed after checking. */
    val modelAnswer: String? = null,
    /** Optional sentence openers to reduce blank-page pressure. */
    val starters: List<String> = emptyList(),
) {
    val isCreative: Boolean get() = kind == ExerciseKind.WRITE

    fun wordCount(text: String): Int =
        text.trim().split(Regex("\\s+")).count { token ->
            token.any { it.isLetterOrDigit() }
        }

    /**
     * Multiple-choice items are right or wrong. Creative tasks are credited
     * for meeting the production target; the model answer and checklist then
     * support a quality conversation rather than pretending to auto-grade style.
     */
    fun isAnswerCorrect(selectedIndex: Int?, text: String): Boolean =
        if (isCreative) wordCount(text) >= minWords else selectedIndex == correctIndex
}

data class Word(
    val id: String,
    val word: String,
    val phonetic: String,
    val partOfSpeech: String,
    val definition: String,
    val example: String,
)

/** A short, choice-driven conversation for practising real-world English. */
data class RoleplayScenario(
    val id: String,
    val title: String,
    val subtitle: String,
    val level: String,
    val sticker: StickerKind,
    val setting: String,
    val steps: List<RoleplayStep>,
    val xp: Int = 30,
)

data class RoleplayStep(
    val speaker: String,
    val avatar: StickerKind,
    val line: String,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
)

/** A sentence-building challenge: tap the words into the correct order. */
data class SentencePuzzle(
    val id: String,
    val sticker: StickerKind,
    val prompt: String,
    val tokens: List<String>,
    val answer: String,
    val explanation: String,
)

enum class PracticeMode(
    val title: String,
    val subtitle: String,
    val sticker: StickerKind,
) {
    DAILY("Daily challenge", "A fresh mix of everything", StickerKind.BOLT),
    LISTENING("Listening lab", "Train your ear, one line at a time", StickerKind.HEADPHONES),
    GRAMMAR("Grammar sprint", "Quick drills on the tricky bits", StickerKind.PENCIL),
    VOCABULARY("Word workout", "Grow your everyday vocabulary", StickerKind.BULB),
}
