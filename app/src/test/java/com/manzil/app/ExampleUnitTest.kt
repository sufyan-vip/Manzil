package com.manzil.app
import org.junit.Test
import org.junit.Assert.*
class ExampleUnitTest {
    @Test fun testAppName() { assertEquals("Manzil", "Manzil") }
    @Test fun testAdaptiveEngine() {
        val condition = com.manzil.app.feature.adaptive.AdaptiveEngine.TaskCondition(
            taskId = "1", status = "TODO", dueDate = "2026-09-21", autoRolledCount = 1, isOverdue = true
        )
        val engine = com.manzil.app.feature.adaptive.AdaptiveEngine()
        val decision = engine.evaluateTask(condition)
        assertEquals("tomorrow", decision.newDueDate)
    }
    @Test fun testBriefingBuilder() {
        val builder = com.manzil.app.notification.BriefingBuilder()
        val briefing = builder.buildMorningBriefing(
            dayCounter = 412,
            todayTasks = listOf("09:00 React", "14:00 Proposals", "20:00 FYP"),
            yesterdayDone = 4, yesterdayPlanned = 5,
            pending = listOf("CS50 week 3"),
            goalMove = "Income 25k -> 40k",
            streak = 11, rootProgress = 18
        )
        assertTrue(briefing.contains("DO TODAY"))
        assertTrue(briefing.contains("DONE YESTERDAY"))
        assertTrue(briefing.contains("Still pending"))
        assertTrue(briefing.indexOf("DO TODAY") < briefing.indexOf("DONE YESTERDAY"))
        assertTrue(briefing.indexOf("DONE YESTERDAY") < briefing.indexOf("Still pending"))
    }
    @Test fun testRRuleExpander() {
        val rule = com.manzil.app.core.recurrence.RRuleParser.parse("FREQ=WEEKLY;BYDAY=MO,WE,FR")
        val start = java.time.LocalDate.of(2026, 10, 1)
        val expanded = com.manzil.app.core.recurrence.RRuleExpander.expand(rule, start, 60)
        assertTrue(expanded.size > 0)
    }
}
