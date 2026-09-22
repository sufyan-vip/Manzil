package com.manzil.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val BRIEFING = "briefing"
    const val REVIEW = "review"
    const val REMINDERS = "reminders"
    const val STREAK = "streak"
    const val AI = "ai"

    fun createAll(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channels = listOf(
                NotificationChannel(BRIEFING, "Daily briefing", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(REVIEW, "Evening review", NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(REMINDERS, "Task reminders", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(STREAK, "Streak alerts", NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(AI, "AI insights", NotificationManager.IMPORTANCE_LOW)
            )
            channels.forEach { manager.createNotificationChannel(it) }
        }
    }
}
