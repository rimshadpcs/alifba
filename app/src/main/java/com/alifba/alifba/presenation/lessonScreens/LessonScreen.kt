package com.alifba.alifba.presenation.lessonScreens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alifba.alifba.R
import com.alifba.alifba.data.models.Lesson
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
    viewModel: LessonScreenViewModel = hiltViewModel(),
    chaptersViewModel: ChaptersViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isLessonsLoaded = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!isLessonsLoaded.value) {
            Log.d("LessonScreen", "Loading lessons for level: $levelId")
            viewModel.initContext(context)
            viewModel.loadLessons(levelId)
            chaptersViewModel.loadChapters(levelId)
            isLessonsLoaded.value = true
        }
    }

    val isLoading by viewModel.loading.observeAsState(false)
    val errorMessage by viewModel.error.observeAsState()
    val lesson = viewModel.getLessonContentByID(lessonId)

    if (isLoading) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    errorMessage?.let {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = it, color = Color.Red)
                Button(onClick = { viewModel.loadLessons(levelId) }) {
                    Text("Retry")
                }
            }
        }
        return
    }

    if (lesson == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text("Lesson not found. ID: $lessonId")
        }
        return
    }

    LessonContent(
        lesson = lesson,
        lessonId = lessonId,
        levelId = levelId,
        navController = navController,
        viewModel = viewModel,
        chaptersViewModel = chaptersViewModel,
        navigateToChapterScreen = navigateToChapterScreen
    )
}

