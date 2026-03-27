package com.mentalgym.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentalgym.app.data.repository.TrainingContentRepository
import com.mentalgym.app.data.repository.WorkoutRepository
import com.mentalgym.app.domain.model.TrainingProgram
import com.mentalgym.app.domain.model.WorkoutSession
import com.mentalgym.app.domain.model.toTrainingProgramOrDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
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
    private val workoutRepository: WorkoutRepository,
    private val trainingContentRepository: TrainingContentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            workoutRepository.userPreferences.collectLatest { prefs ->
                if (prefs == null) {
                    _uiState.update {
                        it.copy(isLoading = false, isOnboarded = false)
                    }
                    return@collectLatest
                }
                val program = prefs.selectedProgram.toTrainingProgramOrDefault()
                val weekPlan = trainingContentRepository.getWeeklyPlan(program)
                val today = getTodaysWorkout(weekPlan)
                _uiState.update {
                    it.copy(
                        currentProgram = program,
                        todaysWorkout = today,
                        currentStreak = prefs.currentStreak,
                        isOnboarded = prefs.onboardingCompleted,
                        isLoading = false
                    )
                }
            }
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
