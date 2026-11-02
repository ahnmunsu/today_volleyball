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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val repository: VolleyballRepository
) : ViewModel() {

    val matchAlarmEnabled: StateFlow<Boolean> = settingsDataStore.matchAlarmEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val dailyAlarmEnabled: StateFlow<Boolean> = settingsDataStore.dailyAlarmEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val dailyAlarmHour: StateFlow<Int> = settingsDataStore.dailyAlarmHour
        .stateIn(viewModelScope, SharingStarted.Eagerly, 9)

    val dailyAlarmMinute: StateFlow<Int> = settingsDataStore.dailyAlarmMinute
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val reservedSchedules: StateFlow<List<Schedule>> = combine(
        repository.getSchedulesFlow(),
        settingsDataStore.scheduledAlarms
    ) { schedules, alarmIds ->
        schedules.filter { alarmIds.contains(it.gnum) }
            .sortedBy { it.date + " " + it.time }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleMatchAlarm(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setMatchAlarmEnabled(enabled)
        }
    }

    fun toggleDailyAlarm(enabled: Boolean, context: Context) {
        viewModelScope.launch {
            val alarmScheduler = AlarmScheduler(context)
            if (enabled) {
                alarmScheduler.scheduleDailyAlarm(dailyAlarmHour.value, dailyAlarmMinute.value)
            } else {
                alarmScheduler.cancelDailyAlarm()
            }
            settingsDataStore.setDailyAlarmEnabled(enabled)
        }
    }

    fun updateDailyAlarmTime(hour: Int, minute: Int, context: Context) {
        viewModelScope.launch {
            settingsDataStore.setDailyAlarmTime(hour, minute)
            if (dailyAlarmEnabled.value) {
                AlarmScheduler(context).scheduleDailyAlarm(hour, minute)
            }
            Toast.makeText(context, String.format("매일 알림 시간이 %02d:%02d로 변경되었습니다.", hour, minute), Toast.LENGTH_SHORT).show()
        }
    }

    fun cancelReservedAlarm(schedule: Schedule, context: Context) {
        viewModelScope.launch {
            AlarmScheduler(context).cancelMatchAlarm(schedule.gnum)
            settingsDataStore.toggleScheduledAlarm(schedule.gnum, false)
            Toast.makeText(context, "예약된 알림이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    fun syncData() {
        viewModelScope.launch {
            repository.fetchAndSaveTeams()
            repository.fetchAndSaveSchedules()
        }
    }
}
