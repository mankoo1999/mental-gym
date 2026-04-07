package com.mentalgym.app

import android.app.Application
import com.mentalgym.app.di.ReminderReceiverEntryPoint
import com.mentalgym.app.reminder.DailyWorkoutReminderScheduler
import com.mentalgym.app.reminder.WorkoutReminderNotifications
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltAndroidApp
class MentalGymApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        WorkoutReminderNotifications.ensureChannel(this)
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                ReminderReceiverEntryPoint::class.java
            )
            val prefs = entryPoint.appPreferencesRepository()
            val enabled = prefs.dailyReminderEnabled.first()
            val time = prefs.dailyReminderTime.first()
            DailyWorkoutReminderScheduler.sync(applicationContext, enabled, time)
        }
    }
}
