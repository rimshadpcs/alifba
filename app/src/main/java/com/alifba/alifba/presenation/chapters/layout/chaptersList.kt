package com.alifba.alifba.presenation.chapters.layout

import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alifba.alifba.presenation.chapters.ChaptersViewModel
import com.alifba.alifba.presenation.chapters.models.Chapter
import kotlinx.coroutines.delay

@Composable
fun LazyChapterColumn(
    lessons: List<Chapter>,
    modifier: Modifier = Modifier,
    navController: NavController,
    onChapterClick: (Chapter) -> Unit,
    viewModel: ChaptersViewModel = hiltViewModel()
) {
    val configuration = LocalConfiguration.current
    val constrainedModifier = if (configuration.screenWidthDp > 600) {
        Modifier.widthIn(max = 600.dp)
    } else {
        Modifier.fillMaxWidth()
    }

    val listState = rememberLazyListState()
    val density = LocalDensity.current

    LaunchedEffect(lessons) {
        if (lessons.isNotEmpty()) {
            // Wait for UI to settle
            delay(300)

            val targetIndex = lessons.indexOfLast { chapter ->
                !chapter.isCompleted && !chapter.isLocked
            }

            if (targetIndex != -1) {
                Log.d("ChapterScroll", "Starting smooth scroll to target index: $targetIndex")

                // Calculate the approximate scroll distance
                val itemHeightPx = with(density) { 120.dp.toPx() }
                val currentIndex = listState.firstVisibleItemIndex
                val distance = (targetIndex - currentIndex) * itemHeightPx

                // Perform a single smooth animation with custom slower settings
                try {
                    listState.animateScrollBy(
                        value = distance,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,  // Remove bounce for smoother feel
                            stiffness = 50f,  // Custom lower stiffness value for slower animation
                            visibilityThreshold = .5f  // Lower threshold for smoother finish
                        )
                    )
                } catch (e: Exception) {
                    Log.e("ChapterScroll", "Error during scroll animation", e)
                }

                Log.d("ChapterScroll", "Smooth scroll animation completed")
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        LazyColumn(
            state = listState,
            modifier = modifier
                .then(constrainedModifier)
                .wrapContentHeight(align = Alignment.Bottom),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            reverseLayout = true
        ) {
            itemsIndexed(lessons) { index, lesson ->
                // Wrap the ChapterPathItems with PulsingStartIndicator if it's the start point
                if (!lesson.isCompleted && !lesson.isLocked) {
                    PulsingStartIndicator {
                        ChapterPathItems(
                            lesson = lesson,
                            index = index,
                            onClick = { onChapterClick(lesson) }
                        )
                    }
                } else {
                    ChapterPathItems(
                        lesson = lesson,
                        index = index,
                        onClick = { onChapterClick(lesson) }
                    )
                }
            }
        }
    }
}
@Composable
fun PulsingStartIndicator(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")

    // Scale animation
    val scale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Box(
        modifier = modifier
            .scale(scale.value).padding(4.dp)
    ) {
        content()
    }
}