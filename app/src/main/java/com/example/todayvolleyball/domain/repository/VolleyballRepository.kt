package com.example.todayvolleyball.domain.repository

import com.example.todayvolleyball.domain.model.Schedule
import com.example.todayvolleyball.domain.model.Team
import com.example.todayvolleyball.domain.model.TeamRank
import kotlinx.coroutines.flow.Flow

interface VolleyballRepository {
    suspend fun fetchAndSaveTeams()
    suspend fun fetchAndSaveSchedules()
    fun getSchedulesFlow(): Flow<List<Schedule>>
    suspend fun getTeams(): List<Team>
    suspend fun fetchAndSaveTeamRanks(leagueCode: String = "201", seasonCode: String = "023")
    fun getTeamRanksFlow(leagueCode: String): Flow<List<TeamRank>>
}

