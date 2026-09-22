package com.manzil.app.data.local.dao

import androidx.room.*
import com.manzil.app.data.local.entity.NotificationLog
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notification_logs ORDER BY sentAt DESC LIMIT 100")
    fun getRecentLogs(): Flow<List<NotificationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: NotificationLog)

    @Query("DELETE FROM notification_logs WHERE sentAt < :before")
    suspend fun clearOldLogs(before: Long)
}
