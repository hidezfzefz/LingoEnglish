package com.lingo.app

import com.lingo.app.data.LingoContent
import com.lingo.app.data.ReviewItem
import com.lingo.app.data.ReviewQueue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewQueueTest {

    private fun item(
        id: String,
        misses: Int = 1,
        streak: Int = 0,
        seen: Long = 0,
    ) = ReviewItem(id, misses, streak, seen)

    @Test
    fun `a miss adds a new item`() {
        val items = ReviewQueue.recordMiss(emptyList(), "u1-l1-e1", 100)
        assertEquals(listOf(item("u1-l1-e1", misses = 1, seen = 100)), items)
    }

    @Test
    fun `another miss increments and resets the streak`() {
        val start = listOf(item("u1-l1-e1", misses = 1, streak = 1, seen = 10))
        val items = ReviewQueue.recordMiss(start, "u1-l1-e1", 200)
        assertEquals(listOf(item("u1-l1-e1", misses = 2, streak = 0, seen = 200)), items)
    }

    @Test
    fun `one correct keeps the item and two correct clears it`() {
        val start = ReviewQueue.recordMiss(emptyList(), "u1-l1-e1", 10)
        val once = ReviewQueue.recordCorrect(start, "u1-l1-e1", 20)
        assertEquals(listOf(item("u1-l1-e1", misses = 1, streak = 1, seen = 20)), once)

        val twice = ReviewQueue.recordCorrect(once, "u1-l1-e1", 30)
        assertTrue(twice.isEmpty())
    }

    @Test
    fun `a later miss starts the streak again`() {
        val start = ReviewQueue.recordMiss(emptyList(), "u1-l1-e1", 10)
        val once = ReviewQueue.recordCorrect(start, "u1-l1-e1", 20)
        val missedAgain = ReviewQueue.recordMiss(once, "u1-l1-e1", 30)
        assertEquals(listOf(item("u1-l1-e1", misses = 2, streak = 0, seen = 30)), missedAgain)
    }

    @Test
    fun `a correct answer on an unknown item changes nothing`() {
        val items = listOf(item("u1-l1-e1", seen = 10))
        assertEquals(items, ReviewQueue.recordCorrect(items, "u1-l1-e2", 20))
    }

    @Test
    fun `due filters unknown ids and sorts by misses then recency`() {
        val items = listOf(
            item("a", misses = 2, seen = 10),
            item("b", misses = 3, seen = 20),
            item("c", misses = 1, seen = 5),
            item("d", misses = 2, seen = 5),
            item("unknown", misses = 99, seen = 1),
        )
        val due = ReviewQueue.due(items, setOf("a", "b", "c", "d"))
        assertEquals(listOf("b", "d", "a", "c"), due.map { it.exerciseId })
    }

    @Test
    fun `due uses the id as a stable final tie break`() {
        val items = listOf(
            item("z", misses = 2, seen = 5),
            item("a", misses = 2, seen = 5),
        )
        val due = ReviewQueue.due(items, setOf("a", "z"))
        assertEquals(listOf("a", "z"), due.map { it.exerciseId })
    }

    @Test
    fun `due limits a session to five by default`() {
        val items = (1..8).map { item("q$it", misses = it) }
        val due = ReviewQueue.due(items, items.map { it.exerciseId }.toSet())
        assertEquals(5, due.size)
        assertEquals(listOf("q8", "q7", "q6", "q5", "q4"), due.map { it.exerciseId })
    }

    @Test
    fun `due excludes malformed and already-cleared items`() {
        val items = listOf(
            item("ok", misses = 1),
            item("cleared", misses = 2, streak = 2),
            item("bad id", misses = 1),
        )
        val due = ReviewQueue.due(items, setOf("ok", "cleared", "bad id"))
        assertEquals(listOf("ok"), due.map { it.exerciseId })
    }

    @Test
    fun `encode and decode round trip`() {
        val items = listOf(
            item("u1-l1-e1", misses = 2, streak = 1, seen = 100),
            item("p-g1", misses = 4, streak = 0, seen = 200),
        )
        assertEquals(items, ReviewQueue.decode(ReviewQueue.encode(items)))
    }

    @Test
    fun `decode ignores malformed lines, duplicates and invalid values`() {
        val raw = listOf(
            "u1-l1-e1\t2\t1\t100",
            "bad line",
            "u1-l1-e2\t0\t0\t100",
            "u1-l1-e3\t1\t2\t100",
            "u1-l1-e4\t1\t0\t-1",
            "u1-l1-e1\t3\t0\t200",
            "u1-l1-e5\t1\t0\t300\textra",
            "bad id\t1\t0\t100",
        ).joinToString("\n")
        assertEquals(
            listOf(item("u1-l1-e1", misses = 2, streak = 1, seen = 100)),
            ReviewQueue.decode(raw),
        )
    }

    @Test
    fun `decode caps input at the maximum item count`() {
        val raw = (0 until 250).joinToString("\n") { "q$it\t1\t0\t$it" }
        val decoded = ReviewQueue.decode(raw)
        assertEquals(ReviewQueue.MAX_ITEMS, decoded.size)
        assertEquals("q0", decoded.first().exerciseId)
        assertEquals("q199", decoded.last().exerciseId)
    }

    @Test
    fun `recordMiss ignores invalid ids`() {
        val items = listOf(item("u1-l1-e1", seen = 10))
        val invalid = listOf("", "bad id", "a\tb", "x".repeat(81))
        invalid.forEach { id ->
            assertEquals(items, ReviewQueue.recordMiss(items, id, 20))
        }
    }

    @Test
    fun `review xp matches the practice quiz economy`() {
        assertEquals(0, ReviewQueue.reviewXp(0, 0))
        assertEquals(0, ReviewQueue.reviewXp(0, 5))
        assertEquals(4, ReviewQueue.reviewXp(1, 5))
        assertEquals(12, ReviewQueue.reviewXp(3, 5))
        assertEquals(26, ReviewQueue.reviewXp(4, 5))
        assertEquals(30, ReviewQueue.reviewXp(5, 5))
        assertEquals(30, ReviewQueue.reviewXp(6, 5))
    }

    @Test
    fun `every reviewable exercise is unique, non-creative and resolves`() {
        val exercises = LingoContent.reviewableExercises
        assertEquals(83, exercises.size)
        assertEquals(exercises.size, exercises.map { it.id }.distinct().size)
        assertTrue(exercises.none { it.isCreative })
        exercises.forEach { exercise ->
            assertEquals(exercise, LingoContent.exerciseById(exercise.id))
        }
    }
}
