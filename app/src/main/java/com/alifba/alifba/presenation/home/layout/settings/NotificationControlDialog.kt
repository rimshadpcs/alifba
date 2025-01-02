package com.alifba.alifba.presenation.home.layout.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.Switch
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

@Composable
fun NotificationDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    isNotificationsEnabled: Boolean,
    onToggleNotification: (Boolean) -> Unit
) {
    if (showDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Notifications",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black // Forced dark text
                    )
                )
            },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .background(Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)) // Light background
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Enable Notifications",
                        style = TextStyle(
                            fontSize = 18.sp,
                            color = Color.Black // Text color for light background
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isNotificationsEnabled,
                        onCheckedChange = onToggleNotification,
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            checkedThumbColor = Color.Blue,
                            uncheckedThumbColor = Color.Gray
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.background(Color.LightGray, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "OK",
                        style = TextStyle(color = Color.Black)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.background(Color.LightGray, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "Cancel",
                        style = TextStyle(color = Color.Black)
                    )
                }
            },
            containerColor = Color.White, // Light background for the dialog
            tonalElevation = 8.dp // Elevation for better visibility
        )
    }
}
