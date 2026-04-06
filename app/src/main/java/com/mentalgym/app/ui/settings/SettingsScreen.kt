package com.mentalgym.app.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Switch
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mentalgym.app.data.preferences.AppPreferencesRepository
import com.mentalgym.app.domain.TrainingSchedule
import com.mentalgym.app.ui.components.ProgramChangeDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentProgram by viewModel.trainingProgram.collectAsState()
    val savedGroqKey by viewModel.groqApiKey.collectAsState()
    val dailyReminderEnabled by viewModel.dailyReminderEnabled.collectAsState()
    val dailyReminderTime by viewModel.dailyReminderTime.collectAsState()
    var groqDraft by remember(savedGroqKey) { mutableStateOf(savedGroqKey) }
    var passwordVisible by remember { mutableStateOf(false) }

    var showProgramDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.setDailyReminderEnabled(true)
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(
                    "Allow notifications in system settings to use reminders."
                )
            }
        }
    }

    val notificationsAllowedForReminders = when {
        Build.VERSION.SDK_INT >= 33 ->
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        else -> true
    } && NotificationManagerCompat.from(context).areNotificationsEnabled()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Training",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                SettingsCard(
                    title = "Training Program",
                    subtitle = currentProgram.displayName,
                    icon = Icons.Default.FitnessCenter,
                    onClick = { showProgramDialog = true }
                )
            }

            item {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }

            item {
                Text(
                    "Reminders",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        "Daily workout nudge",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        buildString {
                                            append("Training days for your program: ")
                                            append(TrainingSchedule.trainingDaysSummary(currentProgram))
                                            append(". At your selected time below if you have not finished a workout that day.")
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = dailyReminderEnabled,
                                onCheckedChange = { wantOn ->
                                    if (wantOn) {
                                        if (Build.VERSION.SDK_INT >= 33) {
                                            when {
                                                ContextCompat.checkSelfPermission(
                                                    context,
                                                    Manifest.permission.POST_NOTIFICATIONS
                                                ) == PackageManager.PERMISSION_GRANTED -> {
                                                    viewModel.setDailyReminderEnabled(true)
                                                }
                                                else -> notificationPermissionLauncher.launch(
                                                    Manifest.permission.POST_NOTIFICATIONS
                                                )
                                            }
                                        } else {
                                            viewModel.setDailyReminderEnabled(true)
                                        }
                                    } else {
                                        viewModel.setDailyReminderEnabled(false)
                                    }
                                }
                            )
                        }

                        if (dailyReminderEnabled && !notificationsAllowedForReminders) {
                            Text(
                                "Notifications are off for this app. Turn them on in system settings or the reminder will not appear.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Text(
                            "Time",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = dailyReminderTime == AppPreferencesRepository.REMINDER_TIME_630,
                                onClick = {
                                    viewModel.setDailyReminderTime(
                                        AppPreferencesRepository.REMINDER_TIME_630
                                    )
                                },
                                label = { Text("6:30 PM") }
                            )
                            FilterChip(
                                selected = dailyReminderTime == AppPreferencesRepository.REMINDER_TIME_700,
                                onClick = {
                                    viewModel.setDailyReminderTime(
                                        AppPreferencesRepository.REMINDER_TIME_700
                                    )
                                },
                                label = { Text("7:00 PM") }
                            )
                        }
                    }
                }
            }

            item {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }

            item {
                Text(
                    "AI weekly plan (optional)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Text(
                    "Weekly workout plans are always built in. If you add a Groq API key, in-session drills " +
                        "(math, sequences, logic, patterns, naming lists, writing coach) use openai/gpt-oss-120b for generation " +
                        "and gpt-oss-20b for quick checks; each exercise still has an offline fallback. " +
                        "Your key stays on this device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                OutlinedTextField(
                    value = groqDraft,
                    onValueChange = { groqDraft = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Groq API key") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                if (passwordVisible) "Hide key" else "Show key"
                            )
                        }
                    }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.saveGroqApiKey(groqDraft) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save key")
                    }
                    TextButton(
                        onClick = {
                            viewModel.clearGroqApiKey()
                            groqDraft = ""
                        }
                    ) {
                        Text("Remove")
                    }
                }
            }

            item {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }

            item {
                Text(
                    "About",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                SettingsCard(
                    title = "Version",
                    subtitle = "1.0.0",
                    icon = Icons.Default.Info,
                    onClick = {}
                )
            }

            item {
                SettingsCard(
                    title = "Privacy Policy",
                    subtitle = "View our privacy policy",
                    icon = Icons.Default.PrivacyTip,
                    onClick = {}
                )
            }

            item {
                SettingsCard(
                    title = "Terms of Service",
                    subtitle = "View terms and conditions",
                    icon = Icons.Default.Description,
                    onClick = {}
                )
            }

            item {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }

            item {
                Text(
                    "Data",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                SettingsCard(
                    title = "Clear All Data",
                    subtitle = "Delete all workouts, progress, and settings",
                    icon = Icons.Default.DeleteForever,
                    iconTint = MaterialTheme.colorScheme.error,
                    onClick = { showClearDataDialog = true }
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showProgramDialog) {
        ProgramChangeDialog(
            currentProgram = currentProgram,
            onDismiss = { showProgramDialog = false },
            onProgramSelected = { program ->
                viewModel.setTrainingProgram(program)
                showProgramDialog = false
            }
        )
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Clear All Data?") },
            text = {
                Text("This will permanently delete your workouts, progress, saved program, and Groq key on this device. This cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        groqDraft = ""
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
