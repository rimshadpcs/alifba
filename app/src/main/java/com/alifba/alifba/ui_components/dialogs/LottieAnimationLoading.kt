package com.alifba.alifba.ui_components.dialogs

import androidx.annotation.RawRes
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
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay


@Composable
fun LottieAnimationLoading(
    showDialog: MutableState<Boolean>,
    @RawRes lottieFileRes: Int,
    isTransparentBackground: Boolean = true,
    onAnimationEnd: (() -> Unit)? = null

) {
    if (showDialog.value) {
        Dialog(
            onDismissRequest = { showDialog.value = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isTransparentBackground) Color.Transparent else Color.White) // Toggle background
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimationCompos(lottieFileRes, Modifier.size(150.dp))
            }
        }

        // Delay to automatically dismiss the dialog after 2 seconds
        LaunchedEffect(key1 = showDialog.value) {
            if (showDialog.value) {
                delay(1500) // Animation duration
                showDialog.value = false
                onAnimationEnd?.invoke()
            }
        }
    }
}

@Composable
fun LottieAnimationCompos(@RawRes lottieFileRes: Int, modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieFileRes))
    LottieAnimation(composition, modifier = modifier)
}

