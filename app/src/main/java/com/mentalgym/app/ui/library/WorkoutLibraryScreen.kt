package com.mentalgym.app.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mentalgym.app.domain.content.WorkoutContentProvider
import com.mentalgym.app.domain.model.CognitiveSystem
import com.mentalgym.app.domain.model.WorkoutSession
import com.mentalgym.app.ui.theme.CreativityYellow
import com.mentalgym.app.ui.theme.FlexibilityPink
import com.mentalgym.app.ui.theme.FocusBlue
import com.mentalgym.app.ui.theme.MemoryPurple
import com.mentalgym.app.ui.theme.NeuralPurple
import com.mentalgym.app.ui.theme.ReasoningGreen
import com.mentalgym.app.ui.theme.SpeedOrange

/**
 * Browse one sample workout per cognitive system — useful for QA and learning the catalog
 * before sticking to the weekly plan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutLibraryScreen(
    onBack: () -> Unit,
    onStartWorkout: (WorkoutSession) -> Unit
) {
    val previews = remember { WorkoutContentProvider.getAllCognitiveSystemPreviewWorkouts() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Workout library",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Preview every cognitive system",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Each card is a short sample session (not your scheduled day). " +
                        "Use it to feel how different systems are trained.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(previews, key = { it.id }) { session ->
                LibraryWorkoutRow(
                    session = session,
                    onStart = { onStartWorkout(session) }
                )
            }
        }
    }
}

@Composable
private fun LibraryWorkoutRow(
    session: WorkoutSession,
    onStart: () -> Unit
) {
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            getCognitiveSystemColor(session.cognitiveSystem).copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCognitiveSystemIcon(session.cognitiveSystem),
                        contentDescription = null,
                        tint = getCognitiveSystemColor(session.cognitiveSystem),
                        modifier = Modifier.size(26.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        session.cognitiveSystem.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        session.cognitiveSystem.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${session.exercises.size} sample exercise(s) · ${session.totalDurationMinutes} min",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Button(
                onClick = onStart,
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = NeuralPurple
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
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

private fun getCognitiveSystemIcon(system: CognitiveSystem): ImageVector {
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
