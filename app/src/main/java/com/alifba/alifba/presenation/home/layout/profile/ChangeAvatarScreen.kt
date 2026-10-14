package com.alifba.alifba.presenation.home.layout.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.navigation.NavController
import com.alifba.alifba.R
import androidx.compose.ui.unit.Dp
import com.alifba.alifba.presenation.login.AllAvatars
import com.alifba.alifba.presenation.home.layout.ProfileViewModel
import com.alifba.alifba.ui_components.widgets.LessonCircleButton
import com.alifba.alifba.ui_components.widgets.LessonPageDots
import com.alifba.alifba.ui_components.widgets.LessonPrimaryButton
import com.alifba.alifba.ui_components.widgets.LessonTheme
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


@Composable
fun ChangeAvatarScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel
) {
    val k = LessonTheme.scale()

    // Retrieve the user's current avatar
    val currentChildProfile by profileViewModel.currentChildProfile.collectAsState()
    val currentAvatar = currentChildProfile?.avatar ?: "Deenasaur" // Use default if null

    // State variable to hold the selected avatar name
    val selectedAvatarName = remember { mutableStateOf(currentAvatar) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.ufo_background),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Round back button on the left, title in a dark translucent pill in the middle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Choose Avatar",
                    style = LessonTheme.question().copy(
                        fontSize = (28 * k).sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier
                        .background(LessonTheme.ink.copy(alpha = 0.35f), RoundedCornerShape(50))
                        .padding(horizontal = 28.dp, vertical = 8.dp)
                )
                Box(modifier = Modifier.align(Alignment.CenterStart)) {
                    LessonCircleButton(
                        iconRes = R.drawable.backarrow,
                        contentDescription = "Back",
                        onClick = { navController.popBackStack() }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            AvatarCarousels(
                selectedAvatarName = selectedAvatarName,
                currentAvatarName = currentAvatar
            )

            // Bottom sheet: name, page dots and the primary button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .navigationBarsPadding()
                    .padding(start = LessonTheme.margin, end = LessonTheme.margin, top = 24.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = selectedAvatarName.value,
                    style = LessonTheme.question().copy(fontSize = (32 * k).sp)
                )
                LessonPageDots(modifier = Modifier.padding(bottom = 8.dp))
                LessonPrimaryButton(
                    title = "Choose ${selectedAvatarName.value}",
                    onClick = {
                        android.util.Log.d("ChangeAvatar", "Updating avatar to: ${selectedAvatarName.value}")
                        profileViewModel.updateAvatar(selectedAvatarName.value)
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun AvatarCarousels(
    selectedAvatarName: androidx.compose.runtime.MutableState<String>,
    currentAvatarName: String,
    /** The centre buddy's size, with the arrows level with its middle; null keeps this screen's own */
    avatarSize: Dp? = null,
    onIndexChange: (Int) -> Unit = {}
) {
    val avatars = AllAvatars


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
        onIndexChange(pagerState.currentPage % avatarsSize)

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
    val avatarDisplaySize = avatarSize
        ?: if (isTablet) (screenWidth * 0.35f).coerceAtMost(300.dp) else 190.dp
    val horizontalPadding = (screenWidth - avatarDisplaySize) / 2
    val arrowSize = (48 * LessonTheme.scale()).dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (avatarSize != null) avatarDisplaySize else avatarDisplaySize + 60.dp)
            .padding(bottom = if (avatarSize != null) 0.dp else 12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(avatarDisplaySize),
            // The padding centers the current item on the screen
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            pageSpacing = 0.dp,
        ) { page ->
            val actualPage = page % avatarsSize
            // Distance of this page from the centre: 0 = current, 1 = a full page away
            val distance = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                .absoluteValue.coerceIn(0f, 1f)
            val scale = lerp(start = 1f, stop = 0.55f, fraction = distance)
            val alpha = lerp(start = 1f, stop = 0.3f, fraction = distance)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = avatars[actualPage].id),
                    contentDescription = "Avatar ${avatars[actualPage].name}",
                    modifier = Modifier
                        .size(avatarDisplaySize)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                )
            }
        }

        // Round arrows at the outer edges, level with the avatar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(
                    y = if (avatarSize != null) -((avatarDisplaySize - arrowSize) / 2).coerceAtLeast(0.dp)
                    else (-avatarDisplaySize * 0.2f)
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LessonCircleButton(
                size = arrowSize,
                onClick = {
                    coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.ChevronLeft,
                    contentDescription = "Scroll Left",
                    tint = LessonTheme.primary,
                    modifier = Modifier.size((30 * LessonTheme.scale()).dp)
                )
            }
            LessonCircleButton(
                size = arrowSize,
                onClick = {
                    coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Scroll Right",
                    tint = LessonTheme.primary,
                    modifier = Modifier.size((30 * LessonTheme.scale()).dp)
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
