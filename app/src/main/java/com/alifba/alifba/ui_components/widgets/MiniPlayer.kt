package com.alifba.alifba.ui_components.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.alifba.alifba.presenation.stories.AudioPlayerViewModel
import com.alifba.alifba.ui_components.theme.*

@Composable
fun MiniPlayer(
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier,
    audioViewModel: AudioPlayerViewModel = hiltViewModel(),
    isInAudioPlayer: Boolean = false,
    onClose: () -> Unit = {}
) {
    val currentStory by audioViewModel.currentStory.collectAsState()
    val isPlaying by audioViewModel.isPlaying.collectAsState()
    val currentPosition by audioViewModel.currentPosition.collectAsState()
    val duration by audioViewModel.duration.collectAsState()
    
    // Only show mini player if there's a current story and audio is loaded, and not in audio player screen
    if (currentStory != null && duration > 0 && !isInAudioPlayer) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(72.dp) // Simpler, smaller height
                .clickable { onExpandClick() }, // Entire card clickable to expand
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Story thumbnail
                AsyncImage(
                    model = currentStory?.background,
                    contentDescription = "Story thumbnail",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Middle: Story info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = currentStory?.name ?: "Unknown Story",
                        color = darkPurple,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Alifba Stories",
                        color = darkPurple.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Right: Play/Pause button only
                IconButton(
                    onClick = { 
                        audioViewModel.togglePlayPause()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(lightPurple, darkPurple)
                            ),
                            shape = CircleShape
                        )
                ) {
                    if (isPlaying) {
                        // Custom pause icon
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(12.dp)
                                    .background(Color.White, RoundedCornerShape(1.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(12.dp)
                                    .background(Color.White, RoundedCornerShape(1.dp))
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}