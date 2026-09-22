package com.manzil.app.data.local.dao

import androidx.room.*
import com.manzil.app.data.local.entity.Goal
import com.manzil.app.data.local.entity.GoalRevision
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY sortOrder ASC")
    fun getAllGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: String): Goal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    @Update
    suspend fun updateGoal(goal: Goal)

    @Delete
    suspend fun deleteGoal(goal: Goal)

    @Query("SELECT * FROM goal_revisions WHERE goalId = :goalId ORDER BY createdAt DESC")
    fun getRevisionsForGoal(goalId: String): Flow<List<GoalRevision>>

    @Query("SELECT * FROM goal_revisions ORDER BY createdAt DESC LIMIT 100")
    fun getAllRevisions(): Flow<List<GoalRevision>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevision(revision: GoalRevision)
}
