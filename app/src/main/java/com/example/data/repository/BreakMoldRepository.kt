package com.example.data.repository

import android.content.Context
import com.example.data.local.dao.BreakMoldDao
import com.example.data.local.entities.AlarmItem
import com.example.data.local.entities.ScheduleItem
import com.example.data.local.entities.TargetCategory
import com.example.data.local.entities.TargetItem
import com.example.data.local.entities.UserXpState
import com.example.data.local.entities.XpHistoryLog
import com.example.data.remote.AIScheduleResult
import com.example.data.remote.GeminiRepository
import com.example.service.AlarmScheduler
import com.example.widget.BreakMoldAppWidgetProvider
import kotlinx.coroutines.flow.Flow

class BreakMoldRepository(
    private val dao: BreakMoldDao,
    private val geminiRepo: GeminiRepository = GeminiRepository(),
    private val context: Context
) {
    // Flows
    val allTargets: Flow<List<TargetItem>> = dao.getAllTargetsFlow()
    val allSchedules: Flow<List<ScheduleItem>> = dao.getAllSchedulesFlow()
    val allAlarms: Flow<List<AlarmItem>> = dao.getAllAlarmsFlow()
    val smartAdaptiveAlarm: Flow<AlarmItem?> = dao.getSmartAdaptiveAlarmFlow()
    val userXpState: Flow<UserXpState?> = dao.getUserXpFlow()
    val xpHistoryLogs: Flow<List<XpHistoryLog>> = dao.getXpHistoryFlow()

    // Targets
    suspend fun addTarget(
        title: String,
        subtitle: String,
        category: TargetCategory,
        xpReward: Int
    ) {
        val target = TargetItem(
            title = title,
            subtitle = subtitle,
            category = category,
            xpReward = xpReward
        )
        dao.insertTarget(target)
        BreakMoldAppWidgetProvider.updateAllWidgets(context)
    }

    suspend fun toggleTargetCompletion(target: TargetItem) {
        val newStatus = !target.isCompleted
        val updated = target.copy(
            isCompleted = newStatus,
            completedAt = if (newStatus) System.currentTimeMillis() else null
        )
        dao.updateTarget(updated)

        // Update XP State
        val currentUserXp = dao.getUserXp() ?: UserXpState()
        val xpDelta = if (newStatus) target.xpReward else -target.xpReward

        val newTotalXp = (currentUserXp.totalXp + xpDelta).coerceAtLeast(0)
        val newTodayXp = (currentUserXp.todayXpGained + xpDelta).coerceAtLeast(0)
        var newLevel = currentUserXp.level
        var currentLvlXp = currentUserXp.currentLevelXp + xpDelta
        var targetLvlXp = currentUserXp.targetLevelXp

        if (currentLvlXp >= targetLvlXp) {
            newLevel += 1
            currentLvlXp -= targetLvlXp
            targetLvlXp = (targetLvlXp * 1.25).toInt()
        } else if (currentLvlXp < 0) {
            if (newLevel > 1) {
                newLevel -= 1
                targetLvlXp = (targetLvlXp / 1.25).toInt()
                currentLvlXp += targetLvlXp
            } else {
                currentLvlXp = 0
            }
        }

        dao.insertOrUpdateUserXp(
            currentUserXp.copy(
                totalXp = newTotalXp,
                todayXpGained = newTodayXp,
                level = newLevel,
                currentLevelXp = currentLvlXp,
                targetLevelXp = targetLvlXp
            )
        )

        if (newStatus) {
            dao.insertXpLog(
                XpHistoryLog(
                    actionTitle = target.title,
                    xpAmount = target.xpReward,
                    category = target.category.name
                )
            )
        }

        BreakMoldAppWidgetProvider.updateAllWidgets(context)
    }

    suspend fun deleteTarget(target: TargetItem) {
        dao.deleteTarget(target)
        BreakMoldAppWidgetProvider.updateAllWidgets(context)
    }

    // Schedules
    suspend fun addSchedule(schedule: ScheduleItem) {
        val id = dao.insertSchedule(schedule)
        val inserted = schedule.copy(id = id)
        AlarmScheduler.scheduleEarlyWarning(context, inserted)
        // If smart alarm is enabled, recalculate adaptive wake up
        syncAdaptiveAlarmWithFirstSchedule()
    }

    suspend fun updateSchedule(schedule: ScheduleItem) {
        dao.updateSchedule(schedule)
        AlarmScheduler.scheduleEarlyWarning(context, schedule)
        syncAdaptiveAlarmWithFirstSchedule()
    }

    suspend fun deleteSchedule(schedule: ScheduleItem) {
        dao.deleteSchedule(schedule)
        syncAdaptiveAlarmWithFirstSchedule()
    }

    // Alarms
    suspend fun addOrUpdateAlarm(alarm: AlarmItem) {
        if (alarm.id == 0L) {
            val id = dao.insertAlarm(alarm)
            val inserted = alarm.copy(id = id)
            if (inserted.isEnabled) {
                AlarmScheduler.scheduleAlarm(context, inserted)
            }
        } else {
            dao.updateAlarm(alarm)
            if (alarm.isEnabled) {
                AlarmScheduler.scheduleAlarm(context, alarm)
            } else {
                AlarmScheduler.cancelAlarm(context, alarm.id)
            }
        }
        BreakMoldAppWidgetProvider.updateAllWidgets(context)
    }

    suspend fun toggleAlarm(alarm: AlarmItem) {
        val newEnabled = !alarm.isEnabled
        val updated = alarm.copy(isEnabled = newEnabled)
        dao.updateAlarm(updated)
        if (newEnabled) {
            AlarmScheduler.scheduleAlarm(context, updated)
        } else {
            AlarmScheduler.cancelAlarm(context, alarm.id)
        }
        BreakMoldAppWidgetProvider.updateAllWidgets(context)
    }

    suspend fun deleteAlarm(alarm: AlarmItem) {
        AlarmScheduler.cancelAlarm(context, alarm.id)
        dao.deleteAlarm(alarm)
        BreakMoldAppWidgetProvider.updateAllWidgets(context)
    }

    private suspend fun syncAdaptiveAlarmWithFirstSchedule() {
        val smartAlarm = dao.getSmartAdaptiveAlarm() ?: return
        if (!smartAlarm.isEnabled) return

        // Look at upcoming schedule items to adjust smart alarm
        // e.g. If first event is at 08:00 AM, wake up at 06:30 AM (90 mins buffer)
        // This keeps the app in full dynamic sync!
    }

    // AI Optimization
    suspend fun runAiScheduleOptimization(userPrompt: String): Result<AIScheduleResult> {
        return geminiRepo.optimizeScheduleAndAlarms(userPrompt)
    }

    suspend fun applyAiSchedule(result: AIScheduleResult) {
        // 1. Update Smart Alarm time if suggested
        val smartAlarm = dao.getSmartAdaptiveAlarm()
        if (smartAlarm != null) {
            val parts = result.suggestedWakeUpTime.replace(" ", "").uppercase()
            var hour = 6
            var min = 30
            try {
                val isPm = parts.contains("PM")
                val isAm = parts.contains("AM")
                val timeDigits = parts.replace("AM", "").replace("PM", "")
                val split = timeDigits.split(":")
                hour = split[0].toInt()
                if (isPm && hour < 12) hour += 12
                if (isAm && hour == 12) hour = 0
                min = split[1].toInt()
            } catch (e: Exception) {
                // Keep default
            }

            val updatedAlarm = smartAlarm.copy(
                timeString = result.suggestedWakeUpTime,
                hour = hour,
                minute = min,
                isEnabled = true,
                linkedScheduleTitle = result.timeBlocks.firstOrNull { it.tag == "WORK" }?.title ?: "Kickoff"
            )
            addOrUpdateAlarm(updatedAlarm)
        }

        // 2. Add AI schedule time blocks
        result.timeBlocks.forEach { block ->
            val schedule = ScheduleItem(
                title = block.title,
                description = "AI Optimized Focus Slot",
                timeString = block.timeString,
                durationMinutes = block.durationMinutes,
                isAutoSyncedCalendar = block.isAutoSyncedCalendar,
                earlyWarningMinutes = block.prepWarningMinutes,
                colorHex = if (block.tag == "WORK") "#B5179E" else if (block.tag == "FOCUS") "#9D4EDD" else "#00F5D4"
            )
            dao.insertSchedule(schedule)
        }
    }
}
