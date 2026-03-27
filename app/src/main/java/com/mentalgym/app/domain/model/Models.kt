package com.mentalgym.app.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents the seven core cognitive systems that Mental Gym trains
 */
@Parcelize
enum class CognitiveSystem(val displayName: String, val description: String) : Parcelable {
    ATTENTION_FOCUS(
        "Attention & Focus",
        "Sustain concentration and ignore distractions"
    ),
    WORKING_MEMORY(
        "Working Memory",
        "Short-term processing capacity"
    ),
    REASONING_LOGIC(
        "Reasoning & Logic",
        "Analyze problems and derive conclusions"
    ),
    COGNITIVE_FLEXIBILITY(
        "Cognitive Flexibility",
        "Switch between ideas and adapt thinking"
    ),
    PROCESSING_SPEED(
        "Processing Speed",
        "Speed of information processing"
    ),
    MEMORY_SYSTEMS(
        "Memory Systems",
        "Long-term and associative memory"
    ),
    CREATIVE_THINKING(
        "Creative Thinking",
        "Generate new ideas and novel solutions"
    )
}

/**
 * Training program commitment levels
 */
enum class TrainingProgram(
    val displayName: String,
    val sessionsPerWeek: Int,
    val description: String
) {
    ESSENTIAL(
        "Essential",
        3,
        "Baseline cognitive fitness for busy people"
    ),
    STANDARD(
        "Standard",
        5,
        "Balanced cognitive conditioning"
    ),
    ELITE(
        "Elite",
        7,
        "Advanced cognitive performance"
    )
}

/**
 * How the workout screen interacts for this exercise.
 * Weekly plans stay fixed from [com.mentalgym.app.domain.content.WorkoutContentProvider]; Groq is used only during these exercise types when a key is set.
 */
@Parcelize
enum class ExerciseInteractionKind : Parcelable {
    OPEN_PRACTICE,

    AI_ARITHMETIC,
    AI_SEQUENCE_RECALL,
    AI_LOGIC_SHORT,
    AI_PATTERN,
    AI_CATEGORY_SPRINT,
    /** User writes freely; model gives coaching feedback only (no single correct answer). */
    AI_TEXT_COACH
}

/**
 * Represents a single cognitive exercise
 */
@Parcelize
data class Exercise(
    val id: String,
    val name: String,
    val cognitiveSystem: CognitiveSystem,
    val description: String,
    val durationMinutes: Int,
    val difficultyLevel: Int, // 1-10
    val instructions: List<String>,
    val interactionKind: ExerciseInteractionKind = ExerciseInteractionKind.OPEN_PRACTICE
) : Parcelable

/**
 * Represents a daily workout session
 */
@Parcelize
data class WorkoutSession(
    val id: String,
    val dayOfWeek: DayOfWeek,
    val cognitiveSystem: CognitiveSystem,
    val exercises: List<Exercise>,
    val totalDurationMinutes: Int,
    val completed: Boolean = false
) : Parcelable

/**
 * User's workout completion record
 */
data class WorkoutCompletion(
    val id: Long = 0,
    val workoutId: String,
    val completedDate: Long,
    val durationMinutes: Int,
    val performanceScore: Int, // 0-100
    val cognitiveSystem: CognitiveSystem
)

/**
 * User progress metrics
 */
data class UserProgress(
    val currentStreak: Int,
    val longestStreak: Int,
    val totalWorkouts: Int,
    val averageScore: Int,
    val systemScores: Map<CognitiveSystem, Int>,
    val currentProgram: TrainingProgram
)

/**
 * Day of week enum
 */
@Parcelize
enum class DayOfWeek(val displayName: String) : Parcelable {
    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday"),
    SUNDAY("Sunday")
}

/** Avoid crashes when Room contains legacy or corrupted enum names. */
fun String.toTrainingProgramOrDefault(): TrainingProgram =
    runCatching { TrainingProgram.valueOf(this) }.getOrDefault(TrainingProgram.ESSENTIAL)

fun String.toCognitiveSystemOrDefault(): CognitiveSystem =
    runCatching { CognitiveSystem.valueOf(this) }.getOrDefault(CognitiveSystem.ATTENTION_FOCUS)
