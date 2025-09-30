package com.alifba.alifba.presenation.stories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.alifba.alifba.R
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import coil.request.CachePolicy
import com.alifba.alifba.data.models.Story
import com.alifba.alifba.ui_components.theme.*
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import android.util.Log

@Composable
fun AudioPlayerScreen(
    story: Story,
    onBackClick: () -> Unit = {},
    onMinimize: () -> Unit = {},
    onCancel: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: AudioPlayerViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600
    
    // Collect states from ViewModel
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Controls visibility state
    var showControls by remember { mutableStateOf(true) }
    
    // Auto-hide controls logic
    LaunchedEffect(isPlaying, showControls) {
        if (isPlaying && showControls && !isLoading) {
            delay(3000) // Wait 3 seconds
            showControls = false
        }
    }
    
    // Load story when screen opens
    LaunchedEffect(story) {
        viewModel.loadStory(story)
    }
    
    // Background panning animation - slow left to right and right to left
    val infiniteTransition = rememberInfiniteTransition(label = "background_pan")
    val panOffset by infiniteTransition.animateFloat(
        initialValue = -0.1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pan_offset"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // No ripple effect
            ) {
                showControls = true
            }
    ) {
        // Background image with panning animation
        AsyncImage(
            model = story.background,
            contentDescription = "Background",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Apply horizontal panning only when audio is playing
                    if (isPlaying) {
                        translationX = size.width * panOffset
                        scaleX = 1.2f // Scale up slightly to avoid edges showing during pan
                        scaleY = 1.2f
                    }
                },
            contentScale = ContentScale.Crop
        )
        
        // Dark overlay for better text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )
        
        // Always visible background content
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Story Title - Always visible
            Text(
                text = story.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (isTablet) 48.dp else 32.dp),
                color = Color.White,
                fontSize = if (isTablet) 40.sp else 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(if (isTablet) 12.dp else 8.dp))
            
            // Story Category - Always visible
            Text(
                text = if (story.isBedtime) "Bedtime Story" else "Story",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = if (isTablet) 22.sp else 16.sp,
                textAlign = TextAlign.Center
            )
        }
        
        // Animated Controls Overlay
        AnimatedVisibility(
            visible = showControls || !isPlaying || isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar with Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(if (isTablet) 24.dp else 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Minimize (Drop Arrow)
                    IconButton(
                        onClick = onMinimize,
                        modifier = Modifier
                            .size(if (isTablet) 56.dp else 40.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Minimize",
                            tint = Color.White,
                            modifier = Modifier.size(if (isTablet) 32.dp else 24.dp)
                        )
                    }
                    
                    // Center: Title
                    Text(
                        text = "Now Playing",
                        color = Color.White,
                        fontSize = if (isTablet) 24.sp else 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    // Right: Cancel/Stop
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .size(if (isTablet) 56.dp else 40.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Stop Playing",
                            tint = Color.White,
                            modifier = Modifier.size(if (isTablet) 32.dp else 24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Show loading or error state
                when {
                    isLoading -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(if (isTablet) 36.dp else 24.dp),
                                color = Color.White.copy(alpha = 0.8f),
                                strokeWidth = if (isTablet) 3.dp else 2.dp
                            )
                            Spacer(modifier = Modifier.height(if (isTablet) 12.dp else 8.dp))
                            Text(
                                text = "Loading audio...",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = if (isTablet) 20.sp else 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(if (isTablet) 48.dp else 32.dp))
                    }
                    error != null && !isPlaying && duration == 0L -> {
                        Text(
                            text = "Audio unavailable",
                            color = Color.Red.copy(alpha = 0.8f),
                            fontSize = if (isTablet) 20.sp else 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = if (isTablet) 48.dp else 32.dp)
                        )
                        Spacer(modifier = Modifier.height(if (isTablet) 48.dp else 32.dp))
                    }
                }
                
                // Seeker/Progress Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isTablet) 48.dp else 32.dp)
                ) {
                    Slider(
                        value = if (duration > 0) currentPosition.toFloat() else 0f,
                        onValueChange = { newPosition ->
                            viewModel.seekTo(newPosition.toLong())
                        },
                        valueRange = 0f..duration.toFloat(),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = duration > 0 && !isLoading,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                    
                    // Time Display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = viewModel.formatTime(currentPosition),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = if (isTablet) 18.sp else 12.sp
                        )
                        Text(
                            text = viewModel.formatTime(duration),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = if (isTablet) 18.sp else 12.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Media Control Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isTablet) 48.dp else 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 10 seconds back
                    IconButton(
                        onClick = { viewModel.skipBackward() },
                        enabled = duration > 0 && !isLoading,
                        modifier = Modifier
                            .size(if (isTablet) 80.dp else 56.dp)
                            .background(
                                color = Color.White.copy(alpha = if (duration > 0 && !isLoading) 0.2f else 0.1f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.tensecbackward),
                            contentDescription = "Skip 10 seconds back",
                            tint = Color.White.copy(alpha = if (duration > 0 && !isLoading) 1f else 0.5f),
                            modifier = Modifier.size(if (isTablet) 36.dp else 24.dp)
                        )
                    }
                    
                    // Play/Pause Button
                    IconButton(
                        onClick = { viewModel.togglePlayPause() },
                        enabled = duration > 0 && !isLoading,
                        modifier = Modifier
                            .size(if (isTablet) 100.dp else 72.dp)
                            .background(
                                color = if (duration > 0 && !isLoading) Color.White else Color.White.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                    ) {
                        when {
                            isLoading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(if (isTablet) 48.dp else 32.dp),
                                    color = lightPurple,
                                    strokeWidth = if (isTablet) 4.dp else 3.dp
                                )
                            }
                            isPlaying -> {
                                // Custom pause icon using two vertical bars
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(if (isTablet) 8.dp else 6.dp)
                                            .height(if (isTablet) 36.dp else 24.dp)
                                            .background(lightPurple, RoundedCornerShape(if (isTablet) 3.dp else 2.dp))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(if (isTablet) 8.dp else 6.dp)
                                            .height(if (isTablet) 36.dp else 24.dp)
                                            .background(lightPurple, RoundedCornerShape(if (isTablet) 3.dp else 2.dp))
                                    )
                                }
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = lightPurple,
                                    modifier = Modifier.size(if (isTablet) 48.dp else 32.dp)
                                )
                            }
                        }
                    }
                    
                    // 10 seconds forward
                    IconButton(
                        onClick = { viewModel.skipForward() },
                        enabled = duration > 0 && !isLoading,
                        modifier = Modifier
                            .size(if (isTablet) 80.dp else 56.dp)
                            .background(
                                color = Color.White.copy(alpha = if (duration > 0 && !isLoading) 0.2f else 0.1f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.tensecforward),
                            contentDescription = "Skip 10 seconds forward",
                            tint = Color.White.copy(alpha = if (duration > 0 && !isLoading) 1f else 0.5f),
                            modifier = Modifier.size(if (isTablet) 36.dp else 24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(if (isTablet) 72.dp else 48.dp))
            }
        }
    }
}

