package com.alifba.alifba.service

import android.annotation.SuppressLint
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
        // Create and show notification
        createLessonReminderNotification(context)
    }

    @SuppressLint("MissingPermission")
    private fun createLessonReminderNotification(context: Context) {
        Log.d("LessonReminder", "Creating notification")

        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)

        intent?.apply {
            putExtra("track_notification", true)
            putExtra("notification_type", "daily_reminder")
            putExtra("notification_time", System.currentTimeMillis())
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationMessages = listOf(
            "Assalamu Alaikum! 🌟 Let’s begin today’s lesson, Bismillah!",
            "Yay! Another chance to learn and grow InshaAllah! 🤩",
            "Ready to explore something new? Let’s say Bismillah and go! 🚀",
            "Your daily lesson is here,Ace it Inshallah! 📚",
            "Alhamdulillah for another day of fun learning! 🌈",
            "Time to unlock your awesomeness—Bismillah! 🌟",
            "Bright minds, big dreams! Let’s dive into today’s adventure! 💡",
            "Each lesson brings you closer to your goals—MashaAllah! 💫",
            "Learning is an amazing journey—ready, set, Bismillah! 🎉"
        )
        val randomMessage = notificationMessages.random()
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.alifbatransround)
            .setContentTitle("Your lessons are waiting \uD83C\uDF19 ")
            .setContentText(randomMessage)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, builder.build())
                Log.d("LessonReminder", "Notification sent successfully")
            }
        } catch (e: SecurityException) {
            Log.e("LessonReminder", "Permission error: ${e.message}")
        }


    }

    companion object {
        private const val CHANNEL_ID = "lesson_reminder_channel"
        private const val NOTIFICATION_ID = 1
        private const val REQUEST_CODE = 100

        // Method to set a daily reminder at a specific time
// In LessonReminderReceiver
        @SuppressLint("MissingPermission", "ObsoleteSdkInt", "ScheduleExactAlarm")
        fun setDailyReminder(context: Context, hour: Int, minute: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, LessonReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Use setExactAndAllowWhileIdle for more reliable alarms
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)

                // If the time is in the past, add a day
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            // Use setExactAndAllowWhileIdle for more reliable alarms
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }

            Log.d("LessonReminder", "Reminder set for ${calendar.time}")
        }
        // Method to cancel the reminder
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
            Log.d("LessonReminder", "Reminder canceled")
        }
    }
}
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