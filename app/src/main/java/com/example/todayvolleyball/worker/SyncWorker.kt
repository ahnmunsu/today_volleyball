package com.example.todayvolleyball.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.todayvolleyball.alarm.AlarmReceiver
import com.example.todayvolleyball.data.local.SettingsDataStore
import com.example.todayvolleyball.domain.model.Schedule
import com.example.todayvolleyball.domain.repository.VolleyballRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

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

            // 2. Check for finished matches today & yesterday and send push notifications
            checkAndNotifyFinishedMatches(appContext, schedules, settingsDataStore)

            // 3. If today has ongoing matches that haven't finished, schedule follow-up check in 15 minutes
            val todayStr = LocalDate.now().toString()
            val currentTime = LocalTime.now()
            
            val hasOngoingMatchToday = schedules.any { schedule ->
                if (schedule.date.trim() != todayStr || schedule.isFinished || schedule.homePoint == 3 || schedule.awayPoint == 3) {
                    return@any false
                }
                val matchTime = try { LocalTime.parse(schedule.time.trim()) } catch (_: Exception) { null }
                matchTime != null && currentTime.isAfter(matchTime.minusMinutes(30))
            }

            if (hasOngoingMatchToday) {
                val followUpWork = OneTimeWorkRequestBuilder<SyncWorker>()
                    .setInitialDelay(15, TimeUnit.MINUTES)
                    .build()
                WorkManager.getInstance(appContext).enqueueUniqueWork(
                    "SyncWorker_OngoingCheck",
                    ExistingWorkPolicy.REPLACE,
                    followUpWork
                )
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        suspend fun checkAndNotifyFinishedMatches(
            context: Context,
            schedules: List<Schedule>,
            settingsDataStore: SettingsDataStore
        ) {
            val matchResultAlarmEnabled = settingsDataStore.matchResultAlarmEnabled.first()
            if (!matchResultAlarmEnabled) return

            val today = LocalDate.now()
            val yesterday = today.minusDays(1)
            val notifiedGnums = settingsDataStore.notifiedFinishedMatches.first()

            val finishedSchedules = schedules.filter { schedule ->
                val schedDate = try { LocalDate.parse(schedule.date.trim()) } catch (e: Exception) { null }
                val isRecent = schedDate != null && (schedDate == today || schedDate == yesterday)
                isRecent &&
                (schedule.isFinished || schedule.homePoint == 3 || schedule.awayPoint == 3) &&
                !notifiedGnums.contains(schedule.gnum)
            }

            for (schedule in finishedSchedules) {
                val winner = if (schedule.homePoint == 3) schedule.homeTeamName else schedule.awayTeamName
                val title = "오늘의 배구 - 경기 종료 🏐"
                val message = "${schedule.homeTeamName} ${schedule.homePoint} : ${schedule.awayPoint} ${schedule.awayTeamName} ($winner 승리)"

                AlarmReceiver.showNotification(context, title, message, schedule.gnum + 10000)
                settingsDataStore.markMatchFinishedNotified(schedule.gnum)
            }
        }
    }
}
