package com.mentalgym.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mentalgym.app.data.local.dao.ExerciseProgressDao
import com.mentalgym.app.data.local.dao.UserPreferencesDao
import com.mentalgym.app.data.local.dao.WorkoutCompletionDao
import com.mentalgym.app.data.local.entity.ExerciseProgressEntity
import com.mentalgym.app.data.local.entity.UserPreferencesEntity
import com.mentalgym.app.data.local.entity.WorkoutCompletionEntity

@Database(
    entities = [
        WorkoutCompletionEntity::class,
        UserPreferencesEntity::class,
        ExerciseProgressEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MentalGymDatabase : RoomDatabase() {
    abstract fun workoutCompletionDao(): WorkoutCompletionDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun exerciseProgressDao(): ExerciseProgressDao
}
