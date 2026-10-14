package com.alifba.alifba.presenation.home.layout.profile

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.geometry.isSpecified
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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
import androidx.compose.foundation.border
import androidx.compose.ui.focus.onFocusChanged
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.alifba.alifba.R
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Delete
import com.alifba.alifba.ui_components.widgets.LessonTheme
import com.alifba.alifba.ui_components.widgets.AnswerCard
import com.alifba.alifba.ui_components.widgets.AnswerState
import com.alifba.alifba.ui_components.widgets.AuthTextField
import com.alifba.alifba.ui_components.widgets.AuthTheme
import com.alifba.alifba.ui_components.widgets.ConfirmSheet
import com.alifba.alifba.ui_components.widgets.ConfirmSheetIcon
import com.alifba.alifba.ui_components.widgets.LessonCircleButton
import com.alifba.alifba.ui_components.widgets.LessonPrimaryButton
import com.alifba.alifba.presenation.home.layout.ProfileViewModel
import com.alifba.alifba.ui_components.dialogs.ParentGate
import com.alifba.alifba.presenation.login.ChildProfile
import com.alifba.alifba.ui_components.theme.black
import com.alifba.alifba.ui_components.theme.lightCandyGreen
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.lightPurple
import com.alifba.alifba.ui_components.theme.lightRed
import com.alifba.alifba.ui_components.theme.lightYellow
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import kotlinx.coroutines.delay

