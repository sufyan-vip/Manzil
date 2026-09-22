package com.manzil.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "time_entries", indices = [Index("taskId"), Index("startedAt")])
data class TimeEntry(
    @PrimaryKey val id: String, val taskId: String?, val goalId: String?,
    val label: String, val startedAt: Long, val endedAt: Long?,
    val source: TimeSource
)
enum class TimeSource { MANUAL, TIMER, IMPORTED }
