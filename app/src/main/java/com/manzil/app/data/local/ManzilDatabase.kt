package com.manzil.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.manzil.app.data.local.dao.*
import com.manzil.app.data.local.entity.*
import com.manzil.app.data.local.fts.SearchDocFts

@Database(
    entities = [
        Goal::class, GoalRevision::class, Milestone::class,
        Task::class, TaskInstance::class, TimeEntry::class,
        CalendarEvent::class, JournalEntry::class, DailyReview::class,
        Habit::class, HabitLog::class, KpiSnapshot::class,
        SearchDoc::class, SearchDocFts::class, NotificationLog::class, Setting::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ManzilDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun taskDao(): TaskDao
    abstract fun searchDao(): SearchDao
    abstract fun calendarDao(): CalendarDao
    abstract fun reviewDao(): ReviewDao
    abstract fun timeDao(): TimeDao
    abstract fun habitDao(): HabitDao
    abstract fun kpiDao(): KpiDao
    abstract fun notificationDao(): NotificationDao
}
