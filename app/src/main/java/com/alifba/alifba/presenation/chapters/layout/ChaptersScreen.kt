package com.alifba.alifba.presenation.chapters

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.work.*
import com.alifba.alifba.R
import com.alifba.alifba.data.db.DatabaseProvider
import com.alifba.alifba.presenation.chapters.layout.LazyChapterColumn
import com.alifba.alifba.presenation.chapters.models.Chapter
import com.alifba.alifba.presenation.home.HomeViewModel
import com.alifba.alifba.presenation.home.layout.ProfileViewModel
import com.alifba.alifba.presenation.home.layout.TopBarIcons
import com.alifba.alifba.presenation.lessonScreens.domain.repository.LessonCacheRepository
import com.alifba.alifba.ui_components.dialogs.BadgeEarnedSnackBar
import com.alifba.alifba.ui_components.theme.black
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.lightRed
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.alifba.alifba.utils.DownloadLessonWorker
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersScreen(
    navController: NavController,
    levelId: String,
    chaptersViewModel: ChaptersViewModel,
    homeViewModel: HomeViewModel,
    profileViewModel: ProfileViewModel
) {
    LaunchedEffect(Unit) {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "ChapterScreen")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "ChapterScreen")
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current

    val levelItem = homeViewModel.levelItemList.find { it.levelId == levelId }
    val levelImage = levelItem?.image ?: R.drawable.levelone

    val userProfile by profileViewModel.userProfileState.collectAsState()

    // Track loading state
    var isLoading by remember { mutableStateOf(true) }

    // 1) Load chapters on first display
    LaunchedEffect(levelId) {
        Log.d("ChaptersScreen", "Fetching chapters for level: $levelId")
        isLoading = true
        chaptersViewModel.loadChapters(levelId)
    }

    // 2) Observe chapters from ViewModel
    val chapters by chaptersViewModel.chapters.observeAsState(initial = emptyList())

    // When chapters update, set loading to false
    LaunchedEffect(chapters) {
        if (chapters.isNotEmpty() || chapters.isEmpty() && !isLoading) {
            // Small delay to ensure the UI has time to update
            kotlinx.coroutines.delay(300)
            isLoading = false
        } else if (chapters.isEmpty() && isLoading) {
            // Add a minimum loading time to prevent flickering
            kotlinx.coroutines.delay(1000)
            isLoading = false
        }
    }

    // 3) Observe badges
    val earnedBadge by chaptersViewModel.badgeEarnedEvent.collectAsState()

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
            // Now the bottom sheet will span the entire width.
            selectedChapter?.let { chapter ->
                Log.d("ChaptersScreen", "selectedChapter: ${chapter.title}")
                Log.d("ChaptersScreen", "userProfile?.avatar = ${userProfile?.avatar}")
                Log.d("ChaptersScreens", "profileViewModel instance = $profileViewModel, userProfileState = ${profileViewModel.userProfileState.value}")

                val avatarRes = if (userProfile != null) {
                    val loadedAvatar = getAvatarImages(userProfile!!.avatar)
                    // (B) Debug what we resolved for the resource:
                    Log.d("ChaptersScreen", "Loaded Avatar Resource = $loadedAvatar")
                    loadedAvatar
                } else {
                    // (C) If userProfile is null
                    Log.d("ChaptersScreen", "userProfile is null => fallback to avatar9")
                    R.drawable.avatar9
                }

                ChapterDownloadBottomSheetContent(
                    chapter = chapter,
                    context = context,
                    levelId = levelId,
                    navController = navController,
                    lessonCacheRepository = chaptersViewModel.lessonCacheRepository,
                    profileViewModel = profileViewModel,
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
        // Background based on state
        if (!isLoading && chapters.isEmpty()) {
            // Empty state background (light color or different pattern)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F9FA)) // Light background for empty state
            )
        } else {
            // Regular background for loading or chapters view
            Image(
                painter = painterResource(id = R.drawable.chapterscreen_bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Column {
            // Top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .background(Color.Transparent)
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TopBarIcons(
                        painter = painterResource(id = R.drawable.goback),
                        contentDescription = "Go Back",
                        onClick = {
                            navController.popBackStack()
                        },
                        shadowColor = Color.Gray,
                        mainColor = Color.White
                    )

                    TopBarIcons(
                        painter = painterResource(id = R.drawable.clipboard),
                        contentDescription = "Level Info",
                        onClick = {
                            navController.navigate("levelInfo/$levelId/$levelImage")
                        },
                        shadowColor = Color.Gray,
                        mainColor = Color.White
                    )
                }
            }

            // Content area
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
                    .align(Alignment.TopCenter)
                    .zIndex(2f)
            ) {
                BadgeEarnedSnackBar(
                    badge = badge,
                    onDismiss = { chaptersViewModel.clearBadgeEvent() }
                )
            }
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
            painter = painterResource(id = R.drawable.qna),  // Add this image to your project
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
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                letterSpacing = 0.8.sp,
                color = lightRed,
                textAlign = TextAlign.Center
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


/**
 * Bottom sheet that does the actual “Download and Start”
 */
@Composable
fun ChapterDownloadBottomSheetContent(
    chapter: Chapter,
    context: Context,
    levelId: String,
    navController: NavController,
    profileViewModel: ProfileViewModel,
    lessonCacheRepository: LessonCacheRepository, // for checking local DB
    onDownloadCompleted: () -> Unit
) {
    val userProfile by profileViewModel.userProfileState.collectAsState()  // ✅ Live observe changes!

    val avatarRes = userProfile?.avatar?.let { getAvatarImages(it) } ?: R.drawable.avatar9  // ✅ Dynamic update
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

   // val userProfile by profileViewModel.userProfileState.collectAsState()

   // val avatarRes = userProfile?.avatar?.let { getAvatarImages(avatarName = it) } ?: R.drawable.avatar9

    // Keep track of the workerId so we can observe its progress
    var workerId by remember { mutableStateOf<UUID?>(null) }
    val workManager = WorkManager.getInstance(context)

    // Observe worker progress if we have an ID
    val workInfo by if (workerId != null) {
        workManager.getWorkInfoByIdLiveData(workerId!!).observeAsState()
    } else {
        remember { mutableStateOf(null) }
    }

    // Each time workInfo changes, update `downloadState`
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

                else -> { /* CANCELLED or other states */
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        val uiDb = DatabaseProvider.getInstance(context)
        Log.d("ChaptersScreen", "UI DB instance hash: ${uiDb.hashCode()}")
    }
    // Check if already cached
    LaunchedEffect(chapter.id, levelId) {
        kotlinx.coroutines.delay(1000)  // wait 1 second
        val cachedLesson = lessonCacheRepository.getLessonCache(chapter.id, levelId)
        Log.d("ChapterScreen", "After delay, cachedLesson: $cachedLesson")
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
                        // Enqueue the Worker
                        val request = OneTimeWorkRequestBuilder<DownloadLessonWorker>()
                            .setInputData(
                                workDataOf(
                                    "chapter_id" to chapter.id,  // Ensure this is the correct ID
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
                // Already in DB, let user open
                CommonButton(
                    buttonText = "Start lesson",
                    mainColor = lightNavyBlue,
                    textColor = white,
                    shadowColor = navyBlue,
                    onClick = {
                        navController.navigate("lessonScreen/${chapter.id}/$levelId")
                        onDownloadCompleted()
                    }
                )
            }

            DownloadState.Downloaded -> {
                // Worker finished => open the lesson
                LaunchedEffect(Unit) {
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

            else -> {}
        }
    }
}

sealed class DownloadState {
    data object Initial : DownloadState()
    data object Downloading : DownloadState()
    data object Cached : DownloadState()
    object Downloaded : DownloadState()
    object Error : DownloadState()
}
