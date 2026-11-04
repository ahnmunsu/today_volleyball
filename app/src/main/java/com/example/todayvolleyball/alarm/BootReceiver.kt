package com.example.todayvolleyball.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.todayvolleyball.data.local.SettingsDataStore
import com.example.todayvolleyball.domain.repository.VolleyballRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    @Inject
    lateinit var repository: VolleyballRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val scheduler = AlarmScheduler(context.applicationContext)

                    // 1. Reschedule Daily Alarm if enabled
                    val dailyEnabled = settingsDataStore.dailyAlarmEnabled.first()
                    if (dailyEnabled) {
                        val hour = settingsDataStore.dailyAlarmHour.first()
                        val minute = settingsDataStore.dailyAlarmMinute.first()
                        scheduler.scheduleDailyAlarm(hour, minute)
                    }

                    // 2. Reschedule Match Alarms
                    val matchAlarmEnabled = settingsDataStore.matchAlarmEnabled.first()
                    if (matchAlarmEnabled) {
                        val scheduledGnums = settingsDataStore.scheduledAlarms.first()
                        if (scheduledGnums.isNotEmpty()) {
                            val schedules = repository.getSchedulesFlow().first()
                            val scheduledMap = schedules.associateBy { it.gnum }
                            for (gnum in scheduledGnums) {
                                val schedule = scheduledMap[gnum]
                                if (schedule != null) {
                                    scheduler.scheduleMatchAlarm(schedule, hoursBefore = 1)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
