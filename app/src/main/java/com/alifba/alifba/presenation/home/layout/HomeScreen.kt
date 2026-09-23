package com.alifba.alifba.presenation.home.layout

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.imageResource
import android.graphics.BitmapShader
import android.graphics.Shader
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.workDataOf
import androidx.work.WorkInfo
import androidx.lifecycle.LiveData
import androidx.compose.runtime.livedata.observeAsState
import android.util.Log
import com.alifba.alifba.R
import com.alifba.alifba.data.db.DatabaseProvider
import java.util.UUID
import com.alifba.alifba.presenation.chapters.layout.LazyChapterColumn
import com.alifba.alifba.ui_components.theme.black
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.theme.lightRed
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.alifba.alifba.utils.DownloadLessonWorker
import com.alifba.alifba.presenation.lessonScreens.domain.repository.LessonCacheRepository
import com.alifba.alifba.presenation.chapters.models.Chapter
import com.alifba.alifba.presenation.home.HomeViewModel
import com.alifba.alifba.presenation.home.layout.settings.NotificationDialog
import com.alifba.alifba.presenation.home.layout.ProfileViewModel
import com.alifba.alifba.presenation.stories.AudioPlayerViewModel
import com.alifba.alifba.ui_components.dialogs.DailyLessonLimitDialog
import com.alifba.alifba.utils.ReminderPreferences
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import com.alifba.alifba.presenation.SubscriptionViewModel
import com.alifba.alifba.presenation.stories.RevenueCatPaywall
import com.alifba.alifba.utils.ReviewPromptManager

