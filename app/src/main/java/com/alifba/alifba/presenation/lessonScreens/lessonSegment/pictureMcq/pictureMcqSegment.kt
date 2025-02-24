package com.alifba.alifba.presenation.lessonScreens.lessonSegment.pictureMcq

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.alifba.alifba.R
import com.alifba.alifba.data.models.LessonSegment
import com.alifba.alifba.data.models.PictureMcqItem
import com.alifba.alifba.presenation.main.logScreenView
import com.alifba.alifba.ui_components.dialogs.LottieAnimationDialog
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.navyBlue

import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.alifba.alifba.ui_components.widgets.buttons.PictureButton
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import kotlinx.coroutines.delay
import com.alifba.alifba.ui_components.widgets.texts.CommonExplanationText as CommonExplanationText

@Composable
fun PictureMcqSegment(segment: LessonSegment.PictureMcqLesson, onNextClicked: () -> Unit, showNextButton: Boolean,) {
    val showNextButton = remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }
    val animationFinished = remember { mutableStateOf(false) }
    val context = LocalContext.current
    // Play audio if needed
    // PlayAudio(audioResId = segment.speech)
    LaunchedEffect(Unit) {
        logScreenView("lesson_screen")
    }
    LaunchedEffect(Unit) {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "PictureMcqSegment")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "PictureMcqSegment")
        }
    }
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        // Display the question text
        CommonExplanationText(
            text = segment.question,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
        )

        // Display the picture choices as buttons
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(segment.pictureChoices) { item: PictureMcqItem ->
                PictureButton(
                    onClick = {
                        if (item.answer) {
                            showNextButton.value = true
                            showDialog.value = true
                        }
                        else{
                            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator.vibrate(100)
                            }
                        }
                    },

                    buttonImage = item.image,
                    buttonText = item.choice
                )
            }
        }

        // Show dialog animation on correct answer
        if (showDialog.value) {
            LottieAnimationDialog(showDialog = showDialog, lottieFileRes = R.raw.tick)
            LaunchedEffect(showDialog.value) {
                delay(2000)
                showDialog.value = false
                animationFinished.value = true
            }
        }
    }

    // Show "Next" button after animation finishes
    if (showNextButton.value && animationFinished.value) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            CommonButton(
                onClick = onNextClicked,
                buttonText = "Next",
                mainColor = lightNavyBlue,
                shadowColor = navyBlue,
                textColor = white
            )
        }
    }
}


//@Preview(showBackground = true, backgroundColor = 0xFFFFFF, widthDp = 320, heightDp = 640)
//@Composable
//fun MCQSegmentPreview() {
//    val sampleSegment = LessonSegment.PictureMcqLesson(
//        question = "Which of this sunnah should we do after eating food ",
//        choices = listOf(
//            PictureMcqItem(R.drawable.game,"Play video game"),
//            PictureMcqItem(R.drawable.washhands,"wash your hands"),
//            PictureMcqItem(R.drawable.watchtv,"Watch Tv"),
//            PictureMcqItem(R.drawable.leavefood,"leave food on plate")
//
//        ),
//        image = R.drawable.food,
//        correctAnswer = "Prayer mat",
//        speech = R.raw.sunnah
//    )
//    PictureMcqSegment(segment = sampleSegment, onNextClicked = { /* Implement action */ })
//}
