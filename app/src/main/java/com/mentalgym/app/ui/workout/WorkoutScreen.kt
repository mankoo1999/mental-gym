package com.mentalgym.app.ui.workout

// BACKEND_TODO: Persist workout completion via WorkoutRepository; sync notes & scores when API exists.
// BACKEND_TODO: Optional AI / coach review of open-ended practice text.

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mentalgym.app.domain.model.Exercise
import com.mentalgym.app.domain.model.ExerciseInteractionKind
import com.mentalgym.app.domain.model.WorkoutSession
import com.mentalgym.app.domain.model.usesInteractivePanel
import com.mentalgym.app.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    workout: WorkoutSession,
    workoutExerciseViewModel: WorkoutExerciseViewModel = hiltViewModel(),
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
                    workoutExerciseViewModel = workoutExerciseViewModel,
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
    workoutExerciseViewModel: WorkoutExerciseViewModel,
    onComplete: () -> Unit
) {
    LaunchedEffect(exercise.id) {
        workoutExerciseViewModel.resetInteractiveUi()
    }

    val interactiveUi by workoutExerciseViewModel.interactiveUi.collectAsState()

    var timeRemaining by remember(exercise.id) {
        mutableIntStateOf(exercise.durationMinutes * 60)
    }
    var isTimerRunning by remember(exercise.id) { mutableStateOf(false) }
    var timerPrimed by remember(exercise.id) { mutableStateOf(false) }
    LaunchedEffect(isTimerRunning, exercise.id) {
        if (exercise.interactionKind.usesInteractivePanel() && isTimerRunning && !timerPrimed) {
            timerPrimed = true
            workoutExerciseViewModel.loadInteractiveChallenge(exercise)
        }
    }
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
            .imePadding()
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

            when {
                exercise.interactionKind.usesInteractivePanel() -> {
                    val showLivePanel = isTimerRunning ||
                        interactiveUi.loading ||
                        interactiveUi.challenge != null ||
                        interactiveUi.error != null
                    if (!showLivePanel) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Live practice",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Start the exercise timer above first. Your drill loads then (memory sequences " +
                                        "use a short memorize countdown tied to that timer).",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        InteractiveExercisePanel(
                            exercise = exercise,
                            interactiveUi = interactiveUi,
                            workoutExerciseViewModel = workoutExerciseViewModel,
                            isTimerRunning = isTimerRunning
                        )
                    }
                }
                else -> {
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
private fun InteractiveExercisePanel(
    exercise: Exercise,
    interactiveUi: InteractiveExerciseUiState,
    workoutExerciseViewModel: WorkoutExerciseViewModel,
    isTimerRunning: Boolean
) {
    val submitLabel = when (exercise.interactionKind) {
        ExerciseInteractionKind.AI_TEXT_COACH -> "Coach feedback"
        ExerciseInteractionKind.AI_CATEGORY_SPRINT -> "Check list"
        else -> "Check answer"
    }
    val showNewChallenge = exercise.interactionKind != ExerciseInteractionKind.AI_TEXT_COACH

    val ch = interactiveUi.challenge
    val useMemorizePhase =
        exercise.interactionKind == ExerciseInteractionKind.AI_SEQUENCE_RECALL &&
            ch != null && ch.memorizeSeconds > 0 && ch.recallPrompt != null
    val memKey =
        if (useMemorizePhase && ch != null) "${ch.expectedTextKey}_${ch.memorizeSeconds}_${ch.body.length}" else ""

    var inRecallPhase by remember(memKey) { mutableStateOf(!useMemorizePhase) }
    var secLeft by remember(memKey) { mutableIntStateOf(ch?.memorizeSeconds ?: 0) }
    val timerRunningNow = rememberUpdatedState(isTimerRunning)

    LaunchedEffect(memKey) {
        val snap = workoutExerciseViewModel.interactiveUi.value.challenge
        val active =
            exercise.interactionKind == ExerciseInteractionKind.AI_SEQUENCE_RECALL &&
                snap != null && snap.memorizeSeconds > 0 && snap.recallPrompt != null &&
                memKey.isNotEmpty()
        if (!active) {
            inRecallPhase = true
            return@LaunchedEffect
        }
        val challengeSnap = snap ?: return@LaunchedEffect
        val total = challengeSnap.memorizeSeconds
        inRecallPhase = false
        secLeft = total
        while (secLeft > 0) {
            delay(1000)
            if (timerRunningNow.value) secLeft--
        }
        inRecallPhase = true
    }

    val canInteract =
        isTimerRunning && !interactiveUi.loading && !interactiveUi.evalLoading &&
            (!useMemorizePhase || inRecallPhase)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Live practice",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Weekly plans stay fixed. Generation uses openai/gpt-oss-120b on Groq; short checks use gpt-oss-20b " +
                    "(Settings → API key). Offline fallbacks still work.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!isTimerRunning) {
                Text(
                    "Timer paused — resume the exercise timer to type, submit, or start a new challenge.",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Warning
                )
            }
            interactiveUi.error?.let { msg ->
                Text(
                    msg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                TextButton(
                    onClick = { workoutExerciseViewModel.loadInteractiveChallenge(exercise) },
                    enabled = isTimerRunning && !interactiveUi.loading
                ) {
                    Text("Retry")
                }
            }
            if (interactiveUi.loading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp),
                        color = NeuralPurple
                    )
                }
            } else {
                val challenge = interactiveUi.challenge
                Text(
                    challenge?.title ?: "…",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (useMemorizePhase && !inRecallPhase) {
                    Text(
                        challenge?.body ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Memorize… hides in ${secLeft}s (pauses if you pause the exercise timer)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeuralPurple
                    )
                } else {
                    Text(
                        if (useMemorizePhase && challenge?.recallPrompt != null) {
                            challenge.recallPrompt
                        } else {
                            challenge?.body ?: ""
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            OutlinedTextField(
                value = interactiveUi.userInput,
                onValueChange = { workoutExerciseViewModel.updateUserInput(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (interactiveUi.challenge?.multilineInput == true) {
                            Modifier.heightIn(min = 120.dp, max = 320.dp)
                        } else {
                            Modifier
                        }
                    ),
                label = {
                    Text(
                        if (interactiveUi.challenge?.multilineInput == true) "Your response"
                        else "Your answer"
                    )
                },
                singleLine = interactiveUi.challenge?.multilineInput != true,
                minLines = if (interactiveUi.challenge?.multilineInput == true) 3 else 1,
                maxLines = if (interactiveUi.challenge?.multilineInput == true) 18 else 1,
                enabled = canInteract && interactiveUi.error == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { workoutExerciseViewModel.submitInteractive(exercise) },
                    enabled = interactiveUi.userInput.isNotBlank() &&
                        canInteract && interactiveUi.challenge != null && interactiveUi.error == null,
                    colors = ButtonDefaults.buttonColors(containerColor = NeuralPurple)
                ) {
                    Text(submitLabel)
                }
                if (showNewChallenge) {
                    TextButton(
                        onClick = { workoutExerciseViewModel.loadInteractiveChallenge(exercise) },
                        enabled = isTimerRunning && !interactiveUi.loading && !interactiveUi.evalLoading
                    ) {
                        Text("New challenge")
                    }
                }
            }
            if (interactiveUi.evalLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = NeuralPurple
                )
            }
            interactiveUi.evaluation?.let { ev ->
                val tint = when {
                    ev.correct == true -> Success
                    ev.correct == false -> MaterialTheme.colorScheme.error
                    else -> NeuralPurple
                }
                Text(
                    ev.feedback,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = tint
                )
                Text(
                    if (ev.usedAi) "Coach: AI (gpt-oss-20b)" else "Coach: offline",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
            "How hard did you push and how focused were you? Adjust the slider — structured drills may have been graded " +
                "in-session; this score is still your overall honest check-in.",
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
