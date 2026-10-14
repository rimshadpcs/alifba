package com.alifba.alifba.presenation.stories

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.alifba.alifba.data.models.Story
import com.alifba.alifba.ui_components.widgets.AuthTheme
import com.alifba.alifba.ui_components.widgets.LessonTheme
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.sin

/**
 * Day / night look of the player. Follows the time of day (night from 7pm to 6am); the switch on
 * the player overrides it until the app is closed.
 */
object PlayerAppearance {
    var override by mutableStateOf<Boolean?>(null)

    fun isNightByClock(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour >= 19 || hour < 6
    }
}

private object PlayerPalette {
    val dayBackground = Color(0xFFFDF3DE)
    val dayEdge = Color(0xFFEBE4C4)
    val dayTrack = Color(0xFFE9E1C8)
    val nightBackground = Color(0xFF212B58)
    val nightFrame = Color(0xFF383E66)
    val nightMuted = Color(0xFFB8BDF0)
    val nightTrack = Color(0xFF4A5078)
    val amber = Color(0xFFFFC83D)
    val amberEdge = Color(0xFFE0A800)
    val amberInk = Color(0xFF3B2F00)
}

@Composable
fun AudioPlayerScreen(
    story: Story,
    onBackClick: () -> Unit = {},
    onMinimize: () -> Unit = {},
    onCancel: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: AudioPlayerViewModel = hiltViewModel(),
    storiesViewModel: StoriesViewModel = hiltViewModel()
) {
    val k = LessonTheme.scale()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isFavorite by storiesViewModel
        .isFavorite(story.documentId, story.category.ifBlank { "stories" })
        .collectAsState()

    val night = PlayerAppearance.override ?: remember { PlayerAppearance.isNightByClock() }
    val isReady = duration > 0 && !isLoading

    val primaryText = if (night) Color.White else LessonTheme.ink
    val secondaryText = if (night) PlayerPalette.nightMuted else AuthTheme.secondaryText
    val background by animateColorAsState(
        if (night) PlayerPalette.nightBackground else PlayerPalette.dayBackground,
        label = "playerBackground"
    )

    // Load story when screen opens
    LaunchedEffect(story) {
        viewModel.loadStory(story)
    }

    val screenWidth = LocalConfiguration.current.screenWidthDp
    val coverSize = minOf((screenWidth - 56).dp, (400 * k).dp)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background)
    ) {
        if (night) NightStars()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top bar: minimise, title, heart, stop
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PlayerRoundButton(
                        icon = Icons.Filled.KeyboardArrowDown,
                        description = "Minimize",
                        tint = if (night) Color.White else LessonTheme.primary,
                        night = night,
                        onClick = onMinimize
                    )
                    Text(
                        text = "Now playing",
                        style = LessonTheme.answer().copy(fontSize = (18 * k).sp, color = secondaryText),
                        modifier = Modifier.weight(1f)
                    )
                    PlayerRoundButton(
                        icon = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        description = if (isFavorite) "Remove from favourites" else "Add to favourites",
                        tint = if (isFavorite) LessonTheme.wrong else if (night) Color.White else Color(0xFFA3A9BC),
                        night = night,
                        onClick = {
                            storiesViewModel.toggleFavorite(story.documentId, story.category.ifBlank { "stories" })
                        }
                    )
                    PlayerRoundButton(
                        icon = Icons.Filled.Close,
                        description = "Stop playing",
                        tint = if (night) Color.White else LessonTheme.primary,
                        night = night,
                        onClick = onCancel
                    )
                }

                // Cover
                Box(modifier = Modifier.padding(top = 22.dp)) {
                    Cover(story = story, size = coverSize, night = night)
                }

                Text(
                    text = story.name,
                    style = LessonTheme.question().copy(fontSize = (30 * k).sp, color = primaryText),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 28.dp, end = 28.dp, top = 26.dp)
                )

                val kind = if (story.isBedtime) "Bedtime story" else "Story"
                val totalMs = if (duration > 0) duration else story.durationSeconds * 1000L
                Text(
                    text = if (totalMs > 0) "$kind · ${storyTimeText(totalMs)}" else kind,
                    style = LessonTheme.answer().copy(fontSize = (17 * k).sp, color = secondaryText),
                    modifier = Modifier.padding(top = 6.dp)
                )

                if (error != null && !isPlaying && duration == 0L) {
                    Text(
                        text = "Audio unavailable",
                        style = LessonTheme.answer().copy(
                            fontSize = (15 * k).sp,
                            color = if (night) Color(0xFFFF9A9F) else LessonTheme.wrongEdge
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Wavy progress + time labels
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, end = 32.dp, top = 26.dp)
                ) {
                    WavyProgress(
                        progress = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f,
                        enabled = isReady,
                        activeColor = if (night) PlayerPalette.amber else LessonTheme.primary,
                        trackColor = if (night) PlayerPalette.nightTrack else PlayerPalette.dayTrack,
                        thumbColor = if (night) Color.White else LessonTheme.primaryEdge,
                        onSeek = { fraction -> viewModel.seekTo((fraction * duration).toLong()) }
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = storyTimeText(currentPosition),
                            style = LessonTheme.answer().copy(fontSize = (15 * k).sp, color = secondaryText)
                        )
                        Text(
                            text = "-" + storyTimeText((duration - currentPosition).coerceAtLeast(0L)),
                            style = LessonTheme.answer().copy(fontSize = (15 * k).sp, color = secondaryText)
                        )
                    }
                }

                // Controls
                Row(
                    modifier = Modifier.padding(top = 22.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((34 * k).dp)
                ) {
                    PlayerRoundButton(
                        icon = Icons.Filled.Replay10,
                        description = "Back 10 seconds",
                        tint = if (night) Color.White else LessonTheme.primary,
                        night = night,
                        size = (64 * k).dp,
                        enabled = isReady,
                        onClick = { viewModel.skipBackward() }
                    )
                    PlayPauseButton(
                        night = night,
                        isPlaying = isPlaying,
                        isLoading = isLoading,
                        enabled = isReady,
                        onClick = { viewModel.togglePlayPause() }
                    )
                    PlayerRoundButton(
                        icon = Icons.Filled.Forward10,
                        description = "Forward 10 seconds",
                        tint = if (night) Color.White else LessonTheme.primary,
                        night = night,
                        size = (64 * k).dp,
                        enabled = isReady,
                        onClick = { viewModel.skipForward() }
                    )
                }

                NightToggle(
                    night = night,
                    modifier = Modifier.padding(top = 30.dp, bottom = 24.dp),
                    onChange = { PlayerAppearance.override = it }
                )
            }
        }
    }
}

