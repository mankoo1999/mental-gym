package com.mentalgym.app.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mentalgym.app.di.ReminderReceiverEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val appContext = context.applicationContext
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    appContext,
                    ReminderReceiverEntryPoint::class.java
                )
                val prefsRepo = entryPoint.appPreferencesRepository()
                val enabled = prefsRepo.dailyReminderEnabled.first()
                val timeKey = prefsRepo.dailyReminderTime.first()
                DailyWorkoutReminderScheduler.sync(appContext, enabled, timeKey)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
