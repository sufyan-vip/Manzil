package com.manzil.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The focus timer is persisted as timestamps, not as a running thread: the app can be
 * killed and reopened and the elapsed time is still correct. No foreground service needed.
 */
data class FocusTimerState(
    val isRunning: Boolean = false,
    val taskId: String? = null,
    val goalId: String? = null,
    val label: String = "",
    val startedAtMillis: Long = 0L,
    val accumulatedMillis: Long = 0L
) {
    fun elapsed(now: Long = System.currentTimeMillis()): Long =
        if (isRunning) accumulatedMillis + (now - startedAtMillis).coerceAtLeast(0L) else accumulatedMillis

    val isIdle: Boolean get() = !isRunning && accumulatedMillis == 0L && label.isBlank()
}

@Singleton
class FocusTimerStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val RUNNING = booleanPreferencesKey("timer_running")
        val TASK_ID = stringPreferencesKey("timer_task_id")
        val GOAL_ID = stringPreferencesKey("timer_goal_id")
        val LABEL = stringPreferencesKey("timer_label")
        val STARTED = longPreferencesKey("timer_started_at")
        val ACCUMULATED = longPreferencesKey("timer_accumulated")
    }

    val state: Flow<FocusTimerState> = context.manzilDataStore.data.map { prefs ->
        FocusTimerState(
            isRunning = prefs[Keys.RUNNING] ?: false,
            taskId = prefs[Keys.TASK_ID],
            goalId = prefs[Keys.GOAL_ID],
            label = prefs[Keys.LABEL].orEmpty(),
            startedAtMillis = prefs[Keys.STARTED] ?: 0L,
            accumulatedMillis = prefs[Keys.ACCUMULATED] ?: 0L
        )
    }

    suspend fun current(): FocusTimerState = state.first()

    suspend fun start(taskId: String?, goalId: String?, label: String) = edit { prefs ->
        prefs[Keys.RUNNING] = true
        prefs[Keys.TASK_ID] = taskId ?: ""
        prefs[Keys.GOAL_ID] = goalId ?: ""
        prefs[Keys.LABEL] = label
        prefs[Keys.STARTED] = System.currentTimeMillis()
    }

    suspend fun pause() {
        val now = System.currentTimeMillis()
        val snapshot = current()
        edit { prefs ->
            prefs[Keys.RUNNING] = false
            prefs[Keys.ACCUMULATED] = snapshot.elapsed(now)
        }
    }

    suspend fun resume() = edit { prefs ->
        prefs[Keys.RUNNING] = true
        prefs[Keys.STARTED] = System.currentTimeMillis()
    }

    suspend fun reset() = edit { prefs ->
        prefs[Keys.RUNNING] = false
        prefs.remove(Keys.TASK_ID)
        prefs.remove(Keys.GOAL_ID)
        prefs[Keys.LABEL] = ""
        prefs[Keys.STARTED] = 0L
        prefs[Keys.ACCUMULATED] = 0L
    }

    suspend fun setLabel(label: String) = edit { it[Keys.LABEL] = label }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.manzilDataStore.edit(block)
    }
}
