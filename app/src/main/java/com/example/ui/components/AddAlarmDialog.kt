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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entities.AlarmItem
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkPill
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AddAlarmDialog(
    onDismiss: () -> Unit,
    onSave: (AlarmItem) -> Unit,
    existingAlarm: AlarmItem? = null
) {
    var label by remember { mutableStateOf(existingAlarm?.label ?: "Morning Focus Alarm") }
    var hour by remember { mutableStateOf(existingAlarm?.hour?.toString() ?: "6") }
    var minute by remember { mutableStateOf(existingAlarm?.minute?.toString() ?: "30") }
    var isSmartAdaptive by remember { mutableStateOf(existingAlarm?.isSmartAdaptive ?: false) }
    var linkedSchedule by remember { mutableStateOf(existingAlarm?.linkedScheduleTitle ?: "08:00 AM Kickoff") }
    var earlyOffsetMinutes by remember { mutableStateOf(existingAlarm?.earlyOffsetMinutes?.toString() ?: "90") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DarkSurface)
                .border(1.dp, ElectricPurple.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (existingAlarm == null) "⏰ Create New Alarm" else "Edit Alarm",
                    color = NeonCyan,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Alarm Label", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Hour & Minute Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = hour,
                        onValueChange = { hour = it },
                        label = { Text("Hour (0-23)", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = minute,
                        onValueChange = { minute = it },
                        label = { Text("Minute (0-59)", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Smart Adaptive Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Smart Adaptive Alarm",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Dynamically adjusts to schedule kickoff",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = isSmartAdaptive,
                        onCheckedChange = { isSmartAdaptive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ElectricPurple,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = DarkPill
                        )
                    )
                }

                if (isSmartAdaptive) {
                    OutlinedTextField(
                        value = linkedSchedule,
                        onValueChange = { linkedSchedule = it },
                        label = { Text("Linked Schedule / Kickoff", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            val h = (hour.toIntOrNull() ?: 6).coerceIn(0, 23)
                            val m = (minute.toIntOrNull() ?: 30).coerceIn(0, 59)
                            val formattedTime = String.format(
                                "%02d:%02d %s",
                                if (h == 0) 12 else if (h > 12) h - 12 else h,
                                m,
                                if (h >= 12) "PM" else "AM"
                            )

                            val alarm = (existingAlarm ?: AlarmItem(
                                label = label,
                                timeString = formattedTime,
                                hour = h,
                                minute = m
                            )).copy(
                                label = label,
                                timeString = formattedTime,
                                hour = h,
                                minute = m,
                                isEnabled = true,
                                isSmartAdaptive = isSmartAdaptive,
                                linkedScheduleTitle = if (isSmartAdaptive) linkedSchedule else null,
                                earlyOffsetMinutes = earlyOffsetMinutes.toIntOrNull() ?: 90
                            )
                            onSave(alarm)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricPurple,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Alarm", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
