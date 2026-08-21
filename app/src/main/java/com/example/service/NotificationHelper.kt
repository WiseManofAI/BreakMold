package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    const val ALARM_CHANNEL_ID = "breakmold_alarm_channel"
    const val EARLY_WARNING_CHANNEL_ID = "breakmold_early_warning_channel"
    const val XP_MILESTONE_CHANNEL_ID = "breakmold_xp_channel"

    const val ACTION_DISMISS = "com.example.ACTION_DISMISS_ALARM"
    const val ACTION_SNOOZE = "com.example.ACTION_SNOOZE_ALARM"
    const val EXTRA_ALARM_ID = "extra_alarm_id"
    const val EXTRA_ALARM_LABEL = "extra_alarm_label"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            // 1. Alarm Channel
            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "BreakMold Smart Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical wake-up and schedule alarms"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 800)
                setSound(alarmSound, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            // 2. Early Warning Channel
            val warningChannel = NotificationChannel(
                EARLY_WARNING_CHANNEL_ID,
                "Schedule Early Warnings",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Heads-up notifications before upcoming schedule routines"
                enableVibration(true)
            }

            // 3. XP / Milestone Channel
            val xpChannel = NotificationChannel(
                XP_MILESTONE_CHANNEL_ID,
                "BreakMold XP & Level Ups",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Gamification level up and streak alerts"
            }

            notificationManager.createNotificationChannel(alarmChannel)
            notificationManager.createNotificationChannel(warningChannel)
            notificationManager.createNotificationChannel(xpChannel)
        }
    }

    fun showAlarmNotification(context: Context, alarmId: Long, label: String, timeString: String) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("ALARM_TRIGGERED_ID", alarmId)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            alarmId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_DISMISS
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            (alarmId * 10 + 1).toInt(),
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_LABEL, label)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (alarmId * 10 + 2).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⏰ $label ($timeString)")
            .setContentText("BreakMold: Time to execute your routine. Break the mold!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setOngoing(true)
            .setContentIntent(openPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Dismiss", dismissPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Snooze 10m", snoozePendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(alarmId.toInt(), notification)
    }

    fun showEarlyWarningNotification(context: Context, scheduleId: Long, title: String, timeString: String, minutesBefore: Int) {
        val openIntent = Intent(context, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            context,
            scheduleId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, EARLY_WARNING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⚡ Upcoming: $title in $minutesBefore mins")
            .setContentText("Scheduled for $timeString. Prepare your focus block now!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((10000 + scheduleId).toInt(), notification)
    }
}
