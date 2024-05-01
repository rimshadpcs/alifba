package com.alifba.alifba.ui_layouts.lessonPath.layout

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.alifba.alifba.ui_layouts.lessonPath.models.LessonPathItem

@Composable
fun LessonPathItems(
    lesson: LessonPathItem,
    index: Int,
    onClick: () -> Unit,
) {
    val sidePadding = 128.dp
    val imageSize = 75.dp
    val borderColor = Color.Gray

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = sidePadding, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(imageSize)
                .align(if (index % 2 == 0) Alignment.CenterStart else Alignment.CenterEnd)
                .clip(CircleShape)
                .border(
                    width = 5.dp,
                    color = borderColor,
                    shape = CircleShape
                )
                .clickable(onClick = onClick)
                .background(Color.White)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = lesson.iconResId),
                contentDescription = null,
                modifier = Modifier.size(imageSize)
            )
        }
    }
}
