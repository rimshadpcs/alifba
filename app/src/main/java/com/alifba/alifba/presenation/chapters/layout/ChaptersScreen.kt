package com.alifba.alifba.presenation.chapters

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.work.*
import com.alifba.alifba.R
import com.alifba.alifba.data.db.DatabaseProvider
import com.alifba.alifba.presenation.chapters.layout.LazyChapterColumn
import com.alifba.alifba.presenation.chapters.models.Chapter
import com.alifba.alifba.presenation.home.HomeViewModel
import com.alifba.alifba.presenation.home.layout.TopBarIcons
import com.alifba.alifba.presenation.lessonScreens.domain.repository.LessonCacheRepository
import com.alifba.alifba.ui_components.dialogs.BadgeEarnedSnackBar
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.alifba.alifba.utils.DownloadLessonWorker
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersScreen(
    navController: NavController,
    levelId: String,
    chaptersViewModel: ChaptersViewModel,
    homeViewModel: HomeViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current

    val levelItem = homeViewModel.levelItemList.find { it.levelId == levelId }
    val levelImage = levelItem?.image ?: R.drawable.levelone

    // 1) Load chapters on first display
    LaunchedEffect(levelId) {
        Log.d("ChaptersScreen", "Fetching chapters for level: $levelId")
        chaptersViewModel.loadChapters(levelId)
    }

    // 2) Observe chapters from ViewModel
    val chapters by chaptersViewModel.chapters.observeAsState(initial = emptyList())

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
            containerColor = Color.White,
        ) {
            selectedChapter?.let { chapter ->
                // We'll pass the same repository the UI uses, for reading DB
                ChapterDownloadBottomSheetContent(
                    chapter = chapter,
                    context = context,
                    levelId = levelId,
                    navController = navController,
                    lessonCacheRepository = chaptersViewModel.lessonCacheRepository,
                    onDownloadCompleted = {
                        // Mark the chapter completed in local or Firestore
                        val nextChapterId = chaptersViewModel.getNextChapterId(chapter.id)
                        chaptersViewModel.markChapterCompleted(chapter.id, nextChapterId)

                        // Hide bottom sheet
                        selectedChapter = null
                        coroutineScope.launch { sheetState.hide() }
                    }
                )
            }
        }
    }

    // Main UI
    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(id = R.drawable.lesson_path_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

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

            // Chapters list
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                LazyChapterColumn(
                    lessons = chapters,
                    modifier = Modifier.fillMaxSize(),
                    navController = navController,
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
 * Bottom sheet that does the actual “Download and Start”
 */
@Composable
fun ChapterDownloadBottomSheetContent(
    chapter: Chapter,
    context: Context,
    levelId: String,
    navController: NavController,
    lessonCacheRepository: LessonCacheRepository, // for checking local DB
    onDownloadCompleted: () -> Unit
) {
    val alifbaFont = FontFamily(Font(R.font.more_sugar_regular, FontWeight.SemiBold))
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
                else -> { /* CANCELLED or other states */ }
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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = chapter.title,
            fontFamily = alifbaFont,
            color = navyBlue,
            fontSize = 23.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
        )

        Image(
            painter = painterResource(id = R.drawable.deenasaur),
            contentDescription = "Chapter Image",
            modifier = Modifier
                .size(150.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(24.dp))

        when (downloadState) {
            DownloadState.Initial -> {
                CommonButton(
                    buttonText = "Download and Start",
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
                    buttonText = "Start",
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
    object Initial : DownloadState()
    object Downloading : DownloadState()
    object Cached : DownloadState()
    object Downloaded : DownloadState()
    object Error : DownloadState()
}
