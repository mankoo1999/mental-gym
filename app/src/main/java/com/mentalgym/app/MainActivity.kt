package com.mentalgym.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.mentalgym.app.data.preferences.AppPreferencesRepository
import com.mentalgym.app.data.repository.WorkoutRepository
import com.mentalgym.app.domain.TrainingSchedule
import com.mentalgym.app.domain.model.toTrainingProgramOrDefault
import com.mentalgym.app.reminder.DailyWorkoutReminderScheduler
import com.mentalgym.app.reminder.WorkoutReminderNotifications
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mentalgym.app.ui.home.HomeScreen
import com.mentalgym.app.ui.library.WorkoutLibraryScreen
import com.mentalgym.app.ui.navigation.Screen
import com.mentalgym.app.ui.progress.ProgressScreen
import com.mentalgym.app.ui.settings.SettingsScreen
import com.mentalgym.app.ui.theme.MentalGymTheme
import com.mentalgym.app.domain.model.WorkoutSession
import com.mentalgym.app.ui.workout.WorkoutScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var appPreferencesRepository: AppPreferencesRepository
    @Inject lateinit var workoutRepository: WorkoutRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MentalGymTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MentalGymApp()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val enabled = appPreferencesRepository.dailyReminderEnabled.first()
            val timeKey = appPreferencesRepository.dailyReminderTime.first()
            DailyWorkoutReminderScheduler.sync(this@MainActivity, enabled, timeKey)

            if (!enabled) return@launch
            val entity = workoutRepository.userPreferences.first() ?: return@launch
            if (!entity.onboardingCompleted) return@launch

            val (hour, minute) = DailyWorkoutReminderScheduler.parseTime(timeKey)
            val now = LocalTime.now()
            if (now.isBefore(LocalTime.of(hour, minute))) return@launch

            val today = LocalDate.now()
            val program = entity.selectedProgram.toTrainingProgramOrDefault()
            if (!TrainingSchedule.isScheduledTrainingDay(program, today)) return@launch
            if (workoutRepository.hasWorkoutCompletedOnLocalDate(today)) return@launch

            WorkoutReminderNotifications.showPendingWorkoutReminder(this@MainActivity)
        }
    }
}

@Composable
fun MentalGymApp() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onStartWorkout = { workout ->
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "workout",
                        workout
                    )
                    navController.navigate(Screen.Workout.route)
                },
                onNavigateToProgress = {
                    navController.navigate(Screen.Progress.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Workout.route) {
            val workout = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<WorkoutSession>("workout")

            if (workout != null) {
                WorkoutScreen(
                    workout = workout,
                    onComplete = { score ->
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Could not load this workout. Go back and try again.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Back")
                    }
                }
            }
        }
        
        composable(Screen.Progress.route) {
            ProgressScreen(
                onBack = {
                    navController.popBackStack()
                },
                onOpenWorkoutLibrary = {
                    navController.navigate(Screen.WorkoutLibrary.route)
                }
            )
        }

        composable(Screen.WorkoutLibrary.route) {
            WorkoutLibraryScreen(
                onBack = { navController.popBackStack() },
                onStartWorkout = { workout ->
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "workout",
                        workout
                    )
                    navController.navigate(Screen.Workout.route)
                }
            )
        }
    }
}