// Base screen background — warm cream, matching Home and Stories (was plain white).
private val profileScreenCream = Color(0xFFFFF8ED)

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
    val currentChildProfile by profileViewModel.currentChildProfile.collectAsState()
    val parentAccount by profileViewModel.parentAccountState.collectAsState()
    val earnedBadges by profileViewModel.earnedBadges.collectAsState()
    val alifbaFont = FontFamily(Font(R.font.vag_round, FontWeight.Bold))
    val alifbaFontBold = FontFamily(Font(R.font.vag_round_boldd, FontWeight.Bold))

    // Window size class for responsive layout
    val windowSize = rememberWindowSizeClass()
    val isTablet = windowSize.widthSizeClass >= WindowWidthSizeClass.Medium

    var showEditProfileSheet by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var editedAge by remember { mutableStateOf<Int?>(null) }
    var showRemoveGate by remember { mutableStateOf(false) }
    var showRemoveConfirm by remember { mutableStateOf(false) }
    
    // Scroll state for collapsing header effect
    val scrollState = rememberLazyListState()
    
    // Animation parameters matching Swift code exactly
    val baseHeaderHeight = if (isTablet) 290.dp else 218.dp
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
            val baseSize = if (isTablet) 150.dp else 108.dp
            val collapsedSize = if (isTablet) 90.dp else 60.dp
            baseSize - (collapseProgress.value * (baseSize.value - collapsedSize.value)).dp
        }
    }
    
    val nameTextSize by remember {
        derivedStateOf {
            if (isTablet) 34.sp else 24.sp
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

    Box(modifier = Modifier.fillMaxSize().background(profileScreenCream)) {
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
                    text = "Progress Overview",
                    fontSize = if (isTablet) 24.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = alifbaFontBold,
                    modifier = Modifier.padding(
                        horizontal = if (isTablet) 24.dp else 16.dp,
                        vertical = if (isTablet) 16.dp else 8.dp
                    )
                )
            }

            // User Stats: a 2x2 grid of soft outlined tiles (streak, lessons, quizzes, XP). The
            // streak tile is tinted while a streak is live and greyed out at zero, with a one-line
            // nudge above the grid.
            item {
                val deviceType = getDeviceType()
                val horizontalPadding = when (deviceType) {
                    DeviceType.LargeTablet -> 40.dp
                    DeviceType.Tablet -> 32.dp
                    DeviceType.SmallTablet -> 24.dp
                    DeviceType.Phone -> 16.dp
                }
                val streak = currentChildProfile?.dayStreak ?: 0

                Column(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                    if (streak == 0) {
                        Text(
                            text = "Complete a lesson today to start your streak!",
                            fontSize = if (isTablet) 16.sp else 14.sp,
                            fontFamily = alifbaFont,
                            color = Color(0xFF6D6A63),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    StatsGrid(
                        stats = listOf(
                            StatTileData(
                                R.drawable.streakscore,
                                "Day Streak",
                                "$streak",
                                lightRed,
                                tint = if (streak > 0) StatStreakTint else white,
                                border = if (streak > 0) StatStreakBorder else StatNeutralBorder,
                                dimmed = streak == 0
                            ),
                            StatTileData(
                                R.drawable.lessonsattended,
                                "Lessons Completed",
                                "${(currentChildProfile?.lessonsCompleted?.size ?: 0) + (currentChildProfile?.storiesCompleted?.size ?: 0)}",
                                lightCandyGreen,
                                tint = StatLessonsTint,
                                border = StatLessonsBorder
                            ),
                            StatTileData(
                                R.drawable.quizzesattended,
                                "Quizzes Attended",
                                "${currentChildProfile?.quizzesAttended ?: 0}",
                                lightYellow,
                                tint = StatQuizzesTint,
                                border = StatQuizzesBorder
                            ),
                            StatTileData(
                                R.drawable.xpgained,
                                "Total XP",
                                "${currentChildProfile?.xp ?: 0}",
                                lightNavyBlue,
                                tint = StatXpTint,
                                border = StatXpBorder
                            )
                        ),
                        isTablet = isTablet
                    )
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
                            fontSize = if (isTablet) 24.sp else 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = alifbaFontBold,
                        )
                        Text(
                            text = "View All",
                            fontSize = if (isTablet) 18.sp else 16.sp,
                            fontWeight = FontWeight.Normal,
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
                        // Show a small grid of recent badges (more on tablets)
                        val maxBadges = if (isTablet) 8 else 6
                        val badgesToShow = earnedBadges.take(maxBadges)
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

                        // Same arrangement as iOS: each badge is an equal share of the row
                        // width (tight gaps), and a partly filled last row is centred rather
                        // than left-aligned.
                        val columns = if (isTablet) 4 else 3
                        val spacing = if (isTablet) 8.dp else 4.dp
                        val rowSpacing = if (isTablet) 4.dp else 2.dp
                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val badgeWidth = (maxWidth - spacing * (columns - 1)) / columns
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(rowSpacing),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                badgesToShow.chunked(columns).forEach { rowBadges ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                                        rowBadges.forEach { badge ->
                                            val painter = rememberAsyncImagePainter(badge.imageUrl)
                                            val intrinsic = painter.intrinsicSize
                                            // Height follows the image's own proportions (iOS
                                            // scaledToFit); a neutral ratio until it has loaded.
                                            val ratio = if (intrinsic.isSpecified && intrinsic.height > 0f) {
                                                intrinsic.width / intrinsic.height
                                            } else 1f
                                            Image(
                                                painter = painter,
                                                contentDescription = badge.title,
                                                modifier = Modifier
                                                    .width(badgeWidth)
                                                    .aspectRatio(ratio)
                                                    .clickable {
                                                        SoundEffectManager.playClickSound()
                                                        selectedBadge = badge
                                                    },
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                    }
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
                    .background(profileScreenCream)
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
                    onAvatarClick = {
                        editedName = currentChildProfile?.childName.orEmpty()
                        editedAge = currentChildProfile?.age?.takeIf { it > 0 }
                        nameError = null
                        showEditProfileSheet = true
                    },
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
                    onAvatarClick = {
                        editedName = currentChildProfile?.childName.orEmpty()
                        editedAge = currentChildProfile?.age?.takeIf { it > 0 }
                        nameError = null
                        showEditProfileSheet = true
                    },
                    onSettingsClick = { 
                        SoundEffectManager.playClickSound()
                        onSettingsClick()
                    },
                    alifbaFont = alifbaFont
                )
            }
        }

        // Edit profile bottom sheet
        if (showEditProfileSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val originalAge = currentChildProfile?.age?.takeIf { it > 0 }
            val canRemove = (parentAccount?.profiles?.size ?: 0) > 1

            // Writes the name and age that changed; the name must not be empty
            fun commitChanges(): Boolean {
                val original = currentChildProfile?.childName.orEmpty()
                val trimmed = editedName.trim()
                if (trimmed.isEmpty()) {
                    nameError = "Please enter a name"
                    return false
                }
                val nameChanged = trimmed != original
                val ageChanged = editedAge != null && editedAge != originalAge
                if (nameChanged || ageChanged) {
                    profileViewModel.updateChildDetails(
                        newName = if (nameChanged) trimmed else null,
                        newAge = if (ageChanged) editedAge else null
                    )
                }
                nameError = null
                return true
            }

            ModalBottomSheet(
                onDismissRequest = { showEditProfileSheet = false },
                sheetState = sheetState,
                containerColor = white
            ) {
                EditProfileSheetContent(
                    avatar = currentChildProfile?.avatar,
                    name = editedName,
                    onNameChange = {
                        editedName = it.take(20)
                        nameError = null
                    },
                    nameError = nameError,
                    age = editedAge,
                    onAgeChange = { editedAge = it },
                    canRemove = canRemove,
                    onChangeBuddy = {
                        if (commitChanges()) {
                            showEditProfileSheet = false
                            navController.navigate("changeAvatar")
                        }
                    },
                    onSave = {
                        if (commitChanges()) {
                            showEditProfileSheet = false
                        }
                    },
                    onClose = { showEditProfileSheet = false },
                    onRemove = { showRemoveGate = true },
                    isTablet = isTablet
                )
            }

            if (showRemoveGate) {
                ParentGate(
                    onVerified = {
                        showRemoveGate = false
                        showRemoveConfirm = true
                    },
                    onDismiss = { showRemoveGate = false }
                )
            }

            if (showRemoveConfirm) {
                ConfirmSheet(
                    title = "Remove ${currentChildProfile?.childName.orEmpty()}\u2019s profile?",
                    message = "This deletes their progress, badges and streak for good. It can\u2019t be undone.",
                    primaryTitle = "Keep this profile",
                    destructiveTitle = "Yes, remove it",
                    onPrimary = { showRemoveConfirm = false },
                    onDestructive = {
                        showRemoveConfirm = false
                        showEditProfileSheet = false
                        profileViewModel.removeActiveChildProfile()
                    },
                    icon = {
                        ConfirmSheetIcon(
                            imageVector = androidx.compose.material.icons.Icons.Outlined.Delete,
                            tint = LessonTheme.wrongEdge,
                            face = Color(0xFFFCEAEA)
                        )
                    }
                )
            }
        }

    }
}

/** The ages a child can be set to; the last one reads "13+" */
private val EditProfileAges = (4..13).toList()

@Composable
private fun EditProfileSheetContent(
    avatar: String?,
    name: String,
    onNameChange: (String) -> Unit,
    nameError: String?,
    age: Int?,
    onAgeChange: (Int) -> Unit,
    canRemove: Boolean,
    onChangeBuddy: () -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
    onRemove: () -> Unit,
    isTablet: Boolean
) {
    val k = LessonTheme.scale()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .navigationBarsPadding()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title centred, round close button on the right
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 12.dp)
        ) {
            Text(
                text = "Edit profile",
                style = LessonTheme.question().copy(fontSize = (28 * k).sp),
                modifier = Modifier.align(Alignment.Center)
            )
            LessonCircleButton(
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = onClose
            ) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = LessonTheme.primary,
                    modifier = Modifier.size((20 * k).dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = LessonTheme.margin)
                .padding(top = 8.dp, bottom = 16.dp)
        ) {
            // Avatar and "Change buddy" both open the avatar picker
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileAvatar(
                    avatar = avatar,
                    size = (120 * k).dp,
                    ringWidth = if (isTablet) 8.dp else 5.dp,
                    badgeSize = (40 * k).dp,
                    modifier = Modifier.clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) {
                        SoundEffectManager.playClickSound()
                        onChangeBuddy()
                    }
                )
                Text(
                    text = "Change buddy",
                    style = LessonTheme.answer().copy(fontSize = (17 * k).sp, color = LessonTheme.primaryEdge),
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) {
                            SoundEffectManager.playClickSound()
                            onChangeBuddy()
                        }
                )
            }

            AuthTextField(
                value = name,
                onValueChange = onNameChange,
                placeholder = "Enter name",
                icon = null,
                modifier = Modifier.padding(top = 24.dp),
                label = "Child\u2019s name",
                hint = nameError,
                hintStyle = com.alifba.alifba.ui_components.widgets.AuthHintStyle.Bad,
                isError = nameError != null,
                capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Words,
                imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                focusHalo = true
            )

            Text(
                text = "Age",
                style = LessonTheme.answer().copy(fontSize = (17 * k).sp),
                modifier = Modifier.padding(start = 4.dp, top = 24.dp, bottom = 8.dp)
            )

            // Two rows of five
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                EditProfileAges.chunked(5).forEach { rowAges ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowAges.forEach { value ->
                            Box(modifier = Modifier.weight(1f)) {
                                AnswerCard(
                                    state = if (age == value) AnswerState.Selected else AnswerState.Normal,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((52 * k).dp),
                                    onClick = { onAgeChange(value) }
                                ) { labelColor ->
                                    Text(
                                        text = if (value == EditProfileAges.last()) "$value+" else "$value",
                                        style = LessonTheme.answer().copy(fontSize = (22 * k).sp, color = labelColor)
                                    )
                                }
                            }
                        }
                        repeat(5 - rowAges.size) { Spacer(modifier = Modifier.weight(1f)) }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(horizontal = LessonTheme.margin)
                .padding(top = 8.dp, bottom = if (canRemove) 4.dp else 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LessonPrimaryButton(title = "Save", onClick = onSave)

            if (canRemove) {
                Text(
                    text = "Remove this profile",
                    style = LessonTheme.answer().copy(fontSize = (17 * k).sp, color = LessonTheme.wrongEdge),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) {
                            SoundEffectManager.playClickSound()
                            onRemove()
                        }
                        .wrapContentHeight(Alignment.CenterVertically)
                )
            }
        }
    }
}

