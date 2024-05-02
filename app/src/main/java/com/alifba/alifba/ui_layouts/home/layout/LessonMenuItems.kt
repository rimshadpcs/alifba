package com.alifba.alifba.ui_layouts.home.layout

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.inset
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val alifbaFont = FontFamily(Font(R.font.more_sugar_regular, FontWeight.Normal))
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val circleSize = screenWidth * 0.75f // Reduced size for the circle

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(circleSize) // Set width to circleSize
                .height(circleSize) // Set height to circleSize to ensure it's fully circular
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val strokeWidth = 10.dp.toPx()
                drawCircle(
                    color = Color.White,
                    radius = size.minDimension / 2 - strokeWidth // Adjusting radius to account for stroke width
                )
                drawCircle(
                    color = Color(0xFFBF8FFD),
                    radius = size.minDimension / 2 - strokeWidth,
                    style = Stroke(width = strokeWidth)
                )
            }
            Image(
                painter = painterResource(id = image),
                contentDescription = name,
                modifier = Modifier
                    .size(circleSize * 0.85f) // Adjust the size to fit within the circle
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .background(Color.White, shape = RoundedCornerShape(12.dp))
        ) {
            Text(
                text = name,
                style = TextStyle(
                    fontFamily = alifbaFont,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFBF8FFD),
                    letterSpacing = 0.15.sp
                ),
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 32.dp)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LessonMenuItemsPreview() {
    LessonMenuItems(
        image = R.drawable.leveltwo, // Use a real drawable resource ID
        name = "Example Lesson",
        modifier = Modifier
            .padding(16.dp) // Add some padding for better visualization in preview
            .background(Color.DarkGray), // Optional background to contrast the white text
        onClick = { /* No action for preview */ }
    )
}