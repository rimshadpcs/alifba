package com.alifba.alifba.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

fun shouldAskNotifications(): Boolean {
    // For demonstration, always returning true
    return true
}

/**
 * Composable Rationale Dialog explaining the benefits of enabling notifications.
 */
@Composable
fun NotificationPermissionRationale(
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enable Notifications") },
        text = {
            Text("Allow Alifba to send you daily lesson and streak reminders.")
        },
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text("Enable")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No thanks")
            }
        }
    )
}

/**
 * Actual function that requests notification permission on Android 13+.
 * On older Android versions, no permission needed for push notifications.
 */
fun requestNotificationPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        )
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            Log.d("Notifications", "Requesting POST_NOTIFICATIONS permission")
            ActivityCompat.requestPermissions(
                (context as Activity),
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                123
            )
        } else {
            Log.d("Notifications", "POST_NOTIFICATIONS permission already granted")
        }
    }
}
