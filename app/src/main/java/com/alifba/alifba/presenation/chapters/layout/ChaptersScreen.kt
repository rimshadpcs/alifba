import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.alifba.alifba.R
import com.alifba.alifba.presenation.chapters.ChaptersViewModel
import com.alifba.alifba.presenation.chapters.layout.LazyChapterColumn
import com.alifba.alifba.presenation.chapters.models.Chapter
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.alifba.alifba.utils.DownloadLessonWorker
import java.util.UUID
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersScreen(
    navController: NavController,
    levelId: String,
    chaptersViewModel: ChaptersViewModel = hiltViewModel() // Ensure correct ViewModel is used
) {
    val context = LocalContext.current

    // Load chapters when the screen is displayed
    LaunchedEffect(levelId) {
        chaptersViewModel.loadChapters(levelId)
    }

    val lessons by chaptersViewModel.chapters.observeAsState(initial = emptyList())
    var selectedChapter by remember { mutableStateOf<Chapter?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

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
                        // Retrieve the next chapter ID
                        val nextChapterId = chaptersViewModel.getNextChapterId(chapter.id)
                        // Mark the current chapter as completed and unlock the next chapter
                        chaptersViewModel.markChapterCompleted(chapter.id, nextChapterId)
                        // Hide the bottom sheet
                        selectedChapter = null
                        coroutineScope.launch { sheetState.hide() }
                    }
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.lesson_path_bg),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        LazyChapterColumn(
            lessons = lessons,
            modifier = Modifier.fillMaxSize(),
            navController = navController,
            onChapterClick = { chapter ->
                if (chapter.isUnlocked || chapter.isCompleted) {
                    selectedChapter = chapter
                    coroutineScope.launch { sheetState.show() }
                } else {
                    selectedChapter = chapter
                    coroutineScope.launch { sheetState.show() }
                }
            }
        )
    }
}



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
            fontFamily = alifbaFont,
            color = navyBlue,
            text = chapter.title,
            fontSize = 23.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
        )

        Image(
            painter = painterResource(id = R.drawable.deenasaur),
            contentDescription = "Chapter Image",
            modifier = Modifier
                .size(150.dp)
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(24.dp))

        when (workInfo?.state) {
            WorkInfo.State.RUNNING -> {
                CircularProgressIndicator()
            }
            WorkInfo.State.SUCCEEDED -> {
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
                    })
            }
        }
    }
}

//fun getChapterStatuses(context: Context): Flow<Map<String, Boolean>> {
//    return context.dataStore.data.map { preferences ->
//        val completedChapters = preferences[DataStoreManager.ChapterPrefKeys.COMPLETED_CHAPTER]?.split(",")?.map { it.trim() } ?: emptyList()
//        val unlockedChapters = preferences[DataStoreManager.ChapterPrefKeys.UNLOCKED_CHAPTER]?.split(",")?.map { it.trim() } ?: emptyList()
//
//        completedChapters.associateWith { true } + unlockedChapters.associateWith { false }
//    }
//}

fun enqueueChapterDownload(context: Context, chapter: Chapter): UUID {
    val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadLessonWorker>()
        .setInputData(workDataOf("chapter_id" to chapter.id))
        .build()

    WorkManager.getInstance(context).enqueue(downloadWorkRequest)
    return downloadWorkRequest.id
}
