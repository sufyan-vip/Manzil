package com.manzil.app.data.local

import com.manzil.app.core.common.Fmt
import com.manzil.app.data.local.entity.CalendarEvent
import com.manzil.app.data.local.entity.EventSource
import com.manzil.app.data.local.entity.Goal
import com.manzil.app.data.local.entity.GoalCategory
import com.manzil.app.data.local.entity.Habit
import com.manzil.app.data.local.entity.KpiSnapshot
import com.manzil.app.data.local.entity.Milestone
import com.manzil.app.data.local.entity.Task
import com.manzil.app.data.local.entity.TaskStatus
import java.time.LocalDate
import java.time.LocalTime

/**
 * Everything a brand new install needs so the first screen is never empty:
 * KPI targets from the plan, four keystone habits, the root goal with sub-goals,
 * milestones and a first set of tasks for today.
 */
object SeedData {

    data class StarterContent(
        val goals: List<Goal>,
        val milestones: List<Milestone>,
        val tasks: List<Task>,
        val kpis: List<KpiSnapshot>,
        val habits: List<Habit>,
        val events: List<CalendarEvent>,
        val rootGoalId: String
    )

    const val ROOT_GOAL_ID = "goal_root_software_house"

    fun starter(): StarterContent {
        val today = LocalDate.now()
        val now = System.currentTimeMillis()
        val start = today.minusDays(1)

        val goals = listOf(
            Goal(
                id = ROOT_GOAL_ID,
                title = "Software House — Sahiwal",
                description = "Zero se start: skills, portfolio, first paying clients, then a team and a registered software house.",
                category = GoalCategory.BUSINESS,
                priority = 1,
                startDate = start,
                targetDate = today.plusYears(3),
                metricLabel = "Monthly revenue",
                metricTarget = 350000.0,
                metricCurrent = 0.0,
                metricUnit = "PKR",
                sortOrder = 0,
                createdAt = now,
                updatedAt = now,
                isPerpetual = true
            ),
            Goal(
                id = "goal_skill",
                title = "Job-ready full-stack skills",
                description = "React + Node + PostgreSQL, shipped in public.",
                category = GoalCategory.SKILL,
                parentGoalId = ROOT_GOAL_ID,
                priority = 1,
                startDate = start,
                targetDate = today.plusMonths(6),
                sortOrder = 1,
                createdAt = now + 1,
                updatedAt = now + 1
            ),
            Goal(
                id = "goal_portfolio",
                title = "Portfolio that sells",
                description = "5 real projects deployed on Vercel, documented on GitHub.",
                category = GoalCategory.SKILL,
                parentGoalId = ROOT_GOAL_ID,
                priority = 2,
                startDate = start,
                targetDate = today.plusMonths(9),
                sortOrder = 2,
                createdAt = now + 2,
                updatedAt = now + 2
            ),
            Goal(
                id = "goal_clients",
                title = "First 3 paying clients",
                description = "Direct outreach on Instagram, Facebook groups and LinkedIn — no Fiverr fees.",
                category = GoalCategory.CLIENT,
                parentGoalId = ROOT_GOAL_ID,
                priority = 1,
                startDate = start,
                targetDate = today.plusMonths(4),
                metricLabel = "Paying clients",
                metricTarget = 3.0,
                metricCurrent = 0.0,
                metricUnit = "clients",
                sortOrder = 3,
                createdAt = now + 3,
                updatedAt = now + 3
            ),
            Goal(
                id = "goal_income",
                title = "Rs 50,000 / month freelance income",
                description = "Retainers beat one-off gigs. Convert every good client.",
                category = GoalCategory.MONEY,
                parentGoalId = ROOT_GOAL_ID,
                priority = 2,
                startDate = start,
                targetDate = today.plusMonths(12),
                metricLabel = "Monthly income",
                metricTarget = 50000.0,
                metricCurrent = 0.0,
                metricUnit = "PKR",
                sortOrder = 4,
                createdAt = now + 4,
                updatedAt = now + 4
            ),
            Goal(
                id = "goal_degree",
                title = "CGPA 3.0+ in BS Software Engineering",
                description = "Degree and business grow together — exams are non-negotiable weeks.",
                category = GoalCategory.EDUCATION,
                parentGoalId = ROOT_GOAL_ID,
                priority = 1,
                startDate = start,
                targetDate = today.plusYears(3),
                metricLabel = "CGPA",
                metricTarget = 3.0,
                metricCurrent = 0.0,
                sortOrder = 5,
                createdAt = now + 5,
                updatedAt = now + 5
            )
        )

        val milestones = listOf(
            milestone("ms_react", "goal_skill", "React + hooks solid (CS50 week 3 done)", today.plusDays(21), 0, now),
            milestone("ms_node", "goal_skill", "Node + REST API + PostgreSQL CRUD", today.plusDays(60), 1, now),
            milestone("ms_portfolio", "goal_portfolio", "3 projects live on Vercel", today.plusDays(45), 0, now),
            milestone("ms_github", "goal_portfolio", "GitHub streak 100 days", today.plusDays(100), 1, now),
            milestone("ms_lead", "goal_clients", "First discovery call booked", today.plusDays(14), 0, now),
            milestone("ms_client1", "goal_clients", "Client 1 delivered + testimonial", today.plusDays(45), 1, now),
            milestone("ms_client3", "goal_clients", "3 clients with public testimonials", today.plusMonths(4), 2, now),
            milestone("ms_income25", "goal_income", "First Rs 25,000 month", today.plusMonths(5), 0, now)
        )

        val tasks = listOf(
            task("task_react", "React: useEffect + data fetching (2h deep work)", today, LocalTime.of(9, 0), 1, "goal_skill", 120, now),
            task("task_proposals", "Send 10 outreach messages — Instagram small businesses", today, LocalTime.of(14, 0), 1, "goal_clients", 60, now),
            task("task_followup", "Follow up 3 old leads on WhatsApp Business", today, LocalTime.of(16, 30), 2, "goal_clients", 30, now),
            task("task_github", "GitHub commit + build-in-public post", today, LocalTime.of(18, 0), 2, "goal_portfolio", 30, now),
            task("task_fyp", "FYP: write chapter 2 outline", today, LocalTime.of(20, 0), 2, "goal_degree", 90, now),
            task("task_english", "English practice: 15 minutes speaking out loud", today, LocalTime.of(21, 0), 3, "goal_skill", 15, now),

            task("task_tomorrow1", "Read 10 pages of Atomic Habits", today.plusDays(1), LocalTime.of(8, 0), 3, "goal_skill", 25, now),
            task("task_tomorrow2", "Design portfolio hero section in Figma", today.plusDays(1), LocalTime.of(11, 0), 2, "goal_portfolio", 60, now),
            task("task_tomorrow3", "Record a 30 second reel about what you built", today.plusDays(1), LocalTime.of(19, 0), 2, "goal_clients", 45, now),
            task("task_tomorrow4", "Update Upwork + LinkedIn profile with new project", today.plusDays(1), null, 3, "goal_clients", 30, now),

            task("task_week1", "Ship project 1: personal portfolio site live on Vercel", today.plusDays(3), null, 1, "goal_portfolio", 180, now),
            task("task_week2", "Write the case study for client 1 delivery", today.plusDays(5), null, 2, "goal_clients", 120, now)
        )

        val habits = listOf(
            Habit(id = "habit_code", name = "Code daily", icon = "01", colorArgb = 0xFF0F766E.toInt(), targetPerWeek = 7, active = true, createdAt = now),
            Habit(id = "habit_english", name = "English practice", icon = "02", colorArgb = 0xFF2563EB.toInt(), targetPerWeek = 7, active = true, createdAt = now + 1),
            Habit(id = "habit_proposals", name = "Send proposals", icon = "03", colorArgb = 0xFFD97706.toInt(), targetPerWeek = 5, active = true, createdAt = now + 2),
            Habit(id = "habit_github", name = "GitHub commit", icon = "04", colorArgb = 0xFF16A34A.toInt(), targetPerWeek = 6, active = true, createdAt = now + 3)
        )

        val events = listOf(
            CalendarEvent(
                id = "event_deepwork",
                title = "Deep work block",
                description = "Phone on silent, one task only.",
                startAt = Fmt.millisAt(today, LocalTime.of(9, 0)),
                endAt = Fmt.millisAt(today, LocalTime.of(11, 0)),
                source = EventSource.LOCAL,
                createdAt = now,
                updatedAt = now
            ),
            CalendarEvent(
                id = "event_class",
                title = "University classes",
                description = "BS Software Engineering — semester timetable",
                startAt = Fmt.millisAt(today, LocalTime.of(12, 0)),
                endAt = Fmt.millisAt(today, LocalTime.of(16, 0)),
                source = EventSource.BSSE_CLASS,
                createdAt = now,
                updatedAt = now
            )
        )

        return StarterContent(
            goals = goals,
            milestones = milestones,
            tasks = tasks,
            kpis = getDefaultKpis(),
            habits = habits,
            events = events,
            rootGoalId = ROOT_GOAL_ID
        )
    }

