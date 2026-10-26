package com.lingo.app.data

import java.time.ZonedDateTime
import java.util.Locale

/**
 * Decides when the next on-device practice nudge should fire.
 *
 * Reminders are deliberately local and single-shot: the receiver shows at most
 * one notification and asks for the next day's alarm. That keeps the schedule
 * correct after reboots, clock changes, and app updates without a repeating
 * alarm that can drift or fire twice.
 */
object ReminderSchedule {

    fun nextTrigger(
        now: ZonedDateTime,
        hour: Int,
        minute: Int,
        practicedToday: Boolean,
    ): ZonedDateTime {
        val safeHour = hour.coerceIn(0, 23)
        val safeMinute = minute.coerceIn(0, 59)
        val todayAt = now
            .withHour(safeHour)
            .withMinute(safeMinute)
            .withSecond(0)
            .withNano(0)
        // If today's session is already done, or the chosen time has passed,
        // the next useful nudge is tomorrow. Keeping the same zone preserves
        // the local wall-clock time across daylight-saving changes.
        return if (!practicedToday && todayAt.isAfter(now)) {
            todayAt
        } else {
            todayAt.plusDays(1)
        }
    }
}

object ReminderCopy {
    const val TITLE = "A few minutes of English?"

    fun body(streak: Int): String = if (streak > 0) {
        "Your $streak-day streak is waiting. Tap to practise."
    } else {
        "A quiet minute or two of English is all it takes. Tap to practise."
    }

    fun timeLabel(hour: Int, minute: Int): String =
        String.format(Locale.ROOT, "%02d:%02d", hour.coerceIn(0, 23), minute.coerceIn(0, 59))
}
