package com.example.service

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import com.example.data.local.entities.ScheduleItem
import java.util.Calendar

object CalendarSyncHelper {

    fun syncEventToNativeCalendar(context: Context, schedule: ScheduleItem) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, "⚡ [BreakMold] " + schedule.title)
            putExtra(CalendarContract.Events.DESCRIPTION, schedule.description + " (Auto-synced from BreakMold Focus Protocol)")
            putExtra(CalendarContract.Events.EVENT_LOCATION, "Focus Zone")

            val startTime = if (schedule.startTimeEpochMs > System.currentTimeMillis()) {
                schedule.startTimeEpochMs
            } else {
                Calendar.getInstance().apply {
                    add(Calendar.HOUR_OF_DAY, 1)
                }.timeInMillis
            }
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startTime + (schedule.durationMinutes * 60 * 1000L))
            putExtra(CalendarContract.Events.HAS_ALARM, 1)
            putExtra(CalendarContract.Reminders.MINUTES, schedule.earlyWarningMinutes)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
            Toast.makeText(context, "Opening Calendar for '${schedule.title}' with early warning alert", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "No calendar app found on device", Toast.LENGTH_SHORT).show()
        }
    }
}
