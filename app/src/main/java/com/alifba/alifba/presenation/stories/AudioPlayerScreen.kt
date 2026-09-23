package com.alifba.alifba.presenation.stories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin
import kotlin.math.PI
import coil.compose.AsyncImage
import com.alifba.alifba.R
import coil.compose.AsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.CachePolicy
import com.alifba.alifba.data.models.Story
import com.alifba.alifba.ui_components.theme.*
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import android.util.Log
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon

// Play button accent — warm amber/gold instead of purple, consistent with the rest of the
// screen's warm palette (matches the amber already used for completed lesson-path nodes).
private val audioPlayerAmber = Color(0xFFC79A22)

// Bottom band background fallback — used until (or unless) Palette extraction from the story's
// own artwork succeeds; also the permanent value when extraction fails (e.g. image not loaded).
private val audioPlayerBand = Color(0xFF4A2E12)

// Scales a color's RGB down towards black, keeping alpha — used to darken whatever Palette
// swatch we extract so the band stays dark enough for white text regardless of how light the
// source artwork's dominant/muted color happens to be.
private fun darkenColor(color: Color, factor: Float): Color = Color(
    red = color.red * factor,
    green = color.green * factor,
    blue = color.blue * factor,
    alpha = color.alpha
)

// Extracts a dominant/muted swatch from the bitmap at [imageUrl] via the Palette API and returns
// a darkened Color, or null if extraction fails for any reason (image not loaded yet, decode
// error, no usable swatch, etc.) — callers fall back to [audioPlayerBand] on null.
private suspend fun extractDarkenedBandColor(context: android.content.Context, imageUrl: String): Color? {
    if (imageUrl.isBlank()) return null
    return try {
        withContext(Dispatchers.IO) {
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                // Palette needs to read pixels directly — hardware bitmaps can't be read on the
                // CPU, so force a software-backed bitmap for this decode.
                .allowHardware(false)
                .build()
            val result = context.imageLoader.execute(request)
            val bitmap = (result.drawable as? BitmapDrawable)?.bitmap ?: return@withContext null

            val palette = Palette.from(bitmap).generate()
            val swatch = palette.darkMutedSwatch
                ?: palette.dominantSwatch
                ?: palette.mutedSwatch
                ?: palette.lightMutedSwatch
                ?: return@withContext null

            darkenColor(Color(swatch.rgb), factor = 0.55f)
        }
    } catch (e: Exception) {
        Log.w("AudioPlayerScreen", "Palette extraction failed for $imageUrl: ${e.message}")
        null
    }
}

