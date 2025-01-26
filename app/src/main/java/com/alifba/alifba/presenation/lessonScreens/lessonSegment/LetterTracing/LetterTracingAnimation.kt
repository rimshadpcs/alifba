package com.alifba.alifba.presenation.lessonScreens.lessonSegment.LetterTracing

import android.graphics.RectF
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alifba.alifba.R
import com.alifba.alifba.data.models.LessonSegment
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.LetterTracing
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.createAlifPath
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.createBaaPath
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.createBaaShape
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.createTaaPath
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.createThaaPath
import com.alifba.alifba.ui_components.theme.darkPurple
import com.alifba.alifba.ui_components.theme.lightPurple
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.atan2

@Composable
fun LetterTracingAnimation(onContinueClicked: () -> Unit) {
    val letterShape = remember { createBaaShape() }
    val arrowPainter = painterResource(id = R.drawable.down_arrow)
    val handPainter = painterResource(id = R.drawable.hand)

    val path = remember { Path() }
    val trackPath = remember { Path() }
    val pathMeasure = remember { PathMeasure() }
    val coroutineScope = rememberCoroutineScope()
    val animatable = remember { Animatable(0f) }
    val handAlpha = remember { Animatable(0f) } // Controls fade effect
    val translation = remember { mutableStateOf(Offset.Zero) }
    var animationPhase by remember { mutableStateOf("path") }
    var currentDotIndex by remember { mutableIntStateOf(0) }
    var showContinueButton by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            // Animate along the path
            if (animationPhase == "path") {
                animatable.snapTo(0f)
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(3000, easing = LinearEasing)
                )
                animationPhase = "dots"
            }

            // Animate hand over dots
            if (animationPhase == "dots") {
                for (index in letterShape.dots.indices) {
                    currentDotIndex = index
                    // Fade in, pause, fade out, repeat
                    repeat(2) {
                        handAlpha.animateTo(1f, animationSpec = tween(500))
                        delay(500) // Pause briefly while fully visible
                        handAlpha.animateTo(0f, animationSpec = tween(500))
                        delay(200) // Pause briefly before appearing again
                    }
                }
                animationPhase = "done"
                showContinueButton = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main canvas
        Canvas(modifier = canvasModifier.padding(16.dp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            if (path.isEmpty) {
                val letterMainPath = letterShape.mainPath
                val pathBounds = RectF()
                letterMainPath.asAndroidPath().computeBounds(pathBounds, true)

                val dx = (canvasWidth - pathBounds.width()) / 2 - pathBounds.left
                val dy = (canvasHeight - pathBounds.height()) / 2 - pathBounds.top
                translation.value = Offset(dx, dy)

                path.addPath(letterMainPath, translation.value)
                pathMeasure.setPath(path, forceClosed = false)
            }

            val pathLength = pathMeasure.length
            val progress = animatable.value.coerceIn(0f, 1f)
            val distance = pathLength * progress

            // Draw the full path
            drawPath(
                path = path,
                color = Color.LightGray,
                style = Stroke(
                    width = 20.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = PathEffect.cornerPathEffect(30f)
                )
            )

            // Draw the animated portion of the path
            if (animationPhase == "path") {
                trackPath.reset()
                pathMeasure.getSegment(0f, distance, trackPath, true)
                drawPath(
                    path = trackPath,
                    color = Color.Black,
                    style = Stroke(
                        width = 20.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                        pathEffect = PathEffect.cornerPathEffect(30f)
                    )
                )

                // Draw arrow
                val position = pathMeasure.getPosition(distance)
                val tangent = pathMeasure.getTangent(distance)
                val angleDegrees = (360 + atan2(tangent.y, tangent.x) * 180 / Math.PI) % 360
                val iconSize = 40.dp.toPx()

                withTransform(
                    transformBlock = {
                        rotate(degrees = angleDegrees.toFloat() - 90f, pivot = position)
                        translate(
                            left = position.x - iconSize / 2,
                            top = position.y - iconSize / 2
                        )
                    }
                ) {
                    with(arrowPainter) {
                        draw(size = Size(iconSize, iconSize))
                    }
                }
            }

            // Draw the dots
            letterShape.dots.forEachIndexed { index, dot ->
                val adjCenter = dot.center + translation.value
                drawCircle(
                    color = if (index <= currentDotIndex && animationPhase == "dots") Color.Green else Color.Gray,
                    radius = dot.radius * 1.2f,
                    center = adjCenter,
                    alpha = 0.8f
                )
            }

            // Draw the hand icon on the current dot during the "dots" phase
            if (animationPhase == "dots" && currentDotIndex < letterShape.dots.size) {
                val currentDot = letterShape.dots[currentDotIndex]
                val adjCenter = currentDot.center + translation.value
                val handIconSize = 45.dp.toPx()

                // Offset the hand slightly below the dot
                val handOffset = Offset(0f, 20.dp.toPx())

                withTransform(
                    transformBlock = {
                        translate(
                            left = adjCenter.x - handIconSize / 2 + handOffset.x,
                            top = adjCenter.y - handIconSize / 2 + handOffset.y
                        )
                    }
                ) {
                    with(handPainter) {
                        draw(size = Size(handIconSize, handIconSize), alpha = handAlpha.value)
                    }
                }
            }
        }

        // Continue button
        if (showContinueButton) {
            CommonButton(
                onClick = onContinueClicked,
                buttonText = "Continue",
                shadowColor = darkPurple,
                mainColor = lightPurple,
                textColor = white,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}


// A shared modifier for the canvas
private val canvasModifier = Modifier
    .shadow(1.dp)
    .background(Color.White)
    .fillMaxWidth()
    .fillMaxHeight()
    .aspectRatio(1f)


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