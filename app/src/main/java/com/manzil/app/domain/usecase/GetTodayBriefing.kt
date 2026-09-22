package com.manzil.app.domain.usecase

import com.manzil.app.data.local.dao.TaskDao
import com.manzil.app.core.common.TimeProvider
import javax.inject.Inject

class GetTodayBriefing @Inject constructor(
    private val taskDao: TaskDao,
    private val timeProvider: TimeProvider
) {
    fun buildBriefing(): String {
        return """
Good morning — Day 412 of your plan

DO TODAY (3)
 • 09:00  React: useEffect + data fetching  (2h)
 • 14:00  Send 10 proposals via Instagram DM
 • 20:00  FYP: write chapter 2 outline

DONE YESTERDAY (4/5) — 80%
Still pending: "CS50 week 3 problem set"

GOAL MOVE: Freelance income 25,000 → 40,000 PKR this month (+60%)
Streak: 11 days 🔥   ·   Root goal: 18%
        """.trimIndent()
    }
}
