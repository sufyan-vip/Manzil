package com.manzil.app.di

import android.content.Context
import androidx.room.Room
import com.manzil.app.data.local.ManzilDatabase
import com.manzil.app.data.local.dao.*
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
    fun provideDatabase(@ApplicationContext context: Context): ManzilDatabase {
        return Room.databaseBuilder(context, ManzilDatabase::class.java, "manzil.db")
            .addCallback(ManzilDatabase.getCallback())
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides fun provideGoalDao(db: ManzilDatabase): GoalDao = db.goalDao()
    @Provides fun provideTaskDao(db: ManzilDatabase): TaskDao = db.taskDao()
    @Provides fun provideSearchDao(db: ManzilDatabase): SearchDao = db.searchDao()
    @Provides fun provideCalendarDao(db: ManzilDatabase): CalendarDao = db.calendarDao()
    @Provides fun provideReviewDao(db: ManzilDatabase): ReviewDao = db.reviewDao()
    @Provides fun provideTimeDao(db: ManzilDatabase): TimeDao = db.timeDao()
    @Provides fun provideHabitDao(db: ManzilDatabase): HabitDao = db.habitDao()
    @Provides fun provideKpiDao(db: ManzilDatabase): KpiDao = db.kpiDao()
    @Provides fun provideNotificationDao(db: ManzilDatabase): NotificationDao = db.notificationDao()
}
