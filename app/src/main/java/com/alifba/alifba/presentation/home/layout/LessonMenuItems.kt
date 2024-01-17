package com.alifba.alifba.presentation.home.layout

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alifba.alifba.R

@Composable
fun LessonMenuItems(
    image: Int,
    name: String,
    modifier: Modifier = Modifier,
    onClick:()->Unit
) {
    val alifbaFont = FontFamily(
        Font(R.font.more_sugar_regular, FontWeight.Normal)
        // Add other font weights and styles if available
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(align = Alignment.CenterVertically)
            .padding(8.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = image),
            contentDescription = name,
            modifier = Modifier.size(400.dp)
        )

        Spacer(modifier = Modifier.height(16.dp)) // Add vertical spacing between image and text

        Text(
            text = name,
            style = TextStyle(
                fontFamily =alifbaFont ,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
            ,
            modifier = Modifier
                .fillMaxWidth() // Align text horizontally within the column
        )
    }
}
