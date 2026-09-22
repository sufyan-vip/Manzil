package com.manzil.app.data.repository

import com.manzil.app.core.common.Fmt
import com.manzil.app.data.local.dao.CalendarDao
import com.manzil.app.data.local.dao.GoalDao
import com.manzil.app.data.local.dao.ReviewDao
import com.manzil.app.data.local.dao.TaskDao
import com.manzil.app.data.local.entity.TaskStatus
import com.manzil.app.domain.model.SearchHit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Instant local search over goals, milestones, tasks, events and journal entries.
 * Ranked in memory: title matches beat body matches, newer beats older.
 * Never blocks and never needs the network.
 */
@Singleton
class SearchRepository @Inject constructor(
    private val goalDao: GoalDao,
    private val taskDao: TaskDao,
    private val calendarDao: CalendarDao,
    private val reviewDao: ReviewDao
) {

    data class Result(
        val hits: List<SearchHit>,
        val tookMillis: Long
    )

    suspend fun search(rawQuery: String, typeFilter: String? = null): Result = withContext(Dispatchers.IO) {
        val started = System.currentTimeMillis()
        val query = rawQuery.trim()
        if (query.length < 2) return@withContext Result(emptyList(), 0L)
        val needles = query.lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }

        val hits = mutableListOf<SearchHit>()

        fun score(title: String, body: String): Int? {
            val lowerTitle = title.lowercase()
            val lowerBody = body.lowercase()
            var total = 0
            for (needle in needles) {
                val inTitle = lowerTitle.contains(needle)
                val inBody = lowerBody.contains(needle)
                if (!inTitle && !inBody) return null
                total += if (inTitle) 30 else 0
                if (lowerTitle.startsWith(needle)) total += 20
                if (inBody) total += 8
            }
            return total
        }

        goalDao.allGoals().forEach { goal ->
            val body = buildString {
                append(goal.description)
                append(' ')
                append(goal.category.name)
                goal.metricLabel?.let { append(' ').append(it) }
            }
            score(goal.title, body)?.let { base ->
                hits += SearchHit(
                    type = "GOAL",
                    id = goal.id,
                    title = goal.title,
                    snippet = goal.description.ifBlank { goal.category.name.lowercase() },
                    dateIso = goal.targetDate?.toString(),
                    score = base + 6
                )
            }
        }

        goalDao.allMilestones().forEach { milestone ->
            score(milestone.title, "")?.let { base ->
                hits += SearchHit(
                    type = "MILESTONE",
                    id = milestone.id,
                    title = milestone.title,
                    snippet = if (milestone.done) "milestone · done" else "milestone · open",
                    dateIso = milestone.dueDate?.toString(),
                    score = base
                )
            }
        }

        taskDao.allActive().forEach { task ->
            val body = buildString {
                append(task.notes)
                append(' ')
                if (task.status == TaskStatus.DONE) append("done ")
                task.clientPlatform?.let { append(it) }
            }
            score(task.title, body)?.let { base ->
                hits += SearchHit(
                    type = "TASK",
                    id = task.id,
                    title = task.title,
                    snippet = buildString {
                        append(if (task.status == TaskStatus.DONE) "done" else "open")
                        task.dueDate?.let { append(" · ").append(Fmt.dateShort(it)) }
                        task.estimatedMinutes?.let { append(" · ").append(Fmt.duration(it)) }
                    },
                    dateIso = task.dueDate?.toString(),
                    score = base
                )
            }
        }

        calendarDao.between(0L, Long.MAX_VALUE - 1).forEach { event ->
            score(event.title, event.description)?.let { base ->
                hits += SearchHit(
                    type = "EVENT",
                    id = event.id,
                    title = event.title,
                    snippet = Fmt.time(event.startAt) + " · " + Fmt.dateShort(Fmt.dayOfMillis(event.startAt)),
                    dateIso = Fmt.dayOfMillis(event.startAt).toString(),
                    score = base - 4
                )
            }
        }

        val journals = reviewDao.journalsSince(LocalDate.now().minusDays(400))
        journals.forEach { journal ->
            val body = "${journal.note} ${journal.wins} ${journal.blockers}"
            score("Journal ${Fmt.dateShort(journal.date)}", body)?.let { base ->
                hits += SearchHit(
                    type = "JOURNAL",
                    id = journal.id,
                    title = "Journal · ${Fmt.dateLong(journal.date)}",
                    snippet = listOf(journal.wins, journal.blockers, journal.note)
                        .firstOrNull { it.isNotBlank() } ?: "mood ${journal.mood}/5",
                    dateIso = journal.date.toString(),
                    score = base - 2
                )
            }
        }

        val filtered = hits
            .filter { typeFilter == null || it.type == typeFilter }
            .sortedWith(compareByDescending<SearchHit> { it.score }.thenByDescending { it.dateIso ?: "" })
            .distinctBy { it.type + it.id }
            .take(60)

        Result(filtered, System.currentTimeMillis() - started)
    }
}
