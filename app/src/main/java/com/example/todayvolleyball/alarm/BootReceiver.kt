package com.example.todayvolleyball.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.todayvolleyball.data.local.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val settingsDataStore = SettingsDataStore(context.applicationContext)
                    val enabled = settingsDataStore.dailyAlarmEnabled.first()
                    if (enabled) {
                        val hour = settingsDataStore.dailyAlarmHour.first()
                        val minute = settingsDataStore.dailyAlarmMinute.first()
                        AlarmScheduler(context.applicationContext).scheduleDailyAlarm(hour, minute)
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
