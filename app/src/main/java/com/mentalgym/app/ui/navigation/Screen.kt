package com.mentalgym.app.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Workout : Screen("workout")
    object Progress : Screen("progress")
    object Settings : Screen("settings")
    object WorkoutLibrary : Screen("workout_library")
}