@Composable
fun AudioPlayerScreen(
    story: Story,
    onBackClick: () -> Unit = {},
    onMinimize: () -> Unit = {},
    onCancel: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: AudioPlayerViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    storiesViewModel: StoriesViewModel = androidx.hilt.navigation.compose.hiltViewModel()
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

    // Bottom band color — extracted per story from its own background artwork via the Palette
    // API, darkened for text contrast. Resets to the neutral fallback immediately when the story
    // changes (rather than holding the previous story's color) and is replaced once extraction
    // for the new artwork finishes; stays on the fallback if extraction fails or never resolves.
    val context = LocalContext.current
    var bandColor by remember(story.documentId) { mutableStateOf(audioPlayerBand) }
    LaunchedEffect(story.documentId, story.background) {
        val extracted = extractDarkenedBandColor(context, story.background)
        if (extracted != null) {
            bandColor = extracted
        }
    }

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
        
        // Title, "Story" label, scrubber, and controls all moved into the solid bottom band
        // below (inside the AnimatedVisibility block) — no more text floating directly on the
        // illustration, so contrast no longer depends on what's in the artwork underneath.

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

                    // Right: Favorite & Cancel buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Favorite button — same chunky pressable 3D effect as the story cards
                        // (static shadow disc behind, main disc on top that compresses onto it
                        // on press), replacing the old flat translucent circle.
                        val isFavorite by storiesViewModel
                            .isFavorite(story.documentId, story.category.ifBlank { "stories" })
                            .collectAsState()
                        val heartScale = remember { Animatable(1f) }
                        val coroutineScope = rememberCoroutineScope()
                        val heartInteractionSource = remember { MutableInteractionSource() }
                        val isHeartPressed by heartInteractionSource.collectIsPressedAsState()
                        val heartOffsetY by animateDpAsState(
                            targetValue = if (isHeartPressed) 0.dp else 3.dp,
                            label = "HeartOffsetY"
                        )
                        val heartButtonSize = if (isTablet) 56.dp else 40.dp

                        Box(
                            modifier = Modifier
                                .size(heartButtonSize)
                                .graphicsLayer {
                                    scaleX = heartScale.value
                                    scaleY = heartScale.value
                                }
                        ) {
                            // Static shadow disc.
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.3f))
                            )
                            // Main disc — compresses onto the shadow layer on press.
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .padding(bottom = heartOffsetY)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f))
                                    .clickable(
                                        interactionSource = heartInteractionSource,
                                        indication = null
                                    ) {
                                        // Bounce animation
                                        coroutineScope.launch {
                                            heartScale.animateTo(0.7f, animationSpec = tween(100))
                                            heartScale.animateTo(1.3f, animationSpec = tween(100))
                                            heartScale.animateTo(1f, animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            ))
                                        }
                                        storiesViewModel.toggleFavorite(
                                            story.documentId,
                                            story.category.ifBlank { "stories" }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                    tint = if (isFavorite) Color.Red else Color.White,
                                    modifier = Modifier.size(if (isTablet) 32.dp else 24.dp)
                                )
                            }
                        }

                        // Cancel/Stop button
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
                }
                
                Spacer(modifier = Modifier.weight(1f))

                // Solid bottom band — title, "Story" label, scrubber, and controls all live
                // here now instead of floating directly on the illustration, so their contrast
                // is guaranteed regardless of the artwork underneath.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bandColor, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (isTablet) 28.dp else 20.dp, bottom = if (isTablet) 40.dp else 28.dp)
                ) {
                    Text(
                        text = story.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = if (isTablet) 48.dp else 32.dp),
                        color = Color.White,
                        fontSize = if (isTablet) 32.sp else 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(if (isTablet) 8.dp else 4.dp))

                    Text(
                        text = if (story.isBedtime) "Bedtime Story" else "Story",
                        modifier = Modifier.fillMaxWidth().padding(horizontal = if (isTablet) 48.dp else 32.dp),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = if (isTablet) 20.sp else 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(if (isTablet) 20.dp else 12.dp))

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
                
                // Wavy Seeker/Progress Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isTablet) 48.dp else 32.dp)
                ) {
                    // Wavy progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isTablet) 80.dp else 60.dp)
                    ) {
                        val progress = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f

                        // Draw wavy progress
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isTablet) 60.dp else 40.dp)
                                .align(Alignment.Center)
                        ) {
                            val width = size.width
                            val height = size.height
                            val centerY = height / 2
                            val amplitude = if (isTablet) 12f else 8f // Wave height (slightly reduced)
                            val wavelength = if (isTablet) 200f else 150f // Distance between wave peaks (increased = fewer waves)
                            val strokeWidth = if (isTablet) 8f else 6f

                            // Draw inactive wavy track (full width, white)
                            val inactivePath = Path()
                            var x = 0f
                            inactivePath.moveTo(0f, centerY)

                            while (x <= width) {
                                val y = centerY + amplitude * sin((x / wavelength) * 2 * PI).toFloat()
                                inactivePath.lineTo(x, y)
                                x += 2f
                            }

                            drawPath(
                                path = inactivePath,
                                color = Color.White.copy(alpha = 0.3f),
                                style = Stroke(
                                    width = strokeWidth,
                                    cap = StrokeCap.Round
                                )
                            )

                            // Draw active wavy track (progress width, gradient)
                            if (progress > 0f) {
                                val activePath = Path()
                                x = 0f
                                val progressWidth = width * progress
                                activePath.moveTo(0f, centerY)

                                while (x <= progressWidth) {
                                    val y = centerY + amplitude * sin((x / wavelength) * 2 * PI).toFloat()
                                    activePath.lineTo(x, y)
                                    x += 2f
                                }

                                // Draw with gradient effect (using multiple colors)
                                drawPath(
                                    path = activePath,
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            lightPurple,
                                            mediumpurple,
                                            darkPurple
                                        ),
                                        startX = 0f,
                                        endX = progressWidth
                                    ),
                                    style = Stroke(
                                        width = strokeWidth + 2f,
                                        cap = StrokeCap.Round
                                    )
                                )

                                // Add glow effect
                                drawPath(
                                    path = activePath,
                                    color = lightPurple.copy(alpha = 0.4f),
                                    style = Stroke(
                                        width = strokeWidth + 8f,
                                        cap = StrokeCap.Round
                                    )
                                )
                            }
                        }

                        // Interactive slider overlay (invisible but captures touch)
                        Slider(
                            value = if (duration > 0) currentPosition.toFloat() else 0f,
                            onValueChange = { newPosition ->
                                viewModel.seekTo(newPosition.toLong())
                            },
                            valueRange = 0f..duration.toFloat(),
                            modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                            enabled = duration > 0 && !isLoading,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White.copy(alpha = 0.9f),
                                activeTrackColor = Color.Transparent,
                                inactiveTrackColor = Color.Transparent,
                                activeTickColor = Color.Transparent,
                                inactiveTickColor = Color.Transparent
                            )
                        )
                    }
                    
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
                    // Bounce animation for skip back button
                    val skipBackScale = remember { Animatable(1f) }
                    val coroutineScope = rememberCoroutineScope()

                    // 10 seconds back
                    IconButton(
                        onClick = {
                            viewModel.skipBackward()
                            // Trigger bounce animation
                            coroutineScope.launch {
                                skipBackScale.animateTo(0.85f, animationSpec = tween(50))
                                skipBackScale.animateTo(1.1f, animationSpec = tween(100))
                                skipBackScale.animateTo(1f, animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ))
                            }
                        },
                        enabled = duration > 0 && !isLoading,
                        modifier = Modifier
                            .size(if (isTablet) 80.dp else 56.dp)
                            .graphicsLayer {
                                scaleX = skipBackScale.value
                                scaleY = skipBackScale.value
                            }
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

                    // Pulse animation for play button when playing
                    val playButtonScale by animateFloatAsState(
                        targetValue = if (isPlaying && !isLoading) 1.08f else 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "play_pulse"
                    )

                    // Play/Pause Button
                    IconButton(
                        onClick = { viewModel.togglePlayPause() },
                        enabled = duration > 0 && !isLoading,
                        modifier = Modifier
                            .size(if (isTablet) 100.dp else 72.dp)
                            .graphicsLayer {
                                scaleX = playButtonScale
                                scaleY = playButtonScale
                            }
                            .background(
                                color = if (duration > 0 && !isLoading) Color.White else Color.White.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                    ) {
                        when {
                            isLoading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(if (isTablet) 48.dp else 32.dp),
                                    color = audioPlayerAmber,
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
                                            .background(audioPlayerAmber, RoundedCornerShape(if (isTablet) 3.dp else 2.dp))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(if (isTablet) 8.dp else 6.dp)
                                            .height(if (isTablet) 36.dp else 24.dp)
                                            .background(audioPlayerAmber, RoundedCornerShape(if (isTablet) 3.dp else 2.dp))
                                    )
                                }
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = audioPlayerAmber,
                                    modifier = Modifier.size(if (isTablet) 48.dp else 32.dp)
                                )
                            }
                        }
                    }

                    // Bounce animation for skip forward button
                    val skipForwardScale = remember { Animatable(1f) }

                    // 10 seconds forward
                    IconButton(
                        onClick = {
                            viewModel.skipForward()
                            // Trigger bounce animation
                            coroutineScope.launch {
                                skipForwardScale.animateTo(0.85f, animationSpec = tween(50))
                                skipForwardScale.animateTo(1.1f, animationSpec = tween(100))
                                skipForwardScale.animateTo(1f, animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ))
                            }
                        },
                        enabled = duration > 0 && !isLoading,
                        modifier = Modifier
                            .size(if (isTablet) 80.dp else 56.dp)
                            .graphicsLayer {
                                scaleX = skipForwardScale.value
                                scaleY = skipForwardScale.value
                            }
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
                }
                }
            }
        }
    }
}
