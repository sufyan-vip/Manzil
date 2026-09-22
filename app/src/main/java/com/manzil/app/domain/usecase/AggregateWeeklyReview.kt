package com.manzil.app.domain.usecase

import com.manzil.app.data.local.dao.ReviewDao
import javax.inject.Inject

class AggregateWeeklyReview @Inject constructor(
    private val reviewDao: ReviewDao
) {
    data class WeeklySummary(
        val totalPlanned: Int,
        val totalDone: Int,
        val avgScore: Int,
        val totalFocusMinutes: Int,
        val wins: List<String>,
        val blockers: List<String>
    )

    suspend fun aggregate(): WeeklySummary {
        // Simplified - real would aggregate last 7 DailyReviews
        return WeeklySummary(35, 28, 80, 1250, listOf("Landed client via Instagram"), listOf("Exam pressure"))
    }
}
