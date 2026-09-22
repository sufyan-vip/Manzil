package com.manzil.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

enum class TaskStatus { TODO, IN_PROGRESS, DONE, CANCELLED, BLOCKED }

@Entity(tableName = "tasks", indices = [Index("goalId"), Index("dueDate"), Index("status")])
data class Task(
    @PrimaryKey val id: String,
    val title: String,
    val notes: String = "",
    val goalId: String? = null,
    val milestoneId: String? = null,
    val parentTaskId: String? = null,
    val dueDate: LocalDate?,
    val dueTime: LocalTime? = null,
    val startDate: LocalDate? = null,
    val estimatedMinutes: Int? = null,
    val actualMinutes: Int = 0,
    val status: TaskStatus = TaskStatus.TODO,
    val priority: Int = 2,
    val recurrenceRule: String? = null,
    val recurrenceEndDate: LocalDate? = null,
    val reminderMinutesBefore: Int? = null,
    val completedAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
    // BS SE Edition - Adaptive fields
    val autoRolledCount: Int = 0, // How many times auto-moved to tomorrow
    val lastAiReason: String? = null, // Why AI moved it
    val clientPlatform: String? = null, // Instagram, LinkedIn, etc.
    val isLatestInfoUpdated: Boolean = false
)

@Entity(tableName = "task_instances", indices = [Index("taskId"), Index("occurrenceDate")], primaryKeys = ["taskId", "occurrenceDate"])
data class TaskInstance(
    val taskId: String,
    val occurrenceDate: LocalDate,
    val status: TaskStatus = TaskStatus.TODO,
    val completedAt: Long? = null,
    val movedFrom: LocalDate? = null
)

enum class TimeSource { MANUAL, TIMER, IMPORTED }

@Entity(tableName = "time_entries", indices = [Index("taskId"), Index("startedAt")])
data class TimeEntry(
    @PrimaryKey val id: String, val taskId: String?, val goalId: String?,
    val label: String, val startedAt: Long, val endedAt: Long?,
    val source: TimeSource
)
