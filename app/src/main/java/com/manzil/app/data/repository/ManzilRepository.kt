package com.manzil.app.data.repository

import com.manzil.app.core.common.Fmt
import com.manzil.app.core.common.ImportStats
import com.manzil.app.core.common.ParsedQuickCapture
import com.manzil.app.core.common.TimeProvider
import com.manzil.app.core.diff.DiffEntry
import com.manzil.app.core.diff.JsonDiff
import com.manzil.app.core.logging.AppLog
import com.manzil.app.core.markdown.RoadmapImporterFull
import com.manzil.app.core.recurrence.RRuleExpander
import com.manzil.app.core.recurrence.RRuleParser
import com.manzil.app.data.local.SeedData
import com.manzil.app.data.local.dao.CalendarDao
import com.manzil.app.data.local.dao.GoalDao
import com.manzil.app.data.local.dao.HabitDao
import com.manzil.app.data.local.dao.KpiDao
import com.manzil.app.data.local.dao.ReviewDao
import com.manzil.app.data.local.dao.TaskDao
import com.manzil.app.data.local.dao.TimeDao
import com.manzil.app.data.local.entity.CalendarEvent
import com.manzil.app.data.local.entity.ChangeType
import com.manzil.app.data.local.entity.DailyReview
import com.manzil.app.data.local.entity.EventSource
import com.manzil.app.data.local.entity.Goal
import com.manzil.app.data.local.entity.GoalCategory
import com.manzil.app.data.local.entity.GoalRevision
import com.manzil.app.data.local.entity.GoalStatus
import com.manzil.app.data.local.entity.Habit
import com.manzil.app.data.local.entity.HabitLog
import com.manzil.app.data.local.entity.JournalEntry
import com.manzil.app.data.local.entity.Milestone
import com.manzil.app.data.local.entity.Task
import com.manzil.app.data.local.entity.TaskInstance
import com.manzil.app.data.local.entity.TaskStatus
import com.manzil.app.data.prefs.SettingsRepository
import com.manzil.app.domain.model.DayTask
import com.manzil.app.domain.model.HabitRow
import com.manzil.app.domain.model.PulseItem
import com.manzil.app.domain.model.TodayBriefing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single source of truth for everything that is not analytics: tasks, goals,
 * milestones, goal revisions (GOAL PULSE), calendar events, journal and habits.
 */
