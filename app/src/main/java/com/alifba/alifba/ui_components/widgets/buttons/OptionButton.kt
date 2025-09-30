package com.alifba.alifba.ui_components.widgets.buttons

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import com.alifba.alifba.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
@Composable
fun OptionButton(onClick: () -> Unit, buttonText: String, isTablet: Boolean = false) {
    val configuration = LocalConfiguration.current
    val actualIsTablet = isTablet || (configuration.screenWidthDp > 600)
    
    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val coroutineScope = rememberCoroutineScope()
    // Animate the offset for the press effect to simulate a shadow
    val padding by animateDpAsState(targetValue = if (isPressed) (if (actualIsTablet) 8.dp else 6.dp) else (if (actualIsTablet) 12.dp else 8.dp), label = "")

    // Button
    Box(
        modifier = Modifier
            .padding(if (actualIsTablet) 12.dp else 8.dp)
            .clip(RoundedCornerShape(if (actualIsTablet) 12.dp else 8.dp))
            .background(Color(0xFF525252)) // Button face color
            .clickable(
                onClick = {
                    coroutineScope.launch {
                        delay(100)
                        onClick()
                    }
                },
                interactionSource = interactionSource,
                indication = null
            )
            .padding(
                horizontal = if (actualIsTablet) 12.dp else 8.dp, 
                vertical = if (actualIsTablet) 12.dp else 8.dp
            ) // Padding inside the button
            .padding(padding), // Simulated shadow effect by increasing padding
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = buttonText,
            fontFamily = alifbaFont,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = if (actualIsTablet) 22.sp else 17.sp,
            letterSpacing = if (actualIsTablet) 0.8.sp else 0.6.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewOptionButton() {
    OptionButton(
        onClick = {}, buttonText = "Next",
    )
}
