package com.manzil.app.core.recurrence

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * RFC-5545 subset expander (FREQ / INTERVAL / BYDAY / BYMONTHDAY / COUNT / UNTIL).
 * Hand written on purpose: no extra dependency, fully unit-testable, offline.
 */
object RRuleExpander {

    private val dayCodes = mapOf(
        "MO" to DayOfWeek.MONDAY,
        "TU" to DayOfWeek.TUESDAY,
        "WE" to DayOfWeek.WEDNESDAY,
        "TH" to DayOfWeek.THURSDAY,
        "FR" to DayOfWeek.FRIDAY,
        "SA" to DayOfWeek.SATURDAY,
        "SU" to DayOfWeek.SUNDAY
    )

    /**
     * Returns every occurrence in `[start, start + daysAhead]` (both ends inclusive)
     * that satisfies the rule, capped at [rule.count] occurrences when COUNT is set.
     */
    fun expand(rule: RRule, start: LocalDate, daysAhead: Int = 60): List<LocalDate> {
        val untilDate = rule.until?.let { runCatching { LocalDate.parse(it.take(10)) }.getOrNull() }
        val occurrences = mutableListOf<LocalDate>()

        for (offset in 0..daysAhead.toLong()) {
            val date = start.plusDays(offset)
            if (untilDate != null && date.isAfter(untilDate)) break
            if (matches(rule, start, date)) {
                occurrences += date
                if (rule.count != null && occurrences.size >= rule.count) break
            }
        }
        return occurrences
    }

    /** Convenience for the scheduler: the next `limit` occurrences on or after [from]. */
    fun next(rule: RRule, start: LocalDate, from: LocalDate, limit: Int = 60): List<LocalDate> =
        expand(rule, start, daysAhead = 400)
            .filter { !it.isBefore(from) }
            .take(limit)

    fun matches(rule: RRule, start: LocalDate, date: LocalDate): Boolean {
        if (date.isBefore(start)) return false
        return when (rule.freq) {
            Freq.DAILY -> {
                val gap = ChronoUnit.DAYS.between(start, date)
                gap % rule.interval == 0L
            }
            Freq.WEEKLY -> {
                val weeks = ChronoUnit.WEEKS.between(weekAnchor(start), weekAnchor(date))
                if (weeks % rule.interval != 0L) return false
                val days = rule.byDay.mapNotNull { dayCodes[it.uppercase()] }
                if (days.isEmpty()) date.dayOfWeek == start.dayOfWeek else date.dayOfWeek in days
            }
            Freq.MONTHLY -> {
                val months = ChronoUnit.MONTHS.between(
                    start.withDayOfMonth(1),
                    date.withDayOfMonth(1)
                )
                if (months % rule.interval != 0L) return false
                val expected = rule.byMonthDay.ifEmpty { listOf(start.dayOfMonth) }
                date.dayOfMonth in expected
            }
            Freq.YEARLY -> {
                val years = ChronoUnit.YEARS.between(start, date)
                if (years % rule.interval != 0L) return false
                date.month == start.month && date.dayOfMonth == start.dayOfMonth
            }
        }
    }

    private fun weekAnchor(date: LocalDate): LocalDate =
        date.minusDays((date.dayOfWeek.value - 1).toLong())
}
