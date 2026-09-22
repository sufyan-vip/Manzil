package com.manzil.app.notification

import org.junit.Test
import org.junit.Assert.*

class BriefingBuilderTest {
    private val builder = BriefingBuilder()

    @Test
    fun testThreeBlocksInOrder() {
        val briefing = builder.buildMorningBriefing(
            dayCounter = 412,
            todayTasks = listOf("09:00 React", "14:00 Proposals", "20:00 FYP"),
            yesterdayDone = 4,
            yesterdayPlanned = 5,
            pending = listOf("CS50 week 3"),
            goalMove = "Income 25k → 40k",
            streak = 11,
            rootProgress = 18,
            language = "en"
        )
        assertTrue(briefing.contains("DO TODAY"))
        assertTrue(briefing.contains("DONE YESTERDAY"))
        assertTrue(briefing.contains("Still pending"))
        val doIndex = briefing.indexOf("DO TODAY")
        val doneIndex = briefing.indexOf("DONE YESTERDAY")
        val pendingIndex = briefing.indexOf("Still pending")
        assertTrue("DO TODAY should come first", doIndex < doneIndex)
        assertTrue("DONE YESTERDAY should come second", doneIndex < pendingIndex)
    }

    @Test
    fun testOverdueInRehGaya() {
        val briefing = builder.buildMorningBriefing(
            dayCounter = 1,
            todayTasks = listOf("Task 1"),
            yesterdayDone = 0,
            yesterdayPlanned = 2,
            pending = listOf("Overdue task 1", "Overdue task 2"),
            goalMove = null,
            streak = 0,
            rootProgress = 0
        )
        assertTrue(briefing.contains("Overdue task 1"))
    }

    @Test
    fun testZeroDataStillMeaningful() {
        val briefing = builder.buildMorningBriefing(
            dayCounter = 1,
            todayTasks = emptyList(),
            yesterdayDone = 0,
            yesterdayPlanned = 0,
            pending = emptyList(),
            goalMove = null,
            streak = 0,
            rootProgress = 0
        )
        assertFalse("Should not be blank", briefing.isBlank())
        assertTrue(briefing.contains("DO TODAY"))
        assertTrue(briefing.contains("Day 1"))
    }

    @Test
    fun testRomanUrduVersion() {
        val briefing = builder.buildMorningBriefing(
            dayCounter = 412,
            todayTasks = listOf("React"),
            yesterdayDone = 4,
            yesterdayPlanned = 5,
            pending = listOf("CS50"),
            goalMove = null,
            streak = 11,
            rootProgress = 18,
            language = "ur"
        )
        assertTrue(briefing.contains("AAJ KARNA HAI"))
        assertTrue(briefing.contains("KAL HO GAYA"))
        assertTrue(briefing.contains("Abhi baqi"))
    }

    @Test
    fun testEveningReview() {
        val review = builder.buildEveningReview(
            done = 4, planned = 6,
            pending = listOf("10 Upwork proposals", "CS50 week 3"),
            focusMinutes = 200, avgMinutes = 165,
            nextTask = "React useEffect"
        )
        assertTrue(review.contains("Aaj ka hisaab"))
        assertTrue(review.contains("Reh gaya"))
        assertTrue(review.contains("Focus time"))
    }
}
