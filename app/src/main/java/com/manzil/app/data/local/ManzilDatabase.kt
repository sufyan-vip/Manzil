package com.manzil.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
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

    companion object {
        fun getCallback() = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Triggers to keep search_docs in sync automatically — real-time search without manual re-index
                // Goals -> search_docs
                db.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS goals_ai AFTER INSERT ON goals BEGIN
                        INSERT INTO search_docs(id, entityType, entityId, title, body, goalTitle, dateIso, updatedAt)
                        VALUES (new.id, 'GOAL', new.id, new.title, new.description, new.title, new.targetDate, new.updatedAt);
                    END
                """.trimIndent())
                db.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS goals_au AFTER UPDATE ON goals BEGIN
                        UPDATE search_docs SET title=new.title, body=new.description, goalTitle=new.title, updatedAt=new.updatedAt WHERE entityId=new.id AND entityType='GOAL';
                    END
                """.trimIndent())
                db.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS goals_ad AFTER DELETE ON goals BEGIN
                        DELETE FROM search_docs WHERE entityId=old.id AND entityType='GOAL';
                    END
                """.trimIndent())
                // Tasks -> search_docs
                db.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS tasks_ai AFTER INSERT ON tasks BEGIN
                        INSERT INTO search_docs(id, entityType, entityId, title, body, goalTitle, dateIso, updatedAt)
                        VALUES (new.id, 'TASK', new.id, new.title, new.notes, NULL, new.dueDate, new.updatedAt);
                    END
                """.trimIndent())
                db.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS tasks_au AFTER UPDATE ON tasks BEGIN
                        UPDATE search_docs SET title=new.title, body=new.notes, dateIso=new.dueDate, updatedAt=new.updatedAt WHERE entityId=new.id AND entityType='TASK';
                    END
                """.trimIndent())
                db.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS tasks_ad AFTER DELETE ON tasks BEGIN
                        DELETE FROM search_docs WHERE entityId=old.id AND entityType='TASK';
                    END
                """.trimIndent())
                // Keep FTS in sync from search_docs
                db.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS search_docs_ai AFTER INSERT ON search_docs BEGIN
                        INSERT INTO search_docs_fts(docId, title, body, goalTitle) VALUES (new.id, new.title, new.body, new.goalTitle);
                    END
                """.trimIndent())
                db.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS search_docs_ad AFTER DELETE ON search_docs BEGIN
                        DELETE FROM search_docs_fts WHERE docId = old.id;
                    END
                """.trimIndent())
                db.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS search_docs_au AFTER UPDATE ON search_docs BEGIN
                        DELETE FROM search_docs_fts WHERE docId = old.id;
                        INSERT INTO search_docs_fts(docId, title, body, goalTitle) VALUES (new.id, new.title, new.body, new.goalTitle);
                    END
                """.trimIndent())
            }
        }
    }
}
