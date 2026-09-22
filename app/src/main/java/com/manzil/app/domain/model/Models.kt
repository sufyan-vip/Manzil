package com.manzil.app.domain.model

import com.manzil.app.data.local.entity.Goal
import com.manzil.app.data.local.entity.Milestone
import com.manzil.app.data.local.entity.Task
import java.time.LocalDate

/** A task placed on a specific day (either its own due date or a recurring occurrence). */
data class DayTask(
    val task: Task,
    val date: LocalDate,
    val done: Boolean,
    val completedAt: Long?,
    val fromInstance: Boolean
) {
    val id: String get() = task.id
    val isRecurring: Boolean get() = task.recurrenceRule != null
}

/** Everything the TODAY briefing card needs — and the same shape the notification uses. */
data class TodayBriefing(
    val dayCounter: Int,
    val todayTasks: List<String>,
    val yesterdayDone: Int,
    val yesterdayPlanned: Int,
    val pending: List<String>,
    val goalMove: String?,
    val streak: Int,
    val rootProgress: Int
)

data class GoalNode(
    val goal: Goal,
    val milestones: List<Milestone>,
    val children: List<GoalNode>,
    val tasksTotal: Int,
    val tasksDone: Int,
    val progress: Int
) {
    val isSlipping: Boolean
        get() = goal.targetDate != null &&
            goal.status != com.manzil.app.data.local.entity.GoalStatus.DONE &&
            goal.targetDate.isBefore(LocalDate.now())

    val daysLate: Int
        get() = goal.targetDate?.let { (LocalDate.now().toEpochDay() - it.toEpochDay()).toInt() } ?: 0
}

data class ProgressRings(
    val today: Float,
    val week: Float,
    val goal: Float
)

data class StreakInfo(
    val current: Int,
    val longest: Int,
    val last14: List<Pair<LocalDate, Int>>
)

data class SearchHit(
    val type: String,
    val id: String,
    val title: String,
    val snippet: String,
    val dateIso: String? = null,
    val score: Int = 0
)

data class KpiCard(
    val key: String,
    val label: String,
    val unit: String?,
    val current: Double,
    val target: Double,
    val history: List<Pair<LocalDate, Double>>
) {
    val progress: Float
        get() = if (target <= 0.0) 0f else (current / target).toFloat().coerceIn(0f, 1f)
}

data class TimeStats(
    val todayMinutes: Int,
    val weekMinutes: Int,
    val sevenDayAverageMinutes: Int,
    val perGoal: List<Pair<String, Int>>,
    val last14Days: List<Pair<LocalDate, Int>>
)

data class HabitRow(
    val id: String,
    val name: String,
    val icon: String,
    val colorArgb: Int,
    val targetPerWeek: Int,
    val doneToday: Boolean,
    val weekDots: List<Boolean>
)

data class WeeklySummary(
    val planned: Int,
    val done: Int,
    val overdue: Int,
    val focusMinutes: Int,
    val averageScore: Int,
    val habitPercent: Int,
    val goalChanges: List<String>,
    val wins: List<String>,
    val blockers: List<String>,
    val priorities: List<String>
)

data class PulseItem(
    val id: String,
    val goalTitle: String,
    val changeType: com.manzil.app.data.local.entity.ChangeType,
    val changes: List<com.manzil.app.core.diff.DiffEntry>,
    val note: String?,
    val atMillis: Long
)

data class AiMessage(
    val id: String,
    val role: String,
    val content: String,
    val atMillis: Long
)

data class UserProfile(
    val name: String,
    val university: String,
    val semester: String,
    val skills: List<String>,
    val mainGoal: String,
    val targetDate: String,
    val dailyHours: Int,
    val preferredPlatforms: List<String>,
    val isPerpetual: Boolean = true
)
