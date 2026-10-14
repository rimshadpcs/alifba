package com.alifba.alifba.presenation.onboarding

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.alifba.alifba.R
import com.alifba.alifba.data.models.Story
import com.alifba.alifba.presenation.home.layout.settings.SettingsDivider
import com.alifba.alifba.presenation.home.layout.settings.SettingsGroup
import com.alifba.alifba.presenation.stories.AudioPlayerViewModel
import com.alifba.alifba.service.LessonReminderReceiver
import com.alifba.alifba.ui_components.widgets.AnswerCard
import com.alifba.alifba.ui_components.widgets.AnswerState
import com.alifba.alifba.ui_components.widgets.AuthTextField
import com.alifba.alifba.ui_components.widgets.AuthTheme
import com.alifba.alifba.ui_components.widgets.DotLottieView
import com.alifba.alifba.ui_components.widgets.LessonButtonKind
import com.alifba.alifba.ui_components.widgets.LessonCircleButton
import com.alifba.alifba.ui_components.widgets.LessonPrimaryButton
import com.alifba.alifba.ui_components.widgets.LessonTheme
import com.alifba.alifba.ui_components.widgets.MoonProgressBar
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager
import com.alifba.alifba.utils.ReminderPreferences
import kotlinx.coroutines.delay

enum class OnboardingQuestionType {
    PictureMcq,
    TextMcq,
    FillBlank
}

data class OnboardingQuestion(
    val id: String,
    val type: OnboardingQuestionType,
    val speech: String? = null,
    val prompt: String? = null,
    val options: List<String>,
    val correctAnswer: String,
    val valueBanner: String,
    val showAudioHint: Boolean = false,
    val pictureResources: List<Int> = emptyList()
)

data class HabitTimeOption(
    val label: String,
    val hour: Int,
    val minute: Int
)

data class Testimonial(val quote: String, val author: String)

// title doubles as the stored selection key (OnboardingUiState.selectedMotivations), so keep
// the wording stable even if the icon or description changes.
data class MotivationOption(val title: String, val description: String, val emoji: String)

object OnboardingCopy {
    const val lastStepIndex = 17

    // Quiz questions sit at these two non-contiguous stepIndex ranges (screens 5-7 and 10-11
    // in the 18-screen flow), with 3 new non-quiz screens — Busy Families, Bedtime, and the
    // Sarah K. testimonial — inserted between them. Keyed explicitly rather than computed by
    // arithmetic (as the old contiguous 2..6 range was) since the ranges no longer line up.
    val quizStepToQuestionIndex: Map<Int, Int> = mapOf(
        4 to 0, 5 to 1, 6 to 2,
        9 to 3, 10 to 4
    )

    const val bedtimeHadith = "The Prophet ﷺ said: 'Whoever recites the last two verses of " +
        "Surat al-Baqarah at night, they will be sufficient for him.'"
    const val bedtimeHadithAttribution = "Sahih al-Bukhari 5009, Sahih Muslim 807"

    val testimonialSarah = Testimonial(
        quote = "My kids actually ask for their \"Alifba time\" before bed. The audio stories " +
            "about the Prophets are their favourite.",
        author = "Sarah K., parent of two"
    )

    val testimonialAisha = Testimonial(
        quote = "The quizzes are actually fun. Like, my daughter doesn't even realize she's " +
            "being tested. She just wants to earn the next badge.",
        author = "Aisha M."
    )

    val quoteScreenAttribution = "Qur'an 31:17"
    const val quoteScreenVerse = "O my dear son! Establish prayer, encourage what is good and " +
        "forbid what is evil, and endure patiently whatever befalls you. Surely this is a " +
        "resolve to aspire to."

    val motivationOptions: List<MotivationOption> = listOf(
        MotivationOption(
            title = "Reduce passive screen time",
            description = "Swap mindless scrolling for learning",
            emoji = "📱"
        ),
        MotivationOption(
            title = "Build a daily Islamic routine",
            description = "Make a little learning part of every day",
            emoji = "🌙"
        ),
        MotivationOption(
            title = "Teach Prophets' stories",
            description = "Audio stories they'll remember",
            emoji = "📖"
        ),
        MotivationOption(
            title = "Build better Islamic habits",
            description = "Kindness, gratitude and good manners",
            emoji = "🌱"
        ),
        MotivationOption(
            title = "All of the above",
            description = "Show me everything Alifba offers",
            emoji = "✨"
        )
    )

    val questions: List<OnboardingQuestion> = listOf(
        OnboardingQuestion(
            id = "yunus_whale",
            type = OnboardingQuestionType.PictureMcq,
            speech = "What swallowed Prophet Yunus (AS)?",
            options = listOf("Whale", "Snake", "Elephant", "Camel"),
            correctAnswer = "Whale",
            valueBanner = "Visual learning helps kids remember Islamic concepts more easily.",
            pictureResources = listOf(
                R.drawable.whale,
                R.drawable.snake,
                R.drawable.elephant,
                R.drawable.camel
            )
        ),
        OnboardingQuestion(
            id = "nuh_ark",
            type = OnboardingQuestionType.TextMcq,
            speech = "Which Prophet was commanded to build the Ark?",
            options = listOf("Musa (AS)", "Nuh (AS)", "Isa (AS)"),
            correctAnswer = "Nuh (AS)",
            valueBanner = "Kids learn through memorable Islamic audio stories.",
            showAudioHint = true
        ),
        OnboardingQuestion(
            id = "thankful",
            type = OnboardingQuestionType.TextMcq,
            speech = "What do we say when we feel thankful to Allah?",
            options = listOf("Alhamdulillah", "SubhanAllah", "Allahu Akbar"),
            correctAnswer = "Alhamdulillah",
            valueBanner = "Bite-sized real-life scenarios help kids apply Islam every day."
        ),
        OnboardingQuestion(
            id = "eid_adha",
            type = OnboardingQuestionType.FillBlank,
            speech = "Complete the sentence!",
            prompt = "We celebrate Eid ul-Adha after Hajj in the month of [ ______ ].",
            options = listOf("Ramadan", "Dhul Hijjah", "Muharram"),
            correctAnswer = "Dhul Hijjah",
            valueBanner = "Interactive activities keep little learners focused and engaged."
        ),
        OnboardingQuestion(
            id = "share_snack",
            type = OnboardingQuestionType.FillBlank,
            prompt = "If a friend drops their snack, a good Muslim will [ ______ ] theirs with them.",
            options = listOf("Laugh at", "Ignore", "Share"),
            correctAnswer = "Share",
            valueBanner = "Alifba helps build kindness, gratitude, and good manners step by step."
        )
    )

    val loadingMessages: List<String> = listOf(
        "Personalising bite-sized Islamic lessons...",
        "Preparing audio stories they'll remember...",
        "Choosing quizzes that make learning fun...",
        "Setting up a gentle daily habit...",
        "Finding lessons that match your goals...",
        "Adding Prophet stories to the path...",
        "Preparing badges and achievements to celebrate progress...",
        "Making the first week easy to start..."
    )