/**
 * The child's avatar in a black ring with the blue pencil badge; the profile header and the Edit
 * profile sheet share it.
 */
@Composable
private fun ProfileAvatar(
    avatar: String?,
    size: Dp,
    ringWidth: Dp,
    badgeSize: Dp,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.size(size)) {
        Image(
            painter = painterResource(
                id = if (avatar != null) getAvatarHeadShots(avatar) else R.drawable.avatar9
            ),
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(white)
                .border(ringWidth, black, CircleShape),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 2.dp, y = 6.dp)
                .size(badgeSize)
                .background(LessonTheme.primary, CircleShape)
                .border(3.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Filled.Edit,
                contentDescription = "Edit profile",
                tint = Color.White,
                modifier = Modifier.size(badgeSize * 0.5f)
            )
        }
    }
}

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

// Each stat tile is tinted (12%) and bordered (60%) in its own icon colour, so the four tiles read
// as a matching set. Keep these in sync with the icon colours passed to StatTileData.
private val StatStreakTint = Color(0xFFFEEAEA)
private val StatStreakBorder = Color(0xFFF89797)
private val StatLessonsTint = Color(0xFFEBF7ED)
private val StatLessonsBorder = Color(0xFF9BD9A3)
private val StatQuizzesTint = Color(0xFFFFF6E5)
private val StatQuizzesBorder = Color(0xFFFFD37C)
private val StatXpTint = Color(0xFFEEF1FF)
private val StatXpBorder = Color(0xFFABBAFF)
private val StatNeutralBorder = Color(0xFFEDEDED)

