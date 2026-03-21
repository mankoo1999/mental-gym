package com.mentalgym.app.domain.content

import com.mentalgym.app.domain.model.*

/**
 * Provides workout content and exercise definitions
 */
object WorkoutContentProvider {

    /**
     * One sample workout per cognitive system — for exploration and QA before launch.
     * Not tied to the weekly calendar.
     */
    fun getAllCognitiveSystemPreviewWorkouts(): List<WorkoutSession> {
        val days = DayOfWeek.entries.toTypedArray()
        return CognitiveSystem.entries.mapIndexed { index, system ->
            val exercises = exercisesForSystemPreview(system)
            WorkoutSession(
                id = "preview_${system.name.lowercase()}",
                dayOfWeek = days[index % days.size],
                cognitiveSystem = system,
                exercises = exercises,
                totalDurationMinutes = exercises.sumOf { it.durationMinutes }
            )
        }
    }

    private fun exercisesForSystemPreview(system: CognitiveSystem): List<Exercise> {
        return when (system) {
            CognitiveSystem.ATTENTION_FOCUS -> getFocusExercises().take(1)
            CognitiveSystem.WORKING_MEMORY -> getMemoryExercises().take(1)
            CognitiveSystem.REASONING_LOGIC -> getReasoningExercises().take(1)
            CognitiveSystem.COGNITIVE_FLEXIBILITY -> getFlexibilityExercises().take(1)
            CognitiveSystem.PROCESSING_SPEED -> getProcessingSpeedExercises()
            CognitiveSystem.MEMORY_SYSTEMS -> getLongTermMemoryExercises().take(1)
            CognitiveSystem.CREATIVE_THINKING -> getCreativityExercises().take(1)
        }
    }
    
    fun getEliteWeeklyPlan(): List<WorkoutSession> = listOf(
        WorkoutSession(
            id = "monday_focus",
            dayOfWeek = DayOfWeek.MONDAY,
            cognitiveSystem = CognitiveSystem.ATTENTION_FOCUS,
            exercises = getFocusExercises(),
            totalDurationMinutes = 20
        ),
        WorkoutSession(
            id = "tuesday_memory",
            dayOfWeek = DayOfWeek.TUESDAY,
            cognitiveSystem = CognitiveSystem.WORKING_MEMORY,
            exercises = getMemoryExercises(),
            totalDurationMinutes = 20
        ),
        WorkoutSession(
            id = "wednesday_reasoning",
            dayOfWeek = DayOfWeek.WEDNESDAY,
            cognitiveSystem = CognitiveSystem.REASONING_LOGIC,
            exercises = getReasoningExercises(),
            totalDurationMinutes = 25
        ),
        WorkoutSession(
            id = "thursday_flexibility",
            dayOfWeek = DayOfWeek.THURSDAY,
            cognitiveSystem = CognitiveSystem.COGNITIVE_FLEXIBILITY,
            exercises = getFlexibilityExercises(),
            totalDurationMinutes = 20
        ),
        WorkoutSession(
            id = "friday_memory_systems",
            dayOfWeek = DayOfWeek.FRIDAY,
            cognitiveSystem = CognitiveSystem.MEMORY_SYSTEMS,
            exercises = getLongTermMemoryExercises(),
            totalDurationMinutes = 25
        ),
        WorkoutSession(
            id = "saturday_creativity",
            dayOfWeek = DayOfWeek.SATURDAY,
            cognitiveSystem = CognitiveSystem.CREATIVE_THINKING,
            exercises = getCreativityExercises(),
            totalDurationMinutes = 20
        ),
        WorkoutSession(
            id = "sunday_endurance",
            dayOfWeek = DayOfWeek.SUNDAY,
            cognitiveSystem = CognitiveSystem.REASONING_LOGIC,
            exercises = getEnduranceExercises(),
            totalDurationMinutes = 30
        )
    )
    
    fun getStandardWeeklyPlan(): List<WorkoutSession> = listOf(
        WorkoutSession(
            id = "monday_focus_std",
            dayOfWeek = DayOfWeek.MONDAY,
            cognitiveSystem = CognitiveSystem.ATTENTION_FOCUS,
            exercises = getFocusExercises(),
            totalDurationMinutes = 20
        ),
        WorkoutSession(
            id = "tuesday_memory_std",
            dayOfWeek = DayOfWeek.TUESDAY,
            cognitiveSystem = CognitiveSystem.WORKING_MEMORY,
            exercises = getMemoryExercises(),
            totalDurationMinutes = 20
        ),
        WorkoutSession(
            id = "wednesday_reasoning_std",
            dayOfWeek = DayOfWeek.WEDNESDAY,
            cognitiveSystem = CognitiveSystem.REASONING_LOGIC,
            exercises = getReasoningExercises(),
            totalDurationMinutes = 25
        ),
        WorkoutSession(
            id = "friday_creativity_std",
            dayOfWeek = DayOfWeek.FRIDAY,
            cognitiveSystem = CognitiveSystem.CREATIVE_THINKING,
            exercises = getCreativityExercises(),
            totalDurationMinutes = 20
        ),
        WorkoutSession(
            id = "sunday_endurance_std",
            dayOfWeek = DayOfWeek.SUNDAY,
            cognitiveSystem = CognitiveSystem.REASONING_LOGIC,
            exercises = getEnduranceExercises(),
            totalDurationMinutes = 25
        )
    )
    
