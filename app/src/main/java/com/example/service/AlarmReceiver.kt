package com.example.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val action = intent.action ?: return
        val alarmId = intent.getLongExtra(NotificationHelper.EXTRA_ALARM_ID, -1L)
        val label = intent.getStringExtra(NotificationHelper.EXTRA_ALARM_LABEL) ?: "BreakMold Alarm"
        val timeString = intent.getStringExtra("EXTRA_TIME_STRING") ?: "Active"

        Log.d("AlarmReceiver", "Received action $action for alarm $alarmId")

        when (action) {
            AlarmScheduler.ACTION_TRIGGER_ALARM -> {
                NotificationHelper.showAlarmNotification(context, alarmId, label, timeString)
                // Mark in DB that alarm is triggering
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = AppDatabase.getDatabase(context).breakMoldDao()
                    val alarm = dao.getAlarmById(alarmId)
                    if (alarm != null) {
                        dao.updateAlarm(alarm.copy(isTriggeringNow = true))
                    }
                }
            }

            NotificationHelper.ACTION_DISMISS -> {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(alarmId.toInt())
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = AppDatabase.getDatabase(context).breakMoldDao()
                    val alarm = dao.getAlarmById(alarmId)
                    if (alarm != null) {
                        dao.updateAlarm(alarm.copy(isTriggeringNow = false))
                    }
                }
            }

            NotificationHelper.ACTION_SNOOZE -> {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(alarmId.toInt())
                // Re-trigger alarm in 10 minutes
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
                    this.action = AlarmScheduler.ACTION_TRIGGER_ALARM
                    putExtra(NotificationHelper.EXTRA_ALARM_ID, alarmId)
                    putExtra(NotificationHelper.EXTRA_ALARM_LABEL, "$label (Snoozed)")
                    putExtra("EXTRA_TIME_STRING", "Snoozed +10m")
                }
                val pendingIntent = android.app.PendingIntent.getBroadcast(
                    context,
                    alarmId.toInt(),
                    snoozeIntent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )
                val triggerAt = System.currentTimeMillis() + (10 * 60 * 1000L)
                alarmManager.setAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
            }
        }
    }
}
