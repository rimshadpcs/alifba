package com.alifba.alifba.ui_components.widgets.buttons

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import coil.compose.rememberImagePainter
import com.alifba.alifba.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PictureButton(
    onClick: () -> Unit,
    buttonImage: String,
    buttonText: String,
    isTablet: Boolean = false,
    tabletSize: Dp = 200.dp,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val actualIsTablet = isTablet || (configuration.screenWidthDp > 600)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val coroutineScope = rememberCoroutineScope()
    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 0.dp else (if (actualIsTablet) 8.dp else 5.dp),
        animationSpec = spring(), label = ""
    )
    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )

    Box(
        modifier = modifier
            .width(if (actualIsTablet) tabletSize else 150.dp)
            .height(if (actualIsTablet) tabletSize else 150.dp)
            .clip(RoundedCornerShape(if (actualIsTablet) 12.dp else 8.dp)),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(bottom = offsetY) // Apply the offset for the press effect
                .clip(RoundedCornerShape(if (actualIsTablet) 12.dp else 8.dp))
                .background(Color(0xFFf9f9f9)) // Button face color
                .clickable(
                    onClick = {
                        coroutineScope.launch {
                            delay(100)
                            onClick()
                        }
                    },
                    interactionSource = interactionSource,
                    indication = null
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = if (buttonImage.startsWith("http")) {
                        rememberImagePainter(buttonImage)
                    } else {
                        painterResource(id = buttonImage.toIntOrNull() ?: R.drawable.error)
                    },
                    contentDescription = "Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(.7f)
                        .padding(if (actualIsTablet) 12.dp else 8.dp)
                        .clip(shape = RoundedCornerShape(if (actualIsTablet) 40.dp else 32.dp)),
                    contentScale = ContentScale.Inside
                )

                Text(
                    text = buttonText,
                    color = Color.Black,
                    fontFamily = alifbaFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (actualIsTablet) 22.sp else 17.sp
                )
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun PreviewPictureButton() {
//    PictureButton(onClick = {}, buttonImage = R.drawable.flower, buttonText = "Flowers")
//}


/**
 * Picture choice card for the picture MCQ lesson (PictureButton above is kept as-is for the
 * onboarding quiz): white face with a thick light-gray border and a gray "lip" underneath (the
 * raised-button look), the picture filling most of the card and the label under it. Pressing
 * sinks the face onto the lip and tints it brand blue for a moment.
 */
@Composable
fun PictureChoiceCard(
    onClick: () -> Unit,
    buttonImage: String,
    buttonText: String,
    size: Dp,
    isTablet: Boolean = false,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val coroutineScope = rememberCoroutineScope()
    val lip = if (isTablet) 8.dp else 6.dp
    val faceOffset by animateDpAsState(
        targetValue = if (isPressed) lip else 0.dp,
        animationSpec = spring(), label = ""
    )
    val radius = if (isTablet) 28.dp else 22.dp
    val borderWidth = 4.dp
    val faceColor = if (isPressed) Color(0xFFEAF0FF) else Color.White
    val borderColor = if (isPressed) Color(0xFF7C93F8) else Color(0xFFE5E5E5)
    val lipColor = if (isPressed) Color(0xFF526FF6) else Color(0xFFD9D9D9)
    val labelColor = if (isPressed) Color(0xFF526FF6) else Color(0xFF33333A)
    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )

    Box(
        modifier = modifier
            .width(size)
            .height(size + lip)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    coroutineScope.launch {
                        delay(100)
                        onClick()
                    }
                }
            )
    ) {
        // Lip (the gray edge under the card)
        Box(
            modifier = Modifier
                .offset(y = lip)
                .size(size)
                .clip(RoundedCornerShape(radius))
                .background(lipColor)
        )
        // Face: border colour, then the face colour inset by the border width
        Box(
            modifier = Modifier
                .offset(y = faceOffset)
                .size(size)
                .clip(RoundedCornerShape(radius))
                .background(borderColor)
                .padding(borderWidth)
                .clip(RoundedCornerShape(radius - borderWidth))
                .background(faceColor)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = if (buttonImage.startsWith("http")) {
                        rememberImagePainter(buttonImage)
                    } else {
                        painterResource(id = buttonImage.toIntOrNull() ?: R.drawable.error)
                    },
                    contentDescription = buttonText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 14.dp, start = 12.dp, end = 12.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = buttonText,
                    color = labelColor,
                    fontFamily = alifbaFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isTablet) 22.sp else 17.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
                )
            }
        }
    }
}
