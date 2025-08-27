package com.alifba.alifba.presenation.home.layout.profile

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.zIndex
import kotlin.math.max
import kotlin.math.min
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.alifba.alifba.R
import com.alifba.alifba.presenation.home.layout.ProfileViewModel
import com.alifba.alifba.presenation.home.layout.ParentGate
import com.alifba.alifba.presenation.login.UserProfile
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager
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
    // Show ParentGate as full screen overlay if needed
    var showParentGate by remember { mutableStateOf(false) }
    
    if (showParentGate) {
        ParentGate(
            onVerified = {
                showParentGate = false
                navController.navigate("settings")
            },
            onDismiss = {
                showParentGate = false
            }
        )
    } else {
        // Call the actual profile screen implementation
        ProfileScreenWithoutTopBar(
            navController = navController,
            profileViewModel = profileViewModel,
            onSettingsClick = { showParentGate = true }
        )
    }
}

@Composable
fun ProfileScreenWithoutTopBar(
    navController: NavController,
    profileViewModel: ProfileViewModel,
    onSettingsClick: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "ProfileScreen")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "ProfileScreen")
        }
    }

    val userProfile by profileViewModel.userProfileState.collectAsState()
    val earnedBadges by profileViewModel.earnedBadges.collectAsState()
    val alifbaFont = FontFamily(Font(R.font.vag_round, FontWeight.Bold))

    // Window size class for responsive layout
    val windowSize = rememberWindowSizeClass()
    val isTablet = windowSize.widthSizeClass >= WindowWidthSizeClass.Medium
    
    // Scroll state for collapsing header effect
    val scrollState = rememberLazyListState()
    
    // Animation parameters matching Swift code exactly
    val baseHeaderHeight = 160.dp
    val collapsedHeaderHeight = 80.dp
    val collapseRange = baseHeaderHeight - collapsedHeaderHeight
    
    // Calculate scroll-based animations
    val scrollOffset = remember {
        derivedStateOf {
            scrollState.firstVisibleItemScrollOffset.toFloat()
        }
    }
    
    val collapseProgress = remember {
        derivedStateOf {
            min(max(scrollOffset.value / collapseRange.value, 0f), 1f)
        }
    }
    
    val headerHeight by remember {
        derivedStateOf {
            baseHeaderHeight - (collapseProgress.value * collapseRange.value).dp
        }
    }
    
    val avatarSize by remember {
        derivedStateOf {
            100.dp - (collapseProgress.value * (100f - 60f)).dp
        }
    }
    
    val nameTextSize by remember {
        derivedStateOf {
            (24f - (collapseProgress.value * (24f - 18f))).sp
        }
    }
    
    val headerAlpha by remember {
        derivedStateOf {
            1f - collapseProgress.value
        }
    }
    
    val collapsedHeaderAlpha by remember {
        derivedStateOf {
            collapseProgress.value
        }
    }

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

    Box(modifier = Modifier.fillMaxSize().background(white)) {
        // Main scrollable content
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize()
        ) {
            // Spacer item to account for header height
            item {
                Spacer(modifier = Modifier.height(headerHeight))
            }
            
            // Progress Overview Section
            item {
                Text(
                    text = "Progress Overview",
                    fontSize = if (isTablet) 22.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = alifbaFont,
                    modifier = Modifier.padding(horizontal = if (isTablet) 24.dp else 16.dp, vertical = 8.dp)
                )
            }

            // User Stats Cards
            item {
                val userCards = listOf(
                    UserCardData(
                        R.drawable.lessonbook,
                        "Lessons \nCompleted",
                        "${userProfile?.lessonsCompleted?.size ?: 0}"
                    ),
                    UserCardData(
                        R.drawable.storybook,
                        "Stories \nCompleted",
                        "${userProfile?.storiesCompleted?.size ?: 0}"
                    ),
                    UserCardData(
                        R.drawable.alphab,
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

                Column(
                    modifier = Modifier.padding(horizontal = if (isTablet) 24.dp else 16.dp)
                ) {
                    // Layout for tablet vs phone
                    if (isTablet) {
                        // Tablet layout - 4 cards per row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            userCards.take(4).forEach { card ->
                                UserCard(
                                    card,
                                    elevation = 2.dp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(max = 160.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Second row - 2 cards + spacers for balance
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            userCards.subList(4, 6).forEach { card ->
                                UserCard(
                                    card,
                                    elevation = 2.dp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(max = 160.dp)
                                )
                            }
                            // Add empty space for balance
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    } else {
                        // Phone layout - 3 cards per row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            userCards.take(3).forEach { card ->
                                UserCard(
                                    card,
                                    elevation = 2.dp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Second row - 3 cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            userCards.subList(3, 6).forEach { card ->
                                UserCard(
                                    card,
                                    elevation = 2.dp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            
            // Spacer
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Achievements Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isTablet) 24.dp else 16.dp, vertical = 8.dp)
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
                            fontWeight = FontWeight.Bold,
                            fontFamily = alifbaFont,
                        )
                        Text(
                            text = "View All",
                            fontSize = if (isTablet) 18.sp else 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = navyBlue,
                            modifier = Modifier.clickable {
                                SoundEffectManager.playClickSound()
                                navController.navigate("allBadges")
                            },
                            fontFamily = alifbaFont,
                        )
                    }

                    // Badges display
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
                        // Adaptive layout based on badge count
                        val badgeCount = earnedBadges.size
                        if (isTablet) {
                            // For tablets
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = if (badgeCount < 5) Arrangement.Center else Arrangement.spacedBy(8.dp)
                            ) {
                                // Add spacing to center when there are few badges
                                if (badgeCount < 5) {
                                    Spacer(modifier = Modifier.weight(0.5f))
                                }

                                // Display badges
                                earnedBadges.take(5).forEach { badge ->
                                    Box(
                                        modifier = if (badgeCount < 5) Modifier.weight(1f) else Modifier.weight(1f)
                                    ) {
                                        BadgeCard(
                                            badge = badge,
                                            fixedWidth = if (badgeCount <= 2) 120.dp else null
                                        )
                                    }
                                }

                                // Add spacing to center when there are few badges
                                if (badgeCount < 5) {
                                    Spacer(modifier = Modifier.weight(0.5f))
                                }
                            }
                        } else {
                            // For phones
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = if (badgeCount < 3) Arrangement.Center else Arrangement.spacedBy(4.dp)
                            ) {
                                // Add spacing to center when there are few badges
                                if (badgeCount < 3) {
                                    Spacer(modifier = Modifier.weight(0.5f))
                                }

                                // Display badges
                                earnedBadges.take(3).forEach { badge ->
                                    Box(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        BadgeCard(
                                            badge = badge,
                                            fixedWidth = if (badgeCount <= 2) 100.dp else null
                                        )
                                    }
                                }

                                // Add spacing to center when there are few badges
                                if (badgeCount < 3) {
                                    Spacer(modifier = Modifier.weight(0.5f))
                                }
                            }
                        }
                    }
                }
            }
            
            // Extra space at bottom for navigation
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
        
        // Floating header with expand/collapse animation
        Column(
            modifier = Modifier.fillMaxWidth().zIndex(1f)
        ) {
            // Minimal top spacing for status bar
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
                    .background(white)
                    .shadow(
                        elevation = (collapsedHeaderAlpha * 4).dp,
                        spotColor = navyBlue.copy(alpha = 0.15f)
                    )
            ) {
                // Expanded header (fades out on scroll)
                ExpandedHeader(
                    userProfile = userProfile,
                    avatarSize = avatarSize,
                    nameTextSize = nameTextSize,
                    alpha = headerAlpha,
                    onAvatarClick = { navController.navigate("changeAvatar") },
                    onSettingsClick = { 
                        SoundEffectManager.playClickSound()
                        onSettingsClick()
                    },
                    alifbaFont = alifbaFont
                )
                
                // Collapsed header (fades in on scroll)
                CollapsedHeader(
                    userProfile = userProfile,
                    alpha = collapsedHeaderAlpha,
                    onAvatarClick = { navController.navigate("changeAvatar") },
                    onSettingsClick = { 
                        SoundEffectManager.playClickSound()
                        onSettingsClick()
                    },
                    alifbaFont = alifbaFont
                )
            }
        }

    }
}

