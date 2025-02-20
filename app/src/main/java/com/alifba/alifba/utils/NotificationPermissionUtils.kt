package com.alifba.alifba.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.alifba.alifba.ui_components.theme.black
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.lightRed
import com.alifba.alifba.ui_components.theme.white

fun Context.shouldAskNotifications(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    } else {
        false // No need to ask on versions lower than Android 13
    }
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
        // Set a light background for the dialog
        containerColor = white,
        // Slight elevation to differentiate from the background
        tonalElevation = 4.dp,
        title = {
            Text(
                text = "Enable Notifications",
                // Use a larger title style and a primary color for emphasis
                style = MaterialTheme.typography.titleLarge,
                color = black
            )
        },
        text = {
            Text(
                text = "Alifba wants to send you daily reminders on lessons.",
                // Use a body style and a lighter on-surface color variant
                style = MaterialTheme.typography.bodyLarge,
                color = black
            )
        },
        confirmButton = {
            TextButton(
                onClick = onAccept,
                // Make the 'Yes' button stand out with the primary color
                colors = ButtonDefaults.textButtonColors(
                    containerColor = lightNavyBlue,
                    contentColor = white
                )
            ) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                // Use a secondary or surface variant color for the 'No' button
                colors = ButtonDefaults.textButtonColors(
                    containerColor = lightRed,
                    contentColor = white
                )
            ) {
                Text("No")
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
