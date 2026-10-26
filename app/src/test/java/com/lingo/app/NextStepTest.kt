package com.lingo.app

import com.lingo.app.data.CefrLevel
import com.lingo.app.data.LingoContent
import com.lingo.app.data.NextStepKind
import com.lingo.app.data.NextStepPlanner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NextStepTest {

    private val lesson = LingoContent.lessons.first()

    @Test
    fun `hides the card when the daily goal is complete`() {
        val step = NextStepPlanner.recommend(
            weeklyDone = false,
            examTarget = CefrLevel.A2,
            examReady = true,
            nextLesson = lesson,
            allLessonsComplete = false,
            goalComplete = true,
        )
        assertNull(step)
    }

    @Test
    fun `weekly check-in comes before everything else`() {
        val step = NextStepPlanner.recommend(
            weeklyDone = false,
            examTarget = CefrLevel.A2,
            examReady = true,
            nextLesson = lesson,
            allLessonsComplete = false,
            goalComplete = false,
        )
        assertEquals(NextStepKind.WEEKLY_TEST, step?.kind)
        assertEquals("Start weekly test", step?.action)
    }

    @Test
    fun `an eligible exam is recommended once the week is done`() {
        val step = NextStepPlanner.recommend(
            weeklyDone = true,
            examTarget = CefrLevel.A2,
            examReady = true,
            nextLesson = lesson,
            allLessonsComplete = false,
            goalComplete = false,
        )
        assertEquals(NextStepKind.LEVEL_EXAM, step?.kind)
        assertEquals(CefrLevel.A2, step?.targetLevel)
        assertEquals("Take the exam", step?.action)
    }

    @Test
    fun `the next lesson is returned but Home hides it behind the hero`() {
        val step = NextStepPlanner.recommend(
            weeklyDone = true,
            examTarget = null,
            examReady = false,
            nextLesson = lesson,
            allLessonsComplete = false,
            goalComplete = false,
        )
        assertEquals(NextStepKind.NEXT_LESSON, step?.kind)
        assertEquals(lesson.id, step?.lessonId)
    }

    @Test
    fun `a finished path recommends free practice`() {
        val step = NextStepPlanner.recommend(
            weeklyDone = true,
            examTarget = null,
            examReady = false,
            nextLesson = null,
            allLessonsComplete = true,
            goalComplete = false,
        )
        assertEquals(NextStepKind.FREE_PRACTICE, step?.kind)
        assertEquals("Open practice", step?.action)
    }

    @Test
    fun `missing content still offers practice instead of crashing`() {
        val step = NextStepPlanner.recommend(
            weeklyDone = true,
            examTarget = null,
            examReady = false,
            nextLesson = null,
            allLessonsComplete = false,
            goalComplete = false,
        )
        assertEquals(NextStepKind.FREE_PRACTICE, step?.kind)
    }
}
