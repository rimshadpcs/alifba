package com.alifba.alifba.presenation.lessonScreens

import android.util.Log
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
import androidx.compose.runtime.MutableState
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
    viewModel: LessonScreenViewModel = hiltViewModel(),
    chaptersViewModel: ChaptersViewModel = hiltViewModel()
) {
    val lesson = viewModel.getLessonContentByID(lessonId)
    val showCancelDialog = remember { mutableStateOf(false) }
    val showCompletionDialog = remember { mutableStateOf(false) }

    // State for segment index and accumulated XP
    val currentSegmentIndex = remember { mutableStateOf(0) }
    val accumulatedXp = remember { mutableStateOf(0) }

    val lessons by viewModel.lessons.observeAsState(emptyList())
    val loading by viewModel.loading.observeAsState(false)
    val error by viewModel.error.observeAsState(null)
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.initContext(context) // Pass the context to the ViewModel
        viewModel.loadLessons()
        chaptersViewModel.loadChapters(levelId)
    }

    val nextChapterId = chaptersViewModel.getNextChapterId(lessonId)

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

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

    if (lesson != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column {
                val totalSegments = lesson.segments.size
                val progress = if (totalSegments > 0) currentSegmentIndex.value / totalSegments.toFloat() else 0f

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

                    IconButton(
                        onClick = { showCancelDialog.value = true }
                    ) {
                        CompositionLocalProvider(LocalContentColor provides Color.Unspecified) {
                            Image(
                                painter = painterResource(id = R.drawable.close),
                                contentDescription = "Cancel Session"
                            )
                        }
                    }
                }

                // Handle lesson segments
                when (val currentSegment = lesson.segments[currentSegmentIndex.value]) {
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
                        DisposableEffect(currentSegment) {
                            viewModel.stopAudio()
                            viewModel.startAudio(currentSegment.speech)
                            onDispose { viewModel.stopAudio() }
                        }

                        CommonLessonSegment(
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

                    is LessonSegment.FillInTheBlanks -> {
                        DisposableEffect(currentSegment) {
                            viewModel.stopAudio()
                            // currentSegment.speech might be a URL or local file path
                            viewModel.startAudio(currentSegment.exercise.speech)
                            onDispose { viewModel.stopAudio() }
                        }

                        FillInTheBlanksExerciseScreen(
                            segment = currentSegment,
                            onNextClicked = {
                                viewModel.incrementQuizzesAttended()
                                handleNextSegment(
                                    currentSegmentIndex,
                                    totalSegments,
                                    accumulatedXp,
                                    showCompletionDialog,
                                    currentSegment)
                            }
                        )
                    }

                    is LessonSegment.PictureMcqLesson -> {
                        DisposableEffect(currentSegment) {
                            viewModel.stopAudio()
                            // currentSegment.speech might be a URL or local file path
                            viewModel.startAudio(currentSegment.speech)
                            onDispose { viewModel.stopAudio() }
                        }
                        PictureMcqSegment(
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
                            // currentSegment.speech might be a URL or local file path
//                            viewModel.startAudio(currentSegment.speech)
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

                    is LessonSegment.TextMcqLesson -> {
                        DisposableEffect(currentSegment) {
                            viewModel.stopAudio()
                            // currentSegment.speech might be a URL or local file path
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

                    else -> {}
                }


                // Cancel dialog
                if (showCancelDialog.value) {
                    AlertDialog(
                        onDismissRequest = { showCancelDialog.value = false },
                        title = { Text(text = "Cancel Lessons?") },
                        text = { Text("Are you sure you want to cancel the Lesson?") },
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
                        viewModel.startLocalAudio(R.raw.yay)
                        onDispose {}
                    }

                    LaunchedEffect(showCompletionDialog.value) {
                        if (showCompletionDialog.value) {
                            delay(2000)
                            showCompletionDialog.value = false

                            // (A) Add XP at the end
                            viewModel.updateLessonProgress(
                                lessonId = lessonId,
                                levelId = levelId,
                                chapterId = lesson.id.toString(),
                                earnedXP = accumulatedXp.value
                            )

                            // (B) Mark the chapter as complete in Firestore
                            // In LessonScreen.kt
                            chaptersViewModel.checkAndMarkChapterCompletion(
                                chapterId = lesson.id.toString(),
//                                totalLessons = totalSegments,
                                levelId = levelId,
                                earnedXP = accumulatedXp.value,
                                chapterType = lesson.chapterType
                            )

                            // Then navigate
                            navigateToChapterScreen()
                            navController.navigate("lessonPathScreen/$levelId") {
                                popUpTo("homeScreen") { inclusive = false }
                            }
                        }
                    }

                }
            }
        }
    } else {
        if (!loading) {
            Text("Lesson not found. ID: $lessonId")
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

        // Add XP based on the segment type
        when (currentSegment) {
            is LessonSegment.TextMcqLesson,
            is LessonSegment.PictureMcqLesson,
            is LessonSegment.FillInTheBlanks -> {
                accumulatedXp.value += 5 // Add 5 XP for quizzes
            }
            is LessonSegment.CommonLesson -> {
                accumulatedXp.value += 1 // Add 1 XP for common lessons
            }
            is LessonSegment.LetterTracing->{
                accumulatedXp.value +=10
            }
            is LessonSegment.FlashCardExercise->{
                accumulatedXp.value +=1
            }
            else -> {
                // No XP for other segment types
            }
        }
    } else {
        showDialog.value = true // Mark lesson as completed
    }
}

