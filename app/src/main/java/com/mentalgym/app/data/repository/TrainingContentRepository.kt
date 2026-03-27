package com.mentalgym.app.data.repository

import com.mentalgym.app.domain.content.WorkoutContentProvider
import com.mentalgym.app.domain.model.TrainingProgram
import com.mentalgym.app.domain.model.WorkoutSession
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Weekly workout plans are fixed in [WorkoutContentProvider]. Groq is used only inside individual exercises (see [ExerciseInteractionKind]).
 */
@Singleton
class TrainingContentRepository @Inject constructor() {

    fun getWeeklyPlan(program: TrainingProgram): List<WorkoutSession> = when (program) {
        TrainingProgram.ESSENTIAL -> WorkoutContentProvider.getEssentialWeeklyPlan()
        TrainingProgram.STANDARD -> WorkoutContentProvider.getStandardWeeklyPlan()
        TrainingProgram.ELITE -> WorkoutContentProvider.getEliteWeeklyPlan()
    }
}
