package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ScheduleItem
import com.example.service.CalendarSyncHelper
import com.example.ui.theme.DarkPill
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TimelineSection(
    schedules: List<ScheduleItem>,
    onAddClick: () -> Unit,
    onItemClick: (ScheduleItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Header
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
                        .background(Color(0xFF221538)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Timeline Icon",
                        tint = ElectricPurple,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "Timeline",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = onAddClick,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(DarkPill)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Schedule",
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Timeline Container Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(DarkSurface)
                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 18.dp, vertical = 20.dp)
        ) {
            if (schedules.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No routines scheduled yet.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Tap '+' or use AI Optimizer to build your day.",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onAddClick() }
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    schedules.forEachIndexed { index, item ->
                        TimelineNodeItem(
                            schedule = item,
                            isFirst = index == 0,
                            isLast = index == schedules.size - 1,
                            onClick = { onItemClick(item) },
                            onSyncCalendar = {
                                CalendarSyncHelper.syncEventToNativeCalendar(context, item)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineNodeItem(
    schedule: ScheduleItem,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    onSyncCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nodeColor = try {
        Color(android.graphics.Color.parseColor(schedule.colorHex))
    } catch (e: Exception) {
        if (schedule.timeString.contains("08:00")) ElectricPurple else NeonCyan
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Node Dot & Connector Line Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(16.dp)
        ) {
            // Node circle
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (schedule.isAutoSyncedCalendar) nodeColor else DarkSurface)
                    .border(2.dp, nodeColor, CircleShape)
            )

            // Connecting line down
            if (!isLast) {
                Spacer(
                    modifier = Modifier
                        .width(1.5.dp)
                        .height(64.dp)
                        .background(DarkSurfaceBorder)
                )
            }
        }

        // Content Column
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Time string
            Text(
                text = schedule.timeString,
                color = nodeColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            // Title
            Text(
                text = schedule.title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            // Subtitle description if present
            if (schedule.description.isNotBlank()) {
                Text(
                    text = schedule.description,
                    color = TextMuted,
                    fontSize = 12.sp,
                    maxLines = 2
                )
            }

            // Tags row (Prep Alert, Auto-Synced)
            if (schedule.earlyWarningMinutes > 0 || schedule.isAutoSyncedCalendar) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (schedule.earlyWarningMinutes > 0) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkPill)
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Alert",
                                tint = TextSecondary,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "Prep Alert (${schedule.earlyWarningMinutes}m)",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    if (schedule.isAutoSyncedCalendar) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkPill)
                                .clickable { onSyncCalendar() }
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Calendar Sync",
                                tint = NeonCyan,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "Auto-Synced",
                                color = NeonCyan,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
