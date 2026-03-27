package com.mentalgym.app.data.local.dao

import androidx.room.*
import com.mentalgym.app.data.local.entity.ExerciseProgressEntity
import com.mentalgym.app.data.local.entity.UserPreferencesEntity
import com.mentalgym.app.data.local.entity.WorkoutCompletionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutCompletionDao {
    @Query("SELECT * FROM workout_completions ORDER BY completedDate DESC")
    fun getAllCompletions(): Flow<List<WorkoutCompletionEntity>>
    
    @Query("SELECT * FROM workout_completions WHERE completedDate >= :startDate ORDER BY completedDate DESC")
    fun getCompletionsSince(startDate: Long): Flow<List<WorkoutCompletionEntity>>
    
    @Query("SELECT COUNT(*) FROM workout_completions")
    suspend fun getTotalWorkouts(): Int
    
    @Insert
    suspend fun insertCompletion(completion: WorkoutCompletionEntity)
    
    @Query("DELETE FROM workout_completions")
    suspend fun deleteAll()

    @Query(
        "SELECT COUNT(*) FROM workout_completions WHERE completedDate >= :startInclusive AND completedDate < :endExclusive"
    )
    suspend fun countCompletionsInRange(startInclusive: Long, endExclusive: Long): Int
}

@Dao
interface UserPreferencesDao {
    @Query("SELECT * FROM user_preferences WHERE id = 1")
    fun getUserPreferences(): Flow<UserPreferencesEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreferences(preferences: UserPreferencesEntity)
    
    @Update
    suspend fun updatePreferences(preferences: UserPreferencesEntity)
    
    @Query("UPDATE user_preferences SET currentStreak = :streak WHERE id = 1")
    suspend fun updateStreak(streak: Int)
    
    @Query("UPDATE user_preferences SET selectedProgram = :program WHERE id = 1")
    suspend fun updateProgram(program: String)

    @Query("DELETE FROM user_preferences")
    suspend fun deleteAll()
}

@Dao
interface ExerciseProgressDao {
    @Query("SELECT * FROM exercise_progress WHERE exerciseId = :exerciseId")
    suspend fun getProgressForExercise(exerciseId: String): ExerciseProgressEntity?
    
    @Query("SELECT * FROM exercise_progress WHERE cognitiveSystem = :system")
    fun getProgressForSystem(system: String): Flow<List<ExerciseProgressEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: ExerciseProgressEntity)
    
    @Update
    suspend fun updateProgress(progress: ExerciseProgressEntity)

    @Query("DELETE FROM exercise_progress")
    suspend fun deleteAll()
}
