package com.alifba.alifba.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.alifba.alifba.R
import java.util.Calendar

class SubscriptionTrialReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive triggered")
        val message = intent.getStringExtra(EXTRA_MESSAGE)
            ?: "We hope you keep learning! Your subscription starts soon. If you change your mind, you can manage it directly in the App Store."
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Trial Reminder"
        val planType = intent.getStringExtra(EXTRA_PLAN_TYPE) ?: "unknown"
        val action = intent.getStringExtra(EXTRA_ACTION)

        val pendingIntent = if (action == ACTION_OPEN_DISCOUNT) {
            val mainIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                putExtra("navigate_to", "discountPaywall")
                putExtra("track_notification", true)
                putExtra("notification_type", "discount_reminder")
                putExtra("notification_time", System.currentTimeMillis())
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            PendingIntent.getActivity(
                context,
                REQUEST_CODE_DISCOUNT,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            val subscriptionUri = android.net.Uri.parse(
                "https://play.google.com/store/account/subscriptions?package=${context.packageName}"
            )
            val playStoreIntent = Intent(Intent.ACTION_VIEW, subscriptionUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            PendingIntent.getActivity(
                context,
                0,
                playStoreIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.alifbatransround)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            val notificationId = NOTIFICATION_ID_BASE + planType.hashCode()
            Log.d(TAG, "Posting notification: id=$notificationId, title=$title")
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            Log.e(TAG, "Notification error: ${e.message}")
        }
    }

    companion object {
        private const val CHANNEL_ID = "lesson_reminder_channel"
        private const val TAG = "SubscriptionReminder"
        private const val NOTIFICATION_ID_BASE = 2000
        private const val REQUEST_CODE_MONTHLY = 2201
        private const val REQUEST_CODE_ANNUAL = 2202
        private const val REQUEST_CODE_DISCOUNT = 2203
        private const val EXTRA_MESSAGE = "extra_message"
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_PLAN_TYPE = "extra_plan_type"
        private const val EXTRA_ACTION = "extra_action"
        private const val ACTION_OPEN_DISCOUNT = "action_open_discount"

        fun scheduleDiscountReminder(context: Context, triggerAtMillis: Long) {
            Log.d(TAG, "scheduleDiscountReminder: triggerAtMillis=$triggerAtMillis")
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, SubscriptionTrialReminderReceiver::class.java).apply {
                putExtra(EXTRA_TITLE, "Salam!")
                putExtra(EXTRA_MESSAGE, "Your special offer expires in 2 hours. Claim it now to unlock all features! 🎁")
                putExtra(EXTRA_PLAN_TYPE, "discount_reminder")
                putExtra(EXTRA_ACTION, ACTION_OPEN_DISCOUNT)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_DISCOUNT,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        }

        fun cancelDiscountReminder(context: Context) {
            cancelReminder(context, REQUEST_CODE_DISCOUNT)
        }

        fun scheduleMonthlyReminder(context: Context, testDelayMillis: Long? = null) {
            scheduleReminder(
                context = context,
                requestCode = REQUEST_CODE_MONTHLY,
                daysFromNow = 2,
                title = "Trial Reminder",
                message = "We hope you keep learning! Your subscription starts in 1 day. If you change your mind, you can manage it directly in the App Store.",
                planType = "monthly",
                testDelayMillis = testDelayMillis
            )
            cancelReminder(context, REQUEST_CODE_ANNUAL)
        }

        fun scheduleAnnualReminder(context: Context, testDelayMillis: Long? = null) {
            scheduleReminder(
                context = context,
                requestCode = REQUEST_CODE_ANNUAL,
                daysFromNow = 3,
                title = "Trial Reminder",
                message = "We hope you keep learning! Your subscription starts in 2 days. If you change your mind, you can manage it directly in the App Store.",
                planType = "annual",
                testDelayMillis = testDelayMillis
            )
            cancelReminder(context, REQUEST_CODE_MONTHLY)
        }

        fun cancelAll(context: Context) {
            cancelReminder(context, REQUEST_CODE_MONTHLY)
            cancelReminder(context, REQUEST_CODE_ANNUAL)
            cancelReminder(context, REQUEST_CODE_DISCOUNT)
        }

        private fun scheduleReminder(
            context: Context,
            requestCode: Int,
            daysFromNow: Int,
            title: String,
            message: String,
            planType: String,
            testDelayMillis: Long? = null
        ) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, SubscriptionTrialReminderReceiver::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_MESSAGE, message)
                putExtra(EXTRA_PLAN_TYPE, planType)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerAtMillis = if (testDelayMillis != null) {
                System.currentTimeMillis() + testDelayMillis
            } else {
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                    add(Calendar.DAY_OF_YEAR, daysFromNow)
                    set(Calendar.HOUR_OF_DAY, 20)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (timeInMillis <= System.currentTimeMillis()) {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                }
                calendar.timeInMillis
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        }

        private fun cancelReminder(context: Context, requestCode: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, SubscriptionTrialReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}
