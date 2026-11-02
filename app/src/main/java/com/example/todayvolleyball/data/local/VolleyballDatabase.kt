package com.example.todayvolleyball.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TeamEntity::class, ScheduleEntity::class, ScheduleChangeEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VolleyballDatabase : RoomDatabase() {
    abstract fun volleyballDao(): VolleyballDao
}
