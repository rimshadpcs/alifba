package com.alifba.alifba.presenation.home.layout

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
        val totalSeconds = timeRemaining / 1000
        val hours = (totalSeconds / 3600)
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        val countdownText = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        DiscountBannerContent(
            countdownText = countdownText,
            onClick = onClick
        )
    }
}

@Composable
fun DiscountBannerContent(
    countdownText: String,
    onClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    
    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )

    val infiniteTransition = rememberInfiniteTransition(label = "bannerPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isTablet) 32.dp else 16.dp, vertical = 8.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF2C2C2C), Color(0xFF000000)) // Sleek black gradient
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Same image as paywall
            Image(
                painter = painterResource(id = R.drawable.limitedoffer),
                contentDescription = null,
                modifier = Modifier
                    .size(if (isTablet) 70.dp else 50.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    },
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.width(if (isTablet) 24.dp else 12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Your 20% Gift is EXTENDED!",
                    fontFamily = alifbaFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isTablet) 18.sp else 14.sp,
                    color = Color(0xFFf29c1f) // Matching gold/orange
                )
                Text(
                    text = "Expires in $countdownText",
                    fontFamily = alifbaFont,
                    fontSize = if (isTablet) 15.sp else 12.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            
            Text(
                text = "CLAIM →",
                fontFamily = alifbaFont,
                fontWeight = FontWeight.Bold,
                fontSize = if (isTablet) 16.sp else 13.sp,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F6EF)
@Composable
fun DiscountBannerMobilePreview() {
    DiscountBannerContent(
        countdownText = "23:59:59",
        onClick = {}
    )
}

@Preview(showBackground = true, widthDp = 800, backgroundColor = 0xFFF7F6EF)
@Composable
fun DiscountBannerTabletPreview() {
    DiscountBannerContent(
        countdownText = "23:59:59",
        onClick = {}
    )
}
