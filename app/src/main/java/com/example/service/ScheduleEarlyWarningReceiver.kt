package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ScheduleEarlyWarningReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val scheduleId = intent.getLongExtra("EXTRA_SCHEDULE_ID", -1L)
        val title = intent.getStringExtra("EXTRA_SCHEDULE_TITLE") ?: "Scheduled Routine"
        val timeString = intent.getStringExtra("EXTRA_SCHEDULE_TIME") ?: ""
        val minutesBefore = intent.getIntExtra("EXTRA_WARNING_MINUTES", 15)

        NotificationHelper.showEarlyWarningNotification(
            context = context,
            scheduleId = scheduleId,
            title = title,
            timeString = timeString,
            minutesBefore = minutesBefore
        )
    }
}