    val habitOptions: List<HabitTimeOption> = listOf(
        HabitTimeOption("After School - 6:00 PM", 18, 0),
        HabitTimeOption("Before Bedtime - 9:00 PM", 21, 0),
        HabitTimeOption("Custom Time", 18, 30)
    )
}

@Composable
fun OnboardingScreen(
    onComplete: (childName: String) -> Unit,
    onExit: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
    audioPlayerViewModel: AudioPlayerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val currentStory by audioPlayerViewModel.currentStory.collectAsState()
    val isAudioPlaying by audioPlayerViewModel.isPlaying.collectAsState()
    val isAudioLoading by audioPlayerViewModel.isLoading.collectAsState()
    val audioError by audioPlayerViewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        // TODO analytics: onboarding_started
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Alternates per screen (even stepIndex -> clouds, odd -> grass hill), cross-fading so the
        // change reads as a soft transition. Matches iOS's OnboardingView.
        Crossfade(
            targetState = state.stepIndex % 2 == 0,
            animationSpec = tween(durationMillis = 350),
            label = "onboardingBackground"
        ) { clouds ->
            Image(
                painter = painterResource(
                    id = if (clouds) R.drawable.onboardingbgone else R.drawable.onboardingbgtwo
                ),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Top and sides only: each screen handles the bottom inset (and the keyboard) itself so
                // its pinned button and the feedback sheet can reach the screen edge.
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                )
        ) {
            val quizQuestionIndex = OnboardingCopy.quizStepToQuestionIndex[state.stepIndex]
            // Round back button, the Moon riding the bar, and "N of 18" on the right.
            OnboardingTopBar(
                current = state.stepIndex + 1,
                total = OnboardingCopy.lastStepIndex + 1,
                onBack = {
                    val handled = viewModel.previousStep()
                    if (!handled) {
                        onExit()
                    }
                }
            )
            Box(modifier = Modifier.weight(1f)) {
                when {
                    state.stepIndex == 0 -> ChildNameOnboardingScreen(
                        childName = state.childName,
                        onChildNameChange = viewModel::setChildName,
                        onContinue = viewModel::nextStep
                    )
                    state.stepIndex == 1 -> MotivationOnboardingScreen(
                        selectedMotivations = state.selectedMotivations,
                        onMotivationSelected = viewModel::toggleMotivation,
                        onContinue = viewModel::nextStep
                    )
                    state.stepIndex == 2 -> BusyFamiliesScreen(
                        childName = state.childName,
                        onContinue = viewModel::nextStep
                    )
                    state.stepIndex == 3 -> ParentChallengeIntroScreen(
                        childName = state.childName,
                        onTakeChallenge = viewModel::nextStep
                    )
                    quizQuestionIndex != null -> {
                        val question = OnboardingCopy.questions[quizQuestionIndex]
                        if (question.showAudioHint) {
                            LaunchedEffect(question.id) {
                                viewModel.loadAudioPreviewStory()
                            }
                        }
                        OnboardingQuizScreen(
                            question = question,
                            questionNumber = quizQuestionIndex + 1,
                            childName = state.childName,
                            audioPreviewStory = state.audioPreviewStory,
                            isAudioPreviewLoading = state.isAudioPreviewLoading ||
                                (isAudioLoading && currentStory?.documentId == state.audioPreviewStory?.documentId),
                            isAudioPreviewPlaying = isAudioPlaying && currentStory?.documentId == state.audioPreviewStory?.documentId,
                            audioPreviewError = state.audioPreviewError ?: audioError,
                            onAudioPreviewClick = { story ->
                                if (currentStory?.documentId == story.documentId) {
                                    if (isAudioPlaying) {
                                        audioPlayerViewModel.pause()
                                    } else {
                                        audioPlayerViewModel.play()
                                    }
                                } else {
                                    audioPlayerViewModel.loadStory(story, autoPlay = true)
                                }
                            },
                            onAnswered = { isCorrect ->
                                viewModel.answerQuestion(question.id, isCorrect)
                            },
                            onNext = {
                                if (state.stepIndex == 10) {
                                    // TODO analytics: onboarding_challenge_completed
                                }
                                viewModel.nextStep()
                            }
                        )
                    }
                    state.stepIndex == 7 -> BedtimeScreen(
                        childName = state.childName,
                        onContinue = viewModel::nextStep
                    )
                    state.stepIndex == 8 -> TestimonialScreen(
                        testimonial = OnboardingCopy.testimonialSarah,
                        onContinue = viewModel::nextStep
                    )
                    state.stepIndex == 11 -> TestimonialScreen(
                        testimonial = OnboardingCopy.testimonialAisha,
                        onContinue = viewModel::nextStep
                    )
                    state.stepIndex == 12 -> OnboardingLoadingScreen(
                        childName = state.childName,
                        onFinished = viewModel::nextStep
                    )
                    state.stepIndex == 13 -> OnboardingResultsScreen(
                        childName = state.childName,
                        score = state.score,
                        onContinue = viewModel::nextStep
                    )
                    state.stepIndex == 14 -> ThanksForSharingScreen(
                        childName = state.childName,
                        onContinue = viewModel::nextStep
                    )
                    state.stepIndex == 15 -> QuoteScreen(
                        childName = state.childName,
                        onContinue = viewModel::nextStep
                    )
                    state.stepIndex == 16 -> HabitBuilderScreen(
                        childName = state.childName,
                        selectedHabitTime = state.selectedHabitTime,
                        selectedReminderHour = state.selectedReminderHour,
                        selectedReminderMinute = state.selectedReminderMinute,
                        onHabitSelected = viewModel::selectHabitTime,
                        onContinue = viewModel::nextStep
                    )
                    state.stepIndex == 17 -> NotificationPrePromptScreen(
                        childName = state.childName,
                        reminderHour = state.selectedReminderHour,
                        reminderMinute = state.selectedReminderMinute,
                        onRemindMe = {
                            // TODO analytics: onboarding_completed
                            onComplete(state.childName)
                        },
                        onSkip = {
                            // No permission prompt and no reminder scheduled
                            onComplete(state.childName)
                        }
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Shell: top bar, scaffold, shared pieces
// ---------------------------------------------------------------------------------------------

@Composable
private fun OnboardingTopBar(
    current: Int,
    total: Int,
    onBack: () -> Unit
) {
    val k = LessonTheme.scale()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LessonCircleButton(
            iconRes = R.drawable.backarrow,
            contentDescription = "Back",
            onClick = onBack
        )
        MoonProgressBar(
            progress = current.toFloat() / total.coerceAtLeast(1),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$current of $total",
            style = LessonTheme.answer().copy(fontSize = (17 * k).sp)
        )
    }
}

/**
 * Scrolling content with the primary action pinned underneath (above the nav bar / keyboard).
 * [centered] vertically centres the content when it is shorter than the screen.
 */
@Composable
private fun OnboardingScaffold(
    centered: Boolean = false,
    bottom: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val isTablet = maxWidth > 600.dp
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .widthIn(max = if (isTablet) 720.dp else 560.dp)
                .padding(horizontal = LessonTheme.margin)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val minHeight = maxHeight
                val focusManager = LocalFocusManager.current
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = minHeight)
                        .verticalScroll(rememberScrollState())
                        // Tapping empty space drops the keyboard
                        .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = if (centered) Arrangement.Center else Arrangement.Top,
                    content = content
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                    .padding(top = 8.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = bottom
            )
        }
    }
}

/** White card with a 2dp border and a raised bottom edge */
@Composable
private fun RaisedPanel(
    modifier: Modifier = Modifier,
    radius: Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(radius)
    val edge = LessonTheme.answerEdge
    Box(modifier = modifier.padding(bottom = edge)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = edge)
                    .background(LessonTheme.lineEdge, shape)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape)
                    .border(2.dp, LessonTheme.line, shape),
                content = content
            )
        }
    }
}

