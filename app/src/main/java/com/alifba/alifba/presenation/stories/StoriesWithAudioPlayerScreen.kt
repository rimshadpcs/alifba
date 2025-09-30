package com.alifba.alifba.presenation.stories

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.alifba.alifba.data.models.Story
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager

@Composable
fun StoriesWithAudioPlayerScreen(
    modifier: Modifier = Modifier,
    onShowBottomNav: (Boolean) -> Unit = {},
    initialSelectedStory: Story? = null,
    shouldOpenAudioPlayer: Boolean = false,
    onAudioPlayerOpened: () -> Unit = {}
) {
    var selectedStory by remember { mutableStateOf(initialSelectedStory) }
    var showPremiumUnlock by remember { mutableStateOf(false) }
    var showAllStories by remember { mutableStateOf(false) }
    var showAllSahabaStories by remember { mutableStateOf(false) }
    val audioViewModel: AudioPlayerViewModel = hiltViewModel()
    
    // Handle audio player opening trigger
    LaunchedEffect(shouldOpenAudioPlayer) {
        if (shouldOpenAudioPlayer) {
            selectedStory = audioViewModel.currentStory.value
            onAudioPlayerOpened()
        }
    }
    
    // Update bottom nav visibility based on which screen is showing
    LaunchedEffect(selectedStory, showPremiumUnlock, showAllStories, showAllSahabaStories) {
        onShowBottomNav(selectedStory == null && !showPremiumUnlock && !showAllStories && !showAllSahabaStories) // Hide nav when in audio player, premium screen, or all stories screens
    }
    
    when {
        showPremiumUnlock -> {
            // Show Premium Unlock Screen
            PremiumUnlockScreen(
                onCloseClick = { showPremiumUnlock = false },
                onSubscribeClick = { plan ->
                    // TODO: Handle subscription logic
                    showPremiumUnlock = false
                }
            )
        }
        showAllStories -> {
            // Show All Stories Screen
            AllStoriesScreen(
                onStoryClick = { story ->
                    if (story.status == "premium") {
                        // Show premium unlock for premium stories
                        showPremiumUnlock = true
                    } else {
                        // Play free stories directly
                        audioViewModel.stopAndClearAudio()
                        selectedStory = story
                    }
                },
                onBackClick = { showAllStories = false }
            )
        }
        showAllSahabaStories -> {
            // Show All Sahaba Stories Screen
            AllSahabaStoriesScreen(
                onStoryClick = { story ->
                    if (story.status == "premium") {
                        // Show premium unlock for premium stories
                        showPremiumUnlock = true
                    } else {
                        // Play free stories directly
                        audioViewModel.stopAndClearAudio()
                        selectedStory = story
                    }
                },
                onBackClick = { showAllSahabaStories = false }
            )
        }
        selectedStory != null -> {
            // Show Audio Player Screen (full screen, no bottom nav)
            Box(modifier = Modifier.fillMaxSize()) {
                AudioPlayerScreen(
                    story = selectedStory!!,
                    onBackClick = { selectedStory = null }, // Back to stories list
                    onMinimize = { selectedStory = null }, // Minimize to mini player banner
                    onCancel = { 
                        // Stop audio completely and clear current story
                        audioViewModel.stopAndClearAudio()
                        selectedStory = null
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        else -> {
            // Show Stories Screen (with bottom nav)
            StoriesScreen(
                onStoryClick = { story ->
                    if (story.status == "premium") {
                        // Show premium unlock for premium stories
                        showPremiumUnlock = true
                    } else {
                        // Play free stories directly
                        audioViewModel.stopAndClearAudio()
                        selectedStory = story
                    }
                },
                onMoreClick = { showAllStories = true },
                onProphetMuhammadStoryClick = { story ->
                    if (story.status == "premium") {
                        // Show premium unlock for premium stories
                        showPremiumUnlock = true
                    } else {
                        // Play free stories directly
                        audioViewModel.stopAndClearAudio()
                        selectedStory = story
                    }
                },
                onSahabaStoryClick = { story ->
                    if (story.status == "premium") {
                        // Show premium unlock for premium stories
                        showPremiumUnlock = true
                    } else {
                        // Play free stories directly
                        audioViewModel.stopAndClearAudio()
                        selectedStory = story
                    }
                },
                onSahabaMoreClick = { 
                    SoundEffectManager.playClickSound()
                    showAllSahabaStories = true
                }
            )
        }
    }
}