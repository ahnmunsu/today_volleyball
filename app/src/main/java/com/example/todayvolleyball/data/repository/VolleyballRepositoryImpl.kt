package com.example.todayvolleyball.data.repository

import com.example.todayvolleyball.data.local.ScheduleEntity
import com.example.todayvolleyball.data.local.TeamEntity
import com.example.todayvolleyball.data.local.VolleyballDao
import com.example.todayvolleyball.data.remote.KovoApi
import com.example.todayvolleyball.domain.model.Schedule
import com.example.todayvolleyball.domain.model.Team
import com.example.todayvolleyball.domain.repository.VolleyballRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class VolleyballRepositoryImpl @Inject constructor(
    private val api: KovoApi,
    private val dao: VolleyballDao
) : VolleyballRepository {

    override suspend fun fetchAndSaveTeams() {
        try {
            val response = api.getTeams()
            val entities = response.payload?.mapNotNull { payload ->
                val teamInfo = payload.team ?: return@mapNotNull null
                val translation = teamInfo.translations?.firstOrNull { it.languageCode == "ko" } ?: return@mapNotNull null
                val lightThemeColor = payload.themes?.firstOrNull { it.type == "LIGHT" }?.primaryColor 
                    ?: payload.themes?.firstOrNull()?.primaryColor
                val colorHex = getTeamColorHex(translation.name ?: "", lightThemeColor)

                TeamEntity(
                    code = teamInfo.code ?: "",
                    gender = teamInfo.gender ?: "",
                    name = translation.name ?: "",
                    shortName = translation.shortName ?: "",
                    primaryColor = colorHex
                )
            } ?: emptyList()
            dao.insertTeams(entities)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun fetchAndSaveSchedules() {
        try {
            val response = api.getSchedules()
            val entities = response.payload?.content?.map { content ->
                ScheduleEntity(
                    gnum = content.gnum,
                    seasonCode = content.seasonCode ?: "",
                    leagueCode = content.leagueCode ?: "",
                    gender = content.gender ?: "",
                    homeCode = content.hcode ?: "",
                    homeName = content.hname ?: "",
                    awayCode = content.acode ?: "",
                    awayName = content.aname ?: "",
                    date = content.gdate ?: "",
                    time = content.gstime ?: "",
                    place = content.place ?: "",
                    homePoint = content.hspoint,
                    awayPoint = content.aspoint,
                    homeSets = listOf(content.hs1point, content.hs2point, content.hs3point, content.hs4point, content.hs5point).joinToString(","),
                    awaySets = listOf(content.as1point, content.as2point, content.as3point, content.as4point, content.as5point).joinToString(",")
                )
            } ?: emptyList()

            dao.insertSchedules(entities)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getSchedulesFlow(): Flow<List<Schedule>> {
        return dao.getSchedulesFlow().map { entities ->
            val teams = dao.getTeams().associateBy { it.code }
            entities.map { entity ->
                val homeTeam = teams[entity.homeCode]
                val awayTeam = teams[entity.awayCode]

                val homeColor = homeTeam?.primaryColor ?: getTeamColorHex(entity.homeName)
                val awayColor = awayTeam?.primaryColor ?: getTeamColorHex(entity.awayName)

                Schedule(
                    gnum = entity.gnum,
                    date = entity.date,
                    time = entity.time,
                    homeTeamName = entity.homeName,
                    homeTeamShortName = entity.homeName.toCustomShortName(),
                    homeTeamColor = homeColor,
                    awayTeamName = entity.awayName,
                    awayTeamShortName = entity.awayName.toCustomShortName(),
                    awayTeamColor = awayColor,
                    homePoint = entity.homePoint,
                    awayPoint = entity.awayPoint,
                    place = entity.place,
                    isMen = entity.gender == "1",
                    isFinished = entity.homePoint == 3 || entity.awayPoint == 3
                )
            }
        }
    }

    override suspend fun getTeams(): List<Team> {
        return dao.getTeams().map {
            Team(
                code = it.code,
                name = it.name,
                shortName = it.shortName,
                isMen = it.gender == "MALE",
                primaryColor = it.primaryColor
            )
        }
    }

    private fun getTeamColorHex(teamName: String, apiColor: String? = null): String {
        if (apiColor != null && apiColor.startsWith("#")) {
            return apiColor
        }
        return when {
            teamName.contains("대한항공") -> "#051766"
            teamName.contains("현대캐피탈") -> "#0082ca"
            teamName.contains("우리카드") || teamName.contains("우리WON") -> "#0055a5"
            teamName.contains("KB") -> "#e5a100"
            teamName.contains("한국전력") -> "#002d62"
            teamName.contains("OK") -> "#ff6600"
            teamName.contains("삼성화재") -> "#004b97"
            teamName.contains("흥국생명") -> "#c408a1"
            teamName.contains("IBK") -> "#0055a5"
            teamName.contains("정관장") -> "#d91833"
            teamName.contains("SOOP") -> "#0545b1"
            teamName.contains("도로공사") || teamName.contains("하이패스") -> "#081855"
            teamName.contains("GS") -> "#0b91a7"
            teamName.contains("현대건설") -> "#17193e"
            else -> "#333333"
        }
    }

    private fun String.toCustomShortName(): String {
        return when {
            contains("대한항공") -> "항공"
            contains("현대캐피탈") -> "현캐"
            contains("우리카드") || contains("우리WON") -> "우리"
            contains("KB") -> "KB"
            contains("한국전력") -> "한전"
            contains("OK") -> "OK"
            contains("삼성화재") -> "삼성"
            contains("흥국생명") -> "흥국"
            contains("IBK") -> "IBK"
            contains("정관장") -> "홍삼"
            contains("SOOP") -> "숲"
            contains("도로공사") || contains("하이패스") -> "도공"
            contains("GS") -> "GS"
            contains("현대건설") -> "현건"
            else -> this
        }
    }
}
