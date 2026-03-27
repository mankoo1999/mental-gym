package com.mentalgym.app.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mentalgym.app.di.ReminderReceiverEntryPoint
import com.mentalgym.app.domain.TrainingSchedule
import com.mentalgym.app.domain.model.toTrainingProgramOrDefault
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class DailyWorkoutReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val appContext = context.applicationContext
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    appContext,
                    ReminderReceiverEntryPoint::class.java
                )
                val prefsRepo = entryPoint.appPreferencesRepository()
                val workoutRepo = entryPoint.workoutRepository()

                if (!prefsRepo.dailyReminderEnabled.first()) return@launch

                val entity = workoutRepo.userPreferences.first()
                if (entity?.onboardingCompleted != true) return@launch

                val program = entity.selectedProgram.toTrainingProgramOrDefault()
                val today = LocalDate.now()
                if (!TrainingSchedule.isScheduledTrainingDay(program, today)) return@launch

                val alreadyDone = workoutRepo.hasWorkoutCompletedOnLocalDate(today)
                if (!alreadyDone) {
                    WorkoutReminderNotifications.showPendingWorkoutReminder(appContext)
                }
            } finally {
                try {
                    val entryPoint = EntryPointAccessors.fromApplication(
                        appContext,
                        ReminderReceiverEntryPoint::class.java
                    )
                    val prefsRepo = entryPoint.appPreferencesRepository()
                    val enabled = prefsRepo.dailyReminderEnabled.first()
                    val timeKey = prefsRepo.dailyReminderTime.first()
                    DailyWorkoutReminderScheduler.sync(appContext, enabled, timeKey)
                } catch (_: Exception) {
                }
                pendingResult.finish()
            }
        }
    }
}
