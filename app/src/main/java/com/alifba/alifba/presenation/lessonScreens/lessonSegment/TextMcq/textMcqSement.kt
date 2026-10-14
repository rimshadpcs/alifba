package com.alifba.alifba.presenation.lessonScreens.lessonSegment.TextMcq

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
fun TextMcqSegment(
    segment: LessonSegment.TextMcqLesson,
    onNextClicked: () -> Unit,
    isLastSegment: Boolean = false,
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var phase by remember { mutableStateOf(AnswerPhase.Answering) }
    val context = LocalContext.current
    val isTablet = LessonTheme.isTablet()

    LaunchedEffect(Unit) { SoundEffectManager.initialize(context) }

    fun stateFor(index: Int) = when {
        selectedIndex != index -> AnswerState.Normal
        phase == AnswerPhase.Answering -> AnswerState.Selected
        phase == AnswerPhase.Correct -> AnswerState.Correct
        else -> AnswerState.Wrong
    }

    fun check() {
        val index = selectedIndex ?: return
        val choice = segment.choices.getOrNull(index) ?: return
        if (choice.answer) {
            SoundEffectManager.playCorrectSound()
            phase = AnswerPhase.Correct
        } else {
            SoundEffectManager.playWrongSound()
            phase = AnswerPhase.Wrong
            SoundEffectManager.wrongAnswerHaptic()
        }
    }

    Box(Modifier.fillMaxSize()) {
        LessonPage(
            bottom = {
                LessonPrimaryButton(
                    title = "Check",
                    kind = if (selectedIndex == null) LessonButtonKind.Disabled else LessonButtonKind.Primary,
                    onClick = ::check
                )
            }
        ) {
            Text(
                text = segment.question,
                style = LessonTheme.question(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (isTablet) 56.dp else 32.dp, bottom = if (isTablet) 32.dp else 24.dp)
            )
            segment.choices.forEachIndexed { index, choice ->
                val state = stateFor(index)
                AnswerCard(
                    state = state,
                    enabled = phase == AnswerPhase.Answering,
                    modifier = Modifier.fillMaxWidth().heightIn(min = (64 * LessonTheme.scale()).dp),
                    onClick = { selectedIndex = index }
                ) { label ->
                    Text(
                        text = choice.choice,
                        style = LessonTheme.answer().copy(color = label),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
                Spacer(Modifier.height(11.dp))
            }
        }
        LessonFeedbackSheet(
            visible = phase != AnswerPhase.Answering,
            correct = phase == AnswerPhase.Correct
        ) {
            if (phase == AnswerPhase.Correct) {
                onNextClicked()
            } else {
                // Try again: back to answering with nothing selected
                selectedIndex = null
                phase = AnswerPhase.Answering
            }
        }
    }
}
