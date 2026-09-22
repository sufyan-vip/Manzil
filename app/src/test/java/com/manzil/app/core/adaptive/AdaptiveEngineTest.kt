package com.manzil.app.core.adaptive

import com.manzil.app.data.local.entity.Task
import com.manzil.app.data.local.entity.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class AdaptiveEngineTest {

    private val engine = AdaptiveEngine()

    @Test
    fun overdueTaskRollsToOneDayWithAReason() {
        val decision = engine.evaluateTask(
            AdaptiveEngine.TaskCondition("1", "TODO", "2026-09-21", 1, true)
        )
        assertEquals("tomorrow", decision.newDueDate)
        assertEquals(-1, decision.priorityAdjustment)
        assertTrue(decision.reason.contains("tomorrow"))
    }

    @Test
    fun blockedTaskGetsSplit() {
        val decision = engine.evaluateTask(
            AdaptiveEngine.TaskCondition("2", "BLOCKED", "2026-09-22", 0, false)
        )
        assertEquals("tomorrow", decision.newDueDate)
        assertTrue(decision.shouldSplit)
    }

    @Test
    fun onTrackTaskIsLeftAlone() {
        val decision = engine.evaluateTask(
            AdaptiveEngine.TaskCondition("3", "TODO", "2026-09-23", 0, false)
        )
        assertEquals("2026-09-23", decision.newDueDate)
        assertFalse(decision.shouldSplit)
    }

    @Test
    fun perpetualEngineAlwaysProducesNextTasks() {
        val tasks = engine.generateNextTasks(18, "Software House", listOf("React"), listOf("Instagram"))
        assertTrue(tasks.isNotEmpty())
        assertTrue(tasks.any { it.contains("Instagram") })
    }

    @Test
    fun latestInfoRefreshesWeekly() {
        assertTrue(engine.shouldUpdateWithLatestInfo(7))
        assertFalse(engine.shouldUpdateWithLatestInfo(3))
    }

    @Test
    fun todayIsRankedByTimeThenPriority() {
        val now = System.currentTimeMillis()
        fun task(id: String, hour: Int?, priority: Int, rolled: Int = 0) = Task(
            id = id,
            title = id,
            dueDate = LocalDate.now(),
            dueTime = hour?.let { LocalTime.of(it, 0) },
            priority = priority,
            status = TaskStatus.TODO,
            autoRolledCount = rolled,
            createdAt = now,
            updatedAt = now
        )
        val ranked = engine.rankForToday(
            listOf(
                task("late", 20, 1),
                task("early", 9, 3),
                task("done", 8, 1).copy(status = TaskStatus.DONE)
            )
        )
        assertEquals(listOf("early", "late"), ranked.map { it.id })
    }

    @Test
    fun clientActionAdaptsToTheChosenPlatform() {
        val action = engine.clientActionOfTheDay(listOf("LinkedIn"), "React", dayIndex = 0)
        assertEquals("LinkedIn", action.platform)
        assertTrue(action.script.isNotBlank())
        assertNotNull(action.expected)
    }

    @Test
    fun rolloverReasonEscalatesKindly() {
        assertTrue(engine.rolloverReason(1, 1).contains("Kal reh gaya"))
        assertTrue(engine.rolloverReason(3, 4).contains("3 din"))
    }
}
