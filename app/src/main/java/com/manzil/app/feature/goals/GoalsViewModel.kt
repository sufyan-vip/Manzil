package com.manzil.app.feature.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manzil.app.data.local.entity.Goal
import com.manzil.app.data.local.entity.GoalCategory
import com.manzil.app.data.local.entity.GoalStatus
import com.manzil.app.data.local.entity.Milestone
import com.manzil.app.data.repository.InsightsRepository
import com.manzil.app.data.repository.ManzilRepository
import com.manzil.app.domain.model.GoalNode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class GoalDraft(
    val id: String? = null,
    val title: String = "",
    val description: String = "",
    val category: GoalCategory = GoalCategory.SKILL,
    val parentGoalId: String? = null,
    val targetDate: LocalDate? = LocalDate.now().plusMonths(6),
    val priority: Int = 2,
    val metricLabel: String = "",
    val metricTarget: String = "",
    val metricUnit: String = "",
    val isPerpetual: Boolean = true
) {
    val isNew: Boolean get() = id == null
}

data class MilestoneDraft(
    val goalId: String,
    val title: String = "",
    val dueDate: LocalDate? = LocalDate.now().plusWeeks(2)
)

data class GoalsState(
    val loading: Boolean = true,
    val tree: List<GoalNode> = emptyList(),
    val expanded: Set<String> = emptySet(),
    val draft: GoalDraft? = null,
    val milestoneDraft: MilestoneDraft? = null,
    val metricGoal: Goal? = null,
    val metricValue: String = "",
    val banner: String? = null,
    val overallProgress: Int = 0,
    val activeCount: Int = 0
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val repository: ManzilRepository,
    private val insights: InsightsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GoalsState())
    val state: StateFlow<GoalsState> = _state.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            repository.observeAllTasks().collect { refresh() }
        }
    }

    fun refresh() = viewModelScope.launch {
        val tree = insights.goalTree()
        _state.value = _state.value.copy(
            loading = false,
            tree = tree,
            overallProgress = tree.firstOrNull()?.progress ?: 0,
            activeCount = tree.flatMap { flatten(it) }.count { it.goal.status == GoalStatus.ACTIVE }
        )
    }

    private fun flatten(node: GoalNode): List<GoalNode> =
        listOf(node) + node.children.flatMap { flatten(it) }

    fun toggleExpanded(goalId: String) {
        val expanded = _state.value.expanded.toMutableSet()
        if (!expanded.add(goalId)) expanded.remove(goalId)
        _state.value = _state.value.copy(expanded = expanded)
    }

    fun openNewGoal(parentId: String? = null) {
        _state.value = _state.value.copy(
            draft = GoalDraft(parentGoalId = parentId, priority = if (parentId == null) 1 else 2)
        )
    }

    fun openGoal(goal: Goal) {
        _state.value = _state.value.copy(
            draft = GoalDraft(
                id = goal.id,
                title = goal.title,
                description = goal.description,
                category = goal.category,
                parentGoalId = goal.parentGoalId,
                targetDate = goal.targetDate,
                priority = goal.priority,
                metricLabel = goal.metricLabel.orEmpty(),
                metricTarget = goal.metricTarget?.toString().orEmpty(),
                metricUnit = goal.metricUnit.orEmpty(),
                isPerpetual = goal.isPerpetual
            )
        )
    }

    fun updateDraft(transform: (GoalDraft) -> GoalDraft) {
        val current = _state.value.draft ?: return
        _state.value = _state.value.copy(draft = transform(current))
    }

    fun closeDraft() {
        _state.value = _state.value.copy(draft = null)
    }

    fun saveDraft() = viewModelScope.launch {
        val draft = _state.value.draft ?: return@launch
        if (draft.title.isBlank()) {
            _state.value = _state.value.copy(banner = "Give the goal a title.")
            return@launch
        }
        val target = draft.metricTarget.toDoubleOrNull()
        if (draft.isNew) {
            repository.createGoal(
                title = draft.title,
                description = draft.description,
                category = draft.category,
                parentGoalId = draft.parentGoalId,
                targetDate = draft.targetDate,
                priority = draft.priority,
                metricLabel = draft.metricLabel.ifBlank { null },
                metricTarget = target,
                metricUnit = draft.metricUnit.ifBlank { null },
                isPerpetual = draft.isPerpetual
            )
            _state.value = _state.value.copy(draft = null, banner = "Goal added — pulse updated.")
        } else {
            val existing = repository.allGoals().firstOrNull { it.id == draft.id }
            if (existing != null) {
                repository.updateGoal(
                    existing.copy(
                        title = draft.title.trim(),
                        description = draft.description,
                        category = draft.category,
                        parentGoalId = draft.parentGoalId,
                        targetDate = draft.targetDate,
                        priority = draft.priority,
                        metricLabel = draft.metricLabel.ifBlank { null },
                        metricTarget = target,
                        metricUnit = draft.metricUnit.ifBlank { null },
                        isPerpetual = draft.isPerpetual
                    )
                )
            }
            _state.value = _state.value.copy(draft = null, banner = "Goal updated — see Goal Pulse.")
        }
        refresh()
    }

    fun deleteGoal(goal: Goal) = viewModelScope.launch {
        repository.deleteGoal(goal)
        _state.value = _state.value.copy(draft = null, banner = "Goal removed.")
        refresh()
    }

    fun setStatus(goal: Goal, status: GoalStatus) = viewModelScope.launch {
        repository.setGoalStatus(goal, status)
        _state.value = _state.value.copy(draft = null, banner = "Goal marked ${status.name.lowercase()}.")
        refresh()
    }

    fun openMilestoneDraft(goalId: String) {
        _state.value = _state.value.copy(milestoneDraft = MilestoneDraft(goalId))
    }

    fun updateMilestoneDraft(transform: (MilestoneDraft) -> MilestoneDraft) {
        val current = _state.value.milestoneDraft ?: return
        _state.value = _state.value.copy(milestoneDraft = transform(current))
    }

    fun closeMilestoneDraft() {
        _state.value = _state.value.copy(milestoneDraft = null)
    }

    fun saveMilestone() = viewModelScope.launch {
        val draft = _state.value.milestoneDraft ?: return@launch
        if (draft.title.isBlank()) {
            _state.value = _state.value.copy(banner = "Milestone needs a title.")
            return@launch
        }
        repository.addMilestone(draft.goalId, draft.title, draft.dueDate)
        _state.value = _state.value.copy(
            milestoneDraft = null,
            banner = "Milestone added.",
            expanded = _state.value.expanded + draft.goalId
        )
        refresh()
    }

    fun toggleMilestone(milestone: Milestone) = viewModelScope.launch {
        repository.toggleMilestone(milestone, !milestone.done)
        refresh()
    }

    fun deleteMilestone(milestone: Milestone) = viewModelScope.launch {
        repository.deleteMilestone(milestone)
        refresh()
    }

    fun openMetric(goal: Goal) {
        _state.value = _state.value.copy(metricGoal = goal, metricValue = "")
    }

    fun updateMetricValue(value: String) {
        _state.value = _state.value.copy(metricValue = value)
    }

    fun closeMetric() {
        _state.value = _state.value.copy(metricGoal = null)
    }

    fun saveMetric() = viewModelScope.launch {
        val goal = _state.value.metricGoal ?: return@launch
        val value = _state.value.metricValue.replace(",", "").toDoubleOrNull()
        if (value == null) {
            _state.value = _state.value.copy(banner = "Enter a number for the metric.")
            return@launch
        }
        repository.trackMetricValue(goal, value)
        _state.value = _state.value.copy(metricGoal = null, banner = "${goal.title} updated to ${value}.")
        refresh()
    }

    fun dismissBanner() {
        _state.value = _state.value.copy(banner = null)
    }
}

