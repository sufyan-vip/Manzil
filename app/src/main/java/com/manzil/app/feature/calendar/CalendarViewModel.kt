package com.manzil.app.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manzil.app.core.common.Fmt
import com.manzil.app.data.local.entity.CalendarEvent
import com.manzil.app.data.local.entity.EventSource
import com.manzil.app.data.local.entity.Task
import com.manzil.app.data.repository.ManzilRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import javax.inject.Inject

enum class CalendarMode { MONTH, WEEK, AGENDA }

data class EventDraft(
    val id: String? = null,
    val title: String = "",
    val description: String = "",
    val date: LocalDate = LocalDate.now(),
    val startTime: LocalTime = LocalTime.of(9, 0),
    val endTime: LocalTime = LocalTime.of(10, 0)
) {
    val isNew: Boolean get() = id == null
}

data class CalendarState(
    val loading: Boolean = true,
    val mode: CalendarMode = CalendarMode.MONTH,
    val month: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val events: List<CalendarEvent> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val draft: EventDraft? = null,
    val banner: String? = null,
    val conflicts: Int = 0
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: ManzilRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CalendarState())
    val state: StateFlow<CalendarState> = _state.asStateFlow()

    init {
        load()
        viewModelScope.launch {
            repository.observeAllTasks().collect {
                load()
            }
        }
    }

    fun setMode(mode: CalendarMode) {
        _state.value = _state.value.copy(mode = mode)
    }

    fun selectDate(date: LocalDate) {
        _state.value = _state.value.copy(selectedDate = date, month = YearMonth.from(date))
        load()
    }

    fun shiftMonth(delta: Long) {
        val month = _state.value.month.plusMonths(delta)
        _state.value = _state.value.copy(month = month, selectedDate = month.atDay(1))
        load()
    }

    fun shiftDays(delta: Long) {
        val date = _state.value.selectedDate.plusDays(delta)
        _state.value = _state.value.copy(selectedDate = date, month = YearMonth.from(date))
        load()
    }

    fun load() = viewModelScope.launch {
        val month = _state.value.month
        val from = Fmt.startOfDayMillis(month.minusMonths(1).atDay(1))
        val to = Fmt.endOfDayMillis(month.plusMonths(1).atEndOfMonth())
        val events = repository.eventsBetween(from, to)
        val tasks = repository.allTasks().filter { task -> task.dueDate != null }
        _state.value = _state.value.copy(
            loading = false,
            events = events,
            tasks = tasks,
            conflicts = countConflicts(events)
        )
    }

    private fun countConflicts(events: List<CalendarEvent>): Int {
        val overlapping = mutableSetOf<String>()
        val byDay = events.groupBy { Fmt.dayOfMillis(it.startAt) }
        byDay.values.forEach { dayEvents ->
            val sorted = dayEvents.sortedBy { it.startAt }
            for (index in 1 until sorted.size) {
                if (sorted[index].startAt < sorted[index - 1].endAt) {
                    overlapping += sorted[index].id
                    overlapping += sorted[index - 1].id
                }
            }
        }
        return overlapping.size
    }

    fun eventsOn(date: LocalDate): List<CalendarEvent> =
        _state.value.events.filter { Fmt.dayOfMillis(it.startAt) == date }.sortedBy { it.startAt }

    fun tasksOn(date: LocalDate): List<Task> =
        _state.value.tasks.filter { it.dueDate == date }
            .sortedBy { it.dueTime ?: LocalTime.MAX }

    fun openNewEvent(date: LocalDate = _state.value.selectedDate, hour: Int = 9) {
        _state.value = _state.value.copy(
            draft = EventDraft(
                date = date,
                startTime = LocalTime.of(hour.coerceIn(0, 23), 0),
                endTime = LocalTime.of((hour + 1).coerceIn(0, 23), 0)
            )
        )
    }

    fun openEvent(event: CalendarEvent) {
        _state.value = _state.value.copy(
            draft = EventDraft(
                id = event.id,
                title = event.title,
                description = event.description,
                date = Fmt.dayOfMillis(event.startAt),
                startTime = Fmt.localTimeOf(event.startAt),
                endTime = Fmt.localTimeOf(event.endAt)
            )
        )
    }

    fun updateDraft(transform: (EventDraft) -> EventDraft) {
        val current = _state.value.draft ?: return
        _state.value = _state.value.copy(draft = transform(current))
    }

    fun closeDraft() {
        _state.value = _state.value.copy(draft = null)
    }

    fun saveDraft() = viewModelScope.launch {
        val draft = _state.value.draft ?: return@launch
        if (draft.title.isBlank()) {
            _state.value = _state.value.copy(banner = "Give the event a title.")
            return@launch
        }
        val startAt = Fmt.millisAt(draft.date, draft.startTime)
        val endAt = Fmt.millisAt(draft.date, draft.endTime)
        if (draft.isNew) {
            repository.createEvent(
                title = draft.title,
                description = draft.description,
                startAt = startAt,
                endAt = endAt,
                source = EventSource.LOCAL
            )
        } else {
            val existing = _state.value.events.firstOrNull { it.id == draft.id }
            if (existing != null) {
                repository.updateEvent(
                    existing.copy(
                        title = draft.title,
                        description = draft.description,
                        startAt = startAt,
                        endAt = endAt
                    )
                )
            }
        }
        _state.value = _state.value.copy(draft = null, banner = "Saved to your calendar.")
        load()
    }

    fun deleteDraft() = viewModelScope.launch {
        val draft = _state.value.draft ?: return@launch
        _state.value.events.firstOrNull { it.id == draft.id }?.let { repository.deleteEvent(it) }
        _state.value = _state.value.copy(draft = null, banner = "Event deleted.")
        load()
    }

    /** One-tap: turn a scheduled task into a real calendar block. */
    fun planTaskBlock(task: Task) = viewModelScope.launch {
        val date = task.dueDate ?: _state.value.selectedDate
        val start = task.dueTime ?: LocalTime.of(9, 0)
        val minutes = task.estimatedMinutes ?: 60
        repository.createEvent(
            title = task.title,
            description = "Planned from the task list",
            startAt = Fmt.millisAt(date, start),
            endAt = Fmt.millisAt(date, start).let { it + minutes * 60_000L },
            source = EventSource.TASK,
            linkedTaskId = task.id
        )
        _state.value = _state.value.copy(banner = "Block added for \"${task.title}\"")
        load()
    }

    fun dismissBanner() {
        _state.value = _state.value.copy(banner = null)
    }
}
