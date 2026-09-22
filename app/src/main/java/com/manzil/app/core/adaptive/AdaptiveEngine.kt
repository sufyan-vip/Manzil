package com.manzil.app.core.adaptive

import com.manzil.app.data.local.entity.Task
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The offline half of the adaptive engine. It always works — no API key, no internet.
 * The AI half (OpenRouter) only sharpens what this already decides, never gates it.
 *
 * User's rules encoded here:
 *  - task na ho to kal ke sath adjust karo (auto rollover with a written reason)
 *  - goal tab tak chale jab tak DONE na ho (perpetual, never a fixed year)
 *  - latest info se tasks update ho (weekly refresh nudge)
 *  - client hunting through Instagram / Facebook / LinkedIn / X / Reddit / Discord
 */
@Singleton
class AdaptiveEngine @Inject constructor() {

    data class TaskCondition(
        val taskId: String,
        val status: String,
        val dueDate: String,
        val autoRolledCount: Int,
        val isOverdue: Boolean
    )

    data class AdaptiveDecision(
        val newDueDate: String,
        val reason: String,
        val priorityAdjustment: Int,
        val shouldSplit: Boolean,
        val clientPlatformSuggestion: String?
    )

    fun evaluateTask(condition: TaskCondition): AdaptiveDecision = when {
        condition.isOverdue && condition.autoRolledCount < 3 -> AdaptiveDecision(
            newDueDate = "tomorrow",
            reason = "Task missed yesterday — moving to tomorrow because you had ${condition.autoRolledCount} rolls. Let's do it first thing 09:00.",
            priorityAdjustment = -1,
            shouldSplit = condition.autoRolledCount >= 2,
            clientPlatformSuggestion = null
        )
        condition.status == "BLOCKED" -> AdaptiveDecision(
            newDueDate = "tomorrow",
            reason = "Blocked — AI will generate 3 unblock actions",
            priorityAdjustment = 0,
            shouldSplit = true,
            clientPlatformSuggestion = "LinkedIn"
        )
        else -> AdaptiveDecision(
            newDueDate = condition.dueDate,
            reason = "On track",
            priorityAdjustment = 0,
            shouldSplit = false,
            clientPlatformSuggestion = null
        )
    }

    fun generateNextTasks(
        currentProgress: Int,
        mainGoal: String,
        skills: List<String>,
        preferredPlatforms: List<String>
    ): List<String> {
        val skill = skills.firstOrNull()?.trim()?.ifBlank { null } ?: "React"
        val platform = preferredPlatforms.firstOrNull()?.trim()?.ifBlank { null } ?: "Instagram"
        return listOf(
            "Deep work 2h on the next $skill milestone",
            "Client hunt via $platform: 10 personalised messages with a value-first script",
            "Push towards $mainGoal: ship one visible deliverable today"
        )
    }

    fun shouldUpdateWithLatestInfo(lastUpdateDaysAgo: Int): Boolean = lastUpdateDaysAgo >= 7

    /** Ordered plan for today, built only from local data. */
    fun rankForToday(tasks: List<Task>): List<Task> = tasks
        .filter { it.status.name != "DONE" && it.status.name != "CANCELLED" }
        .sortedWith(
            compareBy(
                { it.dueTime ?: java.time.LocalTime.MAX },
                { it.priority },
                { -it.autoRolledCount }
            )
        )

    /** Reason shown on a task after it was rolled to today. */
    fun rolloverReason(daysLate: Int, rolledCount: Int): String = when {
        daysLate <= 1 && rolledCount <= 1 -> "Kal reh gaya — aaj pehle isko karo."
        daysLate <= 1 -> "Ye task $rolledCount baar aage aa raha hai. Chhota karo ya drop karo."
        else -> "$daysLate din se pending hai. Aaj sabse pehle isko 30 minute do."
    }

    data class ClientAction(
        val platform: String,
        val searchQuery: String,
        val script: String,
        val followUp: String,
        val expected: String
    )

    /** One concrete, modern client-hunting action per day — never Fiverr grind. */
    fun clientActionOfTheDay(platforms: List<String>, skills: String, dayIndex: Int): ClientAction {
        val available = platforms.map { it.trim() }.filter { it.isNotBlank() }
            .ifEmpty { listOf("Instagram", "LinkedIn", "Facebook", "X", "Reddit", "Discord") }
        return when (available[dayIndex % available.size].lowercase()) {
            "instagram" -> ClientAction(
                platform = "Instagram",
                searchQuery = "#smallbusiness #sahiwalbusiness + city hashtags",
                script = "Salam! Main Sufyan, software engineering student. Aapke page ke liye ek chhota website concept banaya hai — free mockup bhej doon? Koi charge nahi, pasand aaye to baat karenge.",
                followUp = "48 hours later: \"Salam! Mockup dekha? Koi change chahiye to bata dein.\"",
                expected = "10 DMs → 2 replies → 1 discovery call in 7 days"
            )
            "linkedin" -> ClientAction(
                platform = "LinkedIn",
                searchQuery = "Founder / Owner + \"Sahiwal\" / \"Pakistan\" small business",
                script = "Aapka kaam dekha — ye specific problem fix ho sakti hai with a simple web app. Main ek 2-minute Loom walk-through bhej doon?",
                followUp = "3 din baad case study link + 1 concrete idea.",
                expected = "20 targeted messages → 3 conversations / week"
            )
            "facebook" -> ClientAction(
                platform = "Facebook groups",
                searchQuery = "Sahiwal Business, Freelancers Pakistan, Local Business Owners",
                script = "Free value: 3 ways local businesses are losing customers online right now (screenshot proof). Comment below your business, main free audit de dunga.",
                followUp = "DM everyone who engages within 24 hours.",
                expected = "1 free audit per day → 2 clients in 14 days"
            )
            "x", "twitter" -> ClientAction(
                platform = "X / Twitter",
                searchQuery = "#buildinpublic, #100DaysOfCode",
                script = "Day X of building a software house from Sahiwal. Today: <what you built>. Learning in public, shipping daily.",
                followUp = "Reply to 5 other builders with something useful.",
                expected = "Inbound DMs + credibility for later clients"
            )
            "reddit" -> ClientAction(
                platform = "Reddit",
                searchQuery = "r/forhire, r/developersIndia, r/PakistaniTech",
                script = "I built <thing> for a local shop and it improved X. Happy to share the playbook — comment if useful.",
                followUp = "Answer 3 questions helpfully, then link your portfolio.",
                expected = "Category authority + referrals"
            )
            else -> ClientAction(
                platform = "Discord / WhatsApp communities",
                searchQuery = "Local dev + business owner communities",
                script = "Sharing a quick win: <case study>. Need a website or automation? First audit free this week.",
                followUp = "Post weekly progress, never spam daily.",
                expected = "Warm leads with zero ad spend"
            )
        }
    }

    /** Weekly "latest info" nudge tasks so the plan doesn't go stale. */
    fun latestInfoTasks(skills: String): List<String> {
        val focus = skills.split(",").map { it.trim() }.filter { it.isNotBlank() }.firstOrNull() ?: "React"
        return listOf(
            "Check what changed in $focus this week (release notes / changelog)",
            "Update one task to the newest approach you found",
            "Log the change in Journal so the plan stays current"
        )
    }
}
