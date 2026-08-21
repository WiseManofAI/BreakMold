package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.AlarmItem
import com.example.data.local.entities.ScheduleItem
import com.example.data.local.entities.TargetCategory
import com.example.ui.components.AddAlarmDialog
import com.example.ui.components.AddScheduleDialog
import com.example.ui.components.AddTargetDialog
import com.example.ui.components.SmartAlarmCard
import com.example.ui.components.TargetItemCard
import com.example.ui.components.TimelineSection
import com.example.ui.components.XpLevelHeader
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkPill
import com.example.ui.theme.ElectricMagenta
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.BreakMoldViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: BreakMoldViewModel,
    onNavigateToMilestones: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userXp by viewModel.userXp.collectAsStateWithLifecycle()
    val smartAlarm by viewModel.smartAdaptiveAlarm.collectAsStateWithLifecycle()
    val targets by viewModel.targets.collectAsStateWithLifecycle()
    val schedules by viewModel.schedules.collectAsStateWithLifecycle()

    var showAddTargetDialog by remember { mutableStateOf(false) }
    var showAddScheduleDialog by remember { mutableStateOf(false) }
    var showEditAlarmDialog by remember { mutableStateOf(false) }
    var selectedScheduleToEdit by remember { mutableStateOf<ScheduleItem?>(null) }

    // Neon Gradient for Giant Hero Text
    val heroGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF00E5FF),
            Color(0xFF38BDF8),
            Color(0xFF9D4EDD),
            Color(0xFFB5179E)
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        // 1. Top Header: Logo + LVL/XP pill
        item {
            XpLevelHeader(
                userXp = userXp,
                onHeaderClick = onNavigateToMilestones
            )
        }

        // 2. Giant Typography Hero Header: "Today, Break The Mold."
        item {
            Column(
                modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
            ) {
                Text(
                    text = "Today,\nBreak The\nMold.",
                    style = TextStyle(
                        brush = heroGradient,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 52.sp,
                        letterSpacing = (-1.5).sp,
                        fontFamily = FontFamily.SansSerif
                    )
                )
            }
        }

        // 3. Smart Adaptive Alarm Card
        item {
            SmartAlarmCard(
                alarm = smartAlarm,
                onToggle = { viewModel.toggleAlarm(it) },
                onClick = { showEditAlarmDialog = true }
            )
        }

        // 4. Daily Targets Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF331427)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Adjust,
                            contentDescription = "Targets Icon",
                            tint = NeonPink,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Daily Targets",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { showAddTargetDialog = true },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(DarkPill)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Target",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 5. Daily Targets Cards
        items(targets.filter { it.category == TargetCategory.DAILY }, key = { it.id }) { target ->
            TargetItemCard(
                target = target,
                onToggle = { viewModel.toggleTarget(it) },
                onDelete = { viewModel.deleteTarget(it) }
            )
        }

        // 6. Timeline Section
        item {
            TimelineSection(
                schedules = schedules,
                onAddClick = {
                    selectedScheduleToEdit = null
                    showAddScheduleDialog = true
                },
                onItemClick = { item ->
                    selectedScheduleToEdit = item
                    showAddScheduleDialog = true
                }
            )
        }

        // Bottom spacing to avoid navigation bar overlap
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Dialogs
    if (showAddTargetDialog) {
        AddTargetDialog(
            onDismiss = { showAddTargetDialog = false },
            onSave = { title, subtitle, category, xp ->
                viewModel.addTarget(title, subtitle, category, xp)
            }
        )
    }

    if (showAddScheduleDialog) {
        AddScheduleDialog(
            onDismiss = {
                showAddScheduleDialog = false
                selectedScheduleToEdit = null
            },
            onSave = { schedule ->
                if (schedule.id == 0L) {
                    viewModel.addSchedule(schedule)
                } else {
                    viewModel.updateSchedule(schedule)
                }
            },
            existingSchedule = selectedScheduleToEdit
        )
    }

    if (showEditAlarmDialog) {
        AddAlarmDialog(
            onDismiss = { showEditAlarmDialog = false },
            onSave = { alarm ->
                viewModel.saveAlarm(alarm)
            },
            existingAlarm = smartAlarm
        )
    }
}
