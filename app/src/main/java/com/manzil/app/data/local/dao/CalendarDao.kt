package com.manzil.app.data.local.dao

import androidx.room.*
import com.manzil.app.data.local.entity.CalendarEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar_events WHERE startAt BETWEEN :from AND :to ORDER BY startAt ASC")
    fun getEventsBetween(from: Long, to: Long): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events ORDER BY startAt DESC")
    fun getAllEvents(): Flow<List<CalendarEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEvent)

    @Update
    suspend fun updateEvent(event: CalendarEvent)

    @Delete
    suspend fun deleteEvent(event: CalendarEvent)

    @Query("SELECT * FROM calendar_events WHERE id = :id")
    suspend fun getEventById(id: String): CalendarEvent?
}