    private fun milestone(
        id: String,
        goalId: String,
        title: String,
        due: LocalDate,
        order: Int,
        now: Long
    ) = Milestone(id = id, goalId = goalId, title = title, dueDate = due, sortOrder = order, createdAt = now + order)

    private fun task(
        id: String,
        title: String,
        date: LocalDate,
        time: LocalTime?,
        priority: Int,
        goalId: String,
        estimate: Int,
        now: Long
    ) = Task(
        id = id,
        title = title,
        dueDate = date,
        dueTime = time,
        priority = priority,
        goalId = goalId,
        estimatedMinutes = estimate,
        createdAt = now,
        updatedAt = now,
        status = TaskStatus.TODO
    )

    /** KPI targets straight from the plan (Sahiwal software house roadmap). */
    fun getDefaultKpis(): List<KpiSnapshot> {
        val today = LocalDate.now()
        val now = System.currentTimeMillis()
        fun kpi(id: String, key: String, start: Double, note: String) =
            KpiSnapshot(id = id, key = key, value = start, date = today, note = note, createdAt = now)
        return listOf(
            kpi("kpi_savings", "savings_pkr", 0.0, "Savings at graduation — target 55,00,000"),
            kpi("kpi_capital", "capital_needed", 5978000.0, "Launch capital needed — 59,78,000"),
            kpi("kpi_income_y1", "income_y1", 0.0, "Freelance income by end of year 1 — 50k/month"),
            kpi("kpi_income_y2", "income_y2", 0.0, "Freelance income by end of year 2 — 120k/month"),
            kpi("kpi_mrr", "mrr_pkr", 0.0, "MRR before launch — 3,50,000/month"),
            kpi("kpi_clients_intl", "clients_intl", 0.0, "International retainer clients — 3"),
            kpi("kpi_clients_local", "clients_local", 0.0, "Local AMC clients — 10"),
            kpi("kpi_team", "team_size", 0.0, "Team size at launch — 5"),
            kpi("kpi_github", "github_projects", 0.0, "GitHub projects shipped — 20"),
            kpi("kpi_upwork", "upwork_reviews", 0.0, "Upwork reviews — 5"),
            kpi("kpi_cgpa", "cgpa", 0.0, "CGPA floor — 3.0"),
            kpi("kpi_deepwork", "deepwork_hours", 0.0, "Deep work hours per week — 25")
        )
    }

