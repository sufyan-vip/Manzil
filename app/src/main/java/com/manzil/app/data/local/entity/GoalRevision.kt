package com.manzil.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "goal_revisions", indices = [Index("goalId"), Index("createdAt")])
data class GoalRevision(
    @PrimaryKey val id: String,
    val goalId: String,
    val snapshotJson: String,
    val changedFieldsJson: String,
    val changeType: ChangeType,
    val note: String? = null,
    val createdAt: Long
)
