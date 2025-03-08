package com.alifba.alifba.presenation.home.layout

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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.alifba.alifba.R
import com.alifba.alifba.presenation.home.HomeViewModel
import com.alifba.alifba.presenation.home.layout.settings.NotificationDialog
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager
import com.alifba.alifba.utils.ReminderPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navController: NavController,
    isUserLoggedIn: Boolean
) {
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Read the user’s preferred reminder time.
    var reminderTime by remember {
        mutableStateOf(ReminderPreferences.getReminderTime(context))
    }

    // Show the reminder dialog only if notifications haven’t been “handled” yet.
    var showReminderDialog by remember {
        mutableStateOf(!ReminderPreferences.isNotificationPermissionHandled(context))
    }

    // -- Removed the old LaunchedEffect that automatically scheduled the alarm. --

    // If the dialog should show, display it. The user decides to skip or enable.
    if (showReminderDialog) {
        NotificationDialog(
            showDialog = showReminderDialog,
            onDismiss = {
                showReminderDialog = false
            },
            context = context
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.weight(0.3f))

        // Example of a scrollable row of items (lessons).
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

        // Navigation arrows at the bottom to scroll left/right through the LazyRow.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (scrollState.firstVisibleItemIndex +
                scrollState.layoutInfo.visibleItemsInfo.size <
                viewModel.levelItemList.size
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            SoundEffectManager.playClickSound()
                            delay(100)
                            val itemIndex = (scrollState.firstVisibleItemIndex + 1)
                                .coerceAtMost(viewModel.levelItemList.size - 1)
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
                Spacer(modifier = Modifier.size(64.dp))
            }
            if (scrollState.firstVisibleItemIndex > 0) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            SoundEffectManager.playClickSound()
                            delay(100)
                            val itemIndex = (scrollState.firstVisibleItemIndex - 1)
                                .coerceAtLeast(0)
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
                Spacer(modifier = Modifier.size(64.dp))
            }
        }
    }
}
