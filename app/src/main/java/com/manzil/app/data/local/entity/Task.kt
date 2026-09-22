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
    val autoRolledCount: Int = 0,
    val lastAiReason: String? = null,
    val clientPlatform: String? = null,
    val isLatestInfoUpdated: Boolean = false
)
