package com.alifba.alifba.presenation.lessonScreens.lessonSegment.pictureMcq

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

import com.alifba.alifba.data.models.PictureMcqItem

@Composable
fun PictureMcqSegment(
    segment: LessonSegment.PictureMcqLesson,
    onNextClicked: () -> Unit,
    showNextButton: Boolean,
    isLastSegment: Boolean = false
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var phase by remember { mutableStateOf(AnswerPhase.Answering) }
    val context = LocalContext.current
    val isTablet = LessonTheme.isTablet()

    LaunchedEffect(Unit) {
        logScreenView("lesson_screen")
        SoundEffectManager.initialize(context)
    }

    fun stateFor(index: Int) = when {
        selectedIndex != index -> AnswerState.Normal
        phase == AnswerPhase.Answering -> AnswerState.Selected
        phase == AnswerPhase.Correct -> AnswerState.Correct
        else -> AnswerState.Wrong
    }

    fun check() {
        val index = selectedIndex ?: return
        val item = segment.pictureChoices.getOrNull(index) ?: return
        if (item.answer) {
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
                modifier = Modifier.fillMaxWidth().padding(top = if (isTablet) 32.dp else 16.dp)
            )
            Spacer(Modifier.height(24.dp))

            val gap = if (isTablet) 24.dp else 16.dp
            // Square cards sized from the available width (two per row, 24 side margins), capped.
            // A lone last card is centred. Any number of choices is shown.
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val cardSize = minOf((maxWidth - gap) / 2, if (isTablet) 260.dp else 190.dp)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(gap - LessonTheme.answerEdge),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    segment.pictureChoices.withIndex().toList().chunked(2).forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                            rowItems.forEach { (index, item) ->
                                val state = stateFor(index)
                                AnswerCard(
                                    state = state,
                                    enabled = phase == AnswerPhase.Answering,
                                    modifier = Modifier.size(cardSize),
                                    onClick = { selectedIndex = index }
                                ) { label ->
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Image(
                                            painter = if (item.image.startsWith("http")) {
                                                rememberImagePainter(item.image)
                                            } else {
                                                androidx.compose.ui.res.painterResource(id = item.image.toIntOrNull() ?: R.drawable.error)
                                            },
                                            contentDescription = item.choice,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                                .padding(top = 14.dp, start = 14.dp, end = 14.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                        Text(
                                            text = item.choice,
                                            style = LessonTheme.answer().copy(
                                                color = label,
                                                fontSize = (20 * LessonTheme.scale()).sp
                                            ),
                                            maxLines = 2,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        LessonFeedbackSheet(
            visible = phase != AnswerPhase.Answering,
            correct = phase == AnswerPhase.Correct
        ) {
            if (phase == AnswerPhase.Correct) {
                onNextClicked()
            } else {
                selectedIndex = null
                phase = AnswerPhase.Answering
            }
        }
    }
}
