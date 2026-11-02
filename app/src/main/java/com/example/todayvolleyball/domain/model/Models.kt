package com.example.todayvolleyball.domain.model

data class Team(
    val code: String,
    val name: String,
    val shortName: String,
    val isMen: Boolean,
    val primaryColor: String
)

data class Schedule(
    val gnum: Int,
    val date: String,
    val time: String,
    val homeTeamName: String,
    val homeTeamShortName: String,
    val homeTeamColor: String,
    val awayTeamName: String,
    val awayTeamShortName: String,
    val awayTeamColor: String,
    val homePoint: Int,
    val awayPoint: Int,
    val place: String,
    val isMen: Boolean,
    val isFinished: Boolean
)
