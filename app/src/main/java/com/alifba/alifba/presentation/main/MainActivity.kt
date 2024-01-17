package com.alifba.alifba.presentation.main

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
import com.alifba.alifba.presentation.home.HomeViewModel
import com.alifba.alifba.presentation.main.layout.AlifbaMainScreen
import com.alifba.alifba.presentation.home.layout.HomeScreen
import com.alifba.alifba.ui.theme.AlifbaTheme

class MainActivity : ComponentActivity() {

    val viewModel: LessonScreenViewModel by viewModels()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context: Context = applicationContext // Obtain the Context
        viewModel.initContext(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController!!.hide(
                android.view.WindowInsets.Type.statusBars()
            )
        }
        setContent {
            AlifbaMainScreen()
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
