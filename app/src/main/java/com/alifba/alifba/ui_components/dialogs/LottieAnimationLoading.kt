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
fun LottieAnimationLoading(showDialog: MutableState<Boolean>, @RawRes lottieFileRes: Int) {
    if (showDialog.value) {
        Dialog(
            onDismissRequest = { showDialog.value = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false, // Prevent default width to avoid excess dimming
            )
        ) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimationCompos(lottieFileRes, Modifier.size(150.dp))
            }
        }

        LaunchedEffect(key1 = true) {
            delay(2000)
            showDialog.value = false
        }
    }


}
@Composable
fun LottieAnimationCompos(@RawRes lottieFileRes: Int, modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieFileRes))
    LottieAnimation(composition, modifier = modifier)
}