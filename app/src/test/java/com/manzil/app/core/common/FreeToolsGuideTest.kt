package com.manzil.app.core.common

import org.junit.Test
import org.junit.Assert.*

class FreeToolsGuideTest {
    @Test
    fun testFreeToolsNotEmpty() {
        assertTrue(FreeToolsGuide.development.isNotEmpty())
        assertTrue(FreeToolsGuide.learning.isNotEmpty())
        assertTrue(FreeToolsGuide.clientHuntingFree.isNotEmpty())
    }
    @Test
    fun testZeroToGoalRoadmap() {
        val roadmap = FreeToolsGuide.getZeroToGoalRoadmap()
        assertTrue(roadmap.contains("ZERO SE START"))
        assertTrue(roadmap.contains("Rs 0"))
    }
}
