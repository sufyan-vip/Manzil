package com.manzil.app.core.format

import com.manzil.app.core.common.Fmt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class AppFormattersTest {

    @Test
    fun moneyUsesSouthAsianGrouping() {
        assertEquals("25,000", Fmt.money(25000.0))
        assertEquals("59,78,000", Fmt.money(5978000.0))
        assertEquals("15,00,000", Fmt.money(1500000.0))
        assertEquals("3,50,000", Fmt.money(350000.0))
        assertEquals("999", Fmt.money(999.0))
    }

    @Test
    fun durationReadsLikeHumansTalk() {
        assertEquals("45m", Fmt.duration(45))
        assertEquals("2h", Fmt.duration(120))
        assertEquals("2h 15m", Fmt.duration(135))
        assertEquals("0m", Fmt.duration(0))
    }

    @Test
    fun stopwatchIsZeroPadded() {
        assertEquals("00:00:00", Fmt.stopwatch(0))
        assertEquals("00:01:05", Fmt.stopwatch(65_000))
        assertEquals("01:00:00", Fmt.stopwatch(3_600_000))
    }

    @Test
    fun weekStartsOnMonday() {
        // 2026-09-20 is a Sunday
        assertEquals(LocalDate.of(2026, 9, 14), Fmt.startOfWeek(LocalDate.of(2026, 9, 20)))
    }

    @Test
    fun relativeDayLabelsAreFriendly() {
        val today = LocalDate.now()
        assertEquals("Today", Fmt.dayLabel(today))
        assertEquals("Tomorrow", Fmt.dayLabel(today.plusDays(1)))
        assertEquals("Yesterday", Fmt.dayLabel(today.minusDays(1)))
    }

    @Test
    fun kpiDisplayCarriesTheUnit() {
        assertEquals("Rs 25,000", Fmt.kpiDisplay(25000.0, "PKR"))
        assertEquals("3", Fmt.kpiDisplay(3.0, "clients").substringBefore(' '))
        assertTrue(Fmt.kpiDisplay(3.0, "clients").startsWith("3"))
    }

    @Test
    fun timeIsRenderedIn24HourClock() {
        assertEquals("17:30", Fmt.time(LocalTime.of(17, 30)))
    }
}
