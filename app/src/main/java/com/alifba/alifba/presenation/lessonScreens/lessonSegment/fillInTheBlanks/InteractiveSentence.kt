package com.alifba.alifba.presenation.lessonScreens.lessonSegment.fillInTheBlanks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.alifba.alifba.data.models.OptionsForFillInTheBlanks


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InteractiveSentence(
    sentenceParts: List<String>,
    blanksState: Map<Int, OptionsForFillInTheBlanks?>,
    fontFamily: FontFamily,
    isTablet: Boolean,
    onBlankClicked: (Int) -> Unit
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (isTablet) 12.dp else 8.dp)
    ) {
        sentenceParts.forEachIndexed { index, part ->
            Box(modifier = Modifier.padding(bottom = if (isTablet) 4.dp else 2.dp)) {
                if (part.trim().all { it == '_' }) {
                    val selectedOption = blanksState[index]
                    Text(
                        text = selectedOption?.option ?: "____",
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
                        ),
                        modifier = Modifier
                            .clickable { onBlankClicked(index) }
                            .padding(if (isTablet) 6.dp else 4.dp),
                        fontFamily = fontFamily,
                        fontSize = if (isTablet) 32.sp else 25.sp,
                        color = if (selectedOption == null) Color.Black else Color(0xFF8DD54f)
                    )
                } else {
                    Text(
                        text = part,
                        fontFamily = fontFamily,
                        fontSize = if (isTablet) 32.sp else 25.sp
                    )
                }
                //Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color.Gray)) // Underline for each element
            }
        }
    }
}
