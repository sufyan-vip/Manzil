package com.manzil.app.core.common

import org.junit.Test
import org.junit.Assert.*

class QuickCaptureParserTest {
    @Test
    fun testParseFull() {
        val input = "proposal 5pm friday !1 #client"
        val parsed = QuickCaptureParser.parse(input)
        assertEquals("proposal", parsed.title)
        assertEquals(1, parsed.priority)
        assertEquals("client", parsed.goalTag)
        assertNotNull(parsed.dueTime)
        assertEquals(17, parsed.dueTime!!.hour)
        assertNotNull(parsed.dueDate)
    }

    @Test
    fun testParsePriority() {
        val parsed = QuickCaptureParser.parse("fix bug !3")
        assertEquals(3, parsed.priority)
        assertEquals("fix bug", parsed.title)
    }

    @Test
    fun testParseTodayTomorrow() {
        val today = QuickCaptureParser.parse("call mom today")
        assertNotNull(today.dueDate)
        val tomorrow = QuickCaptureParser.parse("call mom tomorrow")
        assertNotNull(tomorrow.dueDate)
        assertEquals(tomorrow.dueDate, today.dueDate!!.plusDays(1))
    }

    @Test
    fun testParseEmpty() {
        val parsed = QuickCaptureParser.parse("")
        assertEquals("", parsed.title)
        assertEquals(2, parsed.priority)
    }

    @Test
    fun testNumbersInTitleAreNotMistakenForTimes() {
        val parsed = QuickCaptureParser.parse("Send 10 proposals")
        assertEquals("Send 10 proposals", parsed.title)
        assertNull(parsed.dueTime)
    }

    @Test
    fun testColonTimeIsParsed() {
        val parsed = QuickCaptureParser.parse("standup 09:30 tomorrow")
        assertEquals("standup", parsed.title)
        assertEquals(9, parsed.dueTime!!.hour)
        assertEquals(30, parsed.dueTime!!.minute)
    }

    @Test
    fun testDescribeSummarisesTheCapture() {
        val parsed = QuickCaptureParser.parse("proposal 5pm friday !1 #client")
        val described = QuickCaptureParser.describe(parsed)
        assertTrue(described.contains("17:00"))
        assertTrue(described.contains("P1"))
        assertTrue(described.contains("#client"))
    }
}
