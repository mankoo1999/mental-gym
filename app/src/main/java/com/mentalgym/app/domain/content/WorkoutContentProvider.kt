package com.mentalgym.app.domain.content

import com.mentalgym.app.domain.model.CognitiveSystem
import com.mentalgym.app.domain.model.DayOfWeek
import com.mentalgym.app.domain.model.Exercise
import com.mentalgym.app.domain.model.ExerciseInteractionKind
import com.mentalgym.app.domain.model.WorkoutSession

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
            description = "Digits show for a few seconds, then hide — type them back in order. AI sequences use Groq when a key is set.",
            durationMinutes = 10,
            difficultyLevel = 5,
            instructions = listOf(
                "Start the exercise timer first — your drill loads then",
                "Watch the memorize countdown; digits hide when it hits zero",
                "Type digits in order with no spaces, then Check answer",
                "Pause the exercise timer if you need more time before digits hide"
            ),
            interactionKind = ExerciseInteractionKind.AI_SEQUENCE_RECALL
        ),
        Exercise(
            id = "mental_math",
            name = "Mental Math",
            cognitiveSystem = CognitiveSystem.WORKING_MEMORY,
            description = "Solve arithmetic problems without writing. With a Groq key, the app generates problems for your level and checks your answer.",
            durationMinutes = 10,
            difficultyLevel = 6,
            instructions = listOf(
                "A new problem appears below — solve it mentally or jot on paper",
                "Difficulty follows the exercise level; tap New problem anytime",
                "Use Check answer for feedback (AI when a key is set, otherwise simple local check)",
                "Use the timer to pace yourself"
            ),
            interactionKind = ExerciseInteractionKind.AI_ARITHMETIC
        )
    )
    
    private fun getReasoningExercises() = listOf(
        Exercise(
            id = "logic_puzzles",
            name = "Logic Puzzles",
            cognitiveSystem = CognitiveSystem.REASONING_LOGIC,
            description = "Short logic prompts with a brief answer. AI creates new puzzles when a Groq key is set.",
            durationMinutes = 15,
            difficultyLevel = 6,
            instructions = listOf(
                "Read the prompt in the practice panel",
                "Answer in a word or short phrase",
                "Check answer for feedback; New challenge for another",
                "Use notes below the timer if you prefer pen and paper"
            ),
            interactionKind = ExerciseInteractionKind.AI_LOGIC_SHORT
        ),
        Exercise(
            id = "pattern_analysis",
            name = "Pattern Analysis",
            cognitiveSystem = CognitiveSystem.REASONING_LOGIC,
            description = "Spot the rule and name the next term. AI varies patterns when online.",
            durationMinutes = 10,
            difficultyLevel = 5,
            instructions = listOf(
                "Study the visible series",
                "Type the next term (or short phrase if words)",
                "Check answer; try New challenge for more",
                "Timer is optional pacing"
            ),
            interactionKind = ExerciseInteractionKind.AI_PATTERN
        )
    )
    
    private fun getFlexibilityExercises() = listOf(
        Exercise(
            id = "perspective_switching",
            name = "Perspective Switching",
            cognitiveSystem = CognitiveSystem.COGNITIVE_FLEXIBILITY,
            description = "The app suggests a concrete claim; you argue multiple sides in writing with optional AI coaching.",
            durationMinutes = 10,
            difficultyLevel = 5,
            instructions = listOf(
                "Start the timer — your topic appears in Live practice",
                "Write a short case for it, then against it, then a third angle",
                "Coach feedback uses Groq when a key is set",
                "Timer helps you allocate time across viewpoints"
            ),
            interactionKind = ExerciseInteractionKind.AI_TEXT_COACH
        ),
        Exercise(
            id = "category_switching",
            name = "Category Switching",
            cognitiveSystem = CognitiveSystem.COGNITIVE_FLEXIBILITY,
            description = "Practice switching rules — reflect in text; AI can comment on your notes.",
            durationMinutes = 10,
            difficultyLevel = 6,
            instructions = listOf(
                "Start the timer — example objects appear in Live practice",
                "Describe how you’d re-sort them under different rules (color, use, size…)",
                "Coach feedback if you use a Groq key",
                "Speed up switches across rounds"
            ),
            interactionKind = ExerciseInteractionKind.AI_TEXT_COACH
        )
    )
    
    private fun getLongTermMemoryExercises() = listOf(
        Exercise(
            id = "memory_palace",
            name = "Memory Palace",
            cognitiveSystem = CognitiveSystem.MEMORY_SYSTEMS,
            description = "Trains long-term memory using the method of loci: you link items to a familiar path so you can replay them in order. " +
                "This block is not about ‘computer memory’ — it’s the mental skill of structured recall.",
            durationMinutes = 15,
            difficultyLevel = 7,
            instructions = listOf(
                "Start the timer — Live practice gives a sample list and the palace steps",
                "Pick a real route you know well (home, campus, metro line)",
                "Place one exaggerated image per stop, in order",
                "Type your path briefly; optional Groq coach for clarity"
            ),
            interactionKind = ExerciseInteractionKind.AI_TEXT_COACH
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
            description = "A concrete brainstorming topic is assigned; list many ideas with optional AI coaching.",
            durationMinutes = 10,
            difficultyLevel = 4,
            instructions = listOf(
                "Start the timer — your topic appears in Live practice",
                "List freely in the panel (quantity first)",
                "Tap Coach feedback if you use Groq",
                "Reflect on quality at the end of the workout"
            ),
            interactionKind = ExerciseInteractionKind.AI_TEXT_COACH
        ),
        Exercise(
            id = "analogy_creation",
            name = "Analogy Creation",
            cognitiveSystem = CognitiveSystem.CREATIVE_THINKING,
            description = "Two concepts are suggested; bridge them with a metaphor and optional AI feedback.",
            durationMinutes = 10,
            difficultyLevel = 6,
            instructions = listOf(
                "Start the timer — concept pair appears in Live practice",
                "Note similarities, then one metaphor in the panel",
                "Coach feedback (Groq) can sharpen the comparison",
                "Timer keeps the block focused"
            ),
            interactionKind = ExerciseInteractionKind.AI_TEXT_COACH
        )
    )

    private fun getProcessingSpeedExercises() = listOf(
        Exercise(
            id = "rapid_naming",
            name = "Rapid Naming",
            cognitiveSystem = CognitiveSystem.PROCESSING_SPEED,
            description = "List items in an AI-picked category (often India-themed); get feedback on your list when online.",
            durationMinutes = 8,
            difficultyLevel = 5,
            instructions = listOf(
                "Start the timer first — then the category appears",
                "Type a comma- or line-separated list; speed beats spelling",
                "Tap Check for AI comments (or offline item count)",
                "New challenge picks another category"
            ),
            interactionKind = ExerciseInteractionKind.AI_CATEGORY_SPRINT
        )
    )
    
    private fun getEnduranceExercises() = listOf(
        Exercise(
            id = "complex_problem",
            name = "Complex Problem Solving",
            cognitiveSystem = CognitiveSystem.REASONING_LOGIC,
            description = "Outline a real problem and steps; optional AI coach on your plan.",
            durationMinutes = 30,
            difficultyLevel = 8,
            instructions = listOf(
                "Start the timer — a scenario appears in Live practice (or use your own)",
                "Break it into sub-problems and draft a solution path in the panel",
                "Coach feedback (Groq) can stress-test gaps",
                "Iterate after feedback"
            ),
            interactionKind = ExerciseInteractionKind.AI_TEXT_COACH
        )
    )
}
