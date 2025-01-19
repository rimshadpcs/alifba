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
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.createTaaPath
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.createThaaPath
import com.alifba.alifba.ui_components.theme.darkPurple
import com.alifba.alifba.ui_components.theme.lightPurple
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import kotlinx.coroutines.launch
import kotlin.math.atan2

@Composable
fun LetterTracingAnimation(onContinueClicked: () -> Unit) {

    // Use a custom drawable for the arrow and the hand
    val arrowPainter = painterResource(id = R.drawable.down_arrow)
    val handPainter = painterResource(id = R.drawable.hand) // Replace with your hand image resource

    val path = remember { Path() }
    val trackPath = remember { Path() }
    val pathMeasure = remember { PathMeasure() }

    val coroutineScope = rememberCoroutineScope()
    val animatable = remember { Animatable(0f) }
    var showContinueButton by remember { mutableStateOf(false) }
    var animationCount by remember { mutableStateOf(0) }


    LaunchedEffect(Unit) {
        // Start the animation twice on launch
        coroutineScope.launch {
            while (true) {
                trackPath.reset()
                animatable.snapTo(0f)
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(3000, easing = LinearEasing)
                )
                animationCount++

                if (animationCount >= 2) {
                    showContinueButton = true
                }
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = canvasModifier.padding(16.dp)) {
            val density = this.density
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Set up the custom path (e.g., Alif path)
            if (path.isEmpty) {
                val alifPath = createBaaPath()  // Replace with your path function
                val pathBounds = RectF()
                alifPath.asAndroidPath().computeBounds(pathBounds, true)

                // Center the path on the canvas
                val dx = (canvasWidth - pathBounds.width()) / 2 - pathBounds.left
                val dy = (canvasHeight - pathBounds.height()) / 2 - pathBounds.top

                path.addPath(alifPath, Offset(dx, dy))
                pathMeasure.setPath(path = path, forceClosed = false)
            }

            val pathLength = pathMeasure.length
            val progress = animatable.value.coerceIn(0f, 1f)
            val distance = pathLength * progress

            val position = pathMeasure.getPosition(distance)
            val tangent = pathMeasure.getTangent(distance)
            val tan = (360 + atan2(tangent.y, tangent.x) * 180 / Math.PI) % 360

            // Draw the path
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

            // Draw the tracked (animated) path
            pathMeasure.getSegment(0f, distance, trackPath)
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

            val iconSize = 40.dp.toPx()
            val handIconSize = 45.dp.toPx() // Adjust the hand icon size

            withTransform(
                transformBlock = {
                    rotate(degrees = tan.toFloat() - 90f, pivot = position)
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

            // Draw the hand icon at the current position
            withTransform(
                transformBlock = {
                    rotate(degrees = tan.toFloat() - 90f, pivot = position)
                    translate(
                        left = position.x - handIconSize / 2 + 25 * density,
                        top = position.y - handIconSize / 2  + 5 * density
                    )
                }
            ) {
                with(handPainter) {
                    draw(size = Size(handIconSize, handIconSize))
                }
            }


        }
        if (showContinueButton) {
            CommonButton(
                onClick = { onContinueClicked() },
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