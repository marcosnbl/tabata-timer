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
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        "Tabata Timer", 
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.headlineLarge
                    ) 
                },
                actions = {
                    IconButton(onClick = { viewModel.saveWorkout() }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                },
                scrollBehavior = scrollBehavior
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
                icon = { Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(32.dp)) },
                text = { Text("START", fontWeight = FontWeight.Bold, letterSpacing = 2.sp) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                TotalDurationCard(uiState.totalTimeSeconds)
            }

            item {
                Text(
                    "Workout Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
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
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TOTAL WORKOUT TIME",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 64.sp
                )
            )
        }
    }
}
