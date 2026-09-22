package com.manzil.app.data.local.dao

import androidx.room.*
import com.manzil.app.data.local.entity.KpiSnapshot
import kotlinx.coroutines.flow.Flow

@Dao
interface KpiDao {
    @Query("SELECT * FROM kpi_snapshots WHERE `key` = :key ORDER BY date DESC")
    fun getSnapshotsForKey(key: String): Flow<List<KpiSnapshot>>

    @Query("SELECT * FROM kpi_snapshots ORDER BY date DESC")
    fun getAllSnapshots(): Flow<List<KpiSnapshot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: KpiSnapshot)

    @Query("SELECT * FROM kpi_snapshots WHERE `key` = :key ORDER BY date DESC LIMIT 1")
    suspend fun getLatestForKey(key: String): KpiSnapshot?
}
