package com.manzil.app.notification

import com.manzil.app.core.common.Fmt
import com.manzil.app.data.prefs.SettingsRepository
import com.manzil.app.data.repository.InsightsRepository
import com.manzil.app.data.repository.ManzilRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds the text of the morning briefing and the evening review from local data.
 * If there is no data yet it still returns something meaningful — a notification
 * must never be empty because the network (or the user) is quiet.
 */
@Singleton
class BriefingComposer @Inject constructor(
    private val repository: ManzilRepository,
    private val insights: InsightsRepository,
    private val settings: SettingsRepository,
    private val builder: BriefingBuilder
) {

    suspend fun morning(): String {
        val data = repository.briefingData()
        val language = if (settings.current().language == "ur") "ur" else "en"
        val text = builder.buildMorningBriefing(
            dayCounter = data.dayCounter,
            todayTasks = data.todayTasks.ifEmpty { listOf("Plan 3 tasks for today in Manzil") },
            yesterdayDone = data.yesterdayDone,
            yesterdayPlanned = data.yesterdayPlanned,
            pending = data.pending,
            goalMove = data.goalMove,
            streak = data.streak,
            rootProgress = data.rootProgress,
            language = language
        )
        return text
    }

    suspend fun evening(): String {
        val timeStats = insights.timeStats()
        val data = repository.briefingData()
        val review = repository.dayTasks(Fmt.today())
        val done = review.count { it.done }
        val planned = review.size
        val pendingTitles = review.filter { !it.done }.map { it.task.title }
        val nextTask = pendingTitles.firstOrNull() ?: data.todayTasks.firstOrNull()
            ?: "Choose tomorrow's first task in Manzil"
        return builder.buildEveningReview(
            done = done,
            planned = planned,
            pending = pendingTitles,
            focusMinutes = timeStats.todayMinutes,
            avgMinutes = timeStats.sevenDayAverageMinutes,
            nextTask = nextTask
        )
    }
}
