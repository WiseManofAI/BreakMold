package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_items")
data class AlarmItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val label: String,
    val timeString: String, // e.g. "06:30 AM"
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val isSmartAdaptive: Boolean = false,
    val linkedScheduleTitle: String? = null, // e.g. "08:00 AM Kickoff"
    val earlyOffsetMinutes: Int = 90, // Wake up 90 mins before kickoff
    val repeatDays: String = "MON,TUE,WED,THU,FRI",
    val soundVibrate: Boolean = true,
    val isTriggeringNow: Boolean = false
)
