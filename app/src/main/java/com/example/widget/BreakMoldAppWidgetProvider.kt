package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class BreakMoldAppWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_COMPLETE_TASK = "com.example.ACTION_WIDGET_COMPLETE_TASK"
        const val EXTRA_TASK_ID = "extra_task_id"

        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, BreakMoldAppWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
                ComponentName(context, BreakMoldAppWidgetProvider::class.java)
            )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_COMPLETE_TASK) {
            val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
            if (taskId != -1L) {
                CoroutineScope(Dispatchers.IO).launch {
                    val db = AppDatabase.getDatabase(context)
                    val task = db.breakMoldDao().getTargetById(taskId)
                    if (task != null && !task.isCompleted) {
                        db.breakMoldDao().updateTarget(task.copy(isCompleted = true, completedAt = System.currentTimeMillis()))
                        val userXp = db.breakMoldDao().getUserXp()
                        if (userXp != null) {
                            val newXp = userXp.totalXp + task.xpReward
                            val newLevelXp = userXp.currentLevelXp + task.xpReward
                            db.breakMoldDao().insertOrUpdateUserXp(
                                userXp.copy(
                                    totalXp = newXp,
                                    currentLevelXp = newLevelXp,
                                    todayXpGained = userXp.todayXpGained + task.xpReward
                                )
                            )
                        }
                    }
                    updateAllWidgets(context)
                }
            }
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.breakmold_appwidget)

        // Launch app on background click
        val openAppIntent = Intent(context, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val userXp = db.breakMoldDao().getUserXp()
            val smartAlarm = db.breakMoldDao().getSmartAdaptiveAlarm()
            val targets = db.breakMoldDao().getAllTargetsFlow().firstOrNull() ?: emptyList()
            val nextTarget = targets.firstOrNull { !it.isCompleted }

            if (userXp != null) {
                views.setTextViewText(R.id.widget_xp_text, "LVL ${userXp.level} • ${userXp.currentLevelXp}/${userXp.targetLevelXp} XP")
            }

            if (smartAlarm != null && smartAlarm.isEnabled) {
                views.setTextViewText(R.id.widget_alarm_time, smartAlarm.timeString)
                views.setTextViewText(R.id.widget_alarm_desc, "⚡ Synced with ${smartAlarm.linkedScheduleTitle ?: "Schedule"}")
                views.setViewVisibility(R.id.widget_alarm_container, View.VISIBLE)
            } else {
                views.setTextViewText(R.id.widget_alarm_time, "Adaptive Alarm Off")
                views.setTextViewText(R.id.widget_alarm_desc, "Tap to enable auto-sync")
            }

            if (nextTarget != null) {
                views.setViewVisibility(R.id.widget_task_container, View.VISIBLE)
                views.setTextViewText(R.id.widget_task_title, "🎯 ${nextTarget.title} (+${nextTarget.xpReward} XP)")

                val completeIntent = Intent(context, BreakMoldAppWidgetProvider::class.java).apply {
                    action = ACTION_COMPLETE_TASK
                    putExtra(EXTRA_TASK_ID, nextTarget.id)
                }
                val completePendingIntent = PendingIntent.getBroadcast(
                    context,
                    nextTarget.id.toInt(),
                    completeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_complete_task, completePendingIntent)
            } else {
                views.setTextViewText(R.id.widget_task_title, "All Daily Targets Complete! 🔥")
                views.setViewVisibility(R.id.widget_btn_complete_task, View.GONE)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
