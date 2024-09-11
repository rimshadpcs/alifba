package com.alifba.alifba.presenation.lessonScreens.lessonSegment.LetterTracing

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.dp
import com.alifba.alifba.R
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
fun LetterTracingExercise(onNextClicked: () -> Unit) {
    val nearestTouchDistance = 100

    // Use a custom drawable for the arrow
    val painter = painterResource(id = R.drawable.down_arrow)

    val path = remember { Path() }
    val trackPath = remember { Path() }
    val userPath = remember { Path() }
    val pathMeasure = remember { PathMeasure() }

    val segmentInfoList = remember { mutableStateListOf<PathSegmentInfo>() }

    var currentIndex by remember { mutableIntStateOf(0) }
    var completedIndex by remember { mutableIntStateOf(-1) }
    var isTouched by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
    val showDialog = remember { mutableStateOf(false) }
    var showContinueButton by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    Box(modifier = Modifier.fillMaxSize()) {

        Column(modifier = Modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                isTouched = false
                                val currentPosition = it
                                var distance = Float.MAX_VALUE
                                var tempIndex = -1

                                segmentInfoList.forEachIndexed { index, pathSegmentInfo ->
                                    val currentDistance =
                                        pathSegmentInfo.position
                                            .minus(currentPosition)
                                            .getDistanceSquared()

                                    if (currentDistance < nearestTouchDistance * nearestTouchDistance &&
                                        currentDistance < distance
                                    ) {
                                        distance = currentDistance
                                        tempIndex = index
                                    }
                                }

                                // Valid touch should start from the top of the path (first segment)
                                val validTouch = if (completedIndex == -1) {
                                    tempIndex == 0 // Start at the first segment only
                                } else {
                                    tempIndex in currentIndex..(currentIndex + 2)
                                }

                                if (validTouch) {
                                    currentIndex = tempIndex
                                    isTouched = true
                                    text = "Touched index $currentIndex"
                                    userPath.moveTo(currentPosition.x, currentPosition.y)
                                } else {
                                    text = "Not correct position\n" +
                                            "tempIndex: $tempIndex, nearestPositionIndex: $currentIndex"
                                }
                            },
                            onDrag = { change: PointerInputChange, _ ->
                                if (isTouched) {
                                    val currentPosition = change.position
                                    var distance = Float.MAX_VALUE
                                    var tempIndex = -1

                                    segmentInfoList.forEachIndexed { index, pathSegmentInfo ->
                                        val currentDistance =
                                            pathSegmentInfo.position
                                                .minus(currentPosition)
                                                .getDistanceSquared()

                                        if (currentDistance < distance) {
                                            distance = currentDistance
                                            tempIndex = index
                                        }
                                    }

                                    text = ""

                                    val dragMinDistance =
                                        (nearestTouchDistance * .65f * nearestTouchDistance * .65)

                                    if (distance > dragMinDistance) {
                                        text = ""
                                        isTouched = false
                                    } else if (tempIndex < currentIndex) {
                                        text = ""
                                        isTouched = false
                                    } else {
                                        currentIndex = tempIndex
                                        userPath.lineTo(currentPosition.x, currentPosition.y)
                                    }

                                    if (currentIndex == segmentInfoList.size - 1 && !showDialog.value) {
                                        showDialog.value = true
                                        coroutineScope.launch {
                                            delay(1000)
                                            showContinueButton = true

                                        }

                                    }
                                }
                            }
                        )
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Calculate the bounds of the Alif path
                val pathBounds = RectF()
                val alifPath = com.alifba.alifba.presenation.lessonScreens.lessonSegment.createBaaPath()
                alifPath.asAndroidPath().computeBounds(pathBounds, true)

                // Calculate the translation needed to center the path
                val dx = (canvasWidth - pathBounds.width()) / 2 - pathBounds.left
                val dy = (canvasHeight - pathBounds.height()) / 2 - pathBounds.top

                // Centering the Alif Path
                if (path.isEmpty) {
                    path.addPath(alifPath, Offset(dx, dy))
                    pathMeasure.setPath(path = path, forceClosed = false)

                    val step = 1
                    val pathLength = pathMeasure.length / 100f

                    for ((index, percent) in (0 until 100 step step).withIndex()) {

                        val destination = Path()

                        val distance = pathLength * percent
                        pathMeasure.getSegment(
                            startDistance = distance,
                            stopDistance = pathLength * (percent + step),
                            destination = destination
                        )

                        val position = pathMeasure.getPosition(distance = distance)
                        val tangent = pathMeasure.getTangent(distance = distance)

                        val tan = (360 + atan2(tangent.y, tangent.x) * 180 / Math.PI) % 360

                        segmentInfoList.add(
                            PathSegmentInfo(
                                index = index,
                                position = position,
                                distance = distance,
                                tangent = tan
                            )
                        )
                    }

                    // Set the arrow to start at the top
                    currentIndex = 0
                }

                // Draw the Alif path
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

                segmentInfoList.getOrNull(currentIndex)?.let {
                    val isPathReceived = pathMeasure.getSegment(
                        startDistance = 0f,
                        stopDistance = it.distance,
                        destination = trackPath
                    )

                    if (isPathReceived) {
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
                    }

                    completedIndex = currentIndex

                    val iconSize = nearestTouchDistance * 2f

                    // Draw the arrow icon at the current position
                    withTransform(
                        transformBlock = {
                            rotate(degrees = it.tangent.toFloat() - 90f, pivot = it.position)
                            translate(
                                left = it.position.x - iconSize / 2,
                                top = it.position.y - iconSize / 2
                            )
                        }
                    ) {
                        with(painter) {
                            draw(size = Size(iconSize, iconSize))
                        }
                    }
                }
            }

        }

        LottieAnimationDialog(showDialog, lottieFileRes = R.raw.burst)

        if (showContinueButton) {
            CommonButton(
                onClick = { onNextClicked()},
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
//fun createLargerAlifPath(): Path {
//    return Path().apply {
//        moveTo(80f, 100f) // Start at the top of the Alif
//        cubicTo(120f, 500f, 80f, 500f, 100f, 700f) // Draw the main body of the Alif downwards
//    }
//}
//fun createBaaPath(): Path {
//    return Path().apply {
//        moveTo(100f, 120f) // Start at the beginning of the curve (left side of "Baa")
//        cubicTo(150f, 300f, 100f, 400f, 450f, 300f) // First curve segment
//        cubicTo(500f, 280f, 600f, 200f, 500f, 100f) // Second curve segment
//
//
//    }
//}