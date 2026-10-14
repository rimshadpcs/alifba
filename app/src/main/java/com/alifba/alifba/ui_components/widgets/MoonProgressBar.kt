package com.alifba.alifba.ui_components.widgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.alifba.alifba.R
import com.alifba.alifba.ui_components.theme.lightNavyBlue

/**
 * Lesson progress bar with the Moon mascot riding the leading edge of the fill (same idea as the
 * onboarding bar). Solid fill, no moving stripes, so the Moon is the only thing in motion.
 */
@Composable
fun MoonProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val isTablet = LocalConfiguration.current.screenWidthDp > 600
    val thumbSize = if (isTablet) 52.dp else 38.dp
    val barHeight = if (isTablet) 20.dp else 14.dp
    val clamped = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = clamped,
        animationSpec = tween(durationMillis = 350),
        label = "moonProgress"
    )

    BoxWithConstraints(
        modifier = modifier
            .height(thumbSize)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(clamped, 0f..1f) },
        contentAlignment = Alignment.CenterStart
    ) {
        // thumbX sweeps the full track so the Moon ends flush right at 100% instead of hanging
        // past the bar's end.
        val thumbX = (maxWidth - thumbSize).coerceAtLeast(0.dp) * animatedProgress
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(RoundedCornerShape(50))
                .background(LessonTheme.track)
        )
        Box(
            modifier = Modifier
                .width(thumbX + thumbSize / 2)
                .height(barHeight)
                .clip(RoundedCornerShape(50))
                .background(LessonTheme.primary)
        )
        Image(
            painter = painterResource(id = R.drawable.moon_progress_thumb),
            contentDescription = null,
            modifier = Modifier
                .offset(x = thumbX)
                .size(thumbSize)
        )
    }
}
