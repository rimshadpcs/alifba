package com.alifba.alifba.presenation.chapters.layout

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.alifba.alifba.presenation.chapters.models.Chapter
@Composable
fun LazyChapterColumn(
    lessons: List<Chapter>,
    modifier: Modifier = Modifier,
    navController: NavController,
    onChapterClick: (Chapter) -> Unit // Accept onChapterClick as a lambda to handle chapter clicks
) {
    val listState = rememberLazyListState()     // Remember the state of the list

    // Auto-scroll to the bottom on initial composition or when lessons change
    LaunchedEffect(lessons) {
        if (lessons.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(), // Apply the modifier passed into the function
        verticalArrangement = Arrangement.spacedBy(32.dp),
        reverseLayout = true  // Start from the bottom
    ) {
        // Footer item
        item {
            Image(
                painter = painterResource(id = R.drawable.footer), // Replace with your footer image resource
                contentDescription = "Footer",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.FillWidth
            )
        }

        // Lesson items
        itemsIndexed(lessons) { index, lesson ->
            LessonPathItems(lesson = lesson, index = index, onClick = {
                onChapterClick(lesson) // Trigger the callback when the chapter is clicked
            })
        }
    }
}


fun getMockLessonPathItems(): List<Chapter> {
    return listOf(
        Chapter(id = 1, title = "Lesson 1", isCompleted = true, isLocked = false, iconResId = R.drawable.start, isUnlocked = true),
        Chapter(id = 2, title = "Lesson 2", isCompleted = true, isLocked = false,iconResId = R.drawable.padlock, isUnlocked = false),
        Chapter(id = 3, title = "Lesson 3", isCompleted = false, isLocked = false,iconResId = R.drawable.padlock, isUnlocked = false),
        Chapter(id = 4, title = "Lesson 4", isCompleted = false, isLocked = true,iconResId = R.drawable.padlock, isUnlocked = false),
        Chapter(id = 5, title = "Lesson 1", isCompleted = false, isLocked = true,iconResId = R.drawable.padlock, isUnlocked = false),
        Chapter(id = 6, title = "Lesson 2", isCompleted = true, isLocked = false,iconResId = R.drawable.padlock, isUnlocked = false),
        Chapter(id = 7, title = "Lesson 3", isCompleted = false, isLocked = false,iconResId = R.drawable.padlock, isUnlocked = false),
        Chapter(id = 8, title = "Lesson 4", isCompleted = false, isLocked = true,iconResId = R.drawable.padlock, isUnlocked = false),

        )
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
fun LessonsPathScreenPreview() {
    val navController= NavController(context = LocalContext.current)
    val mockLessons = getMockLessonPathItems()
    LazyChapterColumn(lessons = mockLessons,navController=navController, onChapterClick = {})
}
