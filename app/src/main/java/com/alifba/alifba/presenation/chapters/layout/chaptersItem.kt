package com.alifba.alifba.presenation.chapters.layout

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.graphics.Color.Companion.White // White is available by default
import com.alifba.alifba.ui_components.theme.lightPurple
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import com.alifba.alifba.R
import com.alifba.alifba.presenation.chapters.models.Chapter
//import com.alifba.alifba.ui_components.theme.darkerNavy // Assuming darkerNavy is not used here
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChapterPathItems(
    lesson: Chapter,
    index: Int,
    onClick: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600

    // 1) Setup icons, sizes, offset - Make even larger for tablets
    val imageSize = if (isTablet) 300.dp else 200.dp  // 50% larger for tablets
    val cornerIconSize = if (isTablet) 58.dp else 38.dp  // 53% larger for tablets
    // val borderColor = lightPurple // borderColor is declared but not used

    val mainIcon = when (lesson.chapterType) {
        "Story"    -> R.drawable.storyisland
        "Alphabet" -> R.drawable.alphab
        "Lesson" -> R.drawable.lessonisland
        else       -> R.drawable.start
    }
    val cornerIcon = when {
        lesson.isCompleted -> R.drawable.tick
        //lesson.isLocked    -> R.drawable.padlock // We will gray out the image instead
        else -> R.drawable.start
    }

    // Zigzag offset logic - Proportional to screen size
    val baseOffset = if (isTablet) 30.dp else 20.dp  // 50% larger offset for tablets
    val horizontalOffset = if (index % 2 == 0) -baseOffset else baseOffset

    // Press animation
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "Scale"
    )
    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 0.dp else 5.dp, // This creates the "press down" effect
        label = "OffsetY"
    )
    val grayscaleMatrix = ColorMatrix().apply {
        setToSaturation(0f) // 0f for grayscale, 1f for original colors
    }
    val coroutineScope = rememberCoroutineScope()
    Log.d("LessonPathItems", "Chapter ${lesson.id} - isCompleted: ${lesson.isCompleted}")

    // 2) Main container for each path item
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Container with animation and click handling
        Box(
            modifier = Modifier
                .size(imageSize + 24.dp) // Overall clickable area
                .offset(x = horizontalOffset)
                .scale(scale)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null, // No ripple effect
                    onClick = {
                        coroutineScope.launch {
                            delay(100) // tiny delay to allow press animation to show
                            onClick()
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            // This Box is for the main image and its "press down" animation
            Box(
                modifier = Modifier
                    .size(imageSize + 16.dp) // Size of the main image container
                    .offset(y = offsetY)  // animate up/down on press. When not pressed, it's 5.dp down.
                    // When pressed, it moves to 0.dp (up by 5.dp)
                    .padding(4.dp), // Inner padding, if needed
                contentAlignment = Alignment.Center
            )
            {
                // Main icon (centered) - Responsive sizing
                val currentImageSize = when (lesson.chapterType) {
                    "Story", "Lesson" -> imageSize + if (isTablet) 30.dp else 20.dp // Make storyisland and lessonisland larger
                    else -> imageSize - if (isTablet) 24.dp else 16.dp
                }
                Image(
                    painter = painterResource(id = mainIcon),
                    contentDescription = lesson.chapterType,
                    modifier = Modifier
                        .size(currentImageSize)
                        .graphicsLayer(alpha = if (lesson.isLocked) 0.6f else 1.0f), // Apply transparency
                    colorFilter = if (lesson.isLocked) {
                        ColorFilter.colorMatrix(grayscaleMatrix) // Apply grayscale
                    } else {
                        null
                    }
                )
            }

            // Corner icon (top-right)
            // Only show corner icon if not locked
            if (!lesson.isLocked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(
                            x = if (isTablet) (-45).dp else (-30).dp, 
                            y = if (isTablet) 6.dp else 4.dp
                        ) // Position relative to the parent Box - proportional for tablets
                        .size(cornerIconSize)
                        .clip(CircleShape)
                        .border(if (isTablet) 3.dp else 2.dp, Color.White, CircleShape)
                        .background(Color.White), // Simplified background
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(id = cornerIcon),
                        contentDescription = when {
                            lesson.isCompleted -> "Completed"
                            else -> "Start"
                        },
                        modifier = Modifier
                            .size(cornerIconSize - 4.dp) // Slightly smaller than its container
                            .padding(2.dp), // Padding within the corner icon's background
                    )
                }
            }
        }
    }
}