@Composable
fun LessonContent(
    lesson: Lesson,
    lessonId: Int,
    levelId: String,
    navController: NavController,
    viewModel: LessonScreenViewModel,
    chaptersViewModel: ChaptersViewModel,
    navigateToChapterScreen: () -> Unit
) {
    val showCancelDialog = remember { mutableStateOf(false) }
    val showCompletionDialog = remember { mutableStateOf(false) }
    val currentSegmentIndex = remember { mutableStateOf(0) }
    val accumulatedXp = remember { mutableStateOf(0) }
    val isAudioCompleted by viewModel.isAudioCompleted.observeAsState(false)

    val totalSegments = lesson.segments.size
    val progress = if (totalSegments > 0) {
        currentSegmentIndex.value / totalSegments.toFloat()
    } else 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StripedProgressIndicator(
                    modifier = Modifier.weight(1f),
                    progress = progress,
                    stripeColorSecondary = lightPurple,
                    backgroundColor = Color.LightGray,
                    stripeColor = mediumpurple
                )
                IconButton(onClick = { showCancelDialog.value = true }) {
                    Image(
                        painter = painterResource(id = R.drawable.close),
                        contentDescription = "Cancel Session"
                    )
                }
            }

            // Force a fresh sub-composition whenever currentSegmentIndex changes
            key(currentSegmentIndex.value) {
                // Render the correct segment
                val currentSegment = lesson.segments[currentSegmentIndex.value]
                when (currentSegment) {

                    is LessonSegment.LetterTracing -> {
                        DisposableEffect(currentSegment) {
                            viewModel.stopAudio()
                            viewModel.startAudio(currentSegment.speech.toString())
                            onDispose { viewModel.stopAudio() }
                        }

                        LetterTracing(
                            segment = currentSegment,
                            onNextClicked = {
                                handleNextSegment(
                                    currentSegmentIndex,
                                    totalSegments,
                                    accumulatedXp,
                                    showCompletionDialog,
                                    currentSegment
                                )
                            }
                        )
                    }

                    is LessonSegment.FlashCardExercise -> {
                        FlashCardLessonSegment(
                            segment = currentSegment,
                            onNextClicked = {
                                handleNextSegment(
                                    currentSegmentIndex,
                                    totalSegments,
                                    accumulatedXp,
                                    showCompletionDialog,
                                    currentSegment
                                )
                            }
                        )
                    }

                    is LessonSegment.CommonLesson -> {
                        // Optionally handle audio for CommonLesson
                        DisposableEffect(currentSegment) {
                            viewModel.stopAudio()
                            viewModel.startAudio(currentSegment.speech)
                            onDispose { viewModel.stopAudio() }
                        }

                        CommonLessonSegment(
                            segment = currentSegment,
                            showNextButton = isAudioCompleted, // if you want gating by audio
                            onNextClicked = {
                                handleNextSegment(
                                    currentSegmentIndex,
                                    totalSegments,
                                    accumulatedXp,
                                    showCompletionDialog,
                                    currentSegment
                                )
                            }
                        )
                    }

                    is LessonSegment.FillInTheBlanks -> {
                        DisposableEffect(currentSegment) {
                            viewModel.stopAudio()
                            viewModel.startAudio(currentSegment.exercise.speech)
                            onDispose { viewModel.stopAudio() }
                        }

                        FillInTheBlanksExerciseScreen(
                            segment = currentSegment,
                            showNextButton = isAudioCompleted, // if you want audio gating
                            onNextClicked = {
                                viewModel.incrementQuizzesAttended()
                                handleNextSegment(
                                    currentSegmentIndex,
                                    totalSegments,
                                    accumulatedXp,
                                    showCompletionDialog,
                                    currentSegment
                                )
                            }
                        )
                    }

                    is LessonSegment.PictureMcqLesson -> {
                        DisposableEffect(currentSegment) {
                            viewModel.stopAudio()
                            viewModel.startAudio(currentSegment.speech)
                            onDispose { viewModel.stopAudio() }
                        }
                        PictureMcqSegment(
                            segment = currentSegment,
                            showNextButton = isAudioCompleted, // if you want audio gating
                            onNextClicked = {
                                viewModel.incrementQuizzesAttended()
                                handleNextSegment(
                                    currentSegmentIndex,
                                    totalSegments,
                                    accumulatedXp,
                                    showCompletionDialog,
                                    currentSegment
                                )
                            }
                        )
                    }

                    is LessonSegment.TextMcqLesson -> {
                        DisposableEffect(currentSegment) {
                            viewModel.stopAudio()
                            viewModel.startAudio(currentSegment.speech)
                            onDispose { viewModel.stopAudio() }
                        }
                        TextMcqSegment(
                            segment = currentSegment,
                            onNextClicked = {
                                viewModel.incrementQuizzesAttended()
                                handleNextSegment(
                                    currentSegmentIndex,
                                    totalSegments,
                                    accumulatedXp,
                                    showCompletionDialog,
                                    currentSegment
                                )
                            }
                        )
                    }

                    is LessonSegment.DragAndDropExperiment -> {
                        DisposableEffect(currentSegment) {
                            viewModel.stopAudio()
                            // If needed: viewModel.startAudio(currentSegment.speech)
                            onDispose { viewModel.stopAudio() }
                        }
                        DragDropLessonScreen(
                            segment = currentSegment,
                            onNextClicked = {
                                viewModel.incrementQuizzesAttended()
                                handleNextSegment(
                                    currentSegmentIndex,
                                    totalSegments,
                                    accumulatedXp,
                                    showCompletionDialog,
                                    currentSegment
                                )
                            }
                        )
                    }

                    else -> {}
                }
            }

            // Cancel (Exit) dialog
            if (showCancelDialog.value) {
                AlertDialog(
                    onDismissRequest = { showCancelDialog.value = false },
                    title = { Text(text = "Cancel Lesson?") },
                    text = { Text("Are you sure you want to cancel this lesson?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showCancelDialog.value = false
                            navigateToChapterScreen()
                            navController.navigate("lessonPathScreen/$levelId") {
                                popUpTo("homeScreen") { inclusive = false }
                            }
                        }) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCancelDialog.value = false }) {
                            Text("No")
                        }
                    }
                )
            }

            // Completion dialog
            if (showCompletionDialog.value) {
                LottieAnimationDialog(
                    showDialog = showCompletionDialog,
                    lottieFileRes = R.raw.celebration,
                )

                DisposableEffect(showCompletionDialog.value) {
                    // Local "yay" audio if wanted
                    viewModel.startLocalAudio(R.raw.yay)
                    onDispose {}
                }

                // Delay for the Lottie animation, then do completion logic
                LaunchedEffect(showCompletionDialog.value) {
                    if (showCompletionDialog.value) {
                        delay(2000)
                        showCompletionDialog.value = false

                        // Add XP at the end
                        viewModel.updateLessonProgress(
                            lessonId = lessonId,
                            levelId = levelId,
                            chapterId = lesson.id.toString(),
                            earnedXP = accumulatedXp.value
                        )

                        // Mark the chapter as complete in Firestore
                        chaptersViewModel.checkAndMarkChapterCompletion(
                            chapterId = lesson.id.toString(),
                            levelId = levelId,
                            earnedXP = accumulatedXp.value,
                            chapterType = lesson.chapterType
                        )

                        // Then navigate back
                        navigateToChapterScreen()
                        navController.navigate("lessonPathScreen/$levelId") {
                            popUpTo("homeScreen") { inclusive = false }
                        }
                    }
                }
            }
        }
    }
}

private fun handleNextSegment(
    currentSegmentIndex: MutableState<Int>,
    totalSegments: Int,
    accumulatedXp: MutableState<Int>,
    showDialog: MutableState<Boolean>,
    currentSegment: LessonSegment
) {
    if (currentSegmentIndex.value < totalSegments - 1) {
        currentSegmentIndex.value++

        // Add XP based on segment type
        when (currentSegment) {
            is LessonSegment.TextMcqLesson,-> accumulatedXp.value += 5
            is LessonSegment.PictureMcqLesson,-> accumulatedXp.value += 5
            is LessonSegment.FillInTheBlanks -> accumulatedXp.value += 5
            is LessonSegment.CommonLesson -> accumulatedXp.value += 1
            is LessonSegment.LetterTracing -> accumulatedXp.value += 10
            is LessonSegment.FlashCardExercise -> accumulatedXp.value += 1
            else -> {}
        }
    } else {
        showDialog.value = true
    }
}
