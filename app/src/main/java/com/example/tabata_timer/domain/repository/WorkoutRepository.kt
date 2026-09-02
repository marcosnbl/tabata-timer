package com.example.tabata_timer.domain.repository

import com.example.tabata_timer.domain.model.WorkoutConfig
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun getAllWorkouts(): Flow<List<WorkoutConfig>>
    suspend fun getWorkoutById(id: Long): WorkoutConfig?
    suspend fun insertWorkout(workout: WorkoutConfig)
    suspend fun deleteWorkout(workout: WorkoutConfig)
}
