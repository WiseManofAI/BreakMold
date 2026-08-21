package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.AlarmItem
import com.example.ui.theme.DarkPill
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SmartAlarmCard(
    alarm: AlarmItem?,
    onToggle: (AlarmItem) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEnabled = alarm?.isEnabled == true
    val timeStr = alarm?.timeString ?: "06:30 AM"
    val linkedTitle = alarm?.linkedScheduleTitle ?: "08:00 AM Kickoff"

    // Parse time into digits and period (AM/PM)
    val parts = timeStr.trim().split(" ")
    val digits = parts.getOrNull(0) ?: "06:30"
    val period = parts.getOrNull(1) ?: "AM"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurface)
            .border(
                1.dp,
                if (isEnabled) ElectricPurple.copy(alpha = 0.4f) else DarkSurfaceBorder,
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(18.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Icon + SMART ADAPTIVE ALARM + Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = "Smart Alarm Icon",
                        tint = if (isEnabled) NeonCyan else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "SMART ADAPTIVE ALARM",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = {
                        if (alarm != null) onToggle(alarm)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ElectricPurple,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = DarkPill
                    ),
                    modifier = Modifier.size(width = 44.dp, height = 24.dp)
                )
            }

            // Big Alarm Time Display
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = digits,
                    color = if (isEnabled) TextPrimary else TextSecondary,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1.0).sp,
                    lineHeight = 44.sp
                )
                Text(
                    text = period,
                    color = if (isEnabled) TextPrimary.copy(alpha = 0.9f) else TextSecondary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // Synced Schedule Tag Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceElevated)
                    .border(0.8.dp, DarkSurfaceBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Synced Event",
                    tint = NeonCyan,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Synced with $linkedTitle",
                    color = TextPrimary.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
