package com.example.tabata_timer.ui.execution

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
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
import com.example.tabata_timer.ui.theme.*
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
            is TimerState.Preparing -> PrepColor
            is TimerState.Working -> WorkColor
            is TimerState.Resting -> RestColor
            is TimerState.Paused -> PauseColor
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = when (state) {
                        is TimerState.Preparing -> "GET READY"
                        is TimerState.Working -> "WORK"
                        is TimerState.Resting -> "REST"
                        is TimerState.Paused -> "PAUSED"
                        is TimerState.Finished -> "FINISHED"
                        else -> ""
                    },
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 6.sp
                    ),
                    color = contentColor
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                if (state !is TimerState.Idle && state !is TimerState.Finished) {
                    Surface(
                        color = contentColor.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Text(
                            text = "ROUND ${state.currentRound}/${state.config?.rounds ?: 0}  •  SET ${state.currentSet}/${state.config?.sets ?: 0}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = contentColor,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
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
                    modifier = Modifier.size(320.dp),
                    strokeWidth = 14.dp,
                    color = contentColor,
                    trackColor = contentColor.copy(alpha = 0.2f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val minutes = state.remainingSeconds / 60
                    val seconds = state.remainingSeconds % 60
                    Text(
                        text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 100.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-2).sp
                        ),
                        color = contentColor
                    )
                }
            }

            // Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = {
                        viewModel.stop()
                        onWorkoutCancelled()
                    },
                    color = contentColor.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = contentColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                LargeFloatingActionButton(
                    onClick = {
                        if (state is TimerState.Paused) viewModel.resume()
                        else viewModel.pause()
                    },
                    containerColor = contentColor,
                    contentColor = backgroundColor,
                    shape = CircleShape,
                    modifier = Modifier.size(100.dp)
                ) {
                    Icon(
                        if (state is TimerState.Paused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (state is TimerState.Paused) "Resume" else "Pause",
                        modifier = Modifier.size(48.dp)
                    )
                }

                Surface(
                    onClick = { viewModel.skip() },
                    color = contentColor.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
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
