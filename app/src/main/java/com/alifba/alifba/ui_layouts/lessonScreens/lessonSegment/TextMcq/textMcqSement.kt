package com.alifba.alifba.ui_layouts.lessonScreens.lessonSegment.TextMcq

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alifba.alifba.R
import com.alifba.alifba.models.LessonSegment
import com.alifba.alifba.models.TextMcqItem
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.alifba.alifba.utils.PlayAudio
import com.alifba.alifba.ui_components.widgets.buttons.MCQChoiceButton
import com.alifba.alifba.ui_components.dialogs.LottieAnimationDialog
import kotlinx.coroutines.delay

@Composable
fun TextMcqSegment(segment: LessonSegment.TextMcqLessonItem, onNextClicked: () -> Unit) {
    val context = LocalContext.current
    val showNextButton = remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }
    val animationFinished = remember { mutableStateOf(false) }

    PlayAudio(audioResId = segment.speech)
    val alifbaFont = FontFamily(Font(R.font.more_sugar_regular, FontWeight.Normal))

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = segment.question,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            fontFamily = alifbaFont,
            modifier = Modifier.padding(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        segment.choices.forEachIndexed { index, choices ->
            val (mainColor, shadowColor) = getButtonColors(index)
            MCQChoiceButton(
                onClick = {
                    if (choices.answer) {
                        showNextButton.value = true
                        showDialog.value = true
                    }
                    Toast.makeText(context, choices.answer.toString(), Toast.LENGTH_SHORT).show()
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
                buttonText = "Next"
            )
        }
    }
}


fun getButtonColors(index: Int): Pair<Long, Long> {
    return when (index) {
        0 -> Pair(0xFFFF8AD1, 0xFFe57cbc) // Pink colors for the first choice
        1 -> Pair(0xFF26c1fc, 0xFF22ade2) // Blue colors for the second choice
        2 -> Pair(0xFF8DD54f, 0xFF7ebf47) // Green colors for the third choice
        else -> Pair(0xFFFFB525, 0xFFe5a221) // Yellow colors for additional choices
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF, widthDp = 320, heightDp = 640)
@Composable
fun TextMcqSegmentPreview() {
    val sampleSegment = LessonSegment.TextMcqLessonItem(
        question = "Who created this amazing world, the beautiful sky, the flowing river and the shining stars?",
        choices = listOf(
            TextMcqItem("Humans", false),
            TextMcqItem("Allah", true),
            TextMcqItem("Someone else", false)
        ),
        speech = R.raw.mcq_sample
    )
    TextMcqSegment(segment = sampleSegment, onNextClicked = { /* Implement action */ })
}
