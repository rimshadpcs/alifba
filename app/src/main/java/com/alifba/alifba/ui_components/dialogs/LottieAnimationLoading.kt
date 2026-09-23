package com.alifba.alifba.ui_components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.alifba.alifba.ui_components.widgets.DotLottieView
import kotlinx.coroutines.delay


@Composable
fun LottieAnimationLoading(
    showDialog: MutableState<Boolean>,
    isTransparentBackground: Boolean = true,
    onAnimationEnd: (() -> Unit)? = null,
    autoDismissMillis: Long? = 1500L,
    isCancelable: Boolean = true,
    lottieName: String = "moon_waiting"
) {
    if (showDialog.value) {
        Dialog(
            onDismissRequest = {
                if (isCancelable) {
                    showDialog.value = false
                }
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = isCancelable,
                dismissOnClickOutside = isCancelable
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isTransparentBackground) Color.Transparent else Color.White) // Toggle background
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                DotLottieView(name = lottieName, modifier = Modifier.size(150.dp))
            }
        }

        // Delay to automatically dismiss the dialog if requested
        if (autoDismissMillis != null) {
            LaunchedEffect(key1 = showDialog.value, key2 = autoDismissMillis) {
                if (showDialog.value) {
                    delay(autoDismissMillis)
                    if (showDialog.value) {
                        showDialog.value = false
                        onAnimationEnd?.invoke()
                    }
                }
            }
        }
    }
}
