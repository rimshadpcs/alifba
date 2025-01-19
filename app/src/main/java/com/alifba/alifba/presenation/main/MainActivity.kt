package com.alifba.alifba.presenation.main

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alifba.alifba.R
import com.alifba.alifba.features.authentication.OnboardingDataStoreManager
import com.alifba.alifba.presenation.lessonScreens.LessonScreenViewModel
import com.alifba.alifba.presenation.Login.AuthViewModel
import com.alifba.alifba.presenation.Login.LoginScreen
import com.alifba.alifba.presenation.Login.ProfileRegistration
import com.alifba.alifba.presenation.chapters.ChaptersViewModel
import com.alifba.alifba.presenation.home.HomeViewModel
import com.alifba.alifba.presenation.main.layout.AlifbaMainScreen
import com.alifba.alifba.presenation.home.layout.HomeScreen
import com.alifba.alifba.presenation.onboarding.OnboardingScreen
import com.alifba.alifba.ui_components.dialogs.LottieAnimationLoading
import com.alifba.alifba.ui_components.theme.AlifbaTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val lessonScreenViewModel: LessonScreenViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private val chaptersViewModel: ChaptersViewModel by viewModels()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        val onboardingDataStore = OnboardingDataStoreManager(applicationContext)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        setContent {
            val navController = rememberNavController()
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            var startDestination by remember { mutableStateOf<String?>(null) }
            var isSplashScreenVisible by remember { mutableStateOf(true) }

            LaunchedEffect(firebaseUser) {
                // 1) If the user is null, they must log in
                if (firebaseUser == null) {
                    startDestination = "login"
                } else {
                    // 2) Wait until userId is *not* null or empty in DataStore
                    var userId: String? = null
                    // We'll poll or read from DataStore until we get a real userId
                    while (userId.isNullOrEmpty()) {
                        userId = authViewModel.dataStoreManager.userId.first()
                        delay(100) // Wait a bit, or you can do a more sophisticated approach
                    }

                    // 3) Now userId is definitely not null or empty
                    val hasCompletedOnboarding = onboardingDataStore.hasCompletedOnboarding.first()
                    val hasProfiles = authViewModel.checkForChildProfiles()  // safely reads userId from DataStore

                    // 4) Decide the start destination
                    startDestination = when {
                        !hasCompletedOnboarding -> "onboarding"
                        !hasProfiles -> "createProfile"
                        else -> "home"
                    }
                }

                // 5) Give your splash screen at least 1 second, if desired
                delay(1000)
                isSplashScreenVisible = false
            }


            Box(modifier = Modifier.fillMaxSize()) {
                if (startDestination != null && !isSplashScreenVisible) {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination!!,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable("login") {
                            LoginScreen(
                                viewModel = authViewModel,
                                navController = navController
                            )
                        }
                        composable("onboarding") {
                            OnboardingScreen(
                                onComplete = {
                                    lifecycleScope.launch {
                                        onboardingDataStore.setOnboardingCompleted(true)
                                    }
                                    navController.navigate("createProfile") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("createProfile") {
                            ProfileRegistration(navController)
                        }
                        composable("home") {
                            AlifbaMainScreen(
                                lessonScreenViewModel,
                                homeViewModel,
                                chaptersViewModel
                            )
                        }
                    }
                }

                if (isSplashScreenVisible) {
                    SplashScreenDummy(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}





    @Composable
    fun SplashScreenDummy(modifier: Modifier = Modifier) {
        Box(
            modifier = modifier
                .background(color = Color.White)
        ) {
            // Center the Lottie animation
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.Center)
            ) {
                LottieAnimationLoading(
                    showDialog = remember { mutableStateOf(true) },
                    lottieFileRes = R.raw.loading_lottie
                )
            }
        }
    }


    @Preview(showBackground = true)
    @Composable
    fun AlifbaLessonPreview() {
        AlifbaTheme {
            val dummyNavController = rememberNavController()
            HomeScreen(HomeViewModel(), dummyNavController)
        }
    }
