package com.alifba.alifba.presenation.home.layout

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.alifba.alifba.R
import com.alifba.alifba.ui_components.theme.darkPink
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.lightPink
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton


@Composable
fun UserProgressScreen() {
    Column(
        modifier = Modifier
            .padding(16.dp)
    ) {
        val alifbaFont = FontFamily(
            Font(R.font.more_sugar_regular, FontWeight.SemiBold)
        )
        // Top Row with Avatar and Profile Info
        Row(verticalAlignment = Alignment.CenterVertically) {
            // User Avatar
            Image(
                painter = painterResource(id = R.drawable.avatar9),
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(lightNavyBlue),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            // Profile Name and Age
            Column {
                Text(
                    text = "Ziya Maryam",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = navyBlue,
                    fontFamily = alifbaFont
                )
                Text(
                    text = "Age: 6",
                    fontSize = 16.sp,
                    color = navyBlue,
                    fontFamily = alifbaFont
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress Meter Bar
        LinearProgressIndicator(
            progress = 0.75f,
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(16.dp)),
            color = darkPink
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Current Chapter: The mercy of Allah",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = alifbaFont,
            //color = navyBlue,

        )

        Spacer(modifier = Modifier.height(16.dp))

        val userCards = listOf(
            UserCardData(R.drawable.lessons_learned, "Lessons learned", "12"),
            UserCardData(R.drawable.quizzes_attented, "Quizzes attended", "35"),
            UserCardData(R.drawable.streak, "Day Streak", "7"),
            UserCardData(R.drawable.xp, "Total xp", "99")
        )
        val badgeCards = listOf(
            BadgeCardData(R.drawable.noor_saber, "Noor Saber"),
            BadgeCardData(R.drawable.iman_inspirer, "Iman inspirer"),
            BadgeCardData(R.drawable.salam_spreader, "Salam spreader"),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()

        ) {
            items(userCards) { card ->
                UserCard(card)
            }
        }
        Row (modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween)
        {
            Text(
                text = "Achievements",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = alifbaFont,
            )
            Text(
                text = "View all",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = alifbaFont,
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()

        ) {
            items(badgeCards) { card ->
                BadgeCard(card)
            }
        }
        CommonButton(
            onClick = { /*TODO*/ },
            buttonText = "Detailed report for parents",
            shadowColor = darkPink ,
            mainColor = lightPink ,
            textColor = white
        )
    }
}

@Composable
fun UserCard(cardData: UserCardData) {
    val alifbaFont = FontFamily(
        Font(R.font.more_sugar_regular, FontWeight.SemiBold)
    )
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(white)
    ) {
        Column {
            Image(
                painter = painterResource(id = cardData.imageRes),
                contentDescription = cardData.title,
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                contentScale = ContentScale.Inside
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = cardData.title,
                fontSize = 18.sp,
                fontFamily = alifbaFont,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                textAlign = TextAlign.Center

            )
            Text(
                text = cardData.description,
                fontSize = 25.sp,
                fontFamily = alifbaFont,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                textAlign = TextAlign.Center
            )

        }

    }

}
@Composable
fun BadgeCard(badgeCardData: BadgeCardData) {
    val alifbaFont = FontFamily(
        Font(R.font.more_sugar_regular, FontWeight.SemiBold)
    )
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(white)
    ) {
        Column {
            Image(
                painter = painterResource(id = badgeCardData.imageRes),
                contentDescription = badgeCardData.title,
                modifier = Modifier
                    .height(75.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Inside
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = badgeCardData.title,
                fontSize = 10.sp,
                fontFamily = alifbaFont,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                textAlign = TextAlign.Center

            )
        }

    }

}


data class UserCardData(
    val imageRes: Int,
    val title: String,
    val description: String
)
data class BadgeCardData(
    val imageRes: Int,
    val title: String,
)

@Preview(showBackground = true)
@Composable
fun UserProgressScreenPreview() {
    UserProgressScreen()
}
