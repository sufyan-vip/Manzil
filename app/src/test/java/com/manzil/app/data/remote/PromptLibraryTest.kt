package com.manzil.app.data.remote

import com.manzil.app.data.remote.openrouter.PromptLibrary
import org.junit.Test
import org.junit.Assert.*

class PromptLibraryTest {
    @Test
    fun testPromptsContainPlaceholders() {
        assertTrue(PromptLibrary.PLAN_MY_DAY.contains("{DATE}"))
        assertTrue(PromptLibrary.PLAN_MY_DAY.contains("{TASKS}"))
        assertTrue(PromptLibrary.PLAN_MY_DAY.contains("{MAIN_GOAL}"))
        assertTrue(PromptLibrary.PLAN_MY_DAY.contains("{PLATFORMS}"))
    }

    @Test
    fun testSystemBase() {
        assertTrue(PromptLibrary.SYSTEM_BASE.contains("{LANGUAGE}"))
        assertTrue(PromptLibrary.SYSTEM_BASE.contains("{MAX_WORDS}"))
        assertTrue(PromptLibrary.SYSTEM_BASE.contains("PERPETUAL"))
    }

    @Test
    fun testStrictAdaptivePrompts() {
        assertTrue(PromptLibrary.SMART_RESCHEDULER.contains("Auto-move to tomorrow"))
        assertTrue(PromptLibrary.MODERN_CLIENT_HUNT.contains("NOT Fiverr/Upwork"))
        assertTrue(PromptLibrary.MODERN_CLIENT_HUNT.contains("Instagram"))
        assertTrue(PromptLibrary.LATEST_INFO_UPDATER.contains("2026-2027"))
    }

    @Test
    fun testAllPromptsExist() {
        assertNotNull(PromptLibrary.PLAN_MY_DAY)
        assertNotNull(PromptLibrary.WEEKLY_REVIEW)
        assertNotNull(PromptLibrary.EXPLAIN_CHANGES)
        assertNotNull(PromptLibrary.UNBLOCK_ME)
        assertNotNull(PromptLibrary.SMART_RESCHEDULER)
        assertNotNull(PromptLibrary.LATEST_INFO_UPDATER)
        assertNotNull(PromptLibrary.MODERN_CLIENT_HUNT)
    }
}
