package com.alifba.alifba.presenation.stories

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import com.alifba.alifba.ui_components.widgets.PullToRefreshLazyColumn
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
import androidx.compose.ui.platform.LocalConfiguration
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


@Composable
fun StoriesScreen(
    storiesViewModel: StoriesViewModel = hiltViewModel(),
    onStoryClick: (Story) -> Unit = {},
    onMoreClick: () -> Unit = {},
    onProphetMuhammadStoryClick: (Story) -> Unit = {},
    onSahabaStoryClick: (Story) -> Unit = {},
    onSahabaMoreClick: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600
    
    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )
    
    // Observe stories from ViewModel
    val stories by storiesViewModel.stories.collectAsState()
    val isLoading by storiesViewModel.isLoading.collectAsState()
    val error by storiesViewModel.error.collectAsState()
    
    // Observe Prophet Muhammad stories from ViewModel
    val prophetMuhammadStories by storiesViewModel.prophetMuhammadStories.collectAsState()
    val isProphetMuhammadLoading by storiesViewModel.isProphetMuhammadLoading.collectAsState()
    
    // Observe Sahaba stories from ViewModel
    val sahabaStories by storiesViewModel.sahabaStories.collectAsState()
    val isSahabaLoading by storiesViewModel.isSahabaLoading.collectAsState()
    
    // Track refresh state for pull-to-refresh
    var isRefreshing by remember { mutableStateOf(false) }
    
    // Handle pull-to-refresh
    val handleRefresh = {
        isRefreshing = true
        storiesViewModel.forceRefreshAll()
    }
    
    // Reset refresh state when all loading states are false
    LaunchedEffect(isLoading, isProphetMuhammadLoading, isSahabaLoading) {
        if (!isLoading && !isProphetMuhammadLoading && !isSahabaLoading) {
            isRefreshing = false
        }
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
        
        PullToRefreshLazyColumn(
            isRefreshing = isRefreshing,
            onRefresh = handleRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp, bottom = 24.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Title row with "Stories" and "More >"
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isTablet) 32.dp else 24.dp, vertical = if (isTablet) 20.dp else 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stories of prophets",
                        fontFamily = alifbaFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isTablet) 32.sp else 24.sp,
                        color = Color.White
                    )
                    
                    Text(
                        text = "More >",
                        fontFamily = alifbaFont,
                        fontWeight = FontWeight.Normal,
                        fontSize = if (isTablet) 22.sp else 16.sp,
                        color = Color.White,
                        modifier = Modifier.clickable { 
                            SoundEffectManager.playClickSound()
                            onMoreClick() 
                        }
                    )
                }
            }
            
            // Stories grid
            item {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Loading stories from Firebase...", fontFamily = alifbaFont, color = Color.White)
                        }
                    }
                    error != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Error: $error", fontFamily = alifbaFont, color = Color.Red)
                                Text("Retry", 
                                    fontFamily = alifbaFont,
                                    color = lightPurple,
                                    modifier = Modifier.clickable { storiesViewModel.forceRefreshStories() }
                                )
                            }
                        }
                    }
                    stories.isEmpty() && !isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No stories found", fontFamily = alifbaFont, color = Color.White)
                                Text("Retry", 
                                    fontFamily = alifbaFont,
                                    color = lightPurple,
                                    modifier = Modifier.clickable { storiesViewModel.forceRefreshStories() }
                                )
                            }
                        }
                    }
                    else -> {
                        val displayedStories = stories.take(4)
                        // Create a custom grid layout instead of LazyVerticalGrid
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // First row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (displayedStories.isNotEmpty()) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        StoryCard(
                                            story = displayedStories[0],
                                            onClick = {
                                                SoundEffectManager.playClickSound()
                                                onStoryClick(displayedStories[0])
                                            },
                                            alifbaFont = alifbaFont,
                                            isTablet = isTablet
                                        )
                                    }
                                }
                                if (displayedStories.size > 1) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        StoryCard(
                                            story = displayedStories[1],
                                            onClick = {
                                                SoundEffectManager.playClickSound()
                                                onStoryClick(displayedStories[1])
                                            },
                                            alifbaFont = alifbaFont,
                                            isTablet = isTablet
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            
                            // Second row
                            if (displayedStories.size > 2) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        StoryCard(
                                            story = displayedStories[2],
                                            onClick = {
                                                SoundEffectManager.playClickSound()
                                                onStoryClick(displayedStories[2])
                                            },
                                            alifbaFont = alifbaFont,
                                            isTablet = isTablet
                                        )
                                    }
                                    if (displayedStories.size > 3) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            StoryCard(
                                                story = displayedStories[3],
                                                onClick = {
                                                    SoundEffectManager.playClickSound()
                                                    onStoryClick(displayedStories[3])
                                                },
                                                alifbaFont = alifbaFont,
                                                isTablet = isTablet
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Prophet Muhammad Stories Section
            item {
                ProphetMuhammadStoriesSection(
                    stories = prophetMuhammadStories,
                    isLoading = isProphetMuhammadLoading,
                    onStoryClick = onProphetMuhammadStoryClick,
                    alifbaFont = alifbaFont,
                    isTablet = isTablet
                )
            }
            
            // Sahaba Stories Section (Below Prophet Muhammad stories)
            item {
                SahabaStoriesSection(
                    stories = sahabaStories,
                    isLoading = isSahabaLoading,
                    onStoryClick = onSahabaStoryClick,
                    onMoreClick = onSahabaMoreClick,
                    alifbaFont = alifbaFont,
                    isTablet = isTablet
                )
            }
        }
    }
}

