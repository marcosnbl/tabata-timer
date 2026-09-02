package com.example.tabata_timer.ui.config

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tabata_timer.domain.model.WorkoutConfig
import com.example.tabata_timer.ui.config.components.ConfigPicker
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkoutScreen(
    onStartWorkout: (WorkoutConfig) -> Unit,
    viewModel: WorkoutConfigViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Workout", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.saveWorkout() }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val config = WorkoutConfig(
                        name = uiState.name,
                        color = uiState.color,
                        warmupSeconds = uiState.warmupSeconds,
                        workSeconds = uiState.workSeconds,
                        restSeconds = uiState.restSeconds,
                        rounds = uiState.rounds,
                        sets = uiState.sets,
                        restBetweenSetsSeconds = uiState.restBetweenSetsSeconds,
                        cooldownSeconds = uiState.cooldownSeconds
                    )
                    onStartWorkout(config)
                },
                icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                text = { Text("Start Workout") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TotalDurationCard(uiState.totalTimeSeconds)
            }

            item {
                ConfigPicker(
                    label = "Warmup",
                    value = "${uiState.warmupSeconds}s",
                    onIncrement = { viewModel.updateWarmup(5) },
                    onDecrement = { viewModel.updateWarmup(-5) }
                )
            }

            item {
                ConfigPicker(
                    label = "Work",
                    value = "${uiState.workSeconds}s",
                    onIncrement = { viewModel.updateWork(5) },
                    onDecrement = { viewModel.updateWork(-5) }
                )
            }

            item {
                ConfigPicker(
                    label = "Rest",
                    value = "${uiState.restSeconds}s",
                    onIncrement = { viewModel.updateRest(5) },
                    onDecrement = { viewModel.updateRest(-5) }
                )
            }

            item {
                ConfigPicker(
                    label = "Rounds",
                    value = uiState.rounds.toString(),
                    onIncrement = { viewModel.updateRounds(1) },
                    onDecrement = { viewModel.updateRounds(-1) }
                )
            }

            item {
                ConfigPicker(
                    label = "Sets",
                    value = uiState.sets.toString(),
                    onIncrement = { viewModel.updateSets(1) },
                    onDecrement = { viewModel.updateSets(-1) }
                )
            }

            item {
                ConfigPicker(
                    label = "Rest between sets",
                    value = "${uiState.restBetweenSetsSeconds}s",
                    onIncrement = { viewModel.updateRestBetweenSets(5) },
                    onDecrement = { viewModel.updateRestBetweenSets(-5) }
                )
            }

            item {
                ConfigPicker(
                    label = "Cooldown",
                    value = "${uiState.cooldownSeconds}s",
                    onIncrement = { viewModel.updateCooldown(5) },
                    onDecrement = { viewModel.updateCooldown(-5) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun TotalDurationCard(totalSeconds: Int) {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Duration",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
            Text(
                text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 48.sp
                ),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
