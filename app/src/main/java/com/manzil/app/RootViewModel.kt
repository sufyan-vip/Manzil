package com.manzil.app

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.manzil.app.core.logging.AppLog
import com.manzil.app.core.theme.ThemeMode
import com.manzil.app.data.prefs.SettingsRepository
import com.manzil.app.data.repository.ManzilRepository
import com.manzil.app.notification.AlarmScheduler
import com.manzil.app.notification.DailyBriefingWorker
import com.manzil.app.notification.NotificationChannels
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class RootState(
    val loading: Boolean = true,
    val onboardingDone: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false
)

@HiltViewModel
class RootViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository,
    private val repository: ManzilRepository,
    private val scheduler: AlarmScheduler
) : ViewModel() {

    private val _state = MutableStateFlow(RootState())
    val state: StateFlow<RootState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settings.settings.collect { appSettings ->
                _state.value = RootState(
                    loading = false,
                    onboardingDone = appSettings.onboardingDone,
                    themeMode = appSettings.themeMode,
                    dynamicColor = appSettings.dynamicColor
                )
            }
        }
        viewModelScope.launch {
            NotificationChannels.createAll(context)
            runCatching { repository.seedIfNeeded() }
            runCatching { repository.runDailyMaintenance() }
            runCatching { repository.refreshAllProgress() }
            runCatching { reschedule() }
            runCatching { enqueueDailyWorker() }
        }
    }

    fun onOnboardingFinished() {
        viewModelScope.launch {
            settings.setOnboardingDone(true)
            runCatching { repository.seedIfNeeded() }
            runCatching { reschedule() }
        }
    }

    private suspend fun reschedule() {
        val current = settings.current()
        scheduler.scheduleBriefing(current.morningHour, current.morningMinute)
        scheduler.scheduleEvening(current.eveningHour, current.eveningMinute)
        scheduler.scheduleMaintenance()
    }

    /** WorkManager safety net for devices where exact alarms are restricted. */
    private fun enqueueDailyWorker() {
        try {
            val request = PeriodicWorkRequestBuilder<DailyBriefingWorker>(6, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                DailyBriefingWorker.UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        } catch (t: Throwable) {
            AppLog.e("RootViewModel", "could not enqueue the maintenance worker", t)
        }
    }
}
