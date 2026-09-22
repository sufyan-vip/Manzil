package com.manzil.app.core.recurrence

import java.time.LocalDate

object RRuleExpander {
    fun expand(rule: RRule, start: LocalDate, daysAhead: Int = 60): List<LocalDate> {
        // Simplified expansion - real implementation would handle all RFC-5545
        val result = mutableListOf<LocalDate>()
        var current = start
        var count = 0
        while (result.size < 60 && count < 1000) {
            if (matches(rule, current)) {
                result.add(current)
            }
            current = current.plusDays(1)
            count++
            if (current.isAfter(start.plusDays(daysAhead.toLong()))) break
            if (rule.count != null && result.size >= rule.count) break
        }
        return result
    }

    private fun matches(rule: RRule, date: LocalDate): Boolean {
        if (rule.byDay.isNotEmpty()) {
            val dayMap = mapOf("MO" to 1, "TU" to 2, "WE" to 3, "TH" to 4, "FR" to 5, "SA" to 6, "SU" to 7)
            val dow = date.dayOfWeek.value
            return rule.byDay.any { dayMap[it] == dow }
        }
        return true
    }
}
