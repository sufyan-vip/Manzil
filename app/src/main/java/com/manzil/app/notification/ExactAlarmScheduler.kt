package com.manzil.app.notification

import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExactAlarmScheduler @Inject constructor() {
    fun scheduleMorningBriefing(context: Context, hour: Int, minute: Int) {}
    fun scheduleEveningReview(context: Context, hour: Int, minute: Int) {}
}
