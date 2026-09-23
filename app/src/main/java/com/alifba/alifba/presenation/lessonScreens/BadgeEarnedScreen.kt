package com.alifba.alifba.presenation.lessonScreens

import android.content.Context
import android.media.MediaPlayer
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.alifba.alifba.R
import com.alifba.alifba.data.models.Badge
import com.alifba.alifba.presenation.chapters.ChaptersViewModel
import com.alifba.alifba.ui_components.widgets.DotLottieView
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton

@Composable
fun BadgeEarnedScreen(
    navController: NavController,
    chaptersViewModel: ChaptersViewModel
) {
    val earnedBadges by chaptersViewModel.badgeEarnedEvent.collectAsState()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600
    var badgesToShow by remember { mutableStateOf<List<Badge>>(emptyList()) }

    LaunchedEffect(earnedBadges) {
        if (earnedBadges.isNotEmpty() && badgesToShow.isEmpty()) {
            badgesToShow = earnedBadges
        }
    }

    if (badgesToShow.isEmpty() && earnedBadges.isEmpty()) {
        LaunchedEffect(Unit) {
            navController.navigate("homeScreen") {
                popUpTo("homeScreen") { inclusive = true }
            }
        }
        return
    }

    LaunchedEffect(badgesToShow) {
        if (badgesToShow.isNotEmpty()) {
            playCheerSound(context)
        }
    }

    val onComplete = {
        chaptersViewModel.clearBadgeEvent()
        navController.navigate("homeScreen") {
            popUpTo("homeScreen") { inclusive = true }
        }
    }

    BackHandler {
        onComplete()
    }

    BadgeEarnedContent(
        badges = badgesToShow,
        onComplete = onComplete,
        isTablet = isTablet
    )
}

@Composable
private fun BadgeEarnedContent(
    badges: List<Badge>,
    onComplete: () -> Unit,
    isTablet: Boolean
) {
    val titleFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )

    val badgeSize = if (isTablet) 180.dp else 140.dp
    val lottieSize = if (isTablet) 260.dp else 200.dp
    val horizontalPadding = if (isTablet) 48.dp else 24.dp
    val verticalPadding = if (isTablet) 32.dp else 24.dp
    var showCelebration by remember { mutableStateOf(true) }

    LaunchedEffect(badges) {
        showCelebration = true
        // Celebration animation is ~2 seconds; hide afterward to reclaim space.
        kotlinx.coroutines.delay(2200)
        showCelebration = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(white)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val isPreview = LocalInspectionMode.current
            if (showCelebration) {
                if (isPreview) {
                    Image(
                        painter = painterResource(R.drawable.alifbaround),
                        contentDescription = null,
                        modifier = Modifier.size(lottieSize),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    DotLottieView(
                        name = "moon_excited_jump",
                        repeatCount = 0,
                        modifier = Modifier.size(lottieSize)
                    )
                }

                Spacer(modifier = Modifier.height(if (isTablet) 24.dp else 16.dp))
            }

            androidx.compose.material3.Text(
                text = "Mashallah, You have won badges",
                fontSize = if (isTablet) 36.sp else 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = titleFont,
                color = navyBlue,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(if (isTablet) 28.dp else 20.dp))

            BadgeGrid(
                badges = badges,
                badgeSize = badgeSize,
                isTablet = isTablet
            )

            Spacer(modifier = Modifier.height(if (isTablet) 20.dp else 16.dp))
        }

        CommonButton(
            buttonText = "Complete",
            mainColor = lightNavyBlue,
            shadowColor = navyBlue,
            textColor = white,
            onClick = onComplete
        )
    }
}

@Composable
private fun BadgeGrid(
    badges: List<Badge>,
    badgeSize: androidx.compose.ui.unit.Dp,
    isTablet: Boolean
) {
    val spacing = if (isTablet) 20.dp else 14.dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        when (badges.size) {
            1 -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    BadgeImage(badge = badges[0], size = badgeSize)
                }
            }
            2 -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally)
                ) {
                    badges.forEach { badge ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            BadgeImage(
                                badge = badge,
                                size = badgeSize
                            )
                        }
                    }
                }
            }
            3 -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally)
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        BadgeImage(
                            badge = badges[0],
                            size = badgeSize
                        )
                    }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        BadgeImage(
                            badge = badges[1],
                            size = badgeSize
                        )
                    }
                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    BadgeImage(badge = badges[2], size = badgeSize)
                }
            }
            else -> {
                badges.chunked(2).forEach { rowBadges ->
                    if (rowBadges.size == 1) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            BadgeImage(
                                badge = rowBadges[0],
                                size = badgeSize
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally)
                        ) {
                            rowBadges.forEach { badge ->
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    BadgeImage(
                                        badge = badge,
                                        size = badgeSize
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

@Composable
private fun BadgeImage(
    badge: Badge,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val isPreview = LocalInspectionMode.current
    Image(
        painter = if (isPreview) {
            painterResource(R.drawable.alifbaround)
        } else {
            rememberAsyncImagePainter(badge.imageUrl)
        },
        contentDescription = badge.title,
        modifier = modifier.size(size),
        contentScale = ContentScale.Fit
    )
}

private fun playCheerSound(context: Context) {
    try {
        val mediaPlayer = MediaPlayer.create(context, R.raw.yay)
        mediaPlayer?.apply {
            setOnCompletionListener { release() }
            start()
        }
    } catch (_: Exception) {
        // Ignore audio playback errors.
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun BadgeEarnedScreenPreviewSingle() {
    BadgeEarnedContent(
        badges = listOf(
            Badge(
                id = "first_lesson_badge",
                title = "First Steps",
                description = "You completed your first lesson!",
                imageUrl = "https://via.placeholder.com/150"
            )
        ),
        onComplete = {},
        isTablet = false
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun BadgeEarnedScreenPreviewTriple() {
    BadgeEarnedContent(
        badges = listOf(
            Badge(
                id = "first_lesson_badge",
                title = "First Steps",
                description = "You completed your first lesson!",
                imageUrl = "https://via.placeholder.com/150"
            ),
            Badge(
                id = "story_badge",
                title = "Story Star",
                description = "You completed your first story!",
                imageUrl = "https://via.placeholder.com/150"
            ),
            Badge(
                id = "streak_badge",
                title = "On a Roll",
                description = "You kept a 3-day streak!",
                imageUrl = "https://via.placeholder.com/150"
            )
        ),
        onComplete = {},
        isTablet = false
    )
}
