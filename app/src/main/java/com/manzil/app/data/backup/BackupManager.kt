package com.manzil.app.data.backup

import com.manzil.app.data.local.dao.CalendarDao
import com.manzil.app.data.local.dao.GoalDao
import com.manzil.app.data.local.dao.KpiDao
import com.manzil.app.data.local.dao.ReviewDao
import com.manzil.app.data.local.dao.TaskDao
import com.manzil.app.data.local.dao.TimeDao
import com.manzil.app.data.local.entity.CalendarEvent
import com.manzil.app.data.local.entity.EventSource
import com.manzil.app.data.local.entity.Goal
import com.manzil.app.data.local.entity.GoalCategory
import com.manzil.app.data.local.entity.GoalStatus
import com.manzil.app.data.local.entity.JournalEntry
import com.manzil.app.data.local.entity.KpiSnapshot
import com.manzil.app.data.local.entity.Milestone
import com.manzil.app.data.local.entity.Task
import com.manzil.app.data.local.entity.TaskStatus
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Plain-JSON export / import. Secrets are never included (the API key lives in
 * Keystore-encrypted storage and is excluded on purpose). Unknown fields from a
 * newer version are ignored instead of failing the import.
 */
@Singleton
class BackupManager @Inject constructor(
    private val goalDao: GoalDao,
    private val taskDao: TaskDao,
    private val kpiDao: KpiDao,
    private val calendarDao: CalendarDao,
    private val reviewDao: ReviewDao,
    private val timeDao: TimeDao
) {

    @Serializable
    data class GoalRecord(
        val id: String,
        val title: String,
        val description: String = "",
        val category: String = "PERSONAL",
        val parentGoalId: String? = null,
        val status: String = "ACTIVE",
        val priority: Int = 2,
        val startDate: String,
        val targetDate: String? = null,
        val progressPercent: Int = 0,
        val metricLabel: String? = null,
        val metricTarget: Double? = null,
        val metricCurrent: Double = 0.0,
        val metricUnit: String? = null,
        val isPerpetual: Boolean = true
    )

    @Serializable
    data class MilestoneRecord(
        val id: String,
        val goalId: String,
        val title: String,
        val dueDate: String? = null,
        val done: Boolean = false
    )

    @Serializable
    data class TaskRecord(
        val id: String,
        val title: String,
        val notes: String = "",
        val goalId: String? = null,
        val milestoneId: String? = null,
        val dueDate: String? = null,
        val dueTime: String? = null,
        val estimatedMinutes: Int? = null,
        val actualMinutes: Int = 0,
        val status: String = "TODO",
        val priority: Int = 2,
        val recurrenceRule: String? = null,
        val completedAt: Long? = null,
        val createdAt: Long = 0,
        val autoRolledCount: Int = 0
    )

    @Serializable
    data class KpiRecord(
        val key: String,
        val value: Double,
        val date: String,
        val note: String? = null
    )

    @Serializable
    data class EventRecord(
        val id: String,
        val title: String,
        val description: String = "",
        val startAt: Long,
        val endAt: Long,
        val source: String = "LOCAL"
    )

    @Serializable
    data class JournalRecord(
        val id: String,
        val date: String,
        val mood: Int,
        val wins: String = "",
        val blockers: String = "",
        val note: String = ""
    )

    @Serializable
    data class Backup(
        val schemaVersion: Int = SCHEMA_VERSION,
        val exportTime: Long = System.currentTimeMillis(),
        val app: String = "Manzil",
        val note: String = "Secrets are never exported. Offline-first JSON backup.",
        val goals: List<GoalRecord> = emptyList(),
        val milestones: List<MilestoneRecord> = emptyList(),
        val tasks: List<TaskRecord> = emptyList(),
        val kpis: List<KpiRecord> = emptyList(),
        val events: List<EventRecord> = emptyList(),
        val journals: List<JournalRecord> = emptyList()
    )

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun createBackupJson(): String {
        val goals = goalDao.allGoals().map { goal ->
            GoalRecord(
                id = goal.id,
                title = goal.title,
                description = goal.description,
                category = goal.category.name,
                parentGoalId = goal.parentGoalId,
                status = goal.status.name,
                priority = goal.priority,
                startDate = goal.startDate.toString(),
                targetDate = goal.targetDate?.toString(),
                progressPercent = goal.progressPercent,
                metricLabel = goal.metricLabel,
                metricTarget = goal.metricTarget,
                metricCurrent = goal.metricCurrent,
                metricUnit = goal.metricUnit,
                isPerpetual = goal.isPerpetual
            )
        }
        val milestones = goalDao.allMilestones().map {
            MilestoneRecord(it.id, it.goalId, it.title, it.dueDate?.toString(), it.done)
        }
        val tasks = taskDao.allActive().map { task ->
            TaskRecord(
                id = task.id,
                title = task.title,
                notes = task.notes,
                goalId = task.goalId,
                milestoneId = task.milestoneId,
                dueDate = task.dueDate?.toString(),
                dueTime = task.dueTime?.toString(),
                estimatedMinutes = task.estimatedMinutes,
                actualMinutes = task.actualMinutes,
                status = task.status.name,
                priority = task.priority,
                recurrenceRule = task.recurrenceRule,
                completedAt = task.completedAt,
                createdAt = task.createdAt,
                autoRolledCount = task.autoRolledCount
            )
        }
        val kpis = kpiDao.all().map { KpiRecord(it.key, it.value, it.date.toString(), it.note) }
        val events = calendarDao.between(0L, Long.MAX_VALUE - 1).map {
            EventRecord(it.id, it.title, it.description, it.startAt, it.endAt, it.source.name)
        }
        val journals = reviewDao.journalsSince(LocalDate.of(2000, 1, 1)).map {
            JournalRecord(it.id, it.date.toString(), it.mood, it.wins, it.blockers, it.note)
        }

        return json.encodeToString(
            Backup.serializer(),
            Backup(
                goals = goals,
                milestones = milestones,
                tasks = tasks,
                kpis = kpis,
                events = events,
                journals = journals
            )
        )
    }

    data class RestoreReport(
        val ok: Boolean,
        val message: String,
        val goals: Int = 0,
        val tasks: Int = 0
    )

    suspend fun restoreFromJson(raw: String): RestoreReport {
        val backup = runCatching { json.decodeFromString(Backup.serializer(), raw.trim()) }
            .getOrElse { return RestoreReport(false, "That file is not a Manzil backup.") }

        if (backup.schemaVersion > SCHEMA_VERSION) {
            // Forward compatible: we still import what we understand.
        }

        val now = System.currentTimeMillis()
        if (backup.goals.isNotEmpty()) {
            goalDao.insertAll(
                backup.goals.map { record ->
                    Goal(
                        id = record.id,
                        title = record.title,
                        description = record.description,
                        category = runCatching { GoalCategory.valueOf(record.category) }
                            .getOrDefault(GoalCategory.PERSONAL),
                        parentGoalId = record.parentGoalId,
                        status = runCatching { GoalStatus.valueOf(record.status) }
                            .getOrDefault(GoalStatus.ACTIVE),
                        priority = record.priority,
                        startDate = runCatching { LocalDate.parse(record.startDate) }
                            .getOrDefault(LocalDate.now()),
                        targetDate = record.targetDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
                        progressPercent = record.progressPercent,
                        metricLabel = record.metricLabel,
                        metricTarget = record.metricTarget,
                        metricCurrent = record.metricCurrent,
                        metricUnit = record.metricUnit,
                        createdAt = now,
                        updatedAt = now,
                        isPerpetual = record.isPerpetual
                    )
                }
            )
        }

        if (backup.milestones.isNotEmpty()) {
            goalDao.insertMilestones(
                backup.milestones.map { record ->
                    Milestone(
                        id = record.id,
                        goalId = record.goalId,
                        title = record.title,
                        dueDate = record.dueDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
                        done = record.done,
                        doneAt = if (record.done) now else null,
                        createdAt = now
                    )
                }
            )
        }

        if (backup.tasks.isNotEmpty()) {
            taskDao.insertAll(
                backup.tasks.map { record ->
                    Task(
                        id = record.id,
                        title = record.title,
                        notes = record.notes,
                        goalId = record.goalId,
                        milestoneId = record.milestoneId,
                        dueDate = record.dueDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
                        dueTime = record.dueTime?.let { runCatching { LocalTime.parse(it) }.getOrNull() },
                        estimatedMinutes = record.estimatedMinutes,
                        actualMinutes = record.actualMinutes,
                        status = runCatching { TaskStatus.valueOf(record.status) }
                            .getOrDefault(TaskStatus.TODO),
                        priority = record.priority,
                        recurrenceRule = record.recurrenceRule,
                        completedAt = record.completedAt,
                        createdAt = if (record.createdAt > 0) record.createdAt else now,
                        updatedAt = now,
                        autoRolledCount = record.autoRolledCount
                    )
                }
            )
        }

        backup.kpis.forEach { record ->
            kpiDao.insert(
                KpiSnapshot(
                    id = "kpi_${record.key}_${record.date}",
                    key = record.key,
                    value = record.value,
                    date = runCatching { LocalDate.parse(record.date) }.getOrDefault(LocalDate.now()),
                    note = record.note,
                    createdAt = now
                )
            )
        }

        backup.events.forEach { record ->
            calendarDao.insert(
                CalendarEvent(
                    id = record.id,
                    title = record.title,
                    description = record.description,
                    startAt = record.startAt,
                    endAt = record.endAt,
                    source = runCatching { EventSource.valueOf(record.source) }
                        .getOrDefault(EventSource.LOCAL),
                    createdAt = now,
                    updatedAt = now
                )
            )
        }

        backup.journals.forEach { record ->
            reviewDao.insertJournal(
                JournalEntry(
                    id = record.id,
                    date = runCatching { LocalDate.parse(record.date) }.getOrDefault(LocalDate.now()),
                    mood = record.mood,
                    wins = record.wins,
                    blockers = record.blockers,
                    note = record.note,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }

        return RestoreReport(
            ok = true,
            message = "Restored ${backup.goals.size} goals and ${backup.tasks.size} tasks.",
            goals = backup.goals.size,
            tasks = backup.tasks.size
        )
    }

    /** CSV of completed work — handy for a portfolio or a client report. */
    suspend fun createTaskCsv(): String {
        val header = "title,status,priority,goal_id,due_date,due_time,estimated_min,actual_min\n"
        return header + taskDao.allActive().joinToString("\n") { task ->
            listOf(
                task.title.replace(',', ' '),
                task.status.name,
                task.priority.toString(),
                task.goalId.orEmpty(),
                task.dueDate?.toString().orEmpty(),
                task.dueTime?.toString().orEmpty(),
                task.estimatedMinutes?.toString().orEmpty(),
                task.actualMinutes.toString()
            ).joinToString(",")
        }
    }

    companion object {
        const val SCHEMA_VERSION = 1
    }
}
