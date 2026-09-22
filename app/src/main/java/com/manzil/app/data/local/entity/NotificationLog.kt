package com.manzil.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_logs")
data class NotificationLog(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val body: String,
    val sentAt: Long
)
