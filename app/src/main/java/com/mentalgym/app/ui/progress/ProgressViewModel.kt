package com.mentalgym.app.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentalgym.app.data.repository.WorkoutRepository
import com.mentalgym.app.domain.model.CognitiveSystem
import com.mentalgym.app.domain.model.TrainingProgram
import com.mentalgym.app.domain.model.toCognitiveSystemOrDefault
import com.mentalgym.app.domain.model.toTrainingProgramOrDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProgressUiState(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalWorkouts: Int = 0,
    val averageScore: Int = 0,
    val systemScores: Map<CognitiveSystem, Int> = emptyMap(),
    val recentWorkouts: List<WorkoutSummary> = emptyList(),
    val weeklyActivity: List<Int> = List(7) { 0 }, // Workouts per day for last 7 days
    val currentProgram: TrainingProgram = TrainingProgram.ESSENTIAL,
    val isLoading: Boolean = true
)

data class WorkoutSummary(
    val date: String,
    val cognitiveSystem: CognitiveSystem,
    val score: Int,
    val durationMinutes: Int
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()
    
    init {
        loadProgressData()
    }
    
    private fun loadProgressData() {
        viewModelScope.launch {
            combine(
                workoutRepository.userPreferences,
                workoutRepository.getRecentCompletions(30)
            ) { prefs, completions ->
                val totalWorkouts = completions.size
                val avgScore = if (completions.isNotEmpty()) {
                    completions.map { it.performanceScore }.average().toInt()
                } else 0
                
                // Calculate system scores
                val systemScores = CognitiveSystem.values().associateWith { system ->
                    val systemCompletions = completions.filter { 
                        it.cognitiveSystem == system.name 
                    }
                    if (systemCompletions.isNotEmpty()) {
                        systemCompletions.map { it.performanceScore }.average().toInt()
                    } else 0
                }
                
                // Recent workouts
                val recentWorkouts = completions.take(10).map { completion ->
                    WorkoutSummary(
                        date = formatDate(completion.completedDate),
                        cognitiveSystem = completion.cognitiveSystem.toCognitiveSystemOrDefault(),
                        score = completion.performanceScore,
                        durationMinutes = completion.durationMinutes
                    )
                }
                
                // Weekly activity
                val weeklyActivity = calculateWeeklyActivity(completions)
                
                ProgressUiState(
                    currentStreak = prefs?.currentStreak ?: 0,
                    longestStreak = prefs?.longestStreak ?: 0,
                    totalWorkouts = totalWorkouts,
                    averageScore = avgScore,
                    systemScores = systemScores,
                    recentWorkouts = recentWorkouts,
                    weeklyActivity = weeklyActivity,
                    currentProgram = prefs?.selectedProgram?.toTrainingProgramOrDefault()
                        ?: TrainingProgram.ESSENTIAL,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
    
    private fun calculateWeeklyActivity(completions: List<com.mentalgym.app.data.local.entity.WorkoutCompletionEntity>): List<Int> {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L
        
        return (0..6).map { daysAgo ->
            val dayStart = now - (daysAgo * oneDayMillis)
            val dayEnd = dayStart + oneDayMillis
            completions.count { it.completedDate in dayStart..dayEnd }
        }.reversed()
    }
    
    fun updateProgram(program: TrainingProgram) {
        viewModelScope.launch {
            workoutRepository.setUserProgram(program)
        }
    }

    private fun formatDate(timestamp: Long): String {
        val date = java.time.Instant.ofEpochMilli(timestamp)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        
        val today = java.time.LocalDate.now()
        val yesterday = today.minusDays(1)
        
        return when (date) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> {
                val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd")
                date.format(formatter)
            }
        }
    }
}
