package com.alifba.alifba.presenation.chapters

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Verifies the ported streak logic (computeNextDayStreak/isDateExactlyOneDayBefore in
// StreakLogic.kt) against actual multi-day progressions, not just by reading the diff —
// this is exactly the bug that made the old Android code always increment on every
// completion regardless of date (see the removed "FORCING dayStreak update for testing"
// stub). Dates are fixed strings rather than the real system clock, so a "skipped day"
// scenario is deterministic without needing to wait real days or fake the device clock.
class StreakLogicTest {

    @Test
    fun `first ever activity starts streak at 1`() {
        val result = computeNextDayStreak(lastActivityDate = "", currentDate = "2026-03-10", currentStreak = 0)
        assertEquals(1, result)
    }

    @Test
    fun `same day completion does not increment`() {
        val result = computeNextDayStreak(lastActivityDate = "2026-03-10", currentDate = "2026-03-10", currentStreak = 3)
        assertEquals(3, result)
    }

    @Test
    fun `repeated same day completions never double count`() {
        var streak = 0
        var lastDate = ""
        val today = "2026-03-10"
        // Three lesson completions on the same day should still leave the streak at 1, not 3.
        repeat(3) {
            streak = computeNextDayStreak(lastDate, today, streak)
            lastDate = today
        }
        assertEquals(1, streak)
    }

    @Test
    fun `consecutive day increments by one`() {
        val result = computeNextDayStreak(lastActivityDate = "2026-03-10", currentDate = "2026-03-11", currentStreak = 3)
        assertEquals(4, result)
    }

    @Test
    fun `skipped day resets to 1`() {
        // Last activity two days before "today" — a full day was skipped in between.
        val result = computeNextDayStreak(lastActivityDate = "2026-03-08", currentDate = "2026-03-10", currentStreak = 5)
        assertEquals(1, result)
    }

    @Test
    fun `skipped week resets to 1`() {
        val result = computeNextDayStreak(lastActivityDate = "2026-03-01", currentDate = "2026-03-10", currentStreak = 6)
        assertEquals(1, result)
    }

    @Test
    fun `full week of consecutive daily completions reaches a 7-day streak`() {
        // Directly exercises the exact value moment the review-prompt trigger depends on.
        val days = listOf(
            "2026-03-01", "2026-03-02", "2026-03-03", "2026-03-04",
            "2026-03-05", "2026-03-06", "2026-03-07"
        )
        var streak = 0
        var lastDate = ""
        val streaksByDay = mutableListOf<Int>()
        for (day in days) {
            streak = computeNextDayStreak(lastDate, day, streak)
            streaksByDay.add(streak)
            lastDate = day
        }
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7), streaksByDay)
    }

    @Test
    fun `streak breaks mid-week then rebuilds from 1`() {
        // Day 1-3 consecutive, day 4 skipped entirely, day 5 resumes — streak should reset on
        // day 5's completion rather than continuing from 3.
        var streak = 0
        var lastDate = ""

        streak = computeNextDayStreak(lastDate, "2026-04-01", streak); lastDate = "2026-04-01"
        assertEquals(1, streak)
        streak = computeNextDayStreak(lastDate, "2026-04-02", streak); lastDate = "2026-04-02"
        assertEquals(2, streak)
        streak = computeNextDayStreak(lastDate, "2026-04-03", streak); lastDate = "2026-04-03"
        assertEquals(3, streak)

        // 2026-04-04 skipped — no completion that day at all.

        streak = computeNextDayStreak(lastDate, "2026-04-05", streak)
        assertEquals(1, streak)
    }

    @Test
    fun `isDateExactlyOneDayBefore handles month boundary`() {
        assertTrue(isDateExactlyOneDayBefore("2026-01-31", "2026-02-01"))
        assertFalse(isDateExactlyOneDayBefore("2026-01-30", "2026-02-01"))
    }

    @Test
    fun `isDateExactlyOneDayBefore handles year boundary`() {
        assertTrue(isDateExactlyOneDayBefore("2025-12-31", "2026-01-01"))
    }

    @Test
    fun `isDateExactlyOneDayBefore handles leap day`() {
        // 2028 is a leap year.
        assertTrue(isDateExactlyOneDayBefore("2028-02-28", "2028-02-29"))
        assertTrue(isDateExactlyOneDayBefore("2028-02-29", "2028-03-01"))
    }

    @Test
    fun `isDateExactlyOneDayBefore rejects same day and multi-day gaps`() {
        assertFalse(isDateExactlyOneDayBefore("2026-03-10", "2026-03-10"))
        assertFalse(isDateExactlyOneDayBefore("2026-03-08", "2026-03-10"))
    }

    @Test
    fun `isDateExactlyOneDayBefore rejects empty or malformed dates`() {
        assertFalse(isDateExactlyOneDayBefore("", "2026-03-10"))
        assertFalse(isDateExactlyOneDayBefore("2026-03-09", ""))
        assertFalse(isDateExactlyOneDayBefore("not-a-date", "2026-03-10"))
    }
}
