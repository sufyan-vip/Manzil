package com.manzil.app.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manzil.app.core.common.Fmt
import com.manzil.app.data.local.entity.Task
import com.manzil.app.data.local.entity.TaskStatus
import com.manzil.app.data.repository.InsightsRepository
import com.manzil.app.data.repository.ManzilRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class TaskRowItem(
    val task: Task,
    val done: Boolean,
    val goalTitle: String?,
    val overdueDays: Int = 0
)

data class TaskDraft(
    val id: String? = null,
    val title: String = "",
    val notes: String = "",
    val dueDate: LocalDate? = LocalDate.now(),
    val dueTime: LocalTime? = null,
    val priority: Int = 2,
    val goalId: String? = null,
    val estimatedMinutes: Int? = 30,
    val recurrenceRule: String? = null
) {
    val isNew: Boolean get() = id == null
}

data class TasksState(
    val loading: Boolean = true,
    val filterIndex: Int = 0,
    val rows: List<TaskRowItem> = emptyList(),
    val counts: List<Int> = List(FILTERS.size) { 0 },
    val goals: List<Pair<String, String>> = emptyList(),
    val draft: TaskDraft? = null,
    val banner: String? = null
) {
    companion object {
        val FILTERS = listOf("Today", "Upcoming", "Overdue", "Done", "All")
    }
}

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val repository: ManzilRepository,
    private val insights: InsightsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TasksState())
    val state: StateFlow<TasksState> = _state.asStateFlow()

    private val filterIndex = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            combine(
                repository.observeAllTasks(),
                repository.observeGoals(),
                filterIndex
            ) { tasks, goals, filter ->
                val goalTitles = goals.associate { it.id to it.title }
                val today = LocalDate.now()
                val mapped = tasks.map { task ->
                    TaskRowItem(
                        task = task,
                        done = task.status == TaskStatus.DONE,
                        goalTitle = task.goalId?.let { goalTitles[it] },
                        overdueDays = task.dueDate
                            ?.takeIf { it.isBefore(today) && task.status != TaskStatus.DONE }
                            ?.let { Fmt.daysBetween(it, today) } ?: 0
                    )
                }
                val filtered = when (filter) {
                    0 -> mapped.filter { it.task.dueDate == today && !it.done }
                    1 -> mapped.filter {
                        it.task.dueDate != null && it.task.dueDate.isAfter(today) && !it.done
                    }.sortedBy { it.task.dueDate }
                    2 -> mapped.filter { it.overdueDays > 0 }.sortedBy { it.task.dueDate }
                    3 -> mapped.filter { it.done }.sortedByDescending { it.task.completedAt }
                    else -> mapped.sortedWith(
                        compareBy({ it.done }, { it.task.dueDate ?: LocalDate.MAX }, { it.task.priority })
                    )
                }
                val counts = listOf(
                    mapped.count { it.task.dueDate == today && !it.done },
                    mapped.count { it.task.dueDate?.isAfter(today) == true && !it.done },
                    mapped.count { it.overdueDays > 0 },
                    mapped.count { it.done },
                    mapped.size
                )
                TasksState(
                    loading = false,
                    filterIndex = filter,
                    rows = filtered,
                    counts = counts,
                    goals = goals.map { it.id to it.title },
                    draft = _state.value.draft,
                    banner = _state.value.banner
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    fun selectFilter(index: Int) {
        filterIndex.value = index
    }

    fun openNewTask(prefill: String = "") {
        _state.value = _state.value.copy(
            draft = TaskDraft(
                title = prefill,
                goalId = _state.value.goals.firstOrNull()?.first
            )
        )
    }

    fun openTask(task: Task) {
        _state.value = _state.value.copy(
            draft = TaskDraft(
                id = task.id,
                title = task.title,
                notes = task.notes,
                dueDate = task.dueDate,
                dueTime = task.dueTime,
                priority = task.priority,
                goalId = task.goalId,
                estimatedMinutes = task.estimatedMinutes,
                recurrenceRule = task.recurrenceRule
            )
        )
    }

    fun closeDraft() {
        _state.value = _state.value.copy(draft = null)
    }

    fun updateDraft(transform: (TaskDraft) -> TaskDraft) {
        val current = _state.value.draft ?: return
        _state.value = _state.value.copy(draft = transform(current))
    }

    fun saveDraft() = viewModelScope.launch {
        val draft = _state.value.draft ?: return@launch
        if (draft.title.isBlank()) {
            _state.value = _state.value.copy(banner = "Give the task a title first.")
            return@launch
        }
        if (draft.isNew) {
            repository.createTask(
                title = draft.title,
                dueDate = draft.dueDate,
                dueTime = draft.dueTime,
                priority = draft.priority,
                goalId = draft.goalId,
                notes = draft.notes,
                estimatedMinutes = draft.estimatedMinutes,
                recurrenceRule = draft.recurrenceRule
            )
            _state.value = _state.value.copy(draft = null, banner = "Task added.")
        } else {
            val existing = repository.taskById(draft.id!!)
            if (existing != null) {
                repository.updateTask(
                    existing.copy(
                        title = draft.title.trim(),
                        notes = draft.notes,
                        dueDate = draft.dueDate,
                        dueTime = draft.dueTime,
                        priority = draft.priority,
                        goalId = draft.goalId,
                        estimatedMinutes = draft.estimatedMinutes,
                        recurrenceRule = draft.recurrenceRule
                    ),
                    reExpand = true
                )
            }
            _state.value = _state.value.copy(draft = null, banner = "Task updated.")
        }
        draft.goalId?.let { repository.refreshProgress(it) }
    }

    fun deleteDraft() = viewModelScope.launch {
        val draft = _state.value.draft ?: return@launch
        draft.id?.let { id ->
            repository.taskById(id)?.let { repository.deleteTask(it) }
        }
        _state.value = _state.value.copy(draft = null, banner = "Task deleted.")
    }

    fun toggleDone(row: TaskRowItem) = viewModelScope.launch {
        val date = row.task.dueDate ?: LocalDate.now()
        repository.toggleTaskDone(row.task, !row.done, date)
        row.task.goalId?.let { repository.refreshProgress(it) }
    }

    fun postponeToTomorrow(row: TaskRowItem) = viewModelScope.launch {
        val from = row.task.dueDate ?: LocalDate.now()
        repository.postponeTask(
            task = row.task,
            from = from,
            toDate = LocalDate.now().plusDays(1),
            reason = "Postponed from the task list."
        )
        _state.value = _state.value.copy(banner = "Moved to tomorrow.")
    }

    fun trackNow(row: TaskRowItem) = viewModelScope.launch {
        repository.setTaskStatus(row.task, TaskStatus.IN_PROGRESS)
        _state.value = _state.value.copy(banner = "Marked as in progress.")
    }

    fun dismissBanner() {
        _state.value = _state.value.copy(banner = null)
    }

    suspend fun goalProgress(): Int = insights.progressRings().goal.let { (it * 100).toInt() }
}
