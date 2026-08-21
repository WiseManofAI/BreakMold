package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.local.entities.AlarmItem
import com.example.data.local.entities.ScheduleItem
import java.util.Calendar

object AlarmScheduler {
    const val ACTION_TRIGGER_ALARM = "com.example.ACTION_TRIGGER_ALARM"
    const val ACTION_EARLY_WARNING = "com.example.ACTION_EARLY_WARNING"

    fun scheduleAlarm(context: Context, alarm: AlarmItem) {
        if (!alarm.isEnabled) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_ALARM
            putExtra(NotificationHelper.EXTRA_ALARM_ID, alarm.id)
            putExtra(NotificationHelper.EXTRA_ALARM_LABEL, alarm.label)
            putExtra("EXTRA_TIME_STRING", alarm.timeString)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

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
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            Log.d("AlarmScheduler", "Scheduled alarm ${alarm.label} at ${calendar.time}")
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "SecurityException scheduling exact alarm", e)
        }
    }

    fun cancelAlarm(context: Context, alarmId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun scheduleEarlyWarning(context: Context, schedule: ScheduleItem) {
        if (schedule.earlyWarningMinutes <= 0 || schedule.startTimeEpochMs <= System.currentTimeMillis()) {
            return
        }

        val warningTimeMs = schedule.startTimeEpochMs - (schedule.earlyWarningMinutes * 60 * 1000L)
        if (warningTimeMs <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ScheduleEarlyWarningReceiver::class.java).apply {
            action = ACTION_EARLY_WARNING
            putExtra("EXTRA_SCHEDULE_ID", schedule.id)
            putExtra("EXTRA_SCHEDULE_TITLE", schedule.title)
            putExtra("EXTRA_SCHEDULE_TIME", schedule.timeString)
            putExtra("EXTRA_WARNING_MINUTES", schedule.earlyWarningMinutes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (10000 + schedule.id).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                warningTimeMs,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Failed to schedule early warning", e)
        }
    }
}
