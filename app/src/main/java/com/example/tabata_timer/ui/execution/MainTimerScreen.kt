package com.example.tabata_timer.ui.execution

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tabata_timer.ui.theme.TabatatimerTheme

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tabata_timer.domain.model.TimerState
import java.util.Locale

data class MainTimerUiState(
    val currentPhase: String = "PREPÁRATE",
    val currentTime: String = "00:00",
    val nextPhase: String = "ENTRENAR",
    val nextPhaseTime: String = "00:00",
    val totalTime: String = "00:00",
    val roundsRemaining: Int = 0,
    val setsRemaining: Int = 0,
    val isRunning: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTimerScreen(
    viewModel: TimerViewModel = hiltViewModel(),
    onWorkoutCancelled: () -> Unit = {}
) {
    val timerState by viewModel.timerState.collectAsState()
    
    // Mapping Domain State to UI State
    val uiState = rememberMainTimerUiState(timerState)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = uiState.totalTime,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onWorkoutCancelled) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.stop() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset")
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavBar()
        }
    ) { innerPadding ->
        MainTimerContent(
            uiState = uiState,
            onToggleTimer = {
                if (uiState.isRunning) viewModel.pause() else viewModel.resume()
            },
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun MainTimerContent(
    uiState: MainTimerUiState,
    onToggleTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Bloque Superior: Preparación
        TimerBlock(
            modifier = Modifier
                .weight(1.5f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer),
            label = uiState.currentPhase,
            time = uiState.currentTime,
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )

        // Bloque Intermedio: Siguiente fase
        TimerBlock(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer),
            label = "SIGUIENTE: ${uiState.nextPhase}",
            time = uiState.nextPhaseTime,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
            small = true
        )

        // Bloque Inferior: Métricas y Control
        Box(
            modifier = Modifier
                .weight(1.5f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetricCounter(
                    label = "RONDAS",
                    count = uiState.roundsRemaining.toString(),
                    modifier = Modifier.weight(1f)
                )

                // Botón Central
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    LargeStartButton(
                        isRunning = uiState.isRunning,
                        onClick = onToggleTimer
                    )
                }

                MetricCounter(
                    label = "CICLOS",
                    count = uiState.setsRemaining.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun rememberMainTimerUiState(timerState: TimerState): MainTimerUiState {
    return when (timerState) {
        is TimerState.Idle -> MainTimerUiState(isRunning = false)
        is TimerState.Finished -> MainTimerUiState(currentPhase = "FINALIZADO", isRunning = false)
        is TimerState.Paused -> {
            rememberMainTimerUiState(timerState.previousState).copy(isRunning = false)
        }
        else -> {
            val config = timerState.config ?: return MainTimerUiState()
            MainTimerUiState(
                currentPhase = when (timerState) {
                    is TimerState.Preparing -> "PREPÁRATE"
                    is TimerState.Working -> "ENTRENAR"
                    is TimerState.Resting -> "DESCANSO"
                    else -> ""
                },
                currentTime = formatTime(timerState.remainingSeconds),
                nextPhase = "Siguiente fase", // Simplificado para este paso
                nextPhaseTime = "00:00",
                totalTime = formatTime(config.totalTimeSeconds),
                roundsRemaining = config.rounds - timerState.currentRound + 1,
                setsRemaining = config.sets - timerState.currentSet + 1,
                isRunning = true
            )
        }
    }
}

fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", m, s)
}

@Composable
fun TimerBlock(
    modifier: Modifier = Modifier,
    label: String,
    time: String,
    labelColor: Color,
    small: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            style = if (small) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineMedium,
            color = labelColor.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = time,
            style = if (small) MaterialTheme.typography.displayMedium else MaterialTheme.typography.displayLarge,
            color = labelColor,
            fontWeight = FontWeight.Black,
            fontSize = if (small) 60.sp else 100.sp
        )
    }
}

@Composable
fun MetricCounter(
    label: String,
    count: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = count,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LargeStartButton(
    isRunning: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(100.dp),
        tonalElevation = 8.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isRunning) "Pausar" else "Empezar",
                modifier = Modifier.size(48.dp),
                tint = if (isRunning) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun BottomNavBar() {
    NavigationBar {
        val items = listOf(
            Triple("Tabata", Icons.Default.Timer, true),
            Triple("Rondas", Icons.Default.Repeat, false),
            Triple("Cronómetro", Icons.Default.HourglassEmpty, false)
        )
        items.forEach { (label, icon, selected) ->
            NavigationBarItem(
                selected = selected,
                onClick = { /* TODO */ },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainTimerScreenPreview() {
    TabatatimerTheme {
        MainTimerContent(
            uiState = MainTimerUiState(),
            onToggleTimer = {}
        )
    }
}
