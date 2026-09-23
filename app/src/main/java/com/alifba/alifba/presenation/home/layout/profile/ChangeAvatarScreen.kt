package com.alifba.alifba.presenation.home.layout.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Button
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alifba.alifba.R
import com.alifba.alifba.presenation.login.Avatar
import com.alifba.alifba.presenation.home.layout.ProfileViewModel
import com.alifba.alifba.ui_components.theme.black
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


@Composable
fun ChangeAvatarScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel
) {
    val alifbaFontBold = FontFamily(
        Font(R.font.vag_round_boldd)
    )


    // Retrieve the user's current avatar
    val currentChildProfile by profileViewModel.currentChildProfile.collectAsState()
    val currentAvatar = currentChildProfile?.avatar ?: "Deenasaur" // Use default if null

    // State variable to hold the selected avatar name
    val selectedAvatarName = remember { mutableStateOf(currentAvatar) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(white)
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.ufo_background),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        val configuration = LocalConfiguration.current
        val isTablet = configuration.screenWidthDp >= 600
        val iconSize = if (isTablet) 32.dp else 24.dp
        val backIconSize = if (isTablet) 64.dp else 48.dp // 2x larger

        // Top Bar with Back Button and Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button (matching AllStories style, no circular background)
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "Back",
                modifier = Modifier
                    .size(backIconSize)
                    .clickable {
                        SoundEffectManager.playClickSound()
                        navController.popBackStack()
                    }
            )

            // Title
            Text(
                text = "Choose Avatar",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = alifbaFontBold,
                color = white,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }

        // Avatar Carousel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = if (isTablet) 96.dp else 64.dp)
        ) {
            AvatarCarousels(
                selectedAvatarName = selectedAvatarName,
                currentAvatarName = currentAvatar
            )
        }

        // "Select Avatar" Button
        CommonButton(
            modifier = Modifier.align(Alignment.BottomCenter),
            onClick = {
                android.util.Log.d("ChangeAvatar", "Updating avatar to: ${selectedAvatarName.value}")
                profileViewModel.updateAvatar(selectedAvatarName.value)
                android.util.Log.d("ChangeAvatar", "Avatar update called, navigating back")
                navController.popBackStack()
            },
            buttonText = "Select Avatar",
            shadowColor = black,
            mainColor = white,
            textColor = navyBlue
        )
    }
}
@Composable
fun AvatarCarousels(
    selectedAvatarName: androidx.compose.runtime.MutableState<String>,
    currentAvatarName: String
) {
    val avatars = listOf(
        Avatar(R.drawable.deenasaur, "Deenasaur"),
        Avatar(R.drawable.duallama, "Duallama"),
        Avatar(R.drawable.firdawsaur, "Firdawsaur"),
        Avatar(R.drawable.ihsaninguin, "Ihsaninguin"),
        Avatar(R.drawable.imamoth, "Imamoth"),
        Avatar(R.drawable.khilafox, "Khilafox"),
        Avatar(R.drawable.shukraf, "Shukraf"),
        Avatar(R.drawable.jannahbee, "Jannah Bee"),
        Avatar(R.drawable.qadragon, "Qadragon"),
        Avatar(R.drawable.sabracorn, "Sabracorn"),
        Avatar(R.drawable.sadiqling, "Sadiqling"),
        Avatar(R.drawable.sidqhog, "Sidqhog")
    )


    val avatarsSize = avatars.size

    // Find the index of the current avatar (defaulting to 0 if not found)
    val currentAvatarIndex = avatars.indexOfFirst { it.name == currentAvatarName }
        .takeIf { it >= 0 } ?: 0

    // Prepare an "infinite" list for smooth scrolling
    val repeatedCount = 1000
    val infiniteAvatars = List(avatarsSize * repeatedCount) { index -> avatars[index % avatarsSize] }
    val middlePosition = (infiniteAvatars.size / 2) - ((infiniteAvatars.size / 2) % avatarsSize) + currentAvatarIndex

    val pagerState = rememberPagerState(
        initialPage = middlePosition,
        pageCount = { infiniteAvatars.size }
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        selectedAvatarName.value = infiniteAvatars[pagerState.currentPage % avatarsSize].name

        // Avoid reaching the boundaries
        if (pagerState.currentPage == 0 || pagerState.currentPage == infiniteAvatars.size - 1) {
            val newPage = middlePosition + (pagerState.currentPage % avatarsSize)
            coroutineScope.launch { pagerState.scrollToPage(newPage) }
        }
    }

    // Calculate the padding so that the centered item is exactly in the middle
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isTablet = configuration.screenWidthDp >= 600
    // Scale avatar size on tablets slightly smaller to avoid clipping
    val proposedSize = screenWidth * 0.35f
    val avatarDisplaySize = if (isTablet) proposedSize.coerceAtMost(380.dp) else 200.dp
    val horizontalPadding = (screenWidth - avatarDisplaySize) / 2

    val containerHeight = if (isTablet) avatarDisplaySize + 180.dp else 300.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(containerHeight),
        contentAlignment = Alignment.Center
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                // Increase pager height to keep the name fully visible
                .height(avatarDisplaySize + if (isTablet) 48.dp else 36.dp),
            // The padding centers the current item on the screen
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            // Increase the gap between items
            pageSpacing = if (isTablet) 48.dp else 32.dp,
        ) { page ->
            val actualPage = page % avatarsSize
            val scale = lerp(
                start = 0.8f,
                stop = 1f,
                fraction = 1f - pagerState.currentPageOffsetFraction.absoluteValue.coerceIn(0f, 1f)
            )
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = avatars[actualPage].id),
                    contentDescription = "Avatar ${avatars[actualPage].name}",
                    modifier = Modifier.size(avatarDisplaySize)
                )
                Spacer(modifier = Modifier.height(8.dp))
                val alifbaFontBold = FontFamily(
                    Font(R.font.vag_round_boldd)
                )
                Text(
                    text = avatars[actualPage].name,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = if (isTablet) (-28).dp else (-24).dp),
                    fontFamily = alifbaFontBold,
                    color = black,
                    fontSize = if (isTablet) 28.sp else 22.sp,
                )
            }
        }

        // Navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        SoundEffectManager.playClickSound()
                        delay(100)
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                elevation = ButtonDefaults.elevation(0.dp),
                modifier = Modifier.size(if (isTablet) 96.dp else 64.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.leftarrow),
                    contentDescription = "Scroll Left",
                    modifier = Modifier.size(if (isTablet) 96.dp else 64.dp)
                )
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        SoundEffectManager.playClickSound()
                        delay(100)
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                elevation = ButtonDefaults.elevation(0.dp),
                modifier = Modifier.size(if (isTablet) 96.dp else 64.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.rightarrow),
                    contentDescription = "Scroll Right",
                    modifier = Modifier.size(if (isTablet) 96.dp else 64.dp)
                )
            }
        }
    }
}




//@Preview(showBackground = true)
//@Composable
//fun ChangeAvatarScreenPreview() {
//    val navController = rememberNavController() // Mock NavController for preview
//    ChangeAvatarScreen(
//        navController = navController,
//        onAvatarSelected = { /* No action needed for preview */ }
//    )
//}
