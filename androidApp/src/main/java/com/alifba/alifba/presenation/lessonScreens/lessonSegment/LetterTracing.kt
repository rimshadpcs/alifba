package com.alifba.alifba.presenation.lessonScreens.lessonSegment

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alifba.alifba.R
import com.alifba.alifba.models.LessonSegment
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.LetterTracing.LetterTracingAnimation
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.LetterTracing.LetterTracingExercise
import com.alifba.alifba.ui_components.dialogs.LottieAnimationDialog
import com.alifba.alifba.ui_components.theme.darkPurple
import com.alifba.alifba.ui_components.theme.lightPurple
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.smarttoolfactory.tutorial1_1basics.chapter6_graphics.PathSegmentInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.atan2


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