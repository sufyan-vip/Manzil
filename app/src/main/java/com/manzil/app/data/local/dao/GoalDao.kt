package com.manzil.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.manzil.app.data.local.entity.Goal
import com.manzil.app.data.local.entity.GoalRevision
import com.manzil.app.data.local.entity.Milestone
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Query("SELECT * FROM goals ORDER BY sortOrder ASC, createdAt ASC")
    fun observeGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE status = 'ACTIVE' ORDER BY sortOrder ASC, createdAt ASC")
    fun observeActiveGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE parentGoalId IS NULL ORDER BY sortOrder ASC, createdAt ASC")
    suspend fun rootGoals(): List<Goal>

    @Query("SELECT * FROM goals ORDER BY sortOrder ASC, createdAt ASC")
    suspend fun allGoals(): List<Goal>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun goalById(id: String): Goal?

    @Query("SELECT * FROM goals WHERE parentGoalId = :parentId ORDER BY sortOrder ASC")
    fun observeChildren(parentId: String): Flow<List<Goal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: Goal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(goals: List<Goal>)

    @Update
    suspend fun update(goal: Goal)

    @Delete
    suspend fun delete(goal: Goal)

    @Query("DELETE FROM goals")
    suspend fun clearAll()

    /* ------------------------------------ revisions (pulse) ---------------------------------- */

    @Query("SELECT * FROM goal_revisions ORDER BY createdAt DESC LIMIT :limit")
    fun observeRevisions(limit: Int = 200): Flow<List<GoalRevision>>

    @Query("SELECT * FROM goal_revisions WHERE goalId = :goalId ORDER BY createdAt DESC")
    fun observeRevisionsForGoal(goalId: String): Flow<List<GoalRevision>>

    @Query("SELECT * FROM goal_revisions WHERE createdAt >= :since ORDER BY createdAt DESC")
    suspend fun revisionsSince(since: Long): List<GoalRevision>

    @Query("SELECT * FROM goal_revisions ORDER BY createdAt DESC")
    suspend fun allRevisions(): List<GoalRevision>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevision(revision: GoalRevision)

    @Query("DELETE FROM goal_revisions WHERE goalId = :goalId")
    suspend fun deleteRevisionsForGoal(goalId: String)

    @Query("DELETE FROM goal_revisions")
    suspend fun clearRevisions()

    /* ----------------------------------------- milestones ------------------------------------ */

    @Query("SELECT * FROM milestones ORDER BY sortOrder ASC, createdAt ASC")
    fun observeMilestones(): Flow<List<Milestone>>

    @Query("SELECT * FROM milestones WHERE goalId = :goalId ORDER BY sortOrder ASC, createdAt ASC")
    fun observeMilestonesForGoal(goalId: String): Flow<List<Milestone>>

    @Query("SELECT * FROM milestones")
    suspend fun allMilestones(): List<Milestone>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: Milestone)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<Milestone>)

    @Query("SELECT * FROM milestones WHERE id = :id")
    suspend fun milestoneById(id: String): Milestone?

    @Update
    suspend fun updateMilestone(milestone: Milestone)

    @Delete
    suspend fun deleteMilestone(milestone: Milestone)

    @Query("DELETE FROM milestones WHERE goalId = :goalId")
    suspend fun deleteMilestonesForGoal(goalId: String)

    @Query("DELETE FROM milestones")
    suspend fun clearMilestones()
}
