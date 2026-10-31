package com.lingo.app.data

enum class LaunchCutsceneMode { FULL, STING, NONE }

object CutscenePolicy {

    fun launchMode(
        seenVersion: Int,
        currentVersion: Int,
        isDeepLink: Boolean = false,
    ): LaunchCutsceneMode = when {
        isDeepLink -> LaunchCutsceneMode.NONE
        seenVersion < currentVersion -> LaunchCutsceneMode.FULL
        else -> LaunchCutsceneMode.STING
    }

    fun shouldShowLessonIntro(
        lessonId: String,
        completedLessons: Set<String>,
        seenIntros: Set<String>,
    ): Boolean = lessonId !in completedLessons && lessonId !in seenIntros
}
