package com.manzil.app.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manzil.app.data.prefs.SettingsRepository
import com.manzil.app.domain.usecase.GetTodayBriefing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository,
    private val getTodayBriefing: GetTodayBriefing
) : ViewModel() {

    var userName: String = "Sufyan"
    var dayCounter: Int = 412
    var semester: String = "1st"
    var university: String = "COMSATS Sahiwal"

    private val _briefing = MutableStateFlow("Loading briefing...")
    val briefing: StateFlow<String> = _briefing

    init {
        viewModelScope.launch {
            // Load user info - simplified for M0
            // settingsRepo.getUserNameFlow().collect { it?.let { name -> userName = name } }
        }
    }

    fun regenerateWithAI() {
        viewModelScope.launch {
            // AI Adaptive Engine will regenerate briefing based on task condition
            _briefing.value = "AI is re-arranging tasks based on your current condition..."
        }
    }
}
