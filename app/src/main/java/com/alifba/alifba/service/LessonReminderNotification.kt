package com.alifba.alifba.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.alifba.alifba.R
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import java.util.Calendar

class LessonReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // When the alarm fires, create and display the notification.
        createLessonReminderNotification(context)
        
        // Reschedule for the next day to ensure continuous daily reminders
        rescheduleForNextDay(context)
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

        // Try displaying the notification with a unique ID to prevent duplicates.
        try {
            // Use a unique notification ID based on the current day to prevent duplicates
            val uniqueNotificationId = NOTIFICATION_ID + (System.currentTimeMillis() / (24 * 60 * 60 * 1000)).toInt()
            NotificationManagerCompat.from(context).notify(uniqueNotificationId, notification)
            Log.d(TAG, "Notification sent successfully with ID: $uniqueNotificationId")
        } catch (e: SecurityException) {
            Log.e(TAG, "Notification error: ${e.message}")
        }
    }

    /**
     * Reschedules the reminder for the next day using stored preferences
     */
    private fun rescheduleForNextDay(context: Context) {
        try {
            // Get the stored reminder time from preferences
            val reminderTime = com.alifba.alifba.utils.ReminderPreferences.getReminderTime(context)
            
            // Schedule for the next day
            setDailyReminder(context, reminderTime.first, reminderTime.second)
            
            Log.d(TAG, "Rescheduled reminder for next day at ${reminderTime.first}:${reminderTime.second}")
        } catch (e: Exception) {
            Log.e(TAG, "Error rescheduling reminder: ${e.message}")
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
            try {
                Log.d(TAG, "Setting daily reminder for $hour:$minute")
                
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(context, LessonReminderReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                // Cancel any existing alarm first to prevent duplicates
                alarmManager.cancel(pendingIntent)
                Log.d(TAG, "Cancelled existing alarms")

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
                
                Log.d(TAG, "Scheduling alarm for ${calendar.time}")

                // On Android 12+ check if the app can schedule exact alarms.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        // Schedule an exact alarm.
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                        Log.d(TAG, "Exact alarm scheduled for ${calendar.time}")
                    } else {
                        // Fallback: schedule an inexact alarm instead of forcing system settings.
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
                    // For older versions, setExact is allowed without special permissions.
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "Exact alarm scheduled for ${calendar.time}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set daily reminder: ${e.message}", e)
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
