package com.example.todayvolleyball.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.todayvolleyball.MainActivity
import com.example.todayvolleyball.R
import com.example.todayvolleyball.data.local.SettingsDataStore
import com.example.todayvolleyball.worker.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == ACTION_MATCH_ALARM || action == ACTION_DAILY_ALARM) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val settingsDataStore = SettingsDataStore(context.applicationContext)
                    val message = intent.getStringExtra(EXTRA_MATCH_INFO) ?: "배구 경기가 곧 시작합니다!"
                    val matchId = intent.getIntExtra(EXTRA_MATCH_ID, 0)

                    if (action == ACTION_MATCH_ALARM) {
                        val enabled = settingsDataStore.matchAlarmEnabled.first()
                        if (enabled) {
                            showNotification(context, "오늘의 배구 🏐", message, matchId)

                            // 경기 종료 체크를 위해 경기 예상 종료 시점(약 2시간 후)에 SyncWorker 실행 예약
                            val followUpWork = OneTimeWorkRequestBuilder<SyncWorker>()
                                .setInitialDelay(2, TimeUnit.HOURS)
                                .build()
                            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                                "SyncWorker_MatchEnd_${matchId}",
                                ExistingWorkPolicy.REPLACE,
                                followUpWork
                            )
                        }
                    } else {
                        showNotification(context, "오늘의 배구 🏐", message, matchId)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        const val ACTION_MATCH_ALARM = "com.example.todayvolleyball.MATCH_ALARM"
        const val ACTION_DAILY_ALARM = "com.example.todayvolleyball.DAILY_ALARM"
        const val EXTRA_MATCH_INFO = "match_info"
        const val EXTRA_MATCH_ID = "match_id"
        const val CHANNEL_ID = "match_alarm_channel"

        fun showNotification(context: Context, title: String, message: String, notificationId: Int) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Volleyball Match Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("match_id", notificationId)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            notificationManager.notify(notificationId, notification)
        }
    }
}
