package com.manzil.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

enum class GoalCategory { SKILL, CLIENT, MONEY, HEALTH, EDUCATION, BUSINESS, PERSONAL, BSSE }
enum class GoalStatus { ACTIVE, PAUSED, DONE, DROPPED }
enum class ChangeType { CREATED, UPDATED, PROGRESS, METRIC, STATUS, DELETED }

@Entity(tableName = "goals", indices = [Index("parentGoalId"), Index("status")])
data class Goal(
    @PrimaryKey val id: String,
    val title: String,
    val description: String = "",
    val category: GoalCategory = GoalCategory.PERSONAL,
    val parentGoalId: String? = null,
    val status: GoalStatus = GoalStatus.ACTIVE,
    val priority: Int = 2,
    val startDate: LocalDate,
    val targetDate: LocalDate?,
    val progressPercent: Int = 0,
    val metricLabel: String? = null,
    val metricTarget: Double? = null,
    val metricCurrent: Double = 0.0,
    val metricUnit: String? = null,
    val sortOrder: Int = 0,
    val createdAt: Long,
    val updatedAt: Long,
    val archivedAt: Long? = null,
    // BS SE Edition additions
    val isPerpetual: Boolean = true, // No fixed 2030, runs till DONE
    val adaptiveEnabled: Boolean = true
)

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

@Entity(tableName = "milestones", indices = [Index("goalId")])
data class Milestone(
    @PrimaryKey val id: String, val goalId: String,
    val title: String, val dueDate: LocalDate?,
    val done: Boolean = false, val doneAt: Long? = null,
    val sortOrder: Int = 0, val createdAt: Long
)
