package com.example.tabata_timer.ui.config

data class WorkoutConfigUiState(
    val name: String = "My Tabata",
    val color: Int = 0xFF2196F3.toInt(), // Default blue
    val warmupSeconds: Int = 10,
    val workSeconds: Int = 20,
    val restSeconds: Int = 10,
    val rounds: Int = 8,
    val sets: Int = 1,
    val restBetweenSetsSeconds: Int = 60,
    val cooldownSeconds: Int = 10,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false
) {
    val totalTimeSeconds: Int
        get() = warmupSeconds + 
                (sets * (rounds * (workSeconds + restSeconds) - restSeconds)) + 
                ((sets - 1) * restBetweenSetsSeconds) + 
                cooldownSeconds
}
