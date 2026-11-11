package com.example.todayvolleyball.data.repository

import com.example.todayvolleyball.data.local.ScheduleEntity
import com.example.todayvolleyball.data.local.TeamEntity
import com.example.todayvolleyball.data.local.TeamRankEntity
import com.example.todayvolleyball.data.local.VolleyballDao
import com.example.todayvolleyball.data.remote.KovoApi
import com.example.todayvolleyball.domain.model.Schedule
import com.example.todayvolleyball.domain.model.Team
import com.example.todayvolleyball.domain.model.TeamRank
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

    override suspend fun fetchAndSaveTeamRanks(leagueCode: String, seasonCode: String) {
        try {
            val menResponse = api.getTeamRank(seasonCode = seasonCode, leagueCode = leagueCode, gender = "1")
            val womenResponse = api.getTeamRank(seasonCode = seasonCode, leagueCode = leagueCode, gender = "2")

            val menEntities = menResponse.payload?.map { p ->
                TeamRankEntity(
                    leagueCode = leagueCode,
                    gender = "1",
                    rank = p.rank,
                    tcode = p.tcode ?: "",
                    tsname = p.tsname ?: "",
                    winp = p.winp,
                    win = p.win,
                    lost = p.lost,
                    swin = p.swin,
                    slost = p.slost,
                    point = p.point,
                    lpoint = p.lpoint,
                    con = p.con ?: ""
                )
            } ?: emptyList()

            val womenEntities = womenResponse.payload?.map { p ->
                TeamRankEntity(
                    leagueCode = leagueCode,
                    gender = "2",
                    rank = p.rank,
                    tcode = p.tcode ?: "",
                    tsname = p.tsname ?: "",
                    winp = p.winp,
                    win = p.win,
                    lost = p.lost,
                    swin = p.swin,
                    slost = p.slost,
                    point = p.point,
                    lpoint = p.lpoint,
                    con = p.con ?: ""
                )
            } ?: emptyList()

            dao.deleteTeamRanksByLeague(leagueCode)
            dao.insertTeamRanks(womenEntities + menEntities)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getTeamRanksFlow(leagueCode: String): Flow<List<TeamRank>> {
        return dao.getTeamRanksFlow(leagueCode).map { entities ->
            val teams = dao.getTeams().associateBy { it.code }
            entities.map { entity ->
                val team = teams[entity.tcode]
                val teamName = team?.name ?: entity.tsname
                val teamShortName = toDisplayTeamName(entity.tsname, team)
                val teamColor = team?.primaryColor ?: getTeamColorHex(teamName.ifBlank { entity.tsname })

                val setRatioStr = calculateRatio(entity.swin.toDouble(), entity.slost.toDouble())
                val scoreRatioStr = calculateRatio(entity.point.toDouble(), entity.lpoint.toDouble())

                TeamRank(
                    rank = entity.rank,
                    teamCode = entity.tcode,
                    teamName = teamName,
                    teamShortName = teamShortName,
                    teamColor = teamColor,
                    points = entity.winp,
                    wins = entity.win,
                    losses = entity.lost,
                    setsWon = entity.swin,
                    setsLost = entity.slost,
                    setRatio = setRatioStr,
                    scorePoints = entity.point,
                    scoreConceded = entity.lpoint,
                    scoreRatio = scoreRatioStr,
                    streak = entity.con,
                    leagueCode = entity.leagueCode,
                    isMen = entity.gender == "1"
                )
            }
        }
    }

    private fun calculateRatio(won: Double, lost: Double): String {
        return if (lost > 0) {
            String.format(java.util.Locale.US, "%.2f", won / lost)
        } else if (won > 0) {
            "MAX"
        } else {
            "0.00"
        }
    }

    private fun toDisplayTeamName(tsname: String, teamEntity: TeamEntity?): String {
        val fullName = if (teamEntity != null && teamEntity.shortName.isNotBlank()) teamEntity.shortName else tsname
        return fullName.toCustomShortName()
    }

    private fun getTeamColorHex(teamName: String, apiColor: String? = null): String {
        if (apiColor != null && apiColor.startsWith("#")) {
            return apiColor
        }
        return when {
            teamName.contains("대한항공") || teamName.contains("KOREAN AIR") -> "#051766"
            teamName.contains("현대캐피탈") || teamName.contains("HYUNDAI CAPITAL") -> "#0082ca"
            teamName.contains("우리카드") || teamName.contains("우리WON") || teamName.contains("WOORICARD") -> "#0055a5"
            teamName.contains("KB") -> "#e5a100"
            teamName.contains("한국전력") || teamName.contains("KEPCO") -> "#002d62"
            teamName.contains("OK") -> "#ff6600"
            teamName.contains("삼성화재") || teamName.contains("SAMSUNG") -> "#004b97"
            teamName.contains("흥국생명") || teamName.contains("HEUNGKUK") -> "#c408a1"
            teamName.contains("IBK") || teamName.contains("INDUSTRIAL BANK") -> "#0055a5"
            teamName.contains("정관장") || teamName.contains("JUNG KWAN JANG") -> "#d91833"
            teamName.contains("SOOP") -> "#0545b1"
            teamName.contains("도로공사") || teamName.contains("하이패스") || teamName.contains("KOREA EXPRESSWAY") -> "#081855"
            teamName.contains("GS") -> "#0b91a7"
            teamName.contains("현대건설") || teamName.contains("HYUNDAI E&C") -> "#17193e"
            else -> "#333333"
        }
    }

    private fun String.toCustomShortName(): String {
        return when {
            contains("대한항공") || contains("KOREAN AIR") -> "항공"
            contains("현대캐피탈") || contains("HYUNDAI CAPITAL") -> "현캐"
            contains("우리카드") || contains("우리WON") || contains("WOORICARD") -> "우리"
            contains("KB") -> "KB"
            contains("한국전력") || contains("KEPCO") -> "한전"
            contains("OK") -> "OK"
            contains("삼성화재") || contains("SAMSUNG") -> "삼성"
            contains("흥국생명") || contains("HEUNGKUK") -> "흥국"
            contains("IBK") || contains("INDUSTRIAL BANK") -> "IBK"
            contains("정관장") || contains("JUNG KWAN JANG") -> "홍삼"
            contains("SOOP") -> "숲"
            contains("도로공사") || contains("하이패스") || contains("KOREA EXPRESSWAY") -> "도공"
            contains("GS") -> "GS"
            contains("현대건설") || contains("HYUNDAI E&C") -> "현건"
            else -> this
        }
    }
}

