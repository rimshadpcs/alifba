package com.alifba.alifba.presenation.stories

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.alifba.alifba.R
import com.alifba.alifba.data.local.PlaybackProgressStore
import com.alifba.alifba.data.models.Story
import com.alifba.alifba.presenation.SubscriptionViewModel
import com.alifba.alifba.presenation.home.layout.settings.SettingsGroup
import com.alifba.alifba.ui_components.widgets.AuthTheme
import com.alifba.alifba.ui_components.widgets.LessonCircleButton
import com.alifba.alifba.ui_components.widgets.LessonTheme
import com.alifba.alifba.ui_components.widgets.PullToRefreshLazyColumn
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager
import kotlinx.coroutines.launch

// Light blue-grey page, shared with Settings and Account.
val StoriesPageBackground = Color(0xFFF6F8FD)

/** m:ss, the way the player and the Continue listening card show time */
fun storyTimeText(milliseconds: Long): String {
    val total = (milliseconds / 1000L).coerceAtLeast(0L)
    return "${total / 60}:${(total % 60).toString().padStart(2, '0')}"
}

private fun storyDedupKey(s: Story): String = when {
    s.audio.isNotEmpty() -> "audio::${s.audio}"
    s.documentId.isNotEmpty() -> "doc::${s.documentId}"
    s.background.isNotEmpty() -> "bg::${s.background}"
    else -> "name::${s.name}"
}

private fun uniqueStories(list: List<Story>): List<Story> = list.distinctBy { storyDedupKey(it) }

