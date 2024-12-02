package com.alifba.alifba.presenation.home.layout

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.alifba.alifba.R
import com.alifba.alifba.presenation.Login.AuthViewModel
import com.alifba.alifba.presenation.Login.Avatar
import com.alifba.alifba.presenation.Login.AvatarCarousel
import com.alifba.alifba.ui_components.theme.black
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


@Composable
fun ChangeAvatarScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val alifbaFont = FontFamily(
        Font(R.font.more_sugar_regular)
    )

    // Retrieve the user's current avatar
    val userProfile by profileViewModel.userProfileState.collectAsState()
    val currentAvatar = userProfile?.avatar ?: "Deenasaur" // Use default if null

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

        // Title Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        ) {
            Text(
                text = "Choose Avatar",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = alifbaFont,
                color = white,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Avatar Carousel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
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
                profileViewModel.updateAvatar(selectedAvatarName.value)
                navController.popBackStack()
            },
            buttonText = "Select Avatar",
            shadowColor = black,
            mainColor = white,
            textColor = navyBlue
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
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

    val alifbaFont = FontFamily(
        Font(R.font.more_sugar_regular)
    )

    val avatarsSize = avatars.size

    // Find the index of the current avatar
    val currentAvatarIndex = avatars.indexOfFirst { it.name == currentAvatarName }.takeIf { it >= 0 } ?: 0

    // Prepare infinite avatars list
    val repeatedCount = 1000
    val infiniteAvatars = List(avatarsSize * repeatedCount) { index -> avatars[index % avatarsSize] }

    // Set initial page to middle position adjusted by current avatar index
    val middlePosition = (infiniteAvatars.size / 2) - ((infiniteAvatars.size / 2) % avatarsSize) + currentAvatarIndex

    val pagerState = rememberPagerState(
        initialPage = middlePosition,
        pageCount = { infiniteAvatars.size }
    )

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        // Update selected avatar name on page change
        selectedAvatarName.value = infiniteAvatars[pagerState.currentPage % avatarsSize].name

        // Infinite scroll logic
        if (pagerState.currentPage == 0 || pagerState.currentPage == infiniteAvatars.size - 1) {
            val newPage = middlePosition + (pagerState.currentPage % avatarsSize)
            coroutineScope.launch {
                pagerState.scrollToPage(newPage)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        // Display the avatars in the HorizontalPager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentPadding = PaddingValues(horizontal = 88.dp),
            pageSpacing = (-30).dp,
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
                    modifier = Modifier.size(200.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = avatars[actualPage].name,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-30).dp),
                    fontFamily = alifbaFont,
                    color = black,
                    fontSize = 24.sp,
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
            // Left Arrow Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                elevation = ButtonDefaults.elevation(0.dp),
                modifier = Modifier.size(64.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.leftarrow),
                    contentDescription = "Scroll Left",
                    modifier = Modifier.size(64.dp)
                )
            }

            // Right Arrow Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                elevation = ButtonDefaults.elevation(0.dp),
                modifier = Modifier.size(64.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.rightarrow),
                    contentDescription = "Scroll Right",
                    modifier = Modifier.size(64.dp)
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
