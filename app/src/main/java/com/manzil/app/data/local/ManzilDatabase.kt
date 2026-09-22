package com.manzil.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.manzil.app.data.local.dao.CalendarDao
import com.manzil.app.data.local.dao.GoalDao
import com.manzil.app.data.local.dao.HabitDao
import com.manzil.app.data.local.dao.KpiDao
import com.manzil.app.data.local.dao.NotificationDao
import com.manzil.app.data.local.dao.ReviewDao
import com.manzil.app.data.local.dao.TaskDao
import com.manzil.app.data.local.dao.TimeDao
import com.manzil.app.data.local.entity.CalendarEvent
import com.manzil.app.data.local.entity.DailyReview
import com.manzil.app.data.local.entity.Goal
import com.manzil.app.data.local.entity.GoalRevision
import com.manzil.app.data.local.entity.Habit
import com.manzil.app.data.local.entity.HabitLog
import com.manzil.app.data.local.entity.JournalEntry
import com.manzil.app.data.local.entity.KpiSnapshot
import com.manzil.app.data.local.entity.Milestone
import com.manzil.app.data.local.entity.NotificationLog
import com.manzil.app.data.local.entity.Setting
import com.manzil.app.data.local.entity.Task
import com.manzil.app.data.local.entity.TaskInstance
import com.manzil.app.data.local.entity.TimeEntry

@Database(
    entities = [
        Goal::class, GoalRevision::class, Milestone::class,
        Task::class, TaskInstance::class, TimeEntry::class,
        CalendarEvent::class, JournalEntry::class, DailyReview::class,
        Habit::class, HabitLog::class, KpiSnapshot::class,
        NotificationLog::class, Setting::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ManzilDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun taskDao(): TaskDao
    abstract fun calendarDao(): CalendarDao
    abstract fun reviewDao(): ReviewDao
    abstract fun timeDao(): TimeDao
    abstract fun habitDao(): HabitDao
    abstract fun kpiDao(): KpiDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        const val NAME = "manzil.db"
    }
}
