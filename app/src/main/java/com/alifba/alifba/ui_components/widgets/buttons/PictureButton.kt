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
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.alifba.alifba.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PictureButton(onClick: () -> Unit, buttonImage: String, buttonText: String) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val coroutineScope = rememberCoroutineScope()
    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 0.dp else 5.dp,
        animationSpec = spring(), label = ""
    )
    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.SemiBold)
    )

    Box(
        modifier = Modifier
            .width(150.dp)
            .height(150.dp)
            .clip(RoundedCornerShape(8.dp)),
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
                .clip(RoundedCornerShape(8.dp))
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
                        .padding(8.dp)
                        .clip(shape = RoundedCornerShape(32.dp)),
                    contentScale = ContentScale.Inside
                )

                Text(
                    text = buttonText,
                    color = Color.Black,
                    fontFamily = alifbaFont,
                    fontSize = 17.sp
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