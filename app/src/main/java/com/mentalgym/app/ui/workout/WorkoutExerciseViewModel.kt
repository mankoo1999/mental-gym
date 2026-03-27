package com.mentalgym.app.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentalgym.app.BuildConfig
import com.mentalgym.app.data.preferences.AppPreferencesRepository
import com.mentalgym.app.data.remote.groq.ExerciseEvaluation
import com.mentalgym.app.data.remote.groq.GroqExerciseCoach
import com.mentalgym.app.data.remote.groq.LoadedInteractiveChallenge
import com.mentalgym.app.domain.model.Exercise
import com.mentalgym.app.domain.model.ExerciseInteractionKind
import com.mentalgym.app.domain.model.usesInteractivePanel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

data class InteractiveExerciseUiState(
    val loading: Boolean = false,
    val challenge: LoadedInteractiveChallenge? = null,
    val userInput: String = "",
    val evalLoading: Boolean = false,
    val evaluation: ExerciseEvaluation? = null,
    val error: String? = null
)

@HiltViewModel
class WorkoutExerciseViewModel @Inject constructor(
    private val coach: GroqExerciseCoach,
    private val appPreferencesRepository: AppPreferencesRepository
) : ViewModel() {

    private val challengeNonce = AtomicLong(System.nanoTime())

    private val _interactive = MutableStateFlow(InteractiveExerciseUiState())
    val interactiveUi: StateFlow<InteractiveExerciseUiState> = _interactive.asStateFlow()

    fun resetInteractiveUi() {
        _interactive.value = InteractiveExerciseUiState()
    }

    fun loadInteractiveChallenge(exercise: Exercise) {
        if (!exercise.interactionKind.usesInteractivePanel()) return
        viewModelScope.launch {
            _interactive.value = InteractiveExerciseUiState(loading = true, error = null)
            try {
                val key = resolveGroqKey()
                val instructions = exercise.instructions.joinToString("\n")
                val recent = if (exercise.interactionKind == ExerciseInteractionKind.AI_TEXT_COACH) {
                    appPreferencesRepository.getRecentDrillSnippets(exercise.id)
                } else {
                    emptyList()
                }
                val ch = coach.loadChallenge(
                    kind = exercise.interactionKind,
                    difficultyLevel = exercise.difficultyLevel,
                    exerciseId = exercise.id,
                    exerciseTitle = exercise.name,
                    exerciseDescription = exercise.description,
                    exerciseInstructions = instructions,
                    apiKey = key,
                    sessionNonce = challengeNonce.incrementAndGet(),
                    recentSnippets = recent
                )
                if (exercise.interactionKind == ExerciseInteractionKind.AI_TEXT_COACH) {
                    appPreferencesRepository.appendDrillSnippet(exercise.id, ch.body)
                }
                _interactive.value = InteractiveExerciseUiState(
                    loading = false,
                    challenge = ch,
                    userInput = "",
                    evaluation = null,
                    evalLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _interactive.value = InteractiveExerciseUiState(
                    loading = false,
                    error = e.message ?: "Could not load this drill.",
                    evalLoading = false
                )
            }
        }
    }

    fun updateUserInput(text: String) {
        _interactive.update { it.copy(userInput = text) }
    }

    fun submitInteractive(exercise: Exercise) {
        val snap = _interactive.value
        val ch = snap.challenge ?: return
        if (snap.userInput.isBlank()) return
        viewModelScope.launch {
            _interactive.update { it.copy(evalLoading = true) }
            val key = resolveGroqKey()
            val result = coach.evaluate(
                kind = exercise.interactionKind,
                challenge = ch,
                userInput = snap.userInput,
                apiKey = key
            )
            _interactive.update {
                it.copy(evalLoading = false, evaluation = result)
            }
        }
    }

    private suspend fun resolveGroqKey(): String? {
        val stored = appPreferencesRepository.groqApiKey.first().trim()
        if (stored.isNotEmpty()) return stored
        val def = BuildConfig.GROQ_API_KEY_DEFAULT.trim()
        return def.ifEmpty { null }
    }
}
