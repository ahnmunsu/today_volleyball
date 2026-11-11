package com.example.todayvolleyball.ui.screen

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todayvolleyball.alarm.AlarmScheduler
import com.example.todayvolleyball.data.local.SettingsDataStore
import com.example.todayvolleyball.domain.model.Schedule
import com.example.todayvolleyball.domain.repository.VolleyballRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: VolleyballRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val schedules: StateFlow<List<Schedule>> = repository.getSchedulesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val scheduledAlarms: StateFlow<Set<Int>> = settingsDataStore.scheduledAlarms
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val savedLeagueFilter: StateFlow<String> = settingsDataStore.savedLeagueFilter
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = "ALL"
        )

    val selectedTeams: StateFlow<Set<String>> = settingsDataStore.selectedTeams
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptySet()
        )

    val savedTeamFilter: StateFlow<String?> = settingsDataStore.savedTeamFilter
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    init {
        refreshData()
    }

    fun fetchData(context: Context? = null) {
        refreshData(context)
    }

    fun refreshData(context: Context? = null) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.fetchAndSaveTeams()
                repository.fetchAndSaveSchedules()
                if (context != null) {
                    val currentSchedules = repository.getSchedulesFlow().first()
                    com.example.todayvolleyball.worker.SyncWorker.checkAndNotifyFinishedMatches(
                        context.applicationContext,
                        currentSchedules,
                        settingsDataStore
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun updateLeagueFilter(league: String) {
        viewModelScope.launch {
            settingsDataStore.saveLeagueFilter(league)
        }
    }

    fun updateSelectedTeams(teams: Set<String>) {
        viewModelScope.launch {
            settingsDataStore.saveSelectedTeams(teams)
        }
    }

    fun toggleTeamFilter(teamName: String) {
        viewModelScope.launch {
            val current = selectedTeams.value
            val updated = if (current.contains(teamName)) {
                current - teamName
            } else {
                current + teamName
            }
            settingsDataStore.saveSelectedTeams(updated)
        }
    }

    fun clearTeamFilter() {
        viewModelScope.launch {
            settingsDataStore.saveSelectedTeams(emptySet())
        }
    }

    fun updateTeamFilter(team: String?) {
        viewModelScope.launch {
            settingsDataStore.saveTeamFilter(team)
        }
    }

    fun toggleAlarm(schedule: Schedule, context: Context, hoursBefore: Int = 1) {
        viewModelScope.launch {
            val current = scheduledAlarms.value
            val isCurrentlyScheduled = current.contains(schedule.gnum)
            val alarmScheduler = AlarmScheduler(context)

            if (isCurrentlyScheduled) {
                alarmScheduler.cancelMatchAlarm(schedule.gnum)
                settingsDataStore.toggleScheduledAlarm(schedule.gnum, false)
                Toast.makeText(context, "알림 예약이 취소되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                if (!schedule.canScheduleAlarm(hoursBefore)) {
                    Toast.makeText(context, "경기 시작 1시간 이내에는 알림을 예약할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val success = alarmScheduler.scheduleMatchAlarm(schedule, hoursBefore)
                if (success) {
                    settingsDataStore.toggleScheduledAlarm(schedule.gnum, true)
                    Toast.makeText(
                        context,
                        "경기 시작 ${hoursBefore}시간 전 알림이 예약되었습니다!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(context, "알림 예약에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
