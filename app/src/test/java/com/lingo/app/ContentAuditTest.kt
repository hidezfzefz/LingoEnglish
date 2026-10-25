package com.lingo.app

import com.lingo.app.data.CefrLevel
import com.lingo.app.data.ExerciseKind
import com.lingo.app.data.LingoContent
import com.lingo.app.data.LingoLevels
import com.lingo.app.data.TestSkill
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Headless content audit. Run with:
 *
 *   ./gradlew :app:testDebugUnitTest --tests com.lingo.app.ContentAuditTest
 *
 * The checks deliberately cover both the learning content and the assessment
 * banks so a content edit cannot silently weaken an exam or a weekly test.
 */
class ContentAuditTest {

    @Test
    fun contentAndAssessmentBanksAreConsistent() {
        val failures = mutableListOf<String>()

        fun check(condition: Boolean, message: String) {
            if (!condition) failures += message
        }

        val units = LingoContent.units
        val lessons = LingoContent.lessons
        check(units.size == 6, "expected 6 units, got ${units.size}")
        check(lessons.size == 20, "expected 20 lessons, got ${lessons.size}")
        check(units.map { it.id }.distinct().size == units.size, "unit ids must be unique")
        check(units.all { it.lessons.isNotEmpty() }, "every unit needs at least one lesson")
        check(
            units.flatMap { it.lessons }.map { it.id }.sorted() == lessons.map { it.id }.sorted(),
            "the lesson list must match the lessons inside each unit",
        )
        check(
            units.all { unit -> unit.lessons.all { it.unitId == unit.id } },
            "every lesson must point back to its containing unit",
        )
        check(lessons.map { it.id }.distinct().size == lessons.size, "lesson ids must be unique")
        check(
            lessons.all { it.slides.isNotEmpty() && it.exercises.isNotEmpty() },
            "every lesson needs at least one slide and one exercise",
        )

        val creativeTasks = lessons
            .flatMap { lesson -> lesson.exercises.filter { it.kind == ExerciseKind.WRITE } }
        check(creativeTasks.size == 20, "expected 20 creative tasks, got ${creativeTasks.size}")
        creativeTasks.forEach { task ->
            check(task.minWords > 0, "${task.id} needs a word target")
            check(task.checklist.size >= 3, "${task.id} needs at least 3 success criteria")
            check(!task.modelAnswer.isNullOrBlank(), "${task.id} needs a model answer")
            check(task.options.isEmpty(), "${task.id} is a writing task and must not have options")
        }

        val exercises = lessons.flatMap { it.exercises }
        check(
            exercises.map { it.id }.distinct().size == exercises.size,
            "exercise ids must be unique across all lessons",
        )
        exercises.filter { it.kind != ExerciseKind.WRITE }.forEach { exercise ->
            check(exercise.options.size == 4, "${exercise.id} should have exactly 4 options")
            check(
                exercise.correctIndex in exercise.options.indices,
                "${exercise.id} has an answer key outside its options",
            )
            check(exercise.explanation.isNotBlank(), "${exercise.id} needs an explanation")
        }

        CefrLevel.entries.forEach { level ->
            val exam = LingoLevels.examFor(level)
            check(
                exam.size == LingoLevels.LEVEL_QUESTION_COUNT,
                "$level exam has ${exam.size} questions, expected ${LingoLevels.LEVEL_QUESTION_COUNT}",
            )
            check(
                LingoLevels.LEVEL_PASS_SCORE <= exam.size,
                "$level pass mark ${LingoLevels.LEVEL_PASS_SCORE} exceeds the exam",
            )
            check(
                exam.map { it.prompt }.distinct().size == exam.size,
                "$level exam contains duplicate prompts",
            )
            check(
                exam.all { it.options.size == 4 && it.correctIndex in it.options.indices },
                "$level exam contains a malformed answer key",
            )
            val examSkillCounts = exam.groupingBy { it.skill }.eachCount()
            val expectedPerSkill = LingoLevels.LEVEL_QUESTION_COUNT / TestSkill.entries.size
            check(
                TestSkill.entries.all { examSkillCounts[it] == expectedPerSkill },
                "$level exam is not balanced: $examSkillCounts",
            )

            repeat(300) { attempt ->
                val weekly = LingoLevels.weeklyQuestions(level)
                check(
                    weekly.size == LingoLevels.WEEKLY_QUESTION_COUNT,
                    "$level weekly attempt $attempt has ${weekly.size} questions",
                )
                check(
                    weekly.map { it.prompt }.distinct().size == weekly.size,
                    "$level weekly attempt $attempt contains duplicate prompts",
                )
                check(
                    weekly.all { it.options.size == 4 && it.correctIndex in it.options.indices },
                    "$level weekly attempt $attempt contains a malformed answer key",
                )
                val weeklySkillCounts = weekly.groupingBy { it.skill }.eachCount()
                check(
                    weeklySkillCounts.keys == TestSkill.entries.toSet() &&
                        (weeklySkillCounts.values.max() - weeklySkillCounts.values.min()) <= 1,
                    "$level weekly attempt $attempt is not balanced: $weeklySkillCounts",
                )
            }

            repeat(50) {
                val prepared = LingoLevels.prepare(exam)
                prepared.forEach { question ->
                    val original = exam.firstOrNull { it.prompt == question.prompt }
                    check(original != null, "$level shuffled a question that is not in the bank")
                    if (original != null) {
                        check(
                            question.options[question.correctIndex] ==
                                original.options[original.correctIndex],
                            "$level shuffled ${question.prompt} but changed the correct answer",
                        )
                    }
                }
            }
        }

        val summary = buildString {
            appendLine("Units: ${units.size}")
            appendLine("Lessons: ${lessons.size}")
            appendLine("Slides: ${lessons.sumOf { it.slides.size }}")
            appendLine("Exercises: ${exercises.size}")
            appendLine("Creative tasks: ${creativeTasks.size}")
            appendLine(
                "Exam banks: " + CefrLevel.entries.joinToString {
                    "${it.label}=${LingoLevels.examFor(it).size}"
                },
            )
            append("Skills: " + TestSkill.entries.joinToString { it.label })
        }

        assertTrue(
            "$summary\n\nContent audit failures:\n" + failures.joinToString("\n"),
            failures.isEmpty(),
        )
        println(summary)
        println("AUDIT PASS")
    }
}
