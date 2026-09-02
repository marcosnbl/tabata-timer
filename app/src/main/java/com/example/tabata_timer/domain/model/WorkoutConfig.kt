package com.example.tabata_timer.domain.model

data class WorkoutConfig(
    val id: Long = 0,
    val name: String,
    val color: Int, // ARGB format
    val warmupSeconds: Int,
    val workSeconds: Int,
    val restSeconds: Int,
    val rounds: Int,
    val sets: Int,
    val restBetweenSetsSeconds: Int,
    val cooldownSeconds: Int
) {
    val totalTimeSeconds: Int
        get() = warmupSeconds + 
                (sets * (rounds * (workSeconds + restSeconds) - restSeconds)) + 
                ((sets - 1) * restBetweenSetsSeconds) + 
                cooldownSeconds
}
