package com.example.todayvolleyball.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.todayvolleyball.MainActivity
import com.example.todayvolleyball.R

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == ACTION_MATCH_ALARM || action == ACTION_DAILY_ALARM) {
            val message = intent.getStringExtra(EXTRA_MATCH_INFO) ?: "배구 경기가 곧 시작합니다!"
            val matchId = intent.getIntExtra(EXTRA_MATCH_ID, 0)
            showNotification(context, message, matchId)
        }
    }

    private fun showNotification(context: Context, message: String, matchId: Int) {
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
            // Pass matchId if you want to deep link to specific match details
            putExtra("match_id", matchId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            matchId, // Use matchId as request code so each match can be opened separately
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with a proper icon later
            .setContentTitle("오늘의 배구")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(matchId, notification)
    }

    companion object {
        const val ACTION_MATCH_ALARM = "com.example.todayvolleyball.MATCH_ALARM"
        const val ACTION_DAILY_ALARM = "com.example.todayvolleyball.DAILY_ALARM"
        const val EXTRA_MATCH_INFO = "match_info"
        const val EXTRA_MATCH_ID = "match_id"
        const val CHANNEL_ID = "match_alarm_channel"
    }
}
