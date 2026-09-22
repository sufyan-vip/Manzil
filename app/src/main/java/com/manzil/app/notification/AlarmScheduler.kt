package com.manzil.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.manzil.app.core.logging.AppLog
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Time critical alarms (briefing, evening review) with an automatic fall back to
 * inexact alarms when the user has not granted SCHEDULE_EXACT_ALARM.
 */
@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val alarmManager: AlarmManager? =
        context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    fun canScheduleExact(): Boolean {
        val manager = alarmManager ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            manager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun scheduleBriefing(hour: Int, minute: Int) = schedule(
        hour = hour,
        minute = minute,
        requestCode = REQUEST_BRIEFING,
        action = ReminderReceiver.ACTION_BRIEFING
    )

    fun scheduleEvening(hour: Int, minute: Int) = schedule(
        hour = hour,
        minute = minute,
        requestCode = REQUEST_EVENING,
        action = ReminderReceiver.ACTION_EVENING
    )

    /** Nightly housekeeping: rollover unfinished work, expand recurrence, prune logs. */
    fun scheduleMaintenance() = schedule(
        hour = 0,
        minute = 20,
        requestCode = REQUEST_MAINTENANCE,
        action = ReminderReceiver.ACTION_MAINTENANCE
    )

    fun cancelAll() {
        listOf(
            REQUEST_BRIEFING to ReminderReceiver.ACTION_BRIEFING,
            REQUEST_EVENING to ReminderReceiver.ACTION_EVENING,
            REQUEST_MAINTENANCE to ReminderReceiver.ACTION_MAINTENANCE
        ).forEach { (code, action) ->
            alarmManager?.cancel(pendingIntent(code, action))
        }
    }

    private fun schedule(hour: Int, minute: Int, requestCode: Int, action: String) {
        val manager = alarmManager ?: return
        val triggerAt = nextTrigger(hour, minute)
        val pending = pendingIntent(requestCode, action)
        runCatching {
            if (canScheduleExact()) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            } else {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            }
        }.onFailure { AppLog.e(TAG, "could not schedule $action", it) }
    }

    private fun pendingIntent(requestCode: Int, action: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply { this.action = action }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextTrigger(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }

    companion object {
        private const val TAG = "AlarmScheduler"
        const val REQUEST_BRIEFING = 4101
        const val REQUEST_EVENING = 4102
        const val REQUEST_MAINTENANCE = 4103
    }
}
