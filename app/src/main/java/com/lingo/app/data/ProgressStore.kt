package com.lingo.app.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDate
import kotlin.math.max

data class LevelInfo(
    val level: Int,
    val title: String,
    val xpInLevel: Int,
    val xpForNext: Int,
    val progress: Float,
)

fun levelFor(totalXp: Int): LevelInfo {
    val perLevel = 250
    val level = totalXp / perLevel + 1
    val inLevel = totalXp % perLevel
    val titles = listOf(
        "First Steps",
        "Curious Mind",
        "Word Explorer",
        "Confident Speaker",
        "Fluent Thinker",
        "Storyteller",
        "English Insider",
    )
    return LevelInfo(
        level = level,
        title = titles[(level - 1).coerceIn(0, titles.lastIndex)],
        xpInLevel = inLevel,
        xpForNext = perLevel,
        progress = inLevel / perLevel.toFloat(),
    )
}

data class DayActivity(val date: LocalDate, val xp: Int)

/**
 * A tiny persistence layer built on SharedPreferences. All values are exposed
 * as Compose state so a single write is reflected across every screen.
 */
class ProgressStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("lingo_progress", Context.MODE_PRIVATE)

    var totalXp by mutableIntStateOf(prefs.getInt("total_xp", 0))
        private set
    var streak by mutableIntStateOf(prefs.getInt("streak", 0))
        private set
    var bestStreak by mutableIntStateOf(prefs.getInt("best_streak", 0))
        private set
    var dailyGoal by mutableIntStateOf(prefs.getInt("daily_goal", 40))
        private set
    var remindersEnabled by mutableStateOf(prefs.getBoolean("reminder_enabled", false))
        private set
    var reminderHour by mutableIntStateOf(prefs.getInt("reminder_hour", 19))
        private set
    var reminderMinute by mutableIntStateOf(prefs.getInt("reminder_minute", 0))
        private set
    var completedLessons by mutableStateOf(prefs.getStringSet("completed_lessons", emptySet())?.toSet() ?: emptySet())
        private set
    var completedRoleplays by mutableStateOf(prefs.getStringSet("completed_roleplays", emptySet())?.toSet() ?: emptySet())
        private set
    var solvedSentences by mutableStateOf(prefs.getStringSet("solved_sentences", emptySet())?.toSet() ?: emptySet())
        private set
    var masteredWords by mutableStateOf(prefs.getStringSet("mastered_words", emptySet())?.toSet() ?: emptySet())
        private set
    var savedWords by mutableStateOf(prefs.getStringSet("saved_words", emptySet())?.toSet() ?: emptySet())
        private set
    var quizAttempts by mutableIntStateOf(prefs.getInt("quiz_attempts", 0))
        private set
    var quizCorrect by mutableIntStateOf(prefs.getInt("quiz_correct", 0))
        private set
    var perfectQuizzes by mutableIntStateOf(prefs.getInt("perfect_quizzes", 0))
        private set
    var totalMinutes by mutableIntStateOf(prefs.getInt("total_minutes", 0))
        private set

    var cefrLevel by mutableStateOf(
        CefrLevel.fromKey(prefs.getString("cefr_level", CefrLevel.A1.name)),
    )
        private set
    var activeDays by mutableStateOf<Set<String>>(
        prefs.getStringSet("active_days", emptySet())?.toSet() ?: emptySet(),
    )
        private set
    var completedExams by mutableStateOf<Set<String>>(
        prefs.getStringSet("completed_exams", emptySet())?.toSet() ?: emptySet(),
    )
        private set
    var examRetryAfterDay by mutableIntStateOf(prefs.getInt("exam_retry_after", 0))
        private set
    var weeklyWeekId by mutableStateOf(prefs.getString("weekly_week", "") ?: "")
        private set
    var weeklyScore by mutableIntStateOf(prefs.getInt("weekly_score", 0))
        private set
    var activeLevelExamTarget by mutableStateOf(
        prefs.getString("active_exam_target", null)?.let { CefrLevel.fromKey(it) },
    )
        private set
    var weeklyAttemptInProgress by mutableStateOf(prefs.getBoolean("weekly_in_progress", false))
        private set

    var todayXp by mutableIntStateOf(0)
        private set
    var todayLessons by mutableIntStateOf(0)
        private set
    var todayWords by mutableIntStateOf(0)
        private set
    var todayQuizzes by mutableIntStateOf(0)
        private set
    var activity by mutableStateOf<Map<LocalDate, Int>>(emptyMap())
        private set

    init {
        loadToday()
        normalizeStreak()
        migrateActiveDays()
        settleAbandonedAttempts()
    }

    /** Existing installs predate active-day tracking; rebuild it from XP history. */
    private fun migrateActiveDays() {
        if (prefs.getBoolean("active_days_migrated", false)) return
        val days = prefs.all.entries.mapNotNull { (key, value) ->
            if (!key.startsWith("xp_") || (value as? Int ?: 0) <= 0) return@mapNotNull null
            val raw = key.removePrefix("xp_")
            runCatching { LocalDate.parse(raw) }.getOrNull()?.toString()
        }.toSet()
        if (days.isNotEmpty()) {
            activeDays = activeDays + days
        }
        prefs.edit()
            .putStringSet("active_days", activeDays)
            .putBoolean("active_days_migrated", true)
            .apply()
    }

    private fun today(): LocalDate = LocalDate.now()
    private fun dateKey(date: LocalDate = today()): String = date.toString()

    private fun loadToday() {
        val key = dateKey()
        todayXp = prefs.getInt("xp_$key", 0)
        todayLessons = prefs.getInt("lessons_$key", 0)
        todayWords = prefs.getInt("words_$key", 0)
        todayQuizzes = prefs.getInt("quizzes_$key", 0)
        activity = (0..6).associate { offset ->
            val date = today().minusDays(offset.toLong())
            date to prefs.getInt("xp_${dateKey(date)}", 0)
        }
    }

    private fun normalizeStreak() {
        val last = prefs.getString("last_study", null)?.let {
            runCatching { LocalDate.parse(it) }.getOrNull()
        } ?: return
        val gap = java.time.temporal.ChronoUnit.DAYS.between(last, today())
        if (gap > 1 && streak != 0) {
            streak = 0
        }
    }

    private fun addDailyXp(xp: Int) {
        if (xp <= 0) return
        val key = dateKey()
        val updated = prefs.getInt("xp_$key", 0) + xp
        prefs.edit().putInt("xp_$key", updated).apply()
        todayXp = updated
        activity = activity + (today() to updated)
    }

    private fun recordStudy() {
        val today = today()
        val last = prefs.getString("last_study", null)?.let {
            runCatching { LocalDate.parse(it) }.getOrNull()
        }
        if (last != today) {
            val nextStreak = if (last == today.minusDays(1)) streak + 1 else 1
            streak = nextStreak
            bestStreak = max(bestStreak, nextStreak)
            prefs.edit()
                .putString("last_study", dateKey(today))
                .putInt("streak", streak)
                .putInt("best_streak", bestStreak)
                .apply()
        }
        if (dateKey(today) !in activeDays) {
            activeDays = activeDays + dateKey(today)
            prefs.edit().putStringSet("active_days", activeDays).apply()
        }
    }

    private fun settleAbandonedAttempts() {
        val active = activeLevelExamTarget
        if (active != null) {
            // A killed process or a crash during the exam counts as leaving it.
            if (active.name !in completedExams) {
                examRetryAfterDay = today().plusDays(LingoLevels.RETRY_DAYS).toEpochDay().toInt()
                prefs.edit().putInt("exam_retry_after", examRetryAfterDay).apply()
            }
            activeLevelExamTarget = null
            prefs.edit().remove("active_exam_target").apply()
        }
        if (weeklyAttemptInProgress) {
            val currentWeek = LingoLevels.currentWeekId()
            if (weeklyWeekId != currentWeek) {
                weeklyWeekId = currentWeek
                weeklyScore = 0
                prefs.edit()
                    .putString("weekly_week", weeklyWeekId)
                    .putInt("weekly_score", 0)
                    .apply()
            }
            weeklyAttemptInProgress = false
            prefs.edit().putBoolean("weekly_in_progress", false).apply()
        }
    }

    fun beginLevelExam(target: CefrLevel) {
        activeLevelExamTarget = target
        prefs.edit().putString("active_exam_target", target.name).apply()
    }

    fun beginWeeklyTest() {
        weeklyAttemptInProgress = true
        prefs.edit().putBoolean("weekly_in_progress", true).apply()
    }

    /** Records a finished level exam. Passing promotes exactly one rung. */
    fun finishLevelExam(target: CefrLevel, passed: Boolean) {
        activeLevelExamTarget = null
        prefs.edit().remove("active_exam_target").apply()
        if (passed) {
            if (target.name !in completedExams) {
                completedExams = completedExams + target.name
                cefrLevel = target
                prefs.edit()
                    .putStringSet("completed_exams", completedExams)
                    .putString("cefr_level", target.name)
                    .apply()
                addTotalXp(50)
                recordStudy()
            }
            examRetryAfterDay = 0
            prefs.edit().putInt("exam_retry_after", 0).apply()
        } else {
            examRetryAfterDay = today().plusDays(LingoLevels.RETRY_DAYS).toEpochDay().toInt()
            prefs.edit().putInt("exam_retry_after", examRetryAfterDay).apply()
        }
    }

    /** Records this week's leaderboard score. Only the first attempt counts. */
    fun finishWeeklyTest(score: Int) {
        val week = LingoLevels.currentWeekId()
        if (weeklyWeekId == week) {
            weeklyAttemptInProgress = false
            prefs.edit().putBoolean("weekly_in_progress", false).apply()
            return
        }
        weeklyWeekId = week
        weeklyScore = score.coerceIn(0, LingoLevels.WEEKLY_QUESTION_COUNT)
        weeklyAttemptInProgress = false
        prefs.edit()
            .putString("weekly_week", weeklyWeekId)
            .putInt("weekly_score", weeklyScore)
            .putBoolean("weekly_in_progress", false)
            .apply()
    }

    val activeDayCount: Int get() = activeDays.size
    val weeklyAttempted: Boolean get() = weeklyWeekId == LingoLevels.currentWeekId()
    val nextCefrLevel: CefrLevel? get() = cefrLevel.next
    val examRetryDate: LocalDate? get() = LingoLevels.retryDate(examRetryAfterDay)

    fun canAttemptLevel(target: CefrLevel): Boolean {
        if (target != cefrLevel.next) return false
        if (totalXp < target.xpGate || activeDayCount < target.activeDaysGate) return false
        if (activeLevelExamTarget != null) return false
        val retry = examRetryDate
        return retry == null || !LocalDate.now().isBefore(retry)
    }

    private fun addTotalXp(amount: Int) {
        if (amount <= 0) return
        totalXp += amount
        prefs.edit().putInt("total_xp", totalXp).apply()
        addDailyXp(amount)
    }

    /** Marks a lesson complete and returns the XP actually awarded. */
    fun completeLesson(lesson: Lesson): Int {
        val isNew = lesson.id !in completedLessons
        val award = if (isNew) lesson.xp else 8
        if (isNew) {
            completedLessons = completedLessons + lesson.id
            prefs.edit().putStringSet("completed_lessons", completedLessons).apply()
            totalMinutes += lesson.minutes
            prefs.edit().putInt("total_minutes", totalMinutes).apply()
        }
        addTotalXp(award)
        val key = dateKey()
        val lessons = prefs.getInt("lessons_$key", 0) + 1
        prefs.edit().putInt("lessons_$key", lessons).apply()
        todayLessons = lessons
        recordStudy()
        return award
    }

    /** First playthrough earns full XP; repeat plays still count as practice. */
    fun completeRoleplay(scenarioId: String, xp: Int): Int {
        val isNew = scenarioId !in completedRoleplays
        val award = if (isNew) xp else 8
        if (isNew) {
            completedRoleplays = completedRoleplays + scenarioId
            prefs.edit().putStringSet("completed_roleplays", completedRoleplays).apply()
        }
        addTotalXp(award)
        recordStudy()
        return award
    }

    /** Each sentence is worth 5 XP the first time and 1 XP as a repeat rep. */
    fun recordSentenceBuild(puzzleId: String): Int {
        val isNew = puzzleId !in solvedSentences
        val award = if (isNew) 5 else 1
        if (isNew) {
            solvedSentences = solvedSentences + puzzleId
            prefs.edit().putStringSet("solved_sentences", solvedSentences).apply()
        }
        addTotalXp(award)
        recordStudy()
        return award
    }

    /** Returns 2 XP the first time a word is learned, otherwise 0. */
    fun learnWord(wordId: String): Int {
        if (wordId in masteredWords) return 0
        masteredWords = masteredWords + wordId
        prefs.edit().putStringSet("mastered_words", masteredWords).apply()
        addTotalXp(2)
        val key = dateKey()
        val words = prefs.getInt("words_$key", 0) + 1
        prefs.edit().putInt("words_$key", words).apply()
        todayWords = words
        recordStudy()
        return 2
    }

    fun toggleMastered(wordId: String) {
        masteredWords = if (wordId in masteredWords) masteredWords - wordId else masteredWords + wordId
        prefs.edit().putStringSet("mastered_words", masteredWords).apply()
    }

    fun toggleSaved(wordId: String) {
        savedWords = if (wordId in savedWords) savedWords - wordId else savedWords + wordId
        prefs.edit().putStringSet("saved_words", savedWords).apply()
    }

    /**
     * Records a finished practice session. XP is based on the score: 4 per
     * correct answer, with a 10 XP bonus for 80% or more.
     */
    fun recordQuiz(correct: Int, total: Int): Int {
        val xp = correct * 4 + if (total > 0 && correct.toFloat() / total >= 0.8f) 10 else 0
        quizAttempts += total
        quizCorrect += correct
        if (total > 0 && correct == total) perfectQuizzes += 1
        prefs.edit()
            .putInt("quiz_attempts", quizAttempts)
            .putInt("quiz_correct", quizCorrect)
            .putInt("perfect_quizzes", perfectQuizzes)
            .apply()
        addTotalXp(xp)
        val key = dateKey()
        val quizzes = prefs.getInt("quizzes_$key", 0) + 1
        prefs.edit().putInt("quizzes_$key", quizzes).apply()
        todayQuizzes = quizzes
        recordStudy()
        return xp
    }

    fun updateDailyGoal(goal: Int) {
        dailyGoal = goal.coerceIn(10, 200)
        prefs.edit().putInt("daily_goal", dailyGoal).apply()
    }

    fun updateRemindersEnabled(enabled: Boolean) {
        remindersEnabled = enabled
        prefs.edit().putBoolean("reminder_enabled", enabled).apply()
    }

    fun updateReminderTime(hour: Int, minute: Int) {
        reminderHour = hour.coerceIn(0, 23)
        reminderMinute = minute.coerceIn(0, 59)
        prefs.edit()
            .putInt("reminder_hour", reminderHour)
            .putInt("reminder_minute", reminderMinute)
            .apply()
    }

    /** True once any learning has been recorded today, whichever screen did it. */
    val practicedToday: Boolean
        get() = prefs.getString("last_study", null) == dateKey()

    /** Keeps a creative response on this phone so leaving a lesson loses no work. */
    fun creativeDraft(exerciseId: String): String =
        prefs.getString("writing_$exerciseId", "").orEmpty()

    fun saveCreativeDraft(exerciseId: String, text: String) {
        prefs.edit().putString("writing_$exerciseId", text).apply()
    }

    fun reset() {
        val goal = dailyGoal
        prefs.edit().clear().putInt("daily_goal", goal).apply()
        totalXp = 0
        streak = 0
        bestStreak = 0
        remindersEnabled = false
        reminderHour = 19
        reminderMinute = 0
        completedLessons = emptySet()
        completedRoleplays = emptySet()
        solvedSentences = emptySet()
        masteredWords = emptySet()
        savedWords = emptySet()
        quizAttempts = 0
        quizCorrect = 0
        perfectQuizzes = 0
        totalMinutes = 0
        todayXp = 0
        todayLessons = 0
        todayWords = 0
        todayQuizzes = 0
        activity = emptyMap()
        cefrLevel = CefrLevel.A1
        activeDays = emptySet()
        completedExams = emptySet()
        examRetryAfterDay = 0
        weeklyWeekId = ""
        weeklyScore = 0
        activeLevelExamTarget = null
        weeklyAttemptInProgress = false
    }

    val accuracy: Int
        get() = if (quizAttempts == 0) 0 else (quizCorrect * 100) / quizAttempts

    val goalProgress: Float
        get() = (todayXp.toFloat() / dailyGoal.coerceAtLeast(1)).coerceIn(0f, 1f)

    val level: LevelInfo
        get() = levelFor(totalXp)

    fun weeklyActivity(): List<DayActivity> = (6 downTo 0).map { offset ->
        val date = today().minusDays(offset.toLong())
        DayActivity(date, activity[date] ?: 0)
    }
}
