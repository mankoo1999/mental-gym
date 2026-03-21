package com.mentalgym.app

/**
 * Frontend-only for now. When you add a backend, prioritize:
 *
 * 1. **Workout completions** — [com.mentalgym.app.data.repository.WorkoutRepository.completeWorkout]:
 *    trusted timestamps, user id, optional device id.
 * 2. **Practice notes** — text typed in [com.mentalgym.app.ui.workout.WorkoutScreen] exercises:
 *    encrypted at rest, optional NLP / coaching endpoints.
 * 3. **Accounts** — auth, settings, and training program sync across devices.
 * 4. **Content** — replace or augment [com.mentalgym.app.domain.content.WorkoutContentProvider]
 *    with remote-configured workouts and scoring rubrics.
 */
@Suppress("unused")
object BackendRoadmap
