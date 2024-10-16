package com.alifba.alifba.presenation.lessonScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavController
import com.alifba.alifba.R
import com.alifba.alifba.data.models.LessonSegment
import com.alifba.alifba.ui_components.dialogs.LottieAnimationDialog
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.CommonLesson.CommonLessonSegment
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.DragAndDropLesson.DragDropLessonScreen
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.LetterTracing
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.pictureMcq.PictureMcqSegment
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.TextMcq.TextMcqSegment
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.fillInTheBlanks.FillInTheBlanksExerciseScreen
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.flashCard.FlashCardLessonSegment
import com.alifba.alifba.utils.PlayAudio
import kotlinx.coroutines.delay

@Composable
fun LessonScreen(lessonId: Int, levelId: String, // Add levelId
                 navController: NavController, navigateToChapterScreen: () -> Unit, viewModel: LessonScreenViewModel) {
    val lesson = viewModel.getLessonContentByID(lessonId) // Use the ViewModel function to fetch lesson by ID
    var currentSegmentIndex by remember { mutableStateOf(0) }
    val showDialog = remember { mutableStateOf(false) }

    val lessons by viewModel.lessons.observeAsState(emptyList())
    val loading by viewModel.loading.observeAsState(false)
    val error by viewModel.error.observeAsState(null)

    // Fetch lessons when the screen is displayed
    LaunchedEffect(Unit) {
        viewModel.loadLessons() // Load lessons from Firestore
    }


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

            // Show Lottie animation and play audio when the lesson is completed
            if (showDialog.value) {
                LottieAnimationDialog(showDialog = showDialog, lottieFileRes = R.raw.celebration)
                // Play audio
                PlayAudio(audioResId = R.raw.yay)

                //lessonPathViewModel.completeLesson(lessonId)
                LaunchedEffect(key1 = showDialog) {
                    delay(2000)
                    showDialog.value = false
                    navigateToChapterScreen()
                }
                navController.navigate("lessonPathScreen/$levelId") {
                    popUpTo("homeScreen") {
                        inclusive = false
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
