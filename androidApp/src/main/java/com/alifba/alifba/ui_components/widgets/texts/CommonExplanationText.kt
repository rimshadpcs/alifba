package com.alifba.alifba.ui_components.widgets.texts

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alifba.alifba.R


@Composable
fun CommonExplanationText(text: String = "",modifier: Modifier) {

    val alifbaFont = FontFamily(
        Font(R.font.more_sugar_regular, FontWeight.SemiBold)
    )
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge, // Example style
        color = Color.Gray,
        fontFamily = alifbaFont,
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