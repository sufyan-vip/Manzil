package com.manzil.app.core.common

data class ImportStats(
    val goals: Int = 0,
    val tasks: Int = 0,
    val kpis: Int = 0
) {
    val isEmpty: Boolean get() = goals == 0 && tasks == 0 && kpis == 0
}
