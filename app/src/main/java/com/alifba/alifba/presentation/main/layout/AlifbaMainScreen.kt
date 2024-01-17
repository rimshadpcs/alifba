package com.alifba.alifba.presentation.main.layout

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alifba.alifba.R
import com.alifba.alifba.models.LessonScreenViewModel
import com.alifba.alifba.presentation.lessonScreens.LessonScreen
import com.alifba.alifba.presentation.home.HomeViewModel
import com.alifba.alifba.presentation.home.layout.HomeScreen
import com.alifba.alifba.presentation.home.layout.HomeTopBar
import com.alifba.alifba.presentation.lessonPath.LessonPathViewModel
import com.alifba.alifba.presentation.lessonPath.layout.LessonsPathScreen
import com.alifba.alifba.ui.theme.AlifbaTheme


@Composable
fun HomeScreenWithScaffold(navController: NavController) {
    Scaffold(topBar = {
        Column {
            Spacer(modifier = Modifier.height(8.dp)) // Spacer on top of the TopAppBar
            HomeTopBar() // Your custom TopAppBar
        }
    }, content = { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.alifba_home),//background sky image
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            HomeScreen(HomeViewModel(), navController) // Your HomeScreen composable
            Spacer(modifier = Modifier.weight(1f))
        }
    })
}
//main screen containing home screen
@Composable
fun AlifbaMainScreen() {
    val viewModel: LessonScreenViewModel = viewModel() // Create or obtain the viewModel instance
    val navController = rememberNavController()
    AlifbaTheme {
        NavHost(navController, startDestination = "homeScreen") {
            composable("homeScreen") {
                HomeScreenWithScaffold(navController)
            }
            composable("lessonPathScreen") {
                LessonsPathScreen(navController)
            }
            composable("lessonScreen/{lessonId}") { backStackEntry ->
                val lessonId = backStackEntry.arguments?.getString("lessonId")?.toIntOrNull() ?: return@composable
                LessonScreen(lessonId, navigateToLessonPathScreen = {
                    navController.navigate("lessonPathScreen")
                }, viewModel = viewModel) // Pass the viewModel instance here
            }
        }
    }
}


