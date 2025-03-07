package com.alifba.alifba.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.alifba.alifba.R
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import java.util.Calendar

class LessonReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // When the alarm fires, create and display the notification.
        createLessonReminderNotification(context)
    }

    /**
     * Builds and displays the lesson reminder notification.
     */
    private fun createLessonReminderNotification(context: Context) {
        Log.d(TAG, "Creating notification")

        // Prepare an intent to launch the app when the notification is tapped.
        val launchIntent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.apply {
                putExtra("track_notification", true)
                putExtra("notification_type", "daily_reminder")
                putExtra("notification_time", System.currentTimeMillis())
            }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Define a set of notification messages.
        val messages = listOf(
            "Assalamu Alaikum! 🌟 Let’s begin today’s lesson, Bismillah!",
            "Yay! Another chance to learn and grow InshaAllah! 🤩",
            "Ready to explore something new? Let’s say Bismillah and go! 🚀",
            "Your daily lesson is here, Ace it Inshallah! 📚",
            "Alhamdulillah for another day of fun learning! 🌈",
            "Time to unlock your awesomeness—Bismillah! 🌟",
            "Bright minds, big dreams! Let’s dive into today’s adventure! 💡",
            "Each lesson brings you closer to your goals—MashaAllah! 💫",
            "Learning is an amazing journey—ready, set, Bismillah! 🎉"
        )
        val randomMessage = messages.random()

        // Build the notification.
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.alifbatransround)
            .setContentTitle("Your lessons are waiting \uD83C\uDF19")
            .setContentText(randomMessage)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Try displaying the notification.
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            Log.d(TAG, "Notification sent successfully")
        } catch (e: SecurityException) {
            Log.e(TAG, "Notification error: ${e.message}")
        }
    }

    companion object {
        private const val CHANNEL_ID = "lesson_reminder_channel"
        private const val NOTIFICATION_ID = 1
        private const val REQUEST_CODE = 100
        private const val TAG = "LessonReminder"

        /**
         * Schedules a daily reminder alarm at the specified [hour] and [minute].
         * If the time is in the past for today, the alarm is scheduled for the next day.
         */
        fun setDailyReminder(context: Context, hour: Int, minute: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, LessonReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Configure the calendar time for the alarm.
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            // On Android 12+ check if the app can schedule exact alarms.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "Exact alarm scheduled for ${calendar.time}")
                } else {
                    // Optionally, direct the user to grant exact alarm permission.
                    val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    context.startActivity(settingsIntent)
                    // Fallback: schedule an inexact alarm.
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.w(TAG, "Exact alarm permission not granted; scheduled inexact alarm for ${calendar.time}")
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Log.d(TAG, "Exact alarm scheduled for ${calendar.time}")
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Log.d(TAG, "Exact alarm scheduled for ${calendar.time}")
            }
        }

        /**
         * Cancels any previously scheduled daily reminder alarm.
         */
        fun cancelReminder(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, LessonReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Reminder canceled")
        }
    }
}

/**
 * Logs notification events to Firebase Analytics.
 */
fun logNotificationEvent(
    eventName: String,
    notificationType: String,
    notificationTime: Long? = null,
    clickTime: Long? = null
) {
    val bundle = Bundle().apply {
        putString("notification_type", notificationType)
        notificationTime?.let { putLong("notification_time", it) }
        clickTime?.let { putLong("click_time", it) }
    }
    Firebase.analytics.logEvent(eventName, bundle)
}
