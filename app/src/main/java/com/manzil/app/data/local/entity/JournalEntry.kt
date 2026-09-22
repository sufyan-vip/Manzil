package com.manzil.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "journal_entries", indices = [Index("date")])
data class JournalEntry(
    @PrimaryKey val id: String, val date: LocalDate,
    val mood: Int = 3,
    val wins: String = "", val blockers: String = "", val note: String = "",
    val createdAt: Long, val updatedAt: Long
)

@Entity(tableName = "daily_reviews", indices = [Index("date")])
data class DailyReview(
    @PrimaryKey val id: String, val date: LocalDate,
    val plannedCount: Int, val doneCount: Int, val pendingCount: Int, val overdueCount: Int,
    val focusMinutes: Int, val scorePercent: Int,
    val pendingTitlesJson: String,
    val aiSummary: String? = null,
    val createdAt: Long
)

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey val id: String, val name: String, val icon: String, val colorArgb: Int,
    val targetPerWeek: Int = 7, val active: Boolean = true, val createdAt: Long
)

@Entity(tableName = "habit_logs", primaryKeys = ["habitId", "date"])
data class HabitLog(val habitId: String, val date: LocalDate, val done: Boolean, val at: Long)

@Entity(tableName = "kpi_snapshots", indices = [Index("key"), Index("date")])
data class KpiSnapshot(
    @PrimaryKey val id: String, val key: String,
    val value: Double, val date: LocalDate, val note: String? = null, val createdAt: Long
)

@Entity(tableName = "search_docs")
data class SearchDoc(
    @PrimaryKey val id: String,
    val entityType: String,
    val entityId: String,
    val title: String, val body: String,
    val goalTitle: String? = null,
    val dateIso: String? = null,
    val updatedAt: Long
)

@Entity(tableName = "notification_logs")
data class NotificationLog(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val body: String,
    val sentAt: Long
)

@Entity(tableName = "settings")
data class Setting(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long
)