// Data class for user cards
data class UserCardData(
    val imageRes: Int,
    val title: String,
    val description: String
)

// Window size class for responsive layout
@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val configuration = LocalConfiguration.current
    return WindowSizeClass(
        widthSizeClass = when {
            configuration.screenWidthDp < 600 -> WindowWidthSizeClass.Compact
            configuration.screenWidthDp < 840 -> WindowWidthSizeClass.Medium
            else -> WindowWidthSizeClass.Expanded
        },
        heightSizeClass = when {
            configuration.screenHeightDp < 480 -> WindowHeightSizeClass.Compact
            configuration.screenHeightDp < 900 -> WindowHeightSizeClass.Medium
            else -> WindowHeightSizeClass.Expanded
        }
    )
}

data class WindowSizeClass(
    val widthSizeClass: WindowWidthSizeClass,
    val heightSizeClass: WindowHeightSizeClass
)

enum class WindowWidthSizeClass { Compact, Medium, Expanded }
enum class WindowHeightSizeClass { Compact, Medium, Expanded }

// UserCard composable
@Composable
fun UserCard(
    cardData: UserCardData,
    elevation: Dp,
    modifier: Modifier = Modifier
) {
    val alifbaFont = FontFamily(Font(R.font.vag_round, FontWeight.Bold))
    
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(elevation),
        modifier = modifier
            .height(150.dp),
        colors = CardDefaults.cardColors(white)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card Image
            Image(
                painter = painterResource(id = cardData.imageRes),
                contentDescription = cardData.title,
                modifier = Modifier
                    .height(50.dp)
                    .padding(bottom = 4.dp),
                contentScale = ContentScale.Fit
            )

            // Card Title
            Text(
                text = cardData.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = alifbaFont,
                textAlign = TextAlign.Center,
                color = navyBlue,
                maxLines = 2
            )

            // Card Description (Stats Value)
            Text(
                text = cardData.description,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = alifbaFont,
                textAlign = TextAlign.Center,
                color = androidx.compose.ui.graphics.Color.Blue,
                maxLines = 1
            )
        }
    }
}

