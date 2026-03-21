package com.mentalgym.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mentalgym.app.data.local.dao.WorkoutCompletionDao
import com.mentalgym.app.data.local.entity.WorkoutCompletionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class MentalGymDatabaseTest {
    
    private lateinit var database: MentalGymDatabase
    private lateinit var workoutDao: WorkoutCompletionDao
    
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            MentalGymDatabase::class.java
        ).build()
        workoutDao = database.workoutCompletionDao()
    }
    
    @After
    fun closeDb() {
        database.close()
    }
    
    @Test
    fun insertAndRetrieveWorkoutCompletion() = runBlocking {
        // Given
        val workout = WorkoutCompletionEntity(
            workoutId = "test_workout",
            completedDate = System.currentTimeMillis(),
            durationMinutes = 20,
            performanceScore = 85,
            cognitiveSystem = "ATTENTION_FOCUS"
        )
        
        // When
        workoutDao.insertCompletion(workout)
        val completions = workoutDao.getAllCompletions().first()
        
        // Then
        assertEquals(1, completions.size)
        assertEquals("test_workout", completions[0].workoutId)
        assertEquals(85, completions[0].performanceScore)
    }
    
    @Test
    fun getTotalWorkoutsReturnsCorrectCount() = runBlocking {
        // Given
        repeat(3) { index ->
            workoutDao.insertCompletion(
                WorkoutCompletionEntity(
                    workoutId = "workout_$index",
                    completedDate = System.currentTimeMillis(),
                    durationMinutes = 20,
                    performanceScore = 80,
                    cognitiveSystem = "WORKING_MEMORY"
                )
            )
        }
        
        // When
        val count = workoutDao.getTotalWorkouts()
        
        // Then
        assertEquals(3, count)
    }
    
    @Test
    fun deleteAllRemovesAllCompletions() = runBlocking {
        // Given
        workoutDao.insertCompletion(
            WorkoutCompletionEntity(
                workoutId = "test",
                completedDate = System.currentTimeMillis(),
                durationMinutes = 20,
                performanceScore = 90,
                cognitiveSystem = "REASONING_LOGIC"
            )
        )
        
        // When
        workoutDao.deleteAll()
        val count = workoutDao.getTotalWorkouts()
        
        // Then
        assertEquals(0, count)
    }
}
