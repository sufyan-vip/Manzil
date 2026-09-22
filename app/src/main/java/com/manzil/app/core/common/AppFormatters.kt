package com.manzil.app.core.common

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Single place for every date / time / duration rendering so the UI stays consistent
 * and digits never jitter (callers pair these with tabular numerals).
 */
object Fmt {

    private val dayMonth = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)
    private val dayMonthYear = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
    private val weekdayLong = DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH)
    private val clock24 = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)

    fun today(): LocalDate = LocalDate.now()

    fun greeting(now: LocalTime = LocalTime.now()): String = when (now.hour) {
        in 4..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }

    fun greetingUr(now: LocalTime = LocalTime.now()): String = when (now.hour) {
        in 4..11 -> "Subah bakhair"
        in 12..16 -> "Dopahar bakhair"
        else -> "Shaam bakhair"
    }

    fun dateLong(date: LocalDate): String =
        "${date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)}, ${dayMonthYear.format(date)}"

    fun dateShort(date: LocalDate): String = dayMonth.format(date)

    fun weekday(date: LocalDate): String = weekdayLong.format(date)

    fun weekdayShort(date: LocalDate): String =
        date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)

    fun time(time: LocalTime): String = clock24.format(time)

    fun time(millis: Long): String =
        clock24.format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalTime())

    fun clockNow(time: LocalTime = LocalTime.now()): String =
        DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.ENGLISH).format(time)

    fun dayLabel(date: LocalDate): String = when (date) {
        LocalDate.now() -> "Today"
        LocalDate.now().plusDays(1) -> "Tomorrow"
        LocalDate.now().minusDays(1) -> "Yesterday"
        else -> dateShort(date)
    }

    fun relativeDay(date: LocalDate): String = when (date) {
        LocalDate.now() -> "Today"
        LocalDate.now().plusDays(1) -> "Tomorrow"
        LocalDate.now().minusDays(1) -> "Yesterday"
        else -> "${Fmt.dateLong(date)}"
    }

    fun startOfWeek(date: LocalDate, firstDay: DayOfWeek = DayOfWeek.MONDAY): LocalDate {
        var d = date
        while (d.dayOfWeek != firstDay) d = d.minusDays(1)
        return d
    }

    /** 2h 15m / 45m / 0m — used for focus time, estimates, rollups. */
    fun duration(minutes: Int): String {
        if (minutes <= 0) return "0m"
        val h = minutes / 60
        val m = minutes % 60
        return when {
            h == 0 -> "${m}m"
            m == 0 -> "${h}h"
            else -> "${h}h ${m}m"
        }
    }

    /** 01:23:45 — the focus timer readout. */
    fun stopwatch(millis: Long): String {
        val total = (millis / 1000).coerceAtLeast(0)
        val h = total / 3600
        val m = (total % 3600) / 60
        val s = total % 60
        return "%02d:%02d:%02d".format(h, m, s)
    }

    fun percent(value: Int): String = "${value.coerceIn(0, 100)}%"

    fun percent(fraction: Float): String = percent((fraction * 100).toInt())

    /** 25000 -> 25,000 ; 1500000 -> 15,00,000 (South-Asian grouping, as used in PK). */
    fun money(value: Double): String {
        val rounded = if (value % 1.0 == 0.0) value.toLong().toString() else String.format(Locale.ENGLISH, "%.1f", value)
        return groupIndian(rounded)
    }

    fun number(value: Double): String =
        if (value % 1.0 == 0.0) groupIndian(value.toLong().toString())
        else String.format(Locale.ENGLISH, "%.1f", value)

    private fun groupIndian(raw: String): String {
        val negative = raw.startsWith("-")
        val digits = raw.removePrefix("-")
        val intPart = digits.substringBefore('.')
        val decPart = digits.substringAfter('.', "")
        val grouped = if (intPart.length <= 3) {
            intPart
        } else {
            val tail = intPart.takeLast(3)
            val head = intPart.dropLast(3)
            val headGrouped = head.reversed().chunked(2).joinToString(",").reversed()
            "$headGrouped,$tail"
        }
        val withDec = if (decPart.isEmpty()) grouped else "$grouped.$decPart"
        return if (negative) "-$withDec" else withDec
    }

    fun kpiDisplay(value: Double, unit: String?): String = when (unit) {
        null, "" -> number(value)
        "PKR" -> "Rs ${money(value)}"
        "%" -> "${number(value)}%"
        else -> "${number(value)} ${unit.lowercase(Locale.ENGLISH)}"
    }

    fun minutesOf(millis: Long): Int = (millis / 60000L).toInt()

    fun dayOfMillis(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()

    fun startOfDayMillis(date: LocalDate): Long =
        date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun endOfDayMillis(date: LocalDate): Long =
        date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

    fun millisAt(date: LocalDate, time: LocalTime): Long =
        date.atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun localTimeOf(millis: Long): LocalTime =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalTime()

    fun daysBetween(from: LocalDate, to: LocalDate): Int =
        (to.toEpochDay() - from.toEpochDay()).toInt()
}
