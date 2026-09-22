package com.manzil.app.feature.time

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manzil.app.core.common.Fmt
import com.manzil.app.data.local.entity.TimeEntry
import com.manzil.app.data.local.entity.TimeSource
import com.manzil.app.data.prefs.FocusTimerStore
import com.manzil.app.data.prefs.SettingsRepository
import com.manzil.app.data.repository.InsightsRepository
import com.manzil.app.data.repository.ManzilRepository
import com.manzil.app.domain.model.TimeStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

data class TimeState(
    val loading: Boolean = true,
    val stats: TimeStats = TimeStats(0, 0, 0, emptyList(), emptyList()),
    val entries: List<TimeEntry> = emptyList(),
    val goalTitles: Map<String, String> = emptyMap(),
    val weeklyTargetHours: Int = 25,
    val manualLabel: String = "",
    val manualMinutes: String = "30",
    val banner: String? = null
)

@HiltViewModel
class TimeViewModel @Inject constructor(
    private val repository: ManzilRepository,
    private val insights: InsightsRepository,
    private val settings: SettingsRepository,
    private val timerStore: FocusTimerStore
) : ViewModel() {

    private val _state = MutableStateFlow(TimeState())
    val state: StateFlow<TimeState> = _state.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            repository.observeGoals().collect {
                _state.value = _state.value.copy(goalTitles = it.associate { goal -> goal.id to goal.title })
            }
        }
    }

    fun refresh() = viewModelScope.launch {
        val today = Fmt.today()
        val entries = repository.timeEntriesBetween(
            Fmt.startOfDayMillis(today.minusDays(13)),
            Fmt.endOfDayMillis(today)
        )
        _state.value = _state.value.copy(
            loading = false,
            stats = insights.timeStats(),
            entries = entries,
            weeklyTargetHours = settings.current().deepWorkTargetHours
        )
    }

    fun updateManualLabel(value: String) {
        _state.value = _state.value.copy(manualLabel = value)
    }

    fun updateManualMinutes(value: String) {
        _state.value = _state.value.copy(manualMinutes = value)
    }

    fun addManualEntry() = viewModelScope.launch {
        val label = _state.value.manualLabel.trim()
        val minutes = _state.value.manualMinutes.toIntOrNull()
        if (label.isBlank() || minutes == null || minutes <= 0) {
            _state.value = _state.value.copy(banner = "Add a label and minutes greater than zero.")
            return@launch
        }
        val endedAt = System.currentTimeMillis()
        repository.saveTimeEntry(
            TimeEntry(
                id = "time_" + UUID.randomUUID().toString().take(8),
                taskId = null,
                goalId = null,
                label = label,
                startedAt = endedAt - minutes * 60_000L,
                endedAt = endedAt,
                source = TimeSource.MANUAL
            ),
            addMinutesToTask = false
        )
        _state.value = _state.value.copy(
            manualLabel = "",
            banner = "Logged ${Fmt.duration(minutes)} of $label."
        )
        refresh()
    }

    fun deleteEntry(entry: TimeEntry) = viewModelScope.launch {
        repository.deleteTimeEntry(entry)
        _state.value = _state.value.copy(banner = "Entry removed.")
        refresh()
    }

    fun startTimerFromEntry(entry: TimeEntry) = viewModelScope.launch {
        timerStore.start(entry.taskId, entry.goalId, entry.label)
        _state.value = _state.value.copy(banner = "Timer started for ${entry.label}.")
    }

    fun dismissBanner() {
        _state.value = _state.value.copy(banner = null)
    }

    suspend fun entriesToday(): List<TimeEntry> =
        repository.timeEntriesBetween(Fmt.startOfDayMillis(Fmt.today()), Fmt.endOfDayMillis(Fmt.today()))

    private suspend fun weeklyTarget(): Int = settings.settings.first().deepWorkTargetHours.also {
        _state.value = _state.value.copy(weeklyTargetHours = it)
    }

    @Suppress("unused")
    private fun nowTime(): LocalTime = LocalTime.now()
}
