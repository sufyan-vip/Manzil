package com.manzil.app.domain.usecase

import com.manzil.app.core.recurrence.RRuleExpander
import com.manzil.app.core.recurrence.RRuleParser
import com.manzil.app.data.local.dao.TaskDao
import com.manzil.app.data.local.entity.TaskInstance
import java.time.LocalDate
import javax.inject.Inject

class ScheduleRecurringTasks @Inject constructor(
    private val taskDao: TaskDao
) {
    suspend fun expandAndSchedule(taskId: String, rruleStr: String, startDate: LocalDate) {
        val rule = RRuleParser.parse(rruleStr)
        val dates = RRuleExpander.expand(rule, startDate, 60)
        dates.forEach { date ->
            taskDao.insertInstance(TaskInstance(taskId = taskId, occurrenceDate = date))
        }
    }
}
