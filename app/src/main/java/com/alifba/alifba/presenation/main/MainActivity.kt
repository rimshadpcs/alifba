package com.alifba.alifba.presenation.main

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alifba.alifba.R
import com.alifba.alifba.presenation.lessonScreens.LessonScreenViewModel
import com.alifba.alifba.presenation.Login.AuthViewModel
import com.alifba.alifba.presenation.Login.LoginScreen
import com.alifba.alifba.presenation.Login.ProfileRegistration
import com.alifba.alifba.presenation.home.HomeViewModel
import com.alifba.alifba.presenation.main.layout.AlifbaMainScreen
import com.alifba.alifba.presenation.home.layout.HomeScreen
import com.alifba.alifba.ui_components.dialogs.LottieAnimationLoading
import com.alifba.alifba.ui_components.theme.AlifbaTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val lessonScreenViewModel: LessonScreenViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        val context: Context = applicationContext
        lessonScreenViewModel.initContext(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController!!.hide(
                android.view.WindowInsets.Type.statusBars()
            )
        }

        setContent {
            val navController = rememberNavController()
            val email by authViewModel.email.collectAsState(initial = null)
            val password by authViewModel.password.collectAsState(initial = null)
            val userId by authViewModel.userId.collectAsState(initial = null)
            val firebaseUser = FirebaseAuth.getInstance().currentUser

            var isSplashScreenVisible by remember { mutableStateOf(true) }
            var startDestination by remember { mutableStateOf<String?>(null) }
            var hasProfilesState by remember { mutableStateOf<Boolean?>(null) }

            LaunchedEffect(firebaseUser, email, password, userId) {
                Log.d("MainActivity", "LaunchedEffect1 - firebaseUser: $firebaseUser, email: $email, password: $password, userId: $userId")
                if (firebaseUser != null && !email.isNullOrEmpty() && !password.isNullOrEmpty() && !userId.isNullOrEmpty()) {
                    // User is signed in
                    val hasProfiles = authViewModel.checkForChildProfiles()
                    hasProfilesState = hasProfiles
                    Log.d("MainActivity", "LaunchedEffect1 - hasProfilesState set to $hasProfiles")
                } else {
                    hasProfilesState = false // User is not signed in
                    Log.d("MainActivity", "LaunchedEffect1 - hasProfilesState set to false")
                }
            }

            LaunchedEffect(hasProfilesState) {
                Log.d("MainActivity", "LaunchedEffect2 - hasProfilesState: $hasProfilesState")
                if (hasProfilesState != null) {
                    startDestination = if (firebaseUser != null && hasProfilesState == true) {
                        "home"
                    } else if (firebaseUser != null && hasProfilesState == false) {
                        "createProfile"
                    } else {
                        "login"
                    }
                    Log.d("MainActivity", "LaunchedEffect2 - startDestination set to $startDestination")
                    // Ensure splash screen is visible for at least 1 second
                    delay(1000)
                    isSplashScreenVisible = false
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (startDestination != null && !isSplashScreenVisible) {
                    // Navigation setup
                    NavHost(
                        navController = navController,
                        startDestination = startDestination!!,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable("login") {
                            LoginScreen(viewModel = authViewModel, navController = navController)
                        }
                        composable("home") {
                            AlifbaMainScreen(lessonScreenViewModel, homeViewModel)
                        }
                        composable("createProfile") {
                            ProfileRegistration(navController)
                        }
                    }
                }

                if (isSplashScreenVisible) {
                    SplashScreenDummy(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(1f)
                    )
                }
            }
        }
    }
}


    @Composable
    fun SplashScreenDummy(modifier: Modifier = Modifier) {
        Box(
            modifier = modifier
                .background(color = Color.White)
        ) {
            // Center the Lottie animation
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.Center)
            ) {
                LottieAnimationLoading(
                    showDialog = remember { mutableStateOf(true) },
                    lottieFileRes = R.raw.loading_lottie
                )
            }
        }
    }


    @Preview(showBackground = true)
    @Composable
    fun AlifbaLessonPreview() {
        AlifbaTheme {
            val dummyNavController = rememberNavController()
            HomeScreen(HomeViewModel(), dummyNavController)
        }
    }
