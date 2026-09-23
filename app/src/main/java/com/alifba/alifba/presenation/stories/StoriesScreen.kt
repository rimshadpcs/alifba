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
import androidx.compose.ui.graphics.ColorFilter
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
import com.alifba.alifba.data.local.PlaybackProgressStore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.CircleShape
import kotlinx.coroutines.launch
import com.alifba.alifba.presenation.SubscriptionViewModel
import kotlin.math.roundToInt
import com.alifba.alifba.ui_components.theme.darkBlue

// Warm parchment replacing the old dark-purple starry-sky background image.
private val storiesScreenCream = Color(0xFFFFF8ED)

// Cycled per section, in order, across the Stories screen (5 sections: Stories of prophets,
// Life of Prophet Muhammad, Sahaba, Women and Mothers, Miracles). Not private — each section's
// "View All" detail screen (AllStoriesScreen etc.) takes its matching tint as a parameter from
// StoriesWithAudioPlayerScreen.kt, which reads this same list so there's one source of truth.
val storySectionTints = listOf(
    Color(0xFFDEF5FE),
    Color(0xFFE5F5E7),
    Color(0xFFFDEAF7),
    Color(0xFFF4EDFE),
    Color(0xFFFFF3DE)
)

@Composable
fun StoriesScreen(
    storiesViewModel: StoriesViewModel = hiltViewModel(),
    onStoryClick: (Story) -> Unit = {},
    onMoreClick: () -> Unit = {},
    onFavoritesMoreClick: () -> Unit = {},
    onProphetMuhammadStoryClick: (Story) -> Unit = {},
    onProphetMuhammadMoreClick: () -> Unit = {},
    onSahabaStoryClick: (Story) -> Unit = {},
    onSahabaMoreClick: () -> Unit = {},
    onWomenAndMothersStoryClick: (Story) -> Unit = {},
    onWomenAndMothersMoreClick: () -> Unit = {},
    onMiraclesStoryClick: (Story) -> Unit = {},
    onMiraclesMoreClick: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600
    
    val favoriteIds by storiesViewModel.getFavoriteStories().collectAsState()

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

    // Observe Women and Mothers stories from ViewModel
    val womenAndMothersStories by storiesViewModel.womenAndMothersStories.collectAsState()
    val isWomenAndMothersLoading by storiesViewModel.isWomenAndMothersLoading.collectAsState()

    // Observe Miracles stories from ViewModel
    val miraclesStories by storiesViewModel.miraclesStories.collectAsState()
    val isMiraclesLoading by storiesViewModel.isMiraclesLoading.collectAsState()
    
    // Progress store for hero and per-card progress
    val context = androidx.compose.ui.platform.LocalContext.current
    val progressStore = remember { PlaybackProgressStore(context) }
    
    // Track refresh state for pull-to-refresh
    var isRefreshing by remember { mutableStateOf(false) }
    
    // Handle pull-to-refresh
    val handleRefresh = {
        isRefreshing = true
        storiesViewModel.forceRefreshAll()
    }
    
    // Reset refresh state when all loading states are false
    LaunchedEffect(isLoading, isProphetMuhammadLoading, isSahabaLoading, isWomenAndMothersLoading, isMiraclesLoading) {
        if (!isLoading && !isProphetMuhammadLoading && !isSahabaLoading && !isWomenAndMothersLoading && !isMiraclesLoading) {
            isRefreshing = false
        }
    }

    val listState = rememberLazyListState()

    // Ensure we start at the top when entering this screen
    LaunchedEffect(Unit) {
        listState.scrollToItem(0)
    }
    // Also reset to top when data finishes loading to avoid mid-list starts
    LaunchedEffect(isLoading, isProphetMuhammadLoading, isSahabaLoading, isWomenAndMothersLoading, isMiraclesLoading) {
        if (!isLoading && !isProphetMuhammadLoading && !isSahabaLoading && !isWomenAndMothersLoading && !isMiraclesLoading) {
            listState.scrollToItem(0)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(storiesScreenCream)
    ) {
        PullToRefreshLazyColumn(
            isRefreshing = isRefreshing,
            onRefresh = handleRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp),
            state = listState,
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Hero Banner (resume/next)
            item {
                val allStories = remember(stories, prophetMuhammadStories, sahabaStories, womenAndMothersStories, miraclesStories) {
                    buildList {
                        addAll(stories)
                        addAll(prophetMuhammadStories)
                        addAll(sahabaStories)
                        addAll(womenAndMothersStories)
                        addAll(miraclesStories)
                    }
                }
                HeroBanner(
                    stories = allStories,
                    onPlayClick = onStoryClick,
                    progressStore = progressStore,
                    isTablet = isTablet,
                    alifbaFont = alifbaFont
                )
            }

            // Favorites Section (shows if user has favorites)
            item {
                val allStories = remember(stories, prophetMuhammadStories, sahabaStories, womenAndMothersStories, miraclesStories) {
                    buildList {
                        addAll(stories)
                        addAll(prophetMuhammadStories)
                        addAll(sahabaStories)
                        addAll(womenAndMothersStories)
                        addAll(miraclesStories)
                    }
                }
                if (favoriteIds.isNotEmpty()) {
                    FavoritesSection(
                        allStories = allStories,
                        favoriteKeys = favoriteIds,
                        onStoryClick = onStoryClick,
                        onMoreClick = onFavoritesMoreClick,
                        alifbaFont = alifbaFont,
                        isTablet = isTablet,
                        progressFractionFor = { story ->
                            val p = progressStore.getProgress(story.documentId)
                            if (p.duration > 0L) (p.position.toFloat() / p.duration.toFloat()).coerceIn(0f, 1f) else null
                        }
                    )
                }
            }

            // Title row with "Stories" and "View All >" — first of 5 sections, tint index 0.
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(storySectionTints[0])
                        .padding(horizontal = if (isTablet) 32.dp else 24.dp, vertical = if (isTablet) 20.dp else 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stories of prophets",
                        fontFamily = alifbaFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isTablet) 32.sp else 24.sp,
                        color = darkBlue
                    )

                    Text(
                        text = "View All >",
                        fontFamily = alifbaFont,
                        fontWeight = FontWeight.Normal,
                        fontSize = if (isTablet) 18.sp else 14.sp,
                        color = darkBlue,
                        modifier = Modifier.clickable {
                            SoundEffectManager.playClickSound()
                            onMoreClick()
                        }
                    )
                }
            }

            // Stories horizontal list (max 5) — same tint as the title row above so the two
            // items (LazyColumn packs them with no gap) read as one contiguous section block.
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(storySectionTints[0])
                ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Loading stories from Firebase...", fontFamily = alifbaFont, color = darkBlue)
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
                                Text("No stories found", fontFamily = alifbaFont, color = darkBlue)
                                Text("Retry",
                                    fontFamily = alifbaFont,
                                    color = lightPurple,
                                    modifier = Modifier.clickable { storiesViewModel.forceRefreshStories() }
                                )
                            }
                        }
                    }
                    else -> {
                        val displayedStories = stories.take(5)
                        val itemWidth = if (isTablet) 260.dp else 180.dp
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = if (isTablet) 24.dp else 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 12.dp)
                        ) {
                            items(displayedStories.size) { idx ->
                                val s = displayedStories[idx]
                                val isFav by storiesViewModel
                                    .isFavorite(s.documentId, s.category.ifBlank { "stories" })
                                    .collectAsState()
                                val progress = remember(s.documentId) {
                                    val p = progressStore.getProgress(s.documentId)
                                    if (p.duration > 0L) (p.position.toFloat() / p.duration.toFloat()).coerceIn(0f, 1f) else 0f
                                }
                                Box(modifier = Modifier.width(itemWidth)) {
                                    StoryCard(
                                        story = s,
                                        onClick = {
                                            SoundEffectManager.playClickSound()
                                            onStoryClick(s)
                                        },
                                        alifbaFont = alifbaFont,
                                        isTablet = isTablet,
                                        progressFraction = progress.takeIf { it > 0f && it < 1f },
                                        isFavorite = isFav,
                                        onFavoriteClick = {
                                            storiesViewModel.toggleFavorite(
                                                s.documentId,
                                                s.category.ifBlank { "stories" }
                                            )
                                        }
                                    )
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
                    onMoreClick = onProphetMuhammadMoreClick,
                    alifbaFont = alifbaFont,
                    isTablet = isTablet,
                    progressFractionFor = { story ->
                        val p = progressStore.getProgress(story.documentId)
                        if (p.duration > 0L) (p.position.toFloat() / p.duration.toFloat()).coerceIn(0f, 1f) else null
                    }
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
                    isTablet = isTablet,
                    progressFractionFor = { story ->
                        val p = progressStore.getProgress(story.documentId)
                        if (p.duration > 0L) (p.position.toFloat() / p.duration.toFloat()).coerceIn(0f, 1f) else null
                    }
                )
            }

            // Women and Mothers Stories Section
            item {
                WomenAndMothersStoriesSection(
                    stories = womenAndMothersStories,
                    isLoading = isWomenAndMothersLoading,
                    onStoryClick = onWomenAndMothersStoryClick,
                    onMoreClick = onWomenAndMothersMoreClick,
                    alifbaFont = alifbaFont,
                    isTablet = isTablet,
                    progressFractionFor = { story ->
                        val p = progressStore.getProgress(story.documentId)
                        if (p.duration > 0L) (p.position.toFloat() / p.duration.toFloat()).coerceIn(0f, 1f) else null
                    }
                )
            }

            // Miracles Stories Section
            item {
                MiraclesStoriesSection(
                    stories = miraclesStories,
                    isLoading = isMiraclesLoading,
                    onStoryClick = onMiraclesStoryClick,
                    onMoreClick = onMiraclesMoreClick,
                    alifbaFont = alifbaFont,
                    isTablet = isTablet,
                    progressFractionFor = { story ->
                        val p = progressStore.getProgress(story.documentId)
                        if (p.duration > 0L) (p.position.toFloat() / p.duration.toFloat()).coerceIn(0f, 1f) else null
                    }
                )
            }
        }
    }
}

