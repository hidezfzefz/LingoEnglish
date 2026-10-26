package com.lingo.app.data

/** The one action Home should nudge toward when the daily goal is still open. */
enum class NextStepKind { WEEKLY_TEST, LEVEL_EXAM, NEXT_LESSON, FREE_PRACTICE }

data class NextStep(
    val kind: NextStepKind,
    val eyebrow: String,
    val title: String,
    val detail: String,
    val action: String,
    val sticker: StickerKind,
    val targetLevel: CefrLevel? = null,
    val lessonId: String? = null,
)

/**
 * Pure recommendation logic so the priority can be tested without Compose.
 *
 * The Home hero already offers the next lesson, so a [NextStepKind.NEXT_LESSON]
 * result is useful to tests but is not drawn as a second competing card.
 */
object NextStepPlanner {

    fun recommend(
        weeklyDone: Boolean,
        examTarget: CefrLevel?,
        examReady: Boolean,
        nextLesson: Lesson?,
        allLessonsComplete: Boolean,
        goalComplete: Boolean,
    ): NextStep? {
        if (goalComplete) return null

        if (!weeklyDone) {
            return NextStep(
                kind = NextStepKind.WEEKLY_TEST,
                eyebrow = "WEEKLY CHECK-IN",
                title = "Your weekly check-in is ready",
                detail = "${LingoLevels.WEEKLY_QUESTION_COUNT} questions · about ${LingoLevels.WEEKLY_MINUTES} min",
                action = "Start weekly test",
                sticker = StickerKind.CALENDAR,
            )
        }

        if (examReady && examTarget != null) {
            return NextStep(
                kind = NextStepKind.LEVEL_EXAM,
                eyebrow = "LEVEL EXAM",
                title = "Ready for ${examTarget.label}?",
                detail = "${examTarget.title} · ${LingoLevels.LEVEL_QUESTION_COUNT} questions · ${LingoLevels.LEVEL_MINUTES} min",
                action = "Take the exam",
                sticker = StickerKind.TARGET,
                targetLevel = examTarget,
            )
        }

        if (allLessonsComplete) {
            return NextStep(
                kind = NextStepKind.FREE_PRACTICE,
                eyebrow = "KEEP IT WARM",
                title = "The path is complete",
                detail = "A short practice keeps your words and sentences fresh.",
                action = "Open practice",
                sticker = StickerKind.BOLT,
            )
        }

        if (nextLesson != null) {
            return NextStep(
                kind = NextStepKind.NEXT_LESSON,
                eyebrow = "NEXT UP",
                title = "Continue ${nextLesson.title}",
                detail = "${nextLesson.minutes} min · +${nextLesson.xp} XP",
                action = "Continue lesson",
                sticker = nextLesson.icon,
                lessonId = nextLesson.id,
            )
        }

        return NextStep(
            kind = NextStepKind.FREE_PRACTICE,
            eyebrow = "KEEP IT WARM",
            title = "A little practice goes a long way",
            detail = "Choose words, a challenge, or a conversation.",
            action = "Open practice",
            sticker = StickerKind.BOLT,
        )
    }
}
