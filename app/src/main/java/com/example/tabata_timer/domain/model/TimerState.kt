package com.example.tabata_timer.domain.model

sealed interface TimerState {
    val remainingSeconds: Int
    val currentRound: Int
    val currentSet: Int
    val config: WorkoutConfig?

    data class Idle(
        override val config: WorkoutConfig? = null,
        override val remainingSeconds: Int = 0,
        override val currentRound: Int = 0,
        override val currentSet: Int = 0
    ) : TimerState

    data class Preparing(
        override val config: WorkoutConfig,
        override val remainingSeconds: Int,
        override val currentRound: Int,
        override val currentSet: Int
    ) : TimerState

    data class Working(
        override val config: WorkoutConfig,
        override val remainingSeconds: Int,
        override val currentRound: Int,
        override val currentSet: Int
    ) : TimerState

    data class Resting(
        override val config: WorkoutConfig,
        override val remainingSeconds: Int,
        override val currentRound: Int,
        override val currentSet: Int
    ) : TimerState

    data class Paused(
        val previousState: TimerState,
        override val config: WorkoutConfig? = previousState.config,
        override val remainingSeconds: Int = previousState.remainingSeconds,
        override val currentRound: Int = previousState.currentRound,
        override val currentSet: Int = previousState.currentSet
    ) : TimerState

    object Finished : TimerState {
        override val remainingSeconds = 0
        override val currentRound = 0
        override val currentSet = 0
        override val config = null
    }
}