@Composable
private fun Cover(story: Story, size: Dp, night: Boolean) {
    val shape = RoundedCornerShape(32.dp)
    val edge = if (night) 0.dp else 10.dp
    Box(modifier = Modifier.padding(bottom = edge)) {
        if (!night) {
            Box(
                modifier = Modifier
                    .size(size)
                    .offset(y = edge)
                    .background(PlayerPalette.dayEdge, shape)
            )
        }
        AsyncImage(
            model = story.background,
            contentDescription = story.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .then(if (night) Modifier.shadow(18.dp, shape) else Modifier)
                .clip(shape)
                .background(if (night) PlayerPalette.nightFrame else PlayerPalette.dayTrack)
                .then(if (night) Modifier.border(6.dp, PlayerPalette.nightFrame, shape) else Modifier)
        )
    }
}

/** Round control: raised white in the day, a soft translucent disc at night */
@Composable
private fun PlayerRoundButton(
    icon: ImageVector,
    description: String,
    tint: Color,
    night: Boolean,
    onClick: () -> Unit,
    size: Dp = (46 * LessonTheme.scale()).dp,
    enabled: Boolean = true
) {
    val edge = 3.dp
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val lift by animateDpAsState(if (pressed && !night) edge else 0.dp, label = "roundLift")

    Box(
        modifier = Modifier
            .alpha(if (enabled) 1f else 0.5f)
            .padding(bottom = if (night) 0.dp else edge)
            .size(size)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled) {
                SoundEffectManager.playClickSound()
                onClick()
            }
    ) {
        if (!night) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = edge)
                    .background(PlayerPalette.dayEdge, CircleShape)
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = lift)
                .background(if (night) Color.White.copy(alpha = if (pressed) 0.22f else 0.14f) else Color.White, CircleShape)
                .then(if (night) Modifier else Modifier.border(1.5.dp, LessonTheme.line, CircleShape)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = tint,
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
}

