package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.UserXpState
import com.example.ui.theme.DarkPill
import com.example.ui.theme.ElectricMagenta
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary

@Composable
fun XpLevelHeader(
    userXp: UserXpState,
    onHeaderClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo & Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Isometric Cube Symbol
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkPill)
                    .border(1.dp, NeonCyan.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "BM",
                    color = NeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Text(
                text = "Break\nMold",
                color = NeonCyan,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 16.sp,
                letterSpacing = (-0.5).sp
            )
        }

        // XP & Level Pill
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1E1635)) // Dark purple tint
                .border(1.dp, ElectricMagenta.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .clickable { onHeaderClick() }
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "LVL\n${userXp.level}",
                color = NeonCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 11.sp
            )

            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(TextPrimary.copy(alpha = 0.7f))
            )

            Text(
                text = "${userXp.currentLevelXp}/${(userXp.targetLevelXp / 1000)}K\nXP",
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 12.sp
            )
        }
    }
}
