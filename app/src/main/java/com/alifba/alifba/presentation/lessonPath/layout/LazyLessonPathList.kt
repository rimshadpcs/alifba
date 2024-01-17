package com.alifba.alifba.presentation.lessonPath.layout

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.alifba.alifba.R
import com.alifba.alifba.presentation.lessonPath.models.LessonPathItem
@Composable
fun LazyLessonPathColumn(
    lessons: List<LessonPathItem>,
    modifier: Modifier = Modifier,
    navController: NavController,) {
    val reversedLessons = lessons.asReversed()  // Reverse the list
    val listState = rememberLazyListState()     // Remember the state of the list
    val selectedLesson = remember { mutableStateOf<LessonPathItem?>(null) }

    // Auto-scroll to the bottom on initial composition
    LaunchedEffect(key1 = "scrollToBottom") {
        listState.scrollToItem(0)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        reverseLayout = true  // Start from the bottom
    ) {
        // Header item
        item {
            Image(
                painter = painterResource(id = R.drawable.footer), // Replace with your footer image resource
                contentDescription = "Header",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
        }

        // Lesson items
        itemsIndexed(reversedLessons) { index, lesson ->
            LessonPathItems(lesson = lesson, index = index, onClick = {
                selectedLesson.value = lesson
                //Toast.makeText(context, lesson.title, Toast.LENGTH_SHORT).show()
                navController.navigate("lessonScreen/${lesson.id}")            })
        }

        // Footer item
        item {
            Image(
                painter = painterResource(id = R.drawable.header), // Replace with your header image resource
                contentDescription = "Footer",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

fun getMockLessonPathItems(): List<LessonPathItem> {
    return listOf(
        LessonPathItem(id = 1, title = "Lesson 1", isCompleted = true, isLocked = false, iconResId = R.drawable.start, isUnlocked = true),
        LessonPathItem(id = 2, title = "Lesson 2", isCompleted = true, isLocked = false,iconResId = R.drawable.padlock, isUnlocked = false),
        LessonPathItem(id = 3, title = "Lesson 3", isCompleted = false, isLocked = false,iconResId = R.drawable.padlock, isUnlocked = false),
        LessonPathItem(id = 4, title = "Lesson 4", isCompleted = false, isLocked = true,iconResId = R.drawable.padlock, isUnlocked = false),
        LessonPathItem(id = 5, title = "Lesson 1", isCompleted = false, isLocked = true,iconResId = R.drawable.padlock, isUnlocked = false),
        LessonPathItem(id = 6, title = "Lesson 2", isCompleted = true, isLocked = false,iconResId = R.drawable.padlock, isUnlocked = false),
        LessonPathItem(id = 7, title = "Lesson 3", isCompleted = false, isLocked = false,iconResId = R.drawable.padlock, isUnlocked = false),
        LessonPathItem(id = 8, title = "Lesson 4", isCompleted = false, isLocked = true,iconResId = R.drawable.padlock, isUnlocked = false),

        )
}
//
//@Preview(showBackground = true, widthDp = 320, heightDp = 640)
//@Composable
//fun LessonsPathScreenPreview() {
//    val mockLessons = getMockLessonPathItems()
//    LazyLessonPathColumn(lessons = mockLessons)
//}