@Composable
fun AllStoriesScreen(
    storiesViewModel: StoriesViewModel = hiltViewModel(),
    onStoryClick: (Story) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )
    
    // Observe stories from ViewModel
    val stories by storiesViewModel.stories.collectAsState()
    val isLoading by storiesViewModel.isLoading.collectAsState()
    val error by storiesViewModel.error.collectAsState()

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
                .padding(top = 24.dp)
        ) {
            // Header with back button and title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                Image(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { 
                            SoundEffectManager.playClickSound()
                            onBackClick() 
                        }
                )
                
                // Centered title
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Stories of prophets",
                        fontFamily = alifbaFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }
            }
            
            // Stories grid
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Loading stories from Firebase...", fontFamily = alifbaFont, color = Color.White)
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
                                modifier = Modifier.clickable { storiesViewModel.forceRefreshStories() }
                            )
                        }
                    }
                }
                stories.isEmpty() && !isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No stories found", fontFamily = alifbaFont, color = Color.White)
                            Text("Retry", 
                                fontFamily = alifbaFont,
                                color = lightPurple,
                                modifier = Modifier.clickable { storiesViewModel.forceRefreshStories() }
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
                        items(stories.size) { index ->
                            val story = stories[index]
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
fun AllProphetMuhammadStoriesScreen(
    storiesViewModel: StoriesViewModel = hiltViewModel(),
    onStoryClick: (Story) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )
    
    // Observe Prophet Muhammad stories from ViewModel
    val stories by storiesViewModel.prophetMuhammadStories.collectAsState()
    val isLoading by storiesViewModel.isProphetMuhammadLoading.collectAsState()
    val error by storiesViewModel.error.collectAsState()

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
                .padding(top = 24.dp)
        ) {
            // Header with back button and title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                Image(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { 
                            SoundEffectManager.playClickSound()
                            onBackClick() 
                        }
                )
                
                // Centered title
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Stories of Prophet Muhammad",
                        fontFamily = alifbaFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }
            }
            
            // Stories grid
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Loading Prophet Muhammad stories...", fontFamily = alifbaFont, color = Color.White)
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
                                modifier = Modifier.clickable { storiesViewModel.forceRefreshProphetMuhammadStories() }
                            )
                        }
                    }
                }
                stories.isEmpty() && !isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No Prophet Muhammad stories found", fontFamily = alifbaFont, color = Color.White)
                            Text("Retry", 
                                fontFamily = alifbaFont,
                                color = lightPurple,
                                modifier = Modifier.clickable { storiesViewModel.forceRefreshProphetMuhammadStories() }
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
                        items(stories.size) { index ->
                            val story = stories[index]
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
fun AllSahabaStoriesScreen(
    storiesViewModel: StoriesViewModel = hiltViewModel(),
    onStoryClick: (Story) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )
    
    // Observe Sahaba stories from ViewModel
    val stories by storiesViewModel.sahabaStories.collectAsState()
    val isLoading by storiesViewModel.isSahabaLoading.collectAsState()
    val error by storiesViewModel.error.collectAsState()

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
                .padding(top = 24.dp)
        ) {
            // Header with back button and title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                Image(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { 
                            SoundEffectManager.playClickSound()
                            onBackClick() 
                        }
                )
                
                // Centered title
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sahaba Stories",
                        fontFamily = alifbaFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }
            }
            
            // Stories grid
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Loading Sahaba stories...", fontFamily = alifbaFont, color = Color.White)
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
                                modifier = Modifier.clickable { storiesViewModel.forceRefreshSahabaStories() }
                            )
                        }
                    }
                }
                stories.isEmpty() && !isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No Sahaba stories found", fontFamily = alifbaFont, color = Color.White)
                            Text("Retry", 
                                fontFamily = alifbaFont,
                                color = lightPurple,
                                modifier = Modifier.clickable { storiesViewModel.forceRefreshSahabaStories() }
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
                        items(stories.size) { index ->
                            val story = stories[index]
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
fun CarouselStoryCard(
    story: Story,
    onClick: () -> Unit,
    alifbaFont: FontFamily,
    isCenter: Boolean = false,
    isTablet: Boolean = false
) {
    var imageState by remember { mutableStateOf<AsyncImagePainter.State?>(null) }
    
    // Animate card size transitions for smooth scaling
    val animatedCardWidth by animateDpAsState(
        targetValue = if (isTablet) {
            if (isCenter) 400.dp else 280.dp
        } else {
            if (isCenter) 280.dp else 200.dp
        },
        animationSpec = tween(durationMillis = 300),
        label = "cardWidth"
    )
    val animatedCardHeight by animateDpAsState(
        targetValue = if (isTablet) {
            if (isCenter) 260.dp else 200.dp
        } else {
            if (isCenter) 180.dp else 140.dp
        },
        animationSpec = tween(durationMillis = 300),
        label = "cardHeight"
    )
    
    Box(
        modifier = Modifier
            .width(animatedCardWidth)
            .height(animatedCardHeight)
            .padding(horizontal = 4.dp) // Reduced padding for tighter spacing
            .clickable { onClick() }
    ) {
        // Main story image
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Shimmer background for loading state
            if (imageState == null || imageState is AsyncImagePainter.State.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
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
                    .clip(RoundedCornerShape(20.dp)),
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
                            RoundedCornerShape(20.dp)
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
            
            // Free overlay for free stories (top left corner)
            if (story.status == "free" && imageState != null && imageState !is AsyncImagePainter.State.Loading) {
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.TopStart)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.free),
                        contentDescription = "Free",
                        modifier = Modifier.size(32.dp) // Full 64dp visible size
                    )
                }
            }
        }
        
        // White background text area at the bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isTablet) 70.dp else 50.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Color.White,
                    RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = story.name,
                fontFamily = alifbaFont,
                fontWeight = FontWeight.Bold,
                fontSize = if (isTablet) {
                    if (isCenter) 20.sp else 16.sp
                } else {
                    if (isCenter) 14.sp else 12.sp
                },
                color = Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.padding(
                    horizontal = if (isTablet) 12.dp else 8.dp, 
                    vertical = if (isTablet) 8.dp else 4.dp
                )
            )
        }
    }
}

