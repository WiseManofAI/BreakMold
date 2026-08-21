package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED || intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Log.d("BootReceiver", "Device rebooted, rescheduling active alarms and early warnings...")
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val alarms = db.breakMoldDao().getAllAlarmsFlow().firstOrNull() ?: emptyList()
                alarms.filter { it.isEnabled }.forEach { alarm ->
                    AlarmScheduler.scheduleAlarm(context, alarm)
                }

                val schedules = db.breakMoldDao().getAllSchedulesFlow().firstOrNull() ?: emptyList()
                schedules.filter { it.earlyWarningMinutes > 0 }.forEach { schedule ->
                    AlarmScheduler.scheduleEarlyWarning(context, schedule)
                }
            }
        }
    }
}
