package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.data.local.entities.AlarmItem
import com.example.data.local.entities.ScheduleItem
import com.example.data.local.entities.TargetItem
import com.example.data.local.entities.UserXpState
import com.example.data.local.entities.XpHistoryLog
import kotlinx.coroutines.flow.Flow

@Dao
interface BreakMoldDao {

    // --- TARGETS ---
    @Query("SELECT * FROM target_items ORDER BY isCompleted ASC, id ASC")
    fun getAllTargetsFlow(): Flow<List<TargetItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarget(target: TargetItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTargets(targets: List<TargetItem>)

    @Update
    suspend fun updateTarget(target: TargetItem)

    @Delete
    suspend fun deleteTarget(target: TargetItem)

    @Query("SELECT * FROM target_items WHERE id = :id")
    suspend fun getTargetById(id: Long): TargetItem?

    // --- SCHEDULES ---
    @Query("SELECT * FROM schedule_items ORDER BY startTimeEpochMs ASC, id ASC")
    fun getAllSchedulesFlow(): Flow<List<ScheduleItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleItem>)

    @Update
    suspend fun updateSchedule(schedule: ScheduleItem)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleItem)

    @Query("DELETE FROM schedule_items")
    suspend fun clearAllSchedules()

    // --- ALARMS ---
    @Query("SELECT * FROM alarm_items ORDER BY isSmartAdaptive DESC, hour ASC, minute ASC")
    fun getAllAlarmsFlow(): Flow<List<AlarmItem>>

    @Query("SELECT * FROM alarm_items WHERE isSmartAdaptive = 1 LIMIT 1")
    fun getSmartAdaptiveAlarmFlow(): Flow<AlarmItem?>

    @Query("SELECT * FROM alarm_items WHERE isSmartAdaptive = 1 LIMIT 1")
    suspend fun getSmartAdaptiveAlarm(): AlarmItem?

    @Query("SELECT * FROM alarm_items WHERE id = :id")
    suspend fun getAlarmById(id: Long): AlarmItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarms(alarms: List<AlarmItem>)

    @Update
    suspend fun updateAlarm(alarm: AlarmItem)

    @Delete
    suspend fun deleteAlarm(alarm: AlarmItem)

    // --- USER XP STATE ---
    @Query("SELECT * FROM user_xp_state WHERE id = 1 LIMIT 1")
    fun getUserXpFlow(): Flow<UserXpState?>

    @Query("SELECT * FROM user_xp_state WHERE id = 1 LIMIT 1")
    suspend fun getUserXp(): UserXpState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserXp(userXp: UserXpState)

    // --- XP HISTORY LOGS ---
    @Query("SELECT * FROM xp_history_logs ORDER BY timestamp DESC LIMIT 20")
    fun getXpHistoryFlow(): Flow<List<XpHistoryLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertXpLog(log: XpHistoryLog)
}
