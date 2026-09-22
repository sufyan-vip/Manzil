package com.manzil.app.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.manzil.app.MainActivity
import com.manzil.app.R
import com.manzil.app.core.logging.AppLog
import com.manzil.app.data.local.dao.NotificationDao
import com.manzil.app.data.local.entity.NotificationLog
import com.manzil.app.data.prefs.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * All notifications go through here so the anti-nag rules can never be bypassed:
 * at most 6 a day, never between 23:00 and 07:00, and nothing is posted while
 * notifications are switched off in Settings.
 */
@Singleton
class Notifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationDao: NotificationDao,
    private val settings: SettingsRepository
) {

    suspend fun postTest() = post(
        channel = NotificationChannels.BRIEFING,
        title = context.getString(R.string.notif_test_title),
        body = context.getString(R.string.notif_test_body),
        type = "test",
        respectQuietHours = false
    )

    suspend fun postBriefing(body: String) = post(
        channel = NotificationChannels.BRIEFING,
        title = context.getString(R.string.notif_briefing_title),
        body = body,
        type = TYPE_BRIEFING,
        respectQuietHours = false
    )

    suspend fun postEveningReview(body: String) = post(
        channel = NotificationChannels.REVIEW,
        title = context.getString(R.string.notif_review_title),
        body = body,
        type = TYPE_REVIEW
    )

    suspend fun postReminder(taskTitle: String) = post(
        channel = NotificationChannels.REMINDERS,
        title = "Coming up: $taskTitle",
        body = "Open Manzil and start the focus timer — one task at a time.",
        type = TYPE_REMINDER
    )

    suspend fun postStreakRisk(streakDays: Int) = post(
        channel = NotificationChannels.STREAK,
        title = "Streak at risk — $streakDays days",
        body = "Nothing completed today yet. One small task keeps the streak alive.",
        type = TYPE_STREAK
    )

    suspend fun postAiInsight(title: String, body: String) = post(
        channel = NotificationChannels.AI,
        title = title,
        body = body,
        type = TYPE_AI
    )

    /** Has this type already been posted today? Used to avoid duplicate briefings. */
    suspend fun postedToday(type: String): Boolean = withContext(Dispatchers.IO) {
        val todayStart = java.time.LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        notificationDao.byType(type, 1).firstOrNull()?.sentAt?.let { it >= todayStart } ?: false
    }

    private suspend fun post(
        channel: String,
        title: String,
        body: String,
        type: String,
        respectQuietHours: Boolean = true
    ) {
        withContext(Dispatchers.IO) {
            val appSettings = settings.current()
            if (!appSettings.notificationsEnabled && type != "test") return@withContext
            if (respectQuietHours && isQuietHour()) return@withContext
            if (!respectQuietHours && isQuietHour() && type != TYPE_BRIEFING) return@withContext
            if (!hasPermission()) {
                AppLog.d(TAG, "notification permission not granted — skipping $type")
                return@withContext
            }
            val dailyCount = notificationDao.countSince(startOfToday())
            if (dailyCount >= MAX_PER_DAY) {
                AppLog.d(TAG, "daily notification cap reached ($MAX_PER_DAY) — skipping $type")
                return@withContext
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pending = PendingIntent.getActivity(
                context,
                type.hashCode() and 0xFFFF,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(context, channel)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(pending)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            runCatching {
                NotificationManagerCompat.from(context).notify(type.hashCode() and 0xFFFF, notification)
            }.onFailure { AppLog.e(TAG, "notify failed", it) }
            notificationDao.insert(
                NotificationLog(
                    id = UUID.randomUUID().toString(),
                    type = type,
                    title = title,
                    body = body,
                    sentAt = System.currentTimeMillis()
                )
            )
        }
    }

    private fun isQuietHour(): Boolean {
        val hour = LocalTime.now().hour
        return hour >= 23 || hour < 7
    }

    private fun hasPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startOfToday(): Long =
        java.time.LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant().toEpochMilli()

    companion object {
        const val MAX_PER_DAY = 6
        const val TYPE_BRIEFING = "briefing"
        const val TYPE_REVIEW = "review"
        const val TYPE_REMINDER = "reminder"
        const val TYPE_STREAK = "streak"
        const val TYPE_AI = "ai"
        private const val TAG = "Notifier"
    }
}
