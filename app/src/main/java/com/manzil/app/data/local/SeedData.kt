package com.manzil.app.data.local

import com.manzil.app.data.local.entity.*
import java.time.LocalDate

object SeedData {
    fun getDefaultKpis(): List<KpiSnapshot> {
        val today = LocalDate.now()
        val now = System.currentTimeMillis()
        return listOf(
            KpiSnapshot(id = "kpi_savings", key = "savings_pkr", value = 0.0, date = today, note = "Savings at graduation target 55L", createdAt = now),
            KpiSnapshot(id = "kpi_capital", key = "capital_needed", value = 5978000.0, date = today, note = "Launch business capital needed", createdAt = now),
            KpiSnapshot(id = "kpi_income_y1", key = "income_y1", value = 0.0, date = today, note = "Freelance income by end Year 1 - 50k/mo", createdAt = now),
            KpiSnapshot(id = "kpi_income_y2", key = "income_y2", value = 0.0, date = today, note = "Freelance income by end Year 2 - 120k/mo", createdAt = now),
            KpiSnapshot(id = "kpi_mrr", key = "mrr_pkr", value = 0.0, date = today, note = "MRR before launch 3.5L/mo", createdAt = now),
            KpiSnapshot(id = "kpi_clients_intl", key = "clients_intl", value = 0.0, date = today, note = "International retainer clients target 3", createdAt = now),
            KpiSnapshot(id = "kpi_clients_local", key = "clients_local", value = 0.0, date = today, note = "Local AMC clients target 10", createdAt = now),
            KpiSnapshot(id = "kpi_team", key = "team_size", value = 0.0, date = today, note = "Team size at launch 5", createdAt = now),
            KpiSnapshot(id = "kpi_github", key = "github_projects", value = 0.0, date = today, note = "GitHub projects shipped 20", createdAt = now),
            KpiSnapshot(id = "kpi_upwork", key = "upwork_reviews", value = 0.0, date = today, note = "Upwork reviews 5", createdAt = now),
            KpiSnapshot(id = "kpi_cgpa", key = "cgpa", value = 0.0, date = today, note = "CGPA floor 3.0", createdAt = now),
            KpiSnapshot(id = "kpi_deepwork", key = "deepwork_hours", value = 0.0, date = today, note = "Deep-work hours per week 25", createdAt = now)
        )
    }

    fun getDefaultHabits(): List<Habit> {
        val now = System.currentTimeMillis()
        return listOf(
            Habit(id = "habit_code", name = "Code Daily", icon = "💻", colorArgb = 0xFF0F766E.toInt(), targetPerWeek = 7, active = true, createdAt = now),
            Habit(id = "habit_english", name = "English Practice", icon = "🗣️", colorArgb = 0xFF2563EB.toInt(), targetPerWeek = 7, active = true, createdAt = now),
            Habit(id = "habit_proposals", name = "Send Proposals", icon = "📨", colorArgb = 0xFFD97706.toInt(), targetPerWeek = 5, active = true, createdAt = now),
            Habit(id = "habit_github", name = "GitHub Commit", icon = "🐙", colorArgb = 0xFF16A34A.toInt(), targetPerWeek = 6, active = true, createdAt = now)
        )
    }
}
