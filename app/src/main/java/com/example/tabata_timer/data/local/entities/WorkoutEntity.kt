package com.example.tabata_timer.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tabata_timer.domain.model.WorkoutConfig

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Int,
    val warmupSeconds: Int,
    val workSeconds: Int,
    val restSeconds: Int,
    val rounds: Int,
    val sets: Int,
    val restBetweenSetsSeconds: Int,
    val cooldownSeconds: Int
)

fun WorkoutEntity.toDomain() = WorkoutConfig(
    id, name, color, warmupSeconds, workSeconds, restSeconds, 
    rounds, sets, restBetweenSetsSeconds, cooldownSeconds
)

fun WorkoutConfig.toEntity() = WorkoutEntity(
    id, name, color, warmupSeconds, workSeconds, restSeconds, 
    rounds, sets, restBetweenSetsSeconds, cooldownSeconds
)
