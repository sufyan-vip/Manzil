package com.manzil.app.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.manzil.app.core.logging.AppLog
import com.manzil.app.data.prefs.SettingsRepository
import com.manzil.app.data.repository.ManzilRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalTime

/**
 * Safety net for devices where exact alarms are not allowed. WorkManager is less
 * precise, but it guarantees the briefing and the daily rollover still happen.
 */
@HiltWorker
class DailyBriefingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val composer: BriefingComposer,
    private val notifier: Notifier,
    private val repository: ManzilRepository,
    private val settings: SettingsRepository,
    private val scheduler: AlarmScheduler
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val current = settings.current()
            repository.runDailyMaintenance()
            if (current.notificationsEnabled) {
                val hour = LocalTime.now().hour
                if (hour in 6..11 && !notifier.postedToday(Notifier.TYPE_BRIEFING)) {
                    notifier.postBriefing(composer.morning())
                }
                if (hour in 20..23 && !notifier.postedToday(Notifier.TYPE_REVIEW)) {
                    notifier.postEveningReview(composer.evening())
                }
            }
            scheduler.scheduleBriefing(current.morningHour, current.morningMinute)
            scheduler.scheduleEvening(current.eveningHour, current.eveningMinute)
            scheduler.scheduleMaintenance()
            Result.success()
        } catch (t: Throwable) {
            AppLog.e(TAG, "worker failed", t)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "DailyBriefingWorker"
        const val UNIQUE_NAME = "manzil_daily_maintenance"
    }
}
