package com.alifba.alifba.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationUtils {
    const val CHANNEL_LESSONS = "lessons"
    const val CHANNEL_STREAKS = "streaks"

    @SuppressLint("ServiceCast")
    fun createNotificationChannels(context: Context) {
        // Only needed for Android 8.0 (API level 26) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create Lessons channel
            val lessonsChannel = NotificationChannel(
                CHANNEL_LESSONS,
                "Daily Lessons",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for daily lesson reminders"
                enableVibration(true)
                enableLights(true)
            }

            // Create Streaks channel
            val streaksChannel = NotificationChannel(
                CHANNEL_STREAKS,
                "Streak Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for maintaining your learning streak"
                enableVibration(true)
                enableLights(true)
            }

            // Register both channels
            notificationManager.createNotificationChannels(listOf(lessonsChannel, streaksChannel))
        }
    }
}
