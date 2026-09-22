package com.manzil.app.feature.adaptive

import org.junit.Test
import org.junit.Assert.*

class AdaptiveEngineTest {
    private val engine = AdaptiveEngine()
    @Test
    fun testOverdueAutoRollover() {
        val condition = AdaptiveEngine.TaskCondition("1", "TODO", "2026-09-21", 1, true)
        val decision = engine.evaluateTask(condition)
        assertEquals("tomorrow", decision.newDueDate)
        assertEquals(-1, decision.priorityAdjustment)
    }
    @Test
    fun testBlockedTask() {
        val condition = AdaptiveEngine.TaskCondition("2", "BLOCKED", "2026-09-22", 0, false)
        val decision = engine.evaluateTask(condition)
        assertEquals("tomorrow", decision.newDueDate)
        assertTrue(decision.shouldSplit)
    }
    @Test
    fun testOnTrack() {
        val condition = AdaptiveEngine.TaskCondition("3", "TODO", "2026-09-23", 0, false)
        val decision = engine.evaluateTask(condition)
        assertEquals("2026-09-23", decision.newDueDate)
    }
    @Test
    fun testGenerateNextTasksPerpetual() {
        val tasks = engine.generateNextTasks(18, "Software House", listOf("React"), listOf("Instagram"))
        assertTrue(tasks.isNotEmpty())
    }
    @Test
    fun testLatestInfoSync() {
        assertTrue(engine.shouldUpdateWithLatestInfo(7))
        assertFalse(engine.shouldUpdateWithLatestInfo(3))
    }
}
