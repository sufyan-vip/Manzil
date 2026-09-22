package com.manzil.app.core.recurrence

import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate

class RRuleExpanderTest {
    @Test
    fun testWeeklyMoWeFr_60days_26occurrences() {
        val rule = RRuleParser.parse("FREQ=WEEKLY;BYDAY=MO,WE,FR")
        val start = LocalDate.of(2026, 10, 1) // Thursday
        val expanded = RRuleExpander.expand(rule, start, 60)
        // MO,WE,FR in 60 days from Oct 1 2026 = 26 occurrences (calculated)
        assertEquals(26, expanded.size)
        // All should be Mon, Wed, Fri
        expanded.forEach { date ->
            val dow = date.dayOfWeek.value
            assertTrue("Should be Mon(1), Wed(3), Fri(5) but was $dow", dow in listOf(1,3,5))
        }
    }

    @Test
    fun testDaily() {
        val rule = RRuleParser.parse("FREQ=DAILY;INTERVAL=1")
        val start = LocalDate.of(2026, 10, 1)
        val expanded = RRuleExpander.expand(rule, start, 10)
        assertEquals(11, expanded.size) // inclusive
    }

    @Test
    fun testCount() {
        val rule = RRuleParser.parse("FREQ=WEEKLY;BYDAY=MO;COUNT=5")
        val start = LocalDate.of(2026, 10, 5) // Monday
        val expanded = RRuleExpander.expand(rule, start, 60)
        assertEquals(5, expanded.size)
    }
}
