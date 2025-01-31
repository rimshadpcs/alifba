package com.alifba.alifba.presenation.chapters.layout

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
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
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val chapterStatus by viewModel.chapterStatuses.collectAsState()

    LaunchedEffect(lessons) {
        if (lessons.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        reverseLayout = true
    ) {
        itemsIndexed(lessons) { index, lesson ->
            val isCompleted = chapterStatus[lesson.id.toString()] == true
            val isUnlocked = if (index == 0) true else chapterStatus[lesson.id.toString()] != null

            // Determine the icon based on completion and unlock status
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
                    // **Workaround Logic:**
                    // Allow opening if the icon is either 'start' (play) or 'book'
                    if (iconId == R.drawable.start || iconId == R.drawable.book || iconId ==R.drawable.alphabeticon || iconId ==R.drawable.tick) {
                        onChapterClick(lesson)
                    } else {
                        onChapterClick(lesson)
                        //Toast.makeText(context, "You must complete the previous chapter", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}



//fun getMockLessonPathItems(): List<Chapter> {
//    return listOf(
//        Chapter(id = 1, title = "Lesson 1", isCompleted = true, isLocked = false, iconResId = R.drawable.start, isUnlocked = true),
//        Chapter(id = 2, title = "Lesson 2", isCompleted = true, isLocked = false,iconResId = R.drawable.padlock, isUnlocked = false),
//        Chapter(id = 3, title = "Lesson 3", isCompleted = false, isLocked = false,iconResId = R.drawable.padlock, isUnlocked = false),
//        Chapter(id = 4, title = "Lesson 4", isCompleted = false, isLocked = true,iconResId = R.drawable.padlock, isUnlocked = false),
//        Chapter(id = 5, title = "Lesson 1", isCompleted = false, isLocked = true,iconResId = R.drawable.padlock, isUnlocked = false),
//        Chapter(id = 6, title = "Lesson 2", isCompleted = true, isLocked = false,iconResId = R.drawable.padlock, isUnlocked = false),
//        Chapter(id = 7, title = "Lesson 3", isCompleted = false, isLocked = false,iconResId = R.drawable.padlock, isUnlocked = false),
//        Chapter(id = 8, title = "Lesson 4", isCompleted = false, isLocked = true,iconResId = R.drawable.padlock, isUnlocked = false),
//
//        )
//}

//@Preview(showBackground = true, widthDp = 320, heightDp = 640)
//@Composable
//fun LessonsPathScreenPreview() {
//    val navController= NavController(context = LocalContext.current)
//    val mockLessons = getMockLessonPathItems()
//    LazyChapterColumn(lessons = mockLessons,navController=navController, onChapterClick = {})
//}
