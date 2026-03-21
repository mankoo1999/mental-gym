package com.mentalgym.app.ui.progress

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mentalgym.app.domain.model.CognitiveSystem
import com.mentalgym.app.domain.model.TrainingProgram
import com.mentalgym.app.ui.components.ProgramChangeDialog
import com.mentalgym.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onOpenWorkoutLibrary: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showProgramDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Your Progress",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
                OverallStatsCard(
                    uiState = uiState,
                    onChangeProgramClick = { showProgramDialog = true }
                )
            }

            item {
                WorkoutLibraryPromoCard(onOpenWorkoutLibrary = onOpenWorkoutLibrary)
            }
            
            item {
                WeeklyActivityChart(uiState.weeklyActivity)
            }
            
            item {
                CognitiveSystemsBreakdown(uiState.systemScores)
            }
            
            item {
                Text(
                    "Recent Workouts",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(uiState.recentWorkouts) { workout ->
                RecentWorkoutItem(workout)
            }
            
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showProgramDialog) {
        ProgramChangeDialog(
            currentProgram = uiState.currentProgram,
            onDismiss = { showProgramDialog = false },
            onProgramSelected = { program: TrainingProgram ->
                viewModel.updateProgram(program)
                showProgramDialog = false
            }
        )
    }
}

@Composable
private fun WorkoutLibraryPromoCard(onOpenWorkoutLibrary: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenWorkoutLibrary),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = NeuralPurple.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Workout library",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Preview sample sessions for every cognitive system — handy before launch or when exploring formats.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OverallStatsCard(
    uiState: ProgressUiState,
    onChangeProgramClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "Overall Performance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCircle(
                    value = uiState.totalWorkouts,
                    label = "Workouts",
                    color = NeuralPurple
                )
                
                StatCircle(
                    value = uiState.currentStreak,
                    label = "Day Streak",
                    color = EnergyOrange
                )
                
                StatCircle(
                    value = uiState.averageScore,
                    label = "Avg Score",
                    color = Success,
                    isPercentage = true
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Current program",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        uiState.currentProgram.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${uiState.currentProgram.sessionsPerWeek} sessions per week",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onChangeProgramClick) {
                        Text("Change program")
                    }
                }
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CognitiveTeal.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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
                            "Best: ${uiState.longestStreak}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = EnergyOrange
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCircle(
    value: Int,
    label: String,
    color: Color,
    isPercentage: Boolean = false
) {
    // Animated progress
    val animatedValue by animateFloatAsState(
        targetValue = if (isPercentage) value / 100f else 1f,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "stat_animation"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(80.dp)
        ) {
            Canvas(modifier = Modifier.size(80.dp)) {
                // Background circle
                drawCircle(
                    color = color.copy(alpha = 0.1f),
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
                
                // Progress arc
                if (isPercentage) {
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedValue,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
            
            Text(
                if (isPercentage) "$value%" else "$value",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WeeklyActivityChart(weeklyActivity: List<Int>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Weekly Activity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val maxValue = weeklyActivity.maxOrNull() ?: 1
                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                
                weeklyActivity.forEachIndexed { index, count ->
                    BarChartColumn(
                        value = count,
                        maxValue = maxValue,
                        label = days.getOrNull(index) ?: "",
                        color = NeuralPurple
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.BarChartColumn(
    value: Int,
    maxValue: Int,
    label: String,
    color: Color
) {
    val heightFraction = if (maxValue > 0) (value.toFloat() / maxValue) else 0f
    val animatedHeight by animateFloatAsState(
        targetValue = heightFraction,
        animationSpec = tween(durationMillis = 800, easing = EaseOutCubic),
        label = "bar_height"
    )
    
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight(animatedHeight)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(color),
            contentAlignment = Alignment.TopCenter
        ) {
            if (value > 0) {
                Text(
                    "$value",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CognitiveSystemsBreakdown(systemScores: Map<CognitiveSystem, Int>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Cognitive Systems",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            systemScores.forEach { (system, score) ->
                CognitiveSystemProgressBar(
                    system = system,
                    score = score
                )
            }
        }
    }
}

@Composable
private fun CognitiveSystemProgressBar(
    system: CognitiveSystem,
    score: Int
) {
    val color = getCognitiveSystemColor(system)
    val animatedProgress by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "progress"
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        getCognitiveSystemIcon(system),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Text(
                    system.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Text(
                "$score%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        
        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}

@Composable
private fun RecentWorkoutItem(workout: WorkoutSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(getCognitiveSystemColor(workout.cognitiveSystem).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        getCognitiveSystemIcon(workout.cognitiveSystem),
                        contentDescription = null,
                        tint = getCognitiveSystemColor(workout.cognitiveSystem),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column {
                    Text(
                        workout.cognitiveSystem.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "${workout.date} • ${workout.durationMinutes} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = getScoreColor(workout.score).copy(alpha = 0.15f)
            ) {
                Text(
                    "${workout.score}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = getScoreColor(workout.score),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
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

private fun getScoreColor(score: Int): Color {
    return when {
        score >= 80 -> Success
        score >= 60 -> Warning
        else -> Error
    }
}