// A stat in the 2x2 grid. [tint] and [border] colour the tile; [dimmed] greys out the icon (an
// empty state, which also uses the neutral white tile).
data class StatTileData(
    val imageRes: Int,
    val title: String,
    val value: String,
    val iconColor: Color,
    val tint: Color = white,
    val border: Color = StatNeutralBorder,
    val dimmed: Boolean = false
)

@Composable
fun StatsGrid(
    stats: List<StatTileData>,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    val gap = if (isTablet) 16.dp else 12.dp
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        stats.chunked(2).forEach { rowStats ->
            // IntrinsicSize.Min keeps both tiles in a row the same height if a label wraps.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(gap)
            ) {
                rowStats.forEach { stat ->
                    StatTile(
                        stat = stat,
                        isTablet = isTablet,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
                if (rowStats.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatTile(
    stat: StatTileData,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    val alifbaFontBold = FontFamily(Font(R.font.vag_round_boldd, FontWeight.Bold))
    val alifbaFont = FontFamily(Font(R.font.vag_round, FontWeight.Normal))
    val shape = RoundedCornerShape(if (isTablet) 22.dp else 18.dp)
    val iconCircleSize = if (isTablet) 44.dp else 36.dp

    Column(
        modifier = modifier
            .clip(shape)
            .background(stat.tint)
            .border(width = 1.5.dp, color = stat.border, shape = shape)
            .padding(
                horizontal = if (isTablet) 18.dp else 14.dp,
                vertical = if (isTablet) 18.dp else 14.dp
            )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(iconCircleSize)
                    .background(if (stat.dimmed) Color(0xFFE3E1DC) else stat.iconColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = stat.imageRes),
                    contentDescription = stat.title,
                    modifier = Modifier
                        .size(iconCircleSize * 0.55f)
                        .alpha(if (stat.dimmed) 0.45f else 1f),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = stat.value,
                fontSize = if (isTablet) 30.sp else 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = alifbaFontBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stat.title,
            fontSize = if (isTablet) 15.sp else 13.sp,
            fontFamily = alifbaFont,
            maxLines = 2
        )
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
    val alifbaFontBold = FontFamily(Font(R.font.vag_round, FontWeight.Bold))

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
                colorFilter = ColorFilter.tint(LocalContentColor.current),
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
                fontFamily = alifbaFont
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

            // Same arrangement as the profile badges (and iOS): three equal-width badges per
            // row with tight gaps, and a partly filled last row centred, not left-aligned.
            val columns = 3
            val spacing = 4.dp
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val badgeWidth = (maxWidth - spacing * (columns - 1)) / columns
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(rows) { rowIndex ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally)
                        ) {
                            earnedBadges.drop(rowIndex * columns).take(columns).forEach { badge ->
                                Image(
                                    painter = rememberAsyncImagePainter(badge.imageUrl),
                                    contentDescription = badge.title,
                                    modifier = Modifier
                                        .width(badgeWidth)
                                        .aspectRatio(1f)
                                        .clickable {
                                            SoundEffectManager.playClickSound()
                                            selectedBadge = badge
                                        },
                                    contentScale = ContentScale.Fit
                                )
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
    
    // Avatar centred with the name and age under it; the settings button stays in the top-right
    // corner.
    val headerNameSize = if (isTablet) 42.sp else 32.sp
    val headerAgeSize = if (isTablet) 26.sp else 19.sp
    val badgeSize = avatarSize * 0.35f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = if (isTablet) 32.dp else 16.dp,
                vertical = if (isTablet) 20.dp else 4.dp
            )
            .graphicsLayer(alpha = alpha)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar with the black ring and the blue edit badge
            ProfileAvatar(
                avatar = childProfile?.avatar,
                size = avatarSize,
                ringWidth = if (isTablet) 8.dp else 5.dp,
                badgeSize = badgeSize,
                modifier = Modifier.clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) { onAvatarClick() }
            )

            Text(
                text = childProfile?.childName ?: "Loading...",
                style = LessonTheme.question().copy(fontSize = headerNameSize),
                modifier = Modifier.padding(top = 10.dp)
            )
            val age = childProfile?.age
            if (age != null && age.toString() != "0") {
                Text(
                    text = "Age $age",
                    style = LessonTheme.answer().copy(fontSize = headerAgeSize, color = AuthTheme.secondaryText)
                )
            }
        }

        // Settings button, top-right
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            SettingsButton(
                buttonSize = if (isTablet) 72.dp else 48.dp,
                onClick = onSettingsClick
            )
        }
    }
}

// Solid cog in a black-ringed circle (no fill), matching the black ring around the avatar. The
// visible ring is tighter than the tap area so the button stays easy to hit.
@Composable
private fun SettingsButton(
    buttonSize: Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(buttonSize)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(buttonSize * 0.83f)
                .border(1.5.dp, black, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.setting),
                contentDescription = "Settings",
                colorFilter = ColorFilter.tint(LocalContentColor.current),
                modifier = Modifier.size(buttonSize * 0.58f)
            )
        }
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
                    .size(if (isTablet) 60.dp else 40.dp)
                    .clickable { onAvatarClick() }
            ) {
                if (childProfile != null) {
                    Image(
                        painter = painterResource(id = getAvatarHeadShots(childProfile?.avatar ?: "")),
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(if (isTablet) 60.dp else 40.dp)
                            .clip(CircleShape)
                            .background(white)
                            .border(4.dp, black, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.avatar9),
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(if (isTablet) 60.dp else 40.dp)
                            .clip(CircleShape)
                            .background(white)
                            .border(4.dp, black, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(if (isTablet) 12.dp else 8.dp))

            Text(
                text = childProfile?.childName ?: "Loading...",
                fontSize = if (isTablet) 28.sp else 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = alifbaFont
            )
        }

        // Settings button on the right
        SettingsButton(
            buttonSize = if (isTablet) 56.dp else 40.dp,
            onClick = onSettingsClick
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
                fontWeight = FontWeight.Bold,
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
