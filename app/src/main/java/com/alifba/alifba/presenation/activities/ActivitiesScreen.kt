package com.alifba.alifba.presenation.activities

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import com.alifba.alifba.R
import com.alifba.alifba.presenation.home.layout.ProfileViewModel

@Composable
fun ActivitiesScreen(
    onShowBottomNav: (Boolean) -> Unit = {},
    profileViewModel: ProfileViewModel
) {
    var showColoringScreen by remember { mutableStateOf(false) }
    val userProfile by profileViewModel.userProfileState.collectAsState()

    // Start and stop the profile listener
    LaunchedEffect(profileViewModel) {
        profileViewModel.startProfileListener()
    }
    DisposableEffect(profileViewModel) {
        onDispose {
            profileViewModel.stopProfileListener()
        }
    }

    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )

    if (showColoringScreen) {
        LaunchedEffect(Unit) {
            onShowBottomNav(false)
        }
        
        ColoringScreen(
            onBackPressed = {
                showColoringScreen = false
                onShowBottomNav(true)
            }
        )
    } else {
        LaunchedEffect(Unit) {
            onShowBottomNav(true)
        }
        
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
                        .padding(horizontal = 24.dp, vertical = 48.dp)
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
                                fontSize = 20.sp,
                                color = Color.Black
                            )
                            Text(
                                text = userProfile?.childName ?: "Loading...",
                                fontFamily = alifbaFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = Color.Black
                            )
                        }
                        
                        Image(
                            painter = painterResource(id = R.drawable.sunny),
                            contentDescription = "Cloudy",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(140.dp)
                                .clip(RoundedCornerShape(8.dp))
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
                            .padding(vertical = 48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Spacer(modifier = Modifier.width(156.dp))
                            Text(
                                text = "Color the Pictures",
                                fontFamily = alifbaFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                ActivityCard(
                                    title = "Popsicle",
                                    description = "Color a delicious popsicle",
                                    iconRes = R.drawable.levelone,
                                    iconColor = Color(0xFFE74C3C),
                                    onClick = { showColoringScreen = true },
                                    alifbaFont = alifbaFont
                                )
                            }
                            item {
                                ActivityCard(
                                    title = "Flower",
                                    description = "Color a beautiful flower",
                                    iconRes = R.drawable.leveltwo,
                                    iconColor = Color(0xFFE74C3C),
                                    onClick = { /* TODO: Navigate to flower coloring */ },
                                    alifbaFont = alifbaFont,
                                    isComingSoon = true
                                )
                            }
                            item {
                                ActivityCard(
                                    title = "Car",
                                    description = "Color a racing car",
                                    iconRes = R.drawable.levelthree,
                                    iconColor = Color(0xFFE74C3C),
                                    onClick = { /* TODO: Navigate to car coloring */ },
                                    alifbaFont = alifbaFont,
                                    isComingSoon = true
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
                            .size(156.dp)
                            .align(Alignment.TopStart)
                            .offset(x = 16.dp, y = (-64).dp)
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
                            .padding(vertical = 48.dp)
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
                                fontSize = 22.sp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(124.dp))
                        }
                        
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                ActivityCard(
                                    title = "Alif",
                                    description = "Learn the letter Alif",
                                    iconRes = R.drawable.levelfour,
                                    iconColor = Color(0xFF3498DB),
                                    onClick = { /* TODO: Navigate to Alif learning */ },
                                    alifbaFont = alifbaFont,
                                    isComingSoon = true
                                )
                            }
                            item {
                                ActivityCard(
                                    title = "Baa",
                                    description = "Learn the letter Baa",
                                    iconRes = R.drawable.levelfive,
                                    iconColor = Color(0xFF3498DB),
                                    onClick = { /* TODO: Navigate to Baa learning */ },
                                    alifbaFont = alifbaFont,
                                    isComingSoon = true
                                )
                            }
                            item {
                                ActivityCard(
                                    title = "Taa",
                                    description = "Learn the letter Taa",
                                    iconRes = R.drawable.levelsix,
                                    iconColor = Color(0xFF3498DB),
                                    onClick = { /* TODO: Navigate to Taa learning */ },
                                    alifbaFont = alifbaFont,
                                    isComingSoon = true
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
                            .size(156.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-16).dp, y = (-64).dp)
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
                            .padding(vertical = 48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Spacer(modifier = Modifier.width(156.dp))
                            Text(
                                text = "Mini Games",
                                fontFamily = alifbaFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                ActivityCard(
                                    title = "Memory Match",
                                    description = "Match Arabic letters",
                                    iconRes = R.drawable.levelseven,
                                    iconColor = Color(0xFF9B59B6),
                                    onClick = { /* TODO: Navigate to memory game */ },
                                    alifbaFont = alifbaFont,
                                    isComingSoon = true
                                )
                            }
                            item {
                                ActivityCard(
                                    title = "Letter Quiz",
                                    description = "Quiz on Arabic letters",
                                    iconRes = R.drawable.leveleight,
                                    iconColor = Color(0xFF9B59B6),
                                    onClick = { /* TODO: Navigate to letter quiz */ },
                                    alifbaFont = alifbaFont,
                                    isComingSoon = true
                                )
                            }
                            item {
                                ActivityCard(
                                    title = "Word Builder",
                                    description = "Build Arabic words",
                                    iconRes = R.drawable.levelnine,
                                    iconColor = Color(0xFF9B59B6),
                                    onClick = { /* TODO: Navigate to word builder */ },
                                    alifbaFont = alifbaFont,
                                    isComingSoon = true
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
                            .size(156.dp)
                            .align(Alignment.TopStart)
                            .offset(x = 16.dp, y = (-64).dp)
                    )
                }
            }
            
            // Footer Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFb6abfe),  // Light purple
                                    Color(0xffd3cdfa)   // Light purple
                                )
                            )
                        )
                        .padding(12.dp)
                ) {
                    // Palm tree - bottom left
                    Image(
                        painter = painterResource(id = R.drawable.palmtree),
                        contentDescription = "Palm Tree",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.BottomStart)
                    )
                    
                    // Camel - bottom right
                    Image(
                        painter = painterResource(id = R.drawable.camel),
                        contentDescription = "Camel",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.BottomEnd)
                    )
                    
                    // Text in center top
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "Keep learning, ${userProfile?.childName ?: "Loading..."}!",
                            fontFamily = alifbaFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Every day is a new adventure! \uD83C\uDF88",
                            fontFamily = alifbaFont,
                            fontSize = 14.sp,
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
    isComingSoon: Boolean = false
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .height(200.dp)
            .clickable(enabled = !isComingSoon) { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 12.dp)
            )
            
            Text(
                text = title,
                fontFamily = alifbaFont,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            
            if (isComingSoon) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "COMING SOON",
                    fontFamily = alifbaFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE67E22))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}