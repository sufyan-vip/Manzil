package com.manzil.app.core.common

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * Natural-language quick capture: "proposal 5pm friday !1 #client"
 * Pure offline parsing — no AI, no network, instant.
 */
data class ParsedQuickCapture(
    val title: String,
    val dueTime: LocalTime?,
    val dueDate: LocalDate?,
    val priority: Int,
    val goalTag: String?
)

object QuickCaptureParser {

    private val priorityRegex = Regex("!(\\d)")
    private val tagRegex = Regex("#([A-Za-z][A-Za-z0-9_-]*)")
    private val amPmRegex = Regex("(\\d{1,2})(?::(\\d{2}))?\\s?(am|pm)", RegexOption.IGNORE_CASE)
    private val hhmmRegex = Regex("(?<!\\d)(\\d{1,2}):(\\d{2})(?!\\d)")
    private val dayNames = mapOf(
        "monday" to DayOfWeek.MONDAY,
        "tuesday" to DayOfWeek.TUESDAY,
        "wednesday" to DayOfWeek.WEDNESDAY,
        "thursday" to DayOfWeek.THURSDAY,
        "friday" to DayOfWeek.FRIDAY,
        "saturday" to DayOfWeek.SATURDAY,
        "sunday" to DayOfWeek.SUNDAY
    )

    fun parse(input: String): ParsedQuickCapture {
        var title = input
        var dueTime: LocalTime? = null
        var dueDate: LocalDate? = null
        var priority = 2
        var goalTag: String? = null

        priorityRegex.find(input)?.let { match ->
            priority = match.groupValues[1].toIntOrNull()?.coerceIn(1, 4) ?: 2
            title = title.replace(match.value, "")
        }

        tagRegex.find(input)?.let { match ->
            goalTag = match.groupValues[1]
            title = title.replace(match.value, "")
        }

        // 5pm / 5:30 pm
        amPmRegex.find(input)?.let { match ->
            val rawHour = match.groupValues[1].toIntOrNull()
            val minute = match.groupValues[2].toIntOrNull() ?: 0
            val suffix = match.groupValues[3].lowercase()
            if (rawHour != null && rawHour in 1..12 && minute in 0..59) {
                var hour = rawHour
                if (suffix == "pm" && hour < 12) hour += 12
                if (suffix == "am" && hour == 12) hour = 0
                dueTime = LocalTime.of(hour, minute)
                title = title.replace(match.value, "")
            }
        }

        // 17:00 — only when the shape is clearly a clock time
        if (dueTime == null) {
            hhmmRegex.find(input)?.let { match ->
                val hour = match.groupValues[1].toIntOrNull()
                val minute = match.groupValues[2].toIntOrNull()
                if (hour != null && minute != null && hour in 0..23 && minute in 0..59) {
                    dueTime = LocalTime.of(hour, minute)
                    title = title.replace(match.value, "")
                }
            }
        }

        val lower = input.lowercase()
        when {
            "day after tomorrow" in lower -> dueDate = LocalDate.now().plusDays(2)
            "tomorrow" in lower -> dueDate = LocalDate.now().plusDays(1)
            "today" in lower || "aaj" in lower -> dueDate = LocalDate.now()
            "kal" in lower -> dueDate = LocalDate.now().plusDays(1)
        }
        if (dueDate == null) {
            for ((name, dow) in dayNames) {
                if (name in lower) {
                    dueDate = nextDay(dow)
                    break
                }
            }
        }

        listOf(
            "day after tomorrow", "tomorrow", "today", "aaj", "kal"
        ).forEach { title = title.replace(Regex("\\b$it\\b", RegexOption.IGNORE_CASE), "") }
        dayNames.keys.forEach { name ->
            title = title.replace(Regex("\\b$name\\b", RegexOption.IGNORE_CASE), "")
        }

        title = title.replace(Regex("\\s+"), " ").trim()

        return ParsedQuickCapture(title, dueTime, dueDate, priority, goalTag)
    }

    /** Shown live under the quick-capture field so the user sees what will be saved. */
    fun describe(parsed: ParsedQuickCapture): String {
        val bits = mutableListOf<String>()
        parsed.dueDate?.let { bits += Fmt.relativeDay(it) }
        parsed.dueTime?.let { bits += Fmt.time(it) }
        bits += "P${parsed.priority}"
        parsed.goalTag?.let { bits += "#$it" }
        return bits.joinToString(" · ")
    }

    private fun nextDay(target: DayOfWeek): LocalDate {
        var date = LocalDate.now()
        repeat(8) {
            if (date.dayOfWeek == target) return date
            date = date.plusDays(1)
        }
        return date
    }
}