    fun getEssentialWeeklyPlan(): List<WorkoutSession> = listOf(
        WorkoutSession(
            id = "monday_focus_ess",
            dayOfWeek = DayOfWeek.MONDAY,
            cognitiveSystem = CognitiveSystem.ATTENTION_FOCUS,
            exercises = getFocusExercises().take(2),
            totalDurationMinutes = 15
        ),
        WorkoutSession(
            id = "wednesday_reasoning_ess",
            dayOfWeek = DayOfWeek.WEDNESDAY,
            cognitiveSystem = CognitiveSystem.REASONING_LOGIC,
            exercises = getReasoningExercises().take(2),
            totalDurationMinutes = 20
        ),
        WorkoutSession(
            id = "saturday_creativity_ess",
            dayOfWeek = DayOfWeek.SATURDAY,
            cognitiveSystem = CognitiveSystem.CREATIVE_THINKING,
            exercises = getCreativityExercises().take(2),
            totalDurationMinutes = 15
        )
    )
    
    private fun getFocusExercises() = listOf(
        Exercise(
            id = "focus_meditation",
            name = "Focus Meditation",
            cognitiveSystem = CognitiveSystem.ATTENTION_FOCUS,
            description = "10-minute mindfulness meditation to improve sustained attention",
            durationMinutes = 10,
            difficultyLevel = 3,
            instructions = listOf(
                "Find a quiet space and sit comfortably",
                "Close your eyes and focus on your breath",
                "When your mind wanders, gently return focus to breathing",
                "Continue for 10 minutes"
            )
        ),
        Exercise(
            id = "deep_reading",
            name = "Deep Reading",
            cognitiveSystem = CognitiveSystem.ATTENTION_FOCUS,
            description = "Read complex text with full concentration",
            durationMinutes = 10,
            difficultyLevel = 4,
            instructions = listOf(
                "Choose a challenging article or book chapter",
                "Read without any distractions",
                "Summarize each paragraph mentally",
                "Test your comprehension at the end"
            )
        )
    )
    
    private fun getMemoryExercises() = listOf(
        Exercise(
            id = "sequence_recall",
            name = "Sequence Recall",
            cognitiveSystem = CognitiveSystem.WORKING_MEMORY,
            description = "Remember and recall number sequences",
            durationMinutes = 10,
            difficultyLevel = 5,
            instructions = listOf(
                "View a sequence of numbers",
                "Wait 10 seconds",
                "Recall the sequence in order",
                "Gradually increase sequence length"
            )
        ),
        Exercise(
            id = "mental_math",
            name = "Mental Math",
            cognitiveSystem = CognitiveSystem.WORKING_MEMORY,
            description = "Solve arithmetic problems without writing",
            durationMinutes = 10,
            difficultyLevel = 6,
            instructions = listOf(
                "Solve multi-step math problems mentally",
                "Start with 2-digit addition/subtraction",
                "Progress to multiplication and division",
                "Track accuracy and speed"
            )
        )
    )
    
    private fun getReasoningExercises() = listOf(
        Exercise(
            id = "logic_puzzles",
            name = "Logic Puzzles",
            cognitiveSystem = CognitiveSystem.REASONING_LOGIC,
            description = "Solve structured logic problems",
            durationMinutes = 15,
            difficultyLevel = 6,
            instructions = listOf(
                "Read the puzzle scenario carefully",
                "Identify the logical constraints",
                "Use deductive reasoning to solve",
                "Verify your solution"
            )
        ),
        Exercise(
            id = "pattern_analysis",
            name = "Pattern Analysis",
            cognitiveSystem = CognitiveSystem.REASONING_LOGIC,
            description = "Identify patterns and predict next elements",
            durationMinutes = 10,
            difficultyLevel = 5,
            instructions = listOf(
                "Observe the pattern sequence",
                "Identify the underlying rule",
                "Predict the next 3 elements",
                "Test your hypothesis"
            )
        )
    )
    
