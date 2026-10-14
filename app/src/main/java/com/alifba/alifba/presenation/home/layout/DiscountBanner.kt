package com.alifba.alifba.presenation.home.layout

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import com.alifba.alifba.ui_components.widgets.LessonTheme
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alifba.alifba.R
import kotlinx.coroutines.delay

@Composable
fun DiscountBanner(
    expiryTimestamp: Long,
    onClick: () -> Unit
) {
    val timeRemaining by produceState(
        initialValue = expiryTimestamp - System.currentTimeMillis(),
        key1 = expiryTimestamp
    ) {
        while (true) {
            value = expiryTimestamp - System.currentTimeMillis()
            if (value <= 0) break
            delay(1000)
        }
    }

    if (timeRemaining > 0) {
        // "23h left", then "45m left" once it drops under an hour
        val totalSeconds = timeRemaining / 1000
        val hours = totalSeconds / 3600
        val countdownText = if (hours >= 1) "${hours}h left" else "${maxOf(1L, totalSeconds / 60)}m left"

        DiscountBannerContent(
            countdownText = countdownText,
            onClick = onClick
        )
    }
}

/**
 * Small gift in the home map's top corner: the discount on a red tag and the time left under it.
 * It never covers the path or the Moon; tapping opens the grown-up check, then the offer.
 */
@Composable
fun DiscountBannerContent(
    countdownText: String,
    onClick: () -> Unit
) {
    val k = LessonTheme.scale()
    val size = (56 * k).dp

    val infiniteTransition = rememberInfiniteTransition(label = "giftWiggle")
    val wiggle by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(animation = tween(1200), repeatMode = RepeatMode.Reverse),
        label = "wiggle"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            SoundEffectManager.playClickSound()
            onClick()
        }
    ) {
        Box(modifier = Modifier.size(size)) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .shadow(6.dp, CircleShape)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Same red present with a gold ribbon as the discount paywall
                com.alifba.alifba.presenation.stories.GiftIllustration(
                    modifier = Modifier
                        .size((32 * k).dp)
                        .rotate(wiggle)
                )
            }
            Text(
                text = "20%",
                style = LessonTheme.answer().copy(fontSize = (12 * k).sp, color = Color.White),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (10 * k).dp, y = (-6 * k).dp)
                    .border(2.dp, Color.White, CircleShape)
                    .background(Color(0xFFF2545B), CircleShape)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Text(
            text = countdownText,
            style = LessonTheme.answer().copy(fontSize = (13 * k).sp),
            modifier = Modifier
                .offset(y = (-8 * k).dp)
                .shadow(3.dp, CircleShape)
                .background(Color.White, CircleShape)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF7CC77A)
@Composable
fun DiscountBannerMobilePreview() {
    Box(modifier = Modifier.padding(20.dp)) {
        DiscountBannerContent(countdownText = "23h left", onClick = {})
    }
}
