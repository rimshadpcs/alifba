package com.alifba.alifba.ui_components.widgets.buttons

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alifba.alifba.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MCQChoiceButton(onClick: () -> Unit, buttonText: String, mainColor: Color, shadowColor: Color) {
    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val coroutineScope = rememberCoroutineScope()
    // Animate the offset for the press effect
    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 0.dp else 5.dp,
        animationSpec = spring(), label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(72.dp) // Total height including the shadow
            .clip(RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.TopCenter,

        ) {
        // Shadow layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(shadowColor) // Shadow color
        )

        // Button face layer
        Box(

            modifier = Modifier
                .matchParentSize()
                .padding(bottom = offsetY) // Apply the offset for the press effect
                .clip(RoundedCornerShape(8.dp))
                .background(mainColor) // Button face color
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
            Text(
                text = buttonText,
                fontFamily = alifbaFont,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 26.sp,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMCQChoiceButton() {
    MCQChoiceButton(onClick = {}, buttonText = "Next", mainColor =Color(0xFFFF8AD1), shadowColor = Color(0xFFe57cbc))
}