/** Favourites are stored as "<category>:<documentId>"; this keeps the stories' own order. */
private fun favoriteStoriesOf(allStories: List<Story>, favoriteKeys: Set<String>): List<Story> {
    val normalized = favoriteKeys.map { key -> if (":" in key) key else "stories:$key" }.toSet()
    return allStories.filter { story ->
        (story.category.ifBlank { "stories" } + ":" + story.documentId) in normalized
    }
}

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
    val k = LessonTheme.scale()
    val isTablet = LessonTheme.isTablet()

    val favoriteIds by storiesViewModel.getFavoriteStories().collectAsState()

    val stories by storiesViewModel.stories.collectAsState()
    val isLoading by storiesViewModel.isLoading.collectAsState()
    val error by storiesViewModel.error.collectAsState()

    val prophetMuhammadStories by storiesViewModel.prophetMuhammadStories.collectAsState()
    val isProphetMuhammadLoading by storiesViewModel.isProphetMuhammadLoading.collectAsState()

    val sahabaStories by storiesViewModel.sahabaStories.collectAsState()
    val isSahabaLoading by storiesViewModel.isSahabaLoading.collectAsState()

    val womenAndMothersStories by storiesViewModel.womenAndMothersStories.collectAsState()
    val isWomenAndMothersLoading by storiesViewModel.isWomenAndMothersLoading.collectAsState()

    val miraclesStories by storiesViewModel.miraclesStories.collectAsState()
    val isMiraclesLoading by storiesViewModel.isMiraclesLoading.collectAsState()

    // Progress store for the Continue listening card
    val context = LocalContext.current
    val progressStore = remember { PlaybackProgressStore(context) }

    var isRefreshing by remember { mutableStateOf(false) }
    val handleRefresh = {
        isRefreshing = true
        storiesViewModel.forceRefreshAll()
    }
    LaunchedEffect(isLoading, isProphetMuhammadLoading, isSahabaLoading, isWomenAndMothersLoading, isMiraclesLoading) {
        if (!isLoading && !isProphetMuhammadLoading && !isSahabaLoading && !isWomenAndMothersLoading && !isMiraclesLoading) {
            isRefreshing = false
        }
    }

    val listState = rememberLazyListState()
    // Start at the top when entering, and again when data finishes loading (avoids mid-list starts)
    LaunchedEffect(Unit) { listState.scrollToItem(0) }
    LaunchedEffect(isLoading, isProphetMuhammadLoading, isSahabaLoading, isWomenAndMothersLoading, isMiraclesLoading) {
        if (!isLoading && !isProphetMuhammadLoading && !isSahabaLoading && !isWomenAndMothersLoading && !isMiraclesLoading) {
            listState.scrollToItem(0)
        }
    }

    val allStories = remember(stories, prophetMuhammadStories, sahabaStories, womenAndMothersStories, miraclesStories) {
        buildList {
            addAll(stories)
            addAll(prophetMuhammadStories)
            addAll(sahabaStories)
            addAll(womenAndMothersStories)
            addAll(miraclesStories)
        }
    }
    val favoriteStories = remember(allStories, favoriteIds) { favoriteStoriesOf(allStories, favoriteIds) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StoriesPageBackground)
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
            item {
                Text(
                    text = "Stories",
                    style = LessonTheme.question().copy(fontSize = (34 * k).sp, textAlign = TextAlign.Start),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 12.dp)
                )
            }

            item {
                HeroBanner(
                    stories = allStories,
                    onPlayClick = onStoryClick,
                    progressStore = progressStore
                )
            }

            if (favoriteStories.isNotEmpty()) {
                item {
                    StorySection(
                        title = "Your favourites",
                        stories = favoriteStories.take(5),
                        isLoading = false,
                        loadingText = "",
                        emptyText = "No favourites yet",
                        keepDuplicates = true,
                        onStoryClick = onStoryClick,
                        onMoreClick = onFavoritesMoreClick,
                        storiesViewModel = storiesViewModel
                    )
                }
            }

            item {
                StorySection(
                    title = "Stories of the Prophets",
                    stories = stories,
                    isLoading = isLoading,
                    loadingText = "Loading stories...",
                    emptyText = "No stories found",
                    error = error,
                    onRetry = { storiesViewModel.forceRefreshStories() },
                    onStoryClick = onStoryClick,
                    onMoreClick = onMoreClick,
                    storiesViewModel = storiesViewModel
                )
            }

            item {
                StorySection(
                    title = "Life of Prophet Muhammad ﷺ",
                    stories = prophetMuhammadStories,
                    isLoading = isProphetMuhammadLoading,
                    loadingText = "Loading...",
                    emptyText = "No stories available",
                    onStoryClick = onProphetMuhammadStoryClick,
                    onMoreClick = onProphetMuhammadMoreClick,
                    storiesViewModel = storiesViewModel
                )
            }

            item {
                StorySection(
                    title = "Sahaba Stories",
                    stories = sahabaStories,
                    isLoading = isSahabaLoading,
                    loadingText = "Loading Sahaba stories...",
                    emptyText = "No Sahaba stories found",
                    onStoryClick = onSahabaStoryClick,
                    onMoreClick = onSahabaMoreClick,
                    storiesViewModel = storiesViewModel
                )
            }

            item {
                StorySection(
                    title = "Women and Mothers of Islam",
                    stories = womenAndMothersStories,
                    isLoading = isWomenAndMothersLoading,
                    loadingText = "Loading...",
                    emptyText = "No stories available",
                    onStoryClick = onWomenAndMothersStoryClick,
                    onMoreClick = onWomenAndMothersMoreClick,
                    storiesViewModel = storiesViewModel
                )
            }

            item {
                StorySection(
                    title = "Miracles in Quran",
                    stories = miraclesStories,
                    isLoading = isMiraclesLoading,
                    loadingText = "Loading...",
                    emptyText = "No stories available",
                    onStoryClick = onMiraclesStoryClick,
                    onMoreClick = onMiraclesMoreClick,
                    storiesViewModel = storiesViewModel
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Section: title + "See all" + a row of cards
// ---------------------------------------------------------------------------------------------

@Composable
fun StorySection(
    title: String,
    stories: List<Story>,
    isLoading: Boolean,
    loadingText: String,
    emptyText: String,
    onStoryClick: (Story) -> Unit,
    onMoreClick: () -> Unit,
    storiesViewModel: StoriesViewModel,
    keepDuplicates: Boolean = false,
    error: String? = null,
    onRetry: () -> Unit = {}
) {
    val k = LessonTheme.scale()
    val isTablet = LessonTheme.isTablet()
    val subscriptionViewModel: SubscriptionViewModel = hiltViewModel()
    val isPremium by subscriptionViewModel.isPremium.collectAsState()

    Column(modifier = Modifier.padding(top = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = LessonTheme.question().copy(fontSize = (24 * k).sp, textAlign = TextAlign.Start),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Row(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        SoundEffectManager.playClickSound()
                        onMoreClick()
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "See all",
                    style = LessonTheme.answer().copy(fontSize = (18 * k).sp, color = LessonTheme.primaryEdge)
                )
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = LessonTheme.primaryEdge,
                    modifier = Modifier.size((22 * k).dp)
                )
            }
        }

        when {
            isLoading -> StoryStateText(loadingText)
            error != null && stories.isEmpty() -> Column(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Couldn't load stories.",
                    style = LessonTheme.answer().copy(fontSize = (17 * k).sp, color = LessonTheme.wrongText)
                )
                Text(
                    text = "Try again",
                    style = LessonTheme.answer().copy(fontSize = (17 * k).sp, color = LessonTheme.primaryEdge),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable { onRetry() }
                )
            }
            stories.isEmpty() -> StoryStateText(emptyText)
            else -> {
                val shown = (if (keepDuplicates) stories else uniqueStories(stories)).take(5)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(shown, key = { it.category + "|" + it.documentId }) { story ->
                        val isFav by storiesViewModel
                            .isFavorite(story.documentId, story.category.ifBlank { "stories" })
                            .collectAsState()
                        StoryCard(
                            story = story,
                            modifier = Modifier.width(if (isTablet) 200.dp else 144.dp),
                            showFreeBadge = !isPremium,
                            isFavorite = isFav,
                            onFavoriteClick = {
                                storiesViewModel.toggleFavorite(story.documentId, story.category.ifBlank { "stories" })
                            },
                            onClick = {
                                SoundEffectManager.playClickSound()
                                onStoryClick(story)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StoryStateText(text: String) {
    val k = LessonTheme.scale()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = LessonTheme.answer().copy(fontSize = (17 * k).sp, color = AuthTheme.secondaryText)
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Story card: square cover on a raised edge, title underneath
// ---------------------------------------------------------------------------------------------

@Composable
fun StoryCard(
    story: Story,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showFreeBadge: Boolean = false,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {}
) {
    val k = LessonTheme.scale()
    val shape = RoundedCornerShape(20.dp)
    val edge = LessonTheme.answerEdge
    val imageUrl = remember(story.thumbnail, story.background) {
        story.thumbnail.ifBlank { story.background }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = edge)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .offset(y = edge)
                    .background(LessonTheme.lineEdge, shape)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(shape)
                    .background(LessonTheme.track)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = story.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (story.isLocked) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopStart)
                            .size((32 * k).dp)
                            .background(LessonTheme.ink.copy(alpha = 0.55f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.padlock),
                            contentDescription = "Locked",
                            modifier = Modifier.size((16 * k).dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                }

                // FREE pill (bottom-right) — hidden for premium users
                if (showFreeBadge && story.status == "free") {
                    Text(
                        text = "FREE",
                        style = LessonTheme.answer().copy(
                            fontSize = (12 * k).sp,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.BottomEnd)
                            .background(LessonTheme.correct, CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            StoryHeart(
                isFavorite = isFavorite,
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
            )
        }

        Text(
            text = story.name,
            style = LessonTheme.answer().copy(fontSize = (18 * k).sp, textAlign = TextAlign.Start),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp)
        )
    }
}

/** Small white disc that sinks onto its edge when pressed, with a little bounce on toggle */
@Composable
private fun StoryHeart(isFavorite: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val k = LessonTheme.scale()
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val lift by animateDpAsState(if (pressed) 0.dp else 3.dp, label = "heartLift")

    Box(
        modifier = modifier
            .size((34 * k).dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
    ) {
        Box(modifier = Modifier.matchParentSize().background(LessonTheme.lineEdge, CircleShape))
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(bottom = lift)
                .background(Color.White, CircleShape)
                .clickable(interactionSource = interaction, indication = null) {
                    scope.launch {
                        scale.animateTo(0.8f, animationSpec = tween(80))
                        scale.animateTo(1.2f, animationSpec = tween(100))
                        scale.animateTo(
                            1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (isFavorite) "Remove from favourites" else "Add to favourites",
                tint = if (isFavorite) LessonTheme.wrong else Color(0xFFA3A9BC),
                modifier = Modifier.size((18 * k).dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Continue listening / Up next card
// ---------------------------------------------------------------------------------------------

@Composable
private fun HeroBanner(
    stories: List<Story>,
    onPlayClick: (Story) -> Unit,
    progressStore: PlaybackProgressStore
) {
    if (stories.isEmpty()) return
    val k = LessonTheme.scale()

    val lastPlayedId = remember(stories) { progressStore.getLastPlayed() }
    val lastPlayed = remember(lastPlayedId, stories) { stories.find { it.documentId == lastPlayedId } }
    val lastProgress = remember(lastPlayedId) { lastPlayedId?.let { progressStore.getProgress(it) } }
    val showResume = lastPlayed != null && lastProgress != null && lastProgress.duration > 0 &&
        lastProgress.position < (lastProgress.duration * 0.98f)
    val target = when {
        showResume -> lastPlayed
        else -> stories.firstOrNull { !progressStore.getProgress(it.documentId).completed } ?: stories.first()
    } ?: return

    val progress = remember(target.documentId) { progressStore.getProgress(target.documentId) }
    val fraction = if (progress.duration > 0L) (progress.position.toFloat() / progress.duration.toFloat()).coerceIn(0f, 1f) else 0f
    val inProgress = fraction > 0f && fraction < 1f

    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
        SettingsGroup {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        SoundEffectManager.playClickSound()
                        onPlayClick(target)
                    }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AsyncImage(
                    model = target.thumbnail.ifBlank { target.background },
                    contentDescription = target.name,
                    modifier = Modifier
                        .size((88 * k).dp)
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = (if (showResume) "Continue listening" else "Up next").uppercase(),
                        style = LessonTheme.answer().copy(
                            fontSize = (13 * k).sp,
                            letterSpacing = 1.2.sp,
                            color = AuthTheme.secondaryText,
                            textAlign = TextAlign.Start
                        )
                    )
                    Text(
                        text = target.name,
                        style = LessonTheme.answer().copy(
                            fontSize = (21 * k).sp,
                            textAlign = TextAlign.Start,
                            lineHeight = 1.2.em
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (inProgress) {
                        Row(
                            modifier = Modifier.padding(top = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(7.dp)
                                    .clip(CircleShape)
                                    .background(LessonTheme.track)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction.coerceAtLeast(0.04f))
                                        .height(7.dp)
                                        .background(LessonTheme.primary, CircleShape)
                                )
                            }
                            Text(
                                text = "${storyTimeText(progress.duration - progress.position)} left",
                                style = LessonTheme.answer().copy(
                                    fontSize = (15 * k).sp,
                                    color = AuthTheme.secondaryText
                                )
                            )
                        }
                    } else if (target.durationSeconds > 0) {
                        Text(
                            text = storyTimeText(target.durationSeconds * 1000L),
                            style = LessonTheme.answer().copy(
                                fontSize = (15 * k).sp,
                                color = AuthTheme.secondaryText,
                                textAlign = TextAlign.Start
                            ),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // Raised round play button
                Box(modifier = Modifier.padding(bottom = 4.dp).size((54 * k).dp)) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(y = 4.dp)
                            .background(LessonTheme.primaryEdge, CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(LessonTheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size((30 * k).dp)
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// See all
// ---------------------------------------------------------------------------------------------

@Composable
private fun StoryGridScreen(
    title: String,
    stories: List<Story>,
    isLoading: Boolean,
    emptyText: String,
    error: String?,
    keepDuplicates: Boolean,
    onStoryClick: (Story) -> Unit,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    storiesViewModel: StoriesViewModel
) {
    val k = LessonTheme.scale()
    val subscriptionViewModel: SubscriptionViewModel = hiltViewModel()
    val isPremium by subscriptionViewModel.isPremium.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StoriesPageBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Round back button, then the title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                LessonCircleButton(
                    iconRes = R.drawable.backarrow,
                    contentDescription = "Back",
                    onClick = onBackClick
                )
                Text(
                    text = title,
                    style = LessonTheme.question().copy(fontSize = (26 * k).sp, textAlign = TextAlign.Start),
                    maxLines = 2,
                    modifier = Modifier.weight(1f)
                )
            }

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Loading stories...",
                        style = LessonTheme.answer().copy(fontSize = (17 * k).sp, color = AuthTheme.secondaryText)
                    )
                }
                error != null && stories.isEmpty() -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Couldn't load stories.",
                        style = LessonTheme.answer().copy(fontSize = (17 * k).sp, color = LessonTheme.wrongText)
                    )
                    Text(
                        text = "Try again",
                        style = LessonTheme.answer().copy(fontSize = (17 * k).sp, color = LessonTheme.primaryEdge),
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clickable { onRetry() }
                    )
                }
                stories.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = emptyText,
                        style = LessonTheme.answer().copy(fontSize = (17 * k).sp, color = AuthTheme.secondaryText)
                    )
                }
                else -> {
                    val shown = if (keepDuplicates) stories else uniqueStories(stories)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 40.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(shown, key = { it.category + "|" + it.documentId }) { story ->
                            val isFav by storiesViewModel
                                .isFavorite(story.documentId, story.category.ifBlank { "stories" })
                                .collectAsState()
                            StoryCard(
                                story = story,
                                showFreeBadge = !isPremium,
                                isFavorite = isFav,
                                onFavoriteClick = {
                                    storiesViewModel.toggleFavorite(story.documentId, story.category.ifBlank { "stories" })
                                },
                                onClick = {
                                    SoundEffectManager.playClickSound()
                                    onStoryClick(story)
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
fun AllStoriesScreen(
    storiesViewModel: StoriesViewModel = hiltViewModel(),
    onStoryClick: (Story) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val stories by storiesViewModel.stories.collectAsState()
    val isLoading by storiesViewModel.isLoading.collectAsState()
    val error by storiesViewModel.error.collectAsState()
    StoryGridScreen(
        title = "Stories of the Prophets",
        stories = stories,
        isLoading = isLoading,
        emptyText = "No stories found",
        error = error,
        keepDuplicates = false,
        onStoryClick = onStoryClick,
        onBackClick = onBackClick,
        onRetry = { storiesViewModel.forceRefreshStories() },
        storiesViewModel = storiesViewModel
    )
}

@Composable
fun AllProphetMuhammadStoriesScreen(
    storiesViewModel: StoriesViewModel = hiltViewModel(),
    onStoryClick: (Story) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val stories by storiesViewModel.prophetMuhammadStories.collectAsState()
    val isLoading by storiesViewModel.isProphetMuhammadLoading.collectAsState()
    StoryGridScreen(
        title = "Life of Prophet Muhammad ﷺ",
        stories = stories,
        isLoading = isLoading,
        emptyText = "No stories available",
        error = null,
        keepDuplicates = false,
        onStoryClick = onStoryClick,
        onBackClick = onBackClick,
        onRetry = { storiesViewModel.forceRefreshProphetMuhammadStories() },
        storiesViewModel = storiesViewModel
    )
}

@Composable
fun AllSahabaStoriesScreen(
    storiesViewModel: StoriesViewModel = hiltViewModel(),
    onStoryClick: (Story) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val stories by storiesViewModel.sahabaStories.collectAsState()
    val isLoading by storiesViewModel.isSahabaLoading.collectAsState()
    StoryGridScreen(
        title = "Sahaba Stories",
        stories = stories,
        isLoading = isLoading,
        emptyText = "No Sahaba stories found",
        error = null,
        keepDuplicates = false,
        onStoryClick = onStoryClick,
        onBackClick = onBackClick,
        onRetry = { storiesViewModel.forceRefreshSahabaStories() },
        storiesViewModel = storiesViewModel
    )
}

@Composable
fun AllWomenAndMothersStoriesScreen(
    storiesViewModel: StoriesViewModel = hiltViewModel(),
    onStoryClick: (Story) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val stories by storiesViewModel.womenAndMothersStories.collectAsState()
    val isLoading by storiesViewModel.isWomenAndMothersLoading.collectAsState()
    StoryGridScreen(
        title = "Women and Mothers of Islam",
        stories = stories,
        isLoading = isLoading,
        emptyText = "No stories available",
        error = null,
        keepDuplicates = false,
        onStoryClick = onStoryClick,
        onBackClick = onBackClick,
        onRetry = { storiesViewModel.forceRefreshWomenAndMothersStories() },
        storiesViewModel = storiesViewModel
    )
}

@Composable
fun AllMiraclesStoriesScreen(
    storiesViewModel: StoriesViewModel = hiltViewModel(),
    onStoryClick: (Story) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val stories by storiesViewModel.miraclesStories.collectAsState()
    val isLoading by storiesViewModel.isMiraclesLoading.collectAsState()
    StoryGridScreen(
        title = "Miracles in Quran",
        stories = stories,
        isLoading = isLoading,
        emptyText = "No stories available",
        error = null,
        keepDuplicates = false,
        onStoryClick = onStoryClick,
        onBackClick = onBackClick,
        onRetry = { storiesViewModel.forceRefreshMiraclesStories() },
        storiesViewModel = storiesViewModel
    )
}

@Composable
fun AllFavoritesScreen(
    storiesViewModel: StoriesViewModel = hiltViewModel(),
    onStoryClick: (Story) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
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
    val favoriteStories = remember(allStories, favoriteKeys) { favoriteStoriesOf(allStories, favoriteKeys) }

    StoryGridScreen(
        title = "Your favourites",
        stories = favoriteStories,
        isLoading = false,
        emptyText = "No favourites yet",
        error = null,
        keepDuplicates = true,
        onStoryClick = onStoryClick,
        onBackClick = onBackClick,
        onRetry = {},
        storiesViewModel = storiesViewModel
    )
}
