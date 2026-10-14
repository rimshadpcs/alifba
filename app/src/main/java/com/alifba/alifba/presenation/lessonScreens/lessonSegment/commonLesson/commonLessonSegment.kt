package com.alifba.alifba.presenation.lessonScreens.lessonSegment.commonLesson

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.alifba.alifba.R
import com.alifba.alifba.data.models.LessonSegment
import com.alifba.alifba.presenation.main.logScreenView
import com.alifba.alifba.ui_components.widgets.*
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun CommonLessonSegment(
    segment: LessonSegment.CommonLesson,
    onNextClicked: () -> Unit,
    showNextButton: Boolean,
    isAudioPlaying: Boolean = false,
) {
    LaunchedEffect(Unit) {
        logScreenView("lesson_screen")
    }

    LessonPage(
        bottom = {
            // CONTINUE once the narration has finished; the speech animation while it plays.
            // Both live in the same pinned bottom slot as CHECK on the question screens.
            Box(
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isAudioPlaying -> SpeechAnimation()
                    showNextButton -> LessonPrimaryButton(title = "Continue", onClick = onNextClicked)
                }
            }
        }
    ) {
        // Image: 24 side margin (from LessonPage), radius 28
        Image(
            painter = rememberImagePainter(
                data = segment.image,
                builder = {
                    crossfade(true)
                    placeholder(R.drawable.loading_bar)
                    error(R.drawable.error)
                }
            ),
            contentDescription = "Image",
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LessonTheme.imageRadius)),
            contentScale = ContentScale.FillWidth
        )
        Spacer(Modifier.height(24.dp))
        // Story text: 24/500 in ink
        Text(
            text = segment.description,
            style = LessonTheme.story(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SpeechAnimation() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.speechanimation)
    )
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = Modifier.size(width = 120.dp, height = 66.dp)
    )
}
