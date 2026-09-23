package com.alifba.alifba.presenation.main

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
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
import androidx.core.view.WindowCompat
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.alifba.alifba.R
import com.alifba.alifba.features.authentication.DataStoreManager
import com.alifba.alifba.features.authentication.OnboardingDataStoreManager
import com.alifba.alifba.presenation.lessonScreens.LessonScreenViewModel
import com.alifba.alifba.presenation.login.AuthViewModel
import com.alifba.alifba.presenation.login.LoginEntryMode
import com.alifba.alifba.presenation.login.LoginScreen
import com.alifba.alifba.presenation.login.ProfileRegistration
import com.alifba.alifba.presenation.chapters.ChaptersViewModel
import com.alifba.alifba.presenation.home.HomeViewModel
import com.alifba.alifba.presenation.main.layout.AlifbaMainScreen
import com.alifba.alifba.presenation.home.layout.HomeScreen
import com.alifba.alifba.presenation.home.layout.ProfileViewModel
import com.alifba.alifba.presenation.login.ForgotPasswordScreen
import com.alifba.alifba.presenation.onboarding.OnboardingScreen
import com.alifba.alifba.presenation.profile.ProfileSelectionScreen
import com.alifba.alifba.presenation.profile.AddProfileScreen
import com.alifba.alifba.presenation.splash.IllustratedSplashScreen
import com.alifba.alifba.presenation.stories.RevenueCatPaywall
import com.alifba.alifba.presenation.stories.DiscountPaywall
import com.alifba.alifba.presenation.stories.AnnualDiscount35Paywall
import com.alifba.alifba.presenation.stories.DiscountPaywallEntryTransition
import com.alifba.alifba.presenation.SubscriptionViewModel
import com.alifba.alifba.ui_components.dialogs.LottieAnimationLoading
import com.alifba.alifba.ui_components.theme.AlifbaTheme
import com.google.firebase.auth.FirebaseAuth
import com.posthog.PostHog
import com.alifba.alifba.BuildConfig

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import javax.inject.Inject
import io.sentry.Sentry

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var dataStoreManager: DataStoreManager

    private val lessonScreenViewModel: LessonScreenViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private val chaptersViewModel: ChaptersViewModel by viewModels()
    private val profileViewModel:ProfileViewModel by viewModels()
    val Context.dataStore by preferencesDataStore(name = "settings")
    private var isInForeground = false
    private var pendingAppOpenSource: String? = null
    private var pendingNotificationType: String? = null
    private var pendingNotificationTime: Long? = null
    private var forceLoginOnLaunch = false
    private var navigateToRoute by mutableStateOf<String?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Track notifications when app is already running
        trackNotificationIfNeeded(intent)
        intent.getStringExtra("navigate_to")?.let {
            navigateToRoute = it
            intent.removeExtra("navigate_to")
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // True edge-to-edge: on Android 15+ (targetSdk 35) this is already enforced by the
        // platform and window.statusBarColor is a no-op, but declaring it explicitly is the
        // correct modern replacement and keeps behavior consistent on older OS versions too.
        // Lets screens (e.g. Home's grass background) draw their own content under the status
        // bar instead of leaving a plain white gap there.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        intent.getStringExtra("navigate_to")?.let {
            navigateToRoute = it
            intent.removeExtra("navigate_to")
        }
        var startDestination by mutableStateOf<String?>(null)

        installSplashScreen().setKeepOnScreenCondition {
            startDestination == null
        }

        // Lock to portrait by default - will be changed programmatically when needed
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val onboardingDataStore = OnboardingDataStoreManager(applicationContext)
        trackNotificationIfNeeded(intent)
        val isFreshInstall = enforceFreshInstallLogout()
        lifecycleScope.launch {
            val destination = withTimeoutOrNull(7000L) { // 7-second timeout for safety
                try {
                    if (isFreshInstall) {
                        onboardingDataStore.setOnboardingCompleted(false)
                        dataStoreManager.resetAppOpenTracking()
                    }
                    val hasCompletedOnboarding = onboardingDataStore.hasCompletedOnboarding.first()
                    val firebaseUser = FirebaseAuth.getInstance().currentUser

                    when {
                        forceLoginOnLaunch || firebaseUser == null -> "login"
                        !hasCompletedOnboarding -> "onboarding"
                        else -> {
                            val hasProfiles = authViewModel.checkForChildProfiles()

                            when {
                                !hasProfiles -> "createProfile"
                                else -> "profileSelection"
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Error determining start destination: ${e.message}")
                    "login" // Default to login on any error
                }
            }
            startDestination = destination ?: "login" // If timeout occurs, default to login
        }

        setContent {
            val navController = rememberNavController()
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                val userId = authViewModel.dataStoreManager.userId.first()
                if (userId != null) {
                    authViewModel.updateTimeZoneIfNeeded(context, userId)
                    // Keep RevenueCat user in sync with Alifba account on app start
                    authViewModel.syncRevenueCatForCurrentUser()
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (startDestination != null) {
                    // Guarded on startDestination (i.e. scoped to once NavHost below is
                    // actually being composed with its graph) — a navigate_to extra can
                    // already be set on the very first composition (e.g. a cold launch from
                    // tapping a notification), while startDestination is still resolving
                    // asynchronously above. Calling navController.navigate() before NavHost
                    // has attached a graph throws IllegalArgumentException("Navigation graph
                    // has not been set"), which crashed exactly this deep-link path.
                    LaunchedEffect(navController, navigateToRoute, startDestination) {
                        navigateToRoute?.let { route ->
                            navController.navigate(route)
                            navigateToRoute = null
                        }
                    }

                    NavHost(
                        navController = navController,
                        // Always the illustrated splash first, regardless of what the async
                        // block above resolved — it shows for a fixed duration then navigates
                        // on to the real (already-resolved) destination itself. The native
                        // installSplashScreen()/setKeepOnScreenCondition above already waits on
                        // startDestination being non-null before this NavHost even composes, so
                        // that resolution is still what gates the illustrated splash's own
                        // handoff target; this doesn't change or duplicate that wait.
                        startDestination = "illustratedSplash",
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable("illustratedSplash") {
                            IllustratedSplashScreen(
                                nextRoute = startDestination!!,
                                onFinished = { nextRoute ->
                                    navController.navigate(nextRoute) {
                                        popUpTo("illustratedSplash") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("login") {
                            LoginScreen(viewModel = authViewModel, navController = navController)
                        }
                        composable(
                            route = "authOptions/{mode}",
                            arguments = listOf(
                                navArgument("mode") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val mode = backStackEntry.arguments?.getString("mode")
                            val entryMode = if (mode == "signup") {
                                LoginEntryMode.SignUp
                            } else {
                                LoginEntryMode.SignIn
                            }
                            LoginScreen(
                                viewModel = authViewModel,
                                navController = navController,
                                entryMode = entryMode
                            )
                        }
                        composable("onboarding") {  // ✅ Ensure this exists
                            OnboardingScreen(
                                onComplete = { childName ->
                                    lifecycleScope.launch {
                                        onboardingDataStore.setOnboardingCompleted(true)
                                        onboardingDataStore.setOnboardingChildName(childName)
                                    }
                                    // Mandatory parent gate + paywall sits between onboarding and
                                    // signup/profile routing — COPPA-driven, so no purchase UI is
                                    // ever shown without the gate clearing first. The actual
                                    // signup-vs-profile branch below is unchanged, just deferred
                                    // until the paywall closes (purchased, restored, or skipped).
                                    navController.navigate("onboardingPaywall") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                },
                                onExit = {
                                    navController.navigate("authOptions/signin") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("onboardingPaywall") {
                            val subscriptionViewModel: SubscriptionViewModel = hiltViewModel()
                            var showDiscountPaywall by remember { mutableStateOf(false) }
                            // Set by RevenueCatPaywall's onPurchased — NOT read from
                            // subscriptionViewModel.isPremium.value in onClose below, since
                            // setPremium() writes to DataStore asynchronously and onClose() fires
                            // essentially immediately after, before that value has propagated.
                            var justPurchased by remember { mutableStateOf(false) }

                            val proceedPastPaywall = {
                                // Counts as the "home entry" paywall too, so HomeScreenGate's own
                                // !hasShownHomePaywall check doesn't fire a second paywall right
                                // after this one closes.
                                homeViewModel.markHomePaywallShown()
                                lifecycleScope.launch {
                                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                                    if (firebaseUser == null) {
                                        navController.navigate("authOptions/signup") {
                                            popUpTo("onboardingPaywall") { inclusive = true }
                                        }
                                    } else {
                                        val hasProfiles = authViewModel.checkForChildProfiles()
                                        val nextRoute = if (hasProfiles) "profileSelection" else "createProfile"
                                        navController.navigate(nextRoute) {
                                            popUpTo("onboardingPaywall") { inclusive = true }
                                        }
                                    }
                                }
                            }

                            if (showDiscountPaywall) {
                                DiscountPaywall(
                                    onClose = { proceedPastPaywall() },
                                    onSuccess = { proceedPastPaywall() }
                                )
                            } else {
                                RevenueCatPaywall(
                                    source = "onboarding",
                                    requireParentGate = true,
                                    onPurchased = { justPurchased = true },
                                    onClose = {
                                        // Skipped without buying: offer the same one-time discount
                                        // downsell HomeScreenGate shows on a standard-paywall skip,
                                        // instead of just moving on with nothing. Mirrors that same
                                        // shared skip-count/downsell-seen state (SubscriptionViewModel),
                                        // so it stays consistent regardless of which paywall this is.
                                        if (justPurchased) {
                                            proceedPastPaywall()
                                        } else {
                                            val isFirstSkipEver = !subscriptionViewModel.hasSeenDownsellModal.value
                                            val skipCount = subscriptionViewModel.standardPaywallSkipCount.value
                                            val isEveryThirdSkip = skipCount > 0 && (skipCount + 1) % 3 == 0
                                            if (isFirstSkipEver) {
                                                subscriptionViewModel.setHasSeenDownsellModal(true)
                                            }
                                            subscriptionViewModel.incrementStandardSkipCount()
                                            if (isFirstSkipEver || isEveryThirdSkip) {
                                                showDiscountPaywall = true
                                            } else {
                                                proceedPastPaywall()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                        composable("createProfile") {
                            ProfileRegistration(navController)
                        }

                        composable("profileSelection") {
                            ProfileSelectionScreen(navController, authViewModel)
                        }

                        composable("addProfile") {
                            AddProfileScreen(navController, authViewModel)
                        }

                        composable("forgotPassword") {
                            ForgotPasswordScreen(navController = navController)
                        }

                        composable("homeScreen") {
                            // Only reach this after successful login
                            authViewModel.fetchUserProfile() // Ensure profile is loaded
                            AlifbaMainScreen(lessonScreenViewModel, homeViewModel, chaptersViewModel, authViewModel, profileViewModel)
                        }

                        // Also reachable from HomeScreen.kt's standard-paywall-skip downsell
                        // (navController.navigate("discountPaywall"), no query param — resolves
                        // to viaNotification's false default) as well as a signup-discount
                        // notification's deep link (navigate_to, set from
                        // DiscountNotificationWorker, with ?viaNotification=true). Popping back
                        // lands on whatever the app's actual startDestination already resolved
                        // to underneath.
                        composable(
                            route = "discountPaywall?viaNotification={viaNotification}",
                            arguments = listOf(
                                navArgument("viaNotification") {
                                    type = NavType.BoolType
                                    defaultValue = false
                                }
                            )
                        ) { backStackEntry ->
                            var showingTransition by remember {
                                mutableStateOf(backStackEntry.arguments?.getBoolean("viaNotification") ?: false)
                            }
                            if (showingTransition) {
                                DiscountPaywallEntryTransition(
                                    mascotName = "moon_thinking",
                                    onFinished = { showingTransition = false }
                                )
                            } else {
                                DiscountPaywall(
                                    onClose = { navController.popBackStack() },
                                    onSuccess = { navController.popBackStack() }
                                )
                            }
                        }
                        composable(
                            route = "annualDiscount35Paywall?viaNotification={viaNotification}",
                            arguments = listOf(
                                navArgument("viaNotification") {
                                    type = NavType.BoolType
                                    defaultValue = false
                                }
                            )
                        ) { backStackEntry ->
                            var showingTransition by remember {
                                mutableStateOf(backStackEntry.arguments?.getBoolean("viaNotification") ?: false)
                            }
                            if (showingTransition) {
                                DiscountPaywallEntryTransition(
                                    mascotName = "moon_excited_jump",
                                    onFinished = { showingTransition = false }
                                )
                            } else {
                                AnnualDiscount35Paywall(
                                    onClose = { navController.popBackStack() },
                                    onSuccess = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun enforceFreshInstallLogout(): Boolean {
        val marker = File(noBackupFilesDir, "install_marker")
        if (!marker.exists()) {
            forceLoginOnLaunch = true
            authViewModel.logout()
            try {
                marker.parentFile?.mkdirs()
                marker.createNewFile()
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Failed to create install marker: ${e.message}", e)
            }
            return true
        }
        return false
    }

    override fun onStart() {
        super.onStart()
        isInForeground = true
        val source = pendingAppOpenSource ?: "direct"
        lifecycleScope.launch {
            dataStoreManager.incrementAppOpenCount()
            authViewModel.identifyCurrentUserForAppOpen()
            logAppOpened(
                source = source,
                notificationType = pendingNotificationType,
                notificationTime = pendingNotificationTime
            )
            pendingAppOpenSource = null
            pendingNotificationType = null
            pendingNotificationTime = null
        }
    }

    override fun onResume() {
        super.onResume()
        authViewModel.updateSubscriptionStatusForCurrentUser()
        authViewModel.updateLastActivityForCurrentUser()
    }

    override fun onStop() {
        super.onStop()
        isInForeground = false
    }

    // Function to change orientation programmatically
    fun setOrientation(orientation: Int) {
        requestedOrientation = orientation
    }

    private fun trackNotificationIfNeeded(intent: Intent?) {
        if (intent?.getBooleanExtra("track_notification", false) == true) {
            pendingAppOpenSource = "notification"
            pendingNotificationType = intent.getStringExtra("notification_type") ?: "unknown"
            pendingNotificationTime = intent.getLongExtra("notification_time", 0)
                .takeIf { it > 0L }
            logReminderClicked(
                notificationType = pendingNotificationType ?: "unknown",
                notificationTime = pendingNotificationTime
            )

            if (isInForeground) {
                logAppOpened(
                    source = "notification",
                    notificationType = pendingNotificationType,
                    notificationTime = pendingNotificationTime
                )
                pendingAppOpenSource = null
                pendingNotificationType = null
                pendingNotificationTime = null
            }
            intent.removeExtra("track_notification")
            intent.removeExtra("notification_type")
            intent.removeExtra("notification_time")
        }
    }
}

@Suppress("UNUSED_PARAMETER")
fun logScreenView(screenName: String) {
}

fun logAppOpened(
    source: String,
    notificationType: String? = null,
    notificationTime: Long? = null
) {
    try {
        val properties = mutableMapOf<String, Any>(
            "source" to source
        )
        notificationType?.let { properties["notification_type"] = it }
        notificationTime?.let { properties["notification_time"] = it }
        if (BuildConfig.DEBUG) {
            android.util.Log.d("PostHog", "capture app_opened $properties")
        }
        PostHog.capture(event = "app_opened", properties = properties)
        if (BuildConfig.DEBUG) {
            PostHog.flush()
        }
    } catch (e: Exception) {
        android.util.Log.e("PostHog", "Failed to capture app opened: ${e.message}", e)
    }
}

fun logReminderClicked(
    notificationType: String,
    notificationTime: Long? = null
) {
    try {
        val properties = mutableMapOf<String, Any>(
            "notification_type" to notificationType
        )
        notificationTime?.let { properties["notification_time"] = it }
        properties["click_time"] = System.currentTimeMillis()
        if (BuildConfig.DEBUG) {
            android.util.Log.d("PostHog", "capture reminder_clicked $properties")
        }
        PostHog.capture(event = "reminder_clicked", properties = properties)
        if (BuildConfig.DEBUG) {
            PostHog.flush()
        }
    } catch (e: Exception) {
        android.util.Log.e("PostHog", "Failed to capture reminder clicked: ${e.message}", e)
    }
}

fun logLessonEvent(
    eventName: String,
    lessonId: Int,
    levelId: String,
    chapterId: String? = null,
    segmentType: String? = null,
    xpEarned: Int? = null,
    totalXp: Int? = null,
    timeSpent: Long? = null,
    segmentIndex: Int? = null,
    totalSegments: Int? = null,
    entrySource: String? = null,
    lastSegmentType: String? = null,
    lastSegmentIndex: Int? = null
) {
    val allowedEvents = setOf("lesson_started", "lesson_completed", "lesson_abandoned")
    if (!allowedEvents.contains(eventName)) {
        return
    }
    try {
        val properties = mutableMapOf<String, Any>()
        when (eventName) {
            "lesson_started" -> {
                properties["lesson_id"] = lessonId
                properties["level_id"] = levelId
                entrySource?.let { properties["entry_source"] = it }
            }
            "lesson_abandoned" -> {
                properties["lesson_id"] = lessonId
                properties["level_id"] = levelId
                lastSegmentType?.let { properties["last_segment_type"] = it }
                lastSegmentIndex?.let { properties["last_segment_index"] = it }
                totalSegments?.let { properties["total_segments"] = it }
                timeSpent?.let { properties["time_spent_ms"] = it }
            }
            "lesson_completed" -> {
                properties["lesson_id"] = lessonId
                properties["level_id"] = levelId
                chapterId?.let { properties["chapter_id"] = it }
                totalXp?.let { properties["total_xp"] = it }
                timeSpent?.let { properties["time_spent_ms"] = it }
            }
        }
        if (BuildConfig.DEBUG) {
            android.util.Log.d("PostHog", "capture $eventName $properties")
        }
        PostHog.capture(event = eventName, properties = properties)
        if (BuildConfig.DEBUG) {
            PostHog.flush()
        }
    } catch (e: Exception) {
        android.util.Log.e("PostHog", "Failed to capture lesson event: ${e.message}", e)
    }
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
                isTransparentBackground = false, // White background for splash screen
                lottieName = "moon_thinking"
            )
        }
    }
}

//
//
//    @Preview(showBackground = true)
//    @Composable
//    fun AlifbaLessonPreview() {
//        AlifbaTheme {
//            val dummyNavController = rememberNavController()
//            HomeScreen(HomeViewModel(), dummyNavController)
//        }
//    }
