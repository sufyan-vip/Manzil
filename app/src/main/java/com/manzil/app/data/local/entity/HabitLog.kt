package com.manzil.app.data.local.entity

import androidx.room.Entity
import java.time.LocalDate

@Entity(tableName = "habit_logs", primaryKeys = ["habitId", "date"])
data class HabitLog(val habitId: String, val date: LocalDate, val done: Boolean, val at: Long)
