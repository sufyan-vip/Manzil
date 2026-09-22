package com.manzil.app.core.recurrence

enum class Freq { DAILY, WEEKLY, MONTHLY, YEARLY }

data class RRule(
    val freq: Freq,
    val interval: Int = 1,
    val byDay: List<String> = emptyList(),
    val byMonthDay: List<Int> = emptyList(),
    val count: Int? = null,
    val until: String? = null
)
