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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
import com.alifba.alifba.ui_components.dialogs.BadgeEarnedSnackBar
import com.alifba.alifba.utils.ReminderPreferences
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navController: NavController,
    isUserLoggedIn: Boolean,
    profileViewModel: ProfileViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current
    val audioPlayerViewModel: AudioPlayerViewModel = hiltViewModel()

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

    // When chapters update, set loading to false
    LaunchedEffect(chapters) {
        if (chapters.isNotEmpty() || chapters.isEmpty() && !isLoading) {
            kotlinx.coroutines.delay(300)
            isLoading = false
        } else if (chapters.isEmpty() && isLoading) {
            kotlinx.coroutines.delay(1000)
            isLoading = false
        }
    }

    // Observe badges
    val earnedBadge by viewModel.badgeEarnedEvent.collectAsState()

    // Track the currently selected chapter (for the bottom sheet)
    var selectedChapter by remember { mutableStateOf<Chapter?>(null) }

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
                        onChapterClick = { chapter ->
                            if (chapter.isUnlocked || chapter.isCompleted) {
                                selectedChapter = chapter
                                coroutineScope.launch {
                                    sheetState.show()
                                }
                            }
                        }
                    )
                }
            }
        }

        // Show Badge Earned Snackbar if we have a new badge
        earnedBadge?.let { badge ->
            Box(
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.TopCenter)
                    .zIndex(2f)
            ) {
                BadgeEarnedSnackBar(
                    badge = badge,
                    onDismiss = { viewModel.clearBadgeEvent() }
                )
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadChapters("level1")
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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
    val userProfile by profileViewModel.userProfileState.collectAsState()

    val avatarRes = userProfile?.avatar?.let { getAvatarImages(it) } ?: R.drawable.avatar9
    LaunchedEffect(userProfile) {
        Log.d("BottomSheetContent", "Updated userProfile: $userProfile")
        Log.d("BottomSheetContent", "Avatar resolved to: $avatarRes")
    }

    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )
    val coroutineScope = rememberCoroutineScope()

    // Track the UI state (Initial, Downloading, Cached, Downloaded, Error)
    var downloadState by remember { mutableStateOf<DownloadState>(DownloadState.Initial) }

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
            fontSize = 23.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
        )

        Image(
            painter = painterResource(id = avatarRes),
            contentDescription = "User Avatar",
            modifier = Modifier.size(150.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(24.dp))

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
                        // Stop any playing audio before navigating to lessons
                        audioPlayerViewModel.stopAndClearAudio()
                        navController.navigate("lessonScreen/${chapter.id}/$levelId")
                        onDownloadCompleted()
                    }
                )
            }

            DownloadState.Downloaded -> {
                LaunchedEffect(Unit) {
                    // Stop any playing audio before navigating to lessons
                    audioPlayerViewModel.stopAndClearAudio()
                    navController.navigate("lessonScreen/${chapter.id}/$levelId")
                    onDownloadCompleted()
                }
            }

            DownloadState.Error -> {
                Text("Download Failed", color = Color.Red)
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