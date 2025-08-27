package com.alifba.alifba.presenation.stories

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import com.alifba.alifba.R
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.theme.lightPurple
import com.alifba.alifba.ui_components.theme.mediumpurple
import com.alifba.alifba.ui_components.theme.darkPurple
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager
import com.alifba.alifba.data.models.Story
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.valentinilk.shimmer.shimmer

// Button types for different styles
enum class ButtonType {
    Stories,
    Bedtime
}

@Composable
fun StoriesScreen(
    storiesViewModel: StoriesViewModel = hiltViewModel(),
    onStoryClick: (Story) -> Unit = {}
) {
    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )
    
    // State for selected tab
    var selectedTab by remember { mutableStateOf("Stories") }
    
    // Observe stories from ViewModel
    val stories by storiesViewModel.stories.collectAsState()
    val isLoading by storiesViewModel.isLoading.collectAsState()
    val error by storiesViewModel.error.collectAsState()
    
    // Filter stories based on selected tab
    val filteredStories = remember(selectedTab, stories) {
        Log.d("StoriesScreen", "Total stories: ${stories.size}")
        stories.forEach { story ->
            Log.d("StoriesScreen", "Story: ${story.name}, isBedtime: ${story.isBedtime}, isLocked: ${story.isLocked}")
        }
        
        val filtered = when (selectedTab) {
            "Stories" -> stories.filter { !it.isBedtime }
            "Bedtime Stories" -> stories.filter { it.isBedtime }
            else -> stories
        }
        
        Log.d("StoriesScreen", "Filtered stories for '$selectedTab': ${filtered.size}")
        filtered.forEach { story ->
            Log.d("StoriesScreen", "Filtered story: ${story.name}")
        }
        
        filtered
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.storiesbackground),
            contentDescription = "Stories Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp) // Add top padding for status bar
        ) {
            
            // Tab selector buttons with different purple styles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Stories button - light purple pill style
                StoryTabButton(
                    text = "Stories",
                    isSelected = selectedTab == "Stories",
                    onClick = { 
                        SoundEffectManager.playClickSound()
                        selectedTab = "Stories" 
                    },
                    modifier = Modifier.weight(1f),
                    alifbaFont = alifbaFont,
                    buttonType = ButtonType.Stories
                )
                
                // Bedtime Stories button - dark purple rounded style
                StoryTabButton(
                    text = "Bedtime Stories",
                    isSelected = selectedTab == "Bedtime Stories",
                    onClick = { 
                        SoundEffectManager.playClickSound()
                        selectedTab = "Bedtime Stories" 
                    },
                    modifier = Modifier.weight(1f),
                    alifbaFont = alifbaFont,
                    buttonType = ButtonType.Bedtime
                )
            }
            
            // Stories grid
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Loading stories from Firebase...", fontFamily = alifbaFont)
                            Text("Tab: $selectedTab", fontFamily = alifbaFont, fontSize = 12.sp)
                        }
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Error: $error", fontFamily = alifbaFont, color = Color.Red)
                            Text("Retry", 
                                fontFamily = alifbaFont,
                                color = lightPurple,
                                modifier = Modifier.clickable { storiesViewModel.refreshStories() }
                            )
                        }
                    }
                }
                filteredStories.isEmpty() && !isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No stories found", fontFamily = alifbaFont)
                            Text("Total stories: ${stories.size}", fontFamily = alifbaFont, fontSize = 12.sp)
                            Text("Tab: $selectedTab", fontFamily = alifbaFont, fontSize = 12.sp)
                            Text("Retry", 
                                fontFamily = alifbaFont,
                                color = lightPurple,
                                modifier = Modifier.clickable { storiesViewModel.refreshStories() }
                            )
                        }
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredStories.size) { index ->
                            val story = filteredStories[index]
                            StoryCard(
                                story = story,
                                onClick = {
                                    SoundEffectManager.playClickSound()
                                    onStoryClick(story)
                                },
                                alifbaFont = alifbaFont
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StoryTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    alifbaFont: FontFamily,
    buttonType: ButtonType
) {
    when (buttonType) {
        ButtonType.Stories -> {
            // Transparent with gradient design for Stories
            val backgroundModifier = if (isSelected) {
                Modifier.background(
                    brush = Brush.verticalGradient(
                        colors = listOf(lightPurple, darkPurple)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            } else {
                Modifier.background(
                    color = lightPurple.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                )
            }
            
            Box(
                modifier = modifier
                    .height(48.dp)
                    .then(backgroundModifier)
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    fontFamily = alifbaFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isSelected) white else lightPurple,
                    textAlign = TextAlign.Center
                )
            }
        }
        ButtonType.Bedtime -> {
            // Transparent with gradient design for Bedtime Stories
            val backgroundModifier = if (isSelected) {
                Modifier.background(
                    brush = Brush.verticalGradient(
                        colors = listOf(darkPurple, mediumpurple)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            } else {
                Modifier.background(
                    color = darkPurple.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                )
            }
            
            Box(
                modifier = modifier
                    .height(48.dp)
                    .then(backgroundModifier)
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    fontFamily = alifbaFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isSelected) white else darkPurple,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun StoryCard(
    story: Story,
    onClick: () -> Unit,
    alifbaFont: FontFamily
) {
    var imageState by remember { mutableStateOf<AsyncImagePainter.State?>(null) }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Just the image - no card background
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            // Shimmer background for loading state
            if (imageState == null || imageState is AsyncImagePainter.State.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .shimmer()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    lightPurple.copy(alpha = 0.3f),
                                    mediumpurple.copy(alpha = 0.3f),
                                    darkPurple.copy(alpha = 0.3f)
                                )
                            )
                        )
                )
            }
            
            // Use AsyncImage for loading Firebase images
            AsyncImage(
                model = story.background,
                contentDescription = story.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop,
                onState = { state ->
                    imageState = state
                }
            )
            
            
            // Lock overlay for locked stories
            if (story.isLocked && imageState != null && imageState !is AsyncImagePainter.State.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.padlock),
                        contentDescription = "Locked",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        
        // Text BELOW the image
        Text(
            text = story.name,
            fontFamily = alifbaFont,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.White,
            textAlign = TextAlign.Start,
            maxLines = 2,
            modifier = Modifier
                .padding(top = 8.dp, start = 4.dp)
                .fillMaxWidth()
        )
    }
}