package com.mentalgym.app.domain

import com.mentalgym.app.domain.model.DayOfWeek
import com.mentalgym.app.domain.model.TrainingProgram
import java.time.LocalDate

/**
 * Training days per program, aligned with [com.mentalgym.app.domain.content.WorkoutContentProvider] schedules.
 */
object TrainingSchedule {

    fun isScheduledTrainingDay(program: TrainingProgram, date: LocalDate): Boolean {
        val day = when (date.dayOfWeek) {
            java.time.DayOfWeek.MONDAY -> DayOfWeek.MONDAY
            java.time.DayOfWeek.TUESDAY -> DayOfWeek.TUESDAY
            java.time.DayOfWeek.WEDNESDAY -> DayOfWeek.WEDNESDAY
            java.time.DayOfWeek.THURSDAY -> DayOfWeek.THURSDAY
            java.time.DayOfWeek.FRIDAY -> DayOfWeek.FRIDAY
            java.time.DayOfWeek.SATURDAY -> DayOfWeek.SATURDAY
            java.time.DayOfWeek.SUNDAY -> DayOfWeek.SUNDAY
        }
        return day in expectedDays(program)
    }

    private fun expectedDays(program: TrainingProgram): Set<DayOfWeek> = when (program) {
        TrainingProgram.ESSENTIAL -> setOf(
            DayOfWeek.MONDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.SATURDAY
        )
        TrainingProgram.STANDARD -> setOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        )
        TrainingProgram.ELITE -> DayOfWeek.entries.toSet()
    }

    /** Short label for settings copy; must stay aligned with [expectedDays]. */
    fun trainingDaysSummary(program: TrainingProgram): String = when (program) {
        TrainingProgram.ESSENTIAL -> "Mon, Wed, Sat"
        TrainingProgram.STANDARD -> "Mon–Fri"
        TrainingProgram.ELITE -> "every day"
    }
}
