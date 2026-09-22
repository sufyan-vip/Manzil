package com.manzil.app.data.local

import androidx.room.TypeConverter
import com.manzil.app.data.local.entity.*
import java.time.LocalDate
import java.time.LocalTime

class Converters {
    @TypeConverter fun fromLocalDate(date: LocalDate?): String? = date?.toString()
    @TypeConverter fun toLocalDate(str: String?): LocalDate? = str?.let { LocalDate.parse(it) }
    @TypeConverter fun fromLocalTime(time: LocalTime?): String? = time?.toString()
    @TypeConverter fun toLocalTime(str: String?): LocalTime? = str?.let { LocalTime.parse(it) }
    @TypeConverter fun fromGoalCategory(cat: GoalCategory): String = cat.name
    @TypeConverter fun toGoalCategory(str: String): GoalCategory = GoalCategory.valueOf(str)
    @TypeConverter fun fromGoalStatus(s: GoalStatus): String = s.name
    @TypeConverter fun toGoalStatus(str: String): GoalStatus = GoalStatus.valueOf(str)
    @TypeConverter fun fromTaskStatus(s: TaskStatus): String = s.name
    @TypeConverter fun toTaskStatus(str: String): TaskStatus = TaskStatus.valueOf(str)
    @TypeConverter fun fromChangeType(c: ChangeType): String = c.name
    @TypeConverter fun toChangeType(str: String): ChangeType = ChangeType.valueOf(str)
    @TypeConverter fun fromEventSource(e: EventSource): String = e.name
    @TypeConverter fun toEventSource(str: String): EventSource = EventSource.valueOf(str)
    @TypeConverter fun fromTimeSource(t: TimeSource): String = t.name
    @TypeConverter fun toTimeSource(str: String): TimeSource = TimeSource.valueOf(str)
}
