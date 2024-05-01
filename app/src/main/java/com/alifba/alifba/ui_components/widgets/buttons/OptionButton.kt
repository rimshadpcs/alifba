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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.alifba.alifba.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
@Composable
fun OptionButton(onClick: () -> Unit, buttonText: String) {
    val alifbaFont = FontFamily(Font(R.font.more_sugar_regular, FontWeight.SemiBold))
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val coroutineScope = rememberCoroutineScope()
    var isSelected by remember { mutableStateOf(false) }
    // Animate the offset for the press effect to simulate a shadow
    val padding by animateDpAsState(targetValue = if (isPressed) 6.dp else 8.dp)

    // Button
    Box(
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
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
            .padding(horizontal = 8.dp, vertical = 8.dp) // Padding inside the button
            .padding(padding), // Simulated shadow effect by increasing padding
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = buttonText,
            fontFamily = alifbaFont,
            color = Color.White,
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
