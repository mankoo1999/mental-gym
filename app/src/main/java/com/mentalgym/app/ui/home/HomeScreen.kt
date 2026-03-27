package com.mentalgym.app.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.hilt.navigation.compose.hiltViewModel
import com.mentalgym.app.R
import com.mentalgym.app.domain.model.CognitiveSystem
import com.mentalgym.app.domain.model.WorkoutSession
import com.mentalgym.app.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onStartWorkout: (WorkoutSession) -> Unit = {},
    onNavigateToProgress: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        !uiState.isOnboarded -> {
            OnboardingScreen(
                onProgramSelected = { program ->
                    viewModel.completeOnboarding(program)
                }
            )
        }
        else -> {
            HomeContent(
                uiState = uiState,
                onStartWorkout = onStartWorkout,
                onNavigateToProgress = onNavigateToProgress,
                onNavigateToSettings = onNavigateToSettings
            )
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onStartWorkout: (WorkoutSession) -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            HomeTopBar(
                streak = uiState.currentStreak,
                onSettingsClick = onNavigateToSettings,
                onProgressClick = onNavigateToProgress
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                MotivationalHeader()
            }
            
            item {
                if (uiState.todaysWorkout != null) {
                    TodaysWorkoutCard(
                        workout = uiState.todaysWorkout,
                        onStartClick = { onStartWorkout(uiState.todaysWorkout) }
                    )
                } else {
                    RestDayCard()
                }
            }
            
            item {
                WeeklyProgressCard(
                    program = uiState.currentProgram,
                    completedDays = uiState.weekProgress
                )
            }
            
            item {
                StatsOverviewCard(
                    totalWorkouts = uiState.totalWorkouts,
                    currentStreak = uiState.currentStreak
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    streak: Int,
    onSettingsClick: () -> Unit,
    onProgressClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Same bitmap as launcher foreground, but shown in a small circle — must zoom so the
                // brain (center of the square asset) reads clearly; Fit + uniform scale avoids squeeze.
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(3.dp, CircleShape, clip = false)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF3D2E7A), Color(0xFF0B0618)),
                                center = Offset(13f, 11f),
                                radius = 74f
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_brain_art),
                        contentDescription = "Mental Gym",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(1.52f)
                    )
                }
                Text(
                    "Mental Gym",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        actions = {
            // Streak indicator
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = EnergyOrange.copy(alpha = 0.15f),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = EnergyOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "$streak",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = EnergyOrange
                    )
                }
            }
            
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, "Settings")
            }
            IconButton(onClick = onProgressClick) {
                Icon(Icons.Default.BarChart, "Progress")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
private fun MotivationalHeader() {
    // Animated gradient background
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        NeuralPurple,
                        CognitiveTeal,
                        NeuralPurple
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Train Your Mind",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Maintain cognitive fitness in the AI era",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun TodaysWorkoutCard(
    workout: WorkoutSession,
    onStartClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Today's Workout",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        workout.cognitiveSystem.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Cognitive system icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(getCognitiveSystemColor(workout.cognitiveSystem).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        getCognitiveSystemIcon(workout.cognitiveSystem),
                        contentDescription = null,
                        tint = getCognitiveSystemColor(workout.cognitiveSystem),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Text(
                workout.cognitiveSystem.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoChip(
                    icon = Icons.Default.Timer,
                    text = "${workout.totalDurationMinutes} min"
                )
                InfoChip(
                    icon = Icons.Default.FitnessCenter,
                    text = "${workout.exercises.size} exercises"
                )
            }
            
            Button(
                onClick = onStartClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeuralPurple
                )
            ) {
                Text(
                    "Start Workout",
                    modifier = Modifier.padding(vertical = 4.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun RestDayCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Success.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.SelfImprovement,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Success
            )
            Text(
                "Rest Day",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Recovery is part of training. Your brain is consolidating today's learning.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WeeklyProgressCard(
    program: com.mentalgym.app.domain.model.TrainingProgram,
    completedDays: List<Boolean>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "This Week",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "${program.displayName} Program - ${program.sessionsPerWeek} days/week",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Week progress dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(7) { index ->
                    val isCompleted = index < completedDays.size && completedDays[index]
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCompleted) Success else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsOverviewCard(
    totalWorkouts: Int,
    currentStreak: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = "$totalWorkouts",
                label = "Total Workouts",
                icon = Icons.Default.FitnessCenter,
                color = NeuralPurple
            )
            StatItem(
                value = "$currentStreak",
                label = "Day Streak",
                icon = Icons.Default.LocalFireDepartment,
                color = EnergyOrange
            )
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

private fun getCognitiveSystemColor(system: CognitiveSystem): Color {
    return when (system) {
        CognitiveSystem.ATTENTION_FOCUS -> FocusBlue
        CognitiveSystem.WORKING_MEMORY -> MemoryPurple
        CognitiveSystem.REASONING_LOGIC -> ReasoningGreen
        CognitiveSystem.COGNITIVE_FLEXIBILITY -> FlexibilityPink
        CognitiveSystem.PROCESSING_SPEED -> SpeedOrange
        CognitiveSystem.MEMORY_SYSTEMS -> MemoryPurple
        CognitiveSystem.CREATIVE_THINKING -> CreativityYellow
    }
}

private fun getCognitiveSystemIcon(system: CognitiveSystem): androidx.compose.ui.graphics.vector.ImageVector {
    return when (system) {
        CognitiveSystem.ATTENTION_FOCUS -> Icons.Default.Visibility
        CognitiveSystem.WORKING_MEMORY -> Icons.Default.Psychology
        CognitiveSystem.REASONING_LOGIC -> Icons.Default.Science
        CognitiveSystem.COGNITIVE_FLEXIBILITY -> Icons.Default.AutoFixHigh
        CognitiveSystem.PROCESSING_SPEED -> Icons.Default.Speed
        CognitiveSystem.MEMORY_SYSTEMS -> Icons.Default.Storage
        CognitiveSystem.CREATIVE_THINKING -> Icons.Default.Lightbulb
    }
}
