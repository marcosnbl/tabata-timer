package com.example.tabata_timer.data.repository

import com.example.tabata_timer.data.local.dao.WorkoutDao
import com.example.tabata_timer.data.local.entities.toDomain
import com.example.tabata_timer.data.local.entities.toEntity
import com.example.tabata_timer.domain.model.WorkoutConfig
import com.example.tabata_timer.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WorkoutRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao
) : WorkoutRepository {

    override fun getAllWorkouts(): Flow<List<WorkoutConfig>> =
        workoutDao.getAllWorkouts().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getWorkoutById(id: Long): WorkoutConfig? =
        workoutDao.getWorkoutById(id)?.toDomain()

    override suspend fun insertWorkout(workout: WorkoutConfig) {
        workoutDao.insertWorkout(workout.toEntity())
    }

    override suspend fun deleteWorkout(workout: WorkoutConfig) {
        workoutDao.deleteWorkout(workout.toEntity())
    }
}
