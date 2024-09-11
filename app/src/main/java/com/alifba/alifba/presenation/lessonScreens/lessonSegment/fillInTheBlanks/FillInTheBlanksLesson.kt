package com.alifba.alifba.presenation.lessonScreens.lessonSegment.fillInTheBlanks

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.alifba.alifba.R
import com.alifba.alifba.models.FillInTheBlanksExercise
import com.alifba.alifba.models.LessonSegment
import com.alifba.alifba.models.OptionsForFillInTheBlanks
import com.alifba.alifba.ui_components.dialogs.LottieAnimationDialog
import com.alifba.alifba.ui_components.theme.darkPurple
import com.alifba.alifba.ui_components.theme.lightPurple
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.alifba.alifba.ui_components.widgets.buttons.OptionButton
import com.alifba.alifba.utils.PlayAudio
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FillInTheBlanksExerciseScreen(segment: LessonSegment.FillInTheBlanks, onNextClicked: () -> Unit) {
    val allOptions = segment.exercise.options.mapIndexed { index, option ->
        OptionsForFillInTheBlanks(option.option, position = index)
    }
    val blanksState = remember { mutableStateOf<Map<Int, OptionsForFillInTheBlanks?>>(emptyMap()) }

    val availableOptions = allOptions.filterNot { optionForFill ->
        blanksState.value.values.any { selectedOption -> selectedOption?.option == optionForFill.option }
    }
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    val animationFinished = remember { mutableStateOf(false) }
    PlayAudio(audioResId = segment.exercise.speech)
    val alifbaFont = FontFamily(Font(R.font.more_sugar_regular, FontWeight.Normal))

    val checkAnswer = {
        val userAnswer = blanksState.value.entries.sortedBy { it.key }.map { it.value?.position }
        val correctAnswer = segment.exercise.correctAnswers
        if (userAnswer == correctAnswer){
            showDialog.value =true

//            if(animationFinished.value) {
//                onNextClicked()
//            }
        }
        else{
            Toast.makeText(context,"wrong",Toast.LENGTH_SHORT).show()
        }
    }


    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxHeight()  // Use fillMaxHeight to use all available space
    ) {

        Image(
            painter = painterResource(id = segment.exercise.imageResId),
            contentDescription = "Exercise Image",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .clip(shape = RoundedCornerShape(64.dp)),
        )
        Spacer(modifier = Modifier.height(16.dp))

        InteractiveSentence(
            sentenceParts = segment.exercise.sentenceParts,
            blanksState = blanksState,
            fontFamily = alifbaFont,
            onBlankClicked = { index ->
                blanksState.value = blanksState.value.toMutableMap().also { it[index] = null }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            availableOptions.forEach { option ->
                OptionButton(
                    buttonText = option.option,
                    onClick = {
                        val firstEmptyBlankIndex = segment.exercise.sentenceParts.indices.firstOrNull { idx ->
                            segment.exercise.sentenceParts[idx].trim() == "____" && blanksState.value[idx] == null
                        }
                        if (firstEmptyBlankIndex != null) {
                            blanksState.value = blanksState.value.toMutableMap().apply {
                                this[firstEmptyBlankIndex] = option
                            }
                        }
                    }
                )
            }
        }
        if (showDialog.value) {
            LottieAnimationDialog(showDialog = showDialog, lottieFileRes = R.raw.tick)
            LaunchedEffect(showDialog.value) {
                delay(2000)  // Assuming 2000 milliseconds animation duration
                showDialog.value = false
                animationFinished.value = true  // Set the animation finish state to true

            }
        }
        Spacer(Modifier.weight(1f))  // This pushes the button to the bottom

        Box(
            modifier = Modifier.fillMaxWidth(),  // Ensure the Box takes the full width
            contentAlignment = Alignment.Center
        ) {
            CommonButton(
                onClick = checkAnswer,
                buttonText = "Check",
                mainColor = lightPurple,
                shadowColor = darkPurple,
                textColor = white
            )
        }
    }
    LaunchedEffect(animationFinished.value) {
        if (animationFinished.value) {
            onNextClicked()
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFFFFFF, widthDp = 412, heightDp = 892)
@Composable
fun DefaultPreview() {
    MaterialTheme {
        val options = listOf(
            OptionsForFillInTheBlanks("angry"),
            OptionsForFillInTheBlanks("thank you"),
            OptionsForFillInTheBlanks("kind"),
            OptionsForFillInTheBlanks("sad"),
            OptionsForFillInTheBlanks("mad"),
            OptionsForFillInTheBlanks("hate")
        )

        val exercise = LessonSegment.FillInTheBlanks(exercise = FillInTheBlanksExercise(
            speech = R.raw.fillin,
            imageResId = R.drawable.sun, // Make sure this resource exists in your drawable folder
            sentenceParts = listOf("Being ", "____", " to others and saying ", "____", " for their help"," are important values in Islam."),            options = options,
            correctAnswers = listOf(2, 1) // Assuming this relates correctly to your options,
        )
        )

        FillInTheBlanksExerciseScreen(  segment = exercise,onNextClicked = { /* Implement action */ })
    }
}


