package com.manzil.app.feature.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manzil.app.core.adaptive.AdaptiveEngine
import com.manzil.app.core.common.Fmt
import com.manzil.app.data.prefs.SettingsRepository
import com.manzil.app.data.repository.ManzilRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class PlaybookEntry(
    val platform: String,
    val cadence: String,
    val where: String,
    val script: String,
    val taskTitle: String
)

data class ToolsState(
    val banner: String? = null,
    val todayAction: AdaptiveEngine.ClientAction = AdaptiveEngine.ClientAction(
        platform = "Instagram",
        searchQuery = "#smallbusiness",
        script = "…",
        followUp = "…",
        expected = "…"
    ),
    val playbook: List<PlaybookEntry> = emptyList(),
    val roadmap: String = ""
)

@HiltViewModel
class ToolsViewModel @Inject constructor(
    private val repository: ManzilRepository,
    private val settings: SettingsRepository,
    private val adaptive: AdaptiveEngine
) : ViewModel() {

    private val _state = MutableStateFlow(ToolsState())
    val state: StateFlow<ToolsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val appSettings = settings.current()
            _state.value = _state.value.copy(
                todayAction = adaptive.clientActionOfTheDay(
                    platforms = appSettings.platforms,
                    skills = appSettings.skills,
                    dayIndex = LocalDate.now().dayOfMonth
                ),
                playbook = playbook(),
                roadmap = com.manzil.app.core.common.FreeToolsGuide.getZeroToGoalRoadmap()
            )
        }
    }

    private fun playbook(): List<PlaybookEntry> = listOf(
        PlaybookEntry(
            platform = "Instagram",
            cadence = "10 DMs / day",
            where = "Hashtags: #smallbusiness + your city, look at tagged photos of local shops",
            script = "Salam! Main Sufyan, software engineering student. Aapke page ke liye ek free website concept banaya hai — bhej doon?",
            taskTitle = "Send 10 Instagram DMs to local businesses"
        ),
        PlaybookEntry(
            platform = "Facebook groups",
            cadence = "1 value post / day",
            where = "Sahiwal Business, Freelancers Pakistan, Local Business Owners",
            script = "3 cheezein jo local businesses online customers kho rahi hain (screenshot proof). Business ka naam comment karein — free audit dunga.",
            taskTitle = "Post a value post in 2 Facebook groups"
        ),
        PlaybookEntry(
            platform = "LinkedIn",
            cadence = "20 messages / day",
            where = "Founders and business owners in Pakistan, filter by city",
            script = "Aapka kaam dekha — ek chhoti si web app is problem ko solve karti hai. 2-minute Loom bhej doon?",
            taskTitle = "Send 20 personalised LinkedIn messages"
        ),
        PlaybookEntry(
            platform = "X / Twitter",
            cadence = "1 post / day",
            where = "#buildinpublic, #100DaysOfCode",
            script = "Day X of building a software house from Sahiwal. Today I shipped <thing>. Learning in public.",
            taskTitle = "Post today's build-in-public update"
        ),
        PlaybookEntry(
            platform = "Reddit",
            cadence = "3 helpful answers / week",
            where = "r/forhire, r/PakistaniTech, r/webdev",
            script = "I solved <problem> for a local shop with <stack>. Writing the playbook — ask me anything.",
            taskTitle = "Answer 3 Reddit questions with real value"
        ),
        PlaybookEntry(
            platform = "WhatsApp Business",
            cadence = "Follow up every 48h",
            where = "Leads who replied but went quiet",
            script = "Salam! Sirf ye poochne ke liye ke mockup pasand aaya? Kuch change karna hai to bata dein.",
            taskTitle = "Follow up 3 silent leads on WhatsApp"
        ),
        PlaybookEntry(
            platform = "Local walk-in",
            cadence = "3 shops / week",
            where = "Main market Sahiwal — shops without a website or with an old one",
            script = "Assalam o alaikum! Main local student hoon, aapki shop ka free Google-ready page bana ke dikha sakta hoon.",
            taskTitle = "Visit 3 local shops and offer a free page"
        )
    )

    fun addClientTask() = viewModelScope.launch {
        repository.createTask(
            title = "Client hunt: ${_state.value.todayAction.platform} — 10 messages",
            dueDate = Fmt.today(),
            priority = 1,
            goalId = repository.allGoals().firstOrNull { it.title.contains("client", ignoreCase = true) }?.id
                ?: repository.rootGoal()?.id,
            estimatedMinutes = 45,
            notes = _state.value.todayAction.script
        )
        _state.value = _state.value.copy(banner = "Added to today's plan.")
    }

    fun addTask(title: String) = viewModelScope.launch {
        repository.createTask(
            title = title,
            dueDate = Fmt.today(),
            priority = 1,
            goalId = repository.rootGoal()?.id,
            estimatedMinutes = 45
        )
        _state.value = _state.value.copy(banner = "Task added: $title")
    }

    fun dismissBanner() {
        _state.value = _state.value.copy(banner = null)
    }
}
