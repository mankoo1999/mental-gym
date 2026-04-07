package com.mentalgym.app.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentalgym.app.data.preferences.AppPreferencesRepository
import com.mentalgym.app.data.repository.WorkoutRepository
import com.mentalgym.app.domain.model.TrainingProgram
import com.mentalgym.app.domain.model.toTrainingProgramOrDefault
import com.mentalgym.app.reminder.DailyWorkoutReminderScheduler
import com.mentalgym.app.reminder.WorkoutReminderNotifications
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val appPreferencesRepository: AppPreferencesRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    val groqApiKey = appPreferencesRepository.groqApiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val trainingProgram = workoutRepository.userPreferences
        .map { prefs ->
            prefs?.selectedProgram?.toTrainingProgramOrDefault() ?: TrainingProgram.ESSENTIAL
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TrainingProgram.ESSENTIAL)

    val dailyReminderEnabled = appPreferencesRepository.dailyReminderEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val dailyReminderTime = appPreferencesRepository.dailyReminderTime
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AppPreferencesRepository.REMINDER_TIME_630
        )

    fun saveGroqApiKey(value: String) {
        viewModelScope.launch {
            appPreferencesRepository.setGroqApiKey(value)
        }
    }

    fun clearGroqApiKey() {
        viewModelScope.launch {
            appPreferencesRepository.clearGroqApiKey()
        }
    }

    fun setDailyReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.setDailyReminderEnabled(enabled)
            val time = appPreferencesRepository.dailyReminderTime.first()
            DailyWorkoutReminderScheduler.sync(appContext, enabled, time)
        }
    }

    fun setDailyReminderTime(timeKey: String) {
        viewModelScope.launch {
            appPreferencesRepository.setDailyReminderTime(timeKey)
            val enabled = appPreferencesRepository.dailyReminderEnabled.first()
            DailyWorkoutReminderScheduler.sync(appContext, enabled, timeKey)
        }
    }

    fun setTrainingProgram(program: TrainingProgram) {
        viewModelScope.launch {
            workoutRepository.setUserProgram(program)
        }
    }

    fun sendTestNotification() {
        WorkoutReminderNotifications.showPendingWorkoutReminder(appContext)
    }

    fun clearAllData() {
        viewModelScope.launch {
            DailyWorkoutReminderScheduler.cancel(appContext)
            workoutRepository.clearAllLocalData()
            appPreferencesRepository.clearAll()
        }
    }
}
