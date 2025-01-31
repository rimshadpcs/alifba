package com.alifba.alifba.presenation.lessonScreens.lessonSegment.CommonLesson

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.alifba.alifba.R
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.alifba.alifba.ui_components.widgets.texts.CommonExplanationText
import com.alifba.alifba.data.models.LessonSegment
import com.alifba.alifba.ui_components.theme.darkPurple
import com.alifba.alifba.ui_components.theme.lightPurple
import com.alifba.alifba.ui_components.theme.white

@Composable
fun CommonLessonSegment(
    segment: LessonSegment.CommonLesson,
    onNextClicked: () -> Unit,
    showNextButton: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Image
        Image(
            painter = rememberImagePainter(
                data = segment.image,
                builder = {
                    crossfade(true)
                    placeholder(R.drawable.loading_bar)
                    error(R.drawable.error)
                }
            ),
            contentDescription = "Image",
            modifier = Modifier
                .fillMaxWidth()
                // Instead of forcing half the screen:
                // .fillMaxHeight(0.5f)
                .clip(shape = RoundedCornerShape(64.dp))
                .padding(8.dp),
            contentScale = ContentScale.FillWidth
        )

        // Explanation text
        CommonExplanationText(
            text = segment.description,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // This spacer absorbs any remaining space. If there is not enough
        // space, everything remains scrollable.
        Spacer(modifier = Modifier.weight(1f))
        if (showNextButton) {
            // Bottom button
            CommonButton(
                onClick = onNextClicked,
                buttonText = "Next",
                mainColor = lightPurple,
                shadowColor = darkPurple,
                textColor = white,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
        }
    }
}

