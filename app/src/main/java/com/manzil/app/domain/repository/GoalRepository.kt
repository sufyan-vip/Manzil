package com.manzil.app.domain.repository
import com.manzil.app.data.local.entity.Goal
import kotlinx.coroutines.flow.Flow
interface GoalRepository {
    fun getAllGoals(): Flow<List<Goal>>
    suspend fun getGoalById(id: String): Goal?
}
