package com.example.tabata_timer.ui.execution

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tabata_timer.domain.model.TimerState
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun WorkoutExecutionScreen(
    onWorkoutCancelled: () -> Unit,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val state by viewModel.timerState.collectAsState()
    val context = LocalContext.current
    val window = (context as? Activity)?.window

    // Keep screen on during workout
    DisposableEffect(state) {
        val keepScreenOn = state !is TimerState.Idle && state !is TimerState.Finished && state !is TimerState.Paused
        if (keepScreenOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = when (state) {
            is TimerState.Preparing -> Color(0xFFFFA500) // Orange
            is TimerState.Working -> Color(0xFF4CAF50) // Green
            is TimerState.Resting -> Color(0xFF2196F3) // Blue
            is TimerState.Paused -> Color(0xFF9E9E9E) // Gray
            else -> MaterialTheme.colorScheme.background
        },
        label = "BackgroundColorAnimation"
    )

    val contentColor = if (state is TimerState.Idle || state is TimerState.Finished) {
        MaterialTheme.colorScheme.onBackground
    } else {
        Color.White
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Info
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = when (state) {
                        is TimerState.Preparing -> "GET READY"
                        is TimerState.Working -> "WORK"
                        is TimerState.Resting -> "REST"
                        is TimerState.Paused -> "PAUSED"
                        is TimerState.Finished -> "FINISHED"
                        else -> ""
                    },
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    ),
                    color = contentColor
                )
                
                if (state !is TimerState.Idle && state !is TimerState.Finished) {
                    Text(
                        text = "Round ${state.currentRound}/${state.config?.rounds ?: 0} • Set ${state.currentSet}/${state.config?.sets ?: 0}",
                        style = MaterialTheme.typography.titleLarge,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
            }

            // Big Timer & Progress
            Box(contentAlignment = Alignment.Center) {
                val initialDuration = when (val s = state) {
                    is TimerState.Preparing -> s.config.warmupSeconds
                    is TimerState.Working -> s.config.workSeconds
                    is TimerState.Resting -> {
                        if (s.currentRound == s.config.rounds) s.config.restBetweenSetsSeconds
                        else s.config.restSeconds
                    }
                    is TimerState.Paused -> {
                        val prev = s.previousState
                        when (prev) {
                            is TimerState.Preparing -> prev.config.warmupSeconds
                            is TimerState.Working -> prev.config.workSeconds
                            is TimerState.Resting -> {
                                if (prev.currentRound == prev.config.rounds) prev.config.restBetweenSetsSeconds
                                else prev.config.restSeconds
                            }
                            else -> 1
                        }
                    }
                    else -> 1
                }.toFloat().coerceAtLeast(1f)

                val progress by animateFloatAsState(
                    targetValue = state.remainingSeconds / initialDuration,
                    label = "TimerProgress"
                )

                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(300.dp),
                    strokeWidth = 12.dp,
                    color = contentColor,
                    trackColor = contentColor.copy(alpha = 0.2f)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val minutes = state.remainingSeconds / 60
                    val seconds = state.remainingSeconds % 60
                    Text(
                        text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = contentColor
                    )
                }
            }

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        viewModel.stop()
                        onWorkoutCancelled()
                    },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = contentColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                FloatingActionButton(
                    onClick = {
                        if (state is TimerState.Paused) viewModel.resume()
                        else viewModel.pause()
                    },
                    containerColor = contentColor,
                    contentColor = backgroundColor,
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(
                        if (state is TimerState.Paused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (state is TimerState.Paused) "Resume" else "Pause",
                        modifier = Modifier.size(40.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.skip() },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Skip",
                        tint = contentColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }

    // Handle Finished state
    LaunchedEffect(state) {
        if (state is TimerState.Finished) {
            // Optional: Show a summary or just go back
            delay(2000)
            onWorkoutCancelled()
        }
    }
}
