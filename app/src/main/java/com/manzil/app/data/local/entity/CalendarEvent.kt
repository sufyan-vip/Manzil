package com.manzil.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class EventSource { LOCAL, TASK, EXAM, CLIENT, IMPORTED, BSSE_CLASS }

@Entity(tableName = "calendar_events", indices = [Index("startAt"), Index("source")])
data class CalendarEvent(
    @PrimaryKey val id: String,
    val title: String, val description: String = "",
    val startAt: Long, val endAt: Long, val allDay: Boolean = false,
    val location: String? = null,
    val source: EventSource = EventSource.LOCAL,
    val linkedTaskId: String? = null,
    val colorArgb: Int? = null,
    val createdAt: Long, val updatedAt: Long
)
