package com.alifba.alifba.presenation.lessonScreens.lessonSegment.flashCard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alifba.alifba.data.models.LessonSegment
import com.alifba.alifba.ui_components.theme.darkPink
import com.alifba.alifba.ui_components.theme.darkPurple
import com.alifba.alifba.ui_components.theme.lightPink
import com.alifba.alifba.ui_components.theme.lightPurple
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton


@Composable
fun FlashCardLessonSegment(segment: LessonSegment.FlashCardExercise, onNextClicked: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom // Ensure content is at the bottom
    ) {
        // FlashCard with limited height
        FlashCard(
            title = segment.title,
            description = segment.description,
            imageResId = segment.image.toInt(),
            modifier = Modifier
                .padding(bottom = 16.dp) // Bottom padding for spacing
                .weight(1f) // Allow FlashCard to take available space but not overflow
        )

        // Button Section
        CommonButton(
            onClick = { onNextClicked() },
            buttonText = "Next",
            modifier = Modifier.align(Alignment.CenterHorizontally), // Center button
            mainColor = lightPink,
            shadowColor = darkPink,
            textColor = white
        )
    }
}
