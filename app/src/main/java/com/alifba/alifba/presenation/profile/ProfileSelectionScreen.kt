package com.alifba.alifba.presenation.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alifba.alifba.R
import com.alifba.alifba.presenation.login.AuthViewModel
import com.alifba.alifba.presenation.login.ChildProfile
import com.alifba.alifba.presenation.home.layout.HomeTopBar
import com.alifba.alifba.presenation.stories.PremiumUnlockScreen
import com.alifba.alifba.ui_components.theme.*

@Composable
fun ProfileSelectionScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val parentAccountState by authViewModel.parentAccountState.collectAsState()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600
    
    val alifbaFont = FontFamily(Font(R.font.vag_round, FontWeight.Bold))
    val alifbaFontBold = FontFamily(Font(R.font.vag_round_boldd))

    var showParentGate by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var profileToDelete by remember { mutableStateOf<Int?>(null) }
    var showPremiumUpgrade by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authViewModel.fetchUserProfile()
    }

    // Show ParentGate if triggered
    if (showParentGate) {
        com.alifba.alifba.presenation.home.layout.ParentGate(
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
            painter = painterResource(id = R.drawable.chooseprofile),
            contentDescription = "Choose Profile Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Title
            Text(
                text = if (isEditMode) "Edit Profiles" else "Choose Your Profile",
                fontFamily = alifbaFontBold,
                fontSize = if (isTablet) 42.sp else 32.sp,
                fontWeight = FontWeight.Bold,
                color = darkBlue,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Select which child will be learning today",
                fontSize = if (isTablet) 24.sp else 18.sp,
                color = darkBlue,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = if (isTablet) 12.dp else 8.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Profile Grid
            parentAccountState?.let { parentAccount ->
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(if (isTablet) 32.dp else 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(if (isTablet) 32.dp else 24.dp),
                    contentPadding = PaddingValues(horizontal = if (isTablet) 40.dp else 32.dp)
                ) {
                    // Profile Cards
                    itemsIndexed(parentAccount.profiles) { index, profile ->
                        ProfileCard(
                            profile = profile,
                            isSelected = false,
                            isEditMode = isEditMode,
                            canRemove = true,
                            isTablet = isTablet,
                            onClick = {
                                if (!isEditMode) {
                                    authViewModel.switchToChildProfile(index)
                                    navController.navigate("homeScreen") {
                                        popUpTo("profileSelection") { inclusive = true }
                                    }
                                }
                            },
                            onRemove = {
                                profileToDelete = index
                                showDeleteDialog = true
                            }
                        )
                    }

                    // Add New Profile Card (only show if less than 3 profiles)
                    if (parentAccount.profiles.size < 3) {
                        item {
                            AddProfileCard(
                                isTablet = isTablet,
                                onClick = {
                                    // Allow first profile creation for free, show upgrade for 2nd+ profiles
                                    if (parentAccount.profiles.isEmpty()) {
                                        // No profiles exist - allow free creation of first profile
                                        navController.navigate("addProfile")
                                    } else {
                                        // Show upgrade screen for users trying to add 2nd or 3rd profile
                                        showPremiumUpgrade = true
                                    }
                                }
                            )
                        }
                    }

                    // Edit Profile Card (always show)
                    item {
                        EditProfileCard(
                            isEditMode = isEditMode,
                            isTablet = isTablet,
                            onClick = {
                                println("EDIT BUTTON CLICKED - isEditMode: $isEditMode")
                                if (isEditMode) {
                                    println("SETTING EDIT MODE TO FALSE")
                                    isEditMode = false
                                } else {
                                    println("SETTING PARENT GATE TO TRUE")
                                    showParentGate = true
                                }
                                println("NEW STATE - isEditMode: $isEditMode, showParentGate: $showParentGate")
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }

    // Show ParentGate if triggered - MUST be after the main content so it appears on top
    if (showParentGate) {
        com.alifba.alifba.presenation.home.layout.ParentGate(
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
        PremiumUnlockScreen(
            onCloseClick = {
                showPremiumUpgrade = false
            },
            onSubscribeClick = { plan ->
                // Handle subscription logic here
                // For now, just close the screen
                showPremiumUpgrade = false
                // TODO: Integrate with actual subscription system
            }
        )
    }
}

@Composable
fun ProfileCard(
    profile: ChildProfile,
    isSelected: Boolean,
    isEditMode: Boolean,
    canRemove: Boolean,
    isTablet: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val alifbaFont = FontFamily(Font(R.font.vag_round, FontWeight.Bold))
    val alifbaFontBold= FontFamily(Font(R.font.vag_round_boldd, FontWeight.Bold))


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        // Headshot Avatar with X button
        Box(
            modifier = Modifier
                .size(if (isTablet) 200.dp else 156.dp)  // Larger size for tablets
                .padding(if (isTablet) 12.dp else 8.dp)  // More padding for tablets
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(white)
            ) {
                Image(
                    painter = painterResource(id = getAvatarDrawable(profile.avatar)),
                    contentDescription = profile.avatar,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentScale = ContentScale.Fit
                )
                
            }

            // X button in edit mode
            if (isEditMode && canRemove) {
                Box(
                    modifier = Modifier
                        .size(if (isTablet) 40.dp else 32.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(lightRed)
                        .clickable { onRemove() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "×",
                        color = white,
                        fontSize = if (isTablet) 24.sp else 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(if (isTablet) 16.dp else 12.dp))

        // Name
        Text(
            text = profile.childName,
            fontFamily = alifbaFont,
            fontSize = if (isTablet) 26.sp else 20.sp,
            fontWeight = FontWeight.Bold,
            color = darkBlue,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AddProfileCard(isTablet: Boolean, onClick: () -> Unit) {
    val alifbaFont = FontFamily(Font(R.font.vag_round, FontWeight.Bold))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        // Add Profile Square
        Box(
            modifier = Modifier
                .size(if (isTablet) 176.dp else 140.dp)
                .clip(RoundedCornerShape(if (isTablet) 20.dp else 16.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.add),
                contentDescription = "Add Profile",
                modifier = Modifier.size(if (isTablet) 80.dp else 60.dp)
            )
        }

        Spacer(modifier = Modifier.height(if (isTablet) 16.dp else 12.dp))

        Text(
            text = "Add Profile",
            fontFamily = alifbaFont,
            fontSize = if (isTablet) 26.sp else 20.sp,
            fontWeight = FontWeight.Bold,
            color = darkBlue,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EditProfileCard(
    isEditMode: Boolean,
    isTablet: Boolean,
    onClick: () -> Unit
) {
    val alifbaFont = FontFamily(Font(R.font.vag_round, FontWeight.Bold))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { 
            android.util.Log.d("EditProfileCard", "Card clicked!")
            onClick() 
        }
    ) {
        // Edit Profile Square
        Box(
            modifier = Modifier
                .size(if (isTablet) 176.dp else 140.dp)
                .clip(RoundedCornerShape(if (isTablet) 20.dp else 16.dp))
                .background(white),
            contentAlignment = Alignment.Center
        ) {
            if (isEditMode) {
                Text(
                    text = "✓",
                    color = darkBlue,
                    fontSize = if (isTablet) 64.sp else 48.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.pencil),
                    contentDescription = "Edit Profiles",
                    modifier = Modifier.size(if (isTablet) 80.dp else 60.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(if (isTablet) 16.dp else 12.dp))

        Text(
            text = if (isEditMode) "Done" else "Edit",
            fontFamily = alifbaFont,
            fontSize = if (isTablet) 26.sp else 20.sp,
            fontWeight = FontWeight.Bold,
            color = darkBlue,
            textAlign = TextAlign.Center
        )
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