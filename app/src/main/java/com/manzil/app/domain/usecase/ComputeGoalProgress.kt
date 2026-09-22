package com.manzil.app.domain.usecase

import javax.inject.Inject

class ComputeGoalProgressUseCase @Inject constructor() {
    fun computeProgress(doneMilestones: Int, totalMilestones: Int, doneTasks: Int, totalTasks: Int): Int {
        return if (totalMilestones > 0) {
            (doneMilestones * 100 / totalMilestones)
        } else if (totalTasks > 0) {
            (doneTasks * 100 / totalTasks)
        } else 0
    }

    fun computeParentProgress(childrenProgress: List<Int>, priorities: List<Int>): Int {
        if (childrenProgress.isEmpty()) return 0
        val weighted = childrenProgress.zip(priorities).sumOf { (prog, prio) -> prog * (5 - prio) }
        val weightSum = priorities.sumOf { 5 - it }
        return if (weightSum > 0) weighted / weightSum else childrenProgress.average().toInt()
    }
}
