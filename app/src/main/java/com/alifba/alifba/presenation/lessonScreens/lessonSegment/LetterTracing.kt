package com.alifba.alifba.presenation.lessonScreens.lessonSegment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alifba.alifba.R
import com.alifba.alifba.data.models.LessonSegment
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.LetterTracing.LetterTracingAnimation
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.LetterTracing.LetterTracingExercise
import com.alifba.alifba.ui_components.dialogs.LottieAnimationDialog
import com.alifba.alifba.ui_components.theme.navyBlue


@Composable
fun LetterTracing(
    segment: LessonSegment.LetterTracing,
    onNextClicked: () -> Unit
) {
    val alifbaFont = FontFamily(Font(R.font.more_sugar_regular, FontWeight.Normal))
    // How many times to trace (fallback to 3 if not provided by backend)
    val timesToTrace = segment.repeatCount ?: 3

    // Which attempt are we on now?
    var attemptIndex by remember { mutableStateOf(1) }

    // Show a “demo” animation only once at start? (If you need it)
    var showIntroAnimation by remember { mutableStateOf(true) }

    // Show final “celebration” animation after the last attempt
    var showFinalAnimation by remember { mutableStateOf(false) }

    // A key that forces the child to re-initialize from scratch each attempt
    var resetTrigger by remember { mutableStateOf(0) }
    // Decide which letter shape we’ll be tracing
    val shape = when (segment.letterId?.lowercase()) {
        "alif" -> createAlifShape()
        "baa"  -> createBaaShape()
        "taa"  -> createTaaShape()
        "thaa" -> createThaaShape()
        "jeem"-> createJeemShape()
        else   -> createBaaShape() // fallback
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            // 1) Optional: If you want an intro animation first
            showIntroAnimation -> {
                LetterTracingAnimation(
                    letterId = segment.letterId,
                    onContinueClicked = {
                        showIntroAnimation = false
                    }
                )
            }

            // 2) If we’ve done all attempts, show final animation
            showFinalAnimation -> {
                LottieAnimationDialog(
                    showDialog = remember { mutableStateOf(true) },
                    lottieFileRes = R.raw.burst
                )
                // After a delay, navigate out
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(1500)
                    onNextClicked() // or go back, or wherever
                }
            }

            // 3) Otherwise, let the user do the letter tracing for the current attempt
            else -> {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1) Show how many attempts remain
                    val attemptsRemaining = timesToTrace - attemptIndex
                    val textToShow = when {
                        attemptsRemaining > 1 -> "$attemptsRemaining more times"
                        attemptsRemaining == 1 -> "1 more time"
                        else -> "Last attempt" // or "0 more times" if you prefer
                    }

                    // Display it near the top
                    Text(
                        text = textToShow,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally),
                        color = navyBlue,
                        fontSize = 22.sp,
                        fontFamily = alifbaFont
                    )

                    // 2) Force re-composition of the exercise each time resetTrigger increments
                    androidx.compose.runtime.key(resetTrigger) {
                        LetterTracingExercise(
                            letterShape = shape,
                            onNextClicked = {
                                if (attemptIndex < timesToTrace) {
                                    attemptIndex++
                                    resetTrigger++
                                } else {
                                    showFinalAnimation = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}


data class LetterShape(
    val mainPath: Path,
    val dots: List<DotData>
)

data class DotData(
    val center: Offset,
    val radius: Float,
    var isTouched: Boolean = false
)


private const val DOT_RADIUS = 24f
fun createAlifShape(): LetterShape {
    val mainPath = Path().apply {
        // Starting point at the top with a slight edge
        moveTo(200f, 80f)

        // Add a tiny edge at the top
        lineTo(205f, 85f)
        lineTo(200f, 90f)

        // Straight line down for most of the character
        lineTo(200f, 500f)

        // Sharp angular hook at the bottom instead of a curve
        lineTo(190f, 530f)
        lineTo(170f, 560f)
    }

    // Alif has no dots
    return LetterShape(mainPath, dots = emptyList())
}
fun createBaaShape(): LetterShape {
    val mainPath = Path().apply {
        // Start from the rightmost point
        moveTo(500f, 100f)
        // Then curve leftwards
        cubicTo(600f, 200f, 500f, 280f, 450f, 300f)
        cubicTo(100f, 400f, 150f, 300f, 100f, 130f)
    }

    val dot = DotData(
        center = Offset(420f, 450f),
        radius = DOT_RADIUS
    )

    return LetterShape(mainPath = mainPath, dots = listOf(dot))
}

fun createTaaShape(): LetterShape {
    val mainPath = Path().apply {
        moveTo(500f, 100f)

        cubicTo(600f, 200f, 500f, 280f, 450f, 300f)

        // Reverse the first cubic
        cubicTo(100f, 400f, 100f, 300f, 50f, 180f)
    }

    val dot1 = DotData(center = Offset(330f, 120f), radius = DOT_RADIUS)
    val dot2 = DotData(center = Offset(250f, 120f), radius = DOT_RADIUS)

    return LetterShape(mainPath = mainPath, dots = listOf(dot1, dot2))
}


fun createThaaShape(): LetterShape {
    val mainPath = Path().apply {
        moveTo(500f, 100f)

        cubicTo(600f, 200f, 500f, 280f, 450f, 300f)

        cubicTo(100f, 400f, 100f, 300f, 50f, 180f)
    }

    val dot1 = DotData(center = Offset(290f, 60f), radius = DOT_RADIUS)
    val dot2 = DotData(center = Offset(330f, 120f), radius = DOT_RADIUS)
    val dot3 = DotData(center = Offset(250f, 120f), radius = DOT_RADIUS)

    return LetterShape(mainPath = mainPath, dots = listOf(dot1, dot2, dot3))
}


fun createJeemShape(): LetterShape {
    val mainPath = Path().apply {
        // Starting point - begin from right side
        moveTo(400f, 350f)

        // Horizontal line moving left
        lineTo(250f, 350f)

        // Create the bowl/cup shape that's characteristic of jeem
        // First curve down and left
        cubicTo(
            200f, 350f,  // First control point
            150f, 370f,  // Second control point
            120f, 420f   // End point - bottom of curve
        )

        // Second curve to create the upward hook at the end
        cubicTo(
            90f, 470f,   // First control point
            120f, 520f,  // Second control point
            170f, 510f   // End point - tip of the hook
        )
    }

    // Jeem has one dot INSIDE the bowl/cup, not below it
    val dot = DotData(
        center = Offset(170f, 420f),
        radius = DOT_RADIUS
    )

    // Return the letter shape with the dot
    return LetterShape(mainPath = mainPath, dots = listOf(dot))
}

fun Path.scale(scaleX: Float, scaleY: Float): Path {
    val scalePath = Path()
    val matrix = android.graphics.Matrix()
    matrix.setScale(scaleX, scaleY)
    this.asAndroidPath().transform(matrix)
    scalePath.asAndroidPath().set(this.asAndroidPath())
    return scalePath
}
