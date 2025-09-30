package com.alifba.alifba.ui_components.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration


@Composable
fun StripedProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float,
    stripeColor: Color,
    stripeColorSecondary: Color,
    backgroundColor: Color,
    clipShape: Shape = RoundedCornerShape(16.dp)
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600
    
    Box(
        modifier = modifier
            .clip(if (isTablet) RoundedCornerShape(20.dp) else clipShape)
            .fillMaxWidth()
            .background(backgroundColor)
            .height(if (isTablet) 27.dp else 18.dp)
    ) {
        // Progress-filled striped area
        Box(
            modifier = Modifier
                .clip(if (isTablet) RoundedCornerShape(20.dp) else clipShape)
                .background(createStripeBrush(stripeColor, stripeColorSecondary, if (isTablet) 7.dp else 5.dp))
                .fillMaxHeight()
                .fillMaxWidth(progress)
        )
    }
}

@Composable
private fun createStripeBrush(
    stripeColor: Color,
    stripeBg: Color,
    stripeWidth: Dp
): Brush {
    val stripeWidthPx = with(LocalDensity.current) { stripeWidth.toPx() }
    val brushSizePx = 2 * stripeWidthPx
    val stripeStart = stripeWidthPx / brushSizePx

    return Brush.linearGradient(
        0.2f to stripeBg,
        stripeStart to stripeColor,
        1.0f to stripeBg,
        start = Offset(0f, 0f),
        end = Offset(brushSizePx, brushSizePx),
        tileMode = TileMode.Mirror
    )
}
