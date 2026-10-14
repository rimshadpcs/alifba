package com.alifba.alifba.ui_components.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.alifba.alifba.presenation.stories.AudioPlayerViewModel
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager

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
    val duration by audioViewModel.duration.collectAsState()
    val k = LessonTheme.scale()

    // Only show mini player if there's a current story and audio is loaded, and not in audio player screen
    if (currentStory != null && duration > 0 && !isInAudioPlayer) {
        val thumbnailUrl = currentStory?.thumbnail?.takeIf { it.isNotBlank() } ?: currentStory?.background
        val shape = RoundedCornerShape(22.dp)
        val edge = LessonTheme.answerEdge

        // Raised white card, like the other cards
        Box(modifier = modifier.fillMaxWidth().padding(bottom = edge)) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(y = edge)
                        .background(LessonTheme.lineEdge, shape)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, shape)
                        .border(2.dp, LessonTheme.line, shape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onExpandClick() }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = thumbnailUrl,
                        contentDescription = "Story thumbnail",
                        modifier = Modifier
                            .size((54 * k).dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentStory?.name ?: "Unknown story",
                            style = LessonTheme.answer().copy(fontSize = (17 * k).sp, textAlign = TextAlign.Start),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Alifba stories",
                            style = LessonTheme.answer().copy(
                                fontSize = (14 * k).sp,
                                color = AuthTheme.secondaryText,
                                textAlign = TextAlign.Start
                            ),
                            maxLines = 1
                        )
                    }

                    // Play / pause: a small raised round button
                    val interaction = remember { MutableInteractionSource() }
                    val pressed by interaction.collectIsPressedAsState()
                    val lift by animateDpAsState(if (pressed) 3.dp else 0.dp, label = "miniLift")
                    Box(
                        modifier = Modifier
                            .padding(bottom = 3.dp)
                            .size((44 * k).dp)
                            .clickable(interactionSource = interaction, indication = null) {
                                SoundEffectManager.playClickSound()
                                audioViewModel.togglePlayPause()
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(y = 3.dp)
                                .background(LessonTheme.primaryEdge, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(y = lift)
                                .background(LessonTheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPlaying) {
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Box(Modifier.width(4.dp).height(15.dp).background(Color.White, RoundedCornerShape(2.dp)))
                                    Box(Modifier.width(4.dp).height(15.dp).background(Color.White, RoundedCornerShape(2.dp)))
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size((26 * k).dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
