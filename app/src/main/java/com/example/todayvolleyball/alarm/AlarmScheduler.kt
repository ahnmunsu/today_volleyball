package com.example.todayvolleyball.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.todayvolleyball.domain.model.Schedule
import java.util.Calendar

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleMatchAlarm(schedule: Schedule, hoursBefore: Int): Boolean {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_MATCH_ALARM
            putExtra(AlarmReceiver.EXTRA_MATCH_INFO, "${schedule.homeTeamName} vs ${schedule.awayTeamName} 경기가 ${hoursBefore}시간 후 시작합니다!")
            putExtra(AlarmReceiver.EXTRA_MATCH_ID, schedule.gnum)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.gnum,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Parse date "YYYY-MM-DD" and time "HH:MM" or "HH:MM:SS"
        val dateParts = schedule.date.trim().split("-")
        val timeParts = schedule.time.trim().split(":")
        
        if (dateParts.size != 3 || timeParts.size < 2) return false

        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, dateParts[0].trim().toInt())
            set(Calendar.MONTH, dateParts[1].trim().toInt() - 1)
            set(Calendar.DAY_OF_MONTH, dateParts[2].trim().toInt())
            set(Calendar.HOUR_OF_DAY, timeParts[0].trim().toInt())
            set(Calendar.MINUTE, timeParts[1].trim().toInt())
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // Subtract hours
            add(Calendar.HOUR_OF_DAY, -hoursBefore)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            return false
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun cancelMatchAlarm(scheduleId: Int) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_MATCH_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun scheduleDailyAlarm(hour: Int = 9, minute: Int = 0) {
        val intent = Intent(context, DailyAlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_DAILY_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelDailyAlarm() {
        val intent = Intent(context, DailyAlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_DAILY_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
