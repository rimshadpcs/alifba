package com.alifba.alifba.presentation.home.layout

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.alifba.alifba.presentation.home.HomeViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel, navController: NavController) {
    val scrollState = rememberLazyListState()

    LazyRow(
        state = scrollState,
        reverseLayout = true
    ) {
        items(viewModel.lessonMenuItemList.size) { index ->
            val item = viewModel.lessonMenuItemList[index]
            LessonMenuItems(
                image = item.image,
                name = item.name,
                onClick = {
                    navController.navigate("lessonPathScreen")
                }
            )
        }
    }
}

