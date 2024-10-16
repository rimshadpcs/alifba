package com.alifba.alifba.presenation.chapters.layout

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.alifba.alifba.R
import com.alifba.alifba.presenation.chapters.ChaptersViewModel
import com.alifba.alifba.presenation.chapters.models.Chapter
import com.alifba.alifba.utils.DownloadLessonWorker
import java.util.UUID


@Composable
fun ChaptersScreen(navController: NavController, levelId: String) {
    val viewModel: ChaptersViewModel = viewModel()
    val context = LocalContext.current

    // Load the chapters when the screen is displayed
    LaunchedEffect(levelId) {
        viewModel.loadChapters(levelId)
    }


    // Observe lessons from ViewModel
    val lessons by viewModel.lessons.observeAsState(initial = emptyList())

    // State to track the selected chapter for download
    var selectedChapter by remember { mutableStateOf<Chapter?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.lessonpath_bg),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        LazyChapterColumn(
            lessons = lessons,
            modifier = Modifier.fillMaxSize(),
            navController = navController,
            onChapterClick = { chapter ->
                // Set the selected chapter when clicked
                selectedChapter = chapter
            }
        )

        // Show the download UI if a chapter is selected
        selectedChapter?.let { chapter ->
            DownloadAndNavigate(
                chapter = chapter,
                context = context,
                navController = navController,
                levelId = levelId,
                onDownloadCompleted = {
                    // Reset selected chapter after download completes
                    selectedChapter = null
                }
            )
        }
    }
}

@Composable
fun DownloadAndNavigate(
    chapter: Chapter,
    context: Context,
    levelId: String,
    navController: NavController,
    onDownloadCompleted: () -> Unit
) {
    // Observe the work state of the current download
    val workId = remember { mutableStateOf<UUID?>(null) }

    val workInfo = workId.value?.let {
        WorkManager.getInstance(context).getWorkInfoByIdLiveData(it).observeAsState()
    }?.value

    when (workInfo?.state) {
        WorkInfo.State.RUNNING -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        WorkInfo.State.SUCCEEDED -> {
            LaunchedEffect(chapter.id) {
                navController.navigate("lessonScreen/${chapter.id}/$levelId")
                onDownloadCompleted()  // Reset selected chapter after download completes
            }
        }
        WorkInfo.State.FAILED -> {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Failed to download lesson", color = Color.Red)
            }
        }
        else -> {
            Button(onClick = {
                val newWorkId = enqueueChapterDownload(context, chapter)
                workId.value = newWorkId
            }) {
                Text("Download & Start")
            }
        }
    }

}

fun enqueueChapterDownload(context: Context, chapter: Chapter): UUID {
    val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadLessonWorker>()
        .setInputData(workDataOf("chapter_id" to chapter.id))
        .build()

    WorkManager.getInstance(context).enqueue(downloadWorkRequest)

    return downloadWorkRequest.id
}
