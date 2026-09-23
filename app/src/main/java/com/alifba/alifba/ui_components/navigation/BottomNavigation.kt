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
import androidx.compose.ui.platform.LocalConfiguration
import com.alifba.alifba.R
import com.alifba.alifba.presenation.home.layout.ProfileViewModel
import com.alifba.alifba.presenation.home.layout.profile.getAvatarHeadShots
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager

@Composable
fun BottomNavigationBar(
    currentDestination: BottomNavDestination,
    onDestinationClick: (BottomNavDestination) -> Unit,
    profileViewModel: ProfileViewModel
) {
    val currentChildProfile by profileViewModel.currentChildProfile.collectAsState()
    val avatarRes = currentChildProfile?.avatar?.let { getAvatarHeadShots(it) } ?: R.drawable.avatar9
    val alifbaFont = FontFamily(Font(R.font.vag_round_boldd, FontWeight.Bold))
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = navBarBackground,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .border(
                width = 2.dp,
                color = navBarDivider,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .padding(
                horizontal = if (isTablet) 48.dp else 32.dp,
                vertical = if (isTablet) 16.dp else 12.dp
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home — single icon asset now (home.png), selection shown via pill outline only,
            // not a gray/filled icon swap.
            NavItem(
                icon = R.drawable.home,
                label = stringResource(id = R.string.home),
                isSelected = currentDestination == BottomNavDestination.Home,
                isTablet = isTablet,
                onClick = {
                    SoundEffectManager.playClickSound()
                    onDestinationClick(BottomNavDestination.Home)
                }
            )

            // Stories — single icon asset (stories.png), same pill-outline selection treatment.
            NavItem(
                icon = R.drawable.stories,
                label = stringResource(id = R.string.stories),
                isSelected = currentDestination == BottomNavDestination.Stories,
                isTablet = isTablet,
                onClick = {
                    SoundEffectManager.playClickSound()
                    onDestinationClick(BottomNavDestination.Stories)
                }
            )

            // Activities (Play) — temporarily hidden until feature is ready
            /*
            NavItem(
                icon = if (currentDestination == BottomNavDestination.Activities) R.drawable.nav_activities_filled else R.drawable.nav_activities_gray,
                label = stringResource(id = R.string.play),
                isSelected = currentDestination == BottomNavDestination.Activities,
                isTablet = isTablet,
                onClick = {
                    SoundEffectManager.playClickSound()
                    onDestinationClick(BottomNavDestination.Activities)
                }
            )
            */

            // Account (Profile Picture)
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isTablet) 58.dp else 46.dp)
                        .let { boxModifier ->
                            // Same filled-pill treatment as Home/Stories — no ring on the
                            // avatar itself.
                            if (currentDestination == BottomNavDestination.Account) {
                                boxModifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(navPillBackground)
                            } else {
                                boxModifier
                            }
                        }
                        .clickable {
                            SoundEffectManager.playClickSound()
                            onDestinationClick(BottomNavDestination.Account)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = avatarRes),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(if (isTablet) 40.dp else 30.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                run {
                    val isAccountSelected = currentDestination == BottomNavDestination.Account
                    Text(
                        text = stringResource(id = R.string.profile),
                        color = if (isAccountSelected) navTextActive else navTextInactive,
                        fontSize = if (isTablet) 16.sp else 14.sp,
                        fontFamily = alifbaFont,
                        fontWeight = if (isAccountSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(top = if (isTablet) 6.dp else 4.dp)
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
    isTablet: Boolean,
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
            modifier = Modifier
                .size(if (isTablet) 58.dp else 46.dp)
                .let { boxModifier ->
                    // Selected state is now a soft filled pastel pill (no border) behind the
                    // icon — unselected items get no container at all. Icons themselves
                    // (home.png/stories.png) are full-color illustrations, not tintable
                    // monochrome glyphs, so only the pill fill + label color change with state.
                    if (isSelected) {
                        boxModifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(navPillBackground)
                    } else {
                        boxModifier
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = label,
                modifier = Modifier.size(if (isTablet) 40.dp else 30.dp)
            )
        }
        Text(
            text = label,
            color = if (isSelected) navTextActive else navTextInactive,
            fontSize = if (isTablet) 16.sp else 14.sp,
            fontFamily = alifbaFont,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(top = if (isTablet) 6.dp else 4.dp)
        )
    }
}

// Bottom nav colors.
private val navBarBackground = Color(0xFFFFFFFF)
private val navBarDivider = Color(0xFF2B2B2B)
private val navPillBackground = Color(0xFFE1F0FF)
private val navTextActive = Color(0xFF2D3142)
private val navTextInactive = Color(0xFF9E9E9E)

enum class BottomNavDestination {
    Home,
    Stories,
    Activities,
    Account
}
