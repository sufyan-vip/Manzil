package com.manzil.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.manzil.app.core.logging.AppLog
import com.manzil.app.data.prefs.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Alarms do not survive a reboot, so they are re-registered here. */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var scheduler: AlarmScheduler
    @Inject lateinit var settings: SettingsRepository

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_LOCALE_CHANGED -> {
                NotificationChannels.createAll(context)
                val pending = goAsync()
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        val current = settings.current()
                        scheduler.scheduleBriefing(current.morningHour, current.morningMinute)
                        scheduler.scheduleEvening(current.eveningHour, current.eveningMinute)
                        scheduler.scheduleMaintenance()
                    } catch (t: Throwable) {
                        AppLog.e(TAG, "rescheduling failed", t)
                    } finally {
                        pending.finish()
                    }
                }
            }
        }
    }

    private companion object {
        const val TAG = "BootReceiver"
    }
}
