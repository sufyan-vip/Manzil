package com.manzil.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.manzil.app.data.local.entity.Task
import com.manzil.app.data.local.entity.TaskInstance
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY dueDate IS NULL, dueDate ASC, sortOrder ASC, priority ASC")
    fun observeAll(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE dueDate = :date ORDER BY sortOrder ASC, priority ASC, dueTime IS NULL, dueTime ASC")
    fun observeForDate(date: LocalDate): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE recurrenceRule IS NOT NULL AND status != 'CANCELLED'")
    fun observeRecurring(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE dueDate BETWEEN :from AND :to ORDER BY dueDate ASC, dueTime IS NULL, dueTime ASC")
    fun observeBetween(from: LocalDate, to: LocalDate): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE dueDate IS NOT NULL AND dueDate < :date AND status NOT IN ('DONE','CANCELLED') ORDER BY dueDate ASC, priority ASC")
    fun observeOverdue(date: LocalDate): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE dueDate = :date ORDER BY sortOrder ASC, priority ASC")
    suspend fun tasksForDate(date: LocalDate): List<Task>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun taskById(id: String): Task?

    @Query("SELECT * FROM tasks WHERE status = 'DONE' AND completedAt IS NOT NULL ORDER BY completedAt DESC")
    suspend fun completedTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE completedAt IS NOT NULL AND completedAt BETWEEN :from AND :to")
    suspend fun completedBetween(from: Long, to: Long): List<Task>

    @Query("SELECT * FROM tasks WHERE status != 'CANCELLED'")
    suspend fun allActive(): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<Task>)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM tasks")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'DONE' AND completedAt >= :from")
    suspend fun doneCountSince(from: Long): Int

    /* ---------------------------------- recurring instances ---------------------------------- */

    @Query("SELECT * FROM task_instances WHERE occurrenceDate = :date")
    fun observeInstancesForDate(date: LocalDate): Flow<List<TaskInstance>>

    @Query("SELECT * FROM task_instances WHERE occurrenceDate BETWEEN :from AND :to")
    fun observeInstancesBetween(from: LocalDate, to: LocalDate): Flow<List<TaskInstance>>

    @Query("SELECT * FROM task_instances WHERE occurrenceDate = :date")
    suspend fun instancesForDate(date: LocalDate): List<TaskInstance>

    @Query("SELECT * FROM task_instances WHERE taskId = :taskId ORDER BY occurrenceDate ASC")
    suspend fun instancesForTask(taskId: String): List<TaskInstance>

    @Query("SELECT * FROM task_instances WHERE taskId = :taskId AND occurrenceDate = :date")
    suspend fun instance(taskId: String, date: LocalDate): TaskInstance?

    @Query("SELECT * FROM task_instances WHERE occurrenceDate < :date AND status != 'DONE'")
    suspend fun unfinishedInstancesBefore(date: LocalDate): List<TaskInstance>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstance(instance: TaskInstance)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstances(instances: List<TaskInstance>)

    @Update
    suspend fun updateInstance(instance: TaskInstance)

    @Query("UPDATE task_instances SET occurrenceDate = :to WHERE taskId = :taskId AND occurrenceDate = :from")
    suspend fun moveInstance(taskId: String, from: LocalDate, to: LocalDate)

    @Query("DELETE FROM task_instances WHERE taskId = :taskId")
    suspend fun deleteInstancesForTask(taskId: String)

    @Query("DELETE FROM task_instances")
    suspend fun clearInstances()
}
