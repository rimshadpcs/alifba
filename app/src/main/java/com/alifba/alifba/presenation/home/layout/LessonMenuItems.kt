package com.alifba.alifba.presenation.home.layout

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alifba.alifba.R
import com.alifba.alifba.presenation.chapters.layout.PulsingStartIndicator

@Composable
fun LessonMenuItems(
    image: Int,
    name: String,
    onClick: () -> Unit,
    playIcon: Int = R.drawable.play
) {
    val alifbaFont = FontFamily(Font(R.font.more_sugar_regular, FontWeight.Normal))
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(screenHeight)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = image),
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
        // Row that shows the level name text and a play icon next to it.
       LevelPlayRow(name,playIcon,alifbaFont) {
           onClick()
       }
    }
}

@Composable
fun LevelPlayRow(
    name: String,
    playIcon: Int,
    alifbaFont: FontFamily,
    onClick: () -> Unit
) {
    PulsingLevelIndicator {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = name,
                style = TextStyle(
                    fontFamily = alifbaFont,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.15.sp,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = Modifier.padding(start = 16.dp))
            Image(
                painter = painterResource(id = playIcon),
                contentDescription = "Play",
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
fun PulsingLevelIndicator(modifier: Modifier = Modifier,
                          content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")

    // Scale animation
    val scale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(750),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Box(
        modifier = modifier
            .scale(scale.value).padding(4.dp)
    ) {
        content()
    }
}
@Preview(showBackground = true)
@Composable
fun LessonMenuItemsPreview() {
    LessonMenuItems(
        image = R.drawable.leveltwo,
        name = "Example Lesson",
        onClick = {}
    )
}