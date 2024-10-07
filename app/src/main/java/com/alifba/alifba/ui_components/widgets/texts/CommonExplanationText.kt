package com.alifba.alifba.ui_components.widgets.texts

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.alifba.alifba.R


@Composable
fun CommonExplanationText(text: String = "",modifier: Modifier) {

    val alifbaFont = FontFamily(
        Font(R.font.more_sugar_regular, FontWeight.SemiBold)
    )
    Text(
        text = text,
        style = LocalTextStyle.current.merge(
            TextStyle(
                lineHeight = 1.5.em,
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                ),
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.None
                )
            )
        )
        ,
        color = Color.Gray,
        fontFamily = alifbaFont,
        fontSize = 25.sp,
        modifier =
        Modifier
            .padding(12.dp)
    )
}
@Preview()
@Composable
fun NextButton() {
    CommonExplanationText(
        modifier = Modifier
    )
}