package com.alifba.alifba.presenation.profile

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alifba.alifba.R
import com.alifba.alifba.presenation.login.AuthViewModel
import com.alifba.alifba.presenation.login.AvatarCarousel
import com.alifba.alifba.presenation.login.AgeSelectionCards
import com.alifba.alifba.presenation.login.ProfileCreationState
import com.alifba.alifba.ui_components.theme.*
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.alifba.alifba.ui_components.widgets.textFields.CustomInputField
import androidx.compose.material3.Text
import com.alifba.alifba.ui_components.dialogs.LottieAnimationLoading

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var isLoading by remember { mutableStateOf(false) }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val halfScreenHeight = screenHeight / 2
    val alifbaFont = FontFamily(Font(R.font.vag_round, FontWeight.Bold))
    val context = LocalContext.current
    var childName by remember { mutableStateOf("") }
    var selectedAge by remember { mutableStateOf<Int?>(null) }
    var selectedAvatarName by remember { mutableStateOf("") }

    val profileCreationState by authViewModel.profileCreationState.collectAsState()

    // Handle navigation after profile is added
    LaunchedEffect(isLoading) {
        if (isLoading) {
            // Wait a moment for the profile to be added
            kotlinx.coroutines.delay(2000)
            Toast.makeText(context, "Profile Added Successfully!", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(white)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(halfScreenHeight)
        ) {
            // Background Image
            Image(
                painter = painterResource(id = R.drawable.ufo_background),
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Avatar Carousel
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            ) {
                AvatarCarousel { avatarName ->
                    selectedAvatarName = avatarName
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Choose Avatar",
            style = androidx.compose.material.MaterialTheme.typography.h6,
            fontFamily = alifbaFont,
            fontWeight = FontWeight.Bold,
            color = navyBlue,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Remaining Content
        Column(modifier = Modifier.padding(16.dp)) {
            // Only Child Name input - no Parent Name
            CustomInputField(
                value = childName,
                onValueChange = { childName = it },
                labelText = "Child Name"
            )

            AgeSelectionCards { age ->
                selectedAge = age
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cancel Button
                CommonButton(
                    onClick = {
                        navController.popBackStack()
                    },
                    buttonText = "Cancel",
                    mainColor = white,
                    shadowColor = mediumNavyBlue,
                    textColor = mediumNavyBlue,
                    modifier = Modifier.weight(1f)
                )

                // Add Profile Button
                CommonButton(
                    onClick = {
                        if (!isLoading && childName.isNotBlank() && selectedAge != null && selectedAvatarName.isNotBlank()) {
                            isLoading = true
                            authViewModel.addNewChildProfile(
                                childName = childName,
                                age = selectedAge!!,
                                avatar = selectedAvatarName
                            )
                        } else {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        }
                    },
                    buttonText = if (isLoading) "Adding..." else "Add Profile",
                    mainColor = navyBlue,
                    shadowColor = lightNavyBlue,
                    textColor = white,
                    modifier = Modifier.weight(1f)
                )
            }

            if (isLoading) {
                LottieAnimationLoading(
                    showDialog = remember { mutableStateOf(true) },
                    lottieFileRes = R.raw.loading_lottie,
                    isTransparentBackground = true,
                    onAnimationEnd = {}
                )
            }
        }
    }
}