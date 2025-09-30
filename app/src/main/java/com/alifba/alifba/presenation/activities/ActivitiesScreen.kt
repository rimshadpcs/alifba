package com.alifba.alifba.presenation.activities

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alifba.alifba.R
import com.alifba.alifba.presenation.home.layout.ProfileViewModel
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ActivitiesScreen(
    onShowBottomNav: (Boolean) -> Unit = {},
    profileViewModel: ProfileViewModel,
    activitiesViewModel: ActivitiesViewModel = viewModel()
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600

    val coroutineScope = rememberCoroutineScope()
    val modalSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmValueChange = { it != ModalBottomSheetValue.HalfExpanded },
        skipHalfExpanded = true
    )

    val hasVoted by activitiesViewModel.hasVoted.collectAsState()
    val currentChildProfile by profileViewModel.currentChildProfile.collectAsState()
    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )

    LaunchedEffect(profileViewModel) {
        profileViewModel.startProfileListener()
    }
    DisposableEffect(profileViewModel) {
        onDispose {
            profileViewModel.stopProfileListener()
        }
    }

    LaunchedEffect(Unit) {
        activitiesViewModel.checkForVote()
    }

    ModalBottomSheetLayout(
        sheetState = modalSheetState,
        sheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        sheetContent = {
            FeedbackForm(onSubmit = {
                activitiesViewModel.submitFeedback(it) {
                    coroutineScope.launch {
                        modalSheetState.hide()
                    }
                }
            })
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Header Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF6ed4ef),  // Dark blue
                                    Color(0xffaed7e3)   // Light blue
                                )
                            )
                        )
                        .padding(
                            horizontal = if (isTablet) 36.dp else 24.dp,
                            vertical = if (isTablet) 72.dp else 48.dp
                        )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Assalamu Alaikkum",
                                fontFamily = alifbaFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isTablet) 28.sp else 20.sp,
                                color = Color.Black
                            )
                            Text(
                                text = currentChildProfile?.childName ?: "Loading...",
                                fontFamily = alifbaFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isTablet) 32.sp else 24.sp,
                                color = Color.Black
                            )
                        }

                        Image(
                            painter = painterResource(id = R.drawable.sunny),
                            contentDescription = "Cloudy",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(if (isTablet) 200.dp else 140.dp)
                                .clip(RoundedCornerShape(if (isTablet) 12.dp else 8.dp))
                        )
                    }
                }
            }
            // Color the Pictures Section
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xfff38f9f),  // Hot pink
                                        Color(0xfff6aeb6)  // Light blue
                                    )
                                )
                            )
                            .padding(vertical = if (isTablet) 72.dp else 48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = if (isTablet) 18.dp else 12.dp)
                        ) {
                            Text(
                                text = "Color the Pictures",
                                fontFamily = alifbaFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isTablet) 30.sp else 22.sp,
                                color = Color.Black
                            )
                        }

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = if (isTablet) 36.dp else 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(if (isTablet) 18.dp else 12.dp)
                        ) {
                            item {
                                ActivityCard(
                                    title = "Makkah",
                                    description = "Color a delicious popsicle",
                                    iconRes = R.drawable.makkah,
                                    iconColor = Color(0xFFE74C3C),
                                    onClick = { /* TODO: Navigate to flower coloring */ },
                                    alifbaFont = alifbaFont,
                                    isComingSoon = true,
                                    isTablet = isTablet
                                )
                            }
                            item {
                                ActivityCard(
                                    title = "Madina",
                                    description = "Color a beautiful flower",
                                    iconRes = R.drawable.madina,
                                    iconColor = Color(0xFFE74C3C),
                                    onClick = { /* TODO: Navigate to flower coloring */ },
                                    alifbaFont = alifbaFont,
                                    isComingSoon = true,
                                    isTablet = isTablet
                                )
                            }
                            item {
                                ActivityCard(
                                    title = "Al Aqsa",
                                    description = "Color a racing car",
                                    iconRes = R.drawable.alaqsa,
                                    iconColor = Color(0xFFE74C3C),
                                    onClick = { /* TODO: Navigate to car coloring */ },
                                    alifbaFont = alifbaFont,
                                    isComingSoon = true,
                                    isTablet = isTablet
                                )
                            }
                        }
                    }

                    // Sabracorn image positioned to protrude above background
                    Image(
                        painter = painterResource(id = R.drawable.sabracorn),
                        contentDescription = "Sabracorn",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(if (isTablet) 220.dp else 156.dp)
                            .align(Alignment.TopStart)
                            .offset(
                                x = if (isTablet) 24.dp else 16.dp,
                                y = if (isTablet) (-96).dp else (-64).dp
                            )
                    )
                }
            }

            // Learn the Letters Section
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF82dda7),  // Dark green
                                        Color(0xffafddc1)   // Light green
                                    )
                                )
                            )
                            .padding(vertical = if (isTablet) 72.dp else 48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = "Learn the Letters",
                                fontFamily = alifbaFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isTablet) 30.sp else 22.sp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(124.dp))
                        }

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = if (isTablet) 36.dp else 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(if (isTablet) 18.dp else 12.dp)
                        ) {
                            item {
                                ActivityCard(
                                    title = "Alif",
                                    description = "Learn the letter Alif",
                                    iconRes = R.drawable.alificon,
                                    iconColor = Color(0xFF3498DB),
                                    onClick = { /* TODO: Navigate to Alif learning */ },
                                    alifbaFont = alifbaFont,
                                    isComingSoon = true,
                                    isTablet = isTablet
                                )
                            }
                            item {
                                ActivityCard(
                                    title = "Baa",
                                    description = "Learn the letter Baa",
                                    iconRes = R.drawable.baaicon,
                                    iconColor = Color(0xFF3498DB),
                                    onClick = { /* TODO: Navigate to Baa learning */ },
                                    alifbaFont = alifbaFont,
                                    isComingSoon = true,
                                    isTablet = isTablet
                                )
                            }
                            item {
                                ActivityCard(
                                    title = "Taa",
                                    description = "Learn the letter Taa",
                                    iconRes = R.drawable.thaaicon,
                                    iconColor = Color(0xFF3498DB),
                                    onClick = { /* TODO: Navigate to Taa learning */ },
                                    alifbaFont = alifbaFont,
                                    isComingSoon = true,
                                    isTablet = isTablet
                                )
                            }
                            item {
                                ActivityCard(
                                    title = "Taa",
                                    description = "Learn the letter Thsaa",
                                    iconRes = R.drawable.thsaaicon,
                                    iconColor = Color(0xFF3498DB),
                                    onClick = { /* TODO: Navigate to Taa learning */ },
                                    alifbaFont = alifbaFont,
                                    isComingSoon = true,
                                    isTablet = isTablet
                                )
                            }
                        }
                    }

                    // Imamoth image positioned to protrude above background (top right)
                    Image(
                        painter = painterResource(id = R.drawable.imamoth),
                        contentDescription = "Imamoth",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(if (isTablet) 220.dp else 156.dp)
                            .align(Alignment.TopEnd)
                            .offset(
                                x = if (isTablet) (-24).dp else (-16).dp,
                                y = if (isTablet) (-96).dp else (-64).dp
                            )
                    )
                }
            }

            // Mini Games Section
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFfff478),  // Light purple
                                        Color(0xfffdf8af)   // Light purple
                                    )
                                )
                            )
                            .padding(vertical = if (isTablet) 72.dp else 48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = if (isTablet) 18.dp else 12.dp)
                        ) {
                            Text(
                                text = "Mini Games",
                                fontFamily = alifbaFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isTablet) 30.sp else 22.sp,
                                color = Color.Black
                            )
                        }

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = if (isTablet) 36.dp else 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(if (isTablet) 18.dp else 12.dp)
                        ) {
                            item {
                                ActivityCard(
                                    title = "Memory Match",
                                    description = "Match Arabic letters",
                                    iconRes = R.drawable.matchinggame,
                                    iconColor = Color(0xFF9B59B6),
                                    onClick = { /* TODO: Navigate to memory game */ },
                                    alifbaFont = alifbaFont,
                                    isComingSoon = true,
                                    isTablet = isTablet
                                )
                            }
                            item {
                                ActivityCard(
                                    title = "Hide & Seek",
                                    description = "Quiz on Arabic letters",
                                    iconRes = R.drawable.hideandseek,
                                    iconColor = Color(0xFF9B59B6),
                                    onClick = { /* TODO: Navigate to letter quiz */ },
                                    alifbaFont = alifbaFont,
                                    isComingSoon = true,
                                    isTablet = isTablet
                                )
                            }
                            item {
                                ActivityCard(
                                    title = "Memory",
                                    description = "Build Arabic words",
                                    iconRes = R.drawable.memorygame,
                                    iconColor = Color(0xFF9B59B6),
                                    onClick = { /* TODO: Navigate to word builder */ },
                                    alifbaFont = alifbaFont,
                                    isComingSoon = true,
                                    isTablet = isTablet
                                )
                            }
                        }
                    }

                    // Ihsaninguin image positioned to protrude above background (top left)
                    Image(
                        painter = painterResource(id = R.drawable.ihsaninguin),
                        contentDescription = "Ihsaninguin",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(if (isTablet) 220.dp else 156.dp)
                            .align(Alignment.TopStart)
                            .offset(
                                x = if (isTablet) 24.dp else 16.dp,
                                y = if (isTablet) (-96).dp else (-64).dp
                            )
                    )
                }
            }

            // Footer Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isTablet) 280.dp else 200.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFb6abfe),  // Light purple
                                    Color(0xffd3cdfa)   // Light purple
                                )
                            )
                        )
                        .padding(if (isTablet) 18.dp else 12.dp)
                ) {
                    // Palm tree - bottom left
                    Image(
                        painter = painterResource(id = R.drawable.palmtree),
                        contentDescription = "Palm Tree",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(if (isTablet) 170.dp else 120.dp)
                            .align(Alignment.BottomStart)
                    )

                    // Camel - bottom right
                    Image(
                        painter = painterResource(id = R.drawable.camel),
                        contentDescription = "Camel",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(if (isTablet) 170.dp else 120.dp)
                            .align(Alignment.BottomEnd)
                    )

                    // Text in center top
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = if (isTablet) 4.dp else 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (!hasVoted) {
                            CommonButton(
                                onClick = { 
                                    coroutineScope.launch {
                                        modalSheetState.show()
                                    }
                                },
                                buttonText = "Vote for our next feature?",
                                mainColor = lightNavyBlue,
                                shadowColor = navyBlue,
                                textColor = white
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                        Text(
                            text = "Keep learning, ${currentChildProfile?.childName ?: "Loading..."}!",
                            fontFamily = alifbaFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isTablet) 26.sp else 18.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "We are building more for you,Inshallah 🕌",
                            fontFamily = alifbaFont,
                            fontSize = if (isTablet) 20.sp else 14.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityCard(
    title: String,
    description: String,
    iconRes: Int,
    iconColor: Color,
    onClick: () -> Unit,
    alifbaFont: FontFamily,
    modifier: Modifier = Modifier,
    isComingSoon: Boolean = false,
    isTablet: Boolean = false
) {
    val configuration = LocalConfiguration.current
    val actualIsTablet = isTablet || (configuration.screenWidthDp > 600)
    Card(
        modifier = modifier
            .width(if (actualIsTablet) 220.dp else 160.dp)
            .height(if (actualIsTablet) 320.dp else 240.dp)
            .clickable(enabled = !isComingSoon) { onClick() },
        shape = RoundedCornerShape(if (actualIsTablet) 24.dp else 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (actualIsTablet) 16.dp else 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background image filling the entire card
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Text section at the bottom with semi-transparent white background
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Color.White.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(
                            bottomStart = if (actualIsTablet) 24.dp else 16.dp,
                            bottomEnd = if (actualIsTablet) 24.dp else 16.dp
                        )
                    )
                    .padding(
                        horizontal = if (actualIsTablet) 16.dp else 12.dp,
                        vertical = if (actualIsTablet) 10.dp else 6.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontFamily = alifbaFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (actualIsTablet) 22.sp else 16.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                if (isComingSoon) {
                    Spacer(modifier = Modifier.height(if (actualIsTablet) 8.dp else 6.dp))
                    Text(
                        text = "COMING SOON",
                        fontFamily = alifbaFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (actualIsTablet) 14.sp else 10.sp,
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(if (actualIsTablet) 6.dp else 4.dp))
                            .background(Color(0xFFE67E22))
                            .padding(
                                horizontal = if (actualIsTablet) 10.dp else 6.dp,
                                vertical = if (actualIsTablet) 4.dp else 2.dp
                            )
                    )
                }
            }
        }
    }
}
