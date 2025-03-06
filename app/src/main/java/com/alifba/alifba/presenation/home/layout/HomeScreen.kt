package com.alifba.alifba.presenation.home.layout

import ShowReminderTimePicker
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.alifba.alifba.R
import com.alifba.alifba.presenation.home.HomeViewModel
import com.alifba.alifba.service.LessonReminderReceiver
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager
import com.alifba.alifba.utils.NotificationPermissionRationale
import com.alifba.alifba.utils.ReminderPreferences
import com.alifba.alifba.utils.requestNotificationPermission
import com.alifba.alifba.utils.shouldAskNotifications
import com.onesignal.notifications.internal.common.NotificationHelper.areNotificationsEnabled
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(viewModel: HomeViewModel, navController: NavController) {
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isNotificationsEnabled by remember { mutableStateOf(areNotificationsEnabled(context)) }
    var showPermissionDialog by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED &&
                    !ReminderPreferences.isNotificationPermissionHandled(context)
        )
    }
    var showReminderTimePicker by remember { mutableStateOf(false) }
    var reminderTime by remember {
        mutableStateOf(ReminderPreferences.getReminderTime(context))
    }


    // Function to set lesson reminder
    fun setLessonReminder(context: Context, hour: Int, minute: Int) {
        LessonReminderReceiver.setDailyReminder(context, hour, minute)
    }

    // Handle notification permission dialog
    if (showPermissionDialog) {
        NotificationPermissionRationale(
            onDismiss = {
                showPermissionDialog = false
                // Set default reminder time if user declines
                val defaultTime = Pair(18, 30)
                ReminderPreferences.setReminderTime(context, defaultTime.first, defaultTime.second)
                LessonReminderReceiver.setDailyReminder(context, defaultTime.first, defaultTime.second)
            },
            onAccept = {
                showPermissionDialog = false
                requestNotificationPermission(context)
                // After permission is granted, show time picker
                showReminderTimePicker = true
            }
        )
    }

    if (showReminderTimePicker) {
        ShowReminderTimePicker(
            onTimeSet = { hour, minute ->
                reminderTime = Pair(hour, minute)
                showReminderTimePicker = false
            },
            title = "Choose a Reminder Time",
            description = "Set the time for your daily lesson reminder."
        )
    }


    // Set initial reminder
    LaunchedEffect(Unit) {
        setLessonReminder(context, reminderTime.first, reminderTime.second)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.weight(.3f))
        LazyRow(
            reverseLayout = true,
            state = scrollState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(viewModel.levelItemList.size) { index ->
                val item = viewModel.levelItemList[index]
                Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                    LessonMenuItems(
                        image = item.image,
                        name = item.name,
                        onClick = {
                            navController.navigate("lessonPathScreen/${item.levelId}")
                        }
                    )
                }
            }
        }

        // Navigation arrows at the bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left arrow for scrolling to show more items on the left
            if (scrollState.firstVisibleItemIndex + scrollState.layoutInfo.visibleItemsInfo.size < viewModel.levelItemList.size) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            SoundEffectManager.playClickSound()
                            delay(100)
                            val itemIndex = (scrollState.firstVisibleItemIndex + 1).coerceAtMost(viewModel.levelItemList.size - 1)
                            scrollState.animateScrollToItem(itemIndex)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.leftarrow),
                        contentDescription = "Show More Left",
                        modifier = Modifier.size(64.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(64.dp))  // Placeholder to keep alignment
            }

            // Right arrow for going back to the right
            if (scrollState.firstVisibleItemIndex > 0) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            SoundEffectManager.playClickSound()
                            delay(100)
                            val itemIndex = (scrollState.firstVisibleItemIndex - 1).coerceAtLeast(0)
                            scrollState.animateScrollToItem(itemIndex)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.rightarrow),
                        contentDescription = "Go Right",
                        modifier = Modifier.size(64.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(64.dp))  // Placeholder to keep alignment
            }
        }
    }
}