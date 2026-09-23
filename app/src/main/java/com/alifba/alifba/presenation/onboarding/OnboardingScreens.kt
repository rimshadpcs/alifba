package com.alifba.alifba.presenation.onboarding

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.alifba.alifba.R
import com.alifba.alifba.data.models.Story
import com.alifba.alifba.service.LessonReminderReceiver
import com.alifba.alifba.presenation.stories.AudioPlayerViewModel
import com.alifba.alifba.ui_components.dialogs.LottieAnimationDialog
import com.alifba.alifba.ui_components.widgets.DotLottieView
import com.alifba.alifba.ui_components.widgets.textFields.CustomInputField
import com.alifba.alifba.ui_components.theme.Beige
import com.alifba.alifba.ui_components.theme.darkCandyGreen
import com.alifba.alifba.ui_components.theme.darkPink
import com.alifba.alifba.ui_components.theme.darkRed
import com.alifba.alifba.ui_components.theme.lightCandyGreen
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.lightPink
import com.alifba.alifba.ui_components.theme.lightPurple
import com.alifba.alifba.ui_components.theme.lightRed
import com.alifba.alifba.ui_components.theme.mediumpurple
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.alifba.alifba.ui_components.widgets.buttons.MCQChoiceButton
import com.alifba.alifba.ui_components.widgets.buttons.OptionButton
import com.alifba.alifba.ui_components.widgets.buttons.PictureButton
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager
import com.alifba.alifba.utils.ReminderPreferences
import kotlinx.coroutines.delay

private val AlifbaFont = FontFamily(
    Font(R.font.vag_round, FontWeight.Normal),
    Font(R.font.vag_round_boldd, FontWeight.Bold)
)

