package com.alifba.alifba.ui_components.widgets.texts

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import com.alifba.alifba.R


@Composable
fun CommonExplanationText(text: String = "",modifier: Modifier) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600

    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )
    
    Text(
        text = text,
        fontFamily = alifbaFont,
        fontWeight = FontWeight.Bold,
        fontSize = if (isTablet) 36.sp else 28.sp,
        color = Color.Gray,
        lineHeight = 1.5.em,
        modifier = modifier.padding(if (isTablet) 16.dp else 12.dp)
    )
}
@Preview(showBackground = true)
@Composable
fun CommonExplanationTextPreview() {
    CommonExplanationText(
        text = "This is the first letter of the Arabic alphabet. It makes the 'a' sound as in 'apple'.",
        modifier = Modifier
    )
}