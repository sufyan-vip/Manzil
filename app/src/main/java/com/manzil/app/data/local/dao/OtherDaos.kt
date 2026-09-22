package com.manzil.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.manzil.app.data.local.entity.CalendarEvent
import com.manzil.app.data.local.entity.DailyReview
import com.manzil.app.data.local.entity.Habit
import com.manzil.app.data.local.entity.HabitLog
import com.manzil.app.data.local.entity.JournalEntry
import com.manzil.app.data.local.entity.KpiSnapshot
import com.manzil.app.data.local.entity.NotificationLog
import com.manzil.app.data.local.entity.TimeEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar_events WHERE endAt >= :from AND startAt <= :to ORDER BY startAt ASC")
    fun observeBetween(from: Long, to: Long): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events WHERE endAt >= :from AND startAt <= :to ORDER BY startAt ASC")
    suspend fun between(from: Long, to: Long): List<CalendarEvent>

    @Query("SELECT * FROM calendar_events ORDER BY startAt DESC")
    fun observeAll(): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events WHERE id = :id")
    suspend fun eventById(id: String): CalendarEvent?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: CalendarEvent)

    @Update
    suspend fun update(event: CalendarEvent)

    @Delete
    suspend fun delete(event: CalendarEvent)

    @Query("DELETE FROM calendar_events")
    suspend fun clearAll()
}

@Dao
interface TimeDao {
    @Query("SELECT * FROM time_entries ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<TimeEntry>>

    @Query("SELECT * FROM time_entries WHERE startedAt BETWEEN :from AND :to ORDER BY startedAt DESC")
    fun observeBetween(from: Long, to: Long): Flow<List<TimeEntry>>

    @Query("SELECT * FROM time_entries WHERE startedAt BETWEEN :from AND :to ORDER BY startedAt DESC")
    suspend fun between(from: Long, to: Long): List<TimeEntry>

    @Query("SELECT * FROM time_entries ORDER BY startedAt DESC")
    suspend fun all(): List<TimeEntry>

    @Query("SELECT * FROM time_entries WHERE id = :id")
    suspend fun entryById(id: String): TimeEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: TimeEntry)

    @Update
    suspend fun update(entry: TimeEntry)

    @Delete
    suspend fun delete(entry: TimeEntry)

    @Query("DELETE FROM time_entries")
    suspend fun clearAll()
}

@Dao
interface KpiDao {
    @Query("SELECT * FROM kpi_snapshots ORDER BY `key` ASC, date DESC")
    fun observeAll(): Flow<List<KpiSnapshot>>

    @Query("SELECT * FROM kpi_snapshots WHERE `key` = :key ORDER BY date ASC")
    fun observeForKey(key: String): Flow<List<KpiSnapshot>>

    @Query("SELECT * FROM kpi_snapshots ORDER BY date DESC")
    suspend fun all(): List<KpiSnapshot>

    @Query("SELECT * FROM kpi_snapshots WHERE `key` = :key ORDER BY date DESC LIMIT 1")
    suspend fun latestForKey(key: String): KpiSnapshot?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: KpiSnapshot)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(snapshots: List<KpiSnapshot>)

    @Query("DELETE FROM kpi_snapshots WHERE `key` = :key")
    suspend fun deleteForKey(key: String)

    @Query("DELETE FROM kpi_snapshots")
    suspend fun clearAll()
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM journal_entries WHERE date = :date LIMIT 1")
    suspend fun journalForDate(date: LocalDate): JournalEntry?

    @Query("SELECT * FROM journal_entries ORDER BY date DESC")
    fun observeJournals(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE date >= :from ORDER BY date DESC")
    suspend fun journalsSince(from: LocalDate): List<JournalEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(entry: JournalEntry)

    @Query("SELECT * FROM daily_reviews WHERE date = :date LIMIT 1")
    suspend fun reviewForDate(date: LocalDate): DailyReview?

    @Query("SELECT * FROM daily_reviews ORDER BY date DESC LIMIT :limit")
    fun observeReviews(limit: Int = 30): Flow<List<DailyReview>>

    @Query("SELECT * FROM daily_reviews WHERE date >= :from ORDER BY date DESC")
    suspend fun reviewsSince(from: LocalDate): List<DailyReview>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: DailyReview)

    @Query("DELETE FROM journal_entries")
    suspend fun clearJournals()

    @Query("DELETE FROM daily_reviews")
    suspend fun clearReviews()
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE active = 1 ORDER BY createdAt ASC")
    fun observeHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits ORDER BY createdAt ASC")
    suspend fun all(): List<Habit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: Habit)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(habits: List<Habit>)

    @Delete
    suspend fun delete(habit: Habit)

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun observeLogsForDate(date: LocalDate): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE date BETWEEN :from AND :to")
    fun observeLogsBetween(from: LocalDate, to: LocalDate): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE date BETWEEN :from AND :to")
    suspend fun logsBetween(from: LocalDate, to: LocalDate): List<HabitLog>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun log(habitId: String, date: LocalDate): HabitLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: HabitLog)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun deleteLog(habitId: String, date: LocalDate)

    @Query("DELETE FROM habit_logs")
    suspend fun clearLogs()
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notification_logs ORDER BY sentAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 60): Flow<List<NotificationLog>>

    @Query("SELECT COUNT(*) FROM notification_logs WHERE sentAt >= :from")
    suspend fun countSince(from: Long): Int

    @Query("SELECT * FROM notification_logs WHERE type = :type ORDER BY sentAt DESC LIMIT :limit")
    suspend fun byType(type: String, limit: Int): List<NotificationLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: NotificationLog)

    @Query("DELETE FROM notification_logs WHERE sentAt < :before")
    suspend fun prune(before: Long)

    @Query("DELETE FROM notification_logs")
    suspend fun clearAll()
}
