package com.lingo.app

import com.lingo.app.data.CutscenePolicy
import com.lingo.app.data.LaunchCutsceneMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CutscenePolicyTest {

    @Test
    fun `a fresh install gets the full launch cutscene`() {
        assertEquals(
            LaunchCutsceneMode.FULL,
            CutscenePolicy.launchMode(seenVersion = 0, currentVersion = 10),
        )
    }

    @Test
    fun `an app update gets the full launch cutscene once`() {
        assertEquals(
            LaunchCutsceneMode.FULL,
            CutscenePolicy.launchMode(seenVersion = 9, currentVersion = 10),
        )
    }

    @Test
    fun `a later cold start on the same version gets the short sting`() {
        assertEquals(
            LaunchCutsceneMode.STING,
            CutscenePolicy.launchMode(seenVersion = 10, currentVersion = 10),
        )
    }

    @Test
    fun `a recovery deep link skips the launch cutscene`() {
        assertEquals(
            LaunchCutsceneMode.NONE,
            CutscenePolicy.launchMode(seenVersion = 0, currentVersion = 10, isDeepLink = true),
        )
    }

    @Test
    fun `a new lesson gets its intro`() {
        assertTrue(
            CutscenePolicy.shouldShowLessonIntro(
                lessonId = "u1-l1",
                completedLessons = emptySet(),
                seenIntros = emptySet(),
            ),
        )
    }

    @Test
    fun `a completed lesson skips its intro`() {
        assertFalse(
            CutscenePolicy.shouldShowLessonIntro(
                lessonId = "u1-l1",
                completedLessons = setOf("u1-l1"),
                seenIntros = emptySet(),
            ),
        )
    }

    @Test
    fun `a lesson whose intro was already seen skips it`() {
        assertFalse(
            CutscenePolicy.shouldShowLessonIntro(
                lessonId = "u1-l1",
                completedLessons = emptySet(),
                seenIntros = setOf("u1-l1"),
            ),
        )
    }
}
