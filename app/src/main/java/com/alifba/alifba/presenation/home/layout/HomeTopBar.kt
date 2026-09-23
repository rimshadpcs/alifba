package com.alifba.alifba.presenation.home.layout

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.alifba.alifba.R
import com.alifba.alifba.presenation.login.AuthViewModel
import com.alifba.alifba.presenation.home.layout.profile.getAvatarHeadShots
import com.alifba.alifba.ui_components.dialogs.ParentGate
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeTopBar(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var showParentGate by remember { mutableStateOf(false) }
    val currentChildProfile by authViewModel.currentChildProfile.collectAsState()
    val avatarName = currentChildProfile?.avatar ?: "deenasaur"
    val avatarDrawable = getAvatarHeadShots(avatarName)
    val coroutineScope = rememberCoroutineScope()

    // Show ParentGate if triggered
    if (showParentGate) {
        ParentGate(
            onVerified = {
                coroutineScope.launch {
                    SoundEffectManager.playClickSound()
                    delay(100)
                    navController.navigate("settings")
                }
                showParentGate = false
            },
            onDismiss = {
                showParentGate = false
            }
        )
    }

    Row(
        modifier = Modifier
            .background(Color.Transparent)
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TopBarIcons(
            painter = painterResource(id = avatarDrawable),
            contentDescription = "Profile",
            onClick = {
                coroutineScope.launch {
                    SoundEffectManager.playClickSound()
                    delay(100)
                    navController.navigate("profile")
                }
            }
        )

        TopBarIcons(
            painter = painterResource(id = R.drawable.setting),
            contentDescription = "Settings",
            onClick = {
                showParentGate = true
            }
        )
    }
}


@Composable
fun TopBarIcons(
    painter: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    shadowColor: Color = Color(0xFFAAAAAA), // Subtle shadow color
    mainColor: Color = Color.White // Main background color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val coroutineScope = rememberCoroutineScope()

    // Press animation: shadow offset
    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 0.dp else 5.dp,
        animationSpec = spring(),
        label = "IconOffset"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(84.dp) // Ensure equal width and height for a perfect circle
            .padding(8.dp) // Padding around the entire box
    ) {
        // Shadow layer
        Box(
            modifier = Modifier
                .size(74.dp) // Shadow size
                .clip(CircleShape)
                .background(shadowColor) // Shadow color
        )

        // Icon layer with press effect
        Box(
            modifier = Modifier
                .size(74.dp) // Same size as the shadow layer
                .padding(bottom = offsetY) // Apply offset for press effect
                .clip(CircleShape)
                .background(mainColor) // Main color of the icon
                .clickable(
                    onClick = {
                        coroutineScope.launch {
                            delay(100)
                            onClick()
                        }
                    },
                    interactionSource = interactionSource,
                    indication = null
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.size(50.dp) // Icon size within the circle
            )
        }
    }
}
