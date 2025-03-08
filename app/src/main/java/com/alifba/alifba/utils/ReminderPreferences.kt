package com.alifba.alifba.utils

import android.content.Context
import android.content.SharedPreferences

object ReminderPreferences {
    private const val PREFS_NAME = "ReminderPreferences"
    private const val KEY_REMINDER_HOUR = "reminder_hour"
    private const val KEY_REMINDER_MINUTE = "reminder_minute"
    private const val KEY_NOTIFICATION_PERMISSION_HANDLED = "notification_permission_handled"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setReminderTime(context: Context, hour: Int, minute: Int) {
        getPreferences(context).edit().apply {
            putInt(KEY_REMINDER_HOUR, hour)
            putInt(KEY_REMINDER_MINUTE, minute)
            putBoolean(KEY_NOTIFICATION_PERMISSION_HANDLED, true)
            apply()
        }
    }

    fun setNotificationPermissionHandled(context: Context, handled: Boolean) {
        getPreferences(context).edit().apply {
            putBoolean(KEY_NOTIFICATION_PERMISSION_HANDLED, handled)
            apply()
        }
    }

    fun getReminderTime(context: Context): Pair<Int, Int> {
        val prefs = getPreferences(context)
        // Default to 6:30 PM if not set
        val hour = prefs.getInt(KEY_REMINDER_HOUR, 18)
        val minute = prefs.getInt(KEY_REMINDER_MINUTE, 30)
        return Pair(hour, minute)
    }

    fun isNotificationPermissionHandled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_NOTIFICATION_PERMISSION_HANDLED, false)
    }
}