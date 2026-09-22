package com.manzil.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "kpi_snapshots", indices = [Index("key"), Index("date")])
data class KpiSnapshot(
    @PrimaryKey val id: String, val key: String,
    val value: Double, val date: LocalDate, val note: String? = null, val createdAt: Long
)
