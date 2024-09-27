package com.alifba.alifba.presenation.home.layout

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alifba.alifba.R

@Composable
fun LessonMenuItems(
    image: Int,
    name: String,
    onClick: () -> Unit
) {
    val alifbaFont = FontFamily(Font(R.font.more_sugar_regular, FontWeight.Normal))
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(screenHeight)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp), // Consistent spacing in LazyRow
        horizontalAlignment = Alignment.CenterHorizontally // This aligns children horizontally
    ) {
        Image(
            painter = painterResource(id = image),
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .weight(1f)  // Image will fill the space, leaving only necessary space for the text
                .fillMaxWidth()
        )
        Text(
            text = name,
            style = TextStyle(
                fontFamily = alifbaFont,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.15.sp,
                textAlign = TextAlign.Center // Centers text within its bounds
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)  // Apply padding uniformly around the text
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LessonMenuItemsPreview() {
    LessonMenuItems(
        image = R.drawable.leveltwo, // Ensure you have this drawable in your resources
        name = "Example Lesson",
        onClick = {}
    )
}
