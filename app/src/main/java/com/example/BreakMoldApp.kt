package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.service.NotificationHelper

class BreakMoldApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        // Warm up database
        AppDatabase.getDatabase(this)
    }
}
