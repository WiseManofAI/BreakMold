package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "xp_history_logs")
data class XpHistoryLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val actionTitle: String,
    val xpAmount: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "TARGET_COMPLETED"
)
