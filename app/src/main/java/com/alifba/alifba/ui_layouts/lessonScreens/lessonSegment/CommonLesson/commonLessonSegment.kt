package com.alifba.alifba.ui_layouts.lessonScreens.lessonSegment.CommonLesson

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alifba.alifba.R
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.alifba.alifba.ui_components.widgets.texts.CommonExplanationText
import com.alifba.alifba.models.LessonSegment

@Composable
fun CommonLessonSegment(segment: LessonSegment.CommonLesson, onNextClicked: () -> Unit) {
    //PlayAudio(audioResId = segment.speech)

    Column(modifier =
    Modifier.padding(16.dp)
    ) {

        Image(
            painter = painterResource(id = segment.image),
            contentDescription = "Image",
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .padding(8.dp)
                .align(Alignment.CenterHorizontally)
                .clip(shape = RoundedCornerShape(64.dp)),
            contentScale = ContentScale.FillWidth

        )

        CommonExplanationText(
            text = segment.description,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )
    }
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            CommonButton(
                onClick = onNextClicked,
                buttonText = "Next"
            )
        }
    }

@Preview(showBackground = true, backgroundColor = 0xFFFFFF, widthDp = 412, heightDp = 892)
@Composable
fun ImageDisplaySegmentPreview() {
    val sampleSegment = LessonSegment.CommonLesson(
        image = R.drawable.rivers, // Replace with actual drawable resource IDs
        description = "Hello, little friends! Today, we're going on a fun adventure to see the beautiful world Allah has made. Let's find out how He created everything and how much He loves us and everything He made!",
        speech = R.raw.intro
    )
    CommonLessonSegment(segment = sampleSegment, onNextClicked = { /* Implement action */ })
}