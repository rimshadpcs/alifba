import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.alifba.alifba.R
import com.alifba.alifba.service.LessonReminderReceiver
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.utils.ReminderPreferences
@Composable
fun ShowReminderTimePicker(
    onTimeSet: (Int, Int) -> Unit,
    title: String = "Daily Lesson Reminder",
    description: String = "Set a daily reminder to help your child stay consistent with learning. We'll send a gentle notification to encourage daily practice."
) {
    val context = LocalContext.current
    val initialReminderTime = ReminderPreferences.getReminderTime(context)

    var showReminderDialog by remember { mutableStateOf(true) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    
    // Auto-hide success message after 3 seconds
    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            delay(3000)
            showSuccessMessage = false
        }
    }

    if (showReminderDialog) {
        AlertDialog(
            onDismissRequest = {
                showReminderDialog = false
                onTimeSet(initialReminderTime.first, initialReminderTime.second)
            },
            containerColor = white,
            title = {
                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = navyBlue
                )
            },
            text = {
                Column {
                    // Success message
                    if (showSuccessMessage) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE8F5E8)
                            )
                        ) {
                            Text(
                                text = successMessage,
                                modifier = Modifier.padding(12.dp),
                                color = Color(0xFF2E7D2E),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    Text(
                        text = description,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Current Reminder Time",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = formatTime(initialReminderTime.first, initialReminderTime.second),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = navyBlue,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            TimePickerDialog(
                                context,
                                R.style.CustomTimePickerTheme,
                                { _, hourOfDay, minute ->
                                    try {
                                        // Apply reminder settings
                                        ReminderPreferences.setReminderTime(context, hourOfDay, minute)
                                        
                                        // Cancel existing reminder and set new one
                                        LessonReminderReceiver.cancelReminder(context)
                                        LessonReminderReceiver.setDailyReminder(context, hourOfDay, minute)

                                        // Show success message
                                        successMessage = "✅ Reminder updated to ${formatTime(hourOfDay, minute)}"
                                        showSuccessMessage = true

                                        // Callback with selected time
                                        onTimeSet(hourOfDay, minute)
                                        
                                        // Don't auto-dismiss, let user see success message
                                    } catch (e: Exception) {
                                        android.util.Log.e("ReminderTimePicker", "Error setting reminder: ${e.message}")
                                        // Still dismiss dialog and callback even if there's an error
                                        showReminderDialog = false
                                        onTimeSet(hourOfDay, minute)
                                    }
                                },
                                initialReminderTime.first,
                                initialReminderTime.second,
                                false
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = navyBlue,
                            contentColor = white
                        )
                    ) {
                        Text("Change Reminder Time")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showReminderDialog = false
                    onTimeSet(initialReminderTime.first, initialReminderTime.second)
                }) {
                    Text("Close", color = navyBlue)
                }
            }
        )
    }
}

// Utility function to format time
fun formatTime(hour: Int, minute: Int): String {
    val amPm = if (hour >= 12) "PM" else "AM"
    val formattedHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
    val formattedMinute = String.format("%02d", minute)
    return "$formattedHour:$formattedMinute $amPm"
}

