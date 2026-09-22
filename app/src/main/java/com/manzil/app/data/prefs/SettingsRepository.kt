package com.manzil.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.manzil.app.core.theme.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

internal val Context.manzilDataStore: DataStore<Preferences> by preferencesDataStore(name = "manzil_settings")

data class AppSettings(
    val onboardingDone: Boolean = false,
    val name: String = "",
    val university: String = "",
    val semester: String = "",
    val skills: String = "",
    val mainGoal: String = "",
    val targetDate: String = "",
    val dailyHours: Int = 3,
    val platforms: List<String> = emptyList(),
    val morningTime: String = "07:00",
    val eveningTime: String = "21:30",
    val notificationsEnabled: Boolean = true,
    val aiBriefingEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: String = "en",
    val dynamicColor: Boolean = false,
    val deepWorkTargetHours: Int = 25,
    val recentSearches: List<String> = emptyList(),
    val lastRolloverDate: String? = null,
    val seededAt: Long? = null
) {
    val displayName: String get() = name.ifBlank { "friend" }
    val morningHour: Int get() = morningTime.substringBefore(':').toIntOrNull() ?: 7
    val morningMinute: Int get() = morningTime.substringAfter(':', "0").toIntOrNull() ?: 0
    val eveningHour: Int get() = eveningTime.substringBefore(':').toIntOrNull() ?: 21
    val eveningMinute: Int get() = eveningTime.substringAfter(':', "30").toIntOrNull() ?: 30
}

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val NAME = stringPreferencesKey("user_name")
        val UNIVERSITY = stringPreferencesKey("university")
        val SEMESTER = stringPreferencesKey("semester")
        val SKILLS = stringPreferencesKey("skills")
        val MAIN_GOAL = stringPreferencesKey("main_goal")
        val TARGET_DATE = stringPreferencesKey("target_date")
        val DAILY_HOURS = intPreferencesKey("daily_hours")
        val PLATFORMS = stringPreferencesKey("platforms")
        val MORNING = stringPreferencesKey("morning_time")
        val EVENING = stringPreferencesKey("evening_time")
        val NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        val AI_BRIEFING = booleanPreferencesKey("ai_briefing_enabled")
        val THEME = stringPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val DEEP_WORK_HOURS = intPreferencesKey("deep_work_hours")
        val RECENT_SEARCHES = stringPreferencesKey("recent_searches")
        val LAST_ROLLOVER = stringPreferencesKey("last_rollover")
        val SEEDED_AT = stringPreferencesKey("seeded_at")
    }

    val settings: Flow<AppSettings> = context.manzilDataStore.data.map { prefs ->
        AppSettings(
            onboardingDone = prefs[Keys.ONBOARDING_DONE] ?: false,
            name = prefs[Keys.NAME].orEmpty(),
            university = prefs[Keys.UNIVERSITY].orEmpty(),
            semester = prefs[Keys.SEMESTER].orEmpty(),
            skills = prefs[Keys.SKILLS].orEmpty(),
            mainGoal = prefs[Keys.MAIN_GOAL].orEmpty(),
            targetDate = prefs[Keys.TARGET_DATE].orEmpty(),
            dailyHours = prefs[Keys.DAILY_HOURS] ?: 3,
            platforms = prefs[Keys.PLATFORMS].orEmpty().split(",").filter { it.isNotBlank() },
            morningTime = prefs[Keys.MORNING] ?: "07:00",
            eveningTime = prefs[Keys.EVENING] ?: "21:30",
            notificationsEnabled = prefs[Keys.NOTIFICATIONS] ?: true,
            aiBriefingEnabled = prefs[Keys.AI_BRIEFING] ?: true,
            themeMode = runCatching { ThemeMode.valueOf(prefs[Keys.THEME] ?: "SYSTEM") }.getOrDefault(ThemeMode.SYSTEM),
            language = prefs[Keys.LANGUAGE] ?: "en",
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: false,
            deepWorkTargetHours = prefs[Keys.DEEP_WORK_HOURS] ?: 25,
            recentSearches = prefs[Keys.RECENT_SEARCHES].orEmpty().split("\n").filter { it.isNotBlank() },
            lastRolloverDate = prefs[Keys.LAST_ROLLOVER],
            seededAt = prefs[Keys.SEEDED_AT]?.toLongOrNull()
        )
    }

    suspend fun current(): AppSettings = settings.first()

    suspend fun setOnboardingDone(done: Boolean) = edit { it[Keys.ONBOARDING_DONE] = done }

    suspend fun saveOnboarding(
        name: String,
        university: String,
        semester: String,
        skills: String,
        mainGoal: String,
        targetDate: String,
        dailyHours: Int,
        morningTime: String,
        eveningTime: String,
        platforms: List<String>
    ) = edit { prefs ->
        prefs[Keys.NAME] = name.trim()
        prefs[Keys.UNIVERSITY] = university.trim()
        prefs[Keys.SEMESTER] = semester.trim()
        prefs[Keys.SKILLS] = skills.trim()
        prefs[Keys.MAIN_GOAL] = mainGoal.trim()
        prefs[Keys.TARGET_DATE] = targetDate.trim()
        prefs[Keys.DAILY_HOURS] = dailyHours
        prefs[Keys.MORNING] = morningTime
        prefs[Keys.EVENING] = eveningTime
        prefs[Keys.PLATFORMS] = platforms.joinToString(",")
        prefs[Keys.ONBOARDING_DONE] = true
    }

    suspend fun setName(name: String) = edit { it[Keys.NAME] = name.trim() }

    suspend fun setUniversity(value: String) = edit { it[Keys.UNIVERSITY] = value.trim() }

    suspend fun setSemester(value: String) = edit { it[Keys.SEMESTER] = value.trim() }

    suspend fun setDailyHours(hours: Int) = edit { it[Keys.DAILY_HOURS] = hours.coerceIn(1, 16) }

    suspend fun setDeepWorkTarget(hours: Int) = edit { it[Keys.DEEP_WORK_HOURS] = hours.coerceIn(1, 80) }

    suspend fun setThemeMode(mode: ThemeMode) = edit { it[Keys.THEME] = mode.name }

    suspend fun setLanguage(language: String) = edit { it[Keys.LANGUAGE] = language }

    suspend fun setDynamicColor(enabled: Boolean) = edit { it[Keys.DYNAMIC_COLOR] = enabled }

    suspend fun setNotificationsEnabled(enabled: Boolean) = edit { it[Keys.NOTIFICATIONS] = enabled }

    suspend fun setAiBriefingEnabled(enabled: Boolean) = edit { it[Keys.AI_BRIEFING] = enabled }

    suspend fun setNotificationTimes(morning: String, evening: String) = edit {
        it[Keys.MORNING] = morning
        it[Keys.EVENING] = evening
    }

    suspend fun pushRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return
        edit { prefs ->
            val existing = prefs[Keys.RECENT_SEARCHES].orEmpty().split("\n").filter { it.isNotBlank() }
            val next = (listOf(trimmed) + existing.filterNot { it.equals(trimmed, ignoreCase = true) }).take(10)
            prefs[Keys.RECENT_SEARCHES] = next.joinToString("\n")
        }
    }

    suspend fun clearRecentSearches() = edit { it.remove(Keys.RECENT_SEARCHES) }

    suspend fun setLastRollover(date: LocalDate) = edit { it[Keys.LAST_ROLLOVER] = date.toString() }

    suspend fun markSeeded() = edit { it[Keys.SEEDED_AT] = System.currentTimeMillis().toString() }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.manzilDataStore.edit(block)
    }
}
