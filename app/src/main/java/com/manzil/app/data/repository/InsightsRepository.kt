package com.manzil.app.data.repository

import com.manzil.app.core.common.Fmt
import com.manzil.app.core.common.TimeProvider
import com.manzil.app.data.local.SeedData
import com.manzil.app.data.local.dao.GoalDao
import com.manzil.app.data.local.dao.HabitDao
import com.manzil.app.data.local.dao.KpiDao
import com.manzil.app.data.local.dao.ReviewDao
import com.manzil.app.data.local.dao.TaskDao
import com.manzil.app.data.local.dao.TimeDao
import com.manzil.app.data.local.entity.Goal
import com.manzil.app.data.local.entity.GoalStatus
import com.manzil.app.data.local.entity.KpiSnapshot
import com.manzil.app.data.local.entity.Milestone
import com.manzil.app.data.local.entity.TaskStatus
import com.manzil.app.data.local.entity.TimeEntry
import com.manzil.app.domain.model.GoalNode
import com.manzil.app.domain.model.KpiCard
import com.manzil.app.domain.model.ProgressRings
import com.manzil.app.domain.model.StreakInfo
import com.manzil.app.domain.model.TimeStats
import com.manzil.app.domain.model.WeeklySummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Everything computed rather than stored: progress rings, streaks, KPI cards,
 * time rollups and the weekly review. All of it works offline.
 */
