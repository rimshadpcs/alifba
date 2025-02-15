package com.alifba.alifba.presenation.chapters.layout

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alifba.alifba.R
import com.alifba.alifba.presenation.chapters.models.Chapter
import com.alifba.alifba.ui_components.theme.white
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LessonPathItems(
    lesson: Chapter,
    index: Int,
    onClick: () -> Unit,
) {
    // 1) Setup icons, sizes, offset
    val imageSize = 75.dp
    val cornerIconSize = 38.dp
    val borderColor = Color.White
    val mainIcon = when (lesson.chapterType) {
        "Story"    -> R.drawable.story
        "Alphabet" -> R.drawable.alphabeticon
        "Lesson"   -> R.drawable.book
        else       -> R.drawable.start
    }
    val cornerIcon = when {
        lesson.isCompleted -> R.drawable.tick
        lesson.isLocked    -> R.drawable.padlock
        else               -> R.drawable.start
    }

    // Zigzag offset logic
    val horizontalOffset = if (index % 2 == 0) (-20).dp else 20.dp

    // Press animation
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "Scale"
    )
    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 0.dp else 5.dp,
        label = "OffsetY"
    )
    val coroutineScope = rememberCoroutineScope()

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
                .size(imageSize + 24.dp)
                .offset(x = horizontalOffset)
                .scale(scale)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        coroutineScope.launch {
                            delay(100) // tiny delay to show press effect
                            onClick()
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            // Shadow (slightly larger and offset down)
            Box(
                modifier = Modifier
                    .size(imageSize + 8.dp)
                    .offset(y = 5.dp)
                    .clip(CircleShape)
                    .background(Color(0x40000000))  // Translucent black for shadow
            )

            // White border ring
            Box(
                modifier = Modifier
                    .size(imageSize + 16.dp)
                    .padding(bottom = offsetY)  // animate up/down on press
                    .border(8.dp, borderColor, CircleShape)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                // Main icon (centered)
                Image(
                    painter = painterResource(id = mainIcon),
                    contentDescription = lesson.chapterType,
                    modifier = Modifier.size(imageSize - 16.dp)
                )
            }

            // Corner icon (top-right), with proper positioning and larger size
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = -8.dp)  // Adjusted for larger size
                    .size(cornerIconSize)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape)
                    .background(
                        when {
                            lesson.isCompleted -> Color(0xFFFFFFFF)  // Brighter green for completed
                            lesson.isLocked -> Color(0xFFFFFFFF)     // Darker orange for locked
                            else -> Color(0xFFFFFFFF)                // Bright blue for start
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = cornerIcon),
                    contentDescription = when {
                        lesson.isCompleted -> "Completed"
                        lesson.isLocked -> "Locked"
                        else -> "Start"
                    },
                    modifier = Modifier
                        .size(cornerIconSize - 12.dp)  // Adjusted for visibility
                        .padding(2.dp),  // Added slight padding
                    //colorFilter = ColorFilter.tint(Color.White)
                )
            }
        }
    }
}