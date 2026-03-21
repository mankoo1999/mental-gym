package com.mentalgym.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mentalgym.app.domain.model.TrainingProgram
import com.mentalgym.app.ui.theme.*

@Composable
fun OnboardingScreen(
    onProgramSelected: (TrainingProgram) -> Unit
) {
    var selectedProgram by remember { mutableStateOf<TrainingProgram?>(null) }
    var currentPage by remember { mutableStateOf(0) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
        
        item {
            OnboardingHeader()
        }
        
        item {
            Text(
                "Choose Your Commitment Level",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        item {
            ProgramCard(
                program = TrainingProgram.ESSENTIAL,
                isSelected = selectedProgram == TrainingProgram.ESSENTIAL,
                onClick = { selectedProgram = TrainingProgram.ESSENTIAL }
            )
        }
        
        item {
            ProgramCard(
                program = TrainingProgram.STANDARD,
                isSelected = selectedProgram == TrainingProgram.STANDARD,
                onClick = { selectedProgram = TrainingProgram.STANDARD }
            )
        }
        
        item {
            ProgramCard(
                program = TrainingProgram.ELITE,
                isSelected = selectedProgram == TrainingProgram.ELITE,
                onClick = { selectedProgram = TrainingProgram.ELITE }
            )
        }
        
        item {
            AnimatedVisibility(visible = selectedProgram != null) {
                Button(
                    onClick = {
                        selectedProgram?.let { onProgramSelected(it) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeuralPurple
                    )
                ) {
                    Text(
                        "Start Your Cognitive Journey",
                        modifier = Modifier.padding(vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun OnboardingHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(NeuralPurple, CognitiveTeal)
                )
            )
            .padding(32.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Psychology,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.White
            )
            Text(
                "Welcome to Mental Gym",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                "Train your brain like you train your body. Maintain cognitive fitness in the AI era.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProgramCard(
    program: TrainingProgram,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isSelected -> NeuralPurple
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }
    
    val backgroundColor = when {
        isSelected -> NeuralPurple.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
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
                Column {
                    Text(
                        program.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${program.sessionsPerWeek} sessions per week",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = NeuralPurple,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Text(
                program.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Features
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (program) {
                    TrainingProgram.ESSENTIAL -> {
                        FeatureItem("15-20 minute sessions")
                        FeatureItem("Core cognitive training")
                        FeatureItem("Perfect for busy schedules")
                    }
                    TrainingProgram.STANDARD -> {
                        FeatureItem("20-25 minute sessions")
                        FeatureItem("Comprehensive brain workout")
                        FeatureItem("Balanced development")
                    }
                    TrainingProgram.ELITE -> {
                        FeatureItem("20-30 minute sessions")
                        FeatureItem("Advanced cognitive challenges")
                        FeatureItem("Maximum performance")
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Success
        )
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
