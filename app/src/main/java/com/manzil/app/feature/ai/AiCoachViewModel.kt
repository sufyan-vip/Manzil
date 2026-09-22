package com.manzil.app.feature.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manzil.app.core.adaptive.AdaptiveEngine
import com.manzil.app.core.common.Fmt
import com.manzil.app.core.common.Result
import com.manzil.app.core.crypto.SecretVault
import com.manzil.app.data.prefs.SettingsRepository
import com.manzil.app.data.remote.openrouter.OpenRouterApi
import com.manzil.app.data.remote.openrouter.PromptLibrary
import com.manzil.app.data.remote.openrouter.dto.ChatRequest
import com.manzil.app.data.repository.ManzilRepository
import com.manzil.app.domain.model.AiMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AiState(
    val messages: List<AiMessage> = emptyList(),
    val input: String = "",
    val sending: Boolean = false,
    val hasApiKey: Boolean = false,
    val modelLabel: String = "openrouter/auto",
    val error: String? = null
)

@HiltViewModel
class AiCoachViewModel @Inject constructor(
    private val ai: OpenRouterApi,
    private val secrets: SecretVault,
    private val settings: SettingsRepository,
    private val repository: ManzilRepository,
    private val adaptive: AdaptiveEngine
) : ViewModel() {

    private val _state = MutableStateFlow(AiState())
    val state: StateFlow<AiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val hasKey = secrets.hasApiKey()
            val model = secrets.getModel() ?: "openrouter/auto"
            _state.value = _state.value.copy(
                hasApiKey = hasKey,
                modelLabel = model,
                messages = listOf(
                    AiMessage(
                        id = "welcome",
                        role = "assistant",
                        content = if (hasKey) {
                            "Salam! Main Manzil ka adaptive coach hoon. Batao aaj kya block kar raha hai — ya neeche se koi action dabao."
                        } else {
                            "AI features are off until you add a free OpenRouter key in Settings.\n\nEverything else in Manzil already works offline — planning, rollover, search, streaks. The four actions below still give you a local plan."
                        },
                        atMillis = System.currentTimeMillis()
                    )
                )
            )
        }
    }

    fun updateInput(value: String) {
        _state.value = _state.value.copy(input = value)
    }

    fun send() = viewModelScope.launch {
        val text = _state.value.input.trim()
        if (text.isBlank()) return@launch
        _state.value = _state.value.copy(input = "")
        pushMessage(AiMessage("u_${UUID.randomUUID()}", "user", text, System.currentTimeMillis()))
        ask(text)
    }

    fun runAction(action: Action) = viewModelScope.launch {
        val appSettings = settings.current()
        val tasks = repository.dayTasks(Fmt.today())
        when (action) {
            Action.PLAN_DAY -> {
                val prompt = PromptLibrary.PLAN_MY_DAY
                    .replace("{DATE}", Fmt.today().toString())
                    .replace("{TASKS}", tasks.joinToString("; ") { it.task.title }.ifBlank { "none" })
                    .replace("{CALENDAR}", "not connected to device calendar")
                    .replace("{MOOD}", "not logged")
                    .replace("{COMPLETION_RATE}", tasks.let { list -> if (list.isEmpty()) 0 else list.count { it.done } * 100 / list.size }.toString())
                    .replace("{SKILLS}", appSettings.skills.ifBlank { "React, JavaScript" })
                    .replace("{SEMESTER}", appSettings.semester.ifBlank { "current" })
                    .replace("{EXAMS}", "none logged")
                    .replace("{PLATFORMS}", appSettings.platforms.joinToString(", ").ifBlank { "Instagram, LinkedIn" })
                    .replace("{MAIN_GOAL}", repository.rootGoal()?.title ?: "my goal")
                ask(prompt, userLabel = "Plan my day")
            }
            Action.WEEKLY_REVIEW -> {
                val prompt = PromptLibrary.WEEKLY_REVIEW
                    .replace("{REVIEWS}", tasks.take(10).joinToString("; ") { it.task.title })
                    .replace("{DIFFS}", repository.revisionFeed().take(8).joinToString("; ") { it.goalTitle })
                    .replace("{SKILLS}", appSettings.skills.ifBlank { "React" })
                ask(prompt, userLabel = "Weekly review")
            }
            Action.EXPLAIN_CHANGES -> {
                val prompt = PromptLibrary.EXPLAIN_CHANGES
                    .replace("{DIFF_JSON}", repository.revisionFeed().take(10).joinToString("\n") { item ->
                        "${item.goalTitle}: ${item.changeType.name}"
                    }.ifBlank { "no changes recorded" })
                ask(prompt, userLabel = "Explain what changed")
            }
            Action.UNBLOCK -> ask(
                PromptLibrary.UNBLOCK_ME
                    .replace("{BLOCKER}", "the thing I keep avoiding")
                    .replace("{TASKS}", tasks.joinToString("; ") { it.task.title })
                    .replace("{SKILLS}", appSettings.skills.ifBlank { "React" }),
                userLabel = "Unblock me"
            )
        }
    }

    /** Works with or without an API key: local answer first, AI polish if possible. */
    private suspend fun ask(prompt: String, userLabel: String? = null) {
        val appSettings = settings.current()
        if (userLabel != null) {
            pushMessage(AiMessage("u_${UUID.randomUUID()}", "user", userLabel, System.currentTimeMillis()))
        }
        if (!_state.value.hasApiKey) {
            val offline = offlineAnswer(userLabel ?: prompt, appSettings.platforms, appSettings.skills)
            pushMessage(AiMessage("a_${UUID.randomUUID()}", "assistant", offline, System.currentTimeMillis()))
            return
        }
        _state.value = _state.value.copy(sending = true, error = null)
        val history = _state.value.messages.takeLast(8).map { ChatRequest.Message(it.role, it.content) }
        val request = ChatRequest(
            model = _state.value.modelLabel,
            messages = history + ChatRequest.Message("user", prompt)
        )
        when (val result = ai.chat(request)) {
            is Result.Success -> {
                val text = result.data.choices.firstOrNull()?.message?.content.orEmpty()
                pushMessage(AiMessage("a_${UUID.randomUUID()}", "assistant", text, System.currentTimeMillis()))
                _state.value = _state.value.copy(sending = false)
            }
            is Result.Error -> {
                _state.value = _state.value.copy(sending = false, error = result.message)
                pushMessage(
                    AiMessage(
                        "a_${UUID.randomUUID()}",
                        "assistant",
                        offlineAnswer(userLabel ?: prompt, appSettings.platforms, appSettings.skills),
                        System.currentTimeMillis()
                    )
                )
            }
            else -> _state.value = _state.value.copy(sending = false)
        }
    }

    private fun offlineAnswer(question: String, platforms: List<String>, skills: String): String {
        val action = adaptive.clientActionOfTheDay(platforms, skills, Fmt.today().dayOfMonth)
        return buildString {
            appendLine("Local plan (AI off):")
            appendLine("1. " + adaptive.generateNextTasks(0, "your goal", emptyList(), platforms).first())
            appendLine("2. ${adaptive.latestInfoTasks(skills).first()}")
            appendLine("3. Client action — ${action.platform}: ${action.script}")
            if (question.length in 1..60) appendLine()
            if (question.isNotBlank()) {
                append("Your note: \"${question.take(60)}\" — add it as a task so it does not stay in your head.")
            }
        }.trim()
    }

    private fun pushMessage(message: AiMessage) {
        _state.value = _state.value.copy(messages = _state.value.messages + message)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun clearChat() {
        _state.value = _state.value.copy(
            messages = _state.value.messages.take(1),
            error = null
        )
    }

    enum class Action { PLAN_DAY, WEEKLY_REVIEW, EXPLAIN_CHANGES, UNBLOCK }
}
