package com.example.todayvolleyball.data.remote

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KovoApi {
    @GET("sites")
    suspend fun getTeams(
        @Query("type") type: String = "CLUB",
        @Header("accept") accept: String = "application/json",
        @Header("origin") origin: String = "https://kovo.co.kr",
        @Header("referer") referer: String = "https://kovo.co.kr/",
        @Header("x-service-name") serviceName: String = "user"
    ): TeamResponse

    @GET("stat/game-schedule")
    suspend fun getSchedules(
        @Query("gcode") gcode: String = "001",
        @Query("seasonCode") seasonCode: String = "023",
        @Query("leagueCode") leagueCode: String = "201",
        @Header("accept") accept: String = "application/json",
        @Header("origin") origin: String = "https://kovo.co.kr",
        @Header("referer") referer: String = "https://kovo.co.kr/",
        @Header("x-service-name") serviceName: String = "user"
    ): ScheduleResponse

    @GET("stat/league/team-rank")
    suspend fun getTeamRank(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 30,
        @Query("seasonCode") seasonCode: String = "023",
        @Query("leagueCode") leagueCode: String = "201",
        @Query("gender") gender: String = "1",
        @Query("round") round: Int = 0,
        @Header("accept") accept: String = "application/json",
        @Header("origin") origin: String = "https://kovo.co.kr",
        @Header("referer") referer: String = "https://kovo.co.kr/",
        @Header("x-service-name") serviceName: String = "user"
    ): TeamRankResponse
}