@Singleton
class InsightsRepository @Inject constructor(
    private val goalDao: GoalDao,
    private val taskDao: TaskDao,
    private val timeDao: TimeDao,
    private val kpiDao: KpiDao,
    private val habitDao: HabitDao,
    private val reviewDao: ReviewDao,
    private val timeProvider: TimeProvider
) {

    fun observeKpiCards(): Flow<List<KpiCard>> = kpiDao.observeAll().map { snapshots ->
        snapshots.groupBy { it.key }.map { (key, values) ->
            val sorted = values.sortedBy { it.date }
            val latest = sorted.last()
            KpiCard(
                key = key,
                label = SeedData.kpiLabel(key),
                unit = SeedData.kpiUnit(key),
                current = latest.value,
                target = SeedData.kpiTarget(key),
                history = sorted.map { it.date to it.value }
            )
        }.sortedBy { it.label }
    }

    fun observeTimeEntries(from: Long, to: Long): Flow<List<TimeEntry>> = timeDao.observeBetween(from, to)

    /* --------------------------------------------------------------------------------------- */

    suspend fun progressRings(): ProgressRings = withContext(Dispatchers.IO) {
        val today = timeProvider.today()
        val weekStart = Fmt.startOfWeek(today, DayOfWeek.MONDAY)
        val allTasks = taskDao.allActive()

        val todayTasks = allTasks.filter { task ->
            task.recurrenceRule == null && task.dueDate == today && task.status != TaskStatus.CANCELLED
        }
        val todayDone = todayTasks.count { it.status == TaskStatus.DONE }

        val weekDone = allTasks.count { task ->
            val completed = task.completedAt?.let { Fmt.dayOfMillis(it) }
            completed != null && !completed.isBefore(weekStart) && !completed.isAfter(today)
        }
        val weekPlanned = allTasks.count { task ->
            val due = task.dueDate
            due != null && !due.isBefore(weekStart) && !due.isAfter(today) &&
                task.status != TaskStatus.CANCELLED
        } + weekDone
        val root = goalDao.rootGoals().firstOrNull()
        val goals = goalDao.allGoals()
        val goalProgress = root?.progressPercent
            ?: if (goals.isEmpty()) 0 else goals.map { it.progressPercent }.average().toInt()

        ProgressRings(
            today = if (todayTasks.isEmpty()) 0f else todayDone.toFloat() / todayTasks.size,
            week = if (weekPlanned <= 0) 0f else (weekDone.toFloat() / weekPlanned).coerceIn(0f, 1f),
            goal = (goalProgress / 100f).coerceIn(0f, 1f)
        )
    }

    suspend fun streakInfo(): StreakInfo = withContext(Dispatchers.IO) {
        val today = timeProvider.today()
        val from = today.minusDays(83)
        val counts = taskDao.completedBetween(Fmt.startOfDayMillis(from), Fmt.endOfDayMillis(today))
            .mapNotNull { it.completedAt }
            .groupingBy { Fmt.dayOfMillis(it) }
            .eachCount()

        var longest = 0
        var run = 0
        var cursor = from
        while (!cursor.isAfter(today)) {
            if ((counts[cursor] ?: 0) > 0) {
                run++
                if (run > longest) longest = run
            } else {
                run = 0
            }
            cursor = cursor.plusDays(1)
        }

        var streakCursor = if (counts.containsKey(today)) today else today.minusDays(1)
        var current = 0
        while (counts.containsKey(streakCursor)) {
            current++
            streakCursor = streakCursor.minusDays(1)
        }

        val last14 = (0..13).map { back ->
            val date = today.minusDays(back.toLong())
            date to (counts[date] ?: 0)
        }
        StreakInfo(current = current, longest = longest, last14 = last14)
    }

    suspend fun goalTree(): List<GoalNode> = withContext(Dispatchers.IO) {
        val goals = goalDao.allGoals()
        val milestones = goalDao.allMilestones()
        val tasks = taskDao.allActive()
        goals.filter { it.parentGoalId == null }
            .sortedBy { it.sortOrder }
            .map { buildNode(it, goals, milestones, tasks) }
    }

    /** Progress: milestones, else tasks, blended with the weighted progress of sub-goals. */
    private fun buildNode(
        goal: Goal,
        allGoals: List<Goal>,
        allMilestones: List<Milestone>,
        allTasks: List<com.manzil.app.data.local.entity.Task>
    ): GoalNode {
        val ownMilestones = allMilestones.filter { it.goalId == goal.id }.sortedBy { it.sortOrder }
        val ownTasks = allTasks.filter { it.goalId == goal.id && it.parentTaskId == null }
        val children = allGoals.filter { it.parentGoalId == goal.id }
            .sortedBy { it.sortOrder }
            .map { buildNode(it, allGoals, allMilestones, allTasks) }
        val ownScore = when {
            ownMilestones.isNotEmpty() -> ownMilestones.count { it.done } * 100 / ownMilestones.size
            ownTasks.isNotEmpty() -> ownTasks.count { it.status == TaskStatus.DONE } * 100 / ownTasks.size
            else -> null
        }
        val progress = if (children.isNotEmpty()) {
            val weights = children.map { 4 - it.goal.priority.coerceIn(1, 3) }
            val totalWeight = weights.sum()
            val fromChildren = if (totalWeight > 0) {
                children.map { it.progress }.zip(weights).sumOf { (score, weight) -> score * weight } / totalWeight
            } else {
                children.map { it.progress }.average().toInt()
            }
            if (ownScore != null) (fromChildren * 2 + ownScore) / 3 else fromChildren
        } else {
            ownScore ?: goal.progressPercent
        }
        return GoalNode(
            goal = goal,
            milestones = ownMilestones,
            children = children,
            tasksTotal = ownTasks.size,
            tasksDone = ownTasks.count { it.status == TaskStatus.DONE },
            progress = progress
        )
    }

    /** Flat list of every active goal with its computed progress — used by pickers. */
    suspend fun goalChoices(): List<Pair<Goal, Int>> = withContext(Dispatchers.IO) {
        goalTree().flatMap { flatten(it) }.map { it.goal to it.progress }
    }

    private fun flatten(node: GoalNode): List<GoalNode> =
        listOf(node) + node.children.flatMap { flatten(it) }

    suspend fun milestonesFor(goalId: String): List<Milestone> = withContext(Dispatchers.IO) {
        goalDao.allMilestones().filter { it.goalId == goalId }.sortedBy { it.sortOrder }
    }

    suspend fun timeStats(): TimeStats = withContext(Dispatchers.IO) {
        val today = timeProvider.today()
        val entries = timeDao.between(Fmt.startOfDayMillis(today.minusDays(13)), Fmt.endOfDayMillis(today))
        val byDay = entries.groupBy { Fmt.dayOfMillis(it.startedAt) }

        fun minutesOf(entry: TimeEntry): Int {
            val end = entry.endedAt ?: timeProvider.nowMillis()
            return ((end - entry.startedAt) / 60000L).toInt().coerceAtLeast(0)
        }

        val todayMinutes = byDay[today]?.sumOf { minutesOf(it) } ?: 0
        val weekStart = Fmt.startOfWeek(today, DayOfWeek.MONDAY)
        val weekMinutes = entries.filter { !Fmt.dayOfMillis(it.startedAt).isBefore(weekStart) }
            .sumOf { minutesOf(it) }
        val last7 = (0..6).map { back -> byDay[today.minusDays(back.toLong())]?.sumOf { minutesOf(it) } ?: 0 }
        val perGoal = entries.groupBy { it.goalId ?: "none" }
            .map { (goalId, list) -> goalId to list.sumOf { minutesOf(it) } }
            .sortedByDescending { it.second }
            .take(6)

        TimeStats(
            todayMinutes = todayMinutes,
            weekMinutes = weekMinutes,
            sevenDayAverageMinutes = last7.sum() / 7,
            perGoal = perGoal,
            last14Days = (13 downTo 0).map { back ->
                val date = today.minusDays(back.toLong())
                date to (byDay[date]?.sumOf { minutesOf(it) } ?: 0)
            }
        )
    }

    suspend fun weeklySummary(): WeeklySummary = withContext(Dispatchers.IO) {
        val today = timeProvider.today()
        val weekStart = Fmt.startOfWeek(today, DayOfWeek.MONDAY)
        val allTasks = taskDao.allActive()
        val plannedThisWeek = allTasks.count { task ->
            task.dueDate != null && !task.dueDate.isBefore(weekStart) && !task.dueDate.isAfter(today)
        }
        val doneThisWeek = allTasks.count { task ->
            val completed = task.completedAt?.let { Fmt.dayOfMillis(it) }
            completed != null && !completed.isBefore(weekStart) && !completed.isAfter(today)
        }
        val overdue = allTasks.count { task ->
            task.dueDate != null && task.dueDate.isBefore(today) &&
                task.status != TaskStatus.DONE && task.status != TaskStatus.CANCELLED
        }
        val reviews = reviewDao.reviewsSince(weekStart)
        val journals = reviewDao.journalsSince(weekStart)
        val focus = reviews.sumOf { it.focusMinutes }
        val habitLogs = habitDao.logsBetween(weekStart, today)
        val habitPercent = if (habitLogs.isEmpty()) 0 else (habitLogs.count { it.done } * 100) / habitLogs.size

        WeeklySummary(
            planned = plannedThisWeek,
            done = doneThisWeek,
            overdue = overdue,
            focusMinutes = focus,
            averageScore = if (reviews.isEmpty()) 0 else reviews.map { it.scorePercent }.average().toInt(),
            habitPercent = habitPercent,
            goalChanges = goalDao.allRevisions()
                .filter { it.createdAt >= Fmt.startOfDayMillis(weekStart) }
                .take(8)
                .mapNotNull { revision ->
                    val goal = goalDao.goalById(revision.goalId) ?: return@mapNotNull null
                    "${revision.changeType.name.lowercase()} · ${goal.title}"
                },
            wins = journals.map { it.wins }.filter { it.isNotBlank() }.take(6),
            blockers = journals.map { it.blockers }.filter { it.isNotBlank() }.take(6),
            priorities = buildPriorities(allTasks.count { it.status == TaskStatus.TODO })
        )
    }

    private fun buildPriorities(openTasks: Int): List<String> {
        val base = mutableListOf(
            "Deep work 2h on the next milestone",
            "10 outreach messages with the daily script",
            "Ship one visible thing (commit / post / case study)"
        )
        if (openTasks > 12) {
            base[0] = "Clear 5 stale tasks first — plan is overloaded ($openTasks open)"
        }
        return base
    }

    suspend fun addKpiReading(key: String, value: Double, note: String? = null) =
        withContext(Dispatchers.IO) {
            kpiDao.insert(
                KpiSnapshot(
                    id = "kpi_${key}_${timeProvider.nowMillis()}",
                    key = key,
                    value = value,
                    date = timeProvider.today(),
                    note = note,
                    createdAt = timeProvider.nowMillis()
                )
            )
        }

    suspend fun activeGoalsCount(): Int = withContext(Dispatchers.IO) {
        goalDao.allGoals().count { it.status == GoalStatus.ACTIVE }
    }
}
