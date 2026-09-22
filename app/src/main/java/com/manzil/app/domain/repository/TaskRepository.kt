package com.manzil.app.domain.repository
import com.manzil.app.data.local.entity.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
interface TaskRepository {
    fun getTasksForDate(date: LocalDate): Flow<List<Task>>
}
