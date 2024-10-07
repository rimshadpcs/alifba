package com.alifba.alifba.presenation.lessonScreens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alifba.alifba.R
import com.alifba.alifba.models.LessonScreenViewModel
import com.alifba.alifba.models.LessonSegment
import com.alifba.alifba.ui_components.dialogs.LottieAnimationDialog
import com.alifba.alifba.presenation.lessonPath.LessonPathViewModel
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.CommonLesson.CommonLessonSegment
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.DragAndDropLesson.DragDropLessonScreen
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.LetterTracing
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.pictureMcq.PictureMcqSegment
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.TextMcq.TextMcqSegment
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.fillInTheBlanks.FillInTheBlanksExerciseScreen
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.flashCard.FlashCardLessonSegment
import com.alifba.alifba.presenation.lessonScreens.lessonSegment.flashCard.FlashCardScreen
import com.alifba.alifba.utils.PlayAudio
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LessonScreen(lessonId: Int, navigateToLessonPathScreen: () -> Unit,viewModel: LessonScreenViewModel) {
    val lessonPathViewModel: LessonPathViewModel = viewModel()
    val lesson = viewModel.getLessonContentById(lessonId)
    var currentSegmentIndex by remember { mutableStateOf(0) }
    val showDialog = remember { mutableStateOf(false) }
    var currentCommonLessonSegment: LessonSegment.CommonLesson? by remember { mutableStateOf(null) }

    val introductionLessons by lessonPathViewModel.introductionLessons.observeAsState(initial = emptyList())


    // Show LottieAnimationDialog based on showDialog state
    if (showDialog.value) {
        LottieAnimationDialog(showDialog = showDialog, lottieFileRes = R.raw.celebration)
        // Play audio
        PlayAudio(audioResId = R.raw.yay)
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        if (lesson != null) {
            when (val currentSegment = lesson.segments[currentSegmentIndex]) {


                is LessonSegment.LetterTracing->{
                    DisposableEffect(currentSegment) {
                        viewModel.stopAudio() // Stop any currently playing audio
                        viewModel.startAudio(currentSegment.speech)
                        onDispose {
                            viewModel.stopAudio()
                        }
                    }

                    LetterTracing(segment = currentSegment,
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
                        //viewModel.stopAudio() // Stop any currently playing audio
                        //viewModel.startAudio(currentSegment.speech) // Start new audio
                        onDispose {
                            viewModel.stopAudio()
                        }

                    }
                    FlashCardLessonSegment(segment = currentSegment,
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
                        viewModel.stopAudio() // Stop any currently playing audio
                        viewModel.startAudio(currentSegment.speech) // Start new audio
                        onDispose {
                            viewModel.stopAudio()
                        }

                    }
                    CommonLessonSegment(segment = currentSegment,
                        onNextClicked = {
                            if (currentSegmentIndex < lesson.segments.size - 1) {
                                currentSegmentIndex++
                            } else {
                                showDialog.value = true
                            }
                        }
                    )
                }




                is LessonSegment.PictureMcqLesson->{
                    DisposableEffect(currentSegment) {
                        viewModel.stopAudio() // Stop any currently playing audio
                        //viewModel.startAudio(currentSegment.speech) // Start new audio
                        onDispose {
                            viewModel.stopAudio()
                        }
                    }

                    PictureMcqSegment(segment = currentSegment,onNextClicked = {
                        if (currentSegmentIndex < lesson.segments.size - 1) {
                            currentSegmentIndex++
                        } else {
                            showDialog.value = true
                        }
                    }
                    )
                }


                is LessonSegment.DragAndDropExperiment->{
                    DisposableEffect(currentSegment) {
                        viewModel.stopAudio() // Stop any currently playing audio
                        //viewModel.startAudio(currentSegment.speech) // Start new audio
                        onDispose {
                            viewModel.stopAudio()
                        }
                    }

                    DragDropLessonScreen(segment = currentSegment,onNextClicked = {
                        if (currentSegmentIndex < lesson.segments.size - 1) {
                            currentSegmentIndex++
                        } else {
                            showDialog.value = true
                        }
                    }
                    )
                }
                is LessonSegment.FillInTheBlanks->{
                    DisposableEffect(currentSegment) {
                        viewModel.stopAudio() // Stop any currently playing audio
                        //viewModel.startAudio(currentSegment.speech) // Start new audio
                        onDispose {
                            viewModel.stopAudio()
                        }
                    }


                    FillInTheBlanksExerciseScreen(segment = currentSegment,onNextClicked = {
                        if (currentSegmentIndex < lesson.segments.size - 1) {
                            currentSegmentIndex++
                        } else {
                            showDialog.value = true
                        }
                    }
                    )
                }

                is LessonSegment.TextMcqLessonItem -> {
                    DisposableEffect(currentSegment) {
                        viewModel.stopAudio() // Stop any currently playing audio
                        viewModel.startAudio(currentSegment.speech) // Start new audio
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
            println("Current CommonLesson Segment: $currentCommonLessonSegment")
            //println("Current Segment: $currentSegment")


            if (showDialog.value) {
                lessonPathViewModel.completeLesson(lessonId)
                LaunchedEffect(key1 = showDialog) {
                    delay(2000)
                    showDialog.value = false
                    navigateToLessonPathScreen()
                }

            }

        } else

        {
            Text("Lesson not found. ID: $lessonId") // Display the ID for debugging
        }
    }

}

