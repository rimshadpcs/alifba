package com.alifba.alifba.presenation.lessonScreens.lessonSegment.fillInTheBlanks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alifba.alifba.models.OptionsForFillInTheBlanks


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InteractiveSentence(
    sentenceParts: List<String>,
    blanksState: MutableState<Map<Int, OptionsForFillInTheBlanks?>>,
    fontFamily: FontFamily,
    onBlankClicked: (Int) -> Unit
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        sentenceParts.forEachIndexed { index, part ->
            Box(modifier = Modifier.padding(bottom = 2.dp)) {
                if (part.trim() == "____") {
                    val selectedOption = blanksState.value[index]
                    Text(
                        text = selectedOption?.option ?: "____",
                        modifier = Modifier
                            .clickable { onBlankClicked(index) }
                            .padding(4.dp),
                        fontFamily = fontFamily,
                        fontSize = 18.sp,
                        color = if (selectedOption == null) Color.Black else Color(0xFF8DD54f)
                    )
                } else {
                    Text(
                        text = part,
                        fontFamily = fontFamily,
                        fontSize = 18.sp
                    )
                }
                //Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color.Gray)) // Underline for each element
            }
        }
    }
}
