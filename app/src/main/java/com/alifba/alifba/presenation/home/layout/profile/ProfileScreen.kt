package com.alifba.alifba.presenation.home.layout.profile

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.alifba.alifba.R
import com.alifba.alifba.data.models.Badge
import com.alifba.alifba.presenation.home.layout.ProfileViewModel
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white

@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel
) {
    val userProfile by profileViewModel.userProfileState.collectAsState()

    LaunchedEffect(Unit) {
        profileViewModel.startProfileListener()
    }

    val alifbaFont = FontFamily(Font(R.font.more_sugar_regular, FontWeight.SemiBold))

    Column(
        modifier = Modifier
            .background(white)
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Row with Avatar and Profile Info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clickable {
                        Log.d("ProfileScreen", "Navigating to changeAvatar")
                        navController.navigate("changeAvatar")
                    }
            ) {
                // Avatar Image
                Image(
                    painter = if (userProfile != null) {
                        painterResource(id = getAvatarDrawable(userProfile!!.avatar))
                    } else {
                        painterResource(id = R.drawable.avatar9) // Placeholder while loading
                    },
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(lightNavyBlue),
                    contentScale = ContentScale.Crop
                )

                // Pencil Icon
                Image(
                    painter = painterResource(id = R.drawable.pencil),
                    contentDescription = "Edit Avatar",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(24.dp)
                        .background(white, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = userProfile?.childName ?: "Loading...",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = navyBlue,
                    fontFamily = alifbaFont
                )
                Text(
                    text = "Age: ${userProfile?.age ?: "--"}",
                    fontSize = 20.sp,
                    color = navyBlue,
                    fontFamily = alifbaFont
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress Overview Section
        Text(
            text = "Progress Overview",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = alifbaFont,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // User Cards with Dynamic Data
        val userCards = listOf(
            UserCardData(
                R.drawable.lessonslearnt_new,
                "Chapters Completed",
                "${userProfile?.chaptersCompleted?.size ?: 0}"
            ),
            UserCardData(
                R.drawable.quizzesnew,
                "Quizzes Attended",
                "${userProfile?.quizzesAttended ?: 0}"
            ),
            UserCardData(
                R.drawable.streaknew,
                "Day Streak",
                "${userProfile?.dayStreak ?: 0}"
            ),
            UserCardData(
                R.drawable.xpnew,
                "Total XP",
                "${userProfile?.xp ?: 0}"
            )
        )


        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(userCards) { card ->
                UserCard(card, elevation = 4.dp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Achievements Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Achievements",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = alifbaFont,
            )
            Text(
                text = "View All",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = navyBlue,
                modifier = Modifier.clickable { /* Navigate to achievements */ },
                fontFamily = alifbaFont,
            )

        }

        Spacer(modifier = Modifier.height(16.dp))

        // Detailed Report Button
//        CommonButton(
//            onClick = { /* Navigate to detailed report */ },
//            buttonText = "Detailed Report for Parents",
//            shadowColor = darkPink,
//            mainColor = lightPink,
//            textColor = white,
//            modifier = Modifier.fillMaxWidth()
//        )
    }
}


@Composable
fun UserCard(cardData: UserCardData, elevation: Dp) {
    val alifbaFont = FontFamily(Font(R.font.more_sugar_regular, FontWeight.SemiBold))

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(elevation), // Dynamic elevation
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(white)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = cardData.imageRes),
                contentDescription = cardData.title,
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Inside
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = cardData.title,
                fontSize = 12.sp,
                fontFamily = alifbaFont,
                textAlign = TextAlign.Center
            )
            Text(
                text = cardData.description,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = alifbaFont,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun BadgeCard(badgeCardData: Badge) {
    val alifbaFont = FontFamily(Font(R.font.more_sugar_regular, FontWeight.SemiBold))

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp), // Subtle elevation for badges
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(white)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = rememberAsyncImagePainter(badgeCardData.imageUrl),
                contentDescription = badgeCardData.title,
                modifier = Modifier
                    .height(75.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Inside
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = badgeCardData.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = alifbaFont,
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

fun getAvatarDrawable(avatarName: String): Int {
    return when (avatarName) {
        "Deenasaur" -> R.drawable.deenasaur_head
        "Duallama" -> R.drawable.duallama_head
        "Firdawsaur" -> R.drawable.firdawsaur_head
        "Ihsaninguin" -> R.drawable.ihsaninguin_head
        "Imamoth" -> R.drawable.imamoth_head
        "Khilafox" -> R.drawable.khilafox_head
        "Shukraf" -> R.drawable.shukraf_head
        "Jannahbee" -> R.drawable.jannahbee_head
        "Qadragon" -> R.drawable.qadragon_head
        "Sabracorn" -> R.drawable.sabracorn_head
        "Sadiqling" -> R.drawable.sadiqling_head
        "Sidqhog" -> R.drawable.sidqhog_head

        else -> R.drawable.avatar9
    }
}
//@Preview(showBackground = true)
//@Composable
//fun UserProgressScreenPreview() {
//    ProfileScreen()
//}
