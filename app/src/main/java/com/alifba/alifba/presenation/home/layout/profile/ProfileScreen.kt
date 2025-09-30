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
import androidx.compose.ui.graphics.Color
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
import com.alifba.alifba.presenation.login.ChildProfile
import com.alifba.alifba.ui_components.theme.black
import com.alifba.alifba.ui_components.theme.lightCandyGreen
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.lightPink
import com.alifba.alifba.ui_components.theme.lightPurple
import com.alifba.alifba.ui_components.theme.lightSkyBlue
import com.alifba.alifba.ui_components.theme.lightYellow
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
    profileViewModel: ProfileViewModel,
    onSettingsClick: () -> Unit = {},
    onParentGateShow: (Boolean) -> Unit = {}
) {
    ProfileScreenWithoutTopBar(
        navController = navController,
        profileViewModel = profileViewModel,
        onSettingsClick = {
            navController.navigate("parentGate")
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
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

    val currentChildProfile by profileViewModel.currentChildProfile.collectAsState()
    val parentAccount by profileViewModel.parentAccountState.collectAsState()
    val earnedBadges by profileViewModel.earnedBadges.collectAsState()
    val alifbaFont = FontFamily(Font(R.font.vag_round, FontWeight.Bold))
    val alifbaFontBold = FontFamily(Font(R.font.vag_round_boldd, FontWeight.Bold))

    // Window size class for responsive layout
    val windowSize = rememberWindowSizeClass()
    val isTablet = windowSize.widthSizeClass >= WindowWidthSizeClass.Medium
    
    // Scroll state for collapsing header effect
    val scrollState = rememberLazyListState()
    
    // Animation parameters matching Swift code exactly - enhanced for tablets
    val baseHeaderHeight = if (isTablet) 200.dp else 160.dp
    val collapsedHeaderHeight = if (isTablet) 100.dp else 80.dp
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
            val baseSize = if (isTablet) 120.dp else 100.dp
            val collapsedSize = if (isTablet) 70.dp else 60.dp
            baseSize - (collapseProgress.value * (baseSize.value - collapsedSize.value)).dp
        }
    }
    
    val nameTextSize by remember {
        derivedStateOf {
            val baseTextSize = if (isTablet) 28f else 24f
            val collapsedTextSize = if (isTablet) 22f else 18f
            (baseTextSize - (collapseProgress.value * (baseTextSize - collapsedTextSize))).sp
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
                val currentDeviceType = getDeviceType()
                Text(
                    text = "Progress Overview - $currentDeviceType",
                    fontSize = if (isTablet) 22.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = alifbaFontBold,
                    modifier = Modifier.padding(horizontal = if (isTablet) 24.dp else 16.dp, vertical = 8.dp)
                )
            }

            // User Stats Cards
            item {
                val userCards = listOf(
                    UserCardData(
                        R.drawable.lessonsattended,
                        "Lessons Completed",
                        "${(currentChildProfile?.lessonsCompleted?.size ?: 0) + (currentChildProfile?.storiesCompleted?.size ?: 0)}",
                        lightCandyGreen// Green
                    ),
                    UserCardData(
                        R.drawable.quizzesattended,
                        "Quizzes Attended",
                        "${currentChildProfile?.quizzesAttended ?: 0}",
                        lightPink// Blue
                    ),
                    UserCardData(
                        R.drawable.streakscore,
                        "Day Streak",
                        "${currentChildProfile?.dayStreak ?: 0}",
                        lightSkyBlue // Orange
                    ),
                    UserCardData(
                        R.drawable.xpgained,
                        "Total XP",
                        "${currentChildProfile?.xp ?: 0}",
                        lightYellow // Purple
                    )
                )

                val deviceType = getDeviceType()

                Column(
                    modifier = Modifier.padding(horizontal = when (deviceType) {
                        DeviceType.LargeTablet -> 40.dp
                        DeviceType.Tablet -> 32.dp
                        DeviceType.SmallTablet -> 24.dp
                        DeviceType.Phone -> 16.dp
                    })
                ) {
                    // Responsive layout based on device type
                    when (deviceType) {
                        DeviceType.LargeTablet -> {
                            // Large tablet - 4 cards in one row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                userCards.forEach { card ->
                                    UserCard(
                                        card,
                                        elevation = 8.dp,
                                        deviceType = deviceType,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        DeviceType.Tablet -> {
                            // Regular tablet - 2x2 grid with better spacing
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                userCards.take(2).forEach { card ->
                                    UserCard(
                                        card,
                                        elevation = 6.dp,
                                        deviceType = deviceType,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                userCards.subList(2, 4).forEach { card ->
                                    UserCard(
                                        card,
                                        elevation = 6.dp,
                                        deviceType = deviceType,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        DeviceType.SmallTablet -> {
                            // Small tablet - 2x2 grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                userCards.take(2).forEach { card ->
                                    UserCard(
                                        card,
                                        elevation = 6.dp,
                                        deviceType = deviceType,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                userCards.subList(2, 4).forEach { card ->
                                    UserCard(
                                        card,
                                        elevation = 6.dp,
                                        deviceType = deviceType,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        DeviceType.Phone -> {
                            // Phone - 2x2 grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                userCards.take(2).forEach { card ->
                                    UserCard(
                                        card,
                                        elevation = 6.dp,
                                        deviceType = deviceType,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                userCards.subList(2, 4).forEach { card ->
                                    UserCard(
                                        card,
                                        elevation = 6.dp,
                                        deviceType = deviceType,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
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
                            fontFamily = alifbaFontBold,
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
                        // Show only first 3 badges in a single row with bottom sheet
                        val badgesToShow = earnedBadges.take(3)
                        var selectedBadge by remember { mutableStateOf<com.alifba.alifba.data.models.Badge?>(null) }

                        // Show bottom sheet when badge is selected
                        selectedBadge?.let { badge ->
                            ModalBottomSheet(
                                onDismissRequest = { selectedBadge = null },
                                containerColor = white,
                                sheetState = rememberModalBottomSheetState()
                            ) {
                                BadgeDetailsBottomSheet(
                                    badge = badge,
                                    onDismiss = { selectedBadge = null }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            repeat(3) { columnIndex ->
                                if (columnIndex < badgesToShow.size) {
                                    // Badge image
                                    Image(
                                        painter = rememberAsyncImagePainter(badgesToShow[columnIndex].imageUrl),
                                        contentDescription = badgesToShow[columnIndex].title,
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clickable {
                                                SoundEffectManager.playClickSound()
                                                selectedBadge = badgesToShow[columnIndex]
                                            },
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    // Empty space for grid alignment
                                    Spacer(modifier = Modifier.weight(1f))
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
                    childProfile = currentChildProfile,
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
                    childProfile = currentChildProfile,
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
    val description: String,
    val backgroundColor: androidx.compose.ui.graphics.Color
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

@Composable
fun getDeviceType(): DeviceType {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp
    val heightDp = configuration.screenHeightDp
    
    Log.d("ProfileScreen", "Device dimensions: ${widthDp}dp x ${heightDp}dp")
    
    val deviceType = when {
        widthDp >= 1000 -> DeviceType.LargeTablet  // Lowered threshold for large tablets
        widthDp >= 750 -> DeviceType.Tablet        // Lowered threshold for Pixel Tablet
        widthDp >= 600 -> DeviceType.SmallTablet   
        else -> DeviceType.Phone
    }
    
    Log.d("ProfileScreen", "Detected device type: $deviceType")
    return deviceType
}

enum class DeviceType { Phone, SmallTablet, Tablet, LargeTablet }

// UserCard composable
@Composable
fun UserCard(
    cardData: UserCardData,
    elevation: Dp,
    deviceType: DeviceType,
    modifier: Modifier = Modifier
) {
    val alifbaFont = FontFamily(Font(R.font.vag_round, FontWeight.Bold))
    val alifbaFontBold = FontFamily(Font(R.font.vag_round_boldd, FontWeight.Bold))

    // Responsive sizing based on device type
    val (cardHeight, cornerRadius, imagePadding, imageHeight, titleFontSize, valueFontSize, maxTitleLines) = when (deviceType) {
        DeviceType.LargeTablet -> Tuple7(240.dp, 28.dp, 20.dp, 80.dp, 16.sp, 24.sp, 2)
        DeviceType.Tablet -> Tuple7(220.dp, 26.dp, 18.dp, 70.dp, 15.sp, 22.sp, 2)  
        DeviceType.SmallTablet -> Tuple7(200.dp, 24.dp, 16.dp, 60.dp, 14.sp, 20.sp, 2)
        DeviceType.Phone -> Tuple7(180.dp, 20.dp, 12.dp, 50.dp, 11.sp, 16.sp, 1)
    }

    Card(
        shape = RoundedCornerShape(cornerRadius),
        elevation = CardDefaults.cardElevation(elevation),
        modifier = modifier.height(cardHeight),
        colors = CardDefaults.cardColors(cardData.backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(imagePadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Top spacing
            Spacer(modifier = Modifier.height(imagePadding * 0.75f))
            
            // Card Image with better sizing
            Image(
                painter = painterResource(id = cardData.imageRes),
                contentDescription = cardData.title,
                modifier = Modifier.height(imageHeight),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(imagePadding * 0.75f))

            // Card Title with responsive text sizing
            Text(
                text = cardData.title,
                fontSize = titleFontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = alifbaFont,
                textAlign = TextAlign.Center,
                color = Color.Black,
                maxLines = maxTitleLines,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                lineHeight = titleFontSize * 1.2f
            )

            Spacer(modifier = Modifier.height(imagePadding * 0.5f))

            // Card Description (Stats Value)
            Text(
                text = cardData.description,
                fontSize = valueFontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = alifbaFontBold,
                textAlign = TextAlign.Center,
                color = Color.Black,
                maxLines = 1
            )
            
            // Bottom spacing
            Spacer(modifier = Modifier.height(imagePadding * 0.75f))
        }
    }
}

// Helper class for multiple return values
data class Tuple7<T1, T2, T3, T4, T5, T6, T7>(
    val first: T1,
    val second: T2, 
    val third: T3,
    val fourth: T4,
    val fifth: T5,
    val sixth: T6,
    val seventh: T7
)

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
            val windowSize = rememberWindowSizeClass()
            val isTablet = windowSize.widthSizeClass >= WindowWidthSizeClass.Medium

            // Badge Image
            Image(
                painter = rememberAsyncImagePainter(badge.imageUrl),
                contentDescription = badge.title,
                modifier = Modifier
                    .size(if (isTablet) 100.dp else 80.dp)
                    .weight(0.7f)
                    .padding(bottom = 4.dp),
                contentScale = ContentScale.Fit
            )

            // Badge Name
            Text(
                text = badge.name,
                fontSize = if (isTablet) 14.sp else 12.sp,
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
    val alifbaFontBold = FontFamily(
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val windowSize = rememberWindowSizeClass()
        val isTablet = windowSize.widthSizeClass >= WindowWidthSizeClass.Medium

        // Badge Image
        Image(
            painter = rememberAsyncImagePainter(badge.imageUrl),
            contentDescription = badge.title,
            modifier = Modifier
                .size(if (isTablet) 140.dp else 120.dp)
                .padding(bottom = if (isTablet) 20.dp else 16.dp),
            contentScale = ContentScale.Fit
        )

        // Badge Title
        Text(
            text = badge.title,
            fontSize = if (isTablet) 28.sp else 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = alifbaFontBold,
            color = black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(if (isTablet) 12.dp else 8.dp))

        // Badge Name
        Text(
            text = badge.name,
            fontSize = if (isTablet) 20.sp else 18.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = alifbaFontBold,
            color = black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(if (isTablet) 20.dp else 16.dp))

        // Badge Description
        Text(
            text = badge.description,
            fontSize = if (isTablet) 18.sp else 16.sp,
            fontFamily = alifbaFont,
            color = black,
            textAlign = TextAlign.Center,
            lineHeight = if (isTablet) 24.sp else 20.sp
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

@OptIn(ExperimentalMaterial3Api::class)
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
                fontSize = if (isTablet) 28.sp else 24.sp,
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
                        fontSize = if (isTablet) 26.sp else 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = alifbaFont,
                        color = navyBlue,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(if (isTablet) 12.dp else 8.dp))
                    
                    Text(
                        text = "Complete lessons and activities to earn your first badge!",
                        fontSize = if (isTablet) 18.sp else 16.sp,
                        fontFamily = alifbaFont,
                        color = navyBlue.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        lineHeight = if (isTablet) 24.sp else 20.sp
                    )
                }
            }
        } else {
            // Simple 3x grid layout for badges without cards with bottom sheet
            val rows = (earnedBadges.size + 2) / 3 // Calculate rows needed for 3-column grid
            var selectedBadge by remember { mutableStateOf<com.alifba.alifba.data.models.Badge?>(null) }

            // Show bottom sheet when badge is selected
            selectedBadge?.let { badge ->
                ModalBottomSheet(
                    onDismissRequest = { selectedBadge = null },
                    containerColor = white,
                    sheetState = rememberModalBottomSheetState()
                ) {
                    BadgeDetailsBottomSheet(
                        badge = badge,
                        onDismiss = { selectedBadge = null }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(rows) { rowIndex ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(3) { columnIndex ->
                            val badgeIndex = rowIndex * 3 + columnIndex
                            if (badgeIndex < earnedBadges.size) {
                                // Badge image
                                Image(
                                    painter = rememberAsyncImagePainter(earnedBadges[badgeIndex].imageUrl),
                                    contentDescription = earnedBadges[badgeIndex].title,
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clickable {
                                            SoundEffectManager.playClickSound()
                                            selectedBadge = earnedBadges[badgeIndex]
                                        },
                                    contentScale = ContentScale.Fit
                                )
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
    childProfile: ChildProfile?,
    avatarSize: Dp,
    nameTextSize: androidx.compose.ui.unit.TextUnit,
    alpha: Float,
    onAvatarClick: () -> Unit,
    onSettingsClick: () -> Unit,
    alifbaFont: FontFamily
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp.dp > 600.dp
    
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = if (isTablet) 32.dp else 16.dp,
                vertical = if (isTablet) 16.dp else 8.dp
            )
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
                    painter = if (childProfile != null) {
                        painterResource(id = getAvatarHeadShots(childProfile?.avatar ?: ""))
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

            Spacer(modifier = Modifier.width(if (isTablet) 24.dp else 16.dp))

            Column {
                Text(
                    text = childProfile?.childName ?: "Loading...",
                    fontSize = nameTextSize,
                    fontWeight = FontWeight.Bold,
                    color = navyBlue,
                    fontFamily = alifbaFont
                )
                Text(
                    text = "Age: ${childProfile?.age ?: "--"}",
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
                .size(if (isTablet) 44.dp else 36.dp)
                .clickable { onSettingsClick() }
        )
    }
}

@Composable
fun CollapsedHeader(
    childProfile: ChildProfile?,
    alpha: Float,
    onAvatarClick: () -> Unit,
    onSettingsClick: () -> Unit,
    alifbaFont: FontFamily
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp.dp > 600.dp
    
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = if (isTablet) 32.dp else 16.dp)
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
                    .size(if (isTablet) 50.dp else 40.dp)
                    .clickable { onAvatarClick() }
            ) {
                if (childProfile != null) {
                    Image(
                        painter = painterResource(id = getAvatarHeadShots(childProfile?.avatar ?: "")),
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(if (isTablet) 50.dp else 40.dp)
                            .clip(CircleShape)
                            .background(lightNavyBlue),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.avatar9),
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(if (isTablet) 50.dp else 40.dp)
                            .clip(CircleShape)
                            .background(lightNavyBlue),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(if (isTablet) 12.dp else 8.dp))

            Text(
                text = childProfile?.childName ?: "Loading...",
                fontSize = if (isTablet) 22.sp else 18.sp,
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
                .size(if (isTablet) 44.dp else 36.dp)
                .clickable { onSettingsClick() }
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun PreviewBadgeAchievementSection() {
    val alifbaFont = FontFamily(Font(R.font.vag_round, FontWeight.Bold))
    val alifbaFontBold = FontFamily(Font(R.font.vag_round_boldd, FontWeight.Bold))

    // Mock badges for preview
    val mockBadges = listOf(
        com.alifba.alifba.data.models.Badge(
            id = "first_lesson",
            title = "First Steps",
            description = "Completed your first lesson",
            imageUrl = "https://via.placeholder.com/100/4CAF50/FFFFFF?text=🏆"
        ),
        com.alifba.alifba.data.models.Badge(
            id = "week_streak",
            title = "Week Warrior",
            description = "7 day learning streak",
            imageUrl = "https://via.placeholder.com/100/FF9800/FFFFFF?text=🔥"
        ),
        com.alifba.alifba.data.models.Badge(
            id = "story_master",
            title = "Story Master",
            description = "Completed 5 stories",
            imageUrl = "https://via.placeholder.com/100/9C27B0/FFFFFF?text=📚"
        ),
        com.alifba.alifba.data.models.Badge(
            id = "quiz_champion",
            title = "Quiz Champion",
            description = "Scored 100% on 10 quizzes",
            imageUrl = "https://via.placeholder.com/100/2196F3/FFFFFF?text=🎯"
        ),
        com.alifba.alifba.data.models.Badge(
            id = "speed_learner",
            title = "Speed Learner",
            description = "Completed lesson in under 5 minutes",
            imageUrl = "https://via.placeholder.com/100/FF5722/FFFFFF?text=⚡"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(white)
            .padding(16.dp)
    ) {
        // Section title and view all
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Achievements",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = alifbaFontBold,
            )
            Text(
                text = "View All",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = navyBlue,
                fontFamily = alifbaFont,
            )
        }

        // Show only first 3 badges in a single row for preview
        val badgesToShow = mockBadges.take(3)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(3) { columnIndex ->
                if (columnIndex < badgesToShow.size) {
                    // Badge image
                    Image(
                        painter = rememberAsyncImagePainter(badgesToShow[columnIndex].imageUrl),
                        contentDescription = badgesToShow[columnIndex].title,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    // Empty space for grid alignment
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
