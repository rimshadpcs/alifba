package com.alifba.alifba.presenation.chapters

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Pure, deterministic day-streak decision logic — no Firestore or system-clock dependency, so
// it's directly unit-testable (see StreakLogicTest) with fixed date strings simulating
// multi-day progression, rather than only verifiable by reading the diff. Mirrors
// ChaptersViewModel.swift's updateStreak(): same day as the last activity is a no-op, exactly
// one calendar day later increments, anything else (including the very first-ever activity,
// when lastActivityDate is empty) resets to 1.
internal fun computeNextDayStreak(
    lastActivityDate: String,
    currentDate: String,
    currentStreak: Int
): Int {
    return when {
        lastActivityDate.isEmpty() -> 1
        lastActivityDate == currentDate -> currentStreak
        isDateExactlyOneDayBefore(lastActivityDate, currentDate) -> currentStreak + 1
        else -> 1
    }
}

// True when laterDateStr is exactly one calendar day after earlierDateStr. Both dates are
// passed in as "yyyy-MM-dd" strings rather than read from Calendar.getInstance() internally,
// so callers (production code and tests alike) control "today" explicitly.
internal fun isDateExactlyOneDayBefore(earlierDateStr: String, laterDateStr: String): Boolean {
    if (earlierDateStr.isEmpty() || laterDateStr.isEmpty()) return false
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        val earlier = dateFormat.parse(earlierDateStr) ?: return false
        val expectedNext = Calendar.getInstance().apply {
            time = earlier
            add(Calendar.DAY_OF_YEAR, 1)
        }
        dateFormat.format(expectedNext.time) == laterDateStr
    } catch (e: Exception) {
        false
    }
}
