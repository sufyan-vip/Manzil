package com.manzil.app.domain.repository

import com.manzil.app.data.local.entity.CalendarEvent
import kotlinx.coroutines.flow.Flow

interface CalendarRepository {
    fun getEventsBetween(from: Long, to: Long): Flow<List<CalendarEvent>>
    suspend fun insertEvent(event: CalendarEvent)
}
