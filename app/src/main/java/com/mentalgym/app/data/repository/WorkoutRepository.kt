package com.mentalgym.app.data.repository

import com.mentalgym.app.data.local.dao.ExerciseProgressDao
import com.mentalgym.app.data.local.dao.UserPreferencesDao
import com.mentalgym.app.data.local.dao.WorkoutCompletionDao
import com.mentalgym.app.data.local.entity.UserPreferencesEntity
import com.mentalgym.app.data.local.entity.WorkoutCompletionEntity
import com.mentalgym.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local persistence for preferences and completions.
 *
 * Backend roadmap: sync [completeWorkout] to a server for multi-device history, tamper-resistant
 * streaks, and analytics; hydrate content from CMS instead of only [com.mentalgym.app.domain.content.WorkoutContentProvider].
 */
@Singleton
class WorkoutRepository @Inject constructor(
    private val workoutCompletionDao: WorkoutCompletionDao,
    private val userPreferencesDao: UserPreferencesDao,
    private val exerciseProgressDao: ExerciseProgressDao
) {
    
    val userPreferences: Flow<UserPreferencesEntity?> = userPreferencesDao.getUserPreferences()
    
    suspend fun completeWorkout(
        workoutId: String,
        cognitiveSystem: CognitiveSystem,
        durationMinutes: Int,
        performanceScore: Int
    ) {
        val completion = WorkoutCompletionEntity(
            workoutId = workoutId,
            completedDate = System.currentTimeMillis(),
            durationMinutes = durationMinutes,
            performanceScore = performanceScore,
            cognitiveSystem = cognitiveSystem.name
        )
        workoutCompletionDao.insertCompletion(completion)
        updateStreak()
    }
    
    suspend fun getUserProgress(): UserProgress {
        val prefs = userPreferencesDao.getUserPreferences()
        val totalWorkouts = workoutCompletionDao.getTotalWorkouts()
        
        // Calculate average score and system scores
        val completions = workoutCompletionDao.getAllCompletions()
        
        return UserProgress(
            currentStreak = 0, // Will be calculated
            longestStreak = 0,
            totalWorkouts = totalWorkouts,
            averageScore = 0,
            systemScores = emptyMap(),
            currentProgram = TrainingProgram.ESSENTIAL
        )
    }
    
    private suspend fun updateStreak() {
        val prefs = userPreferencesDao.getUserPreferences()
        // Streak calculation logic here
    }
    
    suspend fun setUserProgram(program: TrainingProgram) {
        userPreferencesDao.updateProgram(program.name)
    }
    
    suspend fun completeOnboarding(program: TrainingProgram) {
        val prefs = UserPreferencesEntity(
            id = 1,
            selectedProgram = program.name,
            onboardingCompleted = true
        )
        userPreferencesDao.insertPreferences(prefs)
    }
    
    fun getRecentCompletions(days: Int): Flow<List<WorkoutCompletionEntity>> {
        val startDate = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return workoutCompletionDao.getCompletionsSince(startDate)
    }

    suspend fun clearAllLocalData() {
        workoutCompletionDao.deleteAll()
        exerciseProgressDao.deleteAll()
        userPreferencesDao.deleteAll()
    }

    /** Any workout completed during this local calendar day counts as done for daily reminder purposes. */
    suspend fun hasWorkoutCompletedOnLocalDate(
        date: LocalDate,
        zone: ZoneId = ZoneId.systemDefault()
    ): Boolean {
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val endExclusive = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return workoutCompletionDao.countCompletionsInRange(start, endExclusive) > 0
    }
}
