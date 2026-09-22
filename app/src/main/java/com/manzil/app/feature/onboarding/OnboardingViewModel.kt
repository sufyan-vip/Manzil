package com.manzil.app.feature.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manzil.app.data.prefs.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BS SE Edition Onboarding VM - collects all user info at start
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    var name by mutableStateOf("")
    var university by mutableStateOf("COMSATS Sahiwal")
    var semester by mutableStateOf("1st")
    var mainGoal by mutableStateOf("")
    var targetDate by mutableStateOf("")
    var skills by mutableStateOf("")
    var dailyHours by mutableStateOf(3)
    var morningTime by mutableStateOf("07:00")
    var eveningTime by mutableStateOf("21:30")
    var preferredPlatforms by mutableStateOf(setOf("Instagram", "LinkedIn"))

    fun togglePlatform(platform: String) {
        preferredPlatforms = if (preferredPlatforms.contains(platform)) {
            preferredPlatforms - platform
        } else {
            preferredPlatforms + platform
        }
    }

    fun saveOnboarding() {
        viewModelScope.launch {
            settingsRepo.saveOnboardingData(
                name = name,
                university = university,
                semester = semester,
                mainGoal = mainGoal,
                targetDate = targetDate,
                skills = skills,
                dailyHours = dailyHours,
                morningTime = morningTime,
                eveningTime = eveningTime,
                platforms = preferredPlatforms.toList()
            )
            settingsRepo.setOnboardingCompleted(true)
        }
    }
}
