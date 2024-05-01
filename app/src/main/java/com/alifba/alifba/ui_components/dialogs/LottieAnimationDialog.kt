package com.alifba.alifba.ui_components.dialogs

import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.Dialog
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay

@Composable
fun LottieAnimationDialog(showDialog: MutableState<Boolean>, @RawRes lottieFileRes: Int) {
    if (showDialog.value) {
        Dialog(onDismissRequest = { showDialog.value = false }) {
            LottieAnimationComposition(lottieFileRes)
        }


        LaunchedEffect(key1 = true) {
            delay(2000)
            showDialog.value = false
        }
    }
}

@Composable
fun LottieAnimationComposition(@RawRes lottieFileRes: Int) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieFileRes))
    LottieAnimation(composition)
}
