package com.manzil.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import java.time.LocalDate

@Entity(tableName = "task_instances", indices = [Index("taskId"), Index("occurrenceDate")], primaryKeys = ["taskId", "occurrenceDate"])
data class TaskInstance(
    val taskId: String,
    val occurrenceDate: LocalDate,
    val status: TaskStatus = TaskStatus.TODO,
    val completedAt: Long? = null,
    val movedFrom: LocalDate? = null
)
