package com.manzil.app.data.remote

import com.manzil.app.data.remote.openrouter.PromptLibrary
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptLibraryTest {

    @Test
    fun planMyDayCarriesEveryPlaceholder() {
        assertTrue(PromptLibrary.PLAN_MY_DAY.contains("{DATE}"))
        assertTrue(PromptLibrary.PLAN_MY_DAY.contains("{TASKS}"))
        assertTrue(PromptLibrary.PLAN_MY_DAY.contains("{MAIN_GOAL}"))
        assertTrue(PromptLibrary.PLAN_MY_DAY.contains("{PLATFORMS}"))
    }

    @Test
    fun systemBaseKeepsTheEnginePerpetual() {
        assertTrue(PromptLibrary.SYSTEM_BASE.contains("{LANGUAGE}"))
        assertTrue(PromptLibrary.SYSTEM_BASE.contains("{MAX_WORDS}"))
        assertTrue(PromptLibrary.SYSTEM_BASE.contains("PERPETUAL"))
    }

    @Test
    fun reschedulerRollsWorkToTomorrow() {
        assertTrue(PromptLibrary.SMART_RESCHEDULER.contains("Auto-move to tomorrow"))
    }

    @Test
    fun clientHuntAvoidsTheMarketplaceGrind() {
        assertTrue(PromptLibrary.MODERN_CLIENT_HUNT.contains("Fiverr/Upwork"))
        assertTrue(PromptLibrary.MODERN_CLIENT_HUNT.contains("Instagram"))
    }

    @Test
    fun latestInfoUpdaterTargetsTheCurrentMarket() {
        assertTrue(PromptLibrary.LATEST_INFO_UPDATER.contains("2026-2027"))
    }

    @Test
    fun everyPromptExists() {
        assertNotNull(PromptLibrary.PLAN_MY_DAY)
        assertNotNull(PromptLibrary.WEEKLY_REVIEW)
        assertNotNull(PromptLibrary.EXPLAIN_CHANGES)
        assertNotNull(PromptLibrary.UNBLOCK_ME)
        assertNotNull(PromptLibrary.SMART_RESCHEDULER)
        assertNotNull(PromptLibrary.LATEST_INFO_UPDATER)
        assertNotNull(PromptLibrary.MODERN_CLIENT_HUNT)
    }
}
