package com.manzil.app.di

import android.content.Context
import androidx.room.Room
import com.manzil.app.data.local.ManzilDatabase
import com.manzil.app.data.local.dao.CalendarDao
import com.manzil.app.data.local.dao.GoalDao
import com.manzil.app.data.local.dao.HabitDao
import com.manzil.app.data.local.dao.KpiDao
import com.manzil.app.data.local.dao.NotificationDao
import com.manzil.app.data.local.dao.ReviewDao
import com.manzil.app.data.local.dao.TaskDao
import com.manzil.app.data.local.dao.TimeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ManzilDatabase =
        Room.databaseBuilder(context, ManzilDatabase::class.java, ManzilDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideGoalDao(db: ManzilDatabase): GoalDao = db.goalDao()

    @Provides
    fun provideTaskDao(db: ManzilDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideCalendarDao(db: ManzilDatabase): CalendarDao = db.calendarDao()

    @Provides
    fun provideReviewDao(db: ManzilDatabase): ReviewDao = db.reviewDao()

    @Provides
    fun provideTimeDao(db: ManzilDatabase): TimeDao = db.timeDao()

    @Provides
    fun provideHabitDao(db: ManzilDatabase): HabitDao = db.habitDao()

    @Provides
    fun provideKpiDao(db: ManzilDatabase): KpiDao = db.kpiDao()

    @Provides
    fun provideNotificationDao(db: ManzilDatabase): NotificationDao = db.notificationDao()
}
