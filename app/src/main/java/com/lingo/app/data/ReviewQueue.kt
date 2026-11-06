package com.lingo.app.data

/**
 * One missed question. [correctStreak] counts consecutive correct review
 * answers; at [ReviewQueue.CLEAR_STREAK] the item leaves the queue.
 */
data class ReviewItem(
    val exerciseId: String,
    val misses: Int,
    val correctStreak: Int,
    val lastSeenAt: Long,
)

/**
 * Pure, local-only Smart Review rules. Keeping this file free of Android types
 * makes the two-right rule, ordering and persistence format unit-testable.
 */
object ReviewQueue {

    const val SESSION_SIZE = 5
    const val CLEAR_STREAK = 2
    const val MAX_ITEMS = 200

    private val idPattern = Regex("^[A-Za-z0-9_-]{1,80}$")
    private const val MAX_MISSES = 9_999

    private fun validId(id: String): Boolean = idPattern.matches(id)

    private fun validItem(item: ReviewItem): Boolean =
        validId(item.exerciseId) &&
            item.misses in 1..MAX_MISSES &&
            item.correctStreak in 0 until CLEAR_STREAK &&
            item.lastSeenAt >= 0

    private fun capped(items: List<ReviewItem>): List<ReviewItem> =
        if (items.size <= MAX_ITEMS) {
            items
        } else {
            items.sortedByDescending { it.lastSeenAt }.take(MAX_ITEMS)
        }

    /** A miss adds the exercise or increments its miss count and resets the streak. */
    fun recordMiss(items: List<ReviewItem>, exerciseId: String, now: Long): List<ReviewItem> {
        if (!validId(exerciseId)) return items
        val seen = now.coerceAtLeast(0)
        val index = items.indexOfFirst { it.exerciseId == exerciseId }
        val updated = if (index >= 0) {
            items.toMutableList().also { list ->
                val old = list[index]
                list[index] = old.copy(
                    misses = (old.misses + 1).coerceAtMost(MAX_MISSES),
                    correctStreak = 0,
                    lastSeenAt = seen,
                )
            }
        } else {
            items + ReviewItem(exerciseId, misses = 1, correctStreak = 0, lastSeenAt = seen)
        }
        return capped(updated)
    }

    /** A correct answer keeps the item for one more rep; the second clears it. */
    fun recordCorrect(items: List<ReviewItem>, exerciseId: String, now: Long): List<ReviewItem> {
        if (!validId(exerciseId)) return items
        val index = items.indexOfFirst { it.exerciseId == exerciseId }
        if (index < 0) return items
        val old = items[index]
        val streak = (old.correctStreak + 1).coerceAtMost(CLEAR_STREAK)
        if (streak >= CLEAR_STREAK) {
            return items.filterNot { it.exerciseId == exerciseId }
        }
        return items.toMutableList().also { list ->
            list[index] = old.copy(correctStreak = streak, lastSeenAt = now.coerceAtLeast(0))
        }
    }

    /**
     * The next session: only IDs still present in bundled content, hardest
     * first (most misses), then least recently seen, then stable ID order.
     */
    fun due(
        items: List<ReviewItem>,
        validIds: Set<String>,
        limit: Int = SESSION_SIZE,
    ): List<ReviewItem> {
        val take = limit.coerceIn(0, MAX_ITEMS)
        if (take == 0) return emptyList()
        return items.asSequence()
            .filter { it.exerciseId in validIds && validItem(it) }
            .distinctBy { it.exerciseId }
            .sortedWith(
                compareByDescending<ReviewItem> { it.misses }
                    .thenBy { it.lastSeenAt }
                    .thenBy { it.exerciseId },
            )
            .take(take)
            .toList()
    }

    /**
     * One item per line: `exerciseId<TAB>misses<TAB>correctStreak<TAB>lastSeenAt`.
     * Invalid lines are ignored rather than crashing a learner's review.
     */
    fun encode(items: List<ReviewItem>): String = items.asSequence()
        .filter(::validItem)
        .distinctBy { it.exerciseId }
        .take(MAX_ITEMS)
        .joinToString("\n") {
            "${it.exerciseId}\t${it.misses}\t${it.correctStreak}\t${it.lastSeenAt}"
        }

    fun decode(raw: String?): List<ReviewItem> {
        if (raw.isNullOrBlank()) return emptyList()
        val decoded = LinkedHashMap<String, ReviewItem>()
        raw.lineSequence().take(MAX_ITEMS).forEach { line ->
            val parts = line.split('\t')
            if (parts.size != 4) return@forEach
            val id = parts[0].trim()
            if (!validId(id)) return@forEach
            val misses = parts[1].toIntOrNull() ?: return@forEach
            val streak = parts[2].toIntOrNull() ?: return@forEach
            val seen = parts[3].toLongOrNull() ?: return@forEach
            val item = ReviewItem(id, misses, streak, seen)
            if (!validItem(item)) return@forEach
            decoded.putIfAbsent(id, item)
        }
        return decoded.values.toList()
    }

    /** Same economy as a practice quiz: 4 XP per correct plus 10 for 80%+. */
    fun reviewXp(correct: Int, total: Int): Int {
        if (correct <= 0 || total <= 0) return 0
        val safeCorrect = correct.coerceAtMost(total)
        val bonus = if (safeCorrect.toFloat() / total >= 0.8f) 10 else 0
        return safeCorrect * 4 + bonus
    }
}
