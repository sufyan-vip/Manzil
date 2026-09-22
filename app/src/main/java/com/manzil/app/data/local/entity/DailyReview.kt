package com.manzil.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_reviews", indices = [Index("date")])
data class DailyReview(
    @PrimaryKey val id: String, val date: LocalDate,
    val plannedCount: Int, val doneCount: Int, val pendingCount: Int, val overdueCount: Int,
    val focusMinutes: Int, val scorePercent: Int,
    val pendingTitlesJson: String,
    val aiSummary: String? = null,
    val createdAt: Long
)