// BadgeCard composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeCard(
    badge: com.alifba.alifba.data.models.Badge,
    fixedWidth: Dp? = null
) {
    val alifbaFont = FontFamily(Font(R.font.vag_round, FontWeight.Bold))
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

    // Use the modifier with either fixed width or fillMaxWidth
    val cardModifier = if (fixedWidth != null) {
        Modifier
            .width(fixedWidth)
            .aspectRatio(1f)
            .padding(2.dp)
            .clickable { 
                SoundEffectManager.playClickSound()
                showBottomSheet = true 
            }
    } else {
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(2.dp)
            .clickable { 
                SoundEffectManager.playClickSound()
                showBottomSheet = true 
            }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = cardModifier,
        colors = CardDefaults.cardColors(white)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Badge Image
            Image(
                painter = rememberAsyncImagePainter(badge.imageUrl),
                contentDescription = badge.title,
                modifier = Modifier
                    .size(80.dp)
                    .weight(0.7f)
                    .padding(bottom = 4.dp),
                contentScale = ContentScale.Fit
            )

            // Badge Name
            Text(
                text = badge.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = alifbaFont,
                textAlign = TextAlign.Center,
                color = navyBlue,
                modifier = Modifier.weight(0.3f)
            )
        }
    }
}

@Composable
fun BadgeDetailsBottomSheet(
    badge: com.alifba.alifba.data.models.Badge,
    onDismiss: () -> Unit
) {
    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Bold)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Badge Image
        Image(
            painter = rememberAsyncImagePainter(badge.imageUrl),
            contentDescription = badge.title,
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 16.dp),
            contentScale = ContentScale.Fit
        )

        // Badge Title
        Text(
            text = badge.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = alifbaFont,
            color = navyBlue,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Badge Name
        Text(
            text = badge.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = alifbaFont,
            color = navyBlue,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Badge Description
        Text(
            text = badge.description,
            fontSize = 16.sp,
            fontFamily = alifbaFont,
            color = navyBlue,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Optional: Add a close button
        com.alifba.alifba.ui_components.widgets.buttons.CommonButton(
            buttonText = "Close",
            mainColor = lightNavyBlue,
            shadowColor = navyBlue,
            textColor = white,
            onClick = {
                SoundEffectManager.playClickSound()
                onDismiss()
            }
        )
    }
}

// Avatar helper function
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
        else -> R.drawable.avatar9 // Default fallback
    }
}

