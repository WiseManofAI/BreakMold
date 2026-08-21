package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.AlarmItem
import com.example.data.local.entities.ScheduleItem
import com.example.data.local.entities.TargetCategory
import com.example.data.local.entities.TargetItem
import com.example.data.local.entities.UserXpState
import com.example.data.local.entities.XpHistoryLog
import com.example.data.remote.AIScheduleResult
import com.example.data.repository.BreakMoldRepository
import com.example.service.CalendarSyncHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BreakMoldViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = BreakMoldRepository(
        dao = db.breakMoldDao(),
        context = application
    )

    val targets: StateFlow<List<TargetItem>> = repository.allTargets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val schedules: StateFlow<List<ScheduleItem>> = repository.allSchedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alarms: StateFlow<List<AlarmItem>> = repository.allAlarms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val smartAdaptiveAlarm: StateFlow<AlarmItem?> = repository.smartAdaptiveAlarm
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userXp: StateFlow<UserXpState> = repository.userXpState
        .map { it ?: UserXpState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserXpState())

    val xpLogs: StateFlow<List<XpHistoryLog>> = repository.xpHistoryLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isAiOptimizing = MutableStateFlow(false)
    val isAiOptimizing: StateFlow<Boolean> = _isAiOptimizing.asStateFlow()

    private val _aiResult = MutableStateFlow<AIScheduleResult?>(null)
    val aiResult: StateFlow<AIScheduleResult?> = _aiResult.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _activeAlarmTrigger = MutableStateFlow<AlarmItem?>(null)
    val activeAlarmTrigger: StateFlow<AlarmItem?> = _activeAlarmTrigger.asStateFlow()

    init {
        // Observe if any alarm is triggering now
        viewModelScope.launch {
            repository.allAlarms.collect { alarmList ->
                val triggering = alarmList.firstOrNull { it.isTriggeringNow }
                _activeAlarmTrigger.value = triggering
            }
        }
    }

    fun setSelectedTab(index: Int) {
        _selectedTab.value = index
    }

    // Target actions
    fun toggleTarget(target: TargetItem) {
        viewModelScope.launch {
            repository.toggleTargetCompletion(target)
        }
    }

    fun addTarget(title: String, subtitle: String, category: TargetCategory, xpReward: Int) {
        viewModelScope.launch {
            repository.addTarget(title, subtitle, category, xpReward)
        }
    }

    fun deleteTarget(target: TargetItem) {
        viewModelScope.launch {
            repository.deleteTarget(target)
        }
    }

    // Schedule actions
    fun addSchedule(schedule: ScheduleItem) {
        viewModelScope.launch {
            repository.addSchedule(schedule)
        }
    }

    fun updateSchedule(schedule: ScheduleItem) {
        viewModelScope.launch {
            repository.updateSchedule(schedule)
        }
    }

    fun deleteSchedule(schedule: ScheduleItem) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
        }
    }

    fun syncWithCalendar(context: Context, schedule: ScheduleItem) {
        CalendarSyncHelper.syncEventToNativeCalendar(context, schedule)
    }

    // Alarm actions
    fun toggleAlarm(alarm: AlarmItem) {
        viewModelScope.launch {
            repository.toggleAlarm(alarm)
        }
    }

    fun saveAlarm(alarm: AlarmItem) {
        viewModelScope.launch {
            repository.addOrUpdateAlarm(alarm)
        }
    }

    fun deleteAlarm(alarm: AlarmItem) {
        viewModelScope.launch {
            repository.deleteAlarm(alarm)
        }
    }

    fun dismissActiveAlarm(alarm: AlarmItem) {
        viewModelScope.launch {
            repository.addOrUpdateAlarm(alarm.copy(isTriggeringNow = false))
            _activeAlarmTrigger.value = null
        }
    }

    // AI Scheduling actions
    fun runAiScheduleOptimizer(prompt: String) {
        viewModelScope.launch {
            _isAiOptimizing.value = true
            val currentAlarm = smartAdaptiveAlarm.value?.timeString ?: "06:30 AM"
            val firstSched = schedules.value.firstOrNull { it.tag.name == "WORK" }?.timeString ?: "08:00 AM"

            val result = repository.runAiScheduleOptimization(prompt)
            _isAiOptimizing.value = false
            _aiResult.value = result.getOrNull()
        }
    }

    fun applyAiScheduleResult() {
        val currentResult = _aiResult.value ?: return
        viewModelScope.launch {
            repository.applyAiSchedule(currentResult)
            _aiResult.value = null
            _selectedTab.value = 0 // Return to dashboard
        }
    }

    fun clearAiResult() {
        _aiResult.value = null
    }
}
