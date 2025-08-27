package com.alifba.alifba.ui_components.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alifba.alifba.R
import com.alifba.alifba.presenation.home.layout.ProfileViewModel
import com.alifba.alifba.presenation.home.layout.profile.getAvatarHeadShots
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.theme.black
import com.alifba.alifba.ui_components.theme.mediumNavyBlue
import com.alifba.alifba.ui_components.theme.navTextGray
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager

@Composable
fun BottomNavigationBar(
    currentDestination: BottomNavDestination,
    onDestinationClick: (BottomNavDestination) -> Unit,
    profileViewModel: ProfileViewModel
) {
    val userProfile by profileViewModel.userProfileState.collectAsState()
    val avatarRes = userProfile?.avatar?.let { getAvatarHeadShots(it) } ?: R.drawable.avatar9
    val alifbaFont = FontFamily(Font(R.font.vag_round_boldd, FontWeight.Bold))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = black,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .padding(horizontal = 32.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home
            NavItem(
                icon = if (currentDestination == BottomNavDestination.Home) R.drawable.nav_home_filled else R.drawable.nav_home_gray,
                label = stringResource(id = R.string.home),
                isSelected = currentDestination == BottomNavDestination.Home,
                onClick = { 
                    SoundEffectManager.playClickSound()
                    onDestinationClick(BottomNavDestination.Home) 
                }
            )

            // Stories
            NavItem(
                icon = if (currentDestination == BottomNavDestination.Stories) R.drawable.nav_stories_filled else R.drawable.nav_stories_gray,
                label = stringResource(id = R.string.stories),
                isSelected = currentDestination == BottomNavDestination.Stories,
                onClick = { 
                    SoundEffectManager.playClickSound()
                    onDestinationClick(BottomNavDestination.Stories) 
                }
            )

            // Activities
            NavItem(
                icon = if (currentDestination == BottomNavDestination.Activities) R.drawable.nav_activities_filled else R.drawable.nav_activities_gray,
                label = stringResource(id = R.string.play),
                isSelected = currentDestination == BottomNavDestination.Activities,
                onClick = { 
                    SoundEffectManager.playClickSound()
                    onDestinationClick(BottomNavDestination.Activities) 
                }
            )

            // Account (Profile Picture)
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { 
                            SoundEffectManager.playClickSound()
                            onDestinationClick(BottomNavDestination.Account) 
                        }
                ) {
                    Image(
                        painter = painterResource(id = avatarRes),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .let { modifier ->
                                if (currentDestination == BottomNavDestination.Account) {
                                    modifier.border(2.dp, white, CircleShape)
                                } else {
                                    modifier
                                }
                            },
                        contentScale = ContentScale.Crop
                    )
                }
                if (currentDestination != BottomNavDestination.Account) {
                    Text(
                        text = stringResource(id = R.string.profile),
                        color = navTextGray,
                        fontSize = 14.sp,
                        fontFamily = alifbaFont,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NavItem(
    icon: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val alifbaFont = FontFamily(Font(R.font.vag_round_boldd, FontWeight.Bold))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable { 
            SoundEffectManager.playClickSound()
            onClick() 
        }
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = label,
                modifier = Modifier.size(if (isSelected) 30.dp else 28.dp) // Slightly smaller icon when not selected
            )
        }
        if (!isSelected) {
            Text(
                text = label,
                color = navTextGray,
                fontSize = 14.sp,
                fontFamily = alifbaFont,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

enum class BottomNavDestination {
    Home,
    Stories,
    Activities,
    Account
}