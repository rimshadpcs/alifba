package com.alifba.alifba.presenation.chapters.layout

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.alifba.alifba.R
import com.alifba.alifba.presenation.chapters.models.Chapter
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.hilt.navigation.compose.hiltViewModel
import com.alifba.alifba.features.authentication.DataStoreManager
import com.alifba.alifba.presenation.chapters.ChaptersViewModel
@Composable
fun LazyChapterColumn(
    lessons: List<Chapter>,
    modifier: Modifier = Modifier,
    navController: NavController,
    onChapterClick: (Chapter) -> Unit,
    viewModel: ChaptersViewModel = hiltViewModel()
) {
    val chapterStatus by viewModel.chapterStatuses.collectAsState()
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    // On tablets, constrain the overall width to a maximum (e.g., 600dp)
    val constrainedModifier = if (screenWidthDp > 600) {
        Modifier.widthIn(max = 600.dp)
    } else {
        Modifier.fillMaxWidth()
    }

    // Wrap the list in a Box that fills the screen and aligns content at the bottom.
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        LazyColumn(
            // Make the LazyColumn only take the height it needs and align it to the bottom.
            modifier = modifier.then(constrainedModifier).wrapContentHeight(align = Alignment.Bottom),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            reverseLayout = true
            // Note: Remove reverseLayout if you want the first item to appear at the bottom.
        ) {
            itemsIndexed(lessons) { index, lesson ->
                val isCompleted = chapterStatus[lesson.id.toString()] == true
                val isUnlocked = if (index == 0) true else chapterStatus[lesson.id.toString()] != null

                val iconId = when {
                    lesson.isCompleted && lesson.chapterType == "Lesson" -> R.drawable.tick
                    lesson.isCompleted && lesson.chapterType == "Story" -> R.drawable.book
                    lesson.isCompleted && lesson.chapterType == "Alphabet" -> R.drawable.alphabeticon
                    isUnlocked && lesson.chapterType == "Story" -> R.drawable.book
                    isUnlocked && lesson.chapterType == "Alphabet" -> R.drawable.alphabeticon
                    isUnlocked -> R.drawable.start
                    else -> R.drawable.padlock
                }

                LessonPathItems(
                    lesson = lesson.copy(isCompleted = isCompleted, isUnlocked = isUnlocked),
                    index = index,
                    onClick = {
                        // Use your logic to allow clicking based on the icon.
                        if (iconId == R.drawable.start ||
                            iconId == R.drawable.book ||
                            iconId == R.drawable.alphabeticon ||
                            iconId == R.drawable.tick
                        ) {
                            onChapterClick(lesson)
                        } else {
                            onChapterClick(lesson)
                        }
                    }
                )
            }
        }
    }
}
