package com.alifba.alifba.presenation.lessonScreens.lessonSegment.fillInTheBlanks

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import coil.compose.rememberImagePainter
import com.alifba.alifba.R
import com.alifba.alifba.data.models.LessonSegment
import com.alifba.alifba.data.models.OptionsForFillInTheBlanks
import com.alifba.alifba.presenation.main.logScreenView
import com.alifba.alifba.ui_components.dialogs.LottieAnimationDialog
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.navyBlue

import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.alifba.alifba.ui_components.widgets.buttons.OptionButton
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FillInTheBlanksExerciseScreen(
    segment: LessonSegment.FillInTheBlanks,
    onNextClicked: () -> Unit,
    showNextButton: Boolean,
) {
    LaunchedEffect(Unit) {
        logScreenView("lesson_screen")
    }
    LaunchedEffect(Unit) {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "FillInTheBlanksExerciseScreen")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "FillInTheBlanksExerciseScreen")
        }
    }
    val exerciseKey = segment.exercise.hashCode()

    // Correctly initialize blanksState with explicit type
    val blanksState: MutableState<MutableMap<Int, OptionsForFillInTheBlanks?>> = remember(exerciseKey) {
        mutableStateOf(
            segment.exercise.sentenceParts.mapIndexedNotNull { index, part ->
                if (part.trim().all { it == '_' }) index to null else null
            }.toMap().toMutableMap()
        )
    }
    val firstEmptyBlankIndex = segment.exercise.sentenceParts.indices.firstOrNull { idx ->
        segment.exercise.sentenceParts[idx].trim().all { it == '_' } && blanksState.value[idx] == null
    }



    // Map options with indices to ensure positions are accurate
    val allOptions = segment.exercise.options.mapIndexed { index, option ->
        option.copy(position = index)
    }

    val availableOptions = allOptions.filterNot { optionForFill ->
        blanksState.value.values.any { selectedOption ->
            selectedOption?.option == optionForFill.option
        }
    }

    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    val animationFinished = remember { mutableStateOf(false) }
    val alifbaFont = FontFamily(Font(R.font.more_sugar_regular, FontWeight.Normal))

    val checkAnswer = {
        val normalizedUserAnswer = blanksState.value.entries
            .sortedBy { it.key }
            .map { it.value?.position ?: -1 }

        val normalizedCorrectAnswer = segment.exercise.correctAnswers.map { it ?: -1 }

        // Log both lists for debugging
        Log.d("FillInTheBlanks", "Normalized User answer: $normalizedUserAnswer")
        Log.d("FillInTheBlanks", "Normalized Correct answer: $normalizedCorrectAnswer")

        if (normalizedUserAnswer == normalizedCorrectAnswer) {
            showDialog.value = true
        } else {
            Toast.makeText(context, "Wrong answer", Toast.LENGTH_SHORT).show()

            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(100)
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxHeight()
    ) {
        // Display Image
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
                .fillMaxHeight(0.5f)
                .padding(8.dp)
                .align(Alignment.CenterHorizontally)
                .clip(shape = RoundedCornerShape(64.dp)),
            contentScale = ContentScale.FillWidth
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Interactive Sentence to Fill in the Blanks
        InteractiveSentence(
            sentenceParts = segment.exercise.sentenceParts,
            blanksState = blanksState.value,
            fontFamily = alifbaFont,
            onBlankClicked = { index ->
                blanksState.value = blanksState.value.toMutableMap().apply { this[index] = null }
                Log.d("FillInTheBlanks", "Blank cleared at index: $index")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Option Buttons to Select an Option
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            availableOptions.forEach { option ->
                OptionButton(
                    buttonText = option.option,
                    onClick = {
                        val firstEmptyBlankIndex = segment.exercise.sentenceParts.indices.firstOrNull { idx ->
                            segment.exercise.sentenceParts[idx].trim().all { it == '_' } && blanksState.value[idx] == null
                        }
                        if (firstEmptyBlankIndex != null) {
                            blanksState.value = blanksState.value.toMutableMap().apply {
                                this[firstEmptyBlankIndex] = option
                            }
                            Log.d(
                                "FillInTheBlanks",
                                "Filled blank at index: $firstEmptyBlankIndex with option: ${option.option}, position: ${option.position}"
                            )
                        } else {
                            Toast.makeText(context, "All blanks are filled.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

            }
        }

        // Animation Dialog after Correct Answer
        if (showDialog.value) {
            LottieAnimationDialog(showDialog = showDialog, lottieFileRes = R.raw.tick)
            LaunchedEffect(showDialog.value) {
                delay(2000)
                showDialog.value = false
                animationFinished.value = true
            }
        }

        Spacer(Modifier.weight(1f))

        // Check Answer Button
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CommonButton(
                onClick = checkAnswer,
                buttonText = "Check",
                mainColor = lightNavyBlue,
                shadowColor = navyBlue,
                textColor = white
            )
        }
    }

    // Trigger onNextClicked when animation is finished
    LaunchedEffect(animationFinished.value) {
        if (animationFinished.value) {
            animationFinished.value = false // Reset animation state for next segment
            onNextClicked()
        }
    }
}
