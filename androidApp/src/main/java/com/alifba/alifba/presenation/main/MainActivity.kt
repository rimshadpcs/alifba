package com.alifba.alifba.presenation.main

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.alifba.alifba.models.LessonScreenViewModel
import com.alifba.alifba.presenation.home.HomeViewModel
import com.alifba.alifba.presenation.main.layout.AlifbaMainScreen
import com.alifba.alifba.presenation.home.layout.HomeScreen
import com.alifba.alifba.ui_components.theme.AlifbaTheme

class MainActivity : ComponentActivity() {

    val lessonScreenViewModel: LessonScreenViewModel by viewModels()
    val homeViewModel: HomeViewModel by viewModels()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context: Context = applicationContext // Obtain the Context
        lessonScreenViewModel.initContext(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController!!.hide(
                android.view.WindowInsets.Type.statusBars()
            )
        }
        setContent {
            AlifbaMainScreen(lessonScreenViewModel,homeViewModel)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlifbaLessonPreview() {
    AlifbaTheme {
        val dummyNavController = rememberNavController()
        HomeScreen(HomeViewModel(),dummyNavController)
    }
}
