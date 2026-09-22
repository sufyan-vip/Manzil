package com.manzil.app.domain.model
import java.time.LocalDate
data class UserProfile(
    val name: String,
    val university: String,
    val semester: String,
    val skills: List<String>,
    val mainGoal: String,
    val targetDate: String,
    val dailyHours: Int,
    val preferredPlatforms: List<String>,
    val isPerpetual: Boolean = true
)
data class TodayBriefing(
    val dayCounter: Int,
    val todayTasks: List<String>,
    val yesterdayDone: Int,
    val yesterdayPlanned: Int,
    val pending: List<String>,
    val goalMove: String?,
    val streak: Int,
    val rootProgress: Int
)
