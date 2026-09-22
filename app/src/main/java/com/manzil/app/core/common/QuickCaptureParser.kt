package com.manzil.app.core.common

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * Natural-language parsing: "proposal 5pm friday !1 #client"
 * Free method - no AI needed, pure offline parsing
 */
data class ParsedQuickCapture(
    val title: String,
    val dueTime: LocalTime?,
    val dueDate: LocalDate?,
    val priority: Int,
    val goalTag: String?
)

object QuickCaptureParser {
    fun parse(input: String): ParsedQuickCapture {
        var title = input
        var dueTime: LocalTime? = null
        var dueDate: LocalDate? = null
        var priority = 2
        var goalTag: String? = null

        // Priority: !1 to !4
        Regex("!(\\d)").findAll(input).forEach { match ->
            priority = match.groupValues[1].toIntOrNull()?.coerceIn(1,4) ?: 2
            title = title.replace(match.value, "")
        }

        // Goal tag: #client
        Regex("#(\\w+)").findAll(input).forEach { match ->
            goalTag = match.groupValues[1]
            title = title.replace(match.value, "")
        }

        // Time: 5pm, 5:30pm, 17:00
        Regex("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?", RegexOption.IGNORE_CASE).findAll(input).forEach { match ->
            val hour = match.groupValues[1].toIntOrNull() ?: return@forEach
            val minute = match.groupValues[2].toIntOrNull() ?: 0
            val ampm = match.groupValues[3].lowercase()
            var h = hour
            if (ampm == "pm" && h < 12) h += 12
            if (ampm == "am" && h == 12) h = 0
            if (h in 0..23) {
                dueTime = LocalTime.of(h, minute)
                title = title.replace(match.value, "")
            }
        }

        // Date: friday, monday, today, tomorrow
        val today = LocalDate.now()
        val lower = input.lowercase()
        when {
            "today" in lower -> {
                dueDate = today
                title = title.replace(Regex("today", RegexOption.IGNORE_CASE), "")
            }
            "tomorrow" in lower -> {
                dueDate = today.plusDays(1)
                title = title.replace(Regex("tomorrow", RegexOption.IGNORE_CASE), "")
            }
            "monday" in lower -> dueDate = nextDay(DayOfWeek.MONDAY)
            "tuesday" in lower -> dueDate = nextDay(DayOfWeek.TUESDAY)
            "wednesday" in lower -> dueDate = nextDay(DayOfWeek.WEDNESDAY)
            "thursday" in lower -> dueDate = nextDay(DayOfWeek.THURSDAY)
            "friday" in lower -> dueDate = nextDay(DayOfWeek.FRIDAY)
            "saturday" in lower -> dueDate = nextDay(DayOfWeek.SATURDAY)
            "sunday" in lower -> dueDate = nextDay(DayOfWeek.SUNDAY)
        }
        // Remove day names from title
        listOf("monday","tuesday","wednesday","thursday","friday","saturday","sunday").forEach {
            title = title.replace(Regex(it, RegexOption.IGNORE_CASE), "")
        }

        title = title.trim().replace(Regex("\\s+"), " ")

        return ParsedQuickCapture(title, dueTime, dueDate, priority, goalTag)
    }

    private fun nextDay(target: DayOfWeek): LocalDate {
        var date = LocalDate.now()
        while (date.dayOfWeek != target) {
            date = date.plusDays(1)
        }
        return date
    }
}
