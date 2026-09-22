package com.manzil.app.feature.today

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manzil.app.core.adaptive.AdaptiveEngine
import com.manzil.app.core.common.Fmt
import com.manzil.app.core.common.QuickCaptureParser
import com.manzil.app.core.common.Result
import com.manzil.app.core.crypto.SecretVault
import com.manzil.app.data.local.entity.CalendarEvent
import com.manzil.app.data.local.entity.Task
import com.manzil.app.data.local.entity.TimeEntry
import com.manzil.app.data.local.entity.TimeSource
import com.manzil.app.data.prefs.FocusTimerState
import com.manzil.app.data.prefs.FocusTimerStore
import com.manzil.app.data.prefs.SettingsRepository
import com.manzil.app.data.remote.openrouter.OpenRouterApi
import com.manzil.app.data.remote.openrouter.PromptLibrary
import com.manzil.app.data.remote.openrouter.dto.ChatRequest
import com.manzil.app.data.repository.InsightsRepository
import com.manzil.app.data.repository.ManzilRepository
import com.manzil.app.domain.model.DayTask
import com.manzil.app.domain.model.HabitRow
import com.manzil.app.domain.model.ProgressRings
import com.manzil.app.domain.model.StreakInfo
import com.manzil.app.domain.model.TodayBriefing
import com.manzil.app.widget.WidgetSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

