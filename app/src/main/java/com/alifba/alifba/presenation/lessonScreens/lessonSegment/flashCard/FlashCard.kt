package com.alifba.alifba.presenation.lessonScreens.lessonSegment.flashCard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alifba.alifba.R
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white

@Composable
fun FlashCard(
    title: String,
    description: String,
    imageResId: Int,
    modifier: Modifier = Modifier
) {
    val alifbaFont = FontFamily(
        Font(R.font.more_sugar_regular, FontWeight.SemiBold)
    )
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(vertical = 64.dp, horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = lightNavyBlue)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title and Description
                Text(
                    text = title,
                    color = white,
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = alifbaFont
                )
                Spacer(modifier = Modifier.height(64.dp))
                Text(
                    text = description,
                    fontSize = 18.sp,
                    color = white,
                    textAlign = TextAlign.Center,
                    fontFamily = alifbaFont
                )
            }

            // Image at the bottom
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = null,
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )
        }
    }
}



@Preview(showBackground = true, backgroundColor = 0xFFFFFF, widthDp = 412, heightDp = 892)
@Composable
fun FlashCardScreen() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        FlashCard(
//            title = "Allah's names",
//            description = "Ar -Raheem (The most compassionate)",
//            imageResId = R.drawable.wave
//        )
    }
}