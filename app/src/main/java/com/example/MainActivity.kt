package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AlarmTriggerDialog
import com.example.ui.screens.AlarmsSettingsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MilestonesRewardsScreen
import com.example.ui.screens.ScheduleOptimizerScreen
import com.example.ui.theme.BreakMoldTheme
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkPill
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.ElectricMagenta
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.BreakMoldViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BreakMoldViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BreakMoldTheme(darkTheme = true) {
                // Request Notification Permission on Android 13+
                val context = LocalContext.current
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { /* Granted or denied */ }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
                val activeAlarm by viewModel.activeAlarmTrigger.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = DarkCanvas,
                    bottomBar = {
                        BreakMoldBottomNav(
                            selectedTab = selectedTab,
                            onTabSelected = { viewModel.setSelectedTab(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = innerPadding.calculateTopPadding())
                    ) {
                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "tabSwitch"
                        ) { tab ->
                            when (tab) {
                                0 -> DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToMilestones = { viewModel.setSelectedTab(2) }
                                )
                                1 -> ScheduleOptimizerScreen(viewModel = viewModel)
                                2 -> MilestonesRewardsScreen(viewModel = viewModel)
                                3 -> AlarmsSettingsScreen(viewModel = viewModel)
                            }
                        }

                        // Ringing Alarm Trigger Dialog
                        if (activeAlarm != null) {
                            AlarmTriggerDialog(
                                alarm = activeAlarm!!,
                                onDismiss = { viewModel.dismissActiveAlarm(activeAlarm!!) },
                                onSnooze = { viewModel.dismissActiveAlarm(activeAlarm!!) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BreakMoldBottomNav(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        NavItem(0, "Dashboard", Icons.Default.Dashboard),
        NavItem(1, "AI Optimizer", Icons.Default.AutoAwesome),
        NavItem(2, "Milestones", Icons.Default.EmojiEvents),
        NavItem(3, "Alarms", Icons.Default.Alarm)
    )

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp + navBarPadding),
        contentAlignment = Alignment.Center
    ) {
        // Floating Dark Capsule
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(32.dp))
                .background(DarkSurface)
                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(32.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val isSelected = selectedTab == item.index
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) ElectricPurple else Color.Transparent)
                        .clickable { onTabSelected(item.index) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) Color.White else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

data class NavItem(
    val index: Int,
    val label: String,
    val icon: ImageVector
)
