package com.alifba.alifba.presenation.lessonScreens.lessonSegment.fillInTheBlanks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alifba.alifba.data.models.OptionsForFillInTheBlanks
import com.alifba.alifba.ui_components.widgets.LessonTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InteractiveSentence(
    sentenceParts: List<String>,
    blanksState: Map<Int, OptionsForFillInTheBlanks?>,
    isTablet: Boolean,
    /** Colour of a word once placed in a blank (primary while answering, then green or coral) */
    filledColor: Color = LessonTheme.primary,
    onBlankClicked: (Int) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (isTablet) 12.dp else 8.dp, Alignment.CenterHorizontally)
    ) {
        sentenceParts.forEachIndexed { index, part ->
            Box(modifier = Modifier.padding(bottom = if (isTablet) 4.dp else 2.dp)) {
                if (part.trim().all { it == '_' }) {
                    val selected = blanksState[index]
                    if (selected == null) {
                        // The empty blank is a thick, pale-blue line sitting on the text baseline:
                        // same height as a line of text, line near the bottom
                        val lineHeight = with(LocalDensity.current) { (28 * 1.25 * LessonTheme.scale()).sp.toDp() }
                        Box(
                            modifier = Modifier
                                .padding(if (isTablet) 6.dp else 4.dp)
                                .width(if (isTablet) 140.dp else 110.dp)
                                .height(lineHeight),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(bottom = lineHeight * 0.22f)
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(LessonTheme.blankLine, RoundedCornerShape(2.dp))
                            )
                        }
                    } else {
                        Text(
                            text = selected.option,
                            style = LessonTheme.question().copy(color = filledColor),
                            modifier = Modifier
                                .clickable { onBlankClicked(index) }
                                .padding(if (isTablet) 6.dp else 4.dp)
                        )
                    }
                } else {
                    Text(text = part, style = LessonTheme.question(), modifier = Modifier.padding(4.dp))
                }
            }
        }
    }
}
