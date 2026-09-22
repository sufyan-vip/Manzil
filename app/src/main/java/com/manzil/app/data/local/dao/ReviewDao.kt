package com.manzil.app.data.local.dao

import androidx.room.*
import com.manzil.app.data.local.entity.DailyReview
import com.manzil.app.data.local.entity.JournalEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ReviewDao {
    @Query("SELECT * FROM journal_entries WHERE date = :date LIMIT 1")
    suspend fun getJournalForDate(date: LocalDate): JournalEntry?

    @Query("SELECT * FROM journal_entries ORDER BY date DESC")
    fun getAllJournals(): Flow<List<JournalEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(entry: JournalEntry)

    @Query("SELECT * FROM daily_reviews WHERE date = :date LIMIT 1")
    suspend fun getReviewForDate(date: LocalDate): DailyReview?

    @Query("SELECT * FROM daily_reviews ORDER BY date DESC LIMIT 30")
    fun getRecentReviews(): Flow<List<DailyReview>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: DailyReview)
}
