package com.manzil.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.manzil.app.core.logging.AppLog
import com.manzil.app.data.local.dao.NotificationDao
import com.manzil.app.data.prefs.SettingsRepository
import com.manzil.app.data.repository.ManzilRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Fires the morning briefing, the evening review and the nightly housekeeping.
 * Work happens in a coroutine scope tied to the receiver's goAsync() window so it
 * survives the app being in the background.
 */
@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var composer: BriefingComposer
    @Inject lateinit var notifier: Notifier
    @Inject lateinit var repository: ManzilRepository
    @Inject lateinit var settings: SettingsRepository
    @Inject lateinit var scheduler: AlarmScheduler
    @Inject lateinit var notificationDao: NotificationDao

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                when (action) {
                    ACTION_BRIEFING -> sendBriefing()
                    ACTION_EVENING -> sendEvening()
                    ACTION_MAINTENANCE -> runMaintenance()
                }
            } catch (t: Throwable) {
                AppLog.e(TAG, "receiver failed for $action", t)
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun sendBriefing() {
        val current = settings.current()
        if (!current.notificationsEnabled) return
        if (notifier.postedToday(Notifier.TYPE_BRIEFING)) {
            AppLog.d(TAG, "briefing already posted today")
            return
        }
        notifier.postBriefing(composer.morning())
        scheduler.scheduleBriefing(current.morningHour, current.morningMinute)
    }

    private suspend fun sendEvening() {
        val current = settings.current()
        if (current.notificationsEnabled && !notifier.postedToday(Notifier.TYPE_REVIEW)) {
            notifier.postEveningReview(composer.evening())
        }
        repository.saveDailyReview(LocalDate.now())
        notificationDao.prune(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)
        scheduler.scheduleEvening(current.eveningHour, current.eveningMinute)
    }

    private suspend fun runMaintenance() {
        repository.runDailyMaintenance()
        val current = settings.current()
        scheduler.scheduleBriefing(current.morningHour, current.morningMinute)
        scheduler.scheduleEvening(current.eveningHour, current.eveningMinute)
        scheduler.scheduleMaintenance()
        notificationDao.prune(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)
    }

    companion object {
        private const val TAG = "ReminderReceiver"
        const val ACTION_BRIEFING = "com.manzil.app.action.BRIEFING"
        const val ACTION_EVENING = "com.manzil.app.action.EVENING"
        const val ACTION_MAINTENANCE = "com.manzil.app.action.MAINTENANCE"
    }
}