/** Goal Pulse: what changed in the plan, newest first. */
data class PulseState(
    val loading: Boolean = true,
    val items: List<com.manzil.app.domain.model.PulseItem> = emptyList(),
    val filterIndex: Int = 0,
    val explain: String? = null,
    val explaining: Boolean = false
)

@HiltViewModel
class GoalPulseViewModel @Inject constructor(
    private val repository: ManzilRepository,
    private val ai: com.manzil.app.data.remote.openrouter.OpenRouterApi,
    private val secrets: com.manzil.app.core.crypto.SecretVault
) : ViewModel() {

    private val _state = MutableStateFlow(PulseState())
    val state: StateFlow<PulseState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeRevisions().collect { load() }
        }
    }

    fun setFilter(index: Int) {
        _state.value = _state.value.copy(filterIndex = index)
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val filter = _state.value.filterIndex
        val now = LocalDate.now()
        val since = when (filter) {
            0 -> com.manzil.app.core.common.Fmt.startOfDayMillis(
                com.manzil.app.core.common.Fmt.startOfWeek(now)
            )
            1 -> com.manzil.app.core.common.Fmt.startOfDayMillis(now.withDayOfMonth(1))
            else -> 0L
        }
        _state.value = _state.value.copy(loading = false, items = repository.revisionFeed(since))
    }

    /** "Explain what changed" — local summary, upgraded by AI when a key exists. */
    fun explainChanges() = viewModelScope.launch {
        val items = _state.value.items.take(10)
        val local = items.joinToString("\n") { item ->
            val change = item.changes.firstOrNull()
            "• ${item.goalTitle}: ${item.changeType.name.lowercase()}" +
                (change?.let { " — ${it.field} ${it.from} → ${it.to}" } ?: "")
        }
        if (items.isEmpty()) {
            _state.value = _state.value.copy(explain = "No goal changes recorded yet.")
            return@launch
        }
        if (!secrets.hasApiKey()) {
            _state.value = _state.value.copy(explain = "Local summary:\n$local")
            return@launch
        }
        _state.value = _state.value.copy(explaining = true)
        val prompt = com.manzil.app.data.remote.openrouter.PromptLibrary.EXPLAIN_CHANGES
            .replace("{DIFF_JSON}", local)
        when (val result = ai.chat(
            com.manzil.app.data.remote.openrouter.dto.ChatRequest(
                messages = listOf(
                    com.manzil.app.data.remote.openrouter.dto.ChatRequest.Message("user", prompt)
                )
            )
        )) {
            is com.manzil.app.core.common.Result.Success -> _state.value = _state.value.copy(
                explaining = false,
                explain = result.data.choices.firstOrNull()?.message?.content ?: local
            )
            is com.manzil.app.core.common.Result.Error -> _state.value = _state.value.copy(
                explaining = false,
                explain = "${result.message}\n\n$local"
            )
            else -> _state.value = _state.value.copy(explaining = false)
        }
    }

    fun dismissExplain() {
        _state.value = _state.value.copy(explain = null)
    }
}

