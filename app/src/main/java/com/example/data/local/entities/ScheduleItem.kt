package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ScheduleTag {
    ROUTINE,
    WORK,
    HEALTH,
    FOCUS,
    EVENT,
    COMMUTE
}

@Entity(tableName = "schedule_items")
data class ScheduleItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val timeString: String, // e.g. "08:00 AM"
    val startTimeEpochMs: Long = 0L,
    val durationMinutes: Int = 60,
    val isAutoSyncedCalendar: Boolean = false,
    val earlyWarningMinutes: Int = 15, // e.g. 15m heads-up
    val tag: ScheduleTag = ScheduleTag.WORK,
    val isCompleted: Boolean = false,
    val colorHex: String = "#00F5D4"
)
