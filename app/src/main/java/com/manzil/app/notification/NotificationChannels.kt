package com.manzil.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.manzil.app.R

object NotificationChannels {
    const val BRIEFING = "briefing"
    const val REVIEW = "review"
    const val REMINDERS = "reminders"
    const val STREAK = "streak"
    const val AI = "ai"

    fun createAll(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channels = listOf(
            NotificationChannel(
                BRIEFING,
                context.getString(R.string.notif_channel_briefing),
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = context.getString(R.string.notif_channel_briefing_desc) },
            NotificationChannel(
                REVIEW,
                context.getString(R.string.notif_channel_review),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = context.getString(R.string.notif_channel_review_desc) },
            NotificationChannel(
                REMINDERS,
                context.getString(R.string.notif_channel_reminders),
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = context.getString(R.string.notif_channel_reminders_desc) },
            NotificationChannel(
                STREAK,
                context.getString(R.string.notif_channel_streak),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = context.getString(R.string.notif_channel_streak_desc) },
            NotificationChannel(
                AI,
                context.getString(R.string.notif_channel_ai),
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = context.getString(R.string.notif_channel_ai_desc) }
        )
        channels.forEach { manager.createNotificationChannel(it) }
    }
}
