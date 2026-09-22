package com.manzil.app.notification

/**
 * Builds EN/UR notification text — the three blocks always appear in order:
 * karna hai / ho gaya / reh gaya. Local only: a notification must never be empty
 * because the network failed.
 */
class BriefingBuilder @javax.inject.Inject constructor() {
    fun buildMorningBriefing(
        dayCounter: Int,
        todayTasks: List<String>,
        yesterdayDone: Int,
        yesterdayPlanned: Int,
        pending: List<String>,
        goalMove: String?,
        streak: Int,
        rootProgress: Int,
        language: String = "en"
    ): String {
        val donePercent = if (yesterdayPlanned > 0) (yesterdayDone * 100 / yesterdayPlanned) else 0
        return if (language == "ur") {
            """
Subah bakhair — plan ka din $dayCounter

AAJ KARNA HAI (${todayTasks.size})
${todayTasks.joinToString("\n") { " • $it" }}

KAL HO GAYA ($yesterdayDone/$yesterdayPlanned) — $donePercent%
Abhi baqi: "${pending.firstOrNull() ?: "Kuch nahi"}"

${goalMove?.let { "GOAL UPDATE: $it" } ?: ""}
Streak: $streak din 🔥   ·   Root goal: $rootProgress%
            """.trimIndent()
        } else {
            """
Good morning — Day $dayCounter of your plan

DO TODAY (${todayTasks.size})
${todayTasks.joinToString("\n") { " • $it" }}

DONE YESTERDAY ($yesterdayDone/$yesterdayPlanned) — $donePercent%
Still pending: "${pending.firstOrNull() ?: "Nothing"}"

${goalMove?.let { "GOAL MOVE: $it" } ?: ""}
Streak: $streak days 🔥   ·   Root goal: $rootProgress%
            """.trimIndent()
        }
    }

    fun buildEveningReview(done: Int, planned: Int, pending: List<String>, focusMinutes: Int, avgMinutes: Int, nextTask: String): String {
        val percent = if (planned > 0) done * 100 / planned else 0
        return """
Aaj ka hisaab — $done/$planned done ($percent%)
Reh gaya: "${pending.joinToString(", ")}"
Focus time: ${focusMinutes/60}h ${focusMinutes%60}m (7-day avg ${avgMinutes/60}h ${avgMinutes%60}m) ✅
Kal ka pehla kaam: $nextTask
        """.trimIndent()
    }
}