/** Padded raised card used for lists, quotes and the like */
@Composable
private fun OnboardingCard(
    modifier: Modifier = Modifier,
    padding: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    RaisedPanel(modifier = modifier.fillMaxWidth(), radius = LessonTheme.radius + 4.dp) {
        Column(modifier = Modifier.padding(padding), content = content)
    }
}

/** The Moon (animated) beside a white speech bubble with a small tail */
@Composable
private fun MascotSpeech(
    mascotName: String?,
    text: String,
    compact: Boolean = false
) {
    val k = LessonTheme.scale()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (mascotName != null) {
            DotLottieView(
                name = mascotName,
                modifier = Modifier.size(((if (compact) 78 else 100) * k).dp)
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            RaisedPanel(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = text,
                    style = LessonTheme.question().copy(
                        fontSize = ((if (compact) 22 else 26) * k).sp,
                        textAlign = TextAlign.Start
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)
                )
            }
            if (mascotName != null) {
                BubbleTail(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = (-11).dp)
                )
            }
        }
    }
}

/** Left-pointing notch on the speech bubble */
@Composable
private fun BubbleTail(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 14.dp, height = 26.dp)) {
        val path = Path().apply {
            moveTo(size.width, 0f)
            lineTo(0f, size.height / 2f)
            lineTo(size.width, size.height)
        }
        drawPath(path, Color.White)
        drawPath(path, LessonTheme.line, style = Stroke(width = 2.dp.toPx(), join = StrokeJoin.Round))
        // Hides the bubble's own border where the tail joins it
        drawRect(
            color = Color.White,
            topLeft = Offset(size.width - 2.dp.toPx(), 4.dp.toPx()),
            size = Size(4.dp.toPx(), size.height - 8.dp.toPx())
        )
    }
}

/** Blue CONTINUE-style button; greyed out until the screen has what it needs */
@Composable
private fun PrimaryOnboardingButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    LessonPrimaryButton(
        title = text,
        kind = if (enabled) LessonButtonKind.Primary else LessonButtonKind.Disabled,
        onClick = onClick
    )
}

/** Small secondary action under the primary button ("Not now") */
@Composable
private fun OnboardingGhostButton(text: String, onClick: () -> Unit) {
    val k = LessonTheme.scale()
    Box(
        modifier = Modifier
            .padding(top = 14.dp)
            .height((52 * k).dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.85f))
            .clickable {
                SoundEffectManager.playClickSound()
                onClick()
            }
            .padding(horizontal = 44.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = LessonTheme.answer().copy(fontSize = (20 * k).sp, color = LessonTheme.primaryEdge)
        )
    }
}

/** Centred pill above a quiz question ("Question 2 of 5") */
@Composable
private fun QuestionPill(text: String) {
    val k = LessonTheme.scale()
    Text(
        text = text,
        style = LessonTheme.answer().copy(fontSize = (17 * k).sp, color = AuthTheme.badgeInk),
        modifier = Modifier
            .background(Color(0xFFFFF3D6), CircleShape)
            .padding(horizontal = 18.dp, vertical = 8.dp)
    )
}

@Composable
private fun EmojiTile(emoji: String, size: Int = 44) {
    val k = LessonTheme.scale()
    Box(
        modifier = Modifier
            .size((size * k).dp)
            .background(LessonTheme.sky, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = (size * 0.5f * k).sp, textAlign = TextAlign.Center)
    }
}

// ---------------------------------------------------------------------------------------------
// Screens
// ---------------------------------------------------------------------------------------------

@Composable
fun ChildNameOnboardingScreen(
    childName: String,
    onChildNameChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    val k = LessonTheme.scale()
    OnboardingScaffold(
        bottom = {
            PrimaryOnboardingButton(text = "Continue", enabled = childName.isNotBlank(), onClick = onContinue)
        }
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        MascotSpeech(
            mascotName = "moonwave",
            text = "Assalamu alaikum! What's your child's name?"
        )
        Spacer(modifier = Modifier.height((36 * k).dp))
        AuthTextField(
            value = childName,
            onValueChange = onChildNameChange,
            label = "Child's name",
            placeholder = "Your child's name",
            icon = Icons.Outlined.Person,
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done
        )
        Text(
            text = "You can add more children later.",
            style = LessonTheme.answer().copy(fontSize = (17 * k).sp, textAlign = TextAlign.Start),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, start = 4.dp)
        )
    }
}