@Composable
fun AllBadgesScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel
) {
    val earnedBadges by profileViewModel.earnedBadges.collectAsState()
    val alifbaFont = FontFamily(Font(R.font.vag_round, FontWeight.Bold))
    val windowSize = rememberWindowSizeClass()
    val isTablet = windowSize.widthSizeClass >= WindowWidthSizeClass.Medium

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
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            Image(
                painter = painterResource(id = R.drawable.backarrow),
                contentDescription = "Back",
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        SoundEffectManager.playClickSound()
                        navController.popBackStack()
                    }
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Title
            Text(
                text = "All Badges",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = alifbaFont,
                color = navyBlue
            )
        }
        
        if (earnedBadges.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Empty state icon
                    Image(
                        painter = painterResource(id = R.drawable.quizzesnew),
                        contentDescription = "No badges",
                        modifier = Modifier
                            .size(120.dp)
                            .padding(bottom = 24.dp),
                        contentScale = ContentScale.Fit
                    )
                    
                    Text(
                        text = "No Badges Yet",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = alifbaFont,
                        color = navyBlue,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Complete lessons and activities to earn your first badge!",
                        fontSize = 16.sp,
                        fontFamily = alifbaFont,
                        color = navyBlue.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            // Badges grid
            val columns = if (isTablet) 4 else 3
            val rows = (earnedBadges.size + columns - 1) / columns
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(rows) { rowIndex ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        repeat(columns) { columnIndex ->
                            val badgeIndex = rowIndex * columns + columnIndex
                            if (badgeIndex < earnedBadges.size) {
                                Box(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    BadgeCard(
                                        badge = earnedBadges[badgeIndex],
                                        fixedWidth = null
                                    )
                                }
                            } else {
                                // Empty space for grid alignment
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                
                // Bottom spacing for navigation
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
fun ExpandedHeader(
    userProfile: UserProfile?,
    avatarSize: Dp,
    nameTextSize: androidx.compose.ui.unit.TextUnit,
    alpha: Float,
    onAvatarClick: () -> Unit,
    onSettingsClick: () -> Unit,
    alifbaFont: FontFamily
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .graphicsLayer(alpha = alpha),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with edit button
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .clickable { onAvatarClick() }
            ) {
                // Avatar Image
                Image(
                    painter = if (userProfile != null) {
                        painterResource(id = getAvatarHeadShots(userProfile.avatar))
                    } else {
                        painterResource(id = R.drawable.avatar9)
                    },
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                        .background(lightNavyBlue),
                    contentScale = ContentScale.Crop
                )

                // Pencil Icon - scales with avatar
                Image(
                    painter = painterResource(id = R.drawable.pencil),
                    contentDescription = "Edit Avatar",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(avatarSize * 0.24f)
                        .background(white, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = userProfile?.childName ?: "Loading...",
                    fontSize = nameTextSize,
                    fontWeight = FontWeight.Bold,
                    color = navyBlue,
                    fontFamily = alifbaFont
                )
                Text(
                    text = "Age: ${userProfile?.age ?: "--"}",
                    fontSize = (nameTextSize.value - 4f).sp,
                    color = navyBlue,
                    fontFamily = alifbaFont
                )
            }
        }
        
        // Settings button aligned with profile picture
        Image(
            painter = painterResource(id = R.drawable.setting),
            contentDescription = "Settings",
            modifier = Modifier
                .size(36.dp)
                .clickable { onSettingsClick() }
        )
    }
}

@Composable
fun CollapsedHeader(
    userProfile: UserProfile?,
    alpha: Float,
    onAvatarClick: () -> Unit,
    onSettingsClick: () -> Unit,
    alifbaFont: FontFamily
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .graphicsLayer(alpha = alpha),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Small avatar and name centered
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onAvatarClick() }
            ) {
                if (userProfile != null) {
                    Image(
                        painter = painterResource(id = getAvatarHeadShots(userProfile.avatar)),
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(lightNavyBlue),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.avatar9),
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(lightNavyBlue),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = userProfile?.childName ?: "Loading...",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = navyBlue,
                fontFamily = alifbaFont
            )
        }

        // Settings button on the right
        Image(
            painter = painterResource(id = R.drawable.setting),
            contentDescription = "Settings",
            modifier = Modifier
                .size(36.dp)
                .clickable { onSettingsClick() }
        )
    }
}