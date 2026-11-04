package com.example.todayvolleyball.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todayvolleyball.alarm.AlarmReceiver
import com.example.todayvolleyball.data.local.SettingsDataStore
import com.example.todayvolleyball.domain.repository.VolleyballRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: VolleyballRepository,
    private val settingsDataStore: SettingsDataStore
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            repository.fetchAndSaveTeams()
            repository.fetchAndSaveSchedules()
            
            val schedules = repository.getSchedulesFlow().first()
            val now = LocalDateTime.now()

            // 1. Clean up expired match alarms in background
            val alarmIds = settingsDataStore.scheduledAlarms.first()
            val expiredGnums = schedules
                .filter { alarmIds.contains(it.gnum) && it.hasPassed(now) }
                .map { it.gnum }
            if (expiredGnums.isNotEmpty()) {
                settingsDataStore.removeScheduledAlarms(expiredGnums)
            }

            // 2. Check for finished matches today and send push notifications
            val matchResultAlarmEnabled = settingsDataStore.matchResultAlarmEnabled.first()
            if (matchResultAlarmEnabled) {
                val todayStr = LocalDate.now().toString()
                val notifiedGnums = settingsDataStore.notifiedFinishedMatches.first()

                val finishedTodaySchedules = schedules.filter { schedule ->
                    schedule.date.trim() == todayStr &&
                    (schedule.isFinished || schedule.homePoint == 3 || schedule.awayPoint == 3) &&
                    !notifiedGnums.contains(schedule.gnum)
                }

                for (schedule in finishedTodaySchedules) {
                    val winner = if (schedule.homePoint == 3) schedule.homeTeamName else schedule.awayTeamName
                    val title = "오늘의 배구 - 경기 종료"
                    val message = "${schedule.homeTeamName} ${schedule.homePoint} : ${schedule.awayPoint} ${schedule.awayTeamName} ($winner 승리)"

                    AlarmReceiver.showNotification(appContext, title, message, schedule.gnum + 10000)
                    settingsDataStore.markMatchFinishedNotified(schedule.gnum)
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