@Composable
fun AllStoriesScreen(
    storiesViewModel: StoriesViewModel = hiltViewModel(),
    onStoryClick: (Story) -> Unit = {},
    onBackClick: () -> Unit = {},
    backgroundTint: Color = storySectionTints[0]
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
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundTint)
    ) {
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
                        },
                    colorFilter = ColorFilter.tint(darkBlue)
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
                        color = darkBlue
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
                        Text("Loading stories from Firebase...", fontFamily = alifbaFont, color = darkBlue)
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
                            Text("No stories found", fontFamily = alifbaFont, color = darkBlue)
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
                            val isFav by storiesViewModel
                                .isFavorite(story.documentId, story.category.ifBlank { "stories" })
                                .collectAsState()
                            StoryCard(
                                story = story,
                                onClick = {
                                    SoundEffectManager.playClickSound()
                                    onStoryClick(story)
                                },
                                alifbaFont = alifbaFont,
                                isFavorite = isFav,
                                onFavoriteClick = { 
                                    storiesViewModel.toggleFavorite(
                                        story.documentId,
                                        story.category.ifBlank { "stories" }
                                    ) 
                                }
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
    onBackClick: () -> Unit = {},
    backgroundTint: Color = storySectionTints[1]
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
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundTint)
    ) {
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
                        },
                    colorFilter = ColorFilter.tint(darkBlue)
                )

                // Centered title
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Life of Prophet Muhammad",
                        fontFamily = alifbaFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = darkBlue
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
                        Text("Loading Prophet Muhammad stories...", fontFamily = alifbaFont, color = darkBlue)
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
                            Text("No Prophet Muhammad stories found", fontFamily = alifbaFont, color = darkBlue)
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
                            val isFav by storiesViewModel
                                .isFavorite(story.documentId, story.category.ifBlank { "stories" })
                                .collectAsState()
                            StoryCard(
                                story = story,
                                onClick = {
                                    SoundEffectManager.playClickSound()
                                    onStoryClick(story)
                                },
                                alifbaFont = alifbaFont,
                                isFavorite = isFav,
                                onFavoriteClick = { 
                                    storiesViewModel.toggleFavorite(
                                        story.documentId,
                                        story.category.ifBlank { "stories" }
                                    ) 
                                }
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
    onBackClick: () -> Unit = {},
    backgroundTint: Color = storySectionTints[2]
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
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundTint)
    ) {
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
                        },
                    colorFilter = ColorFilter.tint(darkBlue)
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
                        color = darkBlue
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
                        Text("Loading Sahaba stories...", fontFamily = alifbaFont, color = darkBlue)
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
                            Text("No Sahaba stories found", fontFamily = alifbaFont, color = darkBlue)
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
                            val isFav by storiesViewModel
                                .isFavorite(story.documentId, story.category.ifBlank { "stories" })
                                .collectAsState()
                            StoryCard(
                                story = story,
                                onClick = {
                                    SoundEffectManager.playClickSound()
                                    onStoryClick(story)
                                },
                                alifbaFont = alifbaFont,
                                isFavorite = isFav,
                                onFavoriteClick = { 
                                    storiesViewModel.toggleFavorite(
                                        story.documentId,
                                        story.category.ifBlank { "stories" }
                                    ) 
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AllWomenAndMothersStoriesScreen(
    storiesViewModel: StoriesViewModel = hiltViewModel(),
    onStoryClick: (Story) -> Unit = {},
    onBackClick: () -> Unit = {},
    backgroundTint: Color = storySectionTints[3]
) {
    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )

    // Observe Women and Mothers stories from ViewModel
    val stories by storiesViewModel.womenAndMothersStories.collectAsState()
    val isLoading by storiesViewModel.isWomenAndMothersLoading.collectAsState()
    val error by storiesViewModel.error.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundTint)
    ) {
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
                        },
                    colorFilter = ColorFilter.tint(darkBlue)
                )

                // Centered title
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Women and Mothers of Islam",
                        fontFamily = alifbaFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = darkBlue
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
                        Text("Loading Women and Mothers of Islam stories...", fontFamily = alifbaFont, color = darkBlue)
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
                                modifier = Modifier.clickable { storiesViewModel.forceRefreshWomenAndMothersStories() }
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
                            Text("No Women and Mothers of Islam stories found", fontFamily = alifbaFont, color = darkBlue)
                            Text("Retry",
                                fontFamily = alifbaFont,
                                color = lightPurple,
                                modifier = Modifier.clickable { storiesViewModel.forceRefreshWomenAndMothersStories() }
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
                            val isFav by storiesViewModel
                                .isFavorite(story.documentId, story.category.ifBlank { "stories" })
                                .collectAsState()
                            StoryCard(
                                story = story,
                                onClick = {
                                    SoundEffectManager.playClickSound()
                                    onStoryClick(story)
                                },
                                alifbaFont = alifbaFont,
                                isFavorite = isFav,
                                onFavoriteClick = { 
                                    storiesViewModel.toggleFavorite(
                                        story.documentId,
                                        story.category.ifBlank { "stories" }
                                    ) 
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AllMiraclesStoriesScreen(
    storiesViewModel: StoriesViewModel = hiltViewModel(),
    onStoryClick: (Story) -> Unit = {},
    onBackClick: () -> Unit = {},
    backgroundTint: Color = storySectionTints[4]
) {
    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )

    // Observe Miracles stories from ViewModel
    val stories by storiesViewModel.miraclesStories.collectAsState()
    val isLoading by storiesViewModel.isMiraclesLoading.collectAsState()
    val error by storiesViewModel.error.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundTint)
    ) {
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
                        },
                    colorFilter = ColorFilter.tint(darkBlue)
                )

                // Centered title
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Miracles in Quran",
                        fontFamily = alifbaFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = darkBlue
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
                        Text("Loading Miracles in Quran stories...", fontFamily = alifbaFont, color = darkBlue)
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
                                modifier = Modifier.clickable { storiesViewModel.forceRefreshMiraclesStories() }
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
                            Text("No Miracles in Quran stories found", fontFamily = alifbaFont, color = darkBlue)
                            Text("Retry",
                                fontFamily = alifbaFont,
                                color = lightPurple,
                                modifier = Modifier.clickable { storiesViewModel.forceRefreshMiraclesStories() }
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
                            val isFav by storiesViewModel
                                .isFavorite(story.documentId, story.category.ifBlank { "stories" })
                                .collectAsState()
                            StoryCard(
                                story = story,
                                onClick = {
                                    SoundEffectManager.playClickSound()
                                    onStoryClick(story)
                                },
                                alifbaFont = alifbaFont,
                                isFavorite = isFav,
                                onFavoriteClick = { 
                                    storiesViewModel.toggleFavorite(
                                        story.documentId,
                                        story.category.ifBlank { "stories" }
                                    ) 
                                }
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
    val imageUrl = remember(story.thumbnail, story.background) {
        story.thumbnail.ifBlank { story.background }
    }
    
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
                model = imageUrl,
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
fun FavoritesSection(
    allStories: List<Story>,
    favoriteKeys: Set<String>,
    onStoryClick: (Story) -> Unit,
    onMoreClick: () -> Unit,
    alifbaFont: FontFamily,
    isTablet: Boolean = false,
    progressFractionFor: (Story) -> Float? = { null }
) {
    val storiesViewModel: StoriesViewModel = hiltViewModel()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Title row with "Your Favorites" and "View All >"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(if (isTablet) 28.dp else 20.dp)
                )
                Spacer(modifier = Modifier.width(if (isTablet) 8.dp else 6.dp))
                Text(
                    text = "Your Favorites",
                    fontFamily = alifbaFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isTablet) 28.sp else 20.sp,
                    color = darkBlue
                )
            }
            Text(
                text = "View All >",
                fontFamily = alifbaFont,
                fontWeight = FontWeight.Normal,
                fontSize = if (isTablet) 18.sp else 14.sp,
                color = darkBlue,
                modifier = Modifier.clickable {
                    SoundEffectManager.playClickSound()
                    onMoreClick()
                }
            )
        }

        // Build a favorites list by scanning allStories to preserve a stable order
        val favoritePairs = remember(allStories, favoriteKeys) {
            val normalized = favoriteKeys.map { key ->
                if (":" in key) key else "stories:$key"
            }.toSet()
            allStories.filter { story ->
                val key = (story.category.ifBlank { "stories" }) + ":" + story.documentId
                key in normalized
            }
        }

        if (favoritePairs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No favorites yet", fontFamily = alifbaFont, color = darkBlue)
            }
        } else {
            val displayedStories = favoritePairs.take(5)
            val itemWidth = if (isTablet) 260.dp else 180.dp
            LazyRow(
                contentPadding = PaddingValues(horizontal = if (isTablet) 24.dp else 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 12.dp)
            ) {
                items(displayedStories.size) { idx ->
                    val story = displayedStories[idx]
                    val isFav by storiesViewModel
                        .isFavorite(story.documentId, story.category.ifBlank { "stories" })
                        .collectAsState()
                    val progress = progressFractionFor(story)
                    Box(modifier = Modifier.width(itemWidth)) {
                        StoryCard(
                            story = story,
                            onClick = {
                                SoundEffectManager.playClickSound()
                                onStoryClick(story)
                            },
                            alifbaFont = alifbaFont,
                            isTablet = isTablet,
                            progressFraction = progress?.takeIf { it > 0f && it < 1f },
                            isFavorite = isFav,
                            onFavoriteClick = {
                                storiesViewModel.toggleFavorite(
                                    story.documentId,
                                    story.category.ifBlank { "stories" }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProphetMuhammadStoriesSection(
    stories: List<Story>,
    isLoading: Boolean,
    onStoryClick: (Story) -> Unit,
    onMoreClick: () -> Unit,
    alifbaFont: FontFamily,
    isTablet: Boolean = false,
    progressFractionFor: (Story) -> Float? = { null }
) {
    val storiesVM: StoriesViewModel = hiltViewModel()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(storySectionTints[1])
            .padding(vertical = 16.dp)
    ) {
        // Title row with "Stories of Prophet Muhammad" and "View All"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Life of Prophet Muhammad",
                fontFamily = alifbaFont,
                fontWeight = FontWeight.Bold,
                fontSize = if (isTablet) 28.sp else 20.sp,
                color = darkBlue
            )
            Text(
                text = "View All >",
                fontFamily = alifbaFont,
                fontWeight = FontWeight.Normal,
                fontSize = if (isTablet) 18.sp else 14.sp,
                color = darkBlue,
                modifier = Modifier.clickable {
                    SoundEffectManager.playClickSound()
                    onMoreClick()
                }
            )
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading Prophet Muhammad stories...", fontFamily = alifbaFont, color = darkBlue)
                }
            }
            stories.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Prophet Muhammad stories found", fontFamily = alifbaFont, color = darkBlue)
                }
            }
            else -> {
                val displayedStories = stories.take(5)
                val itemWidth = if (isTablet) 260.dp else 180.dp
                LazyRow(
                    contentPadding = PaddingValues(horizontal = if (isTablet) 24.dp else 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 12.dp)
                ) {
                    items(displayedStories.size) { idx ->
                        val story = displayedStories[idx]
                        val isFav by storiesVM
                            .isFavorite(story.documentId, story.category.ifBlank { "prophet_muhammad" })
                            .collectAsState()
                        val progress = progressFractionFor(story)
                        Box(modifier = Modifier.width(itemWidth)) {
                            StoryCard(
                                story = story,
                                onClick = {
                                    SoundEffectManager.playClickSound()
                                    onStoryClick(story)
                                },
                                alifbaFont = alifbaFont,
                                isTablet = isTablet,
                                progressFraction = progress?.takeIf { it > 0f && it < 1f },
                                isFavorite = isFav,
                                onFavoriteClick = { 
                                    storiesVM.toggleFavorite(
                                        story.documentId,
                                        story.category.ifBlank { "prophet_muhammad" }
                                    ) 
                                }
                            )
                        }
                    }
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
    isTablet: Boolean = false,
    progressFractionFor: (Story) -> Float? = { null }
) {
    val storiesVM: StoriesViewModel = hiltViewModel()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(storySectionTints[2])
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
                color = darkBlue
            )

            Text(
                text = "View All >",
                fontFamily = alifbaFont,
                fontWeight = FontWeight.Normal,
                fontSize = if (isTablet) 18.sp else 14.sp,
                color = darkBlue,
                modifier = Modifier.clickable {
                    SoundEffectManager.playClickSound()
                    onMoreClick()
                }
            )
        }

        // Horizontal list
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading Sahaba stories...", fontFamily = alifbaFont, color = darkBlue)
                }
            }
            stories.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Sahaba stories found", fontFamily = alifbaFont, color = darkBlue)
                }
            }
            else -> {
                val displayedStories = stories.take(5)
                val itemWidth = if (isTablet) 260.dp else 180.dp
                LazyRow(
                    contentPadding = PaddingValues(horizontal = if (isTablet) 24.dp else 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 12.dp)
                ) {
                    items(displayedStories.size) { idx ->
                        val story = displayedStories[idx]
                        val isFav by storiesVM
                            .isFavorite(story.documentId, story.category.ifBlank { "sahaba" })
                            .collectAsState()
                        val progress = progressFractionFor(story)
                        Box(modifier = Modifier.width(itemWidth)) {
                            StoryCard(
                                story = story,
                                onClick = {
                                    SoundEffectManager.playClickSound()
                                    onStoryClick(story)
                                },
                                alifbaFont = alifbaFont,
                                isTablet = isTablet,
                                progressFraction = progress?.takeIf { it > 0f && it < 1f },
                                isFavorite = isFav,
                                onFavoriteClick = { 
                                    storiesVM.toggleFavorite(
                                        story.documentId,
                                        story.category.ifBlank { "sahaba" }
                                    ) 
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WomenAndMothersStoriesSection(
    stories: List<Story>,
    isLoading: Boolean,
    onStoryClick: (Story) -> Unit,
    onMoreClick: () -> Unit,
    alifbaFont: FontFamily,
    isTablet: Boolean = false,
    progressFractionFor: (Story) -> Float? = { null }
) {
    val storiesVM: StoriesViewModel = hiltViewModel()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(storySectionTints[3])
            .padding(vertical = 16.dp)
    ) {
        // Title row with "Women and Mothers of Islam" and "View All >"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Women and Mothers of Islam",
                fontFamily = alifbaFont,
                fontWeight = FontWeight.Bold,
                fontSize = if (isTablet) 28.sp else 20.sp,
                color = darkBlue
            )

            Text(
                text = "View All >",
                fontFamily = alifbaFont,
                fontWeight = FontWeight.Normal,
                fontSize = if (isTablet) 18.sp else 14.sp,
                color = darkBlue,
                modifier = Modifier.clickable {
                    SoundEffectManager.playClickSound()
                    onMoreClick()
                }
            )
        }

        // Horizontal list
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading Women and Mothers of Islam stories...", fontFamily = alifbaFont, color = darkBlue)
                }
            }
            stories.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Women and Mothers of Islam stories found", fontFamily = alifbaFont, color = darkBlue)
                }
            }
            else -> {
                val displayedStories = stories.take(5)
                val itemWidth = if (isTablet) 260.dp else 180.dp
                LazyRow(
                    contentPadding = PaddingValues(horizontal = if (isTablet) 24.dp else 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 12.dp)
                ) {
                    items(displayedStories.size) { idx ->
                        val story = displayedStories[idx]
                        val isFav by storiesVM
                            .isFavorite(story.documentId, story.category.ifBlank { "women_and_mothers" })
                            .collectAsState()
                        val progress = progressFractionFor(story)
                        Box(modifier = Modifier.width(itemWidth)) {
                            StoryCard(
                                story = story,
                                onClick = {
                                    SoundEffectManager.playClickSound()
                                    onStoryClick(story)
                                },
                                alifbaFont = alifbaFont,
                                isTablet = isTablet,
                                progressFraction = progress?.takeIf { it > 0f && it < 1f },
                                isFavorite = isFav,
                                onFavoriteClick = { 
                                    storiesVM.toggleFavorite(
                                        story.documentId,
                                        story.category.ifBlank { "women_and_mothers" }
                                    ) 
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MiraclesStoriesSection(
    stories: List<Story>,
    isLoading: Boolean,
    onStoryClick: (Story) -> Unit,
    onMoreClick: () -> Unit,
    alifbaFont: FontFamily,
    isTablet: Boolean = false,
    progressFractionFor: (Story) -> Float? = { null }
) {
    val storiesVM: StoriesViewModel = hiltViewModel()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(storySectionTints[4])
            .padding(vertical = 16.dp)
    ) {
        // Title row with "Miracles in Quran" and "View All >"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Miracles in Quran",
                fontFamily = alifbaFont,
                fontWeight = FontWeight.Bold,
                fontSize = if (isTablet) 28.sp else 20.sp,
                color = darkBlue
            )

            Text(
                text = "View All >",
                fontFamily = alifbaFont,
                fontWeight = FontWeight.Normal,
                fontSize = if (isTablet) 22.sp else 16.sp,
                color = darkBlue,
                modifier = Modifier.clickable {
                    SoundEffectManager.playClickSound()
                    onMoreClick()
                }
            )
        }

        // Horizontal list
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading Miracles in Quran stories...", fontFamily = alifbaFont, color = darkBlue)
                }
            }
            stories.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Miracles in Quran stories found", fontFamily = alifbaFont, color = darkBlue)
                }
            }
            else -> {
                val displayedStories = stories.take(5)
                val itemWidth = if (isTablet) 260.dp else 180.dp
                LazyRow(
                    contentPadding = PaddingValues(horizontal = if (isTablet) 24.dp else 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 12.dp)
                ) {
                    items(displayedStories.size) { idx ->
                        val story = displayedStories[idx]
                        val isFav by storiesVM
                            .isFavorite(story.documentId, story.category.ifBlank { "miracles" })
                            .collectAsState()
                        val progress = progressFractionFor(story)
                        Box(modifier = Modifier.width(itemWidth)) {
                            StoryCard(
                                story = story,
                                onClick = {
                                    SoundEffectManager.playClickSound()
                                    onStoryClick(story)
                                },
                                alifbaFont = alifbaFont,
                                isTablet = isTablet,
                                progressFraction = progress?.takeIf { it > 0f && it < 1f },
                                isFavorite = isFav,
                                onFavoriteClick = { 
                                    storiesVM.toggleFavorite(
                                        story.documentId,
                                        story.category.ifBlank { "miracles" }
                                    ) 
                                }
                            )
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
    isTablet: Boolean = false,
    progressFraction: Float? = null,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {}
) {
    val subscriptionViewModel: SubscriptionViewModel = hiltViewModel()
    val isPremium by subscriptionViewModel.isPremium.collectAsState()
    var imageState by remember { mutableStateOf<AsyncImagePainter.State?>(null) }
    val imageUrl = remember(story.thumbnail, story.background) {
        story.thumbnail.ifBlank { story.background }
    }
    
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
            // Shimmer background for loading state — top corners only, matching the image
            // below, since the title footer strip (added below the image) supplies its own
            // bottom-rounded background now instead of the image rounding all 4 corners.
            if (imageState == null || imageState is AsyncImagePainter.State.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = if (isTablet) 24.dp else 16.dp, topEnd = if (isTablet) 24.dp else 16.dp))
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

            // Use AsyncImage for loading Firebase images — top corners only (see shimmer note).
            AsyncImage(
                model = imageUrl,
                contentDescription = story.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = if (isTablet) 24.dp else 16.dp, topEnd = if (isTablet) 24.dp else 16.dp)),
                contentScale = ContentScale.Crop,
                onState = { state ->
                    imageState = state
                }
            )
            
            
            // Locked badge (top-left, plain icon, no circular background) — the heart occupies
            // top-right, so this takes the corner it doesn't.
            if (story.isLocked && imageState != null && imageState !is AsyncImagePainter.State.Loading) {
                Image(
                    painter = painterResource(id = R.drawable.padlock),
                    contentDescription = "Locked",
                    modifier = Modifier
                        .padding(if (isTablet) 12.dp else 8.dp)
                        .align(Alignment.TopStart)
                        .size(if (isTablet) 28.dp else 22.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }

            // FREE pill (bottom-right, sage background, white text, no diagonal fold) — hide
            // for premium users. Bottom-right avoids the duration pill (bottom-left) and the
            // heart/padlock up top.
            if (!isPremium && story.status == "free" && imageState != null && imageState !is AsyncImagePainter.State.Loading) {
                Box(
                    modifier = Modifier
                        .padding(if (isTablet) 12.dp else 8.dp)
                        .align(Alignment.BottomEnd)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF00BF63))
                        .padding(horizontal = if (isTablet) 12.dp else 8.dp, vertical = if (isTablet) 6.dp else 4.dp)
                ) {
                    Text(
                        text = "FREE",
                        fontFamily = alifbaFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isTablet) 13.sp else 11.sp,
                        color = Color.White
                    )
                }
            }

            // Duration pill (bottom-left, dark translucent) — just the rounded-minute value,
            // no content-type label since every card here is a story.
            if (story.durationSeconds > 0 && imageState != null && imageState !is AsyncImagePainter.State.Loading) {
                val minutes = (story.durationSeconds / 60f).roundToInt()
                Box(
                    modifier = Modifier
                        .padding(if (isTablet) 12.dp else 8.dp)
                        .align(Alignment.BottomStart)
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = if (isTablet) 12.dp else 8.dp, vertical = if (isTablet) 6.dp else 4.dp)
                ) {
                    Text(
                        text = "${minutes}m",
                        fontFamily = alifbaFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isTablet) 13.sp else 11.sp,
                        color = Color.White
                    )
                }
            }

            // Favorite heart icon (top right corner) — same chunky pressable 3D effect as
            // CommonButton/the lesson nodes (static shadow disc behind, main disc on top whose
            // bottom padding animates on press so it compresses onto the shadow layer), instead
            // of the old flat white circle. The existing bounce-on-toggle animation is kept as a
            // separate flourish on top of the new press-down mechanic.
            if (imageState != null && imageState !is AsyncImagePainter.State.Loading) {
                val heartScale = remember { Animatable(1f) }
                val coroutineScope = rememberCoroutineScope()
                val heartInteractionSource = remember { MutableInteractionSource() }
                val isHeartPressed by heartInteractionSource.collectIsPressedAsState()
                val heartOffsetY by animateDpAsState(
                    targetValue = if (isHeartPressed) 0.dp else 3.dp,
                    label = "HeartOffsetY"
                )
                val heartSize = if (isTablet) 40.dp else 32.dp

                Box(
                    modifier = Modifier
                        .padding(if (isTablet) 12.dp else 8.dp)
                        .align(Alignment.TopEnd)
                        .size(heartSize)
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
                            .background(Color(0xFFD6D2C4))
                    )
                    // Main disc — compresses onto the shadow layer on press.
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(bottom = heartOffsetY)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable(
                                interactionSource = heartInteractionSource,
                                indication = null
                            ) {
                                // Bounce animation
                                coroutineScope.launch {
                                    heartScale.animateTo(0.7f, animationSpec = tween(100))
                                    heartScale.animateTo(1.2f, animationSpec = tween(100))
                                    heartScale.animateTo(1f, animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ))
                                }
                                onFavoriteClick()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) Color.Red else Color.Gray,
                            modifier = Modifier.size(if (isTablet) 24.dp else 20.dp)
                        )
                    }
                }
            }

            // Progress overlay bar at the bottom of the IMAGE (not the whole card anymore —
            // square corners here now, since the title footer strip sits flush below it).
            progressFraction?.let { frac ->
                if (frac in 0f..1f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(Color.White.copy(alpha = 0.4f))
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .height(6.dp)
                            .fillMaxWidth(frac)
                            .background(darkPurple)
                    )
                }
            }
        }
        
        // Title footer strip — solid cream background flush against the image's bottom edge,
        // dark navy text (same color as the section headers). Titles used to sit in white text
        // directly on the surrounding pastel section tint, which had almost no contrast; putting
        // them on their own opaque strip instead guarantees contrast no matter what tint
        // surrounds the card.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    storiesScreenCream,
                    RoundedCornerShape(bottomStart = if (isTablet) 24.dp else 16.dp, bottomEnd = if (isTablet) 24.dp else 16.dp)
                )
        ) {
            Text(
                text = story.name,
                fontFamily = alifbaFont,
                fontWeight = FontWeight.Bold,
                fontSize = if (isTablet) 20.sp else 14.sp,
                color = darkBlue,
                textAlign = TextAlign.Start,
                maxLines = 2,
                modifier = Modifier
                    .padding(
                        horizontal = if (isTablet) 8.dp else 4.dp,
                        vertical = if (isTablet) 12.dp else 8.dp
                    )
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun HeroBanner(
    stories: List<Story>,
    onPlayClick: (Story) -> Unit,
    progressStore: com.alifba.alifba.data.local.PlaybackProgressStore,
    isTablet: Boolean,
    alifbaFont: FontFamily
) {
    if (stories.isEmpty()) return

    val lastPlayedId = remember(stories) { progressStore.getLastPlayed() }
    val lastPlayed = remember(lastPlayedId, stories) { stories.find { it.documentId == lastPlayedId } }
    val lastProgress = remember(lastPlayedId) {
        lastPlayedId?.let { progressStore.getProgress(it) }
    }
    val showResume = lastPlayed != null && lastProgress != null && lastProgress.duration > 0 && lastProgress.position < (lastProgress.duration * 0.98f)
    val targetStory = when {
        showResume -> lastPlayed
        else -> stories.firstOrNull { !progressStore.getProgress(it.documentId).completed } ?: stories.first()
    }
    if (targetStory == null) return

    val progressFraction = remember(targetStory.documentId) {
        val p = progressStore.getProgress(targetStory.documentId)
        if (p.duration > 0L) (p.position.toFloat() / p.duration.toFloat()).coerceIn(0f, 1f) else 0f
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isTablet) 24.dp else 16.dp)
            .padding(top = if (isTablet) 16.dp else 12.dp, bottom = if (isTablet) 8.dp else 4.dp)
    ) {
        // Banner card — rounded and inset like the section cards below, instead of a full-bleed
        // edge-to-edge dark hero.
        val cardCorner = if (isTablet) 24.dp else 16.dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isTablet) 260.dp else 170.dp)
                .clip(RoundedCornerShape(cardCorner))
                .clickable { onPlayClick(targetStory) }
        ) {
            AsyncImage(
                model = targetStory.background,
                contentDescription = targetStory.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Gradient overlay bottom — cream wash (matching the section-card footer style)
            // instead of a dark scrim, so the dark-navy title/label text underneath stays
            // readable against it.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, storiesScreenCream.copy(alpha = 0.94f))
                        )
                    )
            )
            // Play button overlay (icon only, no background)
            Image(
                painter = painterResource(id = R.drawable.playthumb),
                contentDescription = "Play",
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(if (isTablet) 84.dp else 64.dp)
            )
            // Title + CTA
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(if (isTablet) 16.dp else 12.dp)
            ) {
                Text(
                    text = if (showResume) "Resume" else "Up Next",
                    fontFamily = alifbaFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isTablet) 18.sp else 14.sp,
                    color = darkBlue
                )
                Text(
                    text = targetStory.name,
                    fontFamily = alifbaFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isTablet) 22.sp else 16.sp,
                    color = darkBlue
                )
            }
            // Progress bar across bottom
            if (progressFraction in 0f..1f && progressFraction > 0f && progressFraction < 1f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(Color.White.copy(alpha = 0.4f))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .height(6.dp)
                        .fillMaxWidth(progressFraction)
                        .background(darkPurple)
                )
            }
        }
    }
}
@Composable
fun AllFavoritesScreen(
    storiesViewModel: StoriesViewModel = hiltViewModel(),
    onStoryClick: (Story) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )

    // Collect all story lists and favorites
    val stories by storiesViewModel.stories.collectAsState()
    val prophetStories by storiesViewModel.prophetMuhammadStories.collectAsState()
    val sahabaStories by storiesViewModel.sahabaStories.collectAsState()
    val womenStories by storiesViewModel.womenAndMothersStories.collectAsState()
    val miraclesStories by storiesViewModel.miraclesStories.collectAsState()
    val favoriteKeys by storiesViewModel.getFavoriteStories().collectAsState()

    val allStories = remember(stories, prophetStories, sahabaStories, womenStories, miraclesStories) {
        buildList {
            addAll(stories)
            addAll(prophetStories)
            addAll(sahabaStories)
            addAll(womenStories)
            addAll(miraclesStories)
        }
    }

    // Filter stories by favorite keys preserving order
    val favoriteStories = remember(allStories, favoriteKeys) {
        val normalized = favoriteKeys.map { key ->
            if (":" in key) key else "stories:$key"
        }.toSet()
        allStories.filter { s ->
            val k = (s.category.ifBlank { "stories" }) + ":" + s.documentId
            k in normalized
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Your Favorites",
                            fontFamily = alifbaFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Color.White
                        )
                    }
                }
            }

            when {
                favoriteStories.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No favorites yet", fontFamily = alifbaFont, color = Color.White)
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
                        items(favoriteStories.size) { index ->
                            val story = favoriteStories[index]
                            val isFav by storiesViewModel
                                .isFavorite(story.documentId, story.category.ifBlank { "stories" })
                                .collectAsState()
                            StoryCard(
                                story = story,
                                onClick = {
                                    SoundEffectManager.playClickSound()
                                    onStoryClick(story)
                                },
                                alifbaFont = alifbaFont,
                                isFavorite = isFav,
                                onFavoriteClick = {
                                    storiesViewModel.toggleFavorite(
                                        story.documentId,
                                        story.category.ifBlank { "stories" }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
