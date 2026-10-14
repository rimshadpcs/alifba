package com.alifba.alifba.presenation.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.statusBarsPadding
import com.alifba.alifba.ui_components.widgets.LessonTheme
import androidx.compose.ui.draw.drawBehind
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alifba.alifba.R
import com.alifba.alifba.presenation.login.AuthViewModel
import com.alifba.alifba.presenation.login.ChildProfile
import com.alifba.alifba.presenation.stories.RevenueCatPaywall
import com.alifba.alifba.ui_components.theme.*
import com.alifba.alifba.presenation.SubscriptionViewModel

@Composable
fun ProfileSelectionScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val parentAccountState by authViewModel.parentAccountState.collectAsState()
    val subscriptionViewModel: SubscriptionViewModel = hiltViewModel()
    val isPremium by subscriptionViewModel.isPremium.collectAsState()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600
    
    val alifbaFontBold = FontFamily(Font(R.font.vag_round_boldd, FontWeight.Bold))

    var showParentGate by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var profileToDelete by remember { mutableStateOf<Int?>(null) }
    var showPremiumUpgrade by remember { mutableStateOf(false) }
    // Same race-free purchase signal used elsewhere — isPremium.value isn't reliable at
    // onClose time since setPremium() writes to DataStore asynchronously.
    var justPurchasedFromAddProfile by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authViewModel.fetchUserProfile()
    }

    // Show ParentGate if triggered
    if (showParentGate) {
        com.alifba.alifba.ui_components.dialogs.ParentGate(
            onVerified = {
                isEditMode = true
                showParentGate = false
            },
            onDismiss = {
                showParentGate = false
            }
        )
    }

    // Show Delete Confirmation Dialog
    if (showDeleteDialog && profileToDelete != null) {
        parentAccountState?.let { parentAccount ->
            val profileName = parentAccount.profiles.getOrNull(profileToDelete!!)?.childName ?: "Profile"
            DeleteConfirmationDialog(
                profileName = profileName,
                onConfirm = {
                    authViewModel.removeChildProfile(profileToDelete!!)
                    showDeleteDialog = false
                    profileToDelete = null
                },
                onDismiss = {
                    showDeleteDialog = false
                    profileToDelete = null
                }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            // Seaside illustration: a phone cut and a tablet cut
            painter = painterResource(id = if (isTablet) R.drawable.seaside_tablet else R.drawable.seaside_phone),
            contentDescription = "Choose Profile Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Edit / Done pill, top right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = LessonTheme.margin, end = LessonTheme.margin, top = 8.dp)
            ) {
                Spacer(Modifier.weight(1f))
                EditProfileButton(
                    isEditMode = isEditMode,
                    isTablet = isTablet,
                    onClick = {
                        if (isEditMode) {
                            isEditMode = false
                        } else {
                            showParentGate = true
                        }
                    }
                )
            }

            // Title
            Text(
                text = if (isEditMode) "Edit profiles" else "Who\u2019s learning today?",
                fontFamily = alifbaFontBold,
                fontSize = if (isTablet) 40.sp else 32.sp,
                fontWeight = FontWeight.Bold,
                color = LessonTheme.ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = if (isTablet) 40.dp else 28.dp, start = 24.dp, end = 24.dp)
            )
            Text(
                text = if (isEditMode) "Tap \u00D7 to remove a profile" else "Tap your picture to start",
                fontFamily = alifbaFontBold,
                fontSize = if (isTablet) 24.sp else 20.sp,
                color = LessonTheme.ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = if (isTablet) 40.dp else 24.dp)
            )

            // Profile grid: raised cards, two per row; the dashed "Add profile" tile is the last
            // tile while there are fewer than 3 profiles. A lone last tile is centred.
            parentAccountState?.let { parentAccount ->
                val tiles: List<ProfileTile> = buildList {
                    parentAccount.profiles.withIndex().forEach { add(ProfileTile.Profile(it.index, it.value)) }
                    if (parentAccount.profiles.size < 3) add(ProfileTile.Add)
                }
                val gap = if (isTablet) 24.dp else 16.dp

                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = if (isTablet) 60.dp else LessonTheme.margin)
                ) {
                    val tileSize = minOf((maxWidth - gap) / 2, if (isTablet) 260.dp else 190.dp)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 8.dp), // room for the cards' lip
                        verticalArrangement = Arrangement.spacedBy(gap - LessonTheme.answerEdge),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        tiles.chunked(2).forEach { rowTiles ->
                            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                                rowTiles.forEach { tile ->
                                    when (tile) {
                                        is ProfileTile.Profile -> ProfileCard(
                                            profile = tile.profile,
                                            isSelected = false,
                                            isEditMode = isEditMode,
                                            canRemove = true,
                                            isTablet = isTablet,
                                            size = tileSize,
                                            onClick = {
                                                if (!isEditMode) {
                                                    // Gate extra profiles for non-premium users
                                                    if (isPremium || tile.index == 0) {
                                                        authViewModel.switchToChildProfile(tile.index)

                                                        navController.navigate("homeScreen") {
                                                            popUpTo("profileSelection") { inclusive = true }
                                                        }
                                                    } else {
                                                        // Show premium unlock when tapping extra profiles
                                                        showPremiumUpgrade = true
                                                    }
                                                }
                                            },
                                            onRemove = {
                                                profileToDelete = tile.index
                                                showDeleteDialog = true
                                            }
                                        )
                                        ProfileTile.Add -> AddProfileCard(
                                            isTablet = isTablet,
                                            size = tileSize,
                                            onClick = {
                                                // Free users: allow only 1 profile. Premium: allow up to 3.
                                                val canAdd = if (isPremium) parentAccount.profiles.size < 3 else parentAccount.profiles.isEmpty()
                                                if (canAdd) {
                                                    navController.navigate("addProfile")
                                                } else {
                                                    showPremiumUpgrade = true
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Show ParentGate if triggered - MUST be after the main content so it appears on top
    if (showParentGate) {
        com.alifba.alifba.ui_components.dialogs.ParentGate(
            onVerified = {
                isEditMode = true
                showParentGate = false
            },
            onDismiss = {
                showParentGate = false
            }
        )
    }

    // Show Premium Upgrade Screen
    if (showPremiumUpgrade) {
        RevenueCatPaywall(
            onPurchased = { justPurchasedFromAddProfile = true },
            onClose = {
                showPremiumUpgrade = false
                if (!justPurchasedFromAddProfile && subscriptionViewModel.recordStandardPaywallSkip()) {
                    navController.navigate("discountPaywall")
                }
                justPurchasedFromAddProfile = false
            },
            source = "add_profile"
        )
    }
}

private sealed class ProfileTile {
    data class Profile(val index: Int, val profile: ChildProfile) : ProfileTile()
    object Add : ProfileTile()
}

/**
 * Raised card chrome (same look as the picture-MCQ cards): white face with a thick light-gray
 * border and a gray "lip" underneath. Pressing sinks the face onto the lip and tints it brand
 * blue for a moment before the tap is delivered. [content] gets the label colour for the
 * current press state.
 */
@Composable
private fun ProfileChrome(
    width: Dp,
    height: Dp,
    radius: Dp,
    lip: Dp,
    onClick: () -> Unit,
    overlay: @Composable BoxScope.() -> Unit = {},
    content: @Composable BoxScope.(labelColor: Color) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val coroutineScope = rememberCoroutineScope()
    val faceOffset by animateDpAsState(
        targetValue = if (isPressed) lip else 0.dp,
        animationSpec = spring(), label = ""
    )
    val borderWidth = 4.dp
    val faceColor = if (isPressed) LessonTheme.sky else Color.White
    val borderColor = if (isPressed) LessonTheme.primary else LessonTheme.line
    val lipColor = if (isPressed) LessonTheme.primaryEdge else LessonTheme.lineEdge
    val labelColor = if (isPressed) LessonTheme.skyText else LessonTheme.ink

    Box(
        modifier = Modifier
            .width(width)
            .height(height + lip)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    coroutineScope.launch {
                        delay(100)
                        onClick()
                    }
                }
            )
    ) {
        // Lip (the gray edge under the card)
        Box(
            modifier = Modifier
                .offset(y = lip)
                .size(width, height)
                .clip(RoundedCornerShape(radius))
                .background(lipColor)
        )
        // Face: border colour, then the face colour inset by the border width
        Box(
            modifier = Modifier
                .offset(y = faceOffset)
                .size(width, height)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(radius))
                    .background(borderColor)
                    .padding(borderWidth)
                    .clip(RoundedCornerShape(radius - borderWidth))
                    .background(faceColor)
            ) {
                content(labelColor)
            }
            overlay()
        }
    }
}

@Composable
fun ProfileCard(
    profile: ChildProfile,
    isSelected: Boolean,
    isEditMode: Boolean,
    canRemove: Boolean,
    isTablet: Boolean,
    size: Dp,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val alifbaFontBold = FontFamily(Font(R.font.vag_round_boldd, FontWeight.Bold))

    ProfileChrome(
        width = size,
        height = size,
        radius = if (isTablet) 28.dp else 22.dp,
        lip = if (isTablet) 8.dp else 6.dp,
        onClick = onClick,
        overlay = {
            // X button in edit mode
            if (isEditMode && canRemove) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(if (isTablet) 44.dp else 32.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(lightRed)
                        .clickable { onRemove() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "×",
                        color = white,
                        fontSize = if (isTablet) 28.sp else 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { labelColor ->
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Image(
                painter = painterResource(id = getAvatarDrawable(profile.avatar)),
                contentDescription = profile.avatar,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 14.dp, start = 14.dp, end = 14.dp)
                    .scale(0.8f), // 20% smaller than filling the card
                contentScale = ContentScale.Fit
            )
            // Name (inside the card, so it never sits on the busy background)
            Text(
                text = profile.childName,
                fontFamily = alifbaFontBold,
                fontWeight = FontWeight.Bold,
                fontSize = if (isTablet) 28.sp else 22.sp,
                color = labelColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
            )
        }
    }
}

/** Add profile tile: dashed blue outline on a sky tint, a primary round "+" button */
@Composable
fun AddProfileCard(isTablet: Boolean, size: Dp, onClick: () -> Unit) {
    val alifbaFontBold = FontFamily(Font(R.font.vag_round_boldd, FontWeight.Bold))
    val plus = if (isTablet) 84.dp else 64.dp
    val radius = if (isTablet) 28.dp else 22.dp
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val coroutineScope = rememberCoroutineScope()
    val offset by animateDpAsState(if (pressed) 4.dp else 0.dp, label = "addPress")
    val dash = LessonTheme.primary.copy(alpha = 0.55f)

    Box(
        modifier = Modifier
            .padding(bottom = if (isTablet) 8.dp else 6.dp) // same footprint as the raised cards' lip
            .size(size)
            .background(LessonTheme.sky.copy(alpha = 0.65f), RoundedCornerShape(radius))
            .drawBehind {
                drawRoundRect(
                    color = dash,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))
                    )
                )
            }
            .clickable(interactionSource = interaction, indication = null) {
                coroutineScope.launch {
                    delay(100)
                    onClick()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.size(plus).padding(bottom = 4.dp)) {
                Box(Modifier.matchParentSize().offset(y = 4.dp).background(LessonTheme.primaryEdge, CircleShape))
                Box(
                    Modifier.matchParentSize().offset(y = offset).background(LessonTheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = Color.White, fontSize = (plus.value * 0.62f).sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                text = "Add profile",
                fontFamily = alifbaFontBold,
                fontWeight = FontWeight.Bold,
                fontSize = if (isTablet) 26.sp else 22.sp,
                color = LessonTheme.primaryEdge,
                maxLines = 1
            )
        }
    }
}

/** Edit / Done pill (top right): white face with a lip, primary pencil and label */
@Composable
fun EditProfileButton(
    isEditMode: Boolean,
    isTablet: Boolean,
    onClick: () -> Unit
) {
    val alifbaFontBold = FontFamily(Font(R.font.vag_round_boldd, FontWeight.Bold))
    val height = if (isTablet) 56.dp else 46.dp

    ProfileChrome(
        width = if (isTablet) 132.dp else 112.dp,
        height = height,
        radius = height / 2,
        lip = 5.dp,
        onClick = onClick
    ) { _ ->
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isEditMode) {
                Text("\u2713", color = LessonTheme.primaryEdge, fontSize = if (isTablet) 22.sp else 18.sp, fontWeight = FontWeight.Bold)
            } else {
                Image(
                    painter = painterResource(id = R.drawable.pencil),
                    contentDescription = "Edit profiles",
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(LessonTheme.primaryEdge),
                    modifier = Modifier.size(if (isTablet) 22.dp else 18.dp)
                )
            }
            Text(
                text = if (isEditMode) "Done" else "Edit",
                fontFamily = alifbaFontBold,
                fontWeight = FontWeight.Bold,
                fontSize = if (isTablet) 24.sp else 20.sp,
                color = LessonTheme.primaryEdge
            )
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    profileName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val alifbaFont = FontFamily(Font(R.font.vag_round, FontWeight.Bold))

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = white,
        title = {
            Text(
                text = "Remove Profile",
                fontFamily = alifbaFont,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = navyBlue
            )
        },
        text = {
            Text(
                text = "Are you sure you want to remove $profileName's profile? This action cannot be undone.",
                fontSize = 16.sp,
                color = darkBlue
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = lightRed
                )
            ) {
                Text(
                    text = "Remove",
                    color = white,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = navyBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

// Avatar headshot mapping function
//fun getAvatarHeadshot(avatarName: String): Int {
//    // For now, using the same full avatar images as headshots
//    // You can replace these with actual headshot versions when available
//    return when(avatarName) {
//        "Deenasaur" -> R.drawable.deenasaur_head
//        "Duallama" -> R.drawable.duallama_head
//        "Firdawsaur" -> R.drawable.firdawsaur_head
//        "Ihsaninguin" -> R.drawable.ihsaninguin_head
//        "Imamoth" -> R.drawable.imamoth_head
//        "Khilafox" -> R.drawable.khilafox_head
//        "Shukraf" -> R.drawable.shukraf_head
//        "Jannah Bee" -> R.drawable.jannahbee_head
//        "Qadragon" -> R.drawable.qadragon_head
//        "Sabracorn" -> R.drawable.sabracorn_head
//        "Sadiqling" -> R.drawable.sadiqling_head
//        "Sidqhog" -> R.drawable.sidqhog_head
//        else -> R.drawable.deenasaur
//    }
//}

// Avatar mapping function (kept for compatibility with other screens)
fun getAvatarDrawable(avatarName: String): Int {
    return when(avatarName) {
        "Deenasaur" -> R.drawable.deenasaur
        "Duallama" -> R.drawable.duallama
        "Firdawsaur" -> R.drawable.firdawsaur
        "Ihsaninguin" -> R.drawable.ihsaninguin
        "Imamoth" -> R.drawable.imamoth
        "Khilafox" -> R.drawable.khilafox
        "Shukraf" -> R.drawable.shukraf
        "Jannah Bee" -> R.drawable.jannahbee
        "Qadragon" -> R.drawable.qadragon
        "Sabracorn" -> R.drawable.sabracorn
        "Sadiqling" -> R.drawable.sadiqling
        "Sidqhog" -> R.drawable.sidqhog
        else -> R.drawable.deenasaur // default
    }
}
