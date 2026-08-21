package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.BreakMoldDao
import com.example.data.local.entities.AlarmItem
import com.example.data.local.entities.ScheduleItem
import com.example.data.local.entities.ScheduleTag
import com.example.data.local.entities.TargetCategory
import com.example.data.local.entities.TargetItem
import com.example.data.local.entities.UserXpState
import com.example.data.local.entities.XpHistoryLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun fromTargetCategory(value: TargetCategory): String = value.name

    @TypeConverter
    fun toTargetCategory(value: String): TargetCategory = try {
        TargetCategory.valueOf(value)
    } catch (e: Exception) {
        TargetCategory.DAILY
    }

    @TypeConverter
    fun fromScheduleTag(value: ScheduleTag): String = value.name

    @TypeConverter
    fun toScheduleTag(value: String): ScheduleTag = try {
        ScheduleTag.valueOf(value)
    } catch (e: Exception) {
        ScheduleTag.WORK
    }
}

@Database(
    entities = [
        TargetItem::class,
        ScheduleItem::class,
        AlarmItem::class,
        UserXpState::class,
        XpHistoryLog::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun breakMoldDao(): BreakMoldDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "breakmold_database"
                )
                    .addCallback(DatabaseCallback(context))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = getDatabase(context).breakMoldDao()
                    // Initialize default XP state
                    dao.insertOrUpdateUserXp(
                        UserXpState(
                            id = 1,
                            totalXp = 450,
                            currentLevelXp = 450,
                            targetLevelXp = 2000,
                            level = 12,
                            streakDays = 7,
                            todayXpGained = 30,
                            rankTitle = "Cyber Architect"
                        )
                    )

                    // Seed matching concept art
                    dao.insertAlarm(
                        AlarmItem(
                            id = 1,
                            label = "Smart Adaptive Alarm",
                            timeString = "06:30 AM",
                            hour = 6,
                            minute = 30,
                            isEnabled = true,
                            isSmartAdaptive = true,
                            linkedScheduleTitle = "08:00 AM Kickoff",
                            earlyOffsetMinutes = 90,
                            repeatDays = "MON,TUE,WED,THU,FRI",
                            soundVibrate = true
                        )
                    )
                    dao.insertAlarm(
                        AlarmItem(
                            id = 2,
                            label = "Deep Work Focus Block",
                            timeString = "09:00 AM",
                            hour = 9,
                            minute = 0,
                            isEnabled = false,
                            isSmartAdaptive = false,
                            repeatDays = "MON,TUE,WED,THU,FRI"
                        )
                    )

                    // Seed Daily Targets matching concept art
                    dao.insertTargets(
                        listOf(
                            TargetItem(
                                id = 1,
                                title = "Morning Hydration",
                                subtitle = "500ml Water",
                                category = TargetCategory.DAILY,
                                xpReward = 15,
                                isCompleted = false
                            ),
                            TargetItem(
                                id = 2,
                                title = "Read 10 Pages",
                                subtitle = "Atomic Habits",
                                category = TargetCategory.DAILY,
                                xpReward = 20,
                                isCompleted = true,
                                completedAt = System.currentTimeMillis() - 3600000
                            ),
                            TargetItem(
                                id = 3,
                                title = "Deep Work Block",
                                subtitle = "90 Mins uninterrupted",
                                category = TargetCategory.DAILY,
                                xpReward = 50,
                                isCompleted = false
                            ),
                            TargetItem(
                                id = 4,
                                title = "Sprint Code Review",
                                subtitle = "Clear PR backlog & architectural comments",
                                category = TargetCategory.WEEKLY,
                                xpReward = 100,
                                isCompleted = false
                            )
                        )
                    )

                    // Seed Timeline matching concept art
                    dao.insertSchedules(
                        listOf(
                            ScheduleItem(
                                id = 1,
                                title = "Wake Up",
                                description = "Adaptive morning protocol & hydration",
                                timeString = "06:30 AM",
                                startTimeEpochMs = System.currentTimeMillis(),
                                durationMinutes = 30,
                                tag = ScheduleTag.ROUTINE,
                                colorHex = "#00F5D4"
                            ),
                            ScheduleItem(
                                id = 2,
                                title = "Project Kickoff",
                                description = "Q3 Product Architecture & Sprint Strategy",
                                timeString = "08:00 AM",
                                startTimeEpochMs = System.currentTimeMillis() + 5400000,
                                durationMinutes = 60,
                                isAutoSyncedCalendar = true,
                                earlyWarningMinutes = 15,
                                tag = ScheduleTag.WORK,
                                colorHex = "#B5179E"
                            ),
                            ScheduleItem(
                                id = 3,
                                title = "Lunch & Walk",
                                description = "Active recovery, sun exposure & nutrition",
                                timeString = "12:30 PM",
                                startTimeEpochMs = System.currentTimeMillis() + 21600000,
                                durationMinutes = 45,
                                tag = ScheduleTag.HEALTH,
                                colorHex = "#38BDF8"
                            ),
                            ScheduleItem(
                                id = 4,
                                title = "System Architecture Deep Dive",
                                description = "Gemini AI module refactoring & exact alarms",
                                timeString = "03:00 PM",
                                startTimeEpochMs = System.currentTimeMillis() + 30600000,
                                durationMinutes = 90,
                                isAutoSyncedCalendar = true,
                                earlyWarningMinutes = 10,
                                tag = ScheduleTag.FOCUS,
                                colorHex = "#9D4EDD"
                            )
                        )
                    )

                    dao.insertXpLog(
                        XpHistoryLog(
                            actionTitle = "Read 10 Pages (Atomic Habits)",
                            xpAmount = 20,
                            category = "DAILY_TARGET"
                        )
                    )
                }
            }
        }
    }
}
