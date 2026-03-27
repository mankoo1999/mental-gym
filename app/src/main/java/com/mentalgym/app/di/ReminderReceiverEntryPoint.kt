package com.mentalgym.app.di

import com.mentalgym.app.data.preferences.AppPreferencesRepository
import com.mentalgym.app.data.repository.WorkoutRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ReminderReceiverEntryPoint {
    fun workoutRepository(): WorkoutRepository
    fun appPreferencesRepository(): AppPreferencesRepository
}
