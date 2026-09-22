package com.manzil.app.domain.usecase
import org.junit.Test
import org.junit.Assert.*
class ComputeGoalProgressTest {
    private val useCase = ComputeGoalProgress()
    @Test fun testLeafProgressMilestones() { assertEquals(75, useCase.computeProgress(3,4,0,0)) }
    @Test fun testLeafProgressTasks() { assertEquals(50, useCase.computeProgress(0,0,5,10)) }
    @Test fun testParentProgressWeighted() {
        val children = listOf(50,80,100)
        val priorities = listOf(1,2,3)
        assertEquals(71, useCase.computeParentProgress(children, priorities))
    }
}
