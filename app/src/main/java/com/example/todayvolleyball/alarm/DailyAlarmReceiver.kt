package com.example.todayvolleyball.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.room.Room
import com.example.todayvolleyball.MainActivity
import com.example.todayvolleyball.R
import com.example.todayvolleyball.data.local.VolleyballDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class DailyAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    VolleyballDatabase::class.java,
                    "volleyball.db"
                ).build()

                val todayStr = LocalDate.now().toString()
                val todayMatches = db.volleyballDao().getSchedulesByDate(todayStr)

                if (todayMatches.isNotEmpty()) {
                    val summary = todayMatches.joinToString("\n") { "${it.homeName} vs ${it.awayName} (${it.time})" }
                    showNotification(context, summary)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, summary: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            AlarmReceiver.CHANNEL_ID,
            "Volleyball Match Alerts",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            9999,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, AlarmReceiver.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("오늘의 배구 경기 안내 🏐")
            .setContentText("오늘 예정된 경기가 있습니다!")
            .setStyle(NotificationCompat.BigTextStyle().bigText(summary))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(9999, notification)
    }
}
