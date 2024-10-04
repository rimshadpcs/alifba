package com.alifba.alifba.presenation.lessonScreens.lessonSegment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import com.alifba.alifba.R
import com.alifba.alifba.models.LessonSegment
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.LetterTracing.LetterTracingAnimation
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.LetterTracing.LetterTracingExercise


@Composable
fun LetterTracing(segment: LessonSegment.LetterTracing,onNextClicked: () -> Unit) {
    // State to track whether to show the animation or the next composable
    var showAnimation by remember { mutableStateOf(true) }
    //PlayAudio(audioResId = segment.speech)

    Box(modifier = Modifier.fillMaxSize()) {
        if (showAnimation) {
            LetterTracingAnimation(
                onContinueClicked = { showAnimation = false }
            )
        } else {
            LetterTracingExercise( onNextClicked)
        }
    }
}

fun createLargerAlifPath(): Path {
    return Path().apply {
        moveTo(80f, 100f) // Start at the top of the Alif
        cubicTo(120f, 500f, 80f, 500f, 100f, 700f) // Draw the main body of the Alif downwards
    }
}
fun createBaaPath(): Path {
    return Path().apply {
        moveTo(100f, 130f) // Start at the beginning of the curve (left side of "Baa")
        cubicTo(150f, 300f, 100f, 400f, 450f, 300f) // First curve segment
        cubicTo(500f, 280f, 600f, 200f, 500f, 100f) // Second curve segment


    }
}


@Preview
@Composable
fun PreviewLetterTracing() {
    // Mocking the LessonSegment.LetterTracing data
    val mockLessonSegment = LessonSegment.LetterTracing(
        speech = R.raw.letterbaa // Replace with a valid audio resource ID for the preview
    )

    // Call the LetterTracing composable with the mocked data
    LetterTracing(segment = mockLessonSegment, onNextClicked = {})
}