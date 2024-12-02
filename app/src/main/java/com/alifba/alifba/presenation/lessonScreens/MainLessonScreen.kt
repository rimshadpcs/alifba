package com.alifba.alifba.presenation.lessonScreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alifba.alifba.R
import com.alifba.alifba.data.models.LessonSegment
import com.alifba.alifba.presenation.chapters.ChaptersViewModel
import com.alifba.alifba.ui_components.dialogs.LottieAnimationDialog
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.CommonLesson.CommonLessonSegment
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.DragAndDropLesson.DragDropLessonScreen
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.LetterTracing
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.pictureMcq.PictureMcqSegment
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.TextMcq.TextMcqSegment
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.fillInTheBlanks.FillInTheBlanksExerciseScreen
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.flashCard.FlashCardLessonSegment
import com.alifba.alifba.ui_components.theme.lightPurple
import com.alifba.alifba.ui_components.theme.mediumpurple
import com.alifba.alifba.ui_components.widgets.StripedProgressIndicator
import kotlinx.coroutines.delay

@Composable
fun LessonScreen(
    lessonId: Int,
    levelId: String,
    navController: NavController,
    navigateToChapterScreen: () -> Unit,
    viewModel: LessonScreenViewModel = hiltViewModel(), // Ensure Hilt provides the ViewModel
    chaptersViewModel: ChaptersViewModel = hiltViewModel() // Inject ChaptersViewModel
) {
    val lesson = viewModel.getLessonContentByID(lessonId)
    var currentSegmentIndex by remember { mutableStateOf(0) }
    val showDialog = remember { mutableStateOf(false) }

    val lessons by viewModel.lessons.observeAsState(emptyList())
    val loading by viewModel.loading.observeAsState(false)
    val error by viewModel.error.observeAsState(null)

    // Fetch lessons and load chapters when the screen is displayed
    LaunchedEffect(Unit) {
        viewModel.loadLessons()
        chaptersViewModel.loadChapters(levelId)
    }

    val nextChapterId = chaptersViewModel.getNextChapterId(lessonId)

    // Show loading spinner while fetching lessons
    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    // Handle error state, showing retry option
    error?.let {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = it, color = Color.Red)
                Button(onClick = { viewModel.loadLessons() }) {
                    Text("Retry")
                }
            }
        }
        return
    }

    // Show lesson content once loaded
    if (lesson != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column {
                var showCancelDialog by remember { mutableStateOf(false) }
                val totalSegments = lesson?.segments?.size ?: 1
                val progress = if (totalSegments > 0) currentSegmentIndex / totalSegments.toFloat() else 0f
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Progress Indicator with weight
                    StripedProgressIndicator(
                        modifier = Modifier.weight(1f),
                        progress = progress,
                        stripeColorSecondary = lightPurple,
                        backgroundColor = Color.LightGray,
                        stripeColor = mediumpurple
                    )

                    // "X" Icon Button
                    IconButton(
                        onClick = {
                            showCancelDialog = true
                        }
                    ) {
                        CompositionLocalProvider(LocalContentColor provides Color.Unspecified) {
                            Image(
                                painter = painterResource(id = R.drawable.close),
                                contentDescription = "Cancel Session",
                            )
                        }
                    }
                }
                // Handle the segments of the lesson
                when (val currentSegment = lesson.segments[currentSegmentIndex]) {
                    is LessonSegment.LetterTracing -> {
                        DisposableEffect(currentSegment) {
                            viewModel.stopAudio() // Stop any currently playing audio
                            viewModel.startAudio(currentSegment.speech.toString())
                            onDispose {
                                viewModel.stopAudio()
                            }
                        }

                        LetterTracing(
                            segment = currentSegment,
                            onNextClicked = {
                                if (currentSegmentIndex < lesson.segments.size - 1) {
                                    currentSegmentIndex++
                                } else {
                                    showDialog.value = true
                                }
                            })
                    }

                    is LessonSegment.FlashCardExercise -> {
                        DisposableEffect(currentSegment) {
                            onDispose {
                                viewModel.stopAudio()
                            }
                        }

                        FlashCardLessonSegment(
                            segment = currentSegment,
                            onNextClicked = {
                                if (currentSegmentIndex < lesson.segments.size - 1) {
                                    currentSegmentIndex++
                                } else {
                                    showDialog.value = true
                                }
                            }
                        )
                    }

                    is LessonSegment.CommonLesson -> {
                        DisposableEffect(currentSegment) {
                            viewModel.stopAudio()
                            viewModel.startAudio(currentSegment.speech)
                            onDispose {
                                viewModel.stopAudio()
                            }
                        }

                        CommonLessonSegment(
                            segment = currentSegment,
                            onNextClicked = {
                                if (currentSegmentIndex < lesson.segments.size - 1) {
                                    currentSegmentIndex++
                                } else {
                                    showDialog.value = true
                                }
                            }
                        )
                    }

                    is LessonSegment.FillInTheBlanks -> {
                        DisposableEffect(currentSegment) {
                            viewModel.stopAudio()
                            viewModel.startAudio(currentSegment.exercise.speech)
                            onDispose {
                                viewModel.stopAudio()
                            }
                        }

                        FillInTheBlanksExerciseScreen(
                            segment = currentSegment,
                            onNextClicked = {
                                if (currentSegmentIndex < lesson.segments.size - 1) {
                                    currentSegmentIndex++
                                } else {
                                    showDialog.value = true
                                }
                            }
                        )
                    }

                    is LessonSegment.PictureMcqLesson -> {
                        DisposableEffect(currentSegment) {
                            viewModel.stopAudio()
                            viewModel.startAudio(currentSegment.speech)
                            onDispose {
                                viewModel.stopAudio()
                            }
                        }

                        PictureMcqSegment(
                            segment = currentSegment,
                            onNextClicked = {
                                if (currentSegmentIndex < lesson.segments.size - 1) {
                                    currentSegmentIndex++
                                } else {
                                    showDialog.value = true
                                }
                            }
                        )
                    }

                    is LessonSegment.DragAndDropExperiment -> {
                        DisposableEffect(currentSegment) {
                            onDispose {
                                viewModel.stopAudio()
                            }
                        }

                        DragDropLessonScreen(
                            segment = currentSegment,
                            onNextClicked = {
                                if (currentSegmentIndex < lesson.segments.size - 1) {
                                    currentSegmentIndex++
                                } else {
                                    showDialog.value = true
                                }
                            }
                        )
                    }

                    is LessonSegment.TextMcqLesson -> {
                        DisposableEffect(currentSegment) {
                            viewModel.stopAudio()
                            viewModel.startAudio(currentSegment.speech)
                            onDispose {
                                viewModel.stopAudio()
                            }
                        }

                        TextMcqSegment(
                            segment = currentSegment,
                            onNextClicked = {
                                if (currentSegmentIndex < lesson.segments.size - 1) {
                                    currentSegmentIndex++
                                } else {
                                    showDialog.value = true
                                }
                            }
                        )
                    }

                    else -> {}
                }
                if (showCancelDialog) {
                    AlertDialog(
                        onDismissRequest = { showCancelDialog = false },
                        title = { Text(text = "Cancel Lessons?") },
                        text = { Text("Are you sure you want to cancel the Lesson?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showCancelDialog = false
                                    // Handle session cancellation
                                    // For example, navigate back to the chapter screen
                                    navigateToChapterScreen()
                                    navController.navigate("lessonPathScreen/$levelId") {
                                        popUpTo("homeScreen") {
                                            inclusive = false
                                        }
                                    }
                                }
                            ) {
                                Text("Yes")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showCancelDialog = false }
                            ) {
                                Text("No")
                            }
                        }
                    )
                }


                if (showDialog.value) {
                    val context = LocalContext.current
                    // Display the completion dialog and play celebratory audio
                    LottieAnimationDialog(
                        showDialog = showDialog,
                        lottieFileRes = R.raw.celebration
                    )

                    // Play local celebratory audio
                    DisposableEffect(showDialog.value) {
                        viewModel.startLocalAudio(R.raw.yay)  // Play the "yay" sound

                        onDispose { } // Empty onDispose; only to handle DisposableEffect
                    }

                    // Ensure delay before navigating to allow animation and audio to finish
                    LaunchedEffect(showDialog.value) {
                        delay(2000)  // Ensure this matches your audio length if possible
                        showDialog.value = false
                        viewModel.markLessonCompleted(lessonId, nextLessonId = nextChapterId)
                        navigateToChapterScreen()

                        navController.navigate("lessonPathScreen/$levelId") {
                            popUpTo("homeScreen") {
                                inclusive = false
                            }
                        }
                    }
                }
            }
        }
    } else {
        if (!loading) {
            Text("Lesson not found. ID: $lessonId") // Display the ID for debugging
        }
    }
}
