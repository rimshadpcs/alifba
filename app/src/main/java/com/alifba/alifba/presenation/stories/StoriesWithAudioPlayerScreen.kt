package com.alifba.alifba.presenation.stories

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.alifba.alifba.data.models.Story

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
    val audioViewModel: AudioPlayerViewModel = hiltViewModel()
    
    // Handle audio player opening trigger
    LaunchedEffect(shouldOpenAudioPlayer) {
        if (shouldOpenAudioPlayer) {
            selectedStory = audioViewModel.currentStory.value
            onAudioPlayerOpened()
        }
    }
    
    // Update bottom nav visibility based on which screen is showing
    LaunchedEffect(selectedStory, showPremiumUnlock) {
        onShowBottomNav(selectedStory == null && !showPremiumUnlock) // Hide nav when in audio player or premium screen
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
                }
            )
        }
    }
}