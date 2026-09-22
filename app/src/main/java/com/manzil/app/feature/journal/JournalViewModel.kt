package com.manzil.app.feature.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manzil.app.core.common.Fmt
import com.manzil.app.data.local.entity.JournalEntry
import com.manzil.app.data.repository.ManzilRepository
import com.manzil.app.domain.model.WeeklySummary
import com.manzil.app.domain.usecase.AggregateWeeklyReview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class JournalState(
    val loading: Boolean = true,
    val tabIndex: Int = 0,
    val date: LocalDate = LocalDate.now(),
    val mood: Int = 3,
    val wins: String = "",
    val blockers: String = "",
    val note: String = "",
    val prompt: String = "",
    val history: List<JournalEntry> = emptyList(),
    val weekly: WeeklySummary = WeeklySummary(0, 0, 0, 0, 0, 0, emptyList(), emptyList(), emptyList(), emptyList()),
    val banner: String? = null
)

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val repository: ManzilRepository,
    private val weeklyReview: AggregateWeeklyReview
) : ViewModel() {

    private val _state = MutableStateFlow(JournalState())
    val state: StateFlow<JournalState> = _state.asStateFlow()

    init {
        loadToday()
        viewModelScope.launch {
            repository.observeJournals().collect { entries ->
                _state.value = _state.value.copy(history = entries, loading = false)
            }
        }
    }

    fun setTab(index: Int) {
        _state.value = _state.value.copy(tabIndex = index)
        if (index == 2) refreshWeekly()
    }

    fun selectDate(date: LocalDate) {
        _state.value = _state.value.copy(date = date)
        loadToday()
    }

    fun loadToday() = viewModelScope.launch {
        val date = _state.value.date
        val existing = repository.journalFor(date)
        val tasks = repository.dayTasks(date)
        val done = tasks.count { it.done }
        val pending = tasks.filter { !it.done }
        _state.value = _state.value.copy(
            loading = false,
            mood = existing?.mood ?: 3,
            wins = existing?.wins ?: "",
            blockers = existing?.blockers ?: "",
            note = existing?.note ?: "",
            prompt = buildString {
                append("$done/${tasks.size} done on ${Fmt.dateLong(date)}.")
                pending.firstOrNull()?.let { append(" Pending: ${it.task.title}.") }
                if (tasks.isEmpty()) append(" Nothing was planned — that is data too.")
            }
        )
    }

    fun setMood(mood: Int) {
        _state.value = _state.value.copy(mood = mood)
    }

    fun updateWins(value: String) {
        _state.value = _state.value.copy(wins = value)
    }

    fun updateBlockers(value: String) {
        _state.value = _state.value.copy(blockers = value)
    }

    fun updateNote(value: String) {
        _state.value = _state.value.copy(note = value)
    }

    fun save() = viewModelScope.launch {
        val current = _state.value
        repository.saveJournal(
            date = current.date,
            mood = current.mood,
            wins = current.wins,
            blockers = current.blockers,
            note = current.note
        )
        repository.saveDailyReview(current.date)
        _state.value = _state.value.copy(banner = "Journal saved for ${Fmt.dateShort(current.date)}.")
    }

    fun refreshWeekly() = viewModelScope.launch {
        _state.value = _state.value.copy(weekly = weeklyReview.aggregate())
    }

    /** Weekly review priorities become real tasks — that is the whole point. */
    fun createPriorityTask(title: String) = viewModelScope.launch {
        repository.createTask(
            title = title,
            dueDate = LocalDate.now(),
            priority = 1,
            goalId = repository.rootGoal()?.id,
            estimatedMinutes = 60
        )
        _state.value = _state.value.copy(banner = "Added to today: $title")
    }

    fun dismissBanner() {
        _state.value = _state.value.copy(banner = null)
    }
}
