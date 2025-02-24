package com.alifba.alifba.presenation.lessonScreens.lessonSegment.TextMcq

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.alifba.alifba.R
import com.alifba.alifba.data.models.LessonSegment
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.alifba.alifba.ui_components.widgets.buttons.MCQChoiceButton
import com.alifba.alifba.ui_components.dialogs.LottieAnimationDialog
import com.alifba.alifba.ui_components.theme.darkCandyGreen
import com.alifba.alifba.ui_components.theme.darkPink
import com.alifba.alifba.ui_components.theme.darkSkyBlue
import com.alifba.alifba.ui_components.theme.darkYellow
import com.alifba.alifba.ui_components.theme.lightCandyGreen
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.lightPink
import com.alifba.alifba.ui_components.theme.lightSkyBlue
import com.alifba.alifba.ui_components.theme.lightYellow
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.texts.CommonExplanationText
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import kotlinx.coroutines.delay

@Composable
fun TextMcqSegment(
    segment: LessonSegment.TextMcqLesson,
    onNextClicked: () -> Unit,
) {
    val hasAnswered = remember { mutableStateOf(false) }  // New state to track if user has answered
    val showNextButton = remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }
    val animationFinished = remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "PictureMcqSegment")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "PictureMcqSegment")
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = if (hasAnswered.value && showNextButton.value && animationFinished.value) 80.dp else 0.dp)
        ) {
            CommonExplanationText(
                text = segment.question,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp)
            )

            segment.choices.forEachIndexed { index, choice ->
                val (mainColor, shadowColor) = getButtonColors(index)
                MCQChoiceButton(
                    onClick = {
                        if (choice.answer) {
                            hasAnswered.value = true
                            showDialog.value = true
                        } else {
                            // Just vibrate for wrong answer
                            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator.vibrate(100)
                            }
                        }
                    },
                    buttonText = choice.choice,
                    mainColor = mainColor,
                    shadowColor = shadowColor
                )
            }

            Spacer(Modifier.height(16.dp))

//            Image(
//                painter = painterResource(R.drawable.qna),
//                contentDescription = "QnA Image",
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clip(RoundedCornerShape(32.dp)),
//                contentScale = ContentScale.FillWidth
//            )
        }

        // Only show next button if user has answered correctly
        if (hasAnswered.value && showNextButton.value && animationFinished.value) {
            CommonButton(
                onClick = onNextClicked,
                buttonText = "Next",
                mainColor = lightNavyBlue,
                shadowColor = navyBlue,
                textColor = white,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
    }

    if (showDialog.value) {
        LottieAnimationDialog(showDialog = showDialog, lottieFileRes = R.raw.tick)
        LaunchedEffect(showDialog.value) {
            delay(2000)
            showDialog.value = false
            showNextButton.value = true
            animationFinished.value = true
        }
    }
}
fun getButtonColors(index: Int): Pair<Color, Color> {
    return when (index) {
        0 -> Pair(lightPink, darkPink)
        1 -> Pair(lightCandyGreen, darkCandyGreen)
        2 -> Pair(lightSkyBlue, darkSkyBlue)
        else -> Pair(lightYellow, darkYellow)
    }
}



