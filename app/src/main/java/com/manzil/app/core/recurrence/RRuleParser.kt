package com.manzil.app.core.recurrence

object RRuleParser {
    fun parse(rule: String): RRule {
        // Simplified parser for FREQ=WEEKLY;BYDAY=MO,WE,FR etc.
        val parts = rule.split(";").associate {
            val (k,v) = it.split("=")
            k to v
        }
        val freq = Freq.valueOf(parts["FREQ"] ?: "DAILY")
        val interval = parts["INTERVAL"]?.toIntOrNull() ?: 1
        val byDay = parts["BYDAY"]?.split(",") ?: emptyList()
        val byMonthDay = parts["BYMONTHDAY"]?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
        val count = parts["COUNT"]?.toIntOrNull()
        val until = parts["UNTIL"]
        return RRule(freq, interval, byDay, byMonthDay, count, until)
    }
}