@Composable
fun ProphetMuhammadStoriesSection(
    stories: List<Story>,
    isLoading: Boolean,
    onStoryClick: (Story) -> Unit,
    alifbaFont: FontFamily,
    isTablet: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Title
        Text(
            text = "Stories of Prophet Muhammad",
            fontFamily = alifbaFont,
            fontWeight = FontWeight.Bold,
            fontSize = if (isTablet) 28.sp else 20.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isTablet) 32.dp else 24.dp, vertical = if (isTablet) 12.dp else 8.dp)
                .padding(bottom = if (isTablet) 24.dp else 16.dp)
        )
        
        // Carousel
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading...", fontFamily = alifbaFont, color = Color.White)
            }
        } else if (stories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No stories available", fontFamily = alifbaFont, color = Color.White)
            }
        } else {
            val lazyListState = rememberLazyListState()
            
            // Center detection - find the item closest to viewport center
            val centerIndex by remember {
                derivedStateOf {
                    val layoutInfo = lazyListState.layoutInfo
                    val visibleItems = layoutInfo.visibleItemsInfo
                    
                    if (visibleItems.isEmpty()) return@derivedStateOf -1
                    
                    // Calculate viewport center
                    val viewportStart = layoutInfo.viewportStartOffset
                    val viewportEnd = layoutInfo.viewportEndOffset
                    val viewportCenter = (viewportStart + viewportEnd) / 2
                    
                    // Find the item whose center is closest to viewport center
                    var closestIndex = -1
                    var minDistance = Float.MAX_VALUE
                    
                    for (item in visibleItems) {
                        val itemStart = item.offset.toFloat()
                        val itemEnd = (item.offset + item.size).toFloat()
                        val itemCenter = (itemStart + itemEnd) / 2f
                        val distance = kotlin.math.abs(itemCenter - viewportCenter.toFloat())
                        
                        if (distance < minDistance) {
                            minDistance = distance
                            closestIndex = item.index
                        }
                    }
                    
                    closestIndex
                }
            }
            
            // Carousel with center focus and smaller gaps
            LazyRow(
                state = lazyListState,
                contentPadding = PaddingValues(horizontal = if (isTablet) 120.dp else 80.dp),
                horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Infinite scroll effect by repeating the list
                items(stories.size * 1000) { index ->
                    val actualIndex = index % stories.size
                    val story = stories[actualIndex]
                    val isCenter = index == centerIndex
                    
                    CarouselStoryCard(
                        story = story,
                        onClick = {
                            SoundEffectManager.playClickSound()
                            onStoryClick(story)
                        },
                        alifbaFont = alifbaFont,
                        isCenter = isCenter,
                        isTablet = isTablet
                    )
                }
            }
            
            // Start at middle of infinite list to allow scrolling both ways
            LaunchedEffect(stories) {
                if (stories.isNotEmpty()) {
                    lazyListState.scrollToItem(stories.size * 500) // Start in middle of infinite list
                }
            }
        }
    }
}