// TEMP-DEV: set back to false before shipping — disables the paywall (HomeScreenGate),
// the parent gate, and the discount banner (both in HomeScreenWithNavigation.kt) so Home
// testing doesn't get interrupted during development. Search "DEV_DISABLE_PAYWALL_AND_GATE"
// for every usage; there are three (this file + HomeScreenWithNavigation.kt).
@Composable
fun HomeScreenBackground(modifier: Modifier = Modifier) {
    val grassV1 = ImageBitmap.imageResource(id = R.drawable.grass_tile)
    val grassPaintV1 = remember(grassV1) {
        Paint().apply {
            asFrameworkPaint().shader =
                BitmapShader(grassV1.asAndroidBitmap(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        }
    }
    Canvas(modifier = modifier) {
        drawContext.canvas.nativeCanvas.drawRect(
            0f, 0f, size.width, size.height,
            grassPaintV1.asFrameworkPaint()
        )
    }
}

const val DEV_DISABLE_PAYWALL_AND_GATE = false

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenGate(
    viewModel: HomeViewModel,
    navController: NavController,
    isUserLoggedIn: Boolean,
    profileViewModel: ProfileViewModel,
    chaptersViewModel: com.alifba.alifba.presenation.chapters.ChaptersViewModel,
    onPaywallVisibilityChanged: (Boolean) -> Unit = {}
) {
    // session flag to prevent auto-discount loop - MOVED UP
    var hasShownDiscountThisSession by remember { mutableStateOf(false) }
    
    val subscriptionViewModel: SubscriptionViewModel = hiltViewModel()
    val isPremium by subscriptionViewModel.isPremium.collectAsState()
    val appOpenCount by viewModel.appOpenCount.collectAsState()
    val lastPaywallOpenCount by viewModel.lastPaywallOpenCount.collectAsState()
    val hasShownHomePaywall by viewModel.hasShownHomePaywall.collectAsState()
    var showPaywall by remember { mutableStateOf<Boolean?>(null) }
    var pendingInitialPaywall by remember { mutableStateOf(false) }
    var pendingNthPaywall by remember { mutableStateOf(false) }
    // Set by RevenueCatPaywall's onPurchased — NOT read from isPremium.value in onClose below,
    // since setPremium() writes to DataStore asynchronously and onClose() fires essentially
    // immediately after, before that value has propagated (same reasoning as MainActivity's
    // onboardingPaywall). Without this, a user who just bought could be shown the discount
    // downsell right after paying full price.
    var justPurchased by remember { mutableStateOf(false) }

    // TEMP-DEV: paywall disabled for dev/testing convenience — REMEMBER TO SET false BACK
    // BEFORE SHIPPING. Short-circuits the whole gating LaunchedEffect below so showPaywall
    // always resolves to false (i.e. straight to HomeScreen), without touching the real logic.
    LaunchedEffect(Unit) {
        if (DEV_DISABLE_PAYWALL_AND_GATE) {
            showPaywall = false
        }
    }

    LaunchedEffect(appOpenCount, isPremium, hasShownHomePaywall, lastPaywallOpenCount) {
        if (DEV_DISABLE_PAYWALL_AND_GATE) return@LaunchedEffect
        if (isPremium) {
            showPaywall = false
            pendingInitialPaywall = false
            pendingNthPaywall = false
            return@LaunchedEffect
        }

        val shouldShowInitial = !hasShownHomePaywall
        val shouldShowFifth =
            appOpenCount > 0 && appOpenCount % 5 == 0 && lastPaywallOpenCount != appOpenCount

        if (shouldShowInitial || shouldShowFifth) {
            if (showPaywall != true) {
                showPaywall = true
                pendingInitialPaywall = shouldShowInitial
                pendingNthPaywall = shouldShowFifth
                if (shouldShowInitial) {
                    viewModel.markHomePaywallShown()
                }
                if (shouldShowFifth) {
                    viewModel.markPaywallShownForOpen(appOpenCount)
                }
            }
        } else if (showPaywall != true) {
            showPaywall = false
        }
    }

    LaunchedEffect(showPaywall) {
        onPaywallVisibilityChanged(showPaywall == true)
    }

    when (showPaywall) {
        null -> Box(modifier = Modifier.fillMaxSize())
        true -> RevenueCatPaywall(
            onPurchased = { justPurchased = true },
            onClose = {
                showPaywall = false

                // Add discount paywall logic
                if (!justPurchased) {
                    val skipCount = subscriptionViewModel.standardPaywallSkipCount.value
                    val hasSeenDownsellModal = subscriptionViewModel.hasSeenDownsellModal.value

                    val isEveryThirdSkip = skipCount > 0 && (skipCount + 1) % 3 == 0
                    val isFirstSkipEver = !hasSeenDownsellModal

                    val shouldShowDiscount = pendingInitialPaywall || isFirstSkipEver || isEveryThirdSkip

                    if (shouldShowDiscount && !hasShownDiscountThisSession) {
                        if (isFirstSkipEver) {
                            subscriptionViewModel.setHasSeenDownsellModal(true)
                        }
                        hasShownDiscountThisSession = true
                        subscriptionViewModel.incrementStandardSkipCount()
                        navController.navigate("discountPaywall")
                    } else {
                        subscriptionViewModel.incrementStandardSkipCount()
                    }
                }
                justPurchased = false
                pendingInitialPaywall = false
                pendingNthPaywall = false
            },
            source = "home_entry"
        )
        false -> HomeScreen(
            viewModel = viewModel,
            navController = navController,
            isUserLoggedIn = isUserLoggedIn,
            profileViewModel = profileViewModel,
            chaptersViewModel = chaptersViewModel,
            onPaywallVisibilityChanged = onPaywallVisibilityChanged,
            hasShownDiscountThisSession = hasShownDiscountThisSession,
            onDiscountShown = { hasShownDiscountThisSession = true }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navController: NavController,
    isUserLoggedIn: Boolean,
    profileViewModel: ProfileViewModel,
    chaptersViewModel: com.alifba.alifba.presenation.chapters.ChaptersViewModel,
    onPaywallVisibilityChanged: (Boolean) -> Unit = {},
    hasShownDiscountThisSession: Boolean = false,
    onDiscountShown: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current

    // OLD: window.statusBarColor DisposableEffect hack to tint the status bar green — turned
    // out to be a silent no-op on Android 15+ (targetSdk 35 enforces edge-to-edge, where
    // statusBarColor is ignored). Replaced with true edge-to-edge (WindowCompat.setDecorFits
    // SystemWindows(window, false), set once in MainActivity.onCreate) plus the grass background
    // below extending fillMaxSize() under the status bar itself — no per-screen color needed.
    val audioPlayerViewModel: AudioPlayerViewModel = hiltViewModel()
    val subscriptionViewModel: SubscriptionViewModel = hiltViewModel()
    val isPremium by subscriptionViewModel.isPremium.collectAsState()
    val currentChildProfile by profileViewModel.currentChildProfile.collectAsState()
    val avatarRes = currentChildProfile?.avatar?.let { getAvatarImages(it) } ?: R.drawable.avatar9

    // Read the user's preferred reminder time.
    var reminderTime by remember {
        mutableStateOf(ReminderPreferences.getReminderTime(context))
    }

    // Show the reminder dialog only if notifications haven't been "handled" yet.
    var showReminderDialog by remember {
        mutableStateOf(!ReminderPreferences.isNotificationPermissionHandled(context))
    }

    // If the dialog should show, display it. The user decides to skip or enable.
    if (showReminderDialog) {
        NotificationDialog(
            showDialog = showReminderDialog,
            onDismiss = {
                showReminderDialog = false
            },
            context = context
        )
    }

    // Track loading state
    var isLoading by remember { mutableStateOf(true) }

    // Observe chapters from ViewModel
    val chapters by viewModel.chapters.observeAsState(initial = emptyList())

    // Stop showing the spinner as soon as real chapters arrive, however long that takes —
    // the previous version force-cleared isLoading after a blind 1s timeout even when chapters
    // was still empty, which showed the "no lessons" empty state as a false negative whenever
    // the first-launch Firestore listener (right after onboarding, before the auth token has
    // fully propagated) took longer than 1s to deliver data. That listener now retries itself
    // on failure, so this only needs a generous fallback in case chapters genuinely never
    // arrive, instead of racing the ViewModel's own retry backoff.
    LaunchedEffect(chapters) {
        if (chapters.isNotEmpty()) {
            kotlinx.coroutines.delay(300)
            isLoading = false
        }
    }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(20000)
        isLoading = false
    }

    // Observe badges from ChaptersViewModel (where badge awarding logic exists)
    val earnedBadges by chaptersViewModel.badgeEarnedEvent.collectAsState()
    var didNavigateToBadges by remember { mutableStateOf(false) }

    // Track the currently selected chapter (for the bottom sheet)
    var selectedChapter by remember { mutableStateOf<Chapter?>(null) }
    var showPremiumUnlock by remember { mutableStateOf(false) }
    var showDailyLimitDialog by remember { mutableStateOf(false) }
    // Same race-free purchase signal as HomeScreenGate above — isPremium.value isn't reliable
    // at onClose time since setPremium() writes to DataStore asynchronously.
    var justPurchasedFromChapterGate by remember { mutableStateOf(false) }

    LaunchedEffect(showPremiumUnlock) {
        onPaywallVisibilityChanged(showPremiumUnlock)
    }

    // In-app review: fired only at real value moments (first lesson completed, first 7-day
    // streak, first badge earned), detected in ChaptersViewModel and signaled here for the
    // Activity reference Play's In-App Review API needs. ReviewPromptManager itself owns the
    // per-milestone one-shot gating and the weekly attempt cooldown — this just forwards the
    // event and clears it.
    val reviewPromptMilestone by chaptersViewModel.reviewPromptTrigger.collectAsState()
    LaunchedEffect(reviewPromptMilestone) {
        val milestone = reviewPromptMilestone ?: return@LaunchedEffect
        ReviewPromptManager.notify(context, milestone)
        chaptersViewModel.clearReviewPromptTrigger()
    }

    LaunchedEffect(earnedBadges) {
        if (earnedBadges.isEmpty()) {
            didNavigateToBadges = false
        } else if (!didNavigateToBadges) {
            didNavigateToBadges = true
            navController.navigate("badgeEarned") {
                launchSingleTop = true
            }
        }
    }

    // If a chapter is selected, show a bottom sheet
    if (selectedChapter != null) {
        ModalBottomSheet(
            onDismissRequest = {
                coroutineScope.launch {
                    sheetState.hide()
                    selectedChapter = null
                }
            },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            selectedChapter?.let { chapter ->
                ChapterDownloadBottomSheetContent(
                    chapter = chapter,
                    context = context,
                    levelId = "level1", // Fixed to level1 since we're only showing level1 chapters
                    navController = navController,
                    lessonCacheRepository = viewModel.lessonCacheRepository,
                    profileViewModel = profileViewModel,
                    audioPlayerViewModel = audioPlayerViewModel,
                    onDownloadCompleted = {
                        selectedChapter = null
                        coroutineScope.launch { sheetState.hide() }
                    }
                )
            }
        }
    }

    // Main UI
    Box(modifier = Modifier.fillMaxSize()) {
        HomeScreenBackground(modifier = Modifier.fillMaxSize())

        Column {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Content area - Display chapters directly
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    isLoading -> LoadingAnimation()
                    chapters.isEmpty() -> EmptyChaptersState()
                    else -> LazyChapterColumn(
                        lessons = chapters,
                        modifier = Modifier.fillMaxSize(),
                        onChapterClick = { index, chapter ->
                            // Gate access beyond first three when not premium (index-based)
                            val isBeyondFreeLimit = index > 2
                            if (!isPremium && isBeyondFreeLimit) {
                                showPremiumUnlock = true
                            } else if (chapter.isUnlocked || chapter.isCompleted) {
                                coroutineScope.launch {
                                    // Only "new" lesson/story chapters count toward the daily limit.
                                    val typeKey = chapter.chapterType.lowercase()
                                    val isLimitedChapter =
                                        (typeKey == "lesson" || typeKey == "story") &&
                                                !chapter.isCompleted

                                    val limitResult = if (isLimitedChapter) {
                                        profileViewModel.getDailyLessonStatus()
                                    } else {
                                        null
                                    }

                                    if (isLimitedChapter && limitResult != null && !limitResult.allowed) {
                                        showDailyLimitDialog = true
                                        return@launch
                                    }

                                    selectedChapter = chapter
                                    sheetState.show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    // Show Premium Unlock overlay when gated
    if (showPremiumUnlock) {
        RevenueCatPaywall(
            onPurchased = { justPurchasedFromChapterGate = true },
            onClose = {
                showPremiumUnlock = false
                if (!justPurchasedFromChapterGate) {
                    val skipCount = subscriptionViewModel.standardPaywallSkipCount.value
                    val hasSeenDownsellModal = subscriptionViewModel.hasSeenDownsellModal.value

                    val isEveryThirdSkip = skipCount > 0 && (skipCount + 1) % 3 == 0
                    val isFirstSkipEver = !hasSeenDownsellModal

                    val shouldShowDiscount = isFirstSkipEver || isEveryThirdSkip

                    if (shouldShowDiscount && !hasShownDiscountThisSession) {
                        if (isFirstSkipEver) {
                            subscriptionViewModel.setHasSeenDownsellModal(true)
                        }
                        onDiscountShown()
                        subscriptionViewModel.incrementStandardSkipCount()
                        navController.navigate("discountPaywall")
                    } else {
                        subscriptionViewModel.incrementStandardSkipCount()
                    }
                }
                justPurchasedFromChapterGate = false
            },
            source = "chapter"
        )
    }

    if (showDailyLimitDialog) {
        DailyLessonLimitDialog(
            onDismiss = { showDailyLimitDialog = false },
            avatarRes = avatarRes
        )
    }
    
}

/**
 * Loading animation for chapters
 */
@Composable
fun LoadingAnimation() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(60.dp),
                color = black
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Loading lessons...",
                color = black,
                fontSize = 20.sp,
                fontFamily = FontFamily(
                    Font(R.font.vag_round, FontWeight.Normal),
                    Font(R.font.vag_round_boldd, FontWeight.Bold)
                ),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Empty state display when no chapters are available
 */
@Composable
fun EmptyChaptersState() {
    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(white),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Construction worker or similar image
        Image(
            painter = painterResource(id = R.drawable.qna),
            contentDescription = "Under Construction",
            modifier = Modifier
                .size(350.dp)
                .padding(bottom = 24.dp),
            contentScale = ContentScale.Fit
        )

        // Message text with animation
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + expandVertically()
        ) {
            Text(
                text = "Our lesson builders are hard at work! 🏗️",
                fontFamily = FontFamily(
                    Font(R.font.vag_round, FontWeight.Normal),
                    Font(R.font.vag_round_boldd, FontWeight.Bold)
                ),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = navyBlue,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(initialAlpha = 0f, animationSpec = tween(delayMillis = 300)) +
                    expandVertically(animationSpec = tween(delayMillis = 300))
        ) {
            Text(
                text = "More lessons are coming soon!",
                fontFamily = alifbaFont,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                letterSpacing = 0.8.sp,
                color = lightRed,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Bottom sheet that does the actual "Download and Start"
 */
@Composable
fun ChapterDownloadBottomSheetContent(
    chapter: Chapter,
    context: android.content.Context,
    levelId: String,
    navController: NavController,
    profileViewModel: ProfileViewModel,
    lessonCacheRepository: LessonCacheRepository,
    audioPlayerViewModel: AudioPlayerViewModel,
    onDownloadCompleted: () -> Unit
) {
    val currentChildProfile by profileViewModel.currentChildProfile.collectAsState()
    val parentAccount by profileViewModel.parentAccountState.collectAsState()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600

    val avatarRes = currentChildProfile?.avatar?.let { getAvatarImages(it) } ?: R.drawable.avatar9
    LaunchedEffect(currentChildProfile) {
        Log.d("BottomSheetContent", "Updated currentChildProfile: $currentChildProfile")
        Log.d("BottomSheetContent", "Avatar resolved to: $avatarRes")
    }

    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )
    val coroutineScope = rememberCoroutineScope()

    // Track the UI state (Initial, Downloading, Cached, Downloaded, Error)
    var downloadState by remember { mutableStateOf<DownloadState>(DownloadState.Initial) }

    // Daily lesson limit dialog state
    var showDailyLimitDialog by remember { mutableStateOf(false) }

    // Keep track of the workerId so we can observe its progress
    var workerId by remember { mutableStateOf<UUID?>(null) }
    val workManager = WorkManager.getInstance(context)

    // Observe worker progress if we have an ID
    val workInfo by if (workerId != null) {
        workManager.getWorkInfoByIdLiveData(workerId!!).observeAsState()
    } else {
        remember { mutableStateOf(null) }
    }

    // Each time workInfo changes, update downloadState
    LaunchedEffect(workInfo) {
        workInfo?.let { info ->
            when (info.state) {
                WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED, WorkInfo.State.RUNNING -> {
                    downloadState = DownloadState.Downloading
                }
                WorkInfo.State.SUCCEEDED -> {
                    downloadState = DownloadState.Downloaded
                }
                WorkInfo.State.FAILED -> {
                    downloadState = DownloadState.Error
                }
                else -> { /* CANCELLED or other states */ }
            }
        }
    }

    LaunchedEffect(Unit) {
        val uiDb = DatabaseProvider.getInstance(context)
        Log.d("HomeScreen", "UI DB instance hash: ${uiDb.hashCode()}")
    }

    // Check if already cached
    LaunchedEffect(chapter.id, levelId) {
        kotlinx.coroutines.delay(1000)
        val cachedLesson = lessonCacheRepository.getLessonCache(chapter.id, levelId)
        Log.d("HomeScreen", "After delay, cachedLesson: $cachedLesson")
        if (cachedLesson != null) {
            downloadState = DownloadState.Cached
        }
    }

    suspend fun startLessonIfAllowed() {
        // Only "new" lesson/story chapters count toward the daily limit.
        val typeKey = chapter.chapterType.lowercase()
        val isLimitedChapter = (typeKey == "lesson" || typeKey == "story") &&
                !chapter.isCompleted

        val limitResult = if (isLimitedChapter) {
            profileViewModel.getDailyLessonStatus()
        } else {
            null
        }

        if (isLimitedChapter && limitResult != null && !limitResult.allowed) {
            showDailyLimitDialog = true
            return
        }

        // Stop any playing audio before navigating to lessons
        audioPlayerViewModel.stopAndClearAudio()
        navController.navigate("lessonScreen/${chapter.id}/$levelId")
        onDownloadCompleted()
    }

    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = chapter.title,
            fontFamily = alifbaFont,
            fontWeight = FontWeight.Bold,
            color = navyBlue,
            fontSize = if (isTablet) 30.sp else 23.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = if (isTablet) 20.dp else 16.dp)
        )

        Image(
            painter = painterResource(id = avatarRes),
            contentDescription = "User Avatar",
            modifier = Modifier.size(if (isTablet) 200.dp else 150.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(if (isTablet) 32.dp else 24.dp))

        when (downloadState) {
            DownloadState.Initial -> {
                CommonButton(
                    buttonText = "Download lesson",
                    mainColor = lightNavyBlue,
                    shadowColor = navyBlue,
                    textColor = white,
                    onClick = {
                        val request = OneTimeWorkRequestBuilder<DownloadLessonWorker>()
                            .setInputData(
                                workDataOf(
                                    "chapter_id" to chapter.id,
                                    "level_id" to levelId
                                )
                            )
                            .build()

                        workManager.enqueue(request)
                        workerId = request.id
                        downloadState = DownloadState.Downloading
                    }
                )
            }

            DownloadState.Downloading -> {
                CircularProgressIndicator()
            }

            DownloadState.Cached -> {
                CommonButton(
                    buttonText = "Start lesson",
                    mainColor = lightNavyBlue,
                    textColor = white,
                    shadowColor = navyBlue,
                    onClick = {
                        coroutineScope.launch {
                            startLessonIfAllowed()
                        }
                    }
                )
            }

            DownloadState.Downloaded -> {
                LaunchedEffect(Unit) {
                    startLessonIfAllowed()
                }
            }

            DownloadState.Error -> {
                Text(
                    text = "Download Failed", 
                    color = Color.Red,
                    fontSize = if (isTablet) 20.sp else 16.sp,
                    fontFamily = alifbaFont,
                    fontWeight = FontWeight.Bold
                )
                CommonButton(
                    buttonText = "Retry",
                    mainColor = lightNavyBlue,
                    textColor = white,
                    shadowColor = navyBlue,
                    onClick = {
                        downloadState = DownloadState.Initial
                        workerId = null
                    }
                )
            }
        }

        if (showDailyLimitDialog) {
            DailyLessonLimitDialog(
                onDismiss = { showDailyLimitDialog = false },
                avatarRes = avatarRes
            )
        }
    }
}

fun getAvatarImages(avatarName: String): Int {
    return when (avatarName) {
        "Deenasaur" -> R.drawable.deenasaur
        "Duallama" -> R.drawable.duallama
        "Firdawsaur" -> R.drawable.firdawsaur
        "Ihsaninguin" -> R.drawable.ihsaninguin
        "Imamoth" -> R.drawable.imamoth
        "Khilafox" -> R.drawable.khilafox
        "Shukraf" -> R.drawable.shukraf
        "Jannahbee" -> R.drawable.jannahbee
        "Qadragon" -> R.drawable.qadragon
        "Sabracorn" -> R.drawable.sabracorn
        "Sadiqling" -> R.drawable.sadiqling
        "Sidqhog" -> R.drawable.sidqhog
        else -> R.drawable.avatar9
    }
}

sealed class DownloadState {
    data object Initial : DownloadState()
    data object Downloading : DownloadState()
    data object Cached : DownloadState()
    object Downloaded : DownloadState()
    object Error : DownloadState()
}

// Keep this function for backward compatibility if needed elsewhere
@Composable
fun HomeScreenLegacy(
    viewModel: HomeViewModel,
    navController: NavController,
    isUserLoggedIn: Boolean
) {
    // This is the old implementation with level selection
    // Can be removed once all references are updated
}
