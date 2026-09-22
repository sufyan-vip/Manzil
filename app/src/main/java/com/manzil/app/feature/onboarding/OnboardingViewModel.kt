package com.manzil.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manzil.app.data.local.SeedData
import com.manzil.app.data.prefs.SettingsRepository
import com.manzil.app.data.repository.ManzilRepository
import com.manzil.app.notification.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class OnboardingState(
    val step: Int = 0,
    val name: String = "",
    val university: String = "COMSATS Sahiwal",
    val semester: String = "1st",
    val mainGoal: String = "",
    val targetDate: String = "",
    val skills: String = "",
    val dailyHours: Int = 3,
    val morningTime: String = "07:00",
    val eveningTime: String = "21:30",
    val selectedPlatforms: List<String> = listOf("Instagram", "LinkedIn"),
    val allPlatforms: List<String> = listOf("Instagram", "LinkedIn", "Facebook", "X", "Reddit", "Discord"),
    val saving: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val repository: ManzilRepository,
    private val scheduler: AlarmScheduler
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun next() {
        _state.value = _state.value.copy(step = (_state.value.step + 1).coerceAtMost(3))
    }

    fun back() {
        _state.value = _state.value.copy(step = (_state.value.step - 1).coerceAtLeast(0))
    }

    fun updateName(value: String) {
        _state.value = _state.value.copy(name = value)
    }

    fun updateUniversity(value: String) {
        _state.value = _state.value.copy(university = value)
    }

    fun updateSemester(value: String) {
        _state.value = _state.value.copy(semester = value)
    }

    fun updateMainGoal(value: String) {
        _state.value = _state.value.copy(mainGoal = value)
    }

    fun updateTargetDate(value: String) {
        _state.value = _state.value.copy(targetDate = value)
    }

    fun updateSkills(value: String) {
        _state.value = _state.value.copy(skills = value)
    }

    fun setDailyHours(hours: Int) {
        _state.value = _state.value.copy(dailyHours = hours)
    }

    fun setMorning(value: String) {
        _state.value = _state.value.copy(morningTime = value)
    }

    fun setEvening(value: String) {
        _state.value = _state.value.copy(eveningTime = value)
    }

    fun togglePlatform(platform: String) {
        val selected = _state.value.selectedPlatforms.toMutableList()
        if (!selected.remove(platform)) selected.add(platform)
        _state.value = _state.value.copy(selectedPlatforms = selected)
    }

    /** Saves everything and creates a real first plan so the app is never empty. */
    fun finish() = viewModelScope.launch {
        val current = _state.value
        settings.saveOnboarding(
            name = current.name,
            university = current.university,
            semester = current.semester,
            skills = current.skills,
            mainGoal = current.mainGoal,
            targetDate = current.targetDate,
            dailyHours = current.dailyHours,
            morningTime = current.morningTime,
            eveningTime = current.eveningTime,
            platforms = current.selectedPlatforms
        )
        settings.setDeepWorkTarget(current.dailyHours * 7)
        repository.seedIfNeeded()
        renameRootGoal(current)
        scheduler.scheduleBriefing(
            current.morningTime.substringBefore(':').toIntOrNull() ?: 7,
            current.morningTime.substringAfter(':').toIntOrNull() ?: 0
        )
        scheduler.scheduleEvening(
            current.eveningTime.substringBefore(':').toIntOrNull() ?: 21,
            current.eveningTime.substringAfter(':').toIntOrNull() ?: 30
        )
        scheduler.scheduleMaintenance()
    }

    /** The user's own words replace the placeholder root goal title. */
    private suspend fun renameRootGoal(current: OnboardingState) {
        val root = repository.allGoals().firstOrNull { it.id == SeedData.ROOT_GOAL_ID } ?: return
        val title = current.mainGoal.ifBlank { root.title }
        val target = current.targetDate.trim().let { raw ->
            when {
                raw.isEmpty() -> root.targetDate
                raw.length >= 4 && raw.take(4).toIntOrNull() != null ->
                    LocalDate.of(raw.take(4).toInt(), 12, 31)
                else -> root.targetDate
            }
        }
        repository.updateGoal(
            root.copy(
                title = title,
                description = if (current.mainGoal.isBlank()) {
                    root.description
                } else {
                    "Your goal: ${current.mainGoal}. Skills: ${current.skills.ifBlank { "starting fresh" }}."
                },
                targetDate = target
            )
        )
    }
}
