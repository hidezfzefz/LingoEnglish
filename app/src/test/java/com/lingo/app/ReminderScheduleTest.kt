package com.lingo.app.data

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderScheduleTest {

    private val utc = ZoneId.of("UTC")

    private fun at(
        date: String,
        time: String,
        zone: ZoneId = utc,
    ): ZonedDateTime = ZonedDateTime.of(
        LocalDate.parse(date),
        LocalTime.parse(time),
        zone,
    )

    @Test
    fun `uses today when the chosen time is still ahead`() {
        val trigger = ReminderSchedule.nextTrigger(
            now = at("2026-10-25", "09:15"),
            hour = 19,
            minute = 0,
            practicedToday = false,
        )
        assertEquals(at("2026-10-25", "19:00"), trigger)
    }

    @Test
    fun `rolls to tomorrow when the chosen time has passed`() {
        val trigger = ReminderSchedule.nextTrigger(
            now = at("2026-10-25", "20:30"),
            hour = 19,
            minute = 0,
            practicedToday = false,
        )
        assertEquals(at("2026-10-26", "19:00"), trigger)
    }

    @Test
    fun `skips today after the learner has practised`() {
        val trigger = ReminderSchedule.nextTrigger(
            now = at("2026-10-25", "09:15"),
            hour = 19,
            minute = 0,
            practicedToday = true,
        )
        assertEquals(at("2026-10-26", "19:00"), trigger)
    }

    @Test
    fun `handles a midnight reminder without firing twice`() {
        val beforeMidnight = ReminderSchedule.nextTrigger(
            now = at("2026-10-25", "23:59"),
            hour = 0,
            minute = 0,
            practicedToday = false,
        )
        assertEquals(at("2026-10-26", "00:00"), beforeMidnight)

        val exactlyMidnight = ReminderSchedule.nextTrigger(
            now = at("2026-10-26", "00:00"),
            hour = 0,
            minute = 0,
            practicedToday = false,
        )
        assertEquals(at("2026-10-27", "00:00"), exactlyMidnight)
    }

    @Test
    fun `crosses a month and a year boundary`() {
        val month = ReminderSchedule.nextTrigger(
            now = at("2026-01-31", "20:00"),
            hour = 19,
            minute = 0,
            practicedToday = false,
        )
        assertEquals(at("2026-02-01", "19:00"), month)

        val year = ReminderSchedule.nextTrigger(
            now = at("2026-12-31", "20:00"),
            hour = 19,
            minute = 0,
            practicedToday = false,
        )
        assertEquals(at("2027-01-01", "19:00"), year)
    }

    @Test
    fun `keeps a leap day local`() {
        val trigger = ReminderSchedule.nextTrigger(
            now = at("2028-02-28", "20:00"),
            hour = 19,
            minute = 0,
            practicedToday = false,
        )
        assertEquals(at("2028-02-29", "19:00"), trigger)
    }

    @Test
    fun `keeps the wall-clock time across spring daylight saving`() {
        val london = ZoneId.of("Europe/London")
        val trigger = ReminderSchedule.nextTrigger(
            now = at("2026-03-28", "20:00", london),
            hour = 19,
            minute = 0,
            practicedToday = false,
        )
        assertEquals(LocalTime.of(19, 0), trigger.toLocalTime())
        assertEquals(LocalDate.of(2026, 3, 29), trigger.toLocalDate())
        assertEquals(ZoneOffset.ofHours(1), trigger.offset)
    }

    @Test
    fun `keeps the wall-clock time across autumn daylight saving`() {
        val london = ZoneId.of("Europe/London")
        val trigger = ReminderSchedule.nextTrigger(
            now = at("2026-10-24", "20:00", london),
            hour = 19,
            minute = 0,
            practicedToday = false,
        )
        assertEquals(LocalTime.of(19, 0), trigger.toLocalTime())
        assertEquals(LocalDate.of(2026, 10, 25), trigger.toLocalDate())
        assertEquals(ZoneOffset.UTC, trigger.offset)
    }

    @Test
    fun `clamps invalid times instead of crashing`() {
        val trigger = ReminderSchedule.nextTrigger(
            now = at("2026-10-25", "08:00"),
            hour = 99,
            minute = 99,
            practicedToday = false,
        )
        assertEquals(LocalTime.of(23, 59), trigger.toLocalTime())
        assertTrue(trigger.isAfter(at("2026-10-25", "08:00")))
    }

    @Test
    fun `notification copy stays free of personal data`() {
        assertTrue(ReminderCopy.body(0).contains("practise"))
        assertTrue(ReminderCopy.body(4).contains("4-day streak"))
        assertEquals("19:05", ReminderCopy.timeLabel(19, 5))
    }
}
