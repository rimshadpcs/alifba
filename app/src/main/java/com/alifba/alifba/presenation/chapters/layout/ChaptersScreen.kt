package com.alifba.alifba.presenation.chapters

import android.content.Context
import androidx.compose.foundation.Image
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.work.*
import com.alifba.alifba.R
import com.alifba.alifba.presenation.chapters.layout.LazyChapterColumn
import com.alifba.alifba.presenation.chapters.models.Chapter
import com.alifba.alifba.ui_components.dialogs.BadgeEarnedSnackBar
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.alifba.alifba.utils.DownloadLessonWorker
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersScreen(
    navController: NavController,
    levelId: String,
    // We assume you’re already passing the same instance from NavGraph
    chaptersViewModel: ChaptersViewModel
) {
    // For the bottom sheet
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current

    // 1) Load chapters on first display
    LaunchedEffect(levelId) {
        chaptersViewModel.loadChapters(levelId)
    }

    // 2) Observe data
    val chapters by chaptersViewModel.chapters.observeAsState(initial = emptyList())
    val earnedBadge by chaptersViewModel.badgeEarnedEvent.collectAsState()
    var selectedChapter by remember { mutableStateOf<Chapter?>(null) }

    // 3) If bottom sheet is open, display it
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
                ChapterDownloadBottomSheetContent(
                    chapter = chapter,
                    context = context,
                    levelId = levelId,
                    navController = navController,
                    onDownloadCompleted = {
                        val nextChapterId = chaptersViewModel.getNextChapterId(chapter.id)
                        chaptersViewModel.markChapterCompleted(chapter.id, nextChapterId)
                        selectedChapter = null
                        coroutineScope.launch { sheetState.hide() }
                    }
                )
            }
        }
    }

    // 4) Main UI container
    Box(modifier = Modifier.fillMaxSize()) {

        // A) Background image
        Image(
            painter = painterResource(id = R.drawable.lesson_path_bg),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // B) Chapter list
        LazyChapterColumn(
            lessons = chapters,
            modifier = Modifier.fillMaxSize(),
            navController = navController,
            onChapterClick = { chapter ->
                if (chapter.isUnlocked || chapter.isCompleted) {
                    selectedChapter = chapter
                    coroutineScope.launch { sheetState.show() }
                }
            }
        )

        // C) If a badge is earned, show the new “snack bar” near the top
        earnedBadge?.let { badge ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter) // Align top
                    .zIndex(1f)                 // Draw above other elements
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
 * Your bottom sheet content for downloading lessons.
 */
@Composable
fun ChapterDownloadBottomSheetContent(
    chapter: Chapter,
    context: Context,
    levelId: String,
    navController: NavController,
    onDownloadCompleted: () -> Unit
) {
    val workId = remember { mutableStateOf<UUID?>(null) }
    val workInfo = workId.value?.let {
        WorkManager.getInstance(context).getWorkInfoByIdLiveData(it).observeAsState()
    }?.value

    // Example custom font
    val alifbaFont = FontFamily(
        Font(R.font.more_sugar_regular, FontWeight.SemiBold)
    )

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

        // Example image
        Image(
            painter = painterResource(id = R.drawable.deenasaur),
            contentDescription = "Chapter Image",
            modifier = Modifier
                .size(150.dp)
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Download or show progress
        when (workInfo?.state) {
            WorkInfo.State.RUNNING -> {
                CircularProgressIndicator()
            }
            WorkInfo.State.SUCCEEDED -> {
                // Once succeeded, navigate automatically
                LaunchedEffect(chapter.id) {
                    navController.navigate("lessonScreen/${chapter.id}/$levelId")
                    onDownloadCompleted()
                }
            }
            WorkInfo.State.FAILED -> {
                Text("Failed to download lesson", color = Color.Red)
            }
            else -> {
                CommonButton(
                    buttonText = "Download and Start",
                    mainColor = lightNavyBlue,
                    shadowColor = navyBlue,
                    textColor = Color.White,
                    onClick = {
                        val newWorkId = enqueueChapterDownload(context, chapter)
                        workId.value = newWorkId
                    }
                )
            }
        }
    }
}

/**
 * Enqueue your worker for downloading.
 */
fun enqueueChapterDownload(context: Context, chapter: Chapter): UUID {
    val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadLessonWorker>()
        .setInputData(workDataOf("chapter_id" to chapter.id))
        .build()

    WorkManager.getInstance(context).enqueue(downloadWorkRequest)
    return downloadWorkRequest.id
}
