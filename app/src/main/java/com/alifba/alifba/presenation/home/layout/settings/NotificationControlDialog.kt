package com.alifba.alifba.presenation.home.layout.settings

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.alifba.alifba.R
import com.alifba.alifba.service.LessonReminderReceiver
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.utils.ReminderPreferences
import com.alifba.alifba.utils.requestNotificationPermission
import formatTime
@Composable
fun NotificationDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    context: Context
) {
    var reminderTime by remember { mutableStateOf(ReminderPreferences.getReminderTime(context)) }
    var showTimePicker by remember { mutableStateOf(false) }

    if (showTimePicker) {
        TimePickerDialog(
            context,
            R.style.CustomTimePickerTheme,
            { _, hourOfDay, minute ->
                ReminderPreferences.setReminderTime(context, hourOfDay, minute)
                LessonReminderReceiver.setDailyReminder(context, hourOfDay, minute)
                reminderTime = Pair(hourOfDay, minute)
                showTimePicker = false
                onDismiss()
            },
            reminderTime.first,
            reminderTime.second,
            false
        ).show()
    }

    if (showDialog && !showTimePicker) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = white,
            title = {
                Text(
                    text = "Notifications & Reminders",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = navyBlue
                )
            },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Daily Reminder Time",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black
                        )
                        Text(
                            text = formatTime(reminderTime.first, reminderTime.second),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = navyBlue,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = navyBlue, contentColor = white)
                    ) {
                        Text("Change Reminder Time")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // Only call permission request when user taps this button.
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                           requestNotificationPermission(context)
                        }
                    }
                    // Mark permission as handled by saving the current reminder time.
                    ReminderPreferences.setReminderTime(context, reminderTime.first, reminderTime.second)
                    onDismiss()
                }) {
                    Text("Enable Notifications", color = navyBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Skip", color = navyBlue)
                }
            }
        )
    }
}
