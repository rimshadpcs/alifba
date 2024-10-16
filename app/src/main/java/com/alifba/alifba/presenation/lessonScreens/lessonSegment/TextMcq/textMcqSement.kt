package com.alifba.alifba.presenation.lessonScreens.lessonSegment.TextMcq

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alifba.alifba.R
import com.alifba.alifba.data.models.LessonSegment
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.alifba.alifba.utils.PlayAudio
import com.alifba.alifba.ui_components.widgets.buttons.MCQChoiceButton
import com.alifba.alifba.ui_components.dialogs.LottieAnimationDialog
import com.alifba.alifba.ui_components.theme.darkCandyGreen
import com.alifba.alifba.ui_components.theme.darkPink
import com.alifba.alifba.ui_components.theme.darkPurple
import com.alifba.alifba.ui_components.theme.darkSkyBlue
import com.alifba.alifba.ui_components.theme.darkYellow
import com.alifba.alifba.ui_components.theme.lightCandyGreen
import com.alifba.alifba.ui_components.theme.lightPink
import com.alifba.alifba.ui_components.theme.lightPurple
import com.alifba.alifba.ui_components.theme.lightSkyBlue
import com.alifba.alifba.ui_components.theme.lightYellow
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.texts.CommonExplanationText
import kotlinx.coroutines.delay

@Composable
fun TextMcqSegment(segment: LessonSegment.TextMcqLesson, onNextClicked: () -> Unit) {
    val context = LocalContext.current
    val showNextButton = remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }
    val animationFinished = remember { mutableStateOf(false) }

    //PlayAudio(audioResId = segment.speech.toInt())
    val alifbaFont = FontFamily(Font(R.font.more_sugar_regular, FontWeight.Normal))

    Column(modifier = Modifier.padding(16.dp)) {
//        Text(
//            text = segment.question,
//            style = MaterialTheme.typography.bodyLarge,
//            color = Color.Gray,
//            fontFamily = alifbaFont,
//            modifier = Modifier.padding(12.dp)
//        )
        CommonExplanationText(text = segment.question,modifier =Modifier
            .align(Alignment.CenterHorizontally) )
        Spacer(modifier = Modifier.height(16.dp))

        segment.choices.forEachIndexed { index, choices ->
            val (mainColor, shadowColor) = getButtonColors(index)
            MCQChoiceButton(
                onClick = {
                    if (choices.answer) {
                        showNextButton.value = true
                        showDialog.value = true
                    }
                        //Toast.makeText(context, choices.answer.toString(), Toast.LENGTH_SHORT).show()
                },
                buttonText = choices.choice,
                mainColor = mainColor,
                shadowColor = shadowColor
            )
        }

        if (showDialog.value) {
            LottieAnimationDialog(showDialog = showDialog, lottieFileRes = R.raw.tick)
            LaunchedEffect(showDialog.value) {
                delay(2000)  // Assuming 2000 milliseconds animation duration
                showDialog.value = false
                animationFinished.value = true  // Set the animation finish state to true

            }
        }
    }
    if (showNextButton.value && animationFinished.value) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            CommonButton(
                onClick = onNextClicked,
                buttonText = "Next",
                mainColor = lightPurple,
                shadowColor = darkPurple,
                textColor = white
            )
        }
    }
}


fun getButtonColors(index: Int): Pair<Color, Color> {
    return when (index) {
        0 -> Pair(lightPink, darkPink) // Pink colors for the first choice
        1 -> Pair(lightCandyGreen, darkCandyGreen) // Blue colors for the second choice
        2 -> Pair(lightSkyBlue, darkSkyBlue) // Green colors for the third choice
        else -> Pair(lightYellow, darkYellow) // Yellow colors for additional choices
    }
}

//@Preview(showBackground = true, backgroundColor = 0xFFFFFF, widthDp = 320, heightDp = 640)
//@Composable
//fun TextMcqSegmentPreview() {
//    val sampleSegment = LessonSegment.TextMcqLesson(
//        question = "Who created this amazing world, the beautiful sky, the flowing river and the shining stars?",
//        choices = listOf(
//            TextMcqItem("Humans", false),
//            TextMcqItem("Allah", true),
//            TextMcqItem("Someone else", false)
//        ),
//        speech = R.raw.mcq_sample
//    )
//    TextMcqSegment(segment = sampleSegment, onNextClicked = { /* Implement action */ })
//}
