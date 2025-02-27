package com.alifba.alifba.presenation.lessonScreens.lessonSegment.LetterTracing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.alifba.alifba.ui_components.dialogs.LottieAnimationDialog
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.smarttoolfactory.tutorial1_1basics.chapter6_graphics.PathSegmentInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.Text
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.LetterShape
import kotlin.math.pow

import android.graphics.RectF
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.alifba.alifba.R
import com.alifba.alifba.presenation.main.logScreenView
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.navyBlue
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import kotlin.math.atan2

@Composable
fun LetterTracingExercise(
    letterShape: LetterShape,

    onNextClicked: () -> Unit
) {
    LaunchedEffect(Unit) {
        logScreenView("lesson_screen")
    }
    LaunchedEffect(Unit) {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "LetterTracingExercise")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "LetterTracingExercise")
        }
    }

    val hasNoDots = remember { letterShape.dots.isEmpty() }

    // Track whether we show the "burst" Lottie animation
    val showDialog = remember { mutableStateOf(false) }

    // 1) States, coroutines
    val coroutineScope = rememberCoroutineScope()
    val translation = remember { mutableStateOf(Offset.Zero) }
    val path = remember { Path() }
    val trackPath = remember { Path() }
    val userPath = remember { Path() }
    val pathMeasure = remember { PathMeasure() }
    val segmentInfoList = remember { mutableStateListOf<PathSegmentInfo>() }

    var currentIndex by remember { mutableIntStateOf(0) }
    var isTouched by remember { mutableStateOf(false) }
    var pathCompleted by remember { mutableStateOf(false) }
    var allDotsCompleted by remember { mutableStateOf(false) }

    // Called when user completes path + dots
    fun handleExerciseComplete() {
        onNextClicked()
    }

    var showResetButton by remember { mutableStateOf(false) }
    val pathSegments = 500
    val nearestTouchDistance = 60f
    val searchWindow = 50

    // Painter for arrow
    val arrowPainter = painterResource(id = R.drawable.down_arrow)

    fun resetTrace() {
        currentIndex = 0
        isTouched = false
        userPath.reset()
        pathCompleted = false
        allDotsCompleted = false
    }

    // Handle user dragging
    fun handleDrag(currentPosition: Offset) {
        if (pathCompleted) return

        // Search for the nearest point on the path
        val searchStart = maxOf(0, currentIndex - 10)
        val searchEnd = minOf(segmentInfoList.size, currentIndex + searchWindow)

        var minDist = Float.MAX_VALUE
        var nearestIndex = -1

        for (idx in searchStart until searchEnd) {
            val seg = segmentInfoList[idx]
            val distSq = (seg.position - currentPosition).getDistanceSquared()
            if (distSq < minDist) {
                minDist = distSq
                nearestIndex = idx
            }
        }

        val trackThreshold = (nearestTouchDistance * 1.5f).pow(2)

        // If within threshold, update the path
        if (minDist <= trackThreshold) {
            isTouched = true
            if (nearestIndex >= currentIndex - 5) {
                // If user is continuing from a previous point or starting new
                if (!isTouched) {
                    userPath.moveTo(currentPosition.x, currentPosition.y)
                } else {
                    userPath.lineTo(currentPosition.x, currentPosition.y)
                }
                currentIndex = nearestIndex
            }

            // Check for completion
//            if (nearestIndex >= segmentInfoList.size - 1) {
//                isTouched = false
//                pathCompleted = true
//            }
            if (nearestIndex >= segmentInfoList.size - 1) {
                isTouched = false
                pathCompleted = true
                // If there are no dots, we should consider the exercise complete
                if (hasNoDots) {
                    allDotsCompleted = true
                    coroutineScope.launch {
                        // Show the burst animation briefly
                        showDialog.value = true
                        delay(1200)
                        showDialog.value = false
                        // Can trigger any other completion animations here
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                // Handle drag gestures
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (!pathCompleted) {
                                // Find nearest point on path to start from
                                var minDist = Float.MAX_VALUE
                                var startIndex = -1

                                for (idx in currentIndex until minOf(currentIndex + 20, segmentInfoList.size)) {
                                    val seg = segmentInfoList[idx]
                                    val distSq = (seg.position - offset).getDistanceSquared()
                                    if (distSq < minDist) {
                                        minDist = distSq
                                        startIndex = idx
                                    }
                                }

                                // If not found near currentIndex, search from beginning
                                if (minDist > nearestTouchDistance.pow(2)) {
                                    for (idx in 0 until currentIndex) {
                                        val seg = segmentInfoList[idx]
                                        val distSq = (seg.position - offset).getDistanceSquared()
                                        if (distSq < minDist) {
                                            minDist = distSq
                                            startIndex = idx
                                        }
                                    }
                                }

                                if (startIndex >= 0 && minDist < nearestTouchDistance.pow(2)) {
                                    isTouched = true
                                    currentIndex = startIndex
                                    userPath.moveTo(offset.x, offset.y)
                                }
                            }
                        },
                        onDrag = { change, _ ->
                            handleDrag(change.position)
                        }
                    )
                }
                // Handle tap gestures for the dots
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        if (pathCompleted && !allDotsCompleted) {
                            var allTapped = true
                            letterShape.dots.forEach { dot ->
                                val adjustedDotCenter = dot.center + translation.value
                                val dist = (tapOffset - adjustedDotCenter).getDistance()
                                if (dist <= dot.radius * 2) {
                                    dot.isTouched = true
                                }
                                if (!dot.isTouched) allTapped = false
                            }

                            if (allTapped) {
                                allDotsCompleted = true
                                coroutineScope.launch {
                                    // Show the burst animation briefly
                                    showDialog.value = true
                                    delay(1200)
                                    showDialog.value = false
                                }
                            }
                        }
                    }
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Build the path once
            if (path.isEmpty) {
                val bounds = RectF()
                letterShape.mainPath.asAndroidPath().computeBounds(bounds, true)

                // Calculate translation to center the letter
                val dx = (canvasWidth - bounds.width()) / 2 - bounds.left
                val dy = (canvasHeight - bounds.height()) / 2 - bounds.top
                translation.value = Offset(dx, dy)

                path.addPath(letterShape.mainPath, translation.value)
                pathMeasure.setPath(path, false)

                val stepLength = pathMeasure.length / pathSegments
                for (i in 0 until pathSegments) {
                    val distance = i * stepLength
                    val pos = pathMeasure.getPosition(distance)
                    val tan = pathMeasure.getTangent(distance)
                    val angle = (360 + atan2(tan.y, tan.x) * 180 / Math.PI) % 360

                    segmentInfoList.add(
                        PathSegmentInfo(
                            index = i,
                            position = pos,
                            distance = distance,
                            tangent = angle
                        )
                    )
                }
            }

            // Draw entire path in light color
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

            // Draw partial path
            val segment = segmentInfoList.getOrNull(currentIndex)
            if (segment != null) {
                trackPath.reset()
                if (pathMeasure.getSegment(0f, segment.distance, trackPath, true)) {
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

                // Draw arrow if not completed
                if (!pathCompleted) {
                    val arrowSize = nearestTouchDistance * 2f
                    withTransform({
                        rotate(degrees = segment.tangent.toFloat() - 90f, pivot = segment.position)
                        translate(
                            left = segment.position.x - arrowSize / 2,
                            top = segment.position.y - arrowSize / 2
                        )
                    }) {
                        with(arrowPainter) {
                            draw(size = Size(arrowSize, arrowSize))
                        }
                    }
                }
            }

            // Draw dots
            letterShape.dots.forEach { dot ->
                val adjustedCenter = dot.center + translation.value
                drawCircle(
                    color = if (dot.isTouched) Color.Black else Color.LightGray,
                    radius = dot.radius,
                    center = adjustedCenter
                )
            }
        }

        // Buttons at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            when {
                // Show "Next" if path + dots are completed
                (pathCompleted && (allDotsCompleted || hasNoDots)) -> {
                    CommonButton(
                        onClick = { handleExerciseComplete() },
                        buttonText = "Next",
                        shadowColor = navyBlue,
                        mainColor = lightNavyBlue,
                        textColor = white
                    )
                }
                // If user started drawing but hasn't finished
                (currentIndex > 0 && !pathCompleted) -> {
                    CommonButton(
                        onClick = { resetTrace() },
                        buttonText = "Reset",
                        shadowColor = navyBlue,
                        mainColor = lightNavyBlue,
                        textColor = white
                    )

                }
            }
        }

        // The "burst" Lottie animation when user taps all dots
        LottieAnimationDialog(
            showDialog = showDialog,
            lottieFileRes = R.raw.tick
        )
    }
}
