package com.alifba.alifba.presentation.lessonPath.layout

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alifba.alifba.R
import com.alifba.alifba.presentation.lessonPath.LessonPathViewModel
@Composable
fun LessonsPathScreen(navController: NavController) {
    val viewModel: LessonPathViewModel = viewModel()

    // Observe introductionLessons from ViewModel
    val lessons = viewModel.introductionLessons.observeAsState(initial = emptyList())

    Box(modifier = Modifier.fillMaxSize()) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.lessonpath_bg),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Pass the observed lessons to LazyLessonPathColumn
        LazyLessonPathColumn(lessons = lessons.value.reversed(), navController = navController)
    }

}
