package com.manzil.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "journal_entries", indices = [Index("date")])
data class JournalEntry(
    @PrimaryKey val id: String, val date: LocalDate,
    val mood: Int = 3,
    val wins: String = "", val blockers: String = "", val note: String = "",
    val createdAt: Long, val updatedAt: Long
)
