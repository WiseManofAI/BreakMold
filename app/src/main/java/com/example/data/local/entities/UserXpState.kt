package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_xp_state")
data class UserXpState(
    @PrimaryKey
    val id: Int = 1,
    val totalXp: Int = 450,
    val currentLevelXp: Int = 450,
    val targetLevelXp: Int = 2000,
    val level: Int = 12,
    val streakDays: Int = 7,
    val todayXpGained: Int = 65,
    val rankTitle: String = "Cyber Vanguard",
    val lastActiveDate: String = ""
)
