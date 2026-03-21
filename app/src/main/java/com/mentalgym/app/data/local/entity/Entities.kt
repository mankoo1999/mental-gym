package com.mentalgym.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mentalgym.app.domain.model.CognitiveSystem
import com.mentalgym.app.domain.model.TrainingProgram

/**
 * Database entity for workout completions
 */
@Entity(tableName = "workout_completions")
data class WorkoutCompletionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workoutId: String,
    val completedDate: Long,
    val durationMinutes: Int,
    val performanceScore: Int,
    val cognitiveSystem: String
)

/**
 * Database entity for user preferences
 */
@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey
    val id: Int = 1,
    val selectedProgram: String,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastWorkoutDate: Long = 0,
    val onboardingCompleted: Boolean = false
)

/**
 * Database entity for exercise progress
 */
@Entity(tableName = "exercise_progress")
data class ExerciseProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exerciseId: String,
    val cognitiveSystem: String,
    val currentDifficulty: Int,
    val bestScore: Int,
    val timesCompleted: Int,
    val lastCompletedDate: Long
)
