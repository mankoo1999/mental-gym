package com.mentalgym.app.ui.workout

// BACKEND_TODO: Persist workout completion via WorkoutRepository; sync notes & scores when API exists.
// BACKEND_TODO: Optional AI / coach review of open-ended practice text.

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mentalgym.app.domain.model.Exercise
import com.mentalgym.app.domain.model.WorkoutSession
import com.mentalgym.app.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    workout: WorkoutSession,
    onComplete: (Int) -> Unit = {},
    onBack: () -> Unit = {}
) {
    var currentExerciseIndex by remember { mutableStateOf(0) }
    var isCompleted by remember { mutableStateOf(false) }
    var performanceScore by remember { mutableStateOf(70) }
    
    val currentExercise = workout.exercises.getOrNull(currentExerciseIndex)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            workout.cognitiveSystem.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Exercise ${currentExerciseIndex + 1} of ${workout.exercises.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = (currentExerciseIndex + 1f) / workout.exercises.size.coerceAtLeast(1),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = NeuralPurple,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isCompleted) {
                WorkoutCompleteScreen(
                    workout = workout,
                    score = performanceScore,
                    onScoreChange = { performanceScore = it },
                    onFinish = { onComplete(performanceScore) }
                )
            } else if (currentExercise != null) {
                ExerciseView(
                    exercise = currentExercise,
                    onComplete = {
                        if (currentExerciseIndex < workout.exercises.size - 1) {
                            currentExerciseIndex++
                        } else {
                            isCompleted = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ExerciseView(
    exercise: Exercise,
    onComplete: () -> Unit
) {
    var timeRemaining by remember(exercise.id) {
        mutableIntStateOf(exercise.durationMinutes * 60)
    }
    var isTimerRunning by remember(exercise.id) { mutableStateOf(false) }
    var practiceNotes by remember(exercise.id) { mutableStateOf("") }
    var checkedSteps by remember(exercise.id) { mutableStateOf(setOf<Int>()) }

    LaunchedEffect(isTimerRunning, exercise.id) {
        if (!isTimerRunning) return@LaunchedEffect
        while (timeRemaining > 0) {
            delay(1000)
            timeRemaining--
        }
        isTimerRunning = false
    }

    val scrollState = remember(exercise.id) { ScrollState(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = getCognitiveSystemColor(exercise.cognitiveSystem).copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                exercise.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                exercise.cognitiveSystem.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DifficultyBadge(difficulty = exercise.difficultyLevel)
                    }
                    Text(
                        exercise.description,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Timer for this exercise only",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Each block has its own countdown (${exercise.durationMinutes} min for this one). " +
                            "Starting the next exercise resets the clock.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatTime(timeRemaining),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isTimerRunning) NeuralPurple else MaterialTheme.colorScheme.onSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (!isTimerRunning) {
                            Button(
                                onClick = { isTimerRunning = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Success)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start")
                            }
                        } else {
                            Button(
                                onClick = { isTimerRunning = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Warning)
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pause")
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "How this works",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Open-ended drills are not auto-graded. Use checkboxes for steps you attempted, " +
                            "notes for your ideas, and an honest self-rating after the workout. " +
                            "Later, a backend can store this and optional AI feedback.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Steps",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    exercise.instructions.forEachIndexed { index, instruction ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = checkedSteps.contains(index),
                                onCheckedChange = { on ->
                                    checkedSteps =
                                        if (on) checkedSteps + index else checkedSteps - index
                                }
                            )
                            Text(
                                instruction,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    OutlinedTextField(
                        value = practiceNotes,
                        onValueChange = { practiceNotes = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Practice notes (optional)") },
                        placeholder = {
                            Text("Ideas, counts, or reflections — stays on this device for now")
                        },
                        minLines = 3,
                        maxLines = 8
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeuralPurple)
        ) {
            Text(
                if (timeRemaining == 0) "Complete exercise" else "Next exercise",
                modifier = Modifier.padding(vertical = 8.dp),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun WorkoutCompleteScreen(
    workout: WorkoutSession,
    score: Int,
    onScoreChange: (Int) -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Success.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = Success
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            "Workout Complete!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "You trained ${workout.cognitiveSystem.displayName}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            "How hard did you push and how focused were you? Adjust the slider — there is no automated grading yet.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp, start = 8.dp, end = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Self-check score",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    "$score%",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeuralPurple
                )

                Slider(
                    value = score.toFloat(),
                    onValueChange = { onScoreChange(it.toInt().coerceIn(0, 100)) },
                    valueRange = 0f..100f,
                    steps = 19,
                    modifier = Modifier.fillMaxWidth()
                )
                
                LinearProgressIndicator(
                    progress = score / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = NeuralPurple
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeuralPurple
            )
        ) {
            Text(
                "Finish",
                modifier = Modifier.padding(vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DifficultyBadge(difficulty: Int) {
    val color = when {
        difficulty <= 3 -> Success
        difficulty <= 6 -> Warning
        else -> Error
    }
    
    val label = when {
        difficulty <= 3 -> "Easy"
        difficulty <= 6 -> "Medium"
        else -> "Hard"
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(difficulty.coerceAtMost(10)) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

private fun getCognitiveSystemColor(system: com.mentalgym.app.domain.model.CognitiveSystem): Color {
    return when (system) {
        com.mentalgym.app.domain.model.CognitiveSystem.ATTENTION_FOCUS -> FocusBlue
        com.mentalgym.app.domain.model.CognitiveSystem.WORKING_MEMORY -> MemoryPurple
        com.mentalgym.app.domain.model.CognitiveSystem.REASONING_LOGIC -> ReasoningGreen
        com.mentalgym.app.domain.model.CognitiveSystem.COGNITIVE_FLEXIBILITY -> FlexibilityPink
        com.mentalgym.app.domain.model.CognitiveSystem.PROCESSING_SPEED -> SpeedOrange
        com.mentalgym.app.domain.model.CognitiveSystem.MEMORY_SYSTEMS -> MemoryPurple
        com.mentalgym.app.domain.model.CognitiveSystem.CREATIVE_THINKING -> CreativityYellow
    }
}