@Composable
fun MotivationOnboardingScreen(
    selectedMotivations: Set<String>,
    onMotivationSelected: (String) -> Unit,
    onContinue: () -> Unit
) {
    OnboardingScaffold(
        bottom = {
            PrimaryOnboardingButton(
                text = "Continue",
                enabled = selectedMotivations.isNotEmpty(),
                onClick = onContinue
            )
        }
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        MascotSpeech(
            mascotName = "moon_thinking",
            text = "What are you hoping to achieve with Alifba for your little one?"
        )
        Spacer(modifier = Modifier.height(22.dp))
        OnboardingCopy.motivationOptions.forEach { option ->
            MotivationOptionCard(
                option = option,
                selected = option.title in selectedMotivations,
                onToggle = { onMotivationSelected(option.title) }
            )
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}

@Composable
fun BusyFamiliesScreen(childName: String, onContinue: () -> Unit) {
    val k = LessonTheme.scale()
    OnboardingScaffold(
        bottom = { PrimaryOnboardingButton(text = "Continue", onClick = onContinue) }
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        MascotSpeech(
            mascotName = "moon_waiting",
            text = "Made for busy families: just a few minutes a day."
        )
        Spacer(modifier = Modifier.height(30.dp))
        SettingsGroup {
            FeatureRow(icon = Icons.Outlined.Timer, text = "Short, bite-sized lessons")
            SettingsDivider()
            FeatureRow(icon = Icons.Outlined.Book, text = "Stories, quizzes and duas")
            SettingsDivider()
            FeatureRow(icon = Icons.Outlined.FavoriteBorder, text = "A lasting connection to faith")
        }
        Text(
            text = "Alifba will guide you and $childName every step of the way.",
            style = LessonTheme.story().copy(fontSize = (19 * k).sp, lineHeight = 1.35.em),
            modifier = Modifier.padding(top = 14.dp, start = 4.dp, end = 4.dp)
        )
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, text: String) {
    val k = LessonTheme.scale()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size((44 * k).dp)
                .background(LessonTheme.sky, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = LessonTheme.primary,
                modifier = Modifier.size((24 * k).dp)
            )
        }
        Text(
            text = text,
            style = LessonTheme.answer().copy(fontSize = (21 * k).sp, textAlign = TextAlign.Start),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ParentChallengeIntroScreen(childName: String, onTakeChallenge: () -> Unit) {
    val k = LessonTheme.scale()
    OnboardingScaffold(
        bottom = { PrimaryOnboardingButton(text = "Let's try", onClick = onTakeChallenge) }
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        MascotSpeech(
            mascotName = "moon_excited_jump",
            text = "Let's see what $childName will learn inside Alifba!"
        )
        Text(
            text = "Try ${OnboardingCopy.questions.size} quick questions from our lessons, together.",
            style = LessonTheme.question().copy(fontSize = (29 * k).sp, lineHeight = 1.3.em),
            modifier = Modifier.padding(top = 34.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 26.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StepCard(number = 1, text = "Pick a picture", modifier = Modifier.weight(1f))
            StepCard(number = 2, text = "Choose an answer", modifier = Modifier.weight(1f))
            StepCard(number = 3, text = "Fill the gap", modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier
                .padding(top = 24.dp)
                .background(Color.White.copy(alpha = 0.9f), CircleShape)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = Color(0xFFF5B800),
                modifier = Modifier.size((20 * k).dp)
            )
            Text(
                text = "Trusted by 10,000+ Muslim families",
                style = LessonTheme.answer().copy(fontSize = (18 * k).sp)
            )
        }
    }
}

@Composable
private fun StepCard(number: Int, text: String, modifier: Modifier = Modifier) {
    val k = LessonTheme.scale()
    OnboardingCard(modifier = modifier, padding = 12.dp) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size((42 * k).dp)
                    .background(Color(0xFFFFF3D6), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$number",
                    style = LessonTheme.answer().copy(fontSize = (22 * k).sp, color = AuthTheme.badgeInk)
                )
            }
            Text(
                text = text,
                style = LessonTheme.answer().copy(fontSize = (17 * k).sp),
                minLines = 2,
                maxLines = 2
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Quiz
// ---------------------------------------------------------------------------------------------

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun OnboardingQuizScreen(
    question: OnboardingQuestion,
    questionNumber: Int,
    childName: String,
    audioPreviewStory: Story? = null,
    isAudioPreviewLoading: Boolean = false,
    isAudioPreviewPlaying: Boolean = false,
    audioPreviewError: String? = null,
    onAudioPreviewClick: (Story) -> Unit = {},
    onAnswered: (Boolean) -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val k = LessonTheme.scale()
    val isPreview = LocalInspectionMode.current
    // What's picked but not checked yet, and what's locked in after CHECK
    var selectedAnswer by remember(question.id) { mutableStateOf<String?>(null) }
    var checkedAnswer by remember(question.id) { mutableStateOf<String?>(null) }
    val isChecked = checkedAnswer != null

    if (!isPreview) {
        LaunchedEffect(Unit) {
            SoundEffectManager.initialize(context)
        }
    }

    val pillText = buildString {
        append("Question $questionNumber of ${OnboardingCopy.questions.size}")
        if (question.type == OnboardingQuestionType.FillBlank) append(" · Fill the gap")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        OnboardingScaffold(
            bottom = {
                // The feedback sheet takes this slot once the answer is checked
                Box(modifier = Modifier.alpha(if (isChecked) 0f else 1f)) {
                    PrimaryOnboardingButton(
                        text = "Check",
                        enabled = selectedAnswer != null && !isChecked,
                        onClick = {
                            val answer = selectedAnswer ?: return@PrimaryOnboardingButton
                            checkedAnswer = answer
                            val isCorrect = answer == question.correctAnswer
                            onAnswered(isCorrect)
                            if (!isPreview) {
                                if (isCorrect) SoundEffectManager.playCorrectSound() else SoundEffectManager.playWrongSound()
                            }
                        }
                    )
                }
            }
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            QuestionPill(pillText)

            if (question.type != OnboardingQuestionType.FillBlank) {
                question.speech?.let {
                    Text(
                        text = it,
                        style = LessonTheme.question().copy(fontSize = (29 * k).sp, lineHeight = 1.3.em),
                        modifier = Modifier.padding(top = 14.dp, start = 4.dp, end = 4.dp)
                    )
                }
            }

            if (question.showAudioHint) {
                AudioHintButton(
                    story = audioPreviewStory,
                    isLoading = isAudioPreviewLoading,
                    isPlaying = isAudioPreviewPlaying,
                    error = audioPreviewError,
                    onClick = onAudioPreviewClick
                )
            }

            Spacer(modifier = Modifier.height(if (question.type == OnboardingQuestionType.FillBlank) 18.dp else 26.dp))

            val onSelected: (String?) -> Unit = { if (!isChecked) selectedAnswer = it }
            when (question.type) {
                OnboardingQuestionType.PictureMcq -> PictureQuestionOptions(
                    question = question,
                    selected = selectedAnswer,
                    checked = checkedAnswer,
                    onSelected = onSelected
                )
                OnboardingQuestionType.TextMcq -> TextQuestionOptions(
                    question = question,
                    selected = selectedAnswer,
                    checked = checkedAnswer,
                    onSelected = onSelected
                )
                OnboardingQuestionType.FillBlank -> FillBlankQuestion(
                    question = question,
                    selected = selectedAnswer,
                    checked = checkedAnswer,
                    onSelected = onSelected
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        AnimatedVisibility(
            visible = isChecked,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            val correct = checkedAnswer == question.correctAnswer
            OnboardingFeedbackSheet(
                isCorrect = correct,
                title = if (correct) "Masha'Allah, $childName!" else "Good try, $childName!",
                message = if (correct) {
                    question.valueBanner
                } else {
                    "The answer is ${question.correctAnswer}. ${question.valueBanner}"
                },
                onContinue = onNext
            )
        }
    }
}

/** Card state for an option, given what's selected / checked */
private fun optionState(option: String, selected: String?, checked: String?, correct: String): AnswerState {
    if (checked != null) {
        if (option == correct) return AnswerState.Correct
        if (option == checked) return AnswerState.Wrong
    }
    return if (checked == null && option == selected) AnswerState.Selected else AnswerState.Normal
}

@Composable
private fun ResultBadge(state: AnswerState, modifier: Modifier = Modifier) {
    val k = LessonTheme.scale()
    Box(
        modifier = modifier
            .size((28 * k).dp)
            .background(if (state == AnswerState.Correct) LessonTheme.correct else LessonTheme.wrong, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (state == AnswerState.Correct) Icons.Filled.Check else Icons.Filled.Close,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size((17 * k).dp)
        )
    }
}

@Composable
private fun PictureQuestionOptions(
    question: OnboardingQuestion,
    selected: String?,
    checked: String?,
    onSelected: (String?) -> Unit
) {
    val k = LessonTheme.scale()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        question.options.chunked(2).forEachIndexed { rowIndex, rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                rowOptions.forEachIndexed { itemIndex, option ->
                    val imageIndex = rowIndex * 2 + itemIndex
                    val state = optionState(option, selected, checked, question.correctAnswer)
                    // The weight has to sit on the Row's direct child, so the card gets a wrapper
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .alpha(if (checked != null && state == AnswerState.Normal) 0.5f else 1f)
                    ) {
                        AnswerCard(
                            state = state,
                            modifier = Modifier.fillMaxWidth().height((168 * k).dp),
                            enabled = checked == null,
                            onClick = { onSelected(option) }
                        ) { labelColor ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Image(
                                    painter = painterResource(
                                        id = question.pictureResources.getOrElse(imageIndex) { R.drawable.qna }
                                    ),
                                    contentDescription = option,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.height((100 * k).dp)
                                )
                                Text(
                                    text = option,
                                    style = LessonTheme.answer().copy(fontSize = (22 * k).sp, color = labelColor),
                                    maxLines = 1
                                )
                            }
                            if (checked != null && (state == AnswerState.Correct || state == AnswerState.Wrong)) {
                                ResultBadge(
                                    state = state,
                                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TextQuestionOptions(
    question: OnboardingQuestion,
    selected: String?,
    checked: String?,
    onSelected: (String?) -> Unit
) {
    val k = LessonTheme.scale()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        question.options.forEach { option ->
            val state = optionState(option, selected, checked, question.correctAnswer)
            Box(modifier = Modifier.alpha(if (checked != null && state == AnswerState.Normal) 0.5f else 1f)) {
                AnswerCard(
                    state = state,
                    modifier = Modifier.fillMaxWidth().heightIn(min = (66 * k).dp),
                    enabled = checked == null,
                    onClick = { onSelected(option) }
                ) { labelColor ->
                    Text(
                        text = option,
                        style = LessonTheme.answer().copy(fontSize = (22 * k).sp, color = labelColor),
                        modifier = Modifier.padding(horizontal = 48.dp, vertical = 14.dp)
                    )
                    if (checked != null && (state == AnswerState.Correct || state == AnswerState.Wrong)) {
                        ResultBadge(
                            state = state,
                            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 14.dp)
                        )
                    }
                }
            }
        }
    }
}

/** Sentence card with an inline blank, plus the answer chips underneath */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FillBlankQuestion(
    question: OnboardingQuestion,
    selected: String?,
    checked: String?,
    onSelected: (String?) -> Unit
) {
    val k = LessonTheme.scale()
    val pieces = (question.prompt ?: "").split("[ ______ ]")
    val before = pieces.firstOrNull().orEmpty().split(" ").filter { it.isNotBlank() }
    val after = pieces.getOrNull(1).orEmpty().split(" ").filter { it.isNotBlank() }
    val rowHeight = (46 * k).dp
    val blankState = when {
        checked == null -> AnswerState.Selected
        checked == question.correctAnswer -> AnswerState.Correct
        else -> AnswerState.Wrong
    }

    Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
        OnboardingCard(padding = 20.dp) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                @Composable
                fun Word(word: String) {
                    Box(modifier = Modifier.height(rowHeight), contentAlignment = Alignment.Center) {
                        Text(text = word, style = LessonTheme.question().copy(fontSize = (27 * k).sp))
                    }
                }
                before.forEach { Word(it) }
                if (selected != null) {
                    // The chosen chip sits in the sentence; tap it to take it back
                    Box(
                        modifier = Modifier
                            .height(rowHeight)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                when (blankState) {
                                    AnswerState.Correct -> LessonTheme.correctTint
                                    AnswerState.Wrong -> LessonTheme.wrongTint
                                    else -> LessonTheme.sky
                                }
                            )
                            .border(2.dp, blankState.border, RoundedCornerShape(14.dp))
                            .clickable(enabled = checked == null) { onSelected(null) }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selected,
                            style = LessonTheme.question().copy(fontSize = (27 * k).sp, color = blankState.text)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.height(rowHeight).width((100 * k).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .offset(y = (13 * k).dp)
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(LessonTheme.blankLine, RoundedCornerShape(2.dp))
                        )
                    }
                }
                after.forEach { Word(it) }
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            question.options.forEach { option ->
                if (selected == option) {
                    // The chip has moved into the sentence; leave a dashed slot behind
                    Box(
                        modifier = Modifier
                            .padding(bottom = LessonTheme.answerEdge)
                            .height((56 * k).dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(LessonTheme.track.copy(alpha = 0.6f))
                            .drawBehind {
                                drawRoundRect(
                                    color = Color(0xFFC4CADB),
                                    cornerRadius = CornerRadius(18.dp.toPx()),
                                    style = Stroke(
                                        width = 2.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f))
                                    )
                                )
                            }
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            style = LessonTheme.answer().copy(fontSize = (22 * k).sp, color = Color(0xFFA3A9BC))
                        )
                    }
                } else {
                    Box(modifier = Modifier.alpha(if (checked != null) 0.5f else 1f)) {
                        AnswerCard(
                            state = AnswerState.Normal,
                            modifier = Modifier.height((56 * k).dp),
                            enabled = checked == null,
                            onClick = { onSelected(option) }
                        ) { labelColor ->
                            Text(
                                text = option,
                                style = LessonTheme.answer().copy(fontSize = (22 * k).sp, color = labelColor),
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Bottom sheet after CHECK: the Moon, a personal line and one sentence about why it matters */
@Composable
private fun OnboardingFeedbackSheet(
    isCorrect: Boolean,
    title: String,
    message: String,
    onContinue: () -> Unit
) {
    val k = LessonTheme.scale()
    val ink = if (isCorrect) LessonTheme.correctText else LessonTheme.wrongText
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isCorrect) LessonTheme.correctTint else LessonTheme.wrongTint,
                RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            )
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            .padding(start = LessonTheme.margin, end = LessonTheme.margin, top = 22.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Image(
                painter = painterResource(id = R.drawable.moon_progress_thumb),
                contentDescription = null,
                modifier = Modifier
                    .size((70 * k).dp)
                    .background(LessonTheme.sky, RoundedCornerShape(18.dp))
                    .padding(8.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = LessonTheme.question().copy(
                        fontSize = (29 * k).sp,
                        color = ink,
                        textAlign = TextAlign.Start
                    )
                )
                Text(
                    text = message,
                    style = LessonTheme.story().copy(
                        fontSize = (18 * k).sp,
                        color = ink.copy(alpha = 0.85f),
                        textAlign = TextAlign.Start,
                        lineHeight = 1.3.em
                    )
                )
            }
        }
        LessonPrimaryButton(
            title = "Continue",
            kind = if (isCorrect) LessonButtonKind.Correct else LessonButtonKind.Wrong,
            onClick = onContinue
        )
    }
}

@Composable
private fun AudioHintButton(
    story: Story?,
    isLoading: Boolean,
    isPlaying: Boolean,
    error: String?,
    onClick: (Story) -> Unit
) {
    val k = LessonTheme.scale()
    val enabled = story != null && !isLoading
    Row(
        modifier = Modifier
            .padding(top = 14.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.92f))
            .border(2.dp, LessonTheme.line, CircleShape)
            .clickable(enabled = enabled) { story?.let(onClick) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = LessonTheme.primary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Icon(
                imageVector = if (enabled && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = LessonTheme.primary,
                modifier = Modifier.size((22 * k).dp)
            )
        }
        Text(
            text = when {
                isLoading -> "Loading story preview..."
                error != null -> error
                isPlaying -> "Pause audio story preview"
                story != null -> "Play audio story preview"
                else -> "Audio story preview"
            },
            style = LessonTheme.answer().copy(fontSize = (16 * k).sp, color = LessonTheme.primaryEdge)
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Content screens
// ---------------------------------------------------------------------------------------------

@Composable
fun BedtimeScreen(childName: String, onContinue: () -> Unit) {
    val k = LessonTheme.scale()
    OnboardingScaffold(
        bottom = { PrimaryOnboardingButton(text = "Continue", onClick = onContinue) }
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        MascotSpeech(
            mascotName = "moonwave",
            text = "Bedtime is the perfect time for faith 😴"
        )
        Spacer(modifier = Modifier.height(28.dp))
        OnboardingCard(padding = 22.dp) {
            QuoteMark()
            Text(
                text = OnboardingCopy.bedtimeHadith,
                style = LessonTheme.answer().copy(
                    fontSize = (21 * k).sp,
                    textAlign = TextAlign.Start,
                    lineHeight = 1.3.em
                ),
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                text = OnboardingCopy.bedtimeHadithAttribution,
                style = LessonTheme.answer().copy(
                    fontSize = (16 * k).sp,
                    color = AuthTheme.secondaryText,
                    textAlign = TextAlign.Start
                ),
                modifier = Modifier.padding(top = 14.dp)
            )
        }
        Text(
            text = "Let Alifba guide you and $childName through peaceful bedtime stories and dua each night.",
            style = LessonTheme.story().copy(fontSize = (19 * k).sp, lineHeight = 1.35.em),
            modifier = Modifier.padding(top = 14.dp, start = 4.dp, end = 4.dp)
        )
    }
}

/** Big pale quotation mark that opens quote cards */
@Composable
private fun QuoteMark() {
    val k = LessonTheme.scale()
    Text(
        text = "“",
        style = LessonTheme.question().copy(
            fontSize = (64 * k).sp,
            color = LessonTheme.blankLine,
            textAlign = TextAlign.Start,
            lineHeight = 0.8.em
        ),
        modifier = Modifier.height((38 * k).dp)
    )
}

@Composable
fun TestimonialScreen(testimonial: Testimonial, onContinue: () -> Unit) {
    val k = LessonTheme.scale()
    OnboardingScaffold(
        centered = true,
        bottom = { PrimaryOnboardingButton(text = "Continue", onClick = onContinue) }
    ) {
        OnboardingCard(padding = 24.dp) {
            QuoteMark()
            Text(
                text = testimonial.quote,
                style = LessonTheme.answer().copy(
                    fontSize = (23 * k).sp,
                    textAlign = TextAlign.Start,
                    lineHeight = 1.35.em
                ),
                modifier = Modifier.padding(top = 14.dp)
            )
            Text(
                text = testimonial.author,
                style = LessonTheme.answer().copy(
                    fontSize = (18 * k).sp,
                    color = AuthTheme.secondaryText,
                    textAlign = TextAlign.Start
                ),
                modifier = Modifier.padding(top = 14.dp)
            )
        }
    }
}

@Composable
fun OnboardingLoadingScreen(
    childName: String,
    onFinished: () -> Unit
) {
    val k = LessonTheme.scale()
    val isPreview = LocalInspectionMode.current
    var progress by remember { mutableFloatStateOf(if (isPreview) 0.62f else 0f) }
    val transition = rememberInfiniteTransition(label = "loadingScale")
    val scale by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "mascotScale"
    )

    if (!isPreview) {
        LaunchedEffect(Unit) {
            // TODO analytics: onboarding_loading_viewed
            val totalDurationMs = 12_000L
            val tickMs = 100L
            var elapsedMs = 0L
            while (elapsedMs <= totalDurationMs) {
                val movingElapsedMs = when {
                    elapsedMs < 3_500L -> elapsedMs
                    elapsedMs < 4_500L -> 3_500L
                    elapsedMs < 7_500L -> elapsedMs - 1_000L
                    elapsedMs < 8_500L -> 6_500L
                    else -> elapsedMs - 2_000L
                }.coerceIn(0L, 10_000L)
                progress = movingElapsedMs / 10_000f
                delay(tickMs)
                elapsedMs += tickMs
            }
            onFinished()
        }
    }

    val steps = listOf(
        "Bite-sized lessons for $childName",
        "Bedtime stories and duas",
        "Fun quizzes to practise",
        "Badges to celebrate progress"
    )
    val doneUpTo = (progress.coerceIn(0f, 1f) * steps.size).toInt()

    OnboardingScaffold {
        Spacer(modifier = Modifier.height(8.dp))
        DotLottieView(
            name = "moon_thinking",
            modifier = Modifier
                .height((230 * k).dp)
                .fillMaxWidth(0.6f)
                .scale(scale)
        )
        Text(
            text = "Building $childName's learning path...",
            style = LessonTheme.question().copy(fontSize = (29 * k).sp, lineHeight = 1.3.em),
            modifier = Modifier.padding(top = 6.dp, start = 6.dp, end = 6.dp)
        )

        // Plain track + fill
        Box(
            modifier = Modifier
                .padding(top = 22.dp)
                .fillMaxWidth()
                .height((14 * k).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.7f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0.03f, 1f))
                    .height((14 * k).dp)
                    .clip(CircleShape)
                    .background(LessonTheme.primary)
            )
        }

        OnboardingCard(modifier = Modifier.padding(top = 22.dp), padding = 6.dp) {
            steps.forEachIndexed { index, text ->
                if (index > 0) SettingsDivider()
                LoadingStepRow(
                    text = text,
                    done = index < doneUpTo,
                    working = index == doneUpTo
                )
            }
        }
    }
}

@Composable
private fun LoadingStepRow(text: String, done: Boolean, working: Boolean) {
    val k = LessonTheme.scale()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(modifier = Modifier.size((30 * k).dp), contentAlignment = Alignment.Center) {
            when {
                done -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(LessonTheme.correct, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size((18 * k).dp)
                        )
                    }
                }
                working -> CircularProgressIndicator(
                    color = LessonTheme.primary,
                    trackColor = LessonTheme.track,
                    strokeWidth = 3.dp,
                    modifier = Modifier.fillMaxSize()
                )
                else -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(3.dp, LessonTheme.track, CircleShape)
                )
            }
        }
        Text(
            text = text,
            style = LessonTheme.answer().copy(
                fontSize = (19 * k).sp,
                textAlign = TextAlign.Start,
                color = if (done) LessonTheme.ink else AuthTheme.secondaryText
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun OnboardingResultsScreen(childName: String, score: Int, onContinue: () -> Unit) {
    LaunchedEffect(Unit) {
        // TODO analytics: onboarding_results_viewed
    }
    val k = LessonTheme.scale()
    val total = OnboardingCopy.questions.size

    OnboardingScaffold(
        bottom = { PrimaryOnboardingButton(text = "Continue", onClick = onContinue) }
    ) {
        DotLottieView(
            name = "moon_victory",
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth(0.6f)
                .height((190 * k).dp)
        )
        Row(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(total) { index ->
                Icon(
                    imageVector = if (index < score) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = null,
                    tint = if (index < score) Color(0xFFF5B800) else Color(0xFFB7C3DC),
                    modifier = Modifier.size((34 * k).dp)
                )
            }
        }
        Text(
            text = if (score >= 3) "$childName got $score out of $total!" else "Great effort, $childName!",
            style = LessonTheme.question().copy(fontSize = (31 * k).sp),
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = "Here's what $childName can achieve in 3 months:",
            style = LessonTheme.story().copy(
                fontSize = (19 * k).sp,
                color = AuthTheme.secondaryText,
                lineHeight = 1.3.em
            ),
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(22.dp))
        SettingsGroup {
            ValueListItem("🕌 Build a daily Islamic routine — meaningful 5-minute lessons.")
            SettingsDivider()
            ValueListItem("📖 Remember Prophet stories — through audio and visual learning.")
            SettingsDivider()
            ValueListItem("💖 Develop strong character — kindness, gratitude, and good manners.")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ValueListItem(text: String) {
    val k = LessonTheme.scale()
    // The text starts with an emoji; it goes in the tile, the rest is the sentence
    val emoji = text.substringBefore(" ")
    val rest = text.substringAfter(" ", text)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        EmojiTile(emoji)
        Text(
            text = rest,
            style = LessonTheme.answer().copy(
                fontSize = (18 * k).sp,
                textAlign = TextAlign.Start,
                lineHeight = 1.3.em
            )
        )
    }
}

@Composable
fun ThanksForSharingScreen(childName: String, onContinue: () -> Unit) {
    OnboardingScaffold(
        bottom = { PrimaryOnboardingButton(text = "Continue", onClick = onContinue) }
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        MascotSpeech(
            mascotName = "moonwave",
            text = "🙌 Thanks for sharing! Wherever you're starting from, Alifba is here to " +
                "help you and $childName grow 🌱 in faith 🌙, with lessons 📖 that fit your life."
        )
    }
}

@Composable
fun QuoteScreen(childName: String, onContinue: () -> Unit) {
    val k = LessonTheme.scale()
    OnboardingScaffold(
        centered = true,
        bottom = { PrimaryOnboardingButton(text = "Continue", onClick = onContinue) }
    ) {
        Box(
            modifier = Modifier
                .size((80 * k).dp)
                .background(Color(0xFFFFF3D6), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = Color(0xFFF5B800),
                modifier = Modifier.size((40 * k).dp)
            )
        }
        Text(
            text = "You're doing something beautiful for $childName",
            style = LessonTheme.question().copy(fontSize = (30 * k).sp, lineHeight = 1.3.em),
            modifier = Modifier.padding(top = 24.dp, start = 6.dp, end = 6.dp)
        )
        OnboardingCard(modifier = Modifier.padding(top = 26.dp), padding = 22.dp) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "“${OnboardingCopy.quoteScreenVerse}”",
                    style = LessonTheme.answer().copy(fontSize = (21 * k).sp, lineHeight = 1.4.em)
                )
                Text(
                    text = OnboardingCopy.quoteScreenAttribution,
                    style = LessonTheme.answer().copy(fontSize = (16 * k).sp, color = LessonTheme.correctText),
                    modifier = Modifier
                        .background(LessonTheme.correctTint, CircleShape)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Reminder
// ---------------------------------------------------------------------------------------------

@Composable
fun HabitBuilderScreen(
    childName: String,
    selectedHabitTime: String?,
    selectedReminderHour: Int,
    selectedReminderMinute: Int,
    onHabitSelected: (String, Int, Int) -> Unit,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val k = LessonTheme.scale()
    var showCustomTimePicker by remember { mutableStateOf(false) }

    if (showCustomTimePicker && !isPreview) {
        LaunchedEffect(showCustomTimePicker) {
            TimePickerDialog(
                context,
                R.style.CustomTimePickerTheme,
                { _, hourOfDay, minute ->
                    showCustomTimePicker = false
                    onHabitSelected("Custom Time - ${formatReminderTime(hourOfDay, minute)}", hourOfDay, minute)
                },
                selectedReminderHour,
                selectedReminderMinute,
                false
            ).apply {
                setOnCancelListener { showCustomTimePicker = false }
                setOnDismissListener { showCustomTimePicker = false }
            }.show()
        }
    }

    OnboardingScaffold(
        bottom = {
            PrimaryOnboardingButton(
                text = "Continue",
                enabled = selectedHabitTime != null,
                onClick = onContinue
            )
        }
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        MascotSpeech(
            mascotName = "moon_thinking",
            text = "When is the best time for $childName to learn?"
        )
        Spacer(modifier = Modifier.height(28.dp))
        OnboardingCopy.habitOptions.forEach { option ->
            val isCustom = option.label == "Custom Time"
            val optionLabel = if (isCustom && selectedHabitTime?.startsWith("Custom Time") == true) {
                "Custom Time - ${formatReminderTime(selectedReminderHour, selectedReminderMinute)}"
            } else {
                option.label
            }
            val selected = selectedHabitTime == optionLabel ||
                (isCustom && selectedHabitTime?.startsWith("Custom Time") == true)
            RadioOptionRow(
                title = when (option.label) {
                    "After School - 6:00 PM" -> "After school"
                    "Before Bedtime - 9:00 PM" -> "Before bedtime"
                    else -> "Pick a time"
                },
                trailing = when {
                    isCustom && selected -> formatReminderTime(selectedReminderHour, selectedReminderMinute)
                    isCustom -> null
                    else -> formatReminderTime(option.hour, option.minute)
                },
                showsChevron = isCustom && !selected,
                selected = selected,
                onClick = {
                    if (isCustom) {
                        if (isPreview) {
                            onHabitSelected(
                                "Custom Time - ${formatReminderTime(selectedReminderHour, selectedReminderMinute)}",
                                selectedReminderHour,
                                selectedReminderMinute
                            )
                        } else {
                            showCustomTimePicker = true
                        }
                    } else {
                        onHabitSelected(option.label, option.hour, option.minute)
                    }
                }
            )
            Spacer(modifier = Modifier.height(7.dp))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                tint = LessonTheme.primary,
                modifier = Modifier.size((20 * k).dp)
            )
            Text(
                text = "We'll send one gentle reminder at this time. You can change it any time in Settings.",
                style = LessonTheme.answer().copy(
                    fontSize = (17 * k).sp,
                    textAlign = TextAlign.Start,
                    lineHeight = 1.3.em
                )
            )
        }
    }
}

@Composable
private fun RadioOptionRow(
    title: String,
    trailing: String?,
    showsChevron: Boolean,
    selected: Boolean,
    onClick: () -> Unit
) {
    val k = LessonTheme.scale()
    AnswerCard(
        state = if (selected) AnswerState.Selected else AnswerState.Normal,
        modifier = Modifier.fillMaxWidth().heightIn(min = (66 * k).dp),
        onClick = onClick
    ) { labelColor ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size((26 * k).dp)
                    .border(2.5.dp, if (selected) LessonTheme.primary else Color(0xFFB7C3DC), CircleShape)
                    .then(if (selected) Modifier.background(LessonTheme.primary, CircleShape) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size((9 * k).dp)
                            .background(Color.White, CircleShape)
                    )
                }
            }
            Text(
                text = title,
                style = LessonTheme.answer().copy(
                    fontSize = (22 * k).sp,
                    textAlign = TextAlign.Start,
                    color = labelColor
                ),
                modifier = Modifier.weight(1f)
            )
            if (trailing != null) {
                Text(
                    text = trailing,
                    style = LessonTheme.answer().copy(
                        fontSize = (20 * k).sp,
                        color = if (selected) labelColor else AuthTheme.secondaryText
                    )
                )
            }
            if (showsChevron) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFFA3A9BC),
                    modifier = Modifier.size((22 * k).dp)
                )
            }
        }
    }
}

@Composable
fun NotificationPrePromptScreen(
    childName: String,
    reminderHour: Int,
    reminderMinute: Int,
    onRemindMe: () -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    val k = LessonTheme.scale()
    val timeText = formatReminderTime(reminderHour, reminderMinute)

    OnboardingScaffold(
        centered = true,
        bottom = {
            PrimaryOnboardingButton(
                text = "Remind me at $timeText",
                onClick = {
                    // TODO analytics: onboarding_notification_prompt_tapped
                    enableOnboardingReminder(context, reminderHour, reminderMinute)
                    onRemindMe()
                }
            )
            OnboardingGhostButton(text = "Not now", onClick = onSkip)
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size((104 * k).dp)
                    .background(Color.White.copy(alpha = 0.9f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = LessonTheme.primary,
                    modifier = Modifier.size((52 * k).dp)
                )
            }
            // Small wave mascot beside the bell — not replacing it, per spec.
            DotLottieView(
                name = "moonwave",
                modifier = Modifier.size((70 * k).dp)
            )
        }
        Text(
            text = "Want a gentle reminder for $childName's daily lesson?",
            style = LessonTheme.question().copy(fontSize = (29 * k).sp, lineHeight = 1.3.em),
            modifier = Modifier.padding(top = 26.dp, start = 6.dp, end = 6.dp)
        )
        Text(
            text = "We'll only remind you at the time you choose.",
            style = LessonTheme.story().copy(
                fontSize = (19 * k).sp,
                color = AuthTheme.secondaryText,
                lineHeight = 1.3.em
            ),
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = timeText,
            style = LessonTheme.answer().copy(fontSize = (24 * k).sp, color = LessonTheme.skyText),
            modifier = Modifier
                .padding(top = 18.dp)
                .background(Color.White.copy(alpha = 0.92f), CircleShape)
                .padding(horizontal = 22.dp, vertical = 10.dp)
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Option cards
// ---------------------------------------------------------------------------------------------

@Composable
private fun MotivationOptionCard(
    option: MotivationOption,
    selected: Boolean,
    onToggle: () -> Unit
) {
    val k = LessonTheme.scale()
    AnswerCard(
        state = if (selected) AnswerState.Selected else AnswerState.Normal,
        modifier = Modifier.fillMaxWidth().heightIn(min = (72 * k).dp),
        onClick = onToggle
    ) { labelColor ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EmojiTile(option.emoji)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    style = LessonTheme.answer().copy(
                        fontSize = (19 * k).sp,
                        textAlign = TextAlign.Start,
                        color = labelColor
                    )
                )
                Text(
                    text = option.description,
                    style = LessonTheme.story().copy(
                        fontSize = (15 * k).sp,
                        textAlign = TextAlign.Start,
                        color = AuthTheme.secondaryText,
                        lineHeight = 1.25.em
                    )
                )
            }
            val checkSize = (28 * k).dp
            Box(
                modifier = Modifier
                    .size(checkSize)
                    .background(if (selected) LessonTheme.primary else Color.White, CircleShape)
                    .border(2.dp, if (selected) LessonTheme.primary else Color(0xFFB7C3DC), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(checkSize * 0.62f)
                    )
                }
            }
        }
    }
}

private fun requestNotificationsIfPossible(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val activity = context.findActivity() ?: return
    val permission = Manifest.permission.POST_NOTIFICATIONS
    if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(activity, arrayOf(permission), 123)
    }
}

private fun enableOnboardingReminder(context: Context, hour: Int, minute: Int) {
    requestNotificationsIfPossible(context)
    ReminderPreferences.setReminderTime(context, hour, minute)
    ReminderPreferences.setNotificationPermissionHandled(context, true)
    LessonReminderReceiver.cancelReminder(context)
    LessonReminderReceiver.setDailyReminder(context, hour, minute)
}

private fun formatReminderTime(hour: Int, minute: Int): String {
    val amPm = if (hour >= 12) "PM" else "AM"
    val formattedHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
    val formattedMinute = minute.toString().padStart(2, '0')
    return "$formattedHour:$formattedMinute $amPm"
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

// ---------------------------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------------------------

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ChildNamePreview() {
    ChildNameOnboardingScreen(childName = "Rim", onChildNameChange = {}, onContinue = {})
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun MotivationPreview() {
    MotivationOnboardingScreen(
        selectedMotivations = setOf("Build a daily Islamic routine"),
        onMotivationSelected = {},
        onContinue = {}
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ChallengeIntroPreview() {
    ParentChallengeIntroScreen(childName = "Rim", onTakeChallenge = {})
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PictureQuizPreview() {
    OnboardingQuizScreen(
        question = OnboardingCopy.questions[0],
        questionNumber = 1,
        childName = "Rim",
        onAnswered = {},
        onNext = {}
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun FillBlankQuizPreview() {
    OnboardingQuizScreen(
        question = OnboardingCopy.questions[3],
        questionNumber = 4,
        childName = "Rim",
        onAnswered = {},
        onNext = {}
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LoadingPreview() {
    OnboardingLoadingScreen(childName = "Rim", onFinished = {})
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ResultsPreview() {
    OnboardingResultsScreen(childName = "Rim", score = 4, onContinue = {})
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun HabitPreview() {
    HabitBuilderScreen(
        childName = "Rim",
        selectedHabitTime = "Before Bedtime - 9:00 PM",
        selectedReminderHour = 21,
        selectedReminderMinute = 0,
        onHabitSelected = { _, _, _ -> },
        onContinue = {}
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun NotificationPreview() {
    NotificationPrePromptScreen(
        childName = "Rim",
        reminderHour = 21,
        reminderMinute = 0,
        onRemindMe = {},
        onSkip = {}
    )
}
