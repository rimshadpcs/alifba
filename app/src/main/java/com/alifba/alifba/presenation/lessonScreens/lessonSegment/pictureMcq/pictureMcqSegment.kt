package com.alifba.alifba.presenation.lessonScreens.lessonSegment.pictureMcq

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.alifba.alifba.R
import com.alifba.alifba.data.models.LessonSegment
import com.alifba.alifba.data.models.PictureMcqItem
import com.alifba.alifba.presenation.main.logScreenView
import com.alifba.alifba.ui_components.dialogs.LottieAnimationDialog
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.navyBlue

import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.alifba.alifba.ui_components.widgets.buttons.PictureButton
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import kotlinx.coroutines.delay
import com.alifba.alifba.ui_components.widgets.texts.CommonExplanationText as CommonExplanationText

@Composable
fun PictureMcqSegment(segment: LessonSegment.PictureMcqLesson, onNextClicked: () -> Unit, showNextButton: Boolean,) {
    val showNextButtonState = remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }
    val animationFinished = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600
    // Play audio if needed
    // PlayAudio(audioResId = segment.speech)
    LaunchedEffect(Unit) {
        logScreenView("lesson_screen")
    }
    LaunchedEffect(Unit) {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "PictureMcqSegment")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "PictureMcqSegment")
        }
    }
    Column(
        modifier = Modifier
            .padding(if (isTablet) 24.dp else 16.dp)
            .fillMaxWidth()
    ) {
        // Display the question text
        CommonExplanationText(
            text = segment.question,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = if (isTablet) 24.dp else 16.dp)
        )

        // Display the picture choices as buttons in a custom 2x2 grid with centered third item
        CustomPictureGrid(
            items = segment.pictureChoices,
            isTablet = isTablet,
            onItemClick = { item ->
                if (item.answer) {
                    showNextButtonState.value = true
                    showDialog.value = true
                } else {
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(100)
                    }
                }
            }
        )

        // Show dialog animation on correct answer
        if (showDialog.value) {
            LottieAnimationDialog(showDialog = showDialog, lottieFileRes = R.raw.tick)
            LaunchedEffect(showDialog.value) {
                delay(2000)
                showDialog.value = false
                animationFinished.value = true
            }
        }
    }

    // Show "Next" button after animation finishes
    if (showNextButtonState.value && animationFinished.value) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            CommonButton(
                onClick = onNextClicked,
                buttonText = "Next",
                mainColor = lightNavyBlue,
                shadowColor = navyBlue,
                textColor = white
            )
        }
    }
}

