package com.example.todayvolleyball.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VolleyballDao {
    @Query("SELECT * FROM teams")
    suspend fun getTeams(): List<TeamEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeams(teams: List<TeamEntity>)

    @Query("SELECT * FROM schedules")
    suspend fun getSchedules(): List<ScheduleEntity>

    @Query("SELECT * FROM schedules")
    fun getSchedulesFlow(): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE date = :date")
    suspend fun getSchedulesByDate(date: String): List<ScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>)

    @Query("SELECT * FROM schedule_changes WHERE isRead = 0")
    suspend fun getUnreadChanges(): List<ScheduleChangeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChange(change: ScheduleChangeEntity)

    @Query("UPDATE schedule_changes SET isRead = 1 WHERE id = :id")
    suspend fun markChangeAsRead(id: Int)

    @Query("SELECT * FROM team_ranks WHERE leagueCode = :leagueCode ORDER BY gender DESC, rank ASC")
    fun getTeamRanksFlow(leagueCode: String): Flow<List<TeamRankEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeamRanks(ranks: List<TeamRankEntity>)

    @Query("DELETE FROM team_ranks WHERE leagueCode = :leagueCode")
    suspend fun deleteTeamRanksByLeague(leagueCode: String)
}