private val OnboardingBackground = Color(0xFFFFF8EC)
private val TextPrimary = Color(0xFF33333A)
private val TextSecondary = Color(0xFF6D6A63)
private val SoftGreen = Color(0xFFEAF7DC)
private val SoftPink = Color(0xFFFFEDF6)
private val SoftBlue = Color(0xFFEFF4FF)

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
        quote = "Finally, an app that isn't just dry lectures! My kids actually ask to do their " +
            "\"Alifba time\" before bed. The audio stories about the Prophets are their favorite.",
        author = "Sarah K."
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

    val motivations: List<String> = listOf(
        "Reduce passive screen time",
        "Build a daily Islamic routine",
        "Teach Prophets' stories",
        "Build better Islamic habits",
        "All of the above"
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
        // Individual screens still keep their own white/beige card behind mascot speech,
        // quotes, and option rows — this is only the full-bleed backdrop behind those, not
        // a replacement for that legibility layer. Matches iOS's OnboardingView alternation.
        Image(
            painter = painterResource(
                id = if (state.stepIndex % 2 == 0) R.drawable.onboardingbgone else R.drawable.onboardingbgtwo
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.safeDrawing.asPaddingValues())
        ) {
            val quizQuestionIndex = OnboardingCopy.quizStepToQuestionIndex[state.stepIndex]
            // Shared across every screen — shows overall progress through the whole 18-screen
            // flow, not just the quiz's own 5 questions. Deliberately no step numbers, per spec.
            OnboardingTopBar(
                progress = (state.stepIndex + 1f) / (OnboardingCopy.lastStepIndex + 1f),
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
                    state.stepIndex == 12 -> OnboardingLoadingScreen(onFinished = viewModel::nextStep)
                    state.stepIndex == 13 -> OnboardingResultsScreen(
                        childName = state.childName,
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
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ChildNameOnboardingScreen(
    childName: String,
    onChildNameChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    val isTablet = LocalConfiguration.current.screenWidthDp > 600
    OnboardingScaffold {
        Spacer(modifier = Modifier.weight(if (isTablet) 0.9f else 0.65f))
        MascotSpeech(
            mascotName = "moonwave",
            text = "Assalamu alaikum — What's your child's name?"
        )
        Spacer(modifier = Modifier.height(if (isTablet) 14.dp else 22.dp))
        CustomInputField(
            value = childName,
            onValueChange = onChildNameChange,
            labelText = "Child's Name",
            modifier = Modifier.padding(horizontal = if (isTablet) 40.dp else 12.dp)
        )
        Text(
            text = "You can add more children later.",
            fontFamily = AlifbaFont,
            fontWeight = FontWeight.Bold,
            fontSize = if (isTablet) 20.sp else 15.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = if (isTablet) 16.dp else 10.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        StableContinueButton(
            visible = childName.isNotBlank(),
            onClick = onContinue
        )
    }
}

@Composable
fun BusyFamiliesScreen(childName: String, onContinue: () -> Unit) {
    val isTablet = LocalConfiguration.current.screenWidthDp > 600
    OnboardingScaffold {
        Spacer(modifier = Modifier.weight(if (isTablet) 0.9f else 0.65f))
        MascotSpeech(
            mascotName = "moon_waiting",
            text = "Designed for Busy Families — Just a Few Minutes a Day 🕰️"
        )
        Text(
            text = "That's all it takes to build a lasting connection to faith, and Alifba " +
                "will guide you and $childName every step of the way.",
            fontFamily = AlifbaFont,
            fontWeight = FontWeight.Bold,
            fontSize = if (isTablet) 22.sp else 18.sp,
            lineHeight = if (isTablet) 28.sp else 24.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = if (isTablet) 24.dp else 18.dp, start = 20.dp, end = 20.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        CommonButton(
            onClick = onContinue,
            buttonText = "Continue",
            shadowColor = navyBlue,
            mainColor = lightNavyBlue,
            textColor = white
        )
    }
}

@Composable
fun MotivationOnboardingScreen(
    selectedMotivations: Set<String>,
    onMotivationSelected: (String) -> Unit,
    onContinue: () -> Unit
) {
    val isTablet = LocalConfiguration.current.screenWidthDp > 600
    OnboardingScaffold {
        Spacer(modifier = Modifier.weight(if (isTablet) 0.65f else 0.45f))
        MascotSpeech(
            mascotName = "moon_thinking",
            text = "What are you hoping to achieve with Alifba for your little one?"
        )
        Spacer(modifier = Modifier.height(if (isTablet) 14.dp else 22.dp))
        OnboardingCopy.motivations.forEach { motivation ->
            SelectableOptionRow(
                text = motivation,
                selected = motivation in selectedMotivations,
                onClick = { onMotivationSelected(motivation) }
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        StableContinueButton(
            visible = selectedMotivations.isNotEmpty(),
            onClick = onContinue
        )
    }
}

@Composable
fun ParentChallengeIntroScreen(childName: String, onTakeChallenge: () -> Unit) {
    val isTablet = LocalConfiguration.current.screenWidthDp > 600
    OnboardingScaffold {
        Spacer(modifier = Modifier.weight(if (isTablet) 0.9f else 0.65f))
        MascotSpeech(
            mascotName = "moon_excited_jump",
            text = "Let's explore what $childName will learn inside Alifba!"
        )
        Text(
            text = "Just 5 quick questions inspired by our lessons.",
            fontFamily = AlifbaFont,
            fontWeight = FontWeight.Bold,
            fontSize = if (isTablet) 32.sp else 26.sp,
            lineHeight = if (isTablet) 38.sp else 32.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = if (isTablet) 30.dp else 24.dp, start = 20.dp, end = 20.dp)
        )
        Text(
            text = "Trusted by 10,000+ Muslim families.",
            fontFamily = AlifbaFont,
            fontWeight = FontWeight.Bold,
            fontSize = if (isTablet) 22.sp else 18.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = if (isTablet) 24.dp else 18.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SoftGreen)
                .padding(horizontal = if (isTablet) 24.dp else 16.dp, vertical = if (isTablet) 12.dp else 10.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        CommonButton(
            onClick = onTakeChallenge,
            buttonText = "Take the Challenge",
            shadowColor = navyBlue,
            mainColor = lightNavyBlue,
            textColor = white
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun OnboardingQuizScreen(
    question: OnboardingQuestion,
    audioPreviewStory: Story? = null,
    isAudioPreviewLoading: Boolean = false,
    isAudioPreviewPlaying: Boolean = false,
    audioPreviewError: String? = null,
    onAudioPreviewClick: (Story) -> Unit = {},
    onAnswered: (Boolean) -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val isTablet = LocalConfiguration.current.screenWidthDp > 600
    val isPreview = LocalInspectionMode.current
    var selectedAnswer by remember(question.id) { mutableStateOf<String?>(null) }
    var showFeedback by remember(question.id) { mutableStateOf(false) }
    val isAnswered = selectedAnswer != null

    if (!isPreview) {
        LaunchedEffect(Unit) {
            SoundEffectManager.initialize(context)
        }
    }

    LaunchedEffect(selectedAnswer) {
        if (isPreview) return@LaunchedEffect
        val answer = selectedAnswer ?: return@LaunchedEffect
        val isCorrect = answer == question.correctAnswer
        onAnswered(isCorrect)
        if (isCorrect) SoundEffectManager.playCorrectSound() else SoundEffectManager.playWrongSound()
        showFeedback = true
        delay(850)
        showFeedback = false
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = if (isTablet) 36.dp else 18.dp, vertical = if (isTablet) 22.dp else 14.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        val quizWidth = if (maxWidth > 900.dp) 900.dp else maxWidth
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .widthIn(max = quizWidth),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // No mascot on quiz screens, per spec — question.speech is still shown as
                    // plain text (it's the question prompt itself, not decorative copy).
                    question.speech?.let {
                        MascotSpeech(
                            mascotName = null,
                            text = it,
                            compact = true
                        )
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

                    question.prompt?.let {
                        FillBlankPrompt(text = it, selectedAnswer = selectedAnswer)
                    }

                    Spacer(modifier = Modifier.height(if (isTablet) 24.dp else 16.dp))

                    when (question.type) {
                        OnboardingQuestionType.PictureMcq -> PictureQuestionOptions(
                            question = question,
                            selectedAnswer = selectedAnswer,
                            onSelected = { if (!isAnswered) selectedAnswer = it }
                        )
                        OnboardingQuestionType.TextMcq -> TextQuestionOptions(
                            question = question,
                            onSelected = { if (!isAnswered) selectedAnswer = it }
                        )
                        OnboardingQuestionType.FillBlank -> FillBlankOptions(
                            question = question,
                            selectedAnswer = selectedAnswer,
                            onSelected = { if (!isAnswered) selectedAnswer = it }
                        )
                    }

                    val correct = selectedAnswer == question.correctAnswer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isTablet) 54.dp else 40.dp)
                            .padding(top = if (isTablet) 18.dp else 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (correct) "Great answer!" else "Good try - Alifba teaches this gently.",
                            fontFamily = AlifbaFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isTablet) 22.sp else 18.sp,
                            color = if (correct) darkCandyGreen else darkPink,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.alpha(if (isAnswered) 1f else 0f)
                        )
                    }

                    Spacer(modifier = Modifier.height(if (isTablet) 24.dp else 18.dp))
                    ValueBanner(text = question.valueBanner)
                    Spacer(modifier = Modifier.height(if (isTablet) 24.dp else 18.dp))

                    StableContinueButton(
                        visible = isAnswered,
                        onClick = onNext
                    )
                }
            }
        }
    }

    if (showFeedback && !isPreview) {
        val correct = selectedAnswer == question.correctAnswer
        val showDialog = remember { mutableStateOf(true) }
        LottieAnimationDialog(
            showDialog = showDialog,
            lottieFileRes = if (correct) R.raw.tick else R.raw.error,
            durationMs = 800
        )
    }
}

@Composable
fun OnboardingLoadingScreen(onFinished: () -> Unit) {
    val isTablet = LocalConfiguration.current.screenWidthDp > 600
    val isPreview = LocalInspectionMode.current
    var messageIndex by remember { mutableIntStateOf(0) }
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
                messageIndex = ((elapsedMs / totalDurationMs.toFloat()) * OnboardingCopy.loadingMessages.size).toInt()
                    .coerceIn(0, OnboardingCopy.loadingMessages.lastIndex)
                delay(tickMs)
                elapsedMs += tickMs
            }
            onFinished()
        }
    }

    OnboardingScaffold {
        Spacer(modifier = Modifier.weight(1f))
        DotLottieView(
            name = "moon_thinking",
            modifier = Modifier
                .fillMaxWidth(if (isTablet) 0.44f else 0.7f)
                .height(if (isTablet) 320.dp else 260.dp)
                .scale(scale)
        )
        Text(
            text = "Building your little one's learning path...",
            fontFamily = AlifbaFont,
            fontWeight = FontWeight.Bold,
            fontSize = if (isTablet) 36.sp else 28.sp,
            lineHeight = if (isTablet) 42.sp else 34.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(if (isTablet) 28.dp else 22.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth(if (isTablet) 0.78f else 0.9f)
                .height(if (isTablet) 16.dp else 12.dp)
                .clip(RoundedCornerShape(50)),
            color = lightNavyBlue,
            trackColor = Color(0xFFE5DFD1)
        )
        Text(
            text = OnboardingCopy.loadingMessages[messageIndex],
            fontFamily = AlifbaFont,
            fontWeight = FontWeight.Bold,
            fontSize = if (isTablet) 22.sp else 17.sp,
            lineHeight = if (isTablet) 28.sp else 22.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = if (isTablet) 28.dp else 20.dp, start = 20.dp, end = 20.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun OnboardingResultsScreen(childName: String, onContinue: () -> Unit) {
    LaunchedEffect(Unit) {
        // TODO analytics: onboarding_results_viewed
    }
    val isTablet = LocalConfiguration.current.screenWidthDp > 600
    val headline = "$childName is going to love this."

    OnboardingScaffold {
        Spacer(modifier = Modifier.weight(if (isTablet) 0.45f else 0.3f))
        DotLottieView(
            name = "moon_victory",
            modifier = Modifier
                .fillMaxWidth(if (isTablet) 0.46f else 0.62f)
                .height(if (isTablet) 300.dp else 220.dp)
        )
        Text(
            text = headline,
            fontFamily = AlifbaFont,
            fontWeight = FontWeight.Bold,
            fontSize = if (isTablet) 36.sp else 27.sp,
            lineHeight = if (isTablet) 43.sp else 33.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Here's what $childName can achieve in 3 months:",
            fontFamily = AlifbaFont,
            fontWeight = FontWeight.Bold,
            fontSize = if (isTablet) 26.sp else 20.sp,
            lineHeight = if (isTablet) 32.sp else 26.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = if (isTablet) 28.dp else 22.dp, bottom = if (isTablet) 16.dp else 10.dp)
        )
        ValueListItem("🕌 Build a daily Islamic routine - meaningful 5-minute lessons.")
        ValueListItem("📖 Remember Prophet stories - through audio and visual learning.")
        ValueListItem("💖 Develop strong character - kindness, gratitude, and good manners.")
        Spacer(modifier = Modifier.weight(1f))
        CommonButton(
            onClick = onContinue,
            buttonText = "Continue",
            shadowColor = navyBlue,
            mainColor = lightNavyBlue,
            textColor = white
        )
    }
}

@Composable
fun BedtimeScreen(childName: String, onContinue: () -> Unit) {
    val isTablet = LocalConfiguration.current.screenWidthDp > 600
    OnboardingScaffold {
        Spacer(modifier = Modifier.weight(if (isTablet) 0.9f else 0.65f))
        MascotSpeech(
            mascotName = "moonwave",
            text = "Bedtime Is the Perfect Time for Faith 😴"
        )
        Text(
            text = "“${OnboardingCopy.bedtimeHadith}”",
            fontFamily = AlifbaFont,
            fontWeight = FontWeight.Bold,
            fontSize = if (isTablet) 22.sp else 17.sp,
            lineHeight = if (isTablet) 28.sp else 22.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = if (isTablet) 24.dp else 18.dp)
                .clip(RoundedCornerShape(if (isTablet) 24.dp else 16.dp))
                .background(white)
                .border(2.dp, Beige, RoundedCornerShape(if (isTablet) 24.dp else 16.dp))
                .padding(if (isTablet) 24.dp else 16.dp)
        )
        Text(
            text = "— ${OnboardingCopy.bedtimeHadithAttribution}",
            fontFamily = AlifbaFont,
            fontWeight = FontWeight.Bold,
            fontSize = if (isTablet) 18.sp else 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = if (isTablet) 12.dp else 8.dp)
        )
        Text(
            text = "Let Alifba guide you and $childName through peaceful bedtime stories and dua each night.",
            fontFamily = AlifbaFont,
            fontWeight = FontWeight.Bold,
            fontSize = if (isTablet) 22.sp else 18.sp,
            lineHeight = if (isTablet) 28.sp else 24.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = if (isTablet) 20.dp else 14.dp, start = 20.dp, end = 20.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        CommonButton(
            onClick = onContinue,
            buttonText = "Continue",
            shadowColor = navyBlue,
            mainColor = lightNavyBlue,
            textColor = white
        )
    }
}

@Composable
fun TestimonialScreen(testimonial: Testimonial, onContinue: () -> Unit) {
    val isTablet = LocalConfiguration.current.screenWidthDp > 600
    OnboardingScaffold {
        Spacer(modifier = Modifier.weight(1f))
        Image(
            painter = painterResource(id = R.drawable.quote),
            contentDescription = null,
            modifier = Modifier
                .size(if (isTablet) 48.dp else 36.dp)
                .padding(bottom = if (isTablet) 12.dp else 8.dp)
        )
        Text(
            text = "“${testimonial.quote}”",
            fontFamily = AlifbaFont,
            fontWeight = FontWeight.Bold,
            fontSize = if (isTablet) 30.sp else 23.sp,
            lineHeight = if (isTablet) 38.sp else 30.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(if (isTablet) 24.dp else 16.dp))
                .background(white)
                .border(2.dp, Beige, RoundedCornerShape(if (isTablet) 24.dp else 16.dp))
                .padding(if (isTablet) 32.dp else 20.dp)
        )
        Text(
            text = "— ${testimonial.author}",
            fontFamily = AlifbaFont,
            fontWeight = FontWeight.Bold,
            fontSize = if (isTablet) 22.sp else 17.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = if (isTablet) 20.dp else 14.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        CommonButton(
            onClick = onContinue,
            buttonText = "Continue",
            shadowColor = navyBlue,
            mainColor = lightNavyBlue,
            textColor = white
        )
    }
}

@Composable
fun ThanksForSharingScreen(childName: String, onContinue: () -> Unit) {
    val isTablet = LocalConfiguration.current.screenWidthDp > 600
    OnboardingScaffold {
        Spacer(modifier = Modifier.weight(if (isTablet) 0.9f else 0.65f))
        MascotSpeech(
            mascotName = "moonwave",
            text = "🙌 Thanks for sharing! Wherever you're starting from, Alifba is here to " +
                "help you and $childName grow 🌱 in faith 🌙, with lessons 📖 that fit your life."
        )
        Spacer(modifier = Modifier.weight(1f))
        CommonButton(
            onClick = onContinue,
            buttonText = "Continue",
            shadowColor = navyBlue,
            mainColor = lightNavyBlue,
            textColor = white
        )
    }
}

@Composable
fun QuoteScreen(childName: String, onContinue: () -> Unit) {
    val isTablet = LocalConfiguration.current.screenWidthDp > 600
    OnboardingScaffold {
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "🌟 You Are Doing Something Beautiful for $childName 🌟",
            fontFamily = AlifbaFont,
            fontWeight = FontWeight.Bold,
            fontSize = if (isTablet) 32.sp else 25.sp,
            lineHeight = if (isTablet) 40.sp else 31.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Text(
            text = "“${OnboardingCopy.quoteScreenVerse}”",
            fontFamily = AlifbaFont,
            fontWeight = FontWeight.Bold,
            fontSize = if (isTablet) 24.sp else 19.sp,
            lineHeight = if (isTablet) 32.sp else 26.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = if (isTablet) 28.dp else 22.dp)
                .clip(RoundedCornerShape(if (isTablet) 24.dp else 16.dp))
                .background(white)
                .border(2.dp, Beige, RoundedCornerShape(if (isTablet) 24.dp else 16.dp))
                .padding(if (isTablet) 28.dp else 18.dp)
        )
        Text(
            text = "— ${OnboardingCopy.quoteScreenAttribution}",
            fontFamily = AlifbaFont,
            fontWeight = FontWeight.Bold,
            fontSize = if (isTablet) 20.sp else 15.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = if (isTablet) 14.dp else 10.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        CommonButton(
            onClick = onContinue,
            buttonText = "Continue",
            shadowColor = navyBlue,
            mainColor = lightNavyBlue,
            textColor = white
        )
    }
}

@Composable
fun HabitBuilderScreen(
    selectedHabitTime: String?,
    selectedReminderHour: Int,
    selectedReminderMinute: Int,
    onHabitSelected: (String, Int, Int) -> Unit,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val isTablet = LocalConfiguration.current.screenWidthDp > 600
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

    OnboardingScaffold {
        Spacer(modifier = Modifier.weight(if (isTablet) 0.75f else 0.5f))
        MascotSpeech(
            mascotName = "moon_thinking",
            text = "I'll help your little one build a daily learning habit."
        )
        Text(
            text = "When is the best time to learn?",
            fontFamily = AlifbaFont,
            fontWeight = FontWeight.Bold,
            fontSize = if (isTablet) 34.sp else 25.sp,
            lineHeight = if (isTablet) 40.sp else 31.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = if (isTablet) 34.dp else 22.dp)
        )
        OnboardingCopy.habitOptions.forEach { option ->
            val optionLabel = if (option.label == "Custom Time" && selectedHabitTime?.startsWith("Custom Time") == true) {
                "Custom Time - ${formatReminderTime(selectedReminderHour, selectedReminderMinute)}"
            } else {
                option.label
            }
            SelectableOptionRow(
                text = optionLabel,
                selected = selectedHabitTime == optionLabel || (option.label == "Custom Time" && selectedHabitTime?.startsWith("Custom Time") == true),
                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = lightNavyBlue) },
                onClick = {
                    if (option.label == "Custom Time") {
                        if (isPreview) {
                            onHabitSelected("Custom Time - ${formatReminderTime(selectedReminderHour, selectedReminderMinute)}", selectedReminderHour, selectedReminderMinute)
                        } else {
                            showCustomTimePicker = true
                        }
                    } else {
                        onHabitSelected(option.label, option.hour, option.minute)
                    }
                }
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        StableContinueButton(
            visible = selectedHabitTime != null,
            onClick = onContinue
        )
    }
}

@Composable
fun NotificationPrePromptScreen(
    childName: String,
    reminderHour: Int,
    reminderMinute: Int,
    onRemindMe: () -> Unit
) {
    val context = LocalContext.current
    val isTablet = LocalConfiguration.current.screenWidthDp > 600

    OnboardingScaffold {
        Spacer(modifier = Modifier.weight(if (isTablet) 0.85f else 0.55f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(if (isTablet) 140.dp else 104.dp)
                    .clip(CircleShape)
                    .background(SoftBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = lightNavyBlue,
                    modifier = Modifier.size(if (isTablet) 78.dp else 58.dp)
                )
            }
            Spacer(modifier = Modifier.width(if (isTablet) 12.dp else 6.dp))
            // Small wave mascot beside the bell — not replacing it, per spec.
            DotLottieView(
                name = "moonwave",
                modifier = Modifier.size(if (isTablet) 70.dp else 52.dp)
            )
        }
        Text(
            text = "Want a gentle reminder for $childName's daily lesson?",
            fontFamily = AlifbaFont,
            fontWeight = FontWeight.Bold,
            fontSize = if (isTablet) 38.sp else 29.sp,
            lineHeight = if (isTablet) 46.sp else 35.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = if (isTablet) 42.dp else 28.dp)
        )
        Text(
            text = "We'll only remind you at the time you choose.",
            fontFamily = AlifbaFont,
            fontWeight = FontWeight.Bold,
            fontSize = if (isTablet) 24.sp else 18.sp,
            lineHeight = if (isTablet) 30.sp else 24.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = if (isTablet) 22.dp else 14.dp)
        )
        Text(
            text = formatReminderTime(reminderHour, reminderMinute),
            fontFamily = AlifbaFont,
            fontWeight = FontWeight.Bold,
            fontSize = if (isTablet) 30.sp else 22.sp,
            color = navyBlue,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = if (isTablet) 24.dp else 14.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(SoftBlue)
                .padding(horizontal = if (isTablet) 28.dp else 18.dp, vertical = if (isTablet) 12.dp else 8.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        CommonButton(
            onClick = {
                // TODO analytics: onboarding_notification_prompt_tapped
                enableOnboardingReminder(context, reminderHour, reminderMinute)
                onRemindMe()
            },
            buttonText = "Remind Me",
            shadowColor = navyBlue,
            mainColor = lightNavyBlue,
            textColor = white
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun OnboardingScaffold(content: @Composable ColumnScope.() -> Unit) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val isTablet = maxWidth > 600.dp
        val constrainedWidth = if (maxWidth > 920.dp) 920.dp else maxWidth
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .widthIn(max = constrainedWidth)
                .padding(
                    horizontal = if (isTablet) 44.dp else 22.dp,
                    vertical = if (isTablet) 30.dp else 14.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

@Composable
private fun MascotSpeech(
    mascotName: String?,
    text: String,
    compact: Boolean = false
) {
    val isTablet = LocalConfiguration.current.screenWidthDp > 600
    val mascotSize = when {
        isTablet && compact -> 124.dp
        isTablet -> 170.dp
        compact -> 78.dp
        else -> 112.dp
    }
    val speechFontSize = when {
        isTablet && compact -> 31.sp
        isTablet -> 34.sp
        compact -> 22.sp
        else -> 24.sp
    }
    val speechLineHeight = when {
        isTablet && compact -> 38.sp
        isTablet -> 42.sp
        compact -> 28.sp
        else -> 31.sp
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (mascotName != null) {
            DotLottieView(
                name = mascotName,
                modifier = Modifier.size(mascotSize)
            )
            Spacer(modifier = Modifier.width(if (isTablet) 18.dp else 10.dp))
        }
        Text(
            text = text,
            fontFamily = AlifbaFont,
            fontWeight = FontWeight.Bold,
            fontSize = speechFontSize,
            lineHeight = speechLineHeight,
            color = TextPrimary,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(if (isTablet) 24.dp else 18.dp))
                .background(white)
                .border(2.dp, Beige, RoundedCornerShape(if (isTablet) 24.dp else 18.dp))
                .padding(
                    horizontal = if (isTablet) 24.dp else 16.dp,
                    vertical = if (isTablet) 22.dp else 14.dp
                )
        )
    }
}

@Composable
private fun OnboardingBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    padded: Boolean = true
) {
    val isTablet = LocalConfiguration.current.screenWidthDp > 600
    Box(
        modifier = modifier
            .then(
                if (padded) {
                    Modifier.padding(
                        start = if (isTablet) 28.dp else 16.dp,
                        top = if (isTablet) 26.dp else 14.dp
                    )
                } else {
                    Modifier
                }
            )
            .size(if (isTablet) 56.dp else 44.dp)
            .clip(CircleShape)
            .background(white)
            .border(2.dp, Color(0xFFE8DFC9), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = TextPrimary,
            modifier = Modifier.size(if (isTablet) 30.dp else 24.dp)
        )
    }
}

@Composable
private fun OnboardingTopBar(
    progress: Float,
    onBack: () -> Unit
) {
    val isTablet = LocalConfiguration.current.screenWidthDp > 600
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = if (isTablet) 28.dp else 16.dp,
                vertical = if (isTablet) 26.dp else 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OnboardingBackButton(
            onClick = onBack,
            modifier = Modifier,
            padded = false
        )
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .padding(start = if (isTablet) 16.dp else 12.dp)
                .height(if (isTablet) 10.dp else 8.dp)
                .clip(RoundedCornerShape(50)),
            color = lightNavyBlue,
            trackColor = Color(0xFFE5DFD1)
        )
    }
}

@Composable
private fun StableContinueButton(
    visible: Boolean,
    onClick: () -> Unit,
    text: String = "Continue"
) {
    CommonButton(
        onClick = onClick,
        buttonText = text,
        shadowColor = navyBlue,
        mainColor = lightNavyBlue,
        textColor = white,
        enabled = visible,
        modifier = Modifier.alpha(if (visible) 1f else 0f)
    )
}

@Composable
private fun SelectableOptionRow(
    text: String,
    selected: Boolean,
    leadingIcon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    val isTablet = LocalConfiguration.current.screenWidthDp > 600
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isTablet) 9.dp else 6.dp)
            .clip(RoundedCornerShape(if (isTablet) 16.dp else 12.dp))
            .background(if (selected) SoftGreen else white)
            .border(
                width = if (isTablet) 3.dp else 2.dp,
                color = if (selected) lightCandyGreen else Color(0xFFE8DFC9),
                shape = RoundedCornerShape(if (isTablet) 16.dp else 12.dp)
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = if (isTablet) 24.dp else 16.dp,
                vertical = if (isTablet) 22.dp else 16.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingIcon?.invoke()
        if (leadingIcon != null) Spacer(modifier = Modifier.width(if (isTablet) 14.dp else 10.dp))
        Text(
            text = text,
            fontFamily = AlifbaFont,
            fontWeight = FontWeight.Bold,
            fontSize = if (isTablet) 24.sp else 18.sp,
            lineHeight = if (isTablet) 30.sp else 23.sp,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = darkCandyGreen,
                modifier = Modifier.size(if (isTablet) 32.dp else 24.dp)
            )
        }
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
    val enabled = story != null && !isLoading
    Row(
        modifier = Modifier
            .padding(top = 10.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(SoftPink)
            .border(2.dp, lightPink, RoundedCornerShape(40.dp))
            .clickable(enabled = enabled) { story?.let(onClick) }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = darkPink,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
        } else if (enabled) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = darkPink,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.audiostory),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = when {
                isLoading -> "Loading story preview..."
                error != null -> error
                isPlaying -> "Pause audio story preview"
                story != null -> "Play audio story preview"
                else -> "Audio story focus"
            },
            fontFamily = AlifbaFont,
            fontWeight = FontWeight.Bold,
            color = darkPink,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun PictureQuestionOptions(
    question: OnboardingQuestion,
    selectedAnswer: String?,
    onSelected: (String) -> Unit
) {
    val isTablet = LocalConfiguration.current.screenWidthDp > 600
    Column(verticalArrangement = Arrangement.spacedBy(if (isTablet) 22.dp else 12.dp)) {
        question.options.chunked(2).forEachIndexed { rowIndex, rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (isTablet) 22.dp else 12.dp)
            ) {
                rowOptions.forEachIndexed { itemIndex, option ->
                    val imageIndex = rowIndex * 2 + itemIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = if (selectedAnswer == option) 3.dp else 0.dp,
                                color = if (selectedAnswer == question.correctAnswer) lightCandyGreen else lightRed,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        PictureButton(
                            onClick = { onSelected(option) },
                            buttonImage = question.pictureResources.getOrElse(imageIndex) { R.drawable.qna }.toString(),
                            buttonText = option,
                            tabletSize = 260.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TextQuestionOptions(
    question: OnboardingQuestion,
    onSelected: (String) -> Unit
) {
    question.options.forEachIndexed { index, option ->
        val (main, shadow) = optionColors(index)
        MCQChoiceButton(
            onClick = { onSelected(option) },
            buttonText = option,
            mainColor = main,
            shadowColor = shadow
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FillBlankOptions(
    question: OnboardingQuestion,
    selectedAnswer: String?,
    onSelected: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        question.options.forEach { option ->
            Box(
                modifier = Modifier
                    .border(
                        width = if (selectedAnswer == option) 3.dp else 0.dp,
                        color = if (option == question.correctAnswer) lightCandyGreen else lightRed,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                OptionButton(
                    onClick = { onSelected(option) },
                    buttonText = option
                )
            }
        }
    }
}

@Composable
private fun FillBlankPrompt(text: String, selectedAnswer: String?) {
    val isTablet = LocalConfiguration.current.screenWidthDp > 600
    val prompt = selectedAnswer?.let { text.replace("[ ______ ]", "[ $it ]") } ?: text
    Text(
        text = prompt,
        fontFamily = AlifbaFont,
        fontWeight = FontWeight.Bold,
        fontSize = if (isTablet) 34.sp else 25.sp,
        lineHeight = if (isTablet) 44.sp else 34.sp,
        textAlign = TextAlign.Center,
        color = TextPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (isTablet) 28.dp else 16.dp)
            .clip(RoundedCornerShape(if (isTablet) 18.dp else 12.dp))
            .background(white)
            .border(2.dp, Beige, RoundedCornerShape(if (isTablet) 18.dp else 12.dp))
            .padding(if (isTablet) 28.dp else 18.dp)
    )
}

@Composable
private fun ValueBanner(text: String) {
    val isTablet = LocalConfiguration.current.screenWidthDp > 600
    Text(
        text = text,
        fontFamily = AlifbaFont,
        fontWeight = FontWeight.Bold,
        fontSize = if (isTablet) 22.sp else 16.sp,
        lineHeight = if (isTablet) 30.sp else 22.sp,
        color = Color.Black,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(if (isTablet) 16.dp else 10.dp))
            .background(SoftGreen)
            .padding(if (isTablet) 22.dp else 14.dp)
    )
}

@Composable
private fun ValueListItem(text: String) {
    val isTablet = LocalConfiguration.current.screenWidthDp > 600
    Text(
        text = text,
        fontFamily = AlifbaFont,
        fontWeight = FontWeight.Bold,
        fontSize = if (isTablet) 24.sp else 18.sp,
        lineHeight = if (isTablet) 32.sp else 24.sp,
        color = TextPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isTablet) 9.dp else 6.dp)
            .clip(RoundedCornerShape(if (isTablet) 16.dp else 10.dp))
            .background(white)
            .border(2.dp, Color(0xFFE8DFC9), RoundedCornerShape(if (isTablet) 16.dp else 10.dp))
            .padding(if (isTablet) 22.dp else 14.dp)
    )
}

private fun optionColors(index: Int): Pair<Color, Color> {
    return when (index) {
        0 -> lightRed to darkRed
        1 -> lightCandyGreen to darkCandyGreen
        2 -> lightPurple to mediumpurple
        else -> lightPink to darkPink
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

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun MotivationOnboardingScreenPreview() {
    Box(modifier = Modifier.background(OnboardingBackground)) {
        MotivationOnboardingScreen(
            selectedMotivations = setOf(
                "Build a daily Islamic routine",
                "Teach Prophets' stories"
            ),
            onMotivationSelected = {},
            onContinue = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ParentChallengeIntroScreenPreview() {
    Box(modifier = Modifier.background(OnboardingBackground)) {
        ParentChallengeIntroScreen(childName = "Zayd", onTakeChallenge = {})
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OnboardingPictureQuizScreenPreview() {
    Box(modifier = Modifier.background(OnboardingBackground)) {
        OnboardingQuizScreen(
            question = OnboardingCopy.questions[0],
            onAnswered = {},
            onNext = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OnboardingTextQuizScreenPreview() {
    Box(modifier = Modifier.background(OnboardingBackground)) {
        OnboardingQuizScreen(
            question = OnboardingCopy.questions[1],
            onAnswered = {},
            onNext = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OnboardingFillBlankQuizScreenPreview() {
    Box(modifier = Modifier.background(OnboardingBackground)) {
        OnboardingQuizScreen(
            question = OnboardingCopy.questions[3],
            onAnswered = {},
            onNext = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OnboardingLoadingScreenPreview() {
    Box(modifier = Modifier.background(OnboardingBackground)) {
        OnboardingLoadingScreen(onFinished = {})
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OnboardingResultsScreenPreview() {
    Box(modifier = Modifier.background(OnboardingBackground)) {
        OnboardingResultsScreen(childName = "Zayd", onContinue = {})
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun HabitBuilderScreenPreview() {
    Box(modifier = Modifier.background(OnboardingBackground)) {
        HabitBuilderScreen(
            selectedHabitTime = "Before Bedtime - 9:00 PM",
            selectedReminderHour = 21,
            selectedReminderMinute = 0,
            onHabitSelected = { _, _, _ -> },
            onContinue = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun NotificationPrePromptScreenPreview() {
    Box(modifier = Modifier.background(OnboardingBackground)) {
        NotificationPrePromptScreen(
            childName = "Zayd",
            reminderHour = 19,
            reminderMinute = 30,
            onRemindMe = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 1280)
@Composable
private fun MotivationOnboardingTabletPreview() {
    Box(modifier = Modifier.background(OnboardingBackground)) {
        MotivationOnboardingScreen(
            selectedMotivations = setOf(
                "Build a daily Islamic routine",
                "Teach Prophets' stories"
            ),
            onMotivationSelected = {},
            onContinue = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 1280)
@Composable
private fun ParentChallengeIntroTabletPreview() {
    Box(modifier = Modifier.background(OnboardingBackground)) {
        ParentChallengeIntroScreen(childName = "Zayd", onTakeChallenge = {})
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 1280)
@Composable
private fun OnboardingPictureQuizTabletPreview() {
    Box(modifier = Modifier.background(OnboardingBackground)) {
        OnboardingQuizScreen(
            question = OnboardingCopy.questions[0],
            onAnswered = {},
            onNext = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 1280)
@Composable
private fun OnboardingTextQuizTabletPreview() {
    Box(modifier = Modifier.background(OnboardingBackground)) {
        OnboardingQuizScreen(
            question = OnboardingCopy.questions[1],
            onAnswered = {},
            onNext = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 1280)
@Composable
private fun OnboardingFillBlankQuizTabletPreview() {
    Box(modifier = Modifier.background(OnboardingBackground)) {
        OnboardingQuizScreen(
            question = OnboardingCopy.questions[3],
            onAnswered = {},
            onNext = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 1280)
@Composable
private fun OnboardingLoadingTabletPreview() {
    Box(modifier = Modifier.background(OnboardingBackground)) {
        OnboardingLoadingScreen(onFinished = {})
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 1280)
@Composable
private fun OnboardingResultsTabletPreview() {
    Box(modifier = Modifier.background(OnboardingBackground)) {
        OnboardingResultsScreen(childName = "Zayd", onContinue = {})
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 1280)
@Composable
private fun HabitBuilderTabletPreview() {
    Box(modifier = Modifier.background(OnboardingBackground)) {
        HabitBuilderScreen(
            selectedHabitTime = "Before Bedtime - 9:00 PM",
            selectedReminderHour = 21,
            selectedReminderMinute = 0,
            onHabitSelected = { _, _, _ -> },
            onContinue = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 1280)
@Composable
private fun NotificationPrePromptTabletPreview() {
    Box(modifier = Modifier.background(OnboardingBackground)) {
        NotificationPrePromptScreen(
            childName = "Zayd",
            reminderHour = 19,
            reminderMinute = 30,
            onRemindMe = {}
        )
    }
}