/** KPI dashboard. */
data class KpiState(
    val loading: Boolean = true,
    val cards: List<com.manzil.app.domain.model.KpiCard> = emptyList(),
    val editingKey: String? = null,
    val editingLabel: String? = null,
    val value: String = "",
    val banner: String? = null
)

@HiltViewModel
class KpiViewModel @Inject constructor(
    private val insights: InsightsRepository,
    private val trackKpi: com.manzil.app.domain.usecase.TrackKpi
) : ViewModel() {

    private val _state = MutableStateFlow(KpiState())
    val state: StateFlow<KpiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            insights.observeKpiCards().collect { cards ->
                _state.value = _state.value.copy(loading = false, cards = cards)
            }
        }
    }

    fun startEditing(key: String, label: String) {
        _state.value = _state.value.copy(editingKey = key, editingLabel = label, value = "")
    }

    fun updateValue(value: String) {
        _state.value = _state.value.copy(value = value)
    }

    fun cancelEditing() {
        _state.value = _state.value.copy(editingKey = null, editingLabel = null)
    }

    fun save() = viewModelScope.launch {
        val key = _state.value.editingKey ?: return@launch
        val value = _state.value.value.replace(",", "").toDoubleOrNull()
        if (value == null) {
            _state.value = _state.value.copy(banner = "Enter a number.")
            return@launch
        }
        trackKpi.addReading(key, value, "Updated from the KPI dashboard")
        _state.value = _state.value.copy(
            editingKey = null,
            editingLabel = null,
            banner = "Reading saved — trend updated."
        )
    }

    fun dismissBanner() {
        _state.value = _state.value.copy(banner = null)
    }
}
