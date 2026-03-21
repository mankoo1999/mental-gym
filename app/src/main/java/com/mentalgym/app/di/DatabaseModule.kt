package com.mentalgym.app.di

import android.content.Context
import androidx.room.Room
import com.mentalgym.app.data.local.MentalGymDatabase
import com.mentalgym.app.data.local.dao.ExerciseProgressDao
import com.mentalgym.app.data.local.dao.UserPreferencesDao
import com.mentalgym.app.data.local.dao.WorkoutCompletionDao
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
    fun provideDatabase(@ApplicationContext context: Context): MentalGymDatabase {
        return Room.databaseBuilder(
            context,
            MentalGymDatabase::class.java,
            "mental_gym_database"
        ).build()
    }
    
    @Provides
    fun provideWorkoutCompletionDao(database: MentalGymDatabase): WorkoutCompletionDao {
        return database.workoutCompletionDao()
    }
    
    @Provides
    fun provideUserPreferencesDao(database: MentalGymDatabase): UserPreferencesDao {
        return database.userPreferencesDao()
    }
    
    @Provides
    fun provideExerciseProgressDao(database: MentalGymDatabase): ExerciseProgressDao {
        return database.exerciseProgressDao()
    }
}
