package com.alifba.alifba.presenation.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alifba.alifba.R
import com.alifba.alifba.features.authentication.DataStoreManager
import com.alifba.alifba.features.authentication.OnboardingDataStoreManager
import com.alifba.alifba.presenation.lessonScreens.LessonScreenViewModel
import com.alifba.alifba.presenation.login.AuthViewModel
import com.alifba.alifba.presenation.login.LoginScreen
import com.alifba.alifba.presenation.login.ProfileRegistration
import com.alifba.alifba.presenation.chapters.ChaptersViewModel
import com.alifba.alifba.presenation.home.HomeViewModel
import com.alifba.alifba.presenation.main.layout.AlifbaMainScreen
import com.alifba.alifba.presenation.home.layout.HomeScreen
import com.alifba.alifba.presenation.home.layout.ProfileViewModel
import com.alifba.alifba.presenation.onboarding.OnboardingScreen
import com.alifba.alifba.service.logNotificationEvent
import com.alifba.alifba.ui_components.dialogs.LottieAnimationLoading
import com.alifba.alifba.ui_components.theme.AlifbaTheme
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var dataStoreManager: DataStoreManager

    private val lessonScreenViewModel: LessonScreenViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private val chaptersViewModel: ChaptersViewModel by viewModels()
    private val profileViewModel:ProfileViewModel by viewModels()
    val Context.dataStore by preferencesDataStore(name = "settings")

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Track notifications when app is already running
        trackNotificationIfNeeded(intent)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        val onboardingDataStore = OnboardingDataStoreManager(applicationContext)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        trackNotificationIfNeeded(intent)
        setContent {
            val navController = rememberNavController()
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            var startDestination by remember { mutableStateOf("login") } // Default to login
            var isSplashScreenVisible by remember { mutableStateOf(true) }
            val context = LocalContext.current
            LaunchedEffect(firebaseUser) {
                if (firebaseUser == null) {
                    startDestination = "login"
                } else {
                    var userId: String? = null
                    while (userId.isNullOrEmpty()) {
                        userId = authViewModel.dataStoreManager.userId.first()
                        delay(100)
                    }

                    val hasCompletedOnboarding = onboardingDataStore.hasCompletedOnboarding.first()
                    val hasProfiles = authViewModel.checkForChildProfiles()

                    startDestination = when {
                        !hasCompletedOnboarding -> "onboarding"  // ✅ Ensure onboarding is reachable
                        !hasProfiles -> "createProfile"
                        else -> "homeScreen"
                    }
                }

                delay(1000) // Ensure splash animation completes
                isSplashScreenVisible = false
            }
            LaunchedEffect(Unit) {
                val userId = authViewModel.dataStoreManager.userId.first()
                if (userId != null) {
                    authViewModel.updateTimeZoneIfNeeded(context, userId)
                }
            }



            Box(modifier = Modifier.fillMaxSize()) {
                if (!isSplashScreenVisible) {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable("login") {
                            LoginScreen(viewModel = authViewModel, navController = navController)
                        }
                        composable("onboarding") {  // ✅ Ensure this exists
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

                        composable("homeScreen") {
                            AlifbaMainScreen(lessonScreenViewModel, homeViewModel, chaptersViewModel,authViewModel,profileViewModel)
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

private fun trackNotificationIfNeeded(intent: Intent?) {
    if (intent?.getBooleanExtra("track_notification", false) == true) {
        val notificationType = intent.getStringExtra("notification_type") ?: "unknown"
        val notificationTime = intent.getLongExtra("notification_time", 0)

        // Log the notification click event
        logNotificationEvent(
            eventName = "notification_click",
            notificationType = notificationType,
            notificationTime = notificationTime,
            clickTime = System.currentTimeMillis()
        )
    }
}



fun logScreenView(screenName: String) {
    val bundle = Bundle().apply {
        putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
    }
    Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
}

fun logLessonEvent(
    eventName: String,
    lessonId: Int,
    levelId: String,
    chapterId: String? = null,
    segmentType: String? = null,
    xpEarned: Int? = null,
    totalXp: Int? = null,
    timeSpent: Long? = null
) {
    val bundle = Bundle().apply {
        putInt("lesson_id", lessonId)
        putString("level_id", levelId)
        chapterId?.let { putString("chapter_id", it) }
        segmentType?.let { putString("segment_type", it) }
        xpEarned?.let { putInt("xp_earned", it) }
        timeSpent?.let { putLong("time_spent", it) } // 🔥 Now this works
    }
    Firebase.analytics.logEvent(eventName, bundle)
}

@Composable
fun SplashScreenDummy(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(color = Color.White) // Ensure white background
    ) {
        // Center the Lottie animation
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.Center)
        ) {
            LottieAnimationLoading(
                showDialog = remember { mutableStateOf(true) },
                lottieFileRes = R.raw.loading_lottie,
                isTransparentBackground = false // White background for splash screen
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