@Composable
private fun PlayPauseButton(
    night: Boolean,
    isPlaying: Boolean,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val k = LessonTheme.scale()
    val face = if (night) PlayerPalette.amber else LessonTheme.primary
    val edge = if (night) PlayerPalette.amberEdge else LessonTheme.primaryEdge
    val ink = if (night) PlayerPalette.amberInk else Color.White
    val lip = 6.dp
    val size = (88 * k).dp
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val lift by animateDpAsState(if (pressed) lip else 0.dp, label = "playLift")

    Box(
        modifier = Modifier
            .alpha(if (enabled) 1f else 0.6f)
            .padding(bottom = lip)
            .size(size)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled) {
                SoundEffectManager.playClickSound()
                onClick()
            }
    ) {
        Box(modifier = Modifier.matchParentSize().offset(y = lip).background(edge, CircleShape))
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = lift)
                .background(face, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator(
                    color = ink,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size((36 * k).dp)
                )
                isPlaying -> Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Box(Modifier.width((9 * k).dp).height((32 * k).dp).background(ink, RoundedCornerShape(3.dp)))
                    Box(Modifier.width((9 * k).dp).height((32 * k).dp).background(ink, RoundedCornerShape(3.dp)))
                }
                else -> Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play",
                    tint = ink,
                    modifier = Modifier.size((46 * k).dp)
                )
            }
        }
    }
}

@Composable
private fun NightToggle(night: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val k = LessonTheme.scale()
    Row(
        modifier = modifier
            .background(if (night) Color.White.copy(alpha = 0.08f) else Color.White, CircleShape)
            .border(2.dp, if (night) PlayerPalette.amber else PlayerPalette.dayEdge, CircleShape)
            .padding(start = 22.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Bedtime,
            contentDescription = null,
            tint = PlayerPalette.amber,
            modifier = Modifier.size((22 * k).dp)
        )
        Text(
            text = "Night mode",
            style = LessonTheme.answer().copy(
                fontSize = (20 * k).sp,
                color = if (night) Color.White else LessonTheme.ink
            )
        )
        Switch(
            checked = night,
            onCheckedChange = { SoundEffectManager.playClickSound(); onChange(it) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PlayerPalette.amber,
                checkedBorderColor = PlayerPalette.amber,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFD9DEEA),
                uncheckedBorderColor = Color(0xFFD9DEEA)
            )
        )
    }
}

@Composable
private fun NightStars() {
    val stars = listOf(
        Triple(0.92f, 0.11f, 5f), Triple(0.07f, 0.17f, 4f), Triple(0.96f, 0.42f, 5f), Triple(0.04f, 0.50f, 4f),
        Triple(0.18f, 0.64f, 4f), Triple(0.90f, 0.82f, 5f), Triple(0.30f, 0.93f, 4f), Triple(0.76f, 0.95f, 4f)
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        stars.forEach { (x, y, r) ->
            drawCircle(
                color = PlayerPalette.nightMuted.copy(alpha = 0.5f),
                radius = r.dp.toPx() / 2f,
                center = Offset(size.width * x, size.height * y)
            )
        }
    }
}

/** Wavy track with a vertical pill thumb; tap or drag to seek */
@Composable
private fun WavyProgress(
    progress: Float,
    enabled: Boolean,
    activeColor: Color,
    trackColor: Color,
    thumbColor: Color,
    onSeek: (Float) -> Unit
) {
    val k = LessonTheme.scale()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height((44 * k).dp)
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures { offset -> onSeek((offset.x / size.width).coerceIn(0f, 1f)) }
                }
            }
            .pointerInput(enabled) {
                if (enabled) {
                    detectHorizontalDragGestures { change, _ ->
                        onSeek((change.position.x / size.width).coerceIn(0f, 1f))
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val centerY = size.height / 2f
            val amplitude = 3.dp.toPx()
            val wavelength = width / 8f
            val strokeWidth = 6.dp.toPx()

            fun wave(upTo: Float): Path {
                val path = Path()
                path.moveTo(0f, centerY)
                var x = 0f
                while (x <= upTo) {
                    path.lineTo(x, centerY + amplitude * sin((x / wavelength) * 2 * PI).toFloat())
                    x += 2f
                }
                return path
            }

            drawPath(wave(width), trackColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            val progressX = width * progress
            if (progress > 0f) {
                drawPath(wave(progressX), activeColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            }
            val thumbW = 5.dp.toPx()
            val thumbH = 34.dp.toPx()
            drawRoundRect(
                color = thumbColor,
                topLeft = Offset(progressX - thumbW / 2f, centerY - thumbH / 2f),
                size = Size(thumbW, thumbH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(thumbW / 2f)
            )
        }
    }
}
