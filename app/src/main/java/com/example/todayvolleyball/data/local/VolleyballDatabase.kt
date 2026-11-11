package com.example.todayvolleyball.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TeamEntity::class, ScheduleEntity::class, ScheduleChangeEntity::class, TeamRankEntity::class],
    version = 2,
    exportSchema = false
)
abstract class VolleyballDatabase : RoomDatabase() {
    abstract fun volleyballDao(): VolleyballDao
}

