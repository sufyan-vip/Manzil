package com.manzil.app.feature.adaptive

import javax.inject.Inject
import javax.inject.Singleton

/**
 * BS SE Edition - Core Adaptive Engine
 * User requested:
 * - If no task done, adjust with kal
 * - AI checks task condition and arranges next tasks
 * - Perpetual till goal DONE
 * - Latest info updates tasks
 * - Modern client hunting, not Fiverr/Upwork torture
 */
@Singleton
class AdaptiveEngine @Inject constructor() {

    data class TaskCondition(
        val taskId: String,
        val status: String, // TODO, BLOCKED, OVERDUE, DONE
        val dueDate: String,
        val autoRolledCount: Int,
        val isOverdue: Boolean
    )

    data class AdaptiveDecision(
        val newDueDate: String,
        val reason: String,
        val priorityAdjustment: Int,
        val shouldSplit: Boolean,
        val clientPlatformSuggestion: String?
    )

    fun evaluateTask(condition: TaskCondition): AdaptiveDecision {
        return when {
            condition.isOverdue && condition.autoRolledCount < 3 -> {
                AdaptiveDecision(
                    newDueDate = "tomorrow",
                    reason = "Task missed yesterday — moving to tomorrow because you had ${condition.autoRolledCount} rolls. Let's do it first thing 09:00.",
                    priorityAdjustment = -1, // Increase priority
                    shouldSplit = condition.autoRolledCount >= 2,
                    clientPlatformSuggestion = null
                )
            }
            condition.status == "BLOCKED" -> {
                AdaptiveDecision(
                    newDueDate = "tomorrow",
                    reason = "Blocked — AI will generate 3 unblock actions",
                    priorityAdjustment = 0,
                    shouldSplit = true,
                    clientPlatformSuggestion = "LinkedIn"
                )
            }
            else -> {
                AdaptiveDecision(
                    newDueDate = condition.dueDate,
                    reason = "On track",
                    priorityAdjustment = 0,
                    shouldSplit = false,
                    clientPlatformSuggestion = null
                )
            }
        }
    }

    fun generateNextTasks(
        currentProgress: Int,
        mainGoal: String,
        skills: List<String>,
        preferredPlatforms: List<String>
    ): List<String> {
        // Perpetual engine - keeps generating till goal DONE
        return listOf(
            "Learn latest ${skills.firstOrNull() ?: "React"} trend from 2026 market",
            "Client hunt via ${preferredPlatforms.firstOrNull() ?: "Instagram"}: 10 DMs with value script",
            "Push towards $mainGoal: one concrete deliverable today"
        )
    }

    fun shouldUpdateWithLatestInfo(lastUpdateDaysAgo: Int): Boolean {
        return lastUpdateDaysAgo >= 7 // Weekly latest info sync
    }
}
