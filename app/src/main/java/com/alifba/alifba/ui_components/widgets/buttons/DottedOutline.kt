package com.alifba.alifba.ui_components.widgets.buttons

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.sp

@Composable
fun DottedBorderButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Color.Gray,
    strokeWidth: Dp = 6.dp,
    dotSpacing: Dp = 10.dp,
    internalPadding: Dp = 12.dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)  // Adjust padding around the button as needed
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(dotSpacing.toPx(), dotSpacing.toPx()), 0f)
            drawRoundRect(
                color = color,
                topLeft = Offset.Zero,
                size = Size(this.size.width, this.size.height),
                cornerRadius = CornerRadius(12.dp.toPx()),
                style = Stroke(width = strokeWidth.toPx(), pathEffect = pathEffect)
            )
        }
        Text(
            text = text,
            color = Color.Black,
            fontSize = 16.sp,
            modifier = Modifier.padding(internalPadding)// Adjust the font size as needed
        )
    }
}
