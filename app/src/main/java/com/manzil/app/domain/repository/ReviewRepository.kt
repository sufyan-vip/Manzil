package com.manzil.app.domain.repository

import com.manzil.app.data.local.entity.DailyReview
import com.manzil.app.data.local.entity.JournalEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ReviewRepository {
    suspend fun getJournalForDate(date: LocalDate): JournalEntry?
    fun getAllJournals(): Flow<List<JournalEntry>>
    suspend fun insertJournal(entry: JournalEntry)
    fun getRecentReviews(): Flow<List<DailyReview>>
}
