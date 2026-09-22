package com.manzil.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "manzil_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val USER_NAME = stringPreferencesKey("user_name")
        val UNIVERSITY = stringPreferencesKey("university")
        val SEMESTER = stringPreferencesKey("semester")
        val MAIN_GOAL = stringPreferencesKey("main_goal")
        val TARGET_DATE = stringPreferencesKey("target_date")
        val SKILLS = stringPreferencesKey("skills")
        val DAILY_HOURS = intPreferencesKey("daily_hours")
        val MORNING_TIME = stringPreferencesKey("morning_time")
        val EVENING_TIME = stringPreferencesKey("evening_time")
        val PLATFORMS = stringPreferencesKey("platforms")
        val THEME = stringPreferencesKey("theme")
        val LANGUAGE = stringPreferencesKey("language")
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[Keys.ONBOARDING_DONE] ?: false }

    suspend fun setOnboardingCompleted(done: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = done }
    }

    suspend fun saveOnboardingData(
        name: String, university: String, semester: String,
        mainGoal: String, targetDate: String, skills: String,
        dailyHours: Int, morningTime: String, eveningTime: String,
        platforms: List<String>
    ) {
        context.dataStore.edit {
            it[Keys.USER_NAME] = name
            it[Keys.UNIVERSITY] = university
            it[Keys.SEMESTER] = semester
            it[Keys.MAIN_GOAL] = mainGoal
            it[Keys.TARGET_DATE] = targetDate
            it[Keys.SKILLS] = skills
            it[Keys.DAILY_HOURS] = dailyHours
            it[Keys.MORNING_TIME] = morningTime
            it[Keys.EVENING_TIME] = eveningTime
            it[Keys.PLATFORMS] = platforms.joinToString(",")
        }
    }

    suspend fun getUserName(): String? {
        return context.dataStore.data.map { it[Keys.USER_NAME] }.let {
            // Simplified for M0 - will use first() in real impl
            null
        }
    }

    fun getUserNameFlow(): Flow<String?> = context.dataStore.data.map { it[Keys.USER_NAME] }
}
