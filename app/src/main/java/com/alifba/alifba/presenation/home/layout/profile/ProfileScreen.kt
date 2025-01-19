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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.alifba.alifba.R
import com.alifba.alifba.data.models.Badge
import com.alifba.alifba.presenation.home.layout.ProfileViewModel
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton

@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel
) {
    val userProfile by profileViewModel.userProfileState.collectAsState()

    LaunchedEffect(Unit) {
        profileViewModel.startProfileListener()
    }
    val earnedBadges by profileViewModel.earnedBadges.collectAsState()


    val alifbaFont = FontFamily(Font(R.font.more_sugar_regular, FontWeight.SemiBold))

    Column(
        modifier = Modifier
            .background(white)
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.goback),
                contentDescription = "Back",
                modifier = Modifier
                    .clickable { navController.popBackStack() }
                    .size(36.dp),
            )
            Text(
                text = "My Profile",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = navyBlue,
                fontFamily = alifbaFont
            )
            // Empty box for symmetry
            Box(modifier = Modifier.size(24.dp))
        }
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
                R.drawable.levels,
                "Levels Completed",
                "${userProfile?.levelsCompleted?.size ?: 0}"
            ),
            UserCardData(
                R.drawable.chapters,
                "Lessons Completed",
                "${userProfile?.chaptersCompleted?.size ?: 0}"
            ),
            UserCardData(
                R.drawable.stories,
                "Stories Completed",
                "${userProfile?.storiesCompleted?.size ?: 0}"
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
            columns = GridCells.Fixed(2), // 2 columns
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp), // Adjust vertical spacing
            horizontalArrangement = Arrangement.spacedBy(8.dp), // Adjust horizontal spacing
            modifier = Modifier.fillMaxWidth()
        ) {
            items(userCards) { card ->
                UserCard(card, elevation = 2.dp) // Slightly reduce elevation
            }
        }




        Spacer(modifier = Modifier.height(4.dp))

        // Achievements Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
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
                modifier = Modifier.clickable {
                    navController.navigate(
                        "allBadges"
                    ) },
                fontFamily = alifbaFont,

            )
        }

        if (earnedBadges.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Complete activities to earn badges!",
                    fontSize = 16.sp,
                    fontFamily = alifbaFont,
                    color = navyBlue,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(140.dp) // Reduced height for 1 row of 3 badges
            ) {
                items(earnedBadges.take(3)) { badge ->
                    BadgeCard(badge)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}


@Composable
fun UserCard(cardData: UserCardData, elevation: Dp) {
    val alifbaFont = FontFamily(Font(R.font.more_sugar_regular, FontWeight.SemiBold))

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(elevation),
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp), // Set a fixed height for the card
        colors = CardDefaults.cardColors(white)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Align content centrally
        ) {
            Image(
                painter = painterResource(id = cardData.imageRes),
                contentDescription = cardData.title,
                modifier = Modifier
                    .height(60.dp) // Adjust image height
                    .fillMaxWidth(),
                contentScale = ContentScale.Inside
            )
            Spacer(modifier = Modifier.height(4.dp)) // Smaller spacer
            Text(
                text = cardData.title,
                fontSize = 12.sp,
                fontFamily = alifbaFont,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = cardData.description,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = alifbaFont,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeCard(badge: Badge) {
    val alifbaFont = FontFamily(Font(R.font.more_sugar_regular, FontWeight.SemiBold))
    var showBottomSheet by remember { mutableStateOf(false) }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            containerColor = white,
            sheetState = rememberModalBottomSheetState()
        ) {
            BadgeDetailsBottomSheet(
                badge = badge,
                onDismiss = { showBottomSheet = false }
            )
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Ensures all cards have a square aspect ratio
            .padding(2.dp) // Adds internal padding for spacing
            .clickable { showBottomSheet = true },
        colors = CardDefaults.cardColors(white)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Center content within the card
        ) {
            // Badge Image
            Image(
                painter = rememberAsyncImagePainter(badge.imageUrl),
                contentDescription = badge.title,
                modifier = Modifier
                    .size(60.dp) // Adjust size of the badge image
                    .padding(bottom = 2.dp), // Adds space below the image
                contentScale = ContentScale.Fit
            )

            // Badge Name
            Text(
                text = badge.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = alifbaFont,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp)
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

@Composable
fun BadgeDetailsBottomSheet(
    badge: Badge,
    onDismiss: () -> Unit
) {
    val alifbaFont = FontFamily(
        Font(R.font.more_sugar_regular, FontWeight.SemiBold)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Badge Title
        Text(
            text = badge.name,
            fontFamily = alifbaFont,
            color = navyBlue,
            fontSize = 23.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
        )

        // Badge Image
        Image(
            painter = rememberAsyncImagePainter(badge.imageUrl),
            contentDescription = badge.title,
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Badge Description
        Text(
            text = badge.description,
            fontFamily = alifbaFont,
            color = navyBlue,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Optional: Add a close button
        CommonButton(
            buttonText = "Close",
            mainColor = lightNavyBlue,
            shadowColor = navyBlue,
            textColor = white,
            onClick = onDismiss
        )
    }
}

@Composable
fun AllBadgesScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel
) {
    val earnedBadges by profileViewModel.earnedBadges.collectAsState()
    val alifbaFont = FontFamily(Font(R.font.more_sugar_regular, FontWeight.SemiBold))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(white)
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.goback),
                contentDescription = "Back",
                modifier = Modifier
                    .clickable { navController.popBackStack() }
                    .size(36.dp),
            )
            Text(
                text = "My Achievements",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = navyBlue,
                fontFamily = alifbaFont
            )
            // Empty box for symmetry
            Box(modifier = Modifier.size(24.dp))
        }

        // Stats Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(lightNavyBlue.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${earnedBadges.size}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = navyBlue,
                        fontFamily = alifbaFont
                    )
                    Text(
                        text = "Earned",
                        fontSize = 14.sp,
                        color = navyBlue,
                        fontFamily = alifbaFont
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        //text = "${12 - earnedBadges.size}", // Assuming total badges is 12
                        text = "many more",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = navyBlue,
                        fontFamily = alifbaFont
                    )
                    Text(
                        text = "Remaining",
                        fontSize = 14.sp,
                        color = navyBlue,
                        fontFamily = alifbaFont
                    )
                }
            }
        }

        // All Badges Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3), // 3 items per row
            contentPadding = PaddingValues(4.dp), // Padding around the entire grid
            horizontalArrangement = Arrangement.spacedBy(4.dp), // Space between columns
            verticalArrangement = Arrangement.spacedBy(4.dp), // Space between rows
            modifier = Modifier.fillMaxSize()
        ) {
            items(earnedBadges) { badge ->
                BadgeCard(badge)
            }
        }

    }
}
