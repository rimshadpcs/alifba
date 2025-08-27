package com.alifba.alifba.presenation.chapters.layout

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.alifba.alifba.R
import com.alifba.alifba.presenation.chapters.ChaptersViewModel
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent

@Composable
fun LevelInfoScreen(
    chaptersViewModel: ChaptersViewModel,
    levelId: String,
    levelImage:Int,
    navController: NavController
) {
    LaunchedEffect(Unit) {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "LevelInfoScreen")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "LevelInfoScreen")
        }
    }


    LaunchedEffect(levelId) {
        chaptersViewModel.getLevelSummary(levelId)
    }
    val levelSummary by chaptersViewModel.levelSummary.collectAsState()
    Log.d("LevelInfoScreen", "Loading level summary for levelId: $levelId")

    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Bold)
    )
    Log.d("LevelInfoScreen", "Current levelSummary: $levelSummary")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(white),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.goback),
                contentDescription = "Back",
                modifier = Modifier
                    .clickable { navController.popBackStack() }
                    .size(48.dp),
            )

            Text(
                text = levelId.replace("level", "Level "),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = navyBlue,
                fontFamily = FontFamily(Font(R.font.vag_round, FontWeight.Bold))
            )
            // Empty box for symmetry
            Box(modifier = Modifier.size(48.dp))
        }

        Image(
            painter = painterResource(id = levelImage),
            contentDescription = "Level image",
            modifier = Modifier.size(280.dp)
        )
        levelSummary?.let {
            Text(
                text = it.levelDescription,
                Modifier.padding(16.dp),
                fontSize = 20.sp,
                fontFamily = alifbaFont,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Start)
                .padding(48.dp)
        ) {
            LevelInfoRow(
                icon = R.drawable.chapters,
                label = "Lessons",
                count = (levelSummary?.totalChapters ?: 0).toString(),
                alifbaFont = alifbaFont
            )

            Spacer(modifier = Modifier.padding(8.dp))
            LevelInfoRow(
                icon = R.drawable.story,
                label = "Stories",
                count = (levelSummary?.totalStories ?: 0).toString(),
                alifbaFont = alifbaFont
            )

            Spacer(modifier = Modifier.padding(8.dp))

            // Quizzes Row
            LevelInfoRow(
                icon = R.drawable.quizzesnew,
                label = "Quizzes",
                count = "10+",
                alifbaFont = alifbaFont
            )

            Spacer(modifier = Modifier.padding(8.dp))

            // Activities Row
            LevelInfoRow(
                icon = R.drawable.alphab,
                label = "Activities",
                count = (levelSummary?.totalActivities ?: 0).toString(),
                alifbaFont = alifbaFont
            )
        }

    }

}


@Composable
fun LevelInfoRow(
    icon: Int,
    label: String,
    count: String,
    alifbaFont: FontFamily,
    showPlus: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = "$label icon",
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = "$label: ${count}${if (showPlus) "+" else ""}",
            fontSize = 24.sp,
            fontFamily = alifbaFont,
            textAlign = TextAlign.Center,
            color = navyBlue,
            modifier = Modifier.weight(1f)
        )
    }
}