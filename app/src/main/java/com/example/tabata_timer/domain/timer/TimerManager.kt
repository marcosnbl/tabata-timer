package com.example.tabata_timer.domain.timer

import com.example.tabata_timer.domain.audio.SoundPlayer
import com.example.tabata_timer.domain.model.TimerState
import com.example.tabata_timer.domain.model.WorkoutConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerManager @Inject constructor(
    private val soundPlayer: SoundPlayer
) {
    private val _timerState = MutableStateFlow<TimerState>(TimerState.Idle())
    val timerState = _timerState.asStateFlow()

    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var skipRequested = false

    fun startWorkout(config: WorkoutConfig) {
        timerJob?.cancel()
        skipRequested = false
        timerJob = scope.launch {
            runWorkout(config)
        }
    }

    fun pause() {
        val current = _timerState.value
        if (current !is TimerState.Paused && current !is TimerState.Idle && current !is TimerState.Finished) {
            _timerState.value = TimerState.Paused(current)
            timerJob?.cancel()
        }
    }

    fun resume() {
        val current = _timerState.value
        if (current is TimerState.Paused) {
            val previous = current.previousState
            _timerState.value = previous
            skipRequested = false
            timerJob = scope.launch {
                resumeWorkout(previous)
            }
        }
    }

    fun skip() {
        skipRequested = true
    }

    fun stop() {
        timerJob?.cancel()
        _timerState.value = TimerState.Idle()
        soundPlayer.stop()
    }

    private suspend fun runWorkout(
        config: WorkoutConfig,
        startSet: Int = 1,
        startRound: Int = 1,
        startState: TimerState? = null
    ) {
        // Warmup (only if starting from beginning)
        if (startSet == 1 && startRound == 1 && startState == null && config.warmupSeconds > 0) {
            tick(TimerState.Preparing(config, config.warmupSeconds, 1, 1), "Get ready")
        }

        // Handle resuming from a specific state if provided
        var resumeState = startState

        for (set in startSet..config.sets) {
            val roundRange = if (set == startSet) startRound..config.rounds else 1..config.rounds
            for (round in roundRange) {
                // Work
                if (resumeState == null || resumeState is TimerState.Working) {
                    val seconds = (resumeState as? TimerState.Working)?.remainingSeconds ?: config.workSeconds
                    tick(TimerState.Working(config, seconds, round, set), if (resumeState == null) "Work" else null)
                    resumeState = null
                }

                // Rest after round
                if (round < config.rounds) {
                    if (resumeState == null || resumeState is TimerState.Resting) {
                        val seconds = (resumeState as? TimerState.Resting)?.remainingSeconds ?: config.restSeconds
                        tick(TimerState.Resting(config, seconds, round, set), if (resumeState == null) "Rest" else null)
                        resumeState = null
                    }
                }
            }

            // Rest between sets
            if (set < config.sets && config.restBetweenSetsSeconds > 0) {
                if (resumeState == null || (resumeState is TimerState.Resting && resumeState.currentRound == config.rounds)) {
                    val seconds = (resumeState as? TimerState.Resting)?.remainingSeconds ?: config.restBetweenSetsSeconds
                    tick(TimerState.Resting(config, seconds, config.rounds, set), if (resumeState == null) "Set complete. Rest" else null)
                    resumeState = null
                }
            }
        }

        // Cooldown
        if (config.cooldownSeconds > 0) {
            if (resumeState == null || (resumeState is TimerState.Resting && resumeState.currentRound == config.rounds && resumeState.currentSet == config.sets)) {
                val seconds = (resumeState as? TimerState.Resting)?.remainingSeconds ?: config.cooldownSeconds
                tick(TimerState.Resting(config, seconds, config.rounds, config.sets), if (resumeState == null) "Cooldown" else null)
                resumeState = null
            }
        }

        _timerState.value = TimerState.Finished
        soundPlayer.playSound("Workout finished")
    }

    private suspend fun resumeWorkout(state: TimerState) {
        val config = state.config ?: return
        runWorkout(
            config = config,
            startSet = state.currentSet,
            startRound = state.currentRound,
            startState = state
        )
    }

    private suspend fun tick(state: TimerState, startMessage: String?) {
        skipRequested = false
        var remaining = state.remainingSeconds
        startMessage?.let { soundPlayer.playSound(it) }

        while (remaining > 0 && !skipRequested) {
            val currentState = when (state) {
                is TimerState.Preparing -> state.copy(remainingSeconds = remaining)
                is TimerState.Working -> state.copy(remainingSeconds = remaining)
                is TimerState.Resting -> state.copy(remainingSeconds = remaining)
                else -> state
            }
            _timerState.value = currentState

            if (remaining in 1..3) {
                soundPlayer.playSound(remaining.toString())
            }

            delay(1000)
            remaining--
        }
        skipRequested = false
    }
}