@Singleton
class ManzilRepository @Inject constructor(
    private val goalDao: GoalDao,
    private val taskDao: TaskDao,
    private val calendarDao: CalendarDao,
    private val reviewDao: ReviewDao,
    private val habitDao: HabitDao,
    private val kpiDao: KpiDao,
    private val settings: SettingsRepository,
    private val timeProvider: TimeProvider,
    private val importer: RoadmapImporterFull
) {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    /* --------------------------------------------------------------------------------------- */
    /* Observation                                                                              */
    /* --------------------------------------------------------------------------------------- */

    fun observeGoals(): Flow<List<Goal>> = goalDao.observeGoals()

    fun observeMilestones(): Flow<List<Milestone>> = goalDao.observeMilestones()

    fun observeRevisions(): Flow<List<GoalRevision>> = goalDao.observeRevisions(200)

    fun observeAllTasks(): Flow<List<Task>> = taskDao.observeAll()

    fun observeOverdue(): Flow<List<Task>> = taskDao.observeOverdue(timeProvider.today())

    fun observeEvents(from: Long, to: Long): Flow<List<CalendarEvent>> =
        calendarDao.observeBetween(from, to)

    fun observeJournals(): Flow<List<JournalEntry>> = reviewDao.observeJournals()

    fun observeReviews(): Flow<List<DailyReview>> = reviewDao.observeReviews()

    fun observeHabits(): Flow<List<HabitRow>> = combine(
        habitDao.observeHabits(),
        habitDao.observeLogsBetween(timeProvider.today().minusDays(6), timeProvider.today())
    ) { habits, logs ->
        val today = timeProvider.today()
        habits.map { habit ->
            val byDate = logs.filter { it.habitId == habit.id }.associate { it.date to it.done }
            HabitRow(
                id = habit.id,
                name = habit.name,
                icon = habit.icon,
                colorArgb = habit.colorArgb,
                targetPerWeek = habit.targetPerWeek,
                doneToday = byDate[today] ?: false,
                weekDots = (6 downTo 0).map { back -> byDate[today.minusDays(back.toLong())] ?: false }
            )
        }
    }

    /** Live list for one day: one-off tasks plus recurring occurrences. */
    fun observeDayTasks(date: LocalDate): Flow<List<DayTask>> = combine(
        taskDao.observeForDate(date),
        taskDao.observeInstancesForDate(date),
        taskDao.observeRecurring()
    ) { oneOff, instances, recurring ->
        val recurringById = recurring.associateBy { it.id }
        val scheduled = instances.mapNotNull { instance ->
            recurringById[instance.taskId]?.let { task ->
                DayTask(
                    task = task,
                    date = date,
                    done = instance.status == TaskStatus.DONE,
                    completedAt = instance.completedAt,
                    fromInstance = true
                )
            }
        }
        val plain = oneOff
            .filter { it.parentTaskId == null }
            .map { task ->
                DayTask(
                    task = task,
                    date = date,
                    done = task.status == TaskStatus.DONE,
                    completedAt = task.completedAt,
                    fromInstance = false
                )
            }
        (plain + scheduled)
            .filter { it.task.status != TaskStatus.CANCELLED }
            .sortedWith(compareBy({ it.done }, { it.task.sortOrder }, { it.task.dueTime ?: LocalTime.MAX }))
    }

    suspend fun dayTasks(date: LocalDate): List<DayTask> = withContext(Dispatchers.IO) {
        val collected = mutableListOf<DayTask>()
        taskDao.tasksForDate(date)
            .filter { it.parentTaskId == null }
            .forEach { task ->
                collected += DayTask(task, date, task.status == TaskStatus.DONE, task.completedAt, false)
            }
        taskDao.instancesForDate(date).forEach { instance ->
            taskDao.taskById(instance.taskId)?.let { task ->
                collected += DayTask(task, date, instance.status == TaskStatus.DONE, instance.completedAt, true)
            }
        }
        collected
            .filter { it.task.status != TaskStatus.CANCELLED }
            .sortedWith(compareBy({ it.done }, { it.task.sortOrder }, { it.task.dueTime ?: LocalTime.MAX }))
    }

    /* --------------------------------------------------------------------------------------- */
    /* Tasks                                                                                    */
    /* --------------------------------------------------------------------------------------- */

    suspend fun createTask(
        title: String,
        dueDate: LocalDate?,
        dueTime: LocalTime? = null,
        priority: Int = 2,
        goalId: String? = null,
        notes: String = "",
        estimatedMinutes: Int? = null,
        recurrenceRule: String? = null,
        reminderMinutesBefore: Int? = null
    ): String = withContext(Dispatchers.IO) {
        val now = timeProvider.nowMillis()
        val id = "task_" + UUID.randomUUID().toString().take(8)
        taskDao.insert(
            Task(
                id = id,
                title = title.trim().ifBlank { "Untitled task" },
                notes = notes,
                goalId = goalId,
                dueDate = dueDate,
                dueTime = dueTime,
                estimatedMinutes = estimatedMinutes,
                priority = priority.coerceIn(1, 4),
                recurrenceRule = recurrenceRule,
                reminderMinutesBefore = reminderMinutesBefore,
                createdAt = now,
                updatedAt = now,
                sortOrder = nextSortOrder(dueDate)
            )
        )
        if (recurrenceRule != null && dueDate != null) {
            expandTask(id, recurrenceRule, dueDate)
        }
        id
    }

    /** Quick capture: parsed text in, real task out. */
    suspend fun createFromQuickCapture(parsed: ParsedQuickCapture, goalId: String?): String =
        createTask(
            title = parsed.title.ifBlank { "New task" },
            dueDate = parsed.dueDate ?: timeProvider.today(),
            dueTime = parsed.dueTime,
            priority = parsed.priority,
            goalId = goalId
        )

    suspend fun updateTask(task: Task, reExpand: Boolean = false) = withContext(Dispatchers.IO) {
        val now = timeProvider.nowMillis()
        taskDao.update(task.copy(updatedAt = now))
        if (reExpand && task.recurrenceRule != null) {
            val start = task.startDate ?: task.dueDate ?: timeProvider.today()
            taskDao.deleteInstancesForTask(task.id)
            expandTask(task.id, task.recurrenceRule, start)
        }
    }

    suspend fun deleteTask(task: Task) = withContext(Dispatchers.IO) {
        taskDao.deleteInstancesForTask(task.id)
        taskDao.delete(task)
    }

    suspend fun toggleTaskDone(task: Task, done: Boolean, date: LocalDate) =
        withContext(Dispatchers.IO) {
            val now = timeProvider.nowMillis()
            if (task.recurrenceRule != null) {
                val existing = taskDao.instance(task.id, date)
                val instance = existing?.copy(
                    status = if (done) TaskStatus.DONE else TaskStatus.TODO,
                    completedAt = if (done) now else null
                ) ?: TaskInstance(
                    taskId = task.id,
                    occurrenceDate = date,
                    status = if (done) TaskStatus.DONE else TaskStatus.TODO,
                    completedAt = if (done) now else null
                )
                taskDao.insertInstance(instance)
            } else {
                taskDao.update(
                    task.copy(
                        status = if (done) TaskStatus.DONE else TaskStatus.TODO,
                        completedAt = if (done) now else null,
                        updatedAt = now
                    )
                )
            }
        }

    suspend fun setTaskStatus(task: Task, status: TaskStatus) = withContext(Dispatchers.IO) {
        taskDao.update(
            task.copy(
                status = status,
                completedAt = if (status == TaskStatus.DONE) timeProvider.nowMillis() else null,
                updatedAt = timeProvider.nowMillis()
            )
        )
    }

    suspend fun moveTask(task: Task, toDate: LocalDate?, from: LocalDate?) = withContext(Dispatchers.IO) {
        val now = timeProvider.nowMillis()
        if (task.recurrenceRule != null && from != null && toDate != null) {
            taskDao.moveInstance(task.id, from, toDate)
        } else {
            taskDao.update(
                task.copy(
                    dueDate = toDate,
                    autoRolledCount = task.autoRolledCount + 1,
                    sortOrder = nextSortOrder(toDate),
                    updatedAt = now
                )
            )
        }
    }

    /** "Postpone" — never a nag, just a recorded decision. */
    suspend fun postponeTask(task: Task, from: LocalDate, toDate: LocalDate, reason: String) =
        withContext(Dispatchers.IO) {
            val now = timeProvider.nowMillis()
            if (task.recurrenceRule != null) {
                taskDao.moveInstance(task.id, from, toDate)
            } else {
                taskDao.update(
                    task.copy(
                        dueDate = toDate,
                        autoRolledCount = task.autoRolledCount + 1,
                        lastAiReason = reason,
                        sortOrder = nextSortOrder(toDate),
                        updatedAt = now
                    )
                )
            }
        }

    suspend fun dropTask(task: Task) = withContext(Dispatchers.IO) {
        taskDao.update(
            task.copy(
                status = TaskStatus.CANCELLED,
                updatedAt = timeProvider.nowMillis()
            )
        )
    }

    suspend fun reorderDayTasks(ordered: List<DayTask>) = withContext(Dispatchers.IO) {
        ordered.forEachIndexed { index, dayTask ->
            if (dayTask.task.sortOrder != index) {
                taskDao.update(dayTask.task.copy(sortOrder = index))
            }
        }
    }

    suspend fun addMinutesToTask(taskId: String, minutes: Int) = withContext(Dispatchers.IO) {
        taskDao.taskById(taskId)?.let { task ->
            taskDao.update(task.copy(actualMinutes = task.actualMinutes + minutes, updatedAt = timeProvider.nowMillis()))
        }
    }

    suspend fun subtasksOf(parentId: String): List<Task> = withContext(Dispatchers.IO) {
        taskDao.allActive().filter { it.parentTaskId == parentId }
    }

    private suspend fun nextSortOrder(date: LocalDate?): Int =
        date?.let { taskDao.tasksForDate(it).size } ?: 0

    /* --------------------------------------------------------------------------------------- */
    /* Recurrence and rollover                                                                  */
    /* --------------------------------------------------------------------------------------- */

    data class MaintenanceReport(
        val rolledOver: Int,
        val instancesCreated: Int
    )

    suspend fun expandTask(taskId: String, rule: String, start: LocalDate) = withContext(Dispatchers.IO) {
        val parsed = runCatching { RRuleParser.parse(rule) }.getOrNull() ?: return@withContext
        val dates = RRuleExpander.expand(parsed, start, daysAhead = 60)
        taskDao.insertInstances(
            dates.map { date -> TaskInstance(taskId = taskId, occurrenceDate = date) }
        )
    }

    suspend fun expandAllRecurring(daysAhead: Int = 60) = withContext(Dispatchers.IO) {
        var created = 0
        taskDao.allActive()
            .filter { it.recurrenceRule != null && it.status != TaskStatus.DONE }
            .forEach { task ->
                val rule = runCatching { RRuleParser.parse(task.recurrenceRule!!) }.getOrNull() ?: return@forEach
                val start = task.startDate ?: task.dueDate ?: timeProvider.today()
                val horizon = timeProvider.today().plusDays(daysAhead.toLong())
                val existing = taskDao.instancesForTask(task.id).map { it.occurrenceDate }.toSet()
                val missing = RRuleExpander.expand(rule, start, daysAhead = Fmt.daysBetween(start, horizon) + 1)
                    .filter { !existing.contains(it) && !it.isBefore(timeProvider.today()) }
                if (missing.isNotEmpty()) {
                    taskDao.insertInstances(missing.map { TaskInstance(task.id, it) })
                    created += missing.size
                }
            }
        created
    }

    /**
     * Auto-rollover: anything still unfinished from before today moves to today with a
     * written reason, so the plan never silently rots. Runs once per day.
     */
    suspend fun runDailyMaintenance(): MaintenanceReport = withContext(Dispatchers.IO) {
        val today = timeProvider.today()
        val last = settings.current().lastRolloverDate
        if (last == today.toString()) {
            val createdFresh = expandAllRecurring()
            return@withContext MaintenanceReport(0, createdFresh)
        }

        val now = timeProvider.nowMillis()
        var rolled = 0
        taskDao.allActive()
            .filter { task ->
                task.recurrenceRule == null &&
                    task.dueDate != null &&
                    task.dueDate.isBefore(today) &&
                    task.status != TaskStatus.CANCELLED &&
                    task.status != TaskStatus.DONE
            }
            .forEach { task ->
                val daysLate = Fmt.daysBetween(task.dueDate!!, today)
                taskDao.update(
                    task.copy(
                        dueDate = today,
                        autoRolledCount = task.autoRolledCount + 1,
                        lastAiReason = if (daysLate == 1) {
                            "Kal reh gaya — aaj pehle karo. Rolled to today automatically."
                        } else {
                            "$daysLate din reh gaya — aaj ke plan mein le aaya gaya."
                        },
                        sortOrder = nextSortOrder(today),
                        updatedAt = now
                    )
                )
                rolled++
            }

        // recurring occurrences that were missed simply stay behind; nothing to move.
        settings.setLastRollover(today)
        val created = expandAllRecurring()
        MaintenanceReport(rolled, created)
    }

    /* --------------------------------------------------------------------------------------- */
    /* Goals, milestones, revisions (GOAL PULSE)                                                */
    /* --------------------------------------------------------------------------------------- */

    suspend fun createGoal(
        title: String,
        description: String = "",
        category: GoalCategory = GoalCategory.PERSONAL,
        parentGoalId: String? = null,
        targetDate: LocalDate? = null,
        priority: Int = 2,
        metricLabel: String? = null,
        metricTarget: Double? = null,
        metricUnit: String? = null,
        isPerpetual: Boolean = true
    ): String = withContext(Dispatchers.IO) {
        val now = timeProvider.nowMillis()
        val id = "goal_" + UUID.randomUUID().toString().take(8)
        val goal = Goal(
            id = id,
            title = title.trim().ifBlank { "Untitled goal" },
            description = description,
            category = category,
            parentGoalId = parentGoalId,
            priority = priority.coerceIn(1, 3),
            startDate = timeProvider.today(),
            targetDate = targetDate,
            metricLabel = metricLabel,
            metricTarget = metricTarget,
            metricUnit = metricUnit,
            sortOrder = goalDao.allGoals().count { it.parentGoalId == parentGoalId },
            createdAt = now,
            updatedAt = now,
            isPerpetual = isPerpetual
        )
        goalDao.insert(goal)
        recordRevision(goal, ChangeType.CREATED, emptyList(), "New goal added")
        id
    }

    suspend fun updateGoal(updated: Goal, recomputeProgress: Boolean = true) =
        withContext(Dispatchers.IO) {
            val before = goalDao.goalById(updated.id)
            val now = timeProvider.nowMillis()
            val withProgress = if (recomputeProgress) {
                updated.copy(progressPercent = computeProgressFor(updated.id), updatedAt = now)
            } else {
                updated.copy(updatedAt = now)
            }
            goalDao.update(withProgress)
            if (before != null) {
                val beforeJson = json.encodeToString(JsonObject.serializer(), goalToJson(before))
                val afterJson = json.encodeToString(JsonObject.serializer(), goalToJson(withProgress))
                val diffs = JsonDiff.diff(beforeJson, afterJson).filter { it.field != "updatedAt" }
                if (diffs.isNotEmpty()) {
                    val significant = diffs.any { it.field == "metricCurrent" || it.field == "metricTarget" }
                    val progressOnly = diffs.all { it.field == "progressPercent" }
                    val statusOnly = diffs.all { it.field == "status" }
                    val type = when {
                        significant -> ChangeType.METRIC
                        progressOnly -> ChangeType.PROGRESS
                        statusOnly -> ChangeType.STATUS
                        else -> ChangeType.UPDATED
                    }
                    recordRevision(
                        goal = withProgress,
                        changeType = type,
                        diffs = diffs,
                        note = null
                    )
                }
            }
        }

    suspend fun setGoalStatus(goal: Goal, status: GoalStatus) = withContext(Dispatchers.IO) {
        val before = goalDao.goalById(goal.id) ?: goal
        goalDao.update(goal.copy(status = status, updatedAt = timeProvider.nowMillis()))
        recordRevision(
            goal = goal.copy(status = status),
            changeType = ChangeType.STATUS,
            diffs = listOf(DiffEntry("status", before.status.name, status.name)),
            note = null
        )
    }

    suspend fun deleteGoal(goal: Goal) = withContext(Dispatchers.IO) {
        recordRevision(goal, ChangeType.DELETED, emptyList(), "Goal removed")
        goalDao.deleteMilestonesForGoal(goal.id)
        goalDao.delete(goal)
    }

    suspend fun trackMetricValue(goal: Goal, value: Double, note: String? = null) =
        withContext(Dispatchers.IO) {
            val before = goal.metricCurrent
            val updated = goal.copy(metricCurrent = value, updatedAt = timeProvider.nowMillis())
            goalDao.update(updated)
            recordRevision(
                goal = updated,
                changeType = ChangeType.METRIC,
                diffs = listOf(
                    DiffEntry("metricCurrent", Fmt.number(before), Fmt.number(value))
                ),
                note = note
            )
            kpiDao.insert(
                com.manzil.app.data.local.entity.KpiSnapshot(
                    id = "kpi_${goal.id}_${timeProvider.nowMillis()}",
                    key = "goal_${goal.id}",
                    value = value,
                    date = timeProvider.today(),
                    note = note,
                    createdAt = timeProvider.nowMillis()
                )
            )
        }

    suspend fun addMilestone(goalId: String, title: String, dueDate: LocalDate? = null): String =
        withContext(Dispatchers.IO) {
            val id = "ms_" + UUID.randomUUID().toString().take(8)
            goalDao.insertMilestone(
                Milestone(
                    id = id,
                    goalId = goalId,
                    title = title.trim().ifBlank { "Untitled milestone" },
                    dueDate = dueDate,
                    sortOrder = goalDao.allMilestones().count { it.goalId == goalId },
                    createdAt = timeProvider.nowMillis()
                )
            )
            id
        }

    suspend fun toggleMilestone(milestone: Milestone, done: Boolean) = withContext(Dispatchers.IO) {
        val now = timeProvider.nowMillis()
        goalDao.updateMilestone(
            milestone.copy(done = done, doneAt = if (done) now else null)
        )
        refreshProgress(milestone.goalId)
    }

    suspend fun deleteMilestone(milestone: Milestone) = withContext(Dispatchers.IO) {
        goalDao.deleteMilestone(milestone)
        refreshProgress(milestone.goalId)
    }

    /** Recomputes this goal and every ancestor after task/milestone changes. */
    suspend fun refreshProgress(goalId: String) = withContext(Dispatchers.IO) {
        var cursor: String? = goalId
        var guard = 0
        while (cursor != null && guard < 12) {
            val goal = goalDao.goalById(cursor) ?: break
            val progress = computeProgressFor(goal.id)
            if (goal.progressPercent != progress) {
                goalDao.update(goal.copy(progressPercent = progress, updatedAt = timeProvider.nowMillis()))
            }
            cursor = goal.parentGoalId
            guard++
        }
    }

    suspend fun refreshAllProgress() = withContext(Dispatchers.IO) {
        goalDao.allGoals().forEach { goal ->
            val progress = computeProgressFor(goal.id)
            if (goal.progressPercent != progress) {
                goalDao.update(goal.copy(progressPercent = progress))
            }
        }
    }

    private suspend fun computeProgressFor(goalId: String): Int {
        val milestones = goalDao.allMilestones().filter { it.goalId == goalId }
        val children = goalDao.allGoals().filter { it.parentGoalId == goalId }
        val tasks = taskDao.allActive().filter { it.goalId == goalId && it.parentTaskId == null }

        val milestoneScore = if (milestones.isEmpty()) null
        else (milestones.count { it.done } * 100) / milestones.size

        val taskScore = if (tasks.isEmpty()) null
        else (tasks.count { it.status == TaskStatus.DONE } * 100) / tasks.size

        val ownScore = milestoneScore ?: taskScore

        return when {
            children.isNotEmpty() -> {
                val weights = children.map { 4 - it.priority.coerceIn(1, 3) }
                val childScores = children.map { computeProgressFor(it.id) }
                val totalWeight = weights.sum()
                val weighted = childScores.zip(weights).sumOf { (score, weight) -> score * weight }
                val fromChildren = if (totalWeight > 0) weighted / totalWeight else childScores.average().toInt()
                if (ownScore != null) (fromChildren * 2 + ownScore) / 3 else fromChildren
            }
            ownScore != null -> ownScore
            else -> 0
        }
    }

    private suspend fun recordRevision(
        goal: Goal,
        changeType: ChangeType,
        diffs: List<com.manzil.app.core.diff.DiffEntry>,
        note: String?
    ) {
        val changedJson = buildJsonObject {
            put(
                "changes",
                JsonArray(
                    diffs.map { diff -> buildJsonObject {
                        put("field", JsonPrimitive(diff.field))
                        put("from", JsonPrimitive(diff.from))
                        put("to", JsonPrimitive(diff.to))
                    } }
                )
            )
        }
        goalDao.insertRevision(
            GoalRevision(
                id = "rev_" + UUID.randomUUID().toString().take(10),
                goalId = goal.id,
                snapshotJson = json.encodeToString(JsonObject.serializer(), goalToJson(goal)),
                changedFieldsJson = json.encodeToString(JsonObject.serializer(), changedJson),
                changeType = changeType,
                note = note,
                createdAt = timeProvider.nowMillis()
            )
        )
    }

    private fun goalToJson(goal: Goal): JsonObject = buildJsonObject {
        put("title", JsonPrimitive(goal.title))
        put("description", JsonPrimitive(goal.description))
        put("category", JsonPrimitive(goal.category.name))
        put("status", JsonPrimitive(goal.status.name))
        put("priority", JsonPrimitive(goal.priority))
        put("startDate", JsonPrimitive(goal.startDate.toString()))
        put("targetDate", JsonPrimitive(goal.targetDate?.toString() ?: ""))
        put("progressPercent", JsonPrimitive(goal.progressPercent))
        put("metricLabel", JsonPrimitive(goal.metricLabel ?: ""))
        put("metricTarget", JsonPrimitive(goal.metricTarget ?: 0.0))
        put("metricCurrent", JsonPrimitive(goal.metricCurrent))
        put("metricUnit", JsonPrimitive(goal.metricUnit ?: ""))
        put("updatedAt", JsonPrimitive(goal.updatedAt))
    }

    /** GOAL PULSE feed: newest first, already parsed into field-level diffs. */
    suspend fun revisionFeed(since: Long = 0L): List<PulseItem> = withContext(Dispatchers.IO) {
        val goals = goalDao.allGoals().associateBy { it.id }
        goalDao.allRevisions()
            .filter { it.createdAt >= since }
            .map { revision ->
                PulseItem(
                    id = revision.id,
                    goalTitle = goals[revision.goalId]?.title ?: "Removed goal",
                    changeType = revision.changeType,
                    changes = parseChanges(revision.changedFieldsJson),
                    note = revision.note,
                    atMillis = revision.createdAt
                )
            }
    }

    suspend fun revisionsForGoal(goalId: String): List<GoalRevision> = withContext(Dispatchers.IO) {
        goalDao.allRevisions().filter { it.goalId == goalId }
    }

    /* --------------------------------------------------------------------------------------- */
    /* Calendar                                                                                 */
    /* --------------------------------------------------------------------------------------- */

    suspend fun createEvent(
        title: String,
        description: String = "",
        startAt: Long,
        endAt: Long,
        source: EventSource = EventSource.LOCAL,
        location: String? = null,
        linkedTaskId: String? = null,
        colorArgb: Int? = null
    ): String = withContext(Dispatchers.IO) {
        val now = timeProvider.nowMillis()
        val id = "event_" + UUID.randomUUID().toString().take(8)
        calendarDao.insert(
            CalendarEvent(
                id = id,
                title = title.trim().ifBlank { "Untitled event" },
                description = description,
                startAt = startAt,
                endAt = if (endAt <= startAt) startAt + 30 * 60 * 1000 else endAt,
                location = location,
                source = source,
                linkedTaskId = linkedTaskId,
                colorArgb = colorArgb,
                createdAt = now,
                updatedAt = now
            )
        )
        id
    }

    suspend fun updateEvent(event: CalendarEvent) = withContext(Dispatchers.IO) {
        calendarDao.update(event.copy(updatedAt = timeProvider.nowMillis()))
    }

    suspend fun deleteEvent(event: CalendarEvent) = withContext(Dispatchers.IO) {
        calendarDao.delete(event)
    }

    suspend fun eventsBetween(from: Long, to: Long): List<CalendarEvent> = withContext(Dispatchers.IO) {
        calendarDao.between(from, to)
    }

    /* --------------------------------------------------------------------------------------- */
    /* Time tracking                                                                            */
    /* --------------------------------------------------------------------------------------- */

    suspend fun saveTimeEntry(entry: TimeEntry, addMinutesToTask: Boolean = true) =
        withContext(Dispatchers.IO) {
            timeDao.insert(entry)
            val taskId = entry.taskId
            val endedAt = entry.endedAt
            if (addMinutesToTask && taskId != null && endedAt != null) {
                val minutes = ((endedAt - entry.startedAt) / 60000L).toInt()
                if (minutes > 0) addMinutesToTask(taskId, minutes)
            }
        }

    suspend fun timeEntriesBetween(from: Long, to: Long): List<TimeEntry> = withContext(Dispatchers.IO) {
        timeDao.between(from, to)
    }

    suspend fun deleteTimeEntry(entry: TimeEntry) = withContext(Dispatchers.IO) {
        timeDao.delete(entry)
    }

    /* --------------------------------------------------------------------------------------- */
    /* Journal, reviews, habits                                                                 */
    /* --------------------------------------------------------------------------------------- */

    suspend fun journalFor(date: LocalDate): JournalEntry? = withContext(Dispatchers.IO) {
        reviewDao.journalForDate(date)
    }

    suspend fun saveJournal(
        date: LocalDate,
        mood: Int,
        wins: String,
        blockers: String,
        note: String
    ) = withContext(Dispatchers.IO) {
        val existing = reviewDao.journalForDate(date)
        val now = timeProvider.nowMillis()
        reviewDao.insertJournal(
            JournalEntry(
                id = existing?.id ?: "journal_${UUID.randomUUID().toString().take(8)}",
                date = date,
                mood = mood.coerceIn(1, 5),
                wins = wins,
                blockers = blockers,
                note = note,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now
            )
        )
    }

    suspend fun saveDailyReview(date: LocalDate) = withContext(Dispatchers.IO) {
        val planned = dayTasks(date)
        val done = planned.count { it.done }
        val overdue = planned.count { !it.done }
        val focusMinutes = timeDao.between(Fmt.startOfDayMillis(date), Fmt.endOfDayMillis(date))
            .sumOf { entry ->
                val end = entry.endedAt ?: timeProvider.nowMillis()
                ((end - entry.startedAt) / 60000L).toInt()
            }
        reviewDao.insertReview(
            DailyReview(
                id = "review_$date",
                date = date,
                plannedCount = planned.size,
                doneCount = done,
                pendingCount = overdue,
                overdueCount = overdue,
                focusMinutes = focusMinutes,
                scorePercent = if (planned.isEmpty()) 0 else (done * 100) / planned.size,
                pendingTitlesJson = json.encodeToString(
                    JsonArray.serializer(),
                    JsonArray(planned.filter { !it.done }.map { JsonPrimitive(it.task.title) })
                ),
                createdAt = timeProvider.nowMillis()
            )
        )
    }

    suspend fun toggleHabit(habitId: String, date: LocalDate, done: Boolean) =
        withContext(Dispatchers.IO) {
            if (done) {
                habitDao.upsertLog(HabitLog(habitId, date, true, timeProvider.nowMillis()))
            } else {
                habitDao.deleteLog(habitId, date)
            }
        }

    /* --------------------------------------------------------------------------------------- */
    /* Seeding, import, erase                                                                   */
    /* --------------------------------------------------------------------------------------- */

    suspend fun seedIfNeeded(force: Boolean = false) = withContext(Dispatchers.IO) {
        val current = settings.current()
        if (current.seededAt != null && !force) return@withContext
        val starter = SeedData.starter()
        if (goalDao.allGoals().isEmpty()) goalDao.insertAll(starter.goals)
        if (goalDao.allMilestones().isEmpty()) goalDao.insertMilestones(starter.milestones)
        if (taskDao.allActive().isEmpty()) taskDao.insertAll(starter.tasks)
        if (kpiDao.all().isEmpty()) kpiDao.insertAll(starter.kpis)
        if (habitDao.all().isEmpty()) habitDao.insertAll(starter.habits)
        starter.events.forEach { event ->
            if (calendarDao.eventById(event.id) == null) calendarDao.insert(event)
        }
        settings.markSeeded()
    }

    /** Import a markdown roadmap: ## headings become goals, - [ ] become tasks. */
    suspend fun importRoadmap(markdown: String): ImportStats = withContext(Dispatchers.IO) {
        val result = importer.import(markdown)
        val now = timeProvider.nowMillis()
        val goalsByTitle = mutableMapOf<String, String>()
        val existing = goalDao.allGoals().associateBy { it.title.lowercase() }

        result.goals.forEachIndexed { index, parsed ->
            val key = parsed.title.lowercase()
            val alreadyThere = existing[key]?.id ?: goalsByTitle[key]
            if (alreadyThere != null) {
                goalsByTitle[key] = alreadyThere
                return@forEachIndexed
            }
            val parentId = parsed.parent?.lowercase()?.let { goalsByTitle[it] }
            val id = "goal_imp_" + UUID.randomUUID().toString().take(8)
            goalDao.insert(
                Goal(
                    id = id,
                    title = parsed.title,
                    description = "Imported from roadmap",
                    category = GoalCategory.BSSE,
                    parentGoalId = parentId,
                    priority = if (parsed.level <= 2) 1 else 2,
                    startDate = timeProvider.today(),
                    targetDate = timeProvider.today().plusMonths(6),
                    sortOrder = index,
                    createdAt = now + index,
                    updatedAt = now + index
                )
            )
            goalsByTitle[key] = id
        }

        var taskCount = 0
        result.tasks.forEachIndexed { index, parsed ->
            val goalId = parsed.goal?.lowercase()?.let { goalsByTitle[it] }
            val today = timeProvider.today()
            taskDao.insert(
                Task(
                    id = "task_imp_" + UUID.randomUUID().toString().take(8),
                    title = parsed.title,
                    goalId = goalId,
                    dueDate = today.plusDays((index % 30).toLong()),
                    priority = 2,
                    status = if (parsed.done) TaskStatus.DONE else TaskStatus.TODO,
                    completedAt = if (parsed.done) now else null,
                    createdAt = now + index,
                    updatedAt = now + index,
                    sortOrder = index
                )
            )
            taskCount++
        }

        result.kpis.forEach { kpi ->
            val key = "imported_" + kpi.label.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
            kpiDao.insert(
                com.manzil.app.data.local.entity.KpiSnapshot(
                    id = "kpi_$key",
                    key = key,
                    value = 0.0,
                    date = timeProvider.today(),
                    note = "${kpi.label} — target ${kpi.target} ${kpi.unit ?: ""}".trim(),
                    createdAt = now
                )
            )
        }

        ImportStats(
            goals = result.goals.size,
            tasks = taskCount,
            kpis = result.kpis.size
        )
    }

    suspend fun eraseEverything() = withContext(Dispatchers.IO) {
        taskDao.clearInstances()
        taskDao.clearAll()
        goalDao.clearMilestones()
        goalDao.clearRevisions()
        goalDao.clearAll()
        kpiDao.clearAll()
        calendarDao.clearAll()
        timeDao.clearAll()
        habitDao.clearLogs()
        reviewDao.clearReviews()
        reviewDao.clearJournals()
        AppLog.d("ManzilRepository", "all local data erased")
        Unit
    }

    suspend fun rootGoal(): Goal? = withContext(Dispatchers.IO) {
        goalDao.rootGoals().firstOrNull()
    }

    suspend fun allGoals(): List<Goal> = withContext(Dispatchers.IO) { goalDao.allGoals() }

    suspend fun allMilestones(): List<Milestone> = withContext(Dispatchers.IO) { goalDao.allMilestones() }

    suspend fun allTasks(): List<Task> = withContext(Dispatchers.IO) { taskDao.allActive() }

    suspend fun taskById(id: String): Task? = withContext(Dispatchers.IO) { taskDao.taskById(id) }

    suspend fun parseChanges(changedFieldsJson: String): List<DiffEntry> {
        val element = runCatching { json.parseToJsonElement(changedFieldsJson) }.getOrNull() ?: return emptyList()
        val changes = (element as? JsonObject)?.get("changes") as? JsonArray ?: return emptyList()
        return changes.mapNotNull { entry ->
            val obj = entry as? JsonObject ?: return@mapNotNull null
            DiffEntry(
                field = obj["field"]?.jsonPrimitive?.content ?: return@mapNotNull null,
                from = obj["from"]?.jsonPrimitive?.content ?: "",
                to = obj["to"]?.jsonPrimitive?.content ?: ""
            )
        }
    }

    suspend fun briefingData(): TodayBriefing = withContext(Dispatchers.IO) {
        val today = timeProvider.today()
        val yesterday = today.minusDays(1)
        val todays = dayTasks(today)
        val yesterdays = dayTasks(yesterday)
        val root = rootGoal()
        val streak = completedStreak()
        val allGoals = goalDao.allGoals()
        val rootProgress = root?.progressPercent
            ?: if (allGoals.isEmpty()) 0 else allGoals.map { it.progressPercent }.average().toInt()

        TodayBriefing(
            dayCounter = root?.let { Fmt.daysBetween(it.startDate, today) + 1 } ?: 1,
            todayTasks = todays.filter { !it.done }.map { entry ->
                val time = entry.task.dueTime?.let { "${Fmt.time(it)}  " } ?: ""
                val estimate = entry.task.estimatedMinutes?.let { "  (${Fmt.duration(it)})" } ?: ""
                "$time${entry.task.title}$estimate"
            },
            yesterdayDone = yesterdays.count { it.done },
            yesterdayPlanned = yesterdays.size,
            pending = yesterdays.filter { !it.done }.map { it.task.title },
            goalMove = latestMetricChangeLine(),
            streak = streak.first,
            rootProgress = rootProgress
        )
    }

    private suspend fun latestMetricChangeLine(): String? {
        val revision = goalDao.allRevisions().firstOrNull {
            it.changeType == ChangeType.METRIC || it.changeType == ChangeType.PROGRESS
        } ?: return null
        val goal = goalDao.goalById(revision.goalId) ?: return null
        val changes = parseChanges(revision.changedFieldsJson)
        val metric = changes.firstOrNull()
        return when (revision.changeType) {
            ChangeType.METRIC -> metric?.let {
                "${goal.title}: ${it.field} ${it.from} → ${it.to}"
            }
            ChangeType.PROGRESS -> metric?.let {
                "${goal.title}: progress ${it.from}% → ${it.to}%"
            }
            else -> null
        }
    }

    /** Consecutive days (ending today or yesterday) with at least one completed task. */
    suspend fun completedStreak(): Pair<Int, Int> = withContext(Dispatchers.IO) {
        val counts = taskDao.completedTasks()
            .mapNotNull { it.completedAt }
            .groupingBy { Fmt.dayOfMillis(it) }
            .eachCount()
        if (counts.isEmpty()) return@withContext 0 to 0

        val days = counts.keys.sorted()
        var longest = 1
        var run = 1
        for (index in 1 until days.size) {
            run = if (Fmt.daysBetween(days[index - 1], days[index]) == 1) run + 1 else 1
            if (run > longest) longest = run
        }

        val today = timeProvider.today()
        var cursor = if (counts.containsKey(today)) today else today.minusDays(1)
        var current = 0
        while (counts.containsKey(cursor)) {
            current++
            cursor = cursor.minusDays(1)
        }
        current to longest
    }

    suspend fun completionsByDay(from: LocalDate, to: LocalDate): List<Pair<LocalDate, Int>> =
        withContext(Dispatchers.IO) {
            val counts = taskDao.completedBetween(Fmt.startOfDayMillis(from), Fmt.endOfDayMillis(to))
                .mapNotNull { it.completedAt }
                .groupingBy { Fmt.dayOfMillis(it) }
                .eachCount()
            var cursor = from
            val out = mutableListOf<Pair<LocalDate, Int>>()
            while (!cursor.isAfter(to)) {
                out += cursor to (counts[cursor] ?: 0)
                cursor = cursor.plusDays(1)
            }
            out
        }
}
