package com.manzil.app.core.diff

import org.junit.Test
import org.junit.Assert.*

class JsonDiffTest {
    @Test
    fun testDiff() {
        val old = mapOf("targetDate" to "2030-06-01", "title" to "Software House")
        val new = mapOf("targetDate" to "2030-09-01", "title" to "Software House")
        val diffs = com.manzil.app.domain.usecase.BuildGoalDiff().buildDiff(old, new)
        assertEquals(1, diffs.size)
        assertEquals("targetDate", diffs[0].field)
        assertEquals("2030-06-01", diffs[0].from)
        assertEquals("2030-09-01", diffs[0].to)
    }

    @Test
    fun testNoDiff() {
        val old = mapOf("title" to "A")
        val new = mapOf("title" to "A")
        val diffs = com.manzil.app.domain.usecase.BuildGoalDiff().buildDiff(old, new)
        assertTrue(diffs.isEmpty())
    }

    @Test
    fun testJsonDiffObject() {
        val diffs = JsonDiff.diff("{}", "{}")
        assertNotNull(diffs)
    }
}
