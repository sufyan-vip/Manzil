package com.manzil.app.domain.usecase

import com.manzil.app.core.common.ParsedQuickCapture
import com.manzil.app.core.common.QuickCaptureParser
import com.manzil.app.data.repository.InsightsRepository
import com.manzil.app.data.repository.ManzilRepository
import com.manzil.app.data.repository.SearchRepository
import com.manzil.app.domain.model.TodayBriefing
import com.manzil.app.domain.model.WeeklySummary
import javax.inject.Inject

/** Everything TODAY needs, assembled from local data only. */
class GetTodayBriefing @Inject constructor(
    private val repository: ManzilRepository
) {
    suspend fun build(): TodayBriefing = repository.briefingData()
}

/** Runs the nightly style maintenance on demand: rollover + recurring expansion. */
class RunDailyMaintenance @Inject constructor(
    private val repository: ManzilRepository
) {
    suspend fun run(): ManzilRepository.MaintenanceReport = repository.runDailyMaintenance()
}

/** Quick capture: parse, then save. Unit tested through QuickCaptureParser. */
class CaptureQuickTask @Inject constructor(
    private val repository: ManzilRepository
) {
    fun parse(input: String): ParsedQuickCapture = QuickCaptureParser.parse(input)

    suspend fun save(input: String, goalId: String?): String =
        repository.createFromQuickCapture(QuickCaptureParser.parse(input), goalId)
}

class SearchEverything @Inject constructor(
    private val searchRepository: SearchRepository
) {
    suspend fun search(query: String, typeFilter: String? = null): SearchRepository.Result =
        searchRepository.search(query, typeFilter)
}

class AggregateWeeklyReview @Inject constructor(
    private val insights: InsightsRepository
) {
    suspend fun aggregate(): WeeklySummary = insights.weeklySummary()
}

class TrackKpi @Inject constructor(
    private val insights: InsightsRepository
) {
    suspend fun addReading(key: String, value: Double, note: String? = null) =
        insights.addKpiReading(key, value, note)
}
