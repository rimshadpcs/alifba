package com.alifba.alifba.presenation.home.layout.settings

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.material.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.alifba.alifba.ui_components.theme.black
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.lightRed
import com.alifba.alifba.ui_components.theme.white
import okio.blackholeSink

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NotificationDialog(
    showDialog: Boolean,
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
                    color = black
                )
            },
            text = {
                Text(
                    text =
                        "Remember you need notifications to get lesson reminders",
                    color = black
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openAppNotificationSettings(context)
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor =  lightNavyBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Open settings" ,
                        style = MaterialTheme.typography.bodyLarge,
                        color = white
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = lightRed,
                        contentColor = white
                    )
                ) {
                    Text(text = "Cancel",
                        style = MaterialTheme.typography.bodyLarge,
                        color = white)

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