    private fun getFlexibilityExercises() = listOf(
        Exercise(
            id = "perspective_switching",
            name = "Perspective Switching",
            cognitiveSystem = CognitiveSystem.COGNITIVE_FLEXIBILITY,
            description = "View problems from multiple angles",
            durationMinutes = 10,
            difficultyLevel = 5,
            instructions = listOf(
                "Read a controversial statement",
                "Argue for the position (3 minutes)",
                "Argue against the position (3 minutes)",
                "Find a third perspective (4 minutes)"
            )
        ),
        Exercise(
            id = "category_switching",
            name = "Category Switching",
            cognitiveSystem = CognitiveSystem.COGNITIVE_FLEXIBILITY,
            description = "Rapidly switch between classification systems",
            durationMinutes = 10,
            difficultyLevel = 6,
            instructions = listOf(
                "View objects one at a time",
                "Categorize by color, then shape, then size",
                "Switch categories every 10 seconds",
                "Increase switching speed"
            )
        )
    )
    
    private fun getLongTermMemoryExercises() = listOf(
        Exercise(
            id = "memory_palace",
            name = "Memory Palace",
            cognitiveSystem = CognitiveSystem.MEMORY_SYSTEMS,
            description = "Create spatial memory associations",
            durationMinutes = 15,
            difficultyLevel = 7,
            instructions = listOf(
                "Choose a familiar location",
                "Place 10 items mentally in the space",
                "Walk through mentally and recall items",
                "Test recall after 1 hour"
            )
        ),
        Exercise(
            id = "spaced_repetition",
            name = "Spaced Repetition",
            cognitiveSystem = CognitiveSystem.MEMORY_SYSTEMS,
            description = "Learn new information with optimal timing",
            durationMinutes = 10,
            difficultyLevel = 4,
            instructions = listOf(
                "Study new information",
                "Review after 1 minute",
                "Review after 10 minutes",
                "Review tomorrow"
            )
        )
    )
    
    private fun getCreativityExercises() = listOf(
        Exercise(
            id = "idea_generation",
            name = "Rapid Idea Generation",
            cognitiveSystem = CognitiveSystem.CREATIVE_THINKING,
            description = "Brainstorm many ideas on a fixed prompt. Use our suggestions or your own topic — both are fine.",
            durationMinutes = 10,
            difficultyLevel = 4,
            instructions = listOf(
                "Pick a topic: use one of these, or your own — \"ways to reduce screen time\", \"uses for a paperclip\", \"improvements to your commute\", \"gifts under \$20\"",
                "Start the timer below when you begin; it applies only to this exercise (the next block gets its own timer)",
                "List ideas in the optional notes field or on paper — the app does not score creativity yet",
                "Aim for quantity first; you judge quality using the reflection at the end of the workout"
            )
        ),
        Exercise(
            id = "analogy_creation",
            name = "Analogy Creation",
            cognitiveSystem = CognitiveSystem.CREATIVE_THINKING,
            description = "Link two domains with a metaphor. Suggested pairs are optional; free choice works too.",
            durationMinutes = 10,
            difficultyLevel = 6,
            instructions = listOf(
                "Pick two concepts: try \"electricity ↔ water\", \"company ↔ garden\", \"brain ↔ city\", or any two unrelated things you care about",
                "Start the timer when you begin; it resets for each exercise in this workout",
                "Find several similarities, then write one metaphor (optional notes or paper)",
                "Explain it aloud or briefly in notes — you verify your own clarity for now"
            )
        )
    )

    private fun getProcessingSpeedExercises() = listOf(
        Exercise(
            id = "rapid_naming",
            name = "Rapid Naming",
            cognitiveSystem = CognitiveSystem.PROCESSING_SPEED,
            description = "Name items in a category as fast as you can — trains fluent retrieval under mild time pressure.",
            durationMinutes = 8,
            difficultyLevel = 5,
            instructions = listOf(
                "Choose a category (e.g. fruits, countries, tools, mammals, or movies)",
                "Start the timer and list items aloud or in the optional notes field — speed matters more than perfect spelling",
                "When time is up, count how many you produced (honest self-count is enough for now)",
                "Optional: repeat with a harder category or a 3-minute sprint"
            )
        )
    )
    
    private fun getEnduranceExercises() = listOf(
        Exercise(
            id = "complex_problem",
            name = "Complex Problem Solving",
            cognitiveSystem = CognitiveSystem.REASONING_LOGIC,
            description = "Solve one challenging multi-step problem",
            durationMinutes = 30,
            difficultyLevel = 8,
            instructions = listOf(
                "Choose a complex real-world problem",
                "Break it into sub-problems",
                "Solve each systematically",
                "Integrate solutions"
            )
        )
    )
}
