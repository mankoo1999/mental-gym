package com.mentalgym.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentalgym.app.data.repository.WorkoutRepository
import com.mentalgym.app.domain.content.WorkoutContentProvider
import com.mentalgym.app.domain.model.TrainingProgram
import com.mentalgym.app.domain.model.WorkoutSession
import com.mentalgym.app.domain.model.toTrainingProgramOrDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val currentProgram: TrainingProgram = TrainingProgram.ESSENTIAL,
    val todaysWorkout: WorkoutSession? = null,
    val currentStreak: Int = 0,
    val totalWorkouts: Int = 0,
    val weekProgress: List<Boolean> = emptyList(),
    val isOnboarded: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadUserData()
    }
    
    private fun loadUserData() {
        viewModelScope.launch {
            workoutRepository.userPreferences.collect { prefs ->
                if (prefs != null) {
                    val program = prefs.selectedProgram.toTrainingProgramOrDefault()
                    val weekPlan = getWeekPlanForProgram(program)
                    val today = getTodaysWorkout(weekPlan)
                    
                    _uiState.update { current ->
                        current.copy(
                            currentProgram = program,
                            todaysWorkout = today,
                            currentStreak = prefs.currentStreak,
                            isOnboarded = prefs.onboardingCompleted,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, isOnboarded = false) }
                }
            }
        }
    }
    
    private fun getWeekPlanForProgram(program: TrainingProgram): List<WorkoutSession> {
        return when (program) {
            TrainingProgram.ESSENTIAL -> WorkoutContentProvider.getEssentialWeeklyPlan()
            TrainingProgram.STANDARD -> WorkoutContentProvider.getStandardWeeklyPlan()
            TrainingProgram.ELITE -> WorkoutContentProvider.getEliteWeeklyPlan()
        }
    }
    
    private fun getTodaysWorkout(weekPlan: List<WorkoutSession>): WorkoutSession? {
        val today = LocalDate.now().dayOfWeek
        val dayOfWeekMapping = mapOf(
            DayOfWeek.MONDAY to com.mentalgym.app.domain.model.DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY to com.mentalgym.app.domain.model.DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY to com.mentalgym.app.domain.model.DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY to com.mentalgym.app.domain.model.DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY to com.mentalgym.app.domain.model.DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY to com.mentalgym.app.domain.model.DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY to com.mentalgym.app.domain.model.DayOfWeek.SUNDAY
        )
        
        return weekPlan.find { it.dayOfWeek == dayOfWeekMapping[today] }
    }
    
    fun completeOnboarding(program: TrainingProgram) {
        viewModelScope.launch {
            workoutRepository.completeOnboarding(program)
        }
    }
}