@Composable
fun SahabaStoriesSection(
    stories: List<Story>,
    isLoading: Boolean,
    onStoryClick: (Story) -> Unit,
    onMoreClick: () -> Unit,
    alifbaFont: FontFamily,
    isTablet: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Title row with "Sahaba Stories" and "More >"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sahaba Stories",
                fontFamily = alifbaFont,
                fontWeight = FontWeight.Bold,
                fontSize = if (isTablet) 28.sp else 20.sp,
                color = Color.White
            )
            
            Text(
                text = "More >",
                fontFamily = alifbaFont,
                fontWeight = FontWeight.Normal,
                fontSize = if (isTablet) 22.sp else 16.sp,
                color = Color.White,
                modifier = Modifier.clickable { 
                    SoundEffectManager.playClickSound()
                    onMoreClick() 
                }
            )
        }
        
        // 2x2 Grid
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading Sahaba stories...", fontFamily = alifbaFont, color = Color.White)
                }
            }
            stories.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Sahaba stories found", fontFamily = alifbaFont, color = Color.White)
                }
            }
            else -> {
                val displayedStories = stories.take(4) // Limit to 4 stories for 2x2 grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // First row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (displayedStories.isNotEmpty()) {
                            Box(modifier = Modifier.weight(1f)) {
                                StoryCard(
                                    story = displayedStories[0],
                                    onClick = {
                                        SoundEffectManager.playClickSound()
                                        onStoryClick(displayedStories[0])
                                    },
                                    alifbaFont = alifbaFont,
                                    isTablet = isTablet
                                )
                            }
                        }
                        if (displayedStories.size > 1) {
                            Box(modifier = Modifier.weight(1f)) {
                                StoryCard(
                                    story = displayedStories[1],
                                    onClick = {
                                        SoundEffectManager.playClickSound()
                                        onStoryClick(displayedStories[1])
                                    },
                                    alifbaFont = alifbaFont,
                                    isTablet = isTablet
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    
                    // Second row
                    if (displayedStories.size > 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                StoryCard(
                                    story = displayedStories[2],
                                    onClick = {
                                        SoundEffectManager.playClickSound()
                                        onStoryClick(displayedStories[2])
                                    },
                                    alifbaFont = alifbaFont,
                                    isTablet = isTablet
                                )
                            }
                            if (displayedStories.size > 3) {
                                Box(modifier = Modifier.weight(1f)) {
                                    StoryCard(
                                        story = displayedStories[3],
                                        onClick = {
                                            SoundEffectManager.playClickSound()
                                            onStoryClick(displayedStories[3])
                                        },
                                        alifbaFont = alifbaFont,
                                        isTablet = isTablet
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun StoryCard(
    story: Story,
    onClick: () -> Unit,
    alifbaFont: FontFamily,
    isTablet: Boolean = false
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
                    .clip(RoundedCornerShape(if (isTablet) 24.dp else 16.dp)),
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
                            RoundedCornerShape(if (isTablet) 24.dp else 16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.padlock),
                        contentDescription = "Locked",
                        modifier = Modifier.size(if (isTablet) 48.dp else 32.dp)
                    )
                }
            }
            
            // Free overlay for free stories (top left corner)
            if (story.status == "free" && imageState != null && imageState !is AsyncImagePainter.State.Loading) {
                Box(
                    modifier = Modifier
                        .padding(if (isTablet) 12.dp else 8.dp)
                        .align(Alignment.TopStart)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.free),
                        contentDescription = "Free",
                        modifier = Modifier.size(if (isTablet) 48.dp else 32.dp)
                    )
                }
            }
        }
        
        // Text BELOW the image
        Text(
            text = story.name,
            fontFamily = alifbaFont,
            fontWeight = FontWeight.Bold,
            fontSize = if (isTablet) 20.sp else 14.sp,
            color = Color.White,
            textAlign = TextAlign.Start,
            maxLines = 2,
            modifier = Modifier
                .padding(
                    top = if (isTablet) 12.dp else 8.dp, 
                    start = if (isTablet) 8.dp else 4.dp
                )
                .fillMaxWidth()
        )
    }
}