package com.alifba.alifba.presenation.home.layout.profile

import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.alifba.alifba.R
import com.alifba.alifba.data.models.Badge
import com.alifba.alifba.presenation.home.layout.ProfileViewModel
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel
) {
    LaunchedEffect(Unit) {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "ProfileScreen")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "ProfileScreen")
        }
    }

    val userProfile by profileViewModel.userProfileState.collectAsState()
    val earnedBadges by profileViewModel.earnedBadges.collectAsState()
    val alifbaFont = FontFamily(Font(R.font.more_sugar_regular, FontWeight.SemiBold))

    // Scroll state for coordinator-like behavior
    val scrollState = rememberScrollState()

    // Calculate header collapse progress (0 to 1)
    val headerHeightPx = with(LocalDensity.current) { 160.dp.toPx() }
    val headerCollapsedHeightPx = with(LocalDensity.current) { 80.dp.toPx() }
    val collapseRange = headerHeightPx - headerCollapsedHeightPx
    val collapseProgress = (scrollState.value / collapseRange).coerceIn(0f, 1f)

    // Header height animation
    val headerHeight by animateDpAsState(
        targetValue = lerp(160.dp, 80.dp, collapseProgress),
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    // Avatar size animation
    val avatarSize by animateDpAsState(
        targetValue = lerp(100.dp, 60.dp, collapseProgress),
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    // Text size animation
    val nameTextSize by animateFloatAsState(
        targetValue = lerp(24f, 18f, collapseProgress),
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    // Alpha values for smooth transition - FIXED: Ensure complete fade transition
    val headerAlpha = (1f - collapseProgress).coerceIn(0f, 1f)
    val collapsedHeaderAlpha = collapseProgress.coerceIn(0f, 1f)

    // Window size class for responsive layout
    val windowSize = rememberWindowSizeClass()
    val isTablet = windowSize.widthSizeClass >= WindowWidthSizeClass.Medium

    LaunchedEffect(Unit) {
        try {
            profileViewModel.startProfileListener()
            delay(100)
        } catch (e: Exception) {
            Log.e("ProfileScreen", "Error starting profile listener: ${e.message}", e)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            profileViewModel.stopProfileListener()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(white)
    ) {
        // Main scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Spacer for header area
            Spacer(modifier = Modifier.height(headerHeight))

            // Rest of the content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (isTablet) 24.dp else 16.dp)
            ) {
                // Progress Overview Section
                Text(
                    text = "Progress Overview",
                    fontSize = if (isTablet) 22.sp else 20.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = alifbaFont,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // User Cards
                val userCards = listOf(
                    UserCardData(
                        R.drawable.ladder,
                        "Levels \nCompleted",
                        "${userProfile?.levelsCompleted?.size ?: 0}"
                    ),
                    UserCardData(
                        R.drawable.book,
                        "Lessons \nCompleted",
                        "${userProfile?.lessonsCompleted?.size ?: 0}"
                    ),
                    UserCardData(
                        R.drawable.story,
                        "Stories \nCompleted",
                        "${userProfile?.storiesCompleted?.size ?: 0}"
                    ),
                    UserCardData(
                        R.drawable.alphabeticonprofile,
                        "Activities \nCompleted",
                        "${userProfile?.activitiesCompleted?.size ?: 0}"
                    ),
                    UserCardData(
                        R.drawable.quizzesnew,
                        "Quizzes \nAttended",
                        "${userProfile?.quizzesAttended ?: 0}"
                    ),
                    UserCardData(
                        R.drawable.streaknew,
                        "Day \nStreak",
                        "${userProfile?.dayStreak ?: 0}"
                    ),
                    UserCardData(
                        R.drawable.xpnew,
                        "Total XP",
                        "${userProfile?.xp ?: 0}"
                    )
                )

                // FIXED: Layout for tablet vs phone
                if (isTablet) {
                    // Tablet layout - 4 cards per row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        userCards.take(4).forEach { card ->
                            UserCard(
                                card,
                                elevation = 2.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp)
                                    .heightIn(max = 160.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Second row - 3 cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        userCards.subList(4, 7).forEach { card ->
                            UserCard(
                                card,
                                elevation = 2.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp)
                                    .heightIn(max = 160.dp)
                            )
                        }
                        // Add an empty space for balance
                        Spacer(modifier = Modifier.weight(1f))
                    }
                } else {
                    // Phone layout - 3 cards per row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        userCards.take(3).forEach { card ->
                            UserCard(
                                card,
                                elevation = 2.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Second row - 3 cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        userCards.subList(3, 6).forEach { card ->
                            UserCard(
                                card,
                                elevation = 2.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Last row - centered card
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        UserCard(
                            userCards.last(),
                            elevation = 2.dp,
                            modifier = Modifier.width(150.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // FIXED: Achievements Section with adequate spacing
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Achievements",
                            fontSize = if (isTablet) 22.sp else 20.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = alifbaFont,
                        )
                        Text(
                            text = "View All",
                            fontSize = if (isTablet) 18.sp else 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = navyBlue,
                            modifier = Modifier.clickable {
                                navController.navigate("allBadges")
                            },
                            fontFamily = alifbaFont,
                        )
                    }

                    if (earnedBadges.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Complete activities to earn badges!",
                                fontSize = if (isTablet) 18.sp else 16.sp,
                                fontFamily = alifbaFont,
                                color = navyBlue,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // FIXED: Adaptive badge display based on device
                        if (isTablet) {
                            // Grid layout for tablet - show more badges in a grid
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                earnedBadges.take(5).forEach { badge ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 8.dp)
                                    ) {
                                        BadgeCard(badge)
                                    }
                                }
                            }
                        } else {
                            // Row layout for phones
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                earnedBadges.take(3).forEach { badge ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        BadgeCard(badge)
                                    }
                                }
                            }
                        }
                    }
                }

                // Extra space at bottom - FIXED: More space for content safety
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // FIXED: Floating header that collapses - improved shadow and transition
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .background(white)
                .drawWithCache {
                    onDrawBehind {
                        // Add shadow as the header collapses - smoother shadow
                        if (collapseProgress > 0f) {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.LightGray.copy(alpha = 0.15f * collapseProgress),
                                        Color.Transparent
                                    ),
                                    startY = size.height - 4.dp.toPx(),
                                    endY = size.height + 10.dp.toPx()
                                ),
                                size = Size(size.width, 10.dp.toPx())
                            )
                        }
                    }
                }
                .zIndex(10f) // Ensure header is above other content
        ) {
            // Expanded header content (fades out during scroll)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(headerAlpha)
                    .padding(16.dp)
            ) {
                Column {
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
                        Box(modifier = Modifier.size(24.dp))
                    }

                    // Avatar and info row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(avatarSize) // Use animated size
                                .clickable {
                                    navController.navigate("changeAvatar")
                                }
                        ) {
                            // Avatar Image
                            Image(
                                painter = if (userProfile != null) {
                                    painterResource(id = getAvatarHeadShots(userProfile!!.avatar))
                                } else {
                                    painterResource(id = R.drawable.avatar9)
                                },
                                contentDescription = "User Avatar",
                                modifier = Modifier
                                    .size(avatarSize) // Use animated size
                                    .clip(CircleShape)
                                    .background(lightNavyBlue),
                                contentScale = ContentScale.Crop
                            )

                            // Pencil Icon - scale with avatar
                            Image(
                                painter = painterResource(id = R.drawable.pencil),
                                contentDescription = "Edit Avatar",
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(avatarSize.times(0.24f)) // Scale with avatar
                                    .background(white, CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = userProfile?.childName ?: "Loading...",
                                fontSize = with(LocalDensity.current) { nameTextSize.sp },
                                fontWeight = FontWeight.Bold,
                                color = navyBlue,
                                fontFamily = alifbaFont
                            )
                            Text(
                                text = "Age: ${userProfile?.age ?: "--"}",
                                fontSize = with(LocalDensity.current) { (nameTextSize - 4f).sp },
                                color = navyBlue,
                                fontFamily = alifbaFont
                            )
                        }
                    }
                }
            }

            // FIXED: Collapsed header content with better transition
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(collapsedHeaderAlpha)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.goback),
                        contentDescription = "Back",
                        modifier = Modifier
                            .clickable { navController.popBackStack() }
                            .size(36.dp),
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Small avatar
                    Image(
                        painter = if (userProfile != null) {
                            painterResource(id = getAvatarHeadShots(userProfile!!.avatar))
                        } else {
                            painterResource(id = R.drawable.avatar9)
                        },
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(lightNavyBlue),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = userProfile?.childName ?: "Loading...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = navyBlue,
                        fontFamily = alifbaFont
                    )
                }
            }
        }
    }
}

// Helper function to detect window size class for responsiveness
@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    return WindowSizeClass(
        widthSizeClass = when {
            screenWidth < 600.dp -> WindowWidthSizeClass.Compact
            screenWidth < 840.dp -> WindowWidthSizeClass.Medium
            else -> WindowWidthSizeClass.Expanded
        },
        heightSizeClass = when {
            screenHeight < 480.dp -> WindowHeightSizeClass.Compact
            screenHeight < 900.dp -> WindowHeightSizeClass.Medium
            else -> WindowHeightSizeClass.Expanded
        }
    )
}

// Window size classes similar to Material Design 3
enum class WindowWidthSizeClass { Compact, Medium, Expanded }
enum class WindowHeightSizeClass { Compact, Medium, Expanded }

data class WindowSizeClass(
    val widthSizeClass: WindowWidthSizeClass,
    val heightSizeClass: WindowHeightSizeClass
)

// Helper function to interpolate between two values based on progress
private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}

private fun lerp(start: Dp, stop: Dp, fraction: Float): Dp {
    return start + (stop - start) * fraction
}@Composable
fun UserCard(
    cardData: UserCardData,
    elevation: Dp,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    val alifbaFont = FontFamily(Font(R.font.more_sugar_regular, FontWeight.SemiBold))

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(elevation),
        modifier = modifier.height(150.dp),
        colors = CardDefaults.cardColors(white)
    ) {
        // Rest of your card implementation remains the same
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = cardData.imageRes),
                contentDescription = cardData.title,
                modifier = Modifier
                    .height(60.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Inside
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = cardData.title,
                fontSize = 12.sp,
                fontFamily = alifbaFont,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = cardData.description,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = alifbaFont,
                textAlign = TextAlign.Center,
                color = lightNavyBlue,
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
            .aspectRatio(1f)
            .padding(2.dp)
            .clickable { showBottomSheet = true },
        colors = CardDefaults.cardColors(white)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Badge Image - Increased size to fill more space
            Image(
                painter = rememberAsyncImagePainter(badge.imageUrl),
                contentDescription = badge.title,
                modifier = Modifier
                    .size(80.dp)
                    .weight(0.7f)
                    .padding(bottom = 4.dp),
                contentScale = ContentScale.Fit
            )

            // Badge Name - Better spacing and weight
            Text(
                text = badge.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = alifbaFont,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(0.3f)
                    .padding(horizontal = 4.dp)
            )
        }
    }
}

data class UserCardData(
    val imageRes: Int,
    val title: String,
    val description: String
)

fun getAvatarHeadShots(avatarName: String): Int {
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
//            contentPadding = PaddingValues(4.dp), // Padding around the entire grid
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
