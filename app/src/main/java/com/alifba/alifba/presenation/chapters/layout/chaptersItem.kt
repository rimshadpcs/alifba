package com.alifba.alifba.presenation.chapters.layout

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
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


@Composable
fun LessonPathItems(
    lesson: Chapter,
    index: Int,
    onClick: () -> Unit,
) {
    val sidePadding = 128.dp
    val imageSize = 75.dp
    val borderColor = Color.White

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = sidePadding, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(imageSize + 8.dp)
                .align(if (index % 2 == 0) Alignment.CenterStart else Alignment.CenterEnd)
                .border(8.dp, borderColor, CircleShape)
                .padding(4.dp)
        ) {
            Card(
                modifier = Modifier
                    .size(imageSize)
                    .clickable(onClick = onClick),
                elevation = 16.dp,
                shape = CircleShape,
                contentColor = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val iconId = when {
                        lesson.isCompleted -> R.drawable.tick
                        lesson.isUnlocked && lesson.chapterType == "Story" -> R.drawable.book
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
}

