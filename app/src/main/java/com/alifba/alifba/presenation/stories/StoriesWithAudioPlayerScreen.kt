package com.alifba.alifba.presenation.stories

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.alifba.alifba.data.models.Story
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager
import com.alifba.alifba.presenation.SubscriptionViewModel
import androidx.navigation.NavController

@Composable
fun StoriesWithAudioPlayerScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    onShowBottomNav: (Boolean) -> Unit = {},
    initialSelectedStory: Story? = null,
    shouldOpenAudioPlayer: Boolean = false,
    onAudioPlayerOpened: () -> Unit = {}
) {
    var selectedStory by remember { mutableStateOf(initialSelectedStory) }
    var showPremiumUnlock by remember { mutableStateOf(false) }
    var showAllFavorites by remember { mutableStateOf(false) }
    var showAllStories by remember { mutableStateOf(false) }
    var showAllSahabaStories by remember { mutableStateOf(false) }
    var showAllProphetMuhammadStories by remember { mutableStateOf(false) }
    var showAllWomenAndMothersStories by remember { mutableStateOf(false) }
    var showAllMiraclesStories by remember { mutableStateOf(false) }
    val audioViewModel: AudioPlayerViewModel = hiltViewModel()
    val subscriptionViewModel: SubscriptionViewModel = hiltViewModel()
    val isPremium by subscriptionViewModel.isPremium.collectAsState()
    val hasSeenDownsellModal by subscriptionViewModel.hasSeenDownsellModal.collectAsState()
    val skipCount by subscriptionViewModel.standardPaywallSkipCount.collectAsState()
    
    // Handle audio player opening trigger
    LaunchedEffect(shouldOpenAudioPlayer) {
        if (shouldOpenAudioPlayer) {
            selectedStory = audioViewModel.currentStory.value
            onAudioPlayerOpened()
        }
    }
    
    // Update bottom nav visibility based on which screen is showing
    LaunchedEffect(selectedStory, showPremiumUnlock, showAllStories, showAllSahabaStories, showAllProphetMuhammadStories, showAllWomenAndMothersStories, showAllMiraclesStories, showAllFavorites) {
        onShowBottomNav(
            selectedStory == null && !showPremiumUnlock && !showAllStories && !showAllSahabaStories && !showAllProphetMuhammadStories && !showAllWomenAndMothersStories && !showAllMiraclesStories && !showAllFavorites
        ) // Hide nav when in audio player, premium screen, or all stories screens
    }
    
    when {
        showPremiumUnlock -> {
            RevenueCatPaywall(
                onClose = {
                    showPremiumUnlock = false
                    if (!isPremium) {
                        val isEveryThirdSkip = skipCount > 0 && (skipCount + 1) % 3 == 0
                        val isFirstSkipEver = !hasSeenDownsellModal

                        val shouldShowDiscount = isFirstSkipEver || isEveryThirdSkip

                        if (shouldShowDiscount) {
                            if (isFirstSkipEver) {
                                subscriptionViewModel.setHasSeenDownsellModal(true)
                            }
                            subscriptionViewModel.incrementStandardSkipCount()
                            navController.navigate("discountPaywall")
                        } else {
                            subscriptionViewModel.incrementStandardSkipCount()
                        }
                    }
                },
                source = "audio_story"
            )
        }
        showAllFavorites -> {
            // Show All Favorites Screen
            AllFavoritesScreen(
                onStoryClick = { story ->
                    if (story.status == "premium" && !isPremium) {
                        showPremiumUnlock = true
                    } else {
                        audioViewModel.stopAndClearAudio()
                        showAllFavorites = false
                        selectedStory = story
                    }
                },
                onBackClick = { showAllFavorites = false }
            )
        }
        showAllStories -> {
            // Show All Stories Screen — same pastel tint as its section on the Stories screen.
            AllStoriesScreen(
                onStoryClick = { story ->
                    if (story.status == "premium" && !isPremium) {
                        showPremiumUnlock = true
                    } else {
                        audioViewModel.stopAndClearAudio()
                        showAllStories = false
                        selectedStory = story
                    }
                },
                onBackClick = { showAllStories = false },
                backgroundTint = storySectionTints[0]
            )
        }
        showAllSahabaStories -> {
            // Show All Sahaba Stories Screen
            AllSahabaStoriesScreen(
                onStoryClick = { story ->
                    if (story.status == "premium" && !isPremium) {
                        showPremiumUnlock = true
                    } else {
                        audioViewModel.stopAndClearAudio()
                        showAllSahabaStories = false
                        selectedStory = story
                    }
                },
                onBackClick = { showAllSahabaStories = false },
                backgroundTint = storySectionTints[2]
            )
        }
        showAllProphetMuhammadStories -> {
            // Show All Prophet Muhammad Stories Screen
            AllProphetMuhammadStoriesScreen(
                onStoryClick = { story ->
                    if (story.status == "premium" && !isPremium) {
                        showPremiumUnlock = true
                    } else {
                        audioViewModel.stopAndClearAudio()
                        showAllProphetMuhammadStories = false
                        selectedStory = story
                    }
                },
                onBackClick = { showAllProphetMuhammadStories = false },
                backgroundTint = storySectionTints[1]
            )
        }
        showAllWomenAndMothersStories -> {
            // Show All Women and Mothers Stories Screen
            AllWomenAndMothersStoriesScreen(
                onStoryClick = { story ->
                    if (story.status == "premium" && !isPremium) {
                        showPremiumUnlock = true
                    } else {
                        audioViewModel.stopAndClearAudio()
                        showAllWomenAndMothersStories = false
                        selectedStory = story
                    }
                },
                onBackClick = { showAllWomenAndMothersStories = false },
                backgroundTint = storySectionTints[3]
            )
        }
        showAllMiraclesStories -> {
            // Show All Miracles Stories Screen
            AllMiraclesStoriesScreen(
                onStoryClick = { story ->
                    if (story.status == "premium" && !isPremium) {
                        showPremiumUnlock = true
                    } else {
                        audioViewModel.stopAndClearAudio()
                        showAllMiraclesStories = false
                        selectedStory = story
                    }
                },
                onBackClick = { showAllMiraclesStories = false },
                backgroundTint = storySectionTints[4]
            )
        }
        selectedStory != null -> {
            BackHandler {
                selectedStory = null
            }
            Box(modifier = Modifier.fillMaxSize()) {
                AudioPlayerScreen(
                    story = selectedStory!!,
                    onBackClick = { selectedStory = null },
                    onMinimize = { selectedStory = null },
                    onCancel = { 
                        audioViewModel.stopAndClearAudio()
                        selectedStory = null
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        else -> {
            StoriesScreen(
                onStoryClick = { story ->
                    if (story.status == "premium" && !isPremium) {
                        showPremiumUnlock = true
                    } else {
                        audioViewModel.stopAndClearAudio()
                        selectedStory = story
                    }
                },
                onMoreClick = { showAllStories = true },
                onFavoritesMoreClick = { showAllFavorites = true },
                onProphetMuhammadStoryClick = { story ->
                    if (story.status == "premium" && !isPremium) {
                        showPremiumUnlock = true
                    } else {
                        audioViewModel.stopAndClearAudio()
                        selectedStory = story
                    }
                },
                onProphetMuhammadMoreClick = { 
                    SoundEffectManager.playClickSound()
                    showAllProphetMuhammadStories = true 
                },
                onSahabaStoryClick = { story ->
                    if (story.status == "premium" && !isPremium) {
                        showPremiumUnlock = true
                    } else {
                        audioViewModel.stopAndClearAudio()
                        selectedStory = story
                    }
                },
                onSahabaMoreClick = {
                    SoundEffectManager.playClickSound()
                    showAllSahabaStories = true
                },
                onWomenAndMothersStoryClick = { story ->
                    if (story.status == "premium" && !isPremium) {
                        showPremiumUnlock = true
                    } else {
                        audioViewModel.stopAndClearAudio()
                        selectedStory = story
                    }
                },
                onWomenAndMothersMoreClick = {
                    SoundEffectManager.playClickSound()
                    showAllWomenAndMothersStories = true
                },
                onMiraclesStoryClick = { story ->
                    if (story.status == "premium" && !isPremium) {
                        showPremiumUnlock = true
                    } else {
                        audioViewModel.stopAndClearAudio()
                        selectedStory = story
                    }
                },
                onMiraclesMoreClick = {
                    SoundEffectManager.playClickSound()
                    showAllMiraclesStories = true
                }
            )
        }
    }
}
