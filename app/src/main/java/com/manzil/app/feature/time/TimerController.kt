package com.manzil.app.feature.time

import com.manzil.app.core.common.TimeProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerController @Inject constructor(
    private val timeProvider: TimeProvider
) {
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _elapsedMillis = MutableStateFlow(0L)
    val elapsedMillis: StateFlow<Long> = _elapsedMillis

    private var startTime: Long? = null
    var currentTaskId: String? = null
    var currentLabel: String = ""

    fun start(taskId: String? = null, label: String = "Deep work") {
        currentTaskId = taskId
        currentLabel = label
        startTime = timeProvider.nowMillis()
        _isRunning.value = true
    }

    fun pause() {
        startTime?.let { start ->
            val elapsed = timeProvider.nowMillis() - start
            _elapsedMillis.value += elapsed
        }
        startTime = null
        _isRunning.value = false
    }

    fun stop(): Long {
        pause()
        val total = _elapsedMillis.value
        _elapsedMillis.value = 0L
        currentTaskId = null
        return total
    }

    fun getCurrentElapsed(): Long {
        val base = _elapsedMillis.value
        val current = startTime?.let { timeProvider.nowMillis() - it } ?: 0L
        return base + current
    }
}
