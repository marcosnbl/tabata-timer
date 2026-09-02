package com.example.tabata_timer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.tabata_timer.data.local.dao.WorkoutDao
import com.example.tabata_timer.data.local.entities.WorkoutEntity

@Database(entities = [WorkoutEntity::class], version = 1, exportSchema = false)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract val workoutDao: WorkoutDao

    companion object {
        const val DATABASE_NAME = "tabata_timer_db"
    }
}
