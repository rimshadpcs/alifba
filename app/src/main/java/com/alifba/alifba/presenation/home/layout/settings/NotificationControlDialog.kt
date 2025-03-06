package com.alifba.alifba.presenation.home.layout.settings

import android.app.TimePickerDialog
import android.content.Context
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
import com.alifba.alifba.R
import com.alifba.alifba.service.LessonReminderReceiver
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.utils.ReminderPreferences
import formatTime

@Composable
fun NotificationDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    context: Context
) {
    var reminderTime by remember {
        mutableStateOf(ReminderPreferences.getReminderTime(context))
    }
    var showTimePicker by remember { mutableStateOf(false) }

    // Time Picker Logic using a local state variable
    if (showTimePicker) {
        TimePickerDialog(
            context,
            R.style.CustomTimePickerTheme,  // second parameter
            { _, hourOfDay, minute ->
                // Save the selected time
                ReminderPreferences.setReminderTime(context, hourOfDay, minute)

                // Set the reminder
                LessonReminderReceiver.setDailyReminder(context, hourOfDay, minute)

                // Update local state
                reminderTime = Pair(hourOfDay, minute)

                // Close both time picker and notification dialog
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
                    // Current Reminder Time Display
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

                    // Change Reminder Time Button
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
                TextButton(onClick = onDismiss) {
                    Text("Close", color = navyBlue)
                }
            }
        )
    }
}