package com.alifba.alifba.presenation.lessonScreens.lessonSegment.cloudExercise

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.alifba.alifba.R
import com.alifba.alifba.data.models.LessonSegment
import com.alifba.alifba.presenation.chapters.models.Cloud
import com.alifba.alifba.ui_components.dialogs.LottieAnimationDialog
import com.alifba.alifba.ui_components.theme.navyBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun CloudTappingExercise(
    segment: LessonSegment.CloudTappingLesson,
    onNextClicked: () -> Unit,

    ) {
    val textMeasurer = rememberTextMeasurer()
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Pull these from the lesson segment (Firestore).
    // Fallback to some defaults if the fields are empty or missing.
    val targetLetter = segment.targetLetter?.ifEmpty { "أ" }
    val nonTargetLetters = segment.nonTargetLetters?.ifEmpty {
        // If server data is empty, we use a default list.
        listOf("ب", "ت", "ث", "ج", "ح", "خ")
    }
//    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.running_car))
//    val progress by animateLottieCompositionAsState(
//        composition,
//        iterations = LottieConstants.IterateForever
//    )

    // Game state
    var correctTaps by remember { mutableStateOf(0) }
    var gameCompleted by remember { mutableStateOf(false) }

    // List of active clouds
    val clouds = remember { mutableStateListOf<Cloud>() }

    // Control how often the target letter appears
    var spawnCount by remember { mutableStateOf(0) }

    // Continuous animation for cloud movement
    val animationValue = rememberInfiniteTransition(label = "").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )

    // Function to add new clouds periodically
    LaunchedEffect(Unit) {
        while (!gameCompleted) {
            val screenWidth = 1000f // In real usage, you'd get this from Layout coords.

            // Increment the spawn counter
            spawnCount++

            // Every 4th spawn => target letter, else random from non-target list
            val letter = if (spawnCount % 3 == 0) {
                targetLetter
            } else {
                nonTargetLetters?.random()
            }

            val isTarget = (letter == targetLetter)

            letter?.let {
                Cloud(
                    letter = it,
                    position = Offset(
                        x = Random.nextFloat() * screenWidth,
                        // Start well below the screen so it floats up
                        y = 2000f
                    ),
                    // Range for bigger clouds
                    scale = Random.nextFloat() * 0.6f + 1.0f, // e.g. 1.0 to 1.6
                    // Slower speed so the game is not too fast
                    speed = Random.nextFloat() * 1.5f + 0.5f,
                    isTargetLetter = isTarget
                )
            }?.let {
                clouds.add(
                    it
                )
            }

            delay(2000) // Add a cloud every 2 seconds
        }
    }

    // Move clouds upward & check if game is complete
    LaunchedEffect(animationValue.value) {
        clouds.indices.forEach { i ->
            val cloud = clouds[i]
            if (cloud.isActive) {
                clouds[i] = cloud.copy(
                    // Slight horizontal movement using sin()
                    position = Offset(
                        x = cloud.position.x + sin(animationValue.value * 5) * 2,
                        // Move upward
                        y = cloud.position.y - cloud.speed * 2
                    )
                )

                // If the cloud goes off the top of the screen, remove it
                if (clouds[i].position.y < -200) {
                    clouds[i] = clouds[i].copy(isActive = false)
                }
            }
        }

        // Remove all inactive clouds (optional)
        if (animationValue.value == 1f) {
            clouds.removeAll { !it.isActive }
        }

        // Check if user has tapped 3 correct target-letter clouds
        if (correctTaps >= 3 && !gameCompleted) {
            gameCompleted = true
        }
    }

    // Animate the "burst" when we deactivate a cloud
    LaunchedEffect(Unit) {
        while (true) {
            clouds.indices.forEach { i ->
                val cloud = clouds[i]
                if (!cloud.isActive && cloud.burstAnimation < 1f) {
                    clouds[i] = cloud.copy(burstAnimation = cloud.burstAnimation + 0.05f)
                }
            }
            delay(16) // ~60 fps
        }
    }

    // UI layout and Canvas
    Box(
        modifier = Modifier
            .fillMaxSize()

    ) {
        Image(
            painter = painterResource(id = R.drawable.cloudtaplesson_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        if (gameCompleted) {
            // Show a Lottie animation or a success message
            LottieAnimationDialog(
                showDialog = remember { mutableStateOf(true) },
                lottieFileRes = R.raw.burst
            )
            // Delay and proceed when done
            LaunchedEffect(Unit) {
                delay(1500)
                onNextClicked()
            }
        }

        // Main canvas for clouds & taps
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        // Check if tap hits any cloud
                        for (i in clouds.indices) {
                            val cloud = clouds[i]
                            if (cloud.isActive && isPointInCloud(tapOffset, cloud)) {
                                // If it's the target letter, increment correctTaps
                                if (cloud.isTargetLetter) {
                                    correctTaps++
                                    // Deactivate => triggers burst animation
                                    clouds[i] = cloud.copy(isActive = false)
                                } else {
                                    // Vibrate device for incorrect tap
                                    coroutineScope.launch {
                                        val vibrator =
                                            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            vibrator.vibrate(
                                                VibrationEffect.createOneShot(
                                                    100,
                                                    VibrationEffect.DEFAULT_AMPLITUDE
                                                )
                                            )
                                        } else {
                                            @Suppress("DEPRECATION")
                                            vibrator.vibrate(100)
                                        }
                                    }
                                }
                                // Break after first found cloud
                                break
                            }
                        }
                    }
                }
        ) {
            // Draw each cloud or its burst
            clouds.forEach { cloud ->
                if (!cloud.isActive && cloud.burstAnimation in 0f..1f) {
                    drawBurstAnimation(cloud)
                } else if (cloud.isActive) {
                    drawCloud(cloud, textMeasurer)
                }
            }

        }
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(250.dp)
//                .align(Alignment.BottomCenter)
//        ) {
//            LottieAnimation(
//                composition = composition,
//                progress = progress,
//                modifier = Modifier.fillMaxWidth()
//            )
//        }
    }
}


//------------------------------------------------------
// DRAW A CLOUD
//------------------------------------------------------
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCloud(
    cloud: Cloud,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val cloudColor = Color(0xFF80CDF6) // Could differentiate if you like
    val x = cloud.position.x
    val y = cloud.position.y
    val scale = cloud.scale * 200  // Larger base size for a bigger cloud

    // Use a path of overlapping ovals to draw a cloud-like shape
    val cloudPath = Path().apply {
        moveTo(x, y)
        addOval(
            androidx.compose.ui.geometry.Rect(
                left = x - scale * 0.5f,
                top = y - scale * 0.5f,
                right = x + scale * 0.5f,
                bottom = y + scale * 0.5f
            )
        )
        addOval(
            androidx.compose.ui.geometry.Rect(
                left = x - scale * 0.7f,
                top = y - scale * 0.3f,
                right = x - scale * 0.1f,
                bottom = y + scale * 0.3f
            )
        )
        addOval(
            androidx.compose.ui.geometry.Rect(
                left = x + scale * 0.1f,
                top = y - scale * 0.3f,
                right = x + scale * 0.7f,
                bottom = y + scale * 0.3f
            )
        )
        addOval(
            androidx.compose.ui.geometry.Rect(
                left = x - scale * 0.4f,
                top = y - scale * 0.6f,
                right = x + scale * 0.2f,
                bottom = y
            )
        )
        addOval(
            androidx.compose.ui.geometry.Rect(
                left = x - scale * 0.2f,
                top = y - scale * 0.6f,
                right = x + scale * 0.4f,
                bottom = y
            )
        )
    }

    // Draw the cloud
    drawPath(path = cloudPath, color = cloudColor)

    // Draw the letter in the center (fixed size, e.g. 32sp)
    val textLayoutResult = textMeasurer.measure(
        text = cloud.letter,
        style = TextStyle(
            fontSize = 32.sp,
            color = navyBlue,
            textAlign = TextAlign.Center
        )
    )
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(
            x - textLayoutResult.size.width / 2,
            y - textLayoutResult.size.height / 2
        )
    )
}

//------------------------------------------------------
// DRAW BURST ANIMATION
//------------------------------------------------------
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBurstAnimation(cloud: Cloud) {
    val particleCount = 12
    val radius = cloud.scale * 150 * cloud.burstAnimation

    repeat(particleCount) { i ->
        val angle = (i * (360 / particleCount)) * (Math.PI / 180)
        val x = cloud.position.x + cos(angle).toFloat() * radius
        val y = cloud.position.y + sin(angle).toFloat() * radius

        drawCircle(
            color = navyBlue.copy(alpha = 1f - cloud.burstAnimation),
            radius = cloud.scale * 20 * (1f - cloud.burstAnimation),
            center = Offset(x, y)
        )
    }
}

// HIT TEST FOR TAPPING A CLOUD
private fun isPointInCloud(point: Offset, cloud: Cloud): Boolean {
    val distance = calculateDistance(point, cloud.position)
    return distance < cloud.scale * 100  // Scaled radius
}

// Calculate distance between two points
private fun calculateDistance(point1: Offset, point2: Offset): Float {
    val dx = point1.x - point2.x
    val dy = point1.y - point2.y
    return kotlin.math.sqrt(dx * dx + dy * dy)
}
