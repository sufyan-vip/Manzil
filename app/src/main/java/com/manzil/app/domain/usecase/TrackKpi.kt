package com.manzil.app.domain.usecase

import com.manzil.app.data.local.dao.KpiDao
import com.manzil.app.data.local.entity.KpiSnapshot
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class TrackKpi @Inject constructor(
    private val kpiDao: KpiDao
) {
    suspend fun addReading(key: String, value: Double, note: String? = null) {
        val snapshot = KpiSnapshot(
            id = UUID.randomUUID().toString(),
            key = key,
            value = value,
            date = LocalDate.now(),
            note = note,
            createdAt = System.currentTimeMillis()
        )
        kpiDao.insertSnapshot(snapshot)
    }
}
