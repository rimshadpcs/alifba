package com.alifba.alifba.presenation.chapters.layout

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
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
    val sidePadding = 128.dp
    val imageSize = 75.dp
    val borderColor = Color.White

    // Same press animation logic:
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 0.dp else 5.dp,
        animationSpec = spring(),
        label = "IconOffset"
    )
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = sidePadding, vertical = 8.dp)
    ) {
        // Outer container for the circle & border
        Box(
            modifier = Modifier
                .size(imageSize + 8.dp)
                .align(if (index % 2 == 0) Alignment.CenterStart else Alignment.CenterEnd)
                .border(8.dp, borderColor, CircleShape) // The white ring around
                .padding(4.dp)
        ) {
            // 1) SHADOW LAYER
            Box(
                modifier = Modifier
                    .size(imageSize)
                    .clip(CircleShape)
                    .background(Color(0xFFAAAAAA)) // Shadow color
            )

            // 2) MAIN LAYER (with press offset)
            Box(
                modifier = Modifier
                    .size(imageSize)
                    .padding(bottom = offsetY)     // Moves up/down when pressed
                    .clip(CircleShape)
                    .background(Color.White)       // Main circle color
                    .clickable(
                        onClick = {
                            coroutineScope.launch {
                                // tiny delay for press animation
                                delay(100)
                                onClick()
                            }
                        },
                        interactionSource = interactionSource,
                        indication = null
                    ),
                contentAlignment = Alignment.Center
            ) {
                val iconId = when {
                    lesson.isCompleted && lesson.chapterType == "Story" -> R.drawable.book
                    lesson.isCompleted && lesson.chapterType == "Alphabet" -> R.drawable.alphabeticon
                    lesson.isCompleted && lesson.chapterType == "Lesson" -> R.drawable.tick
                    lesson.isUnlocked && lesson.chapterType == "Story" -> R.drawable.book
                    lesson.isUnlocked && lesson.chapterType == "Alphabet" -> R.drawable.alphabeticon
                    lesson.isUnlocked -> R.drawable.start
                    else -> R.drawable.padlock
                }

                Image(
                    painter = painterResource(id = iconId),
                    contentDescription = "Lesson Icon",
                    modifier = Modifier.size(imageSize)
                )
            }
        }
    }
}
