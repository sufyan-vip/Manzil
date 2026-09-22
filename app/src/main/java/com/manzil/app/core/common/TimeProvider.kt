package com.manzil.app.core.common

import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeProvider @Inject constructor() {
    fun nowMillis(): Long = System.currentTimeMillis()
    fun today(): LocalDate = LocalDate.now()
    fun nowDateTime(): LocalDateTime = LocalDateTime.now()
}
