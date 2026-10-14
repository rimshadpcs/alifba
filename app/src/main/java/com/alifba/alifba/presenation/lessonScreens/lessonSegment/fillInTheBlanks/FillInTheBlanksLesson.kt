package com.alifba.alifba.presenation.lessonScreens.lessonSegment.fillInTheBlanks

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
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.alifba.alifba.R
import com.alifba.alifba.data.models.LessonSegment
import com.alifba.alifba.data.models.OptionsForFillInTheBlanks
import com.alifba.alifba.presenation.main.logScreenView
import com.alifba.alifba.ui_components.widgets.*
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FillInTheBlanksExerciseScreen(
    segment: LessonSegment.FillInTheBlanks,
    onNextClicked: () -> Unit,
    showNextButton: Boolean,
) {
    LaunchedEffect(Unit) { logScreenView("lesson_screen") }
    val exerciseKey = segment.exercise.hashCode()
    val context = LocalContext.current
    val isTablet = LessonTheme.isTablet()
    LaunchedEffect(Unit) { SoundEffectManager.initialize(context) }

    val parts = segment.exercise.sentenceParts
    val blankIndices = remember(exerciseKey) { parts.indices.filter { parts[it].trim().all { c -> c == '_' } } }
    var blanksState by remember(exerciseKey) {
        mutableStateOf<Map<Int, OptionsForFillInTheBlanks?>>(blankIndices.associateWith { null })
    }
    var phase by remember(exerciseKey) { mutableStateOf(AnswerPhase.Answering) }

    // Map options with indices so positions are accurate, then hide the ones already placed
    val allOptions = segment.exercise.options.mapIndexed { index, option -> option.copy(position = index) }
    val availableOptions = allOptions.filterNot { opt -> blanksState.values.any { it?.option == opt.option } }
    val allFilled = blankIndices.isNotEmpty() && blankIndices.all { blanksState[it] != null }
    val filledColor = when (phase) {
        AnswerPhase.Answering -> LessonTheme.primary
        AnswerPhase.Correct -> LessonTheme.correct
        AnswerPhase.Wrong -> LessonTheme.wrong
    }

    fun check() {
        val userAnswer = blanksState.toSortedMap().values.map { it?.position ?: -1 }
        val correctAnswer = segment.exercise.correctAnswers.map { it ?: -1 }
        if (userAnswer == correctAnswer) {
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
                    kind = if (allFilled && phase == AnswerPhase.Answering) LessonButtonKind.Primary else LessonButtonKind.Disabled,
                    onClick = ::check
                )
            }
        ) {
            // Exercise image: 24 side margin, radius 28
            Image(
                painter = rememberImagePainter(
                    data = segment.exercise.imageResId,
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

            // Interactive sentence: ink, 28/600, a pale-blue line for each empty blank
            InteractiveSentence(
                sentenceParts = parts,
                blanksState = blanksState,
                isTablet = isTablet,
                filledColor = filledColor,
                onBlankClicked = { index ->
                    if (phase == AnswerPhase.Answering) blanksState = blanksState + (index to null)
                }
            )
            Spacer(Modifier.height(24.dp))

            // Options: the same answer card as every other question type, as chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 12.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(if (isTablet) 11.dp else 7.dp)
            ) {
                availableOptions.forEach { option ->
                    AnswerCard(
                        state = AnswerState.Normal,
                        enabled = phase == AnswerPhase.Answering,
                        modifier = Modifier.heightIn(min = (52 * LessonTheme.scale()).dp),
                        onClick = {
                            // The tapped word moves into the first empty blank
                            val firstEmpty = blankIndices.firstOrNull { blanksState[it] == null }
                            if (firstEmpty != null) blanksState = blanksState + (firstEmpty to option)
                        }
                    ) { label ->
                        Text(
                            text = option.option,
                            style = LessonTheme.answer().copy(color = label),
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 22.dp)
                        )
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
                // Try again: chosen words go back to the options
                blanksState = blankIndices.associateWith { null }
                phase = AnswerPhase.Answering
            }
        }
    }
}