@Composable
fun CustomPictureGrid(
    items: List<PictureMcqItem>,
    isTablet: Boolean,
    onItemClick: (PictureMcqItem) -> Unit
) {
    val spacing = if (isTablet) 16.dp else 8.dp
    val padding = if (isTablet) 12.dp else 8.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        when (items.size) {
            1 -> {
                // Single item - center it
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    PictureButton(
                        onClick = { onItemClick(items[0]) },
                        buttonImage = items[0].image,
                        buttonText = items[0].choice,
                        isTablet = isTablet
                    )
                }
            }
            2 -> {
                // Two items - place them side by side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally)
                ) {
                    items.forEach { item ->
                        PictureButton(
                            onClick = { onItemClick(item) },
                            buttonImage = item.image,
                            buttonText = item.choice,
                            isTablet = isTablet,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            3 -> {
                // Three items - first two in top row, third centered in bottom row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally)
                ) {
                    PictureButton(
                        onClick = { onItemClick(items[0]) },
                        buttonImage = items[0].image,
                        buttonText = items[0].choice,
                        isTablet = isTablet,
                        modifier = Modifier.weight(1f)
                    )
                    PictureButton(
                        onClick = { onItemClick(items[1]) },
                        buttonImage = items[1].image,
                        buttonText = items[1].choice,
                        isTablet = isTablet,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Third item centered in the bottom row
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    PictureButton(
                        onClick = { onItemClick(items[2]) },
                        buttonImage = items[2].image,
                        buttonText = items[2].choice,
                        isTablet = isTablet
                    )
                }
            }
            else -> {
                // Four or more items - use 2x2 grid layout
                items.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally)
                    ) {
                        rowItems.forEach { item ->
                            PictureButton(
                                onClick = { onItemClick(item) },
                                buttonImage = item.image,
                                buttonText = item.choice,
                                isTablet = isTablet,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Add empty space if this row has only one item (for odd number of items > 3)
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFFFFFF, widthDp = 320, heightDp = 640, name = "Phone Preview - 3 Items")
@Composable
fun PictureMcqSegmentPhonePreview() {
    val sampleSegment = LessonSegment.PictureMcqLesson(
        question = "Which of this sunnah should we do after eating food?",
        pictureChoices = listOf(
            PictureMcqItem("android.resource://com.alifba.alifba/" + R.drawable.ic_launcher_background, "Play video game", false),
            PictureMcqItem("android.resource://com.alifba.alifba/" + R.drawable.ic_launcher_background, "Wash your hands", true),
            PictureMcqItem("android.resource://com.alifba.alifba/" + R.drawable.ic_launcher_background, "Watch TV", false)
        )
    )
    PictureMcqSegment(segment = sampleSegment, onNextClicked = { /* Implement action */ }, showNextButton = false)
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF, widthDp = 800, heightDp = 1280, name = "Tablet Preview - 3 Items")
@Composable
fun PictureMcqSegmentTabletPreview() {
    val sampleSegment = LessonSegment.PictureMcqLesson(
        question = "Which of this sunnah should we do after eating food?",
        pictureChoices = listOf(
            PictureMcqItem("android.resource://com.alifba.alifba/" + R.drawable.ic_launcher_background, "Play video game", false),
            PictureMcqItem("android.resource://com.alifba.alifba/" + R.drawable.ic_launcher_background, "Wash your hands", true),
            PictureMcqItem("android.resource://com.alifba.alifba/" + R.drawable.ic_launcher_background, "Watch TV", false)
        )
    )
    PictureMcqSegment(segment = sampleSegment, onNextClicked = { /* Implement action */ }, showNextButton = false)
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF, widthDp = 400, heightDp = 600, name = "Custom Grid Preview")
@Composable
fun CustomPictureGridPreview() {
    val sample3Items = listOf(
        PictureMcqItem("android.resource://com.alifba.alifba/" + R.drawable.ic_launcher_background, "Option A", false),
        PictureMcqItem("android.resource://com.alifba.alifba/" + R.drawable.ic_launcher_background, "Option B", true),
        PictureMcqItem("android.resource://com.alifba.alifba/" + R.drawable.ic_launcher_background, "Option C (Centered)", false)
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        CommonExplanationText(
            text = "Which option is correct? (Third item should be centered)",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
        )

        CustomPictureGrid(
            items = sample3Items,
            isTablet = false,
            onItemClick = { /* Preview action */ }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF, widthDp = 400, heightDp = 700, name = "4 Items Grid Preview")
@Composable
fun FourItemsGridPreview() {
    val sample4Items = listOf(
        PictureMcqItem("android.resource://com.alifba.alifba/" + R.drawable.ic_launcher_background, "Option A", false),
        PictureMcqItem("android.resource://com.alifba.alifba/" + R.drawable.ic_launcher_background, "Option B", true),
        PictureMcqItem("android.resource://com.alifba.alifba/" + R.drawable.ic_launcher_background, "Option C", false),
        PictureMcqItem("android.resource://com.alifba.alifba/" + R.drawable.ic_launcher_background, "Option D", false)
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        CommonExplanationText(
            text = "Which option is correct? (4 items in 2x2 grid)",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
        )

        CustomPictureGrid(
            items = sample4Items,
            isTablet = false,
            onItemClick = { /* Preview action */ }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF, widthDp = 400, heightDp = 800, name = "5 Items Grid Preview")
@Composable
fun FiveItemsGridPreview() {
    val sample5Items = listOf(
        PictureMcqItem("android.resource://com.alifba.alifba/" + R.drawable.ic_launcher_background, "Option A", false),
        PictureMcqItem("android.resource://com.alifba.alifba/" + R.drawable.ic_launcher_background, "Option B", true),
        PictureMcqItem("android.resource://com.alifba.alifba/" + R.drawable.ic_launcher_background, "Option C", false),
        PictureMcqItem("android.resource://com.alifba.alifba/" + R.drawable.ic_launcher_background, "Option D", false),
        PictureMcqItem("android.resource://com.alifba.alifba/" + R.drawable.ic_launcher_background, "Option E", false)
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        CommonExplanationText(
            text = "Which option is correct? (5 items: 2+2+1)",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
        )

        CustomPictureGrid(
            items = sample5Items,
            isTablet = false,
            onItemClick = { /* Preview action */ }
        )
    }
}
