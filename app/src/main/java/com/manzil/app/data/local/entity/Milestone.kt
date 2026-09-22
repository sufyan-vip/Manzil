package com.manzil.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "milestones", indices = [Index("goalId")])
data class Milestone(
    @PrimaryKey val id: String, val goalId: String,
    val title: String, val dueDate: LocalDate?,
    val done: Boolean = false, val doneAt: Long? = null,
    val sortOrder: Int = 0, val createdAt: Long
)
