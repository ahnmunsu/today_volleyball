package com.example.todayvolleyball.domain.model

import java.time.LocalDateTime

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
) {
    fun hasPassed(now: LocalDateTime = LocalDateTime.now()): Boolean {
        return try {
            val dateParts = date.split("-")
            val timeParts = time.split(":")
            if (dateParts.size == 3 && timeParts.size >= 2) {
                val matchDateTime = LocalDateTime.of(
                    dateParts[0].toInt(),
                    dateParts[1].toInt(),
                    dateParts[2].toInt(),
                    timeParts[0].toInt(),
                    timeParts[1].toInt()
                )
                !matchDateTime.isAfter(now)
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }
}
