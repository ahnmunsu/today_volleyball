package com.example.todayvolleyball.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    companion object {
        val ENABLE_MATCH_ALARM = booleanPreferencesKey("enable_match_alarm")
        val ENABLE_MATCH_RESULT_ALARM = booleanPreferencesKey("enable_match_result_alarm")
        val MATCH_ALARM_HOURS_BEFORE = booleanPreferencesKey("match_alarm_hours_before")
        val ENABLE_DAILY_ALARM = booleanPreferencesKey("enable_daily_alarm")
        val DAILY_ALARM_HOUR = intPreferencesKey("daily_alarm_hour")
        val DAILY_ALARM_MINUTE = intPreferencesKey("daily_alarm_minute")
        val SELECTED_TEAMS = stringSetPreferencesKey("selected_teams")
        val SCHEDULED_ALARMS = stringSetPreferencesKey("scheduled_alarms")
        val SAVED_LEAGUE_FILTER = stringPreferencesKey("saved_league_filter")
        val SAVED_TEAM_FILTER = stringPreferencesKey("saved_team_filter")
        val NOTIFIED_FINISHED_MATCHES = stringSetPreferencesKey("notified_finished_matches")
    }

    val matchAlarmEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[ENABLE_MATCH_ALARM] ?: true }

    val matchResultAlarmEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[ENABLE_MATCH_RESULT_ALARM] ?: true }

    val dailyAlarmEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[ENABLE_DAILY_ALARM] ?: false }

    val dailyAlarmHour: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[DAILY_ALARM_HOUR] ?: 9 }

    val dailyAlarmMinute: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[DAILY_ALARM_MINUTE] ?: 0 }

    val selectedTeams: Flow<Set<String>> = context.dataStore.data
        .map { preferences -> preferences[SELECTED_TEAMS] ?: emptySet() }

    val scheduledAlarms: Flow<Set<Int>> = context.dataStore.data
        .map { preferences ->
            preferences[SCHEDULED_ALARMS]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
        }

    val notifiedFinishedMatches: Flow<Set<Int>> = context.dataStore.data
        .map { preferences ->
            preferences[NOTIFIED_FINISHED_MATCHES]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
        }

    val savedLeagueFilter: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[SAVED_LEAGUE_FILTER] ?: "ALL" }

    val savedTeamFilter: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[SAVED_TEAM_FILTER] }

    suspend fun markMatchFinishedNotified(gnum: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[NOTIFIED_FINISHED_MATCHES] ?: emptySet()
            preferences[NOTIFIED_FINISHED_MATCHES] = current + gnum.toString()
        }
    }

    suspend fun setMatchAlarmEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENABLE_MATCH_ALARM] = enabled
        }
    }

    suspend fun setMatchResultAlarmEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENABLE_MATCH_RESULT_ALARM] = enabled
        }
    }

    suspend fun setDailyAlarmEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENABLE_DAILY_ALARM] = enabled
        }
    }

    suspend fun setDailyAlarmTime(hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[DAILY_ALARM_HOUR] = hour
            preferences[DAILY_ALARM_MINUTE] = minute
        }
    }

    suspend fun toggleScheduledAlarm(gnum: Int, isScheduled: Boolean) {
        context.dataStore.edit { preferences ->
            val current = preferences[SCHEDULED_ALARMS] ?: emptySet()
            val updated = if (isScheduled) {
                current + gnum.toString()
            } else {
                current - gnum.toString()
            }
            preferences[SCHEDULED_ALARMS] = updated
        }
    }

    suspend fun removeScheduledAlarms(gnums: Collection<Int>) {
        if (gnums.isEmpty()) return
        context.dataStore.edit { preferences ->
            val current = preferences[SCHEDULED_ALARMS] ?: emptySet()
            val removeSet = gnums.map { it.toString() }.toSet()
            preferences[SCHEDULED_ALARMS] = current - removeSet
        }
    }

    suspend fun saveLeagueFilter(league: String) {
        context.dataStore.edit { preferences ->
            preferences[SAVED_LEAGUE_FILTER] = league
        }
    }

    suspend fun saveTeamFilter(team: String?) {
        context.dataStore.edit { preferences ->
            if (team.isNullOrEmpty()) {
                preferences.remove(SAVED_TEAM_FILTER)
            } else {
                preferences[SAVED_TEAM_FILTER] = team
            }
        }
    }
}
