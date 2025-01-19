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
import androidx.compose.ui.geometry.Rect
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
import com.alifba.alifba.data.models.LessonSegment
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
fun LetterTracing(segment: LessonSegment.LetterTracing, onNextClicked: () -> Unit) {
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

//fun createLargerAlifPath(): Path {
//    return Path().apply {
//        moveTo(80f, 100f) // Start at the top of the Alif
//        cubicTo(120f, 500f, 80f, 500f, 100f, 700f) // Draw the main body of the Alif downwards
//    }
//}
//fun createBaaPath(): Path {
//    return Path().apply {
//        moveTo(100f, 130f) // Start at the beginning of the curve (left side of "Baa")
//        cubicTo(150f, 300f, 100f, 400f, 450f, 300f) // First curve segment
//        cubicTo(500f, 280f, 600f, 200f, 500f, 100f) // Second curve segment
//
//
//    }
//}

fun createAlifPath(): Path {
    return Path().apply {
        // Start at the top with a slight curve for calligraphic style
        moveTo(100f, 80f)

        // Main vertical stroke with subtle curves for calligraphic feel
        cubicTo(
            110f, 200f,  // First control point
            90f, 400f,   // Second control point
            100f, 600f   // End point
        )

        // Add the characteristic base flare
        cubicTo(
            105f, 650f,  // First control point
            120f, 680f,  // Second control point
            140f, 690f   // End point
        )
    }
}
fun createBaaPath(): Path {
    return Path().apply {
        // Keep your existing curve which looks good
        moveTo(100f, 130f)
        cubicTo(150f, 300f, 100f, 400f, 450f, 300f) // First curve segment
        cubicTo(500f, 280f, 600f, 200f, 500f, 100f) // Second curve segment

        // Adjust dot position to be under the deepest part of the curve
        // The deepest point is around x=275 (between 100 and 450) and y=400
        addOval(
            Rect(
                center = Offset(420f, 450f), // Positioned below the curve's deepest point
                radius = 8f,

            )
        )
    }
}
fun createTaaPath(): Path {
    return Path().apply {
        // Start to the right and curve leftwards (approx. shape of ت)
        moveTo(50f, 180f)
        cubicTo(100f, 300f, 100f, 400f, 450f, 300f) // First curve segment
        cubicTo(500f, 280f, 600f, 200f, 500f, 100f) // Second curve segment


        addOval(
            Rect(
                center = Offset(330f, 120f),
                radius = 3f
            )
        )
        addOval(
            Rect(
                center = Offset(250f, 120f),
                radius = 3f
            )
        )
    }
}
fun createThaaPath(): Path {
    return Path().apply {
        // Start to the right and curve leftwards (approx. shape of ت)
        moveTo(50f, 180f)
        cubicTo(100f, 300f, 100f, 400f, 450f, 300f) // First curve segment
        cubicTo(500f, 280f, 600f, 200f, 500f, 100f) // Second curve segment

        addOval(
            Rect(
                center = Offset(290f, 60f),
                radius = 3f
            )
        )

        addOval(
            Rect(
                center = Offset(330f, 120f),
                radius = 3f
            )
        )
        addOval(
            Rect(
                center = Offset(250f, 120f),
                radius = 3f
            )
        )

    }
}


fun createJeemPath(): Path {
    return Path().apply {
        // Start near top-right
        moveTo(450f, 150f)

        // First curve: sweeping downward and left
        cubicTo(
            400f, 220f,  // Control point 1
            300f, 300f,  // Control point 2
            220f, 320f   // End point
        )

        // Second curve: hooking back up to form the top
        cubicTo(
            170f, 280f,
            160f, 180f,
            220f, 150f
        )

        // Add the single dot near the bottom portion (Jeem has one dot typically)
        // Adjust position & size as needed
        addOval(
            Rect(
                center = Offset(250f, 340f),
                radius = 6f
            )
        )
    }
}


        // Add the dot below
//        addOval(
//            Rect(
//                center = Offset(250f, 290f), // Positioned below the main line
//                radius = 8f
//            )
//        )
//    }
//}

//fun createBaaPath(): Path {
//    return Path().apply {
//        // Start from the right side higher up
//        moveTo(400f, 200f)
//
//        // First curve sweeping down and left
//        cubicTo(
//            350f, 250f,  // First control point - controls the initial descent
//            250f, 280f,  // Second control point - controls the middle curve
//            150f, 270f   // End point - where the curve starts to flatten
//        )
//
//        // Second curve continuing left and slightly up for the tail
//        cubicTo(
//            100f, 265f,  // First control point - controls the tail's curve
//            80f, 250f,   // Second control point - controls the tail's end
//            100f, 240f   // End point - slight upward flick at the end
//        )
//
//        // Add the dot below
//        addOval(
//            Rect(
//                center = Offset(250f, 320f), // Positioned below the main curve
//                radius = 8f
//            )
//        )
//    }
//}



//fun createTaaPath(): Path {
//    return Path().apply {
//        // Use the same base shape as Baa
//        moveTo(400f, 200f)
//
//        // First curve sweeping down and left
//        cubicTo(
//            350f, 250f,
//            250f, 280f,
//            150f, 270f
//        )
//
//        // Second curve continuing left and slightly up for the tail
//        cubicTo(
//            100f, 265f,
//            80f, 250f,
//            100f, 240f
//        )
//
//        // Add two dots above the main curve
//        addOval(
//            Rect(
//                center = Offset(240f, 200f), // First dot above the curve
//                radius = 8f
//            )
//        )
//        addOval(
//            Rect(
//                center = Offset(270f, 200f), // Second dot above the curve
//                radius = 8f
//            )
//        )
//    }
//}
//
//// Helper function to create a circular dot
//fun Path.addDot(center: Offset, radius: Float) {
//    addOval(
//        Rect(
//            center = center,
//            radius = radius
//        )
//    )
//}

// Helper function to scale paths based on canvas size
fun Path.scale(scaleX: Float, scaleY: Float): Path {
    val scalePath = Path()
    val matrix = android.graphics.Matrix()
    matrix.setScale(scaleX, scaleY)
    this.asAndroidPath().transform(matrix)
    scalePath.asAndroidPath().set(this.asAndroidPath())
    return scalePath
}


@Preview(showSystemUi = false) // Disables system UI in the preview
@Composable
fun PreviewLetterTracing() {
    // Mocking the LessonSegment.LetterTracing data
    val mockLessonSegment = LessonSegment.LetterTracing(
        speech = R.raw.letterbaa // Replace with a valid audio resource ID for the preview
    )

    // Fullscreen-like simulation using Box
    Box(
        modifier = Modifier
            .fillMaxSize() // Ensures the layout occupies the full available space
            .background(Color.White) // Background to simulate the app's theme
    ) {
        // Call the LetterTracing composable with the mocked data
        LetterTracing(segment = mockLessonSegment, onNextClicked = {})
    }
}