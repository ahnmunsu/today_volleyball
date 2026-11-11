package com.example.todayvolleyball.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey val code: String,
    val gender: String, // MEN, WOMEN
    val name: String,
    val shortName: String,
    val primaryColor: String = "#333333"
)

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey val gnum: Int,
    val seasonCode: String,
    val leagueCode: String,
    val gender: String, // "1", "2"
    val homeCode: String,
    val homeName: String,
    val awayCode: String,
    val awayName: String,
    val date: String,
    val time: String,
    val place: String,
    val homePoint: Int,
    val awayPoint: Int,
    val homeSets: String, // e.g., "25,25,25"
    val awaySets: String  // e.g., "20,20,20"
)

@Entity(tableName = "schedule_changes")
data class ScheduleChangeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gnum: Int,
    val message: String,
    val date: String,
    val isRead: Boolean = false
)

@Entity(tableName = "team_ranks", primaryKeys = ["leagueCode", "gender", "rank"])
data class TeamRankEntity(
    val leagueCode: String,
    val gender: String, // "1": Men, "2": Women
    val rank: Int,
    val tcode: String,
    val tsname: String,
    val winp: Double,
    val win: Int,
    val lost: Int,
    val swin: Int,
    val slost: Int,
    val point: Int,
    val lpoint: Int,
    val con: String
)