data class TodayState(
    val loading: Boolean = true,
    val greeting: String = "Good morning",
    val userName: String = "",
    val dateLabel: String = "",
    val planLine: String = "",
    val dayCounter: Int = 1,
    val briefing: TodayBriefing = TodayBriefing(1, emptyList(), 0, 0, emptyList(), null, 0, 0),
    val briefingText: String = "",
    val tasks: List<DayTask> = emptyList(),
    val leftovers: List<DayTask> = emptyList(),
    val events: List<CalendarEvent> = emptyList(),
    val rings: ProgressRings = ProgressRings(0f, 0f, 0f),
    val streak: StreakInfo = StreakInfo(0, 0, emptyList()),
    val heat: List<Pair<LocalDate, Int>> = emptyList(),
    val habits: List<HabitRow> = emptyList(),
    val timer: FocusTimerState = FocusTimerState(),
    val hasApiKey: Boolean = false,
    val banner: String? = null,
    val bannerIsError: Boolean = false,
    val planning: Boolean = false
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ManzilRepository,
    private val insights: InsightsRepository,
    private val settings: SettingsRepository,
    private val timerStore: FocusTimerStore,
    private val secrets: SecretVault,
    private val ai: OpenRouterApi,
    private val adaptive: AdaptiveEngine
) : ViewModel() {

    private val _state = MutableStateFlow(TodayState())
    val state: StateFlow<TodayState> = _state.asStateFlow()

    private val today: LocalDate get() = LocalDate.now()

    init {
        observeSettings()
        observeTasks()
        observeTimer()
        runStartupMaintenance()
        refresh()
    }

    private fun observeSettings() = viewModelScope.launch {
        settings.settings.collect { appSettings ->
            _state.value = _state.value.copy(
                userName = appSettings.displayName,
                greeting = if (appSettings.language == "ur") Fmt.greetingUr() else Fmt.greeting(),
                dateLabel = Fmt.dateLong(LocalDate.now()),
                planLine = buildString {
                    if (appSettings.university.isNotBlank()) append(appSettings.university)
                    if (appSettings.semester.isNotBlank()) {
                        if (isNotEmpty()) append(" · ")
                        append("Semester ${appSettings.semester}")
                    }
                    if (isNotEmpty()) append(" · ")
                    append("${appSettings.dailyHours}h deep work")
                }
            )
        }
    }

    private fun observeTasks() = viewModelScope.launch {
        repository.observeDayTasks(LocalDate.now()).collect { tasks ->
            _state.value = _state.value.copy(tasks = tasks, loading = false)
            runCatching { repository.refreshAllProgress() }
            pushWidget()
        }
    }

    private fun observeTimer() = viewModelScope.launch {
        timerStore.state.collect { timer -> _state.value = _state.value.copy(timer = timer) }
    }

    private fun runStartupMaintenance() = viewModelScope.launch {
        runCatching { repository.runDailyMaintenance() }
        runCatching { repository.refreshAllProgress() }
    }

    fun refresh() = viewModelScope.launch {
        runCatching {
            val briefing = repository.briefingData()
            val rings = insights.progressRings()
            val streak = insights.streakInfo()
            val heat = repository.completionsByDay(today.minusDays(83), today)
            val leftovers = repository.dayTasks(today.minusDays(1)).filter { !it.done }
            val events = repository.eventsBetween(
                Fmt.startOfDayMillis(today),
                Fmt.endOfDayMillis(today)
            )
            _state.value = _state.value.copy(
                briefing = briefing,
                briefingText = briefingText(briefing),
                rings = rings,
                streak = streak,
                heat = heat,
                leftovers = leftovers,
                events = events,
                habits = repository.observeHabits().first(),
                hasApiKey = secrets.hasApiKey(),
                dayCounter = briefing.dayCounter,
                loading = false
            )
            pushWidget()
        }
    }

    private fun briefingText(briefing: TodayBriefing): String = buildString {
        appendLine("DO TODAY (${briefing.todayTasks.size})")
        if (briefing.todayTasks.isEmpty()) appendLine(" • Nothing planned — add one task below")
        briefing.todayTasks.take(5).forEach { appendLine(" • $it") }
        appendLine()
        append("DONE YESTERDAY (${briefing.yesterdayDone}/${briefing.yesterdayPlanned})")
        briefing.pending.firstOrNull()?.let { append("  ·  reh gaya: $it") }
        briefing.goalMove?.let { appendLine().append("GOAL MOVE: $it") }
        appendLine()
        append("Streak ${briefing.streak} days · Root goal ${briefing.rootProgress}%")
    }

    /* --------------------------------------------------------------------------------------- */

    fun toggleDone(dayTask: DayTask) = viewModelScope.launch {
        repository.toggleTaskDone(dayTask.task, !dayTask.done, dayTask.date)
        dayTask.task.goalId?.let { repository.refreshProgress(it) }
        refresh()
    }

    fun moveLeftoverToToday(dayTask: DayTask) = viewModelScope.launch {
        repository.moveTask(dayTask.task, today, dayTask.date)
        _state.value = _state.value.copy(banner = "Moved \"${dayTask.task.title}\" to today.")
        refresh()
    }

    fun dropLeftover(dayTask: DayTask) = viewModelScope.launch {
        repository.dropTask(dayTask.task)
        _state.value = _state.value.copy(banner = "Dropped \"${dayTask.task.title}\". No guilt.")
        refresh()
    }

    fun postpone(task: Task, toDate: LocalDate) = viewModelScope.launch {
        repository.postponeTask(
            task = task,
            from = today,
            toDate = toDate,
            reason = adaptive.rolloverReason(1, task.autoRolledCount + 1)
        )
        _state.value = _state.value.copy(banner = "Postponed to ${Fmt.dayLabel(toDate)}.")
        refresh()
    }

    fun moveTaskOrder(dayTask: DayTask, direction: Int) = viewModelScope.launch {
        val list = _state.value.tasks.toMutableList()
        val index = list.indexOfFirst { it.id == dayTask.id }
        val target = index + direction
        if (index < 0 || target < 0 || target >= list.size) return@launch
        val moved = list.removeAt(index)
        list.add(target, moved)
        repository.reorderDayTasks(list)
    }

    fun capture(input: String) = viewModelScope.launch {
        val parsed = QuickCaptureParser.parse(input)
        if (parsed.title.isBlank()) {
            _state.value = _state.value.copy(banner = "Type a task first.", bannerIsError = true)
            return@launch
        }
        val goalId = parsed.goalTag?.let { tag ->
            repository.allGoals().firstOrNull { goal ->
                goal.title.contains(tag, ignoreCase = true) ||
                    goal.category.name.equals(tag, ignoreCase = true)
            }?.id
        } ?: repository.rootGoal()?.id
        repository.createFromQuickCapture(parsed, goalId)
        _state.value = _state.value.copy(
            banner = "Added \"${parsed.title}\" · ${QuickCaptureParser.describe(parsed)}",
            bannerIsError = false
        )
        refresh()
    }

    fun toggleHabit(habitId: String, done: Boolean) = viewModelScope.launch {
        repository.toggleHabit(habitId, today, done)
        _state.value = _state.value.copy(habits = repository.observeHabits().first())
    }

    fun dismissBanner() {
        _state.value = _state.value.copy(banner = null, bannerIsError = false)
    }

    /* -------------------------------------- focus timer -------------------------------------- */

    fun startTimer(task: DayTask? = null) = viewModelScope.launch {
        timerStore.start(task?.task?.id, task?.task?.goalId, task?.task?.title ?: "Deep work")
        _state.value = _state.value.copy(banner = "Focus session started — phone face down.")
    }

    fun pauseTimer() = viewModelScope.launch { timerStore.pause() }

    fun resumeTimer() = viewModelScope.launch { timerStore.resume() }

    fun stopTimer() = viewModelScope.launch {
        val timer = timerStore.current()
        val elapsed = timer.elapsed()
        val minutes = (elapsed / 60000L).toInt()
        if (elapsed < 30_000L) {
            timerStore.reset()
            _state.value = _state.value.copy(
                banner = "Under 30 seconds — nothing saved.",
                bannerIsError = true
            )
            return@launch
        }
        val endedAt = System.currentTimeMillis()
        repository.saveTimeEntry(
            TimeEntry(
                id = "time_" + UUID.randomUUID().toString().take(8),
                taskId = timer.taskId,
                goalId = timer.goalId,
                label = timer.label.ifBlank { "Deep work" },
                startedAt = endedAt - elapsed,
                endedAt = endedAt,
                source = TimeSource.TIMER
            ),
            addMinutesToTask = true
        )
        timer.taskId?.let { repository.refreshProgress(it) }
        timerStore.reset()
        _state.value = _state.value.copy(banner = "Saved ${Fmt.duration(minutes)} of focused work.")
        refresh()
    }

    /* ---------------------------------------- AI plan ---------------------------------------- */

    fun regeneratePlan() = viewModelScope.launch {
        val current = _state.value
        val appSettings = settings.current()

        if (!current.hasApiKey) {
            val plan = adaptive.generateNextTasks(
                currentProgress = current.briefing.rootProgress,
                mainGoal = repository.rootGoal()?.title ?: "your goal",
                skills = appSettings.skills.split(",").map { it.trim() }.filter { it.isNotBlank() },
                preferredPlatforms = appSettings.platforms
            )
            _state.value = current.copy(
                banner = "Offline adaptive plan → " + plan.joinToString(" · ")
            )
            return@launch
        }

        _state.value = current.copy(planning = true, banner = null)
        val prompt = PromptLibrary.PLAN_MY_DAY
            .replace("{DATE}", today.toString())
            .replace("{TASKS}", current.tasks.joinToString("; ") { it.task.title }.ifBlank { "none" })
            .replace("{CALENDAR}", current.events.joinToString("; ") { it.title }.ifBlank { "clear" })
            .replace("{MOOD}", "not logged")
            .replace("{COMPLETION_RATE}", (current.rings.today * 100).toInt().toString())
            .replace("{SKILLS}", appSettings.skills.ifBlank { "React, JavaScript" })
            .replace("{SEMESTER}", appSettings.semester.ifBlank { "current" })
            .replace("{EXAMS}", "none logged")
            .replace("{PLATFORMS}", appSettings.platforms.joinToString(", ").ifBlank { "Instagram, LinkedIn" })
            .replace("{MAIN_GOAL}", repository.rootGoal()?.title ?: "my goal")

        when (val result = ai.chat(ChatRequest(messages = listOf(ChatRequest.Message("user", prompt))))) {
            is Result.Success -> {
                val text = result.data.choices.firstOrNull()?.message?.content.orEmpty()
                _state.value = _state.value.copy(planning = false, briefingText = text)
            }
            is Result.Error -> _state.value = _state.value.copy(
                planning = false,
                banner = result.message,
                bannerIsError = true
            )
            else -> _state.value = _state.value.copy(planning = false)
        }
    }

    private fun pushWidget() {
        val current = _state.value
        runCatching {
            WidgetSnapshot.update(
                context = context,
                tasks = current.tasks.map { it.task.title to it.done },
                streakDays = current.streak.current,
                focusLabel = current.timer.label.takeIf { current.timer.isRunning }
            )
        }
    }
}
