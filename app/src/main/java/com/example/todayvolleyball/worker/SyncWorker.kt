package com.example.todayvolleyball.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todayvolleyball.data.local.SettingsDataStore
import com.example.todayvolleyball.domain.repository.VolleyballRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: VolleyballRepository,
    private val settingsDataStore: SettingsDataStore
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            repository.fetchAndSaveTeams()
            repository.fetchAndSaveSchedules()
            
            // Clean up expired match alarms in background
            val schedules = repository.getSchedulesFlow().first()
            val alarmIds = settingsDataStore.scheduledAlarms.first()
            val now = LocalDateTime.now()
            val expiredGnums = schedules
                .filter { alarmIds.contains(it.gnum) && it.hasPassed(now) }
                .map { it.gnum }
            if (expiredGnums.isNotEmpty()) {
                settingsDataStore.removeScheduledAlarms(expiredGnums)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
