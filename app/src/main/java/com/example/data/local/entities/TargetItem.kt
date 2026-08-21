package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TargetCategory {
    DAILY,
    WEEKLY,
    MILESTONE
}

@Entity(tableName = "target_items")
data class TargetItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val subtitle: String = "",
    val category: TargetCategory = TargetCategory.DAILY,
    val xpReward: Int = 15,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val streakCount: Int = 1,
    val targetDate: String = "" // e.g. "2026-08-21"
)
