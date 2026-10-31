package com.lingo.app

import com.lingo.app.data.CutsceneContent
import com.lingo.app.data.LingoContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CutsceneContentTest {

    @Test
    fun `every lesson has a creative spark`() {
        val missing = LingoContent.lessons
            .filter { CutsceneContent.sparkFor(it.id) == null }
            .map { it.id }

        assertTrue("Lessons without a spark: $missing", missing.isEmpty())
    }

    @Test
    fun `every spark has a prompt and two or three usable chips`() {
        val failures = mutableListOf<String>()
        LingoContent.lessons.forEach { lesson ->
            val spark = CutsceneContent.sparkFor(lesson.id)
            if (spark == null) {
                failures += "${lesson.id} has no spark"
                return@forEach
            }
            if (spark.prompt.isBlank()) failures += "${lesson.id} has a blank prompt"
            if (spark.chips.size !in 2..3) {
                failures += "${lesson.id} has ${spark.chips.size} chips, expected 2-3"
            }
            if (spark.chips.map { it.label }.distinct().size != spark.chips.size) {
                failures += "${lesson.id} has duplicate chip labels"
            }
            spark.chips.forEach { chip ->
                if (chip.label.isBlank()) failures += "${lesson.id} has a blank chip label"
                if (chip.example.isBlank()) failures += "${lesson.id} has a blank chip example"
            }
        }

        assertTrue(failures.joinToString("\n"), failures.isEmpty())
    }

    @Test
    fun `sparks cover exactly the lesson catalogue`() {
        assertEquals(
            LingoContent.lessons.map { it.id }.sorted(),
            CutsceneContent.sparks.keys.sorted(),
        )
    }

    @Test
    fun `an unknown lesson has no spark`() {
        assertNull(CutsceneContent.sparkFor("not-a-lesson"))
    }
}
