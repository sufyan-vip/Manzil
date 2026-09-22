package com.manzil.app.core.diff

import com.manzil.app.domain.usecase.BuildGoalDiff
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonDiffTest {

    @Test
    fun buildGoalDiffReportsOnlyChangedFields() {
        val diffs = BuildGoalDiff().buildDiff(
            mapOf("targetDate" to "2030-06-01", "title" to "Software House"),
            mapOf("targetDate" to "2030-09-01", "title" to "Software House")
        )
        assertEquals(1, diffs.size)
        assertEquals("targetDate", diffs[0].field)
        assertEquals("2030-06-01", diffs[0].from)
        assertEquals("2030-09-01", diffs[0].to)
    }

    @Test
    fun identicalDocumentsProduceNoDiff() {
        assertTrue(JsonDiff.diff("""{"a":1}""", """{"a":1}""").isEmpty())
    }

    @Test
    fun nestedFieldsAreFlattened() {
        val diffs = JsonDiff.diff(
            """{"metric":{"current":1000,"target":5000}}""",
            """{"metric":{"current":2500,"target":5000}}"""
        )
        assertEquals(1, diffs.size)
        assertEquals("metric.current", diffs[0].field)
        assertEquals("1000", diffs[0].from)
        assertEquals("2500", diffs[0].to)
    }

    @Test
    fun brokenJsonNeverCrashes() {
        assertTrue(JsonDiff.diff("{not json", "{}").isNotEmpty())
    }

    @Test
    fun labelsAreHumanReadable() {
        assertEquals("Metric target", JsonDiff.label("metricTarget"))
        assertEquals("Due date", JsonDiff.label("dueDate"))
    }
}
