package com.manzil.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey val id: String, val name: String, val icon: String, val colorArgb: Int,
    val targetPerWeek: Int = 7, val active: Boolean = true, val createdAt: Long
)
