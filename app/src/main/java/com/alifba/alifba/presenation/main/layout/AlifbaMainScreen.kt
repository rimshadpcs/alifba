package com.alifba.alifba.presenation.main.layout

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.alifba.alifba.R
import com.alifba.alifba.presenation.Login.AuthViewModel
import com.alifba.alifba.presenation.Login.LoginScreen
import com.alifba.alifba.presenation.chapters.ChaptersScreen
import com.alifba.alifba.presenation.chapters.ChaptersViewModel
import com.alifba.alifba.presenation.chapters.layout.LevelInfoScreen
import com.alifba.alifba.presenation.lessonScreens.LessonScreenViewModel
import com.alifba.alifba.presenation.lessonScreens.LessonScreen
import com.alifba.alifba.presenation.home.HomeViewModel
import com.alifba.alifba.presenation.home.layout.profile.ChangeAvatarScreen
import com.alifba.alifba.presenation.home.layout.HomeScreen
import com.alifba.alifba.presenation.home.layout.HomeTopBar
import com.alifba.alifba.presenation.home.layout.profile.ProfileScreen
import com.alifba.alifba.presenation.home.layout.ProfileViewModel
import com.alifba.alifba.presenation.home.layout.profile.AllBadgesScreen
import com.alifba.alifba.presenation.home.layout.settings.AccountScreen
import com.alifba.alifba.presenation.home.layout.settings.SettingsScreen
import com.alifba.alifba.ui_components.theme.AlifbaTheme


@Composable
fun HomeScreenWithScaffold(navController: NavController,homeViewModel: HomeViewModel, chaptersViewModel: ChaptersViewModel,authViewModel: AuthViewModel) {
    Scaffold(topBar = {
        Column {
            Spacer(modifier = Modifier.height(2.dp)) // Spacer on top of the TopAppBar
            HomeTopBar(navController,authViewModel)
        }
    }, content = { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {

            LottieAnimationScreen()
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            HomeScreen(homeViewModel, navController)
            Spacer(modifier = Modifier.weight(1f))
        }
    })
}

@Composable
fun AlifbaMainScreen(lessonViewModel: LessonScreenViewModel, homeViewModel: HomeViewModel,chaptersViewModel: ChaptersViewModel,authViewModel: AuthViewModel = hiltViewModel(),profileViewModel: ProfileViewModel= hiltViewModel()) {
   // val viewModel: LessonScreenViewModel = viewModel() // Create or obtain the viewModel instance
    LaunchedEffect(Unit) {
        authViewModel.fetchUserProfile()
    }

    val navController = rememberNavController()
    AlifbaTheme {
        NavHost(navController, startDestination = "homeScreen") {
            composable("homeScreen") {
                HomeScreenWithScaffold(
                    navController,
                    homeViewModel,
                    chaptersViewModel = chaptersViewModel,
                    authViewModel
                )
            }
            composable("profile") {
                ProfileScreen(navController, profileViewModel)
            }
            composable("changeAvatar") {
                ChangeAvatarScreen(navController = navController)
            }
            composable(
                route = "levelInfo/{levelId}/{levelImage}",
                arguments = listOf(
                    navArgument("levelId") { type = NavType.StringType },
                    navArgument("levelImage") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val levelId = backStackEntry.arguments?.getString("levelId") ?: ""
                val levelImage = backStackEntry.arguments?.getInt("levelImage") ?: R.drawable.levelone

                LevelInfoScreen(
                    chaptersViewModel = chaptersViewModel,
                    levelId = levelId,
                    levelImage = levelImage,
                    navController = navController
                )
            }



            composable("settings") {
                SettingsScreen(navController)
            }
            composable("accountScreen") {
                AccountScreen(authViewModel = authViewModel, navController = navController)
            }
            composable("allBadges") {
                AllBadgesScreen(navController = navController, profileViewModel = profileViewModel)
            }
            composable("lessonPathScreen/{levelId}") { backStackEntry ->
                val levelId = backStackEntry.arguments?.getString("levelId") ?: return@composable
                // PASS THE SAME chaptersViewModel HERE
                ChaptersScreen(
                    navController = navController,
                    levelId = levelId,
                    chaptersViewModel = chaptersViewModel,
                    homeViewModel = HomeViewModel()

                )
            }
            composable("lessonScreen/{lessonId}/{levelId}") { backStackEntry ->
                val lessonId = backStackEntry.arguments?.getString("lessonId")?.toIntOrNull()
                    ?: return@composable
                val levelId = backStackEntry.arguments?.getString("levelId") ?: return@composable
                LessonScreen(
                    lessonId = lessonId,
                    levelId = levelId,
                    navController = navController,
                    navigateToChapterScreen = {
                        navController.navigate("lessonPathScreen/$levelId")
                    },
                    viewModel = lessonViewModel,
                    chaptersViewModel = chaptersViewModel
                )
            }
            // Add login screen route
            composable("login") {
                LoginScreen(viewModel = authViewModel, navController = navController)
            }
        }
    }
}



@Composable
fun AlifbaMainScreenPreview(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    AlifbaTheme {
        Scaffold(
            topBar = {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    HomeTopBar(navController,authViewModel) // Assuming HomeTopBar is defined somewhere in your code.
                }
            },
            content = { paddingValues ->
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.alifba_homenew), // Make sure this resource is available
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
                    //Spacer(modifier = Modifier.weight(1f))
                    MockHomeScreen() // Mock version for preview
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        )
    }
}
@Composable
fun LottieAnimationScreen() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.skybackground))

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.newbg), // Ensure this resource is available
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // This scales the image to fill the size of its container
        )

        // Lottie Animation
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.fillMaxSize() // Ensures the Lottie animation covers the full area of the box
        )
    }
}


@Composable
fun MockHomeScreen() {
    // This is a placeholder for preview purposes, adjust as needed.
    //Text("Home Screen", modifier = Modifier.align(Alignment.Center))
}

@Composable
fun HomeTopBar() {
    // Placeholder TopAppBar for preview
    Text("Top App Bar", modifier = Modifier.padding(16.dp))
}

// Assuming AlifbaTheme is defined somewhere in your code.
@Composable
fun AlifbaTheme(content: @Composable () -> Unit) {
    content()
}
