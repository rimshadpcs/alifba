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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
// import com.alifba.alifba.presenation.activities.ActivitiesScreen // Temporarily hidden
import com.alifba.alifba.presenation.SubscriptionViewModel
import com.alifba.alifba.presenation.home.HomeViewModel
import com.alifba.alifba.presenation.home.layout.profile.ProfileScreen
import com.alifba.alifba.presenation.stories.AudioPlayerViewModel
import com.alifba.alifba.ui_components.dialogs.ParentGate
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
    val subscriptionViewModel: SubscriptionViewModel = hiltViewModel()
    val isPremium by subscriptionViewModel.isPremium.collectAsState()
    val expiryTimestamp by subscriptionViewModel.discountExpiryTimestamp.collectAsState()
    var showParentGate by remember { mutableStateOf(false) }

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
                    HomeScreenGate(
                        viewModel = viewModel,
                        navController = navController,
                        isUserLoggedIn = isUserLoggedIn,
                        profileViewModel = profileViewModel,
                        chaptersViewModel = chaptersViewModel,
                        onPaywallVisibilityChanged = { isVisible ->
                            showBottomNav = !isVisible
                        }
                    )
                }
                BottomNavDestination.Stories -> {
                    val currentStory by audioPlayerViewModel.currentStory.collectAsState()
                    StoriesWithAudioPlayerScreen(
                        navController = navController,
                        onShowBottomNav = { show -> showBottomNav = show },
                        initialSelectedStory = if (shouldOpenAudioPlayer) currentStory else null,
                        shouldOpenAudioPlayer = shouldOpenAudioPlayer,
                        onAudioPlayerOpened = { shouldOpenAudioPlayer = false }
                    )
                }
                // Activities (Play) — temporarily hidden until feature is ready
                /*
                BottomNavDestination.Activities -> {
                    ActivitiesScreen(
                        onShowBottomNav = { show -> showBottomNav = show },
                        profileViewModel = profileViewModel
                    )
                }
                */
                BottomNavDestination.Account -> {
                    ProfileScreen(
                        navController = navController,
                        profileViewModel = profileViewModel
                    )
                }

                // Activities (Play) — feature not ready yet; the nav item above is commented
                // out so this isn't reachable by tapping, but a TODO() here is a crash waiting
                // for any other path (state restoration, re-enabling the nav item) that lands
                // on this case. Falls back to Stories rather than rendering nothing; the state
                // write is deferred to LaunchedEffect since writing directly during composition
                // is unsafe.
                BottomNavDestination.Activities -> {
                    LaunchedEffect(Unit) {
                        currentDestination = BottomNavDestination.Stories
                    }
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

        // Floating Discount Banner - only on Home tab and when bottom nav is visible (no paywall)
        // TEMP-DEV: DEV_DISABLE_PAYWALL_AND_GATE gate added — see HomeScreen.kt for the flag.
        if (
            !DEV_DISABLE_PAYWALL_AND_GATE &&
            !isPremium &&
            expiryTimestamp > System.currentTimeMillis() &&
            currentDestination == BottomNavDestination.Home &&
            showBottomNav
        ) {
            Box(
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.TopCenter)
                    .padding(top = 8.dp)
            ) {
                DiscountBanner(
                    expiryTimestamp = expiryTimestamp,
                    onClick = { showParentGate = true }
                )
            }
        }

        // Parent Gate for the banner
        if (showParentGate) {
            ParentGate(
                onVerified = {
                    showParentGate = false
                    navController.navigate("discountPaywall")
                },
                onDismiss = { showParentGate = false }
            )
        }
    }
}
