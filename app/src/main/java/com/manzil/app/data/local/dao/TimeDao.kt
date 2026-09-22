package com.manzil.app.data.local.dao

import androidx.room.*
import com.manzil.app.data.local.entity.TimeEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeDao {
    @Query("SELECT * FROM time_entries ORDER BY startedAt DESC")
    fun getAllEntries(): Flow<List<TimeEntry>>

    @Query("SELECT * FROM time_entries WHERE taskId = :taskId ORDER BY startedAt DESC")
    fun getEntriesForTask(taskId: String): Flow<List<TimeEntry>>

    @Query("SELECT * FROM time_entries WHERE goalId = :goalId ORDER BY startedAt DESC")
    fun getEntriesForGoal(goalId: String): Flow<List<TimeEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: TimeEntry)

    @Delete
    suspend fun deleteEntry(entry: TimeEntry)

    @Query("SELECT SUM(CASE WHEN endedAt IS NOT NULL THEN endedAt - startedAt ELSE 0 END) FROM time_entries")
    suspend fun getTotalTrackedMillis(): Long
}