    fun kpiLabel(key: String): String = when (key) {
        "savings_pkr" -> "Savings"
        "capital_needed" -> "Capital needed"
        "income_y1" -> "Income (year 1)"
        "income_y2" -> "Income (year 2)"
        "mrr_pkr" -> "MRR"
        "clients_intl" -> "Intl clients"
        "clients_local" -> "Local clients"
        "team_size" -> "Team size"
        "github_projects" -> "Projects shipped"
        "upwork_reviews" -> "Upwork reviews"
        "cgpa" -> "CGPA"
        "deepwork_hours" -> "Deep work / week"
        else -> key.replace('_', ' ')
    }

    fun kpiUnit(key: String): String? = when (key) {
        "savings_pkr", "capital_needed", "income_y1", "income_y2", "mrr_pkr" -> "PKR"
        "cgpa" -> null
        "deepwork_hours" -> "h"
        else -> null
    }

    fun kpiTarget(key: String): Double = when (key) {
        "savings_pkr" -> 5500000.0
        "capital_needed" -> 5978000.0
        "income_y1" -> 50000.0
        "income_y2" -> 120000.0
        "mrr_pkr" -> 350000.0
        "clients_intl" -> 3.0
        "clients_local" -> 10.0
        "team_size" -> 5.0
        "github_projects" -> 20.0
        "upwork_reviews" -> 5.0
        "cgpa" -> 3.0
        "deepwork_hours" -> 25.0
        else -> 100.0
    }
}
