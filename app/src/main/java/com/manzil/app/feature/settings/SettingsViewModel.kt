package com.manzil.app.feature.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manzil.app.core.common.Result
import com.manzil.app.core.crypto.SecretVault
import com.manzil.app.core.theme.ThemeMode
import com.manzil.app.data.backup.BackupManager
import com.manzil.app.data.prefs.AppSettings
import com.manzil.app.data.prefs.SettingsRepository
import com.manzil.app.data.remote.openrouter.OpenRouterApi
import com.manzil.app.data.remote.openrouter.OrModel
import com.manzil.app.data.repository.ManzilRepository
import com.manzil.app.notification.AlarmScheduler
import com.manzil.app.notification.Notifier
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SettingsState(
    val loading: Boolean = true,
    val settings: AppSettings = AppSettings(),
    val maskedKey: String? = null,
    val keyInput: String = "",
    val showKey: Boolean = false,
    val testing: Boolean = false,
    val status: String? = null,
    val statusIsError: Boolean = false,
    val models: List<OrModel> = emptyList(),
    val loadingModels: Boolean = false,
    val selectedModel: String = "openrouter/auto",
    val exactAlarmsAllowed: Boolean = true,
    val confirmErase: Boolean = false,
    val nameInput: String = "",
    val universityInput: String = "",
    val semesterInput: String = ""
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository,
    private val secrets: SecretVault,
    private val api: OpenRouterApi,
    private val backup: BackupManager,
    private val repository: ManzilRepository,
    private val notifier: Notifier,
    private val scheduler: AlarmScheduler
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settings.settings.collect { appSettings ->
                _state.value = _state.value.copy(
                    loading = false,
                    settings = appSettings,
                    nameInput = _state.value.nameInput.ifBlank { appSettings.name },
                    universityInput = _state.value.universityInput.ifBlank { appSettings.university },
                    semesterInput = _state.value.semesterInput.ifBlank { appSettings.semester },
                    exactAlarmsAllowed = scheduler.canScheduleExact(),
                    selectedModel = secrets.getModel() ?: "openrouter/auto",
                    maskedKey = (secrets as? com.manzil.app.core.crypto.KeystoreSecretVault)?.maskedApiKey()
                        ?: secrets.getApiKey()?.let { key -> "${key.take(9)}••••••••${key.takeLast(4)}" }
                )
            }
        }
    }

    fun updateKeyInput(value: String) {
        _state.value = _state.value.copy(keyInput = value)
    }

    fun toggleShowKey() {
        _state.value = _state.value.copy(showKey = !_state.value.showKey)
    }

    fun saveKey() = viewModelScope.launch {
        val key = _state.value.keyInput.trim()
        if (key.length < 12) {
            status("That does not look like an OpenRouter key.", isError = true)
            return@launch
        }
        secrets.saveApiKey(key)
        _state.value = _state.value.copy(keyInput = "", maskedKey = mask(key))
        status("Key saved on this device only, encrypted with the Android Keystore.")
        loadModels()
    }

    fun removeKey() = viewModelScope.launch {
        secrets.clearApiKey()
        _state.value = _state.value.copy(maskedKey = null, models = emptyList())
        status("Key removed. Manzil is fully offline again.")
    }

    fun testKey() = viewModelScope.launch {
        _state.value = _state.value.copy(testing = true)
        when (val result = api.listModels()) {
            is Result.Success -> {
                _state.value = _state.value.copy(testing = false, models = result.data)
                status("Connected — ${result.data.size} models available.")
            }
            is Result.Error -> {
                _state.value = _state.value.copy(testing = false)
                status(result.message, isError = true)
            }
            else -> _state.value = _state.value.copy(testing = false)
        }
    }

    fun loadModels() = viewModelScope.launch {
        if (!secrets.hasApiKey()) {
            status("Add a key first to browse models.", isError = true)
            return@launch
        }
        _state.value = _state.value.copy(loadingModels = true)
        when (val result = api.listModels()) {
            is Result.Success -> {
                val sorted = result.data
                    .filter { it.id.isNotBlank() }
                    .sortedWith(compareByDescending<OrModel> { it.isFree }.thenBy { it.id })
                    .take(60)
                _state.value = _state.value.copy(loadingModels = false, models = sorted)
                if (sorted.isEmpty()) status("No models returned — try again.", isError = true)
            }
            is Result.Error -> {
                _state.value = _state.value.copy(loadingModels = false)
                status(result.message, isError = true)
            }
            else -> _state.value = _state.value.copy(loadingModels = false)
        }
    }

    fun selectModel(modelId: String) = viewModelScope.launch {
        secrets.saveModel(modelId)
        _state.value = _state.value.copy(selectedModel = modelId)
        status("Model set to $modelId.")
    }

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { settings.setThemeMode(mode) }

    fun setLanguage(language: String) = viewModelScope.launch { settings.setLanguage(language) }

    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch { settings.setDynamicColor(enabled) }

    fun setNotifications(enabled: Boolean) = viewModelScope.launch {
        settings.setNotificationsEnabled(enabled)
        if (enabled) reschedule()
    }

    fun setAiBriefing(enabled: Boolean) = viewModelScope.launch { settings.setAiBriefingEnabled(enabled) }

    fun setNotificationTime(morning: Boolean, hour: Int, minute: Int) = viewModelScope.launch {
        val current = settings.current()
        val value = "%02d:%02d".format(hour, minute)
        val morningTime = if (morning) value else current.morningTime
        val eveningTime = if (morning) current.eveningTime else value
        settings.setNotificationTimes(morningTime, eveningTime)
        reschedule()
        status(if (morning) "Morning briefing set to $value." else "Evening review set to $value.")
    }

    fun sendTestNotification() = viewModelScope.launch {
        notifier.postTest()
        status("Test notification sent — check your notification shade.")
    }

    fun reschedule() = viewModelScope.launch {
        val current = settings.current()
        scheduler.scheduleBriefing(current.morningHour, current.morningMinute)
        scheduler.scheduleEvening(current.eveningHour, current.eveningMinute)
        scheduler.scheduleMaintenance()
    }

    fun saveProfile() = viewModelScope.launch {
        settings.setName(_state.value.nameInput)
        settings.setUniversity(_state.value.universityInput)
        settings.setSemester(_state.value.semesterInput)
        status("Profile updated.")
    }

    fun updateName(value: String) {
        _state.value = _state.value.copy(nameInput = value)
    }

    fun updateUniversity(value: String) {
        _state.value = _state.value.copy(universityInput = value)
    }

    fun updateSemester(value: String) {
        _state.value = _state.value.copy(semesterInput = value)
    }

    fun setDailyHours(hours: Int) = viewModelScope.launch {
        settings.setDailyHours(hours)
        status("Daily deep-work target: ${hours}h.")
    }

    fun setDeepWorkTarget(hours: Int) = viewModelScope.launch {
        settings.setDeepWorkTarget(hours)
        status("Weekly deep-work target: ${hours}h.")
    }

    fun exportBackup(uri: Uri) = viewModelScope.launch {
        val json = backup.createBackupJson()
        val ok = write(uri, json)
        status(if (ok) "Backup written — secrets were not included." else "Could not write the backup.", !ok)
    }

    fun exportCsv(uri: Uri) = viewModelScope.launch {
        val csv = backup.createTaskCsv()
        val ok = write(uri, csv)
        status(if (ok) "CSV exported." else "Could not write the CSV.", !ok)
    }

    fun importBackup(uri: Uri) = viewModelScope.launch {
        val text = withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }.getOrNull()
        }
        if (text.isNullOrBlank()) {
            status("Could not read that file.", isError = true)
            return@launch
        }
        val report = backup.restoreFromJson(text)
        repository.refreshAllProgress()
        status(report.message, isError = !report.ok)
    }

    fun loadStarterRoadmap() = viewModelScope.launch {
        val stats = repository.importRoadmap(STARTER_ROADMAP)
        status("Starter plan loaded: ${stats.goals} goals, ${stats.tasks} tasks.")
    }

    fun requestErase() {
        _state.value = _state.value.copy(confirmErase = true)
    }

    fun cancelErase() {
        _state.value = _state.value.copy(confirmErase = false)
    }

    fun confirmErase() = viewModelScope.launch {
        repository.eraseEverything()
        settings.setOnboardingDone(false)
        _state.value = _state.value.copy(confirmErase = false)
        status("All local data erased.")
    }

    fun clearStatus() {
        _state.value = _state.value.copy(status = null, statusIsError = false)
    }

    private suspend fun write(uri: Uri, content: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(content) }
            true
        }.getOrDefault(false)
    }

    private fun status(message: String, isError: Boolean = false) {
        _state.value = _state.value.copy(status = message, statusIsError = isError)
    }

    private fun mask(key: String): String = "${key.take(9)}••••••••${key.takeLast(4)}"

    companion object {
        private val STARTER_ROADMAP = """
## Phase 0 — Foundation (week 1-2, Rs 0)
- [ ] Create GitHub account and commit code daily
- [ ] Install VS Code + Git, set up a clean workspace
- [ ] Finish freeCodeCamp Responsive Web Design
- [ ] Enrol in CS50x (free) and finish week 0-3
## Phase 1 — Portfolio (week 3-4)
- [ ] Build and deploy 3 static sites on Vercel
- [ ] Write a real case study for each project
- [ ] Complete LinkedIn profile with projects
## Phase 2 — First money (week 5-8)
- [ ] Send 10 Instagram DMs to local businesses daily
- [ ] Post value content in 2 Facebook business groups weekly
- [ ] Send 20 personalised LinkedIn messages daily
- [ ] Deliver 3 free local websites as proof
## Phase 3 — Retainers (week 9-12)
- [ ] Convert 2 clients to monthly retainers
- [ ] Set up Wave invoicing and a simple contract
- [ ] Ask every happy client for a testimonial
## KPIs
| Monthly income target | 50000 | PKR |
| MRR before launch | 350000 | PKR |
| Paying clients | 3 | clients |
| Deep work per week | 25 | hours |
        """.trimIndent()
    }
}
