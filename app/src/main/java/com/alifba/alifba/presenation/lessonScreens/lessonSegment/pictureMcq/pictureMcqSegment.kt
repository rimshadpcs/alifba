package com.alifba.alifba.presenation.lessonScreens.lessonSegment.pictureMcq

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alifba.alifba.R
import com.alifba.alifba.models.LessonSegment
import com.alifba.alifba.models.PictureMcqItem
import com.alifba.alifba.ui_components.dialogs.LottieAnimationDialog
import com.alifba.alifba.ui_components.theme.darkPurple
import com.alifba.alifba.ui_components.theme.lightPurple
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.alifba.alifba.ui_components.widgets.buttons.PictureButton
import com.alifba.alifba.utils.PlayAudio
import kotlinx.coroutines.delay
import com.alifba.alifba.ui_components.widgets.texts.CommonExplanationText as CommonExplanationText

@Composable
fun PictureMcqSegment(segment: LessonSegment.PictureMcqLesson, onNextClicked: () -> Unit) {
    val showNextButton = remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }
    val animationFinished = remember { mutableStateOf(false) }

    PlayAudio(audioResId = segment.speech)
    val alifbaFont = FontFamily(Font(R.font.more_sugar_regular, FontWeight.Normal))

    Column(modifier = Modifier.padding(16.dp)) {
        Image(
            painter =painterResource(segment.image),

            contentDescription = "",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .align(Alignment.CenterHorizontally)
                .clip(shape = RoundedCornerShape(64.dp)))

        Spacer(modifier = Modifier.height(8.dp))

        CommonExplanationText(
            text = segment.question,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )

        LazyVerticalGrid(columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp), // Padding around the grid
            horizontalArrangement = Arrangement.spacedBy(8.dp), // Spacing between columns
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ){
            items(segment.choices) { item: PictureMcqItem ->
                PictureButton(
                    onClick = {
                        if (item.answer == segment.correctAnswer){
                            showNextButton.value =true
                            showDialog.value = true
                        }


                    }, buttonImage = item.image, buttonText = item.answer
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

@Preview(showBackground = true, backgroundColor = 0xFFFFFF, widthDp = 320, heightDp = 640)
@Composable
fun MCQSegmentPreview() {
    val sampleSegment = LessonSegment.PictureMcqLesson(
        question = "Which of this sunnah should we do after eating food ",
        choices = listOf(
            PictureMcqItem(R.drawable.game,"Play video game"),
            PictureMcqItem(R.drawable.washhands,"wash your hands"),
            PictureMcqItem(R.drawable.watchtv,"Watch Tv"),
            PictureMcqItem(R.drawable.leavefood,"leave food on plate")

        ),
        image = R.drawable.food,
        correctAnswer = "Prayer mat",
        speech = R.raw.sunnah
    )
    PictureMcqSegment(segment = sampleSegment, onNextClicked = { /* Implement action */ })
}
