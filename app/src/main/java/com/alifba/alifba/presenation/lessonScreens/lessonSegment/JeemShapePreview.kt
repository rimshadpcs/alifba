package com.alifba.alifba.presenation.lessonScreens.lessonSegment

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun JeemShapePreview() {
    val jeemShape = remember { createJeemShape() }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            // Calculate bounds and center the shape
            val bounds = RectF()
            jeemShape.mainPath.asAndroidPath().computeBounds(bounds, true)
            
            val dx = (canvasWidth - bounds.width()) / 2 - bounds.left
            val dy = (canvasHeight - bounds.height()) / 2 - bounds.top
            val translation = Offset(dx, dy)
            
            // Create centered path
            val centeredPath = Path().apply {
                addPath(jeemShape.mainPath, translation)
            }
            
            // Draw the main path
            drawPath(
                path = centeredPath,
                color = Color.Black,
                style = Stroke(
                    width = 20.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
            
            // Draw dots
            jeemShape.dots.forEach { dot ->
                val adjustedCenter = dot.center + translation
                drawCircle(
                    color = Color.Black,
                    radius = dot.radius,
                    center = adjustedCenter
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JeemShapePreviewPreview() {
    JeemShapePreview()
}