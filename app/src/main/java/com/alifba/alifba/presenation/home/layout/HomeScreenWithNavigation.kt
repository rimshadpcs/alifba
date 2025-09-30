package com.alifba.alifba.presenation.home.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.alifba.alifba.presenation.activities.ActivitiesScreen
import com.alifba.alifba.presenation.home.HomeViewModel
import com.alifba.alifba.presenation.home.layout.profile.ProfileScreen
import com.alifba.alifba.presenation.stories.AudioPlayerViewModel
import com.alifba.alifba.presenation.stories.StoriesWithAudioPlayerScreen
import com.alifba.alifba.ui_components.navigation.BottomNavigationBar
import com.alifba.alifba.ui_components.navigation.BottomNavDestination
import com.alifba.alifba.ui_components.theme.Beige
import com.alifba.alifba.ui_components.widgets.MiniPlayer

@Composable
fun HomeScreenWithNavigation(
    viewModel: HomeViewModel,
    navController: NavController,
    isUserLoggedIn: Boolean,
    profileViewModel: ProfileViewModel,
    chaptersViewModel: com.alifba.alifba.presenation.chapters.ChaptersViewModel
) {
    // Start ProfileViewModel real-time listener to get immediate updates
    LaunchedEffect(profileViewModel) {
        android.util.Log.d("HomeRefresh", "Starting ProfileViewModel listener for real-time updates")
        profileViewModel.startProfileListener()
    }
    
    // Stop listener when component is disposed to prevent memory leaks
    DisposableEffect(profileViewModel) {
        onDispose {
            android.util.Log.d("HomeRefresh", "Stopping ProfileViewModel listener")
            profileViewModel.stopProfileListener()
        }
    }
    
    
    
    var currentDestination by remember { mutableStateOf(BottomNavDestination.Home) }
    var showBottomNav by remember { mutableStateOf(true) }
    var shouldOpenAudioPlayer by remember { mutableStateOf(false) }
    val audioPlayerViewModel: AudioPlayerViewModel = hiltViewModel()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Main content with conditional bottom padding 
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showBottomNav) 80.dp else 0.dp) // Only add padding when bottom nav is visible
        ) {
            when (currentDestination) {
                BottomNavDestination.Home -> {
                    HomeScreen(
                        viewModel = viewModel,
                        navController = navController,
                        isUserLoggedIn = isUserLoggedIn,
                        profileViewModel = profileViewModel,
                        chaptersViewModel = chaptersViewModel
                    )
                }
                BottomNavDestination.Stories -> {
                    val currentStory by audioPlayerViewModel.currentStory.collectAsState()
                    StoriesWithAudioPlayerScreen(
                        onShowBottomNav = { show -> showBottomNav = show },
                        initialSelectedStory = if (shouldOpenAudioPlayer) currentStory else null,
                        shouldOpenAudioPlayer = shouldOpenAudioPlayer,
                        onAudioPlayerOpened = { shouldOpenAudioPlayer = false }
                    )
                }
                BottomNavDestination.Activities -> {
                    ActivitiesScreen(
                        onShowBottomNav = { show -> showBottomNav = show },
                        profileViewModel = profileViewModel
                    )
                }
                BottomNavDestination.Account -> {
                    ProfileScreen(
                        navController = navController,
                        profileViewModel = profileViewModel
                    )
                }
            }
        }
        
        // Mini player overlay (floating above bottom navigation) - only show when not in audio player
        MiniPlayer(
            onExpandClick = {
                if (currentDestination == BottomNavDestination.Stories) {
                    // Already in Stories, trigger audio player to open
                    shouldOpenAudioPlayer = true
                } else {
                    // Switch to Stories and open audio player
                    currentDestination = BottomNavDestination.Stories
                    shouldOpenAudioPlayer = true
                }
            },
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = if (showBottomNav) 112.dp else 16.dp), // Increased from 96dp to 112dp for better spacing
            isInAudioPlayer = !showBottomNav,
            onClose = {
                // Stop audio and hide mini player by clearing the current story
            }
        )
        
        // Bottom navigation overlay - only show when not in audio player
        if (showBottomNav) {
            Box(
                modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
            ) {
                BottomNavigationBar(
                    currentDestination = currentDestination,
                    onDestinationClick = { destination ->
                        currentDestination = destination
                    },
                    profileViewModel = profileViewModel
                )
            }
        }
    }
}