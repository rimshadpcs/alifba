package com.alifba.alifba.presenation.home.layout.settings

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.alifba.alifba.ui_components.theme.black
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.lightRed
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NotificationDialog(
    showDialog: Boolean,
    isNotificationsEnabled: Boolean,
    onDismiss: () -> Unit,
    context: Context
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color.White, // Light background for the dialog
            tonalElevation = 4.dp, // Slight elevation for contrast
            title = {
                Text(
                    text = "Notification Settings",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black
                )
            },
            text = {
                Text(
                    text = if (isNotificationsEnabled) {
                        "Notifications are currently enabled. You can turn them off in system settings."
                    } else {
                        "Notifications are currently disabled. You can enable them in system settings."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openAppNotificationSettings(context)
                        onDismiss() // Close dialog after action
                    },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (isNotificationsEnabled) (lightNavyBlue) else (lightRed), // Red for "Turn Off", Blue for "Turn On"
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (isNotificationsEnabled) "Turn Off" else "Turn On",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = Color.LightGray,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun openAppNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    }
    context.startActivity(intent)
}

fun areNotificationsEnabled(context: Context): Boolean {
    return NotificationManagerCompat.from(context).areNotificationsEnabled()
}
