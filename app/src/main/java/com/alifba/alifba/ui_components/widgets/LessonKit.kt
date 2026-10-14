package com.alifba.alifba.ui_components.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.alifba.alifba.R
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager

/**
 * Shared building blocks for the lesson screens, following the lesson design spec: one colour
 * palette where every colour has one job, ONE answer card (text pills, picture cards and fill-in
 * chips are all the same card with different content) in four states, a primary button pinned at
 * the same bottom position everywhere, a green/coral feedback sheet, and one header.
 */
object LessonTheme {
    // Primary: buttons, progress, current node
    val primary = Color(0xFF5B7BF5)
    val primaryEdge = Color(0xFF3E5BD6)
    // Ink: all questions and captions (12:1 on white)
    val ink = Color(0xFF2F3350)
    // Card: every answer, every type
    val line = Color(0xFFE3E6EF)
    val lineEdge = Color(0xFFD6DAE6)
    // Sky: selected state
    val sky = Color(0xFFEEF2FF)
    val skyText = Color(0xFF2C48B8)
    // Correct / Try again: only after an answer
    val correct = Color(0xFF21B35B)
    val correctEdge = Color(0xFF1A9049)
    val correctTint = Color(0xFFE6F7EC)
    val correctText = Color(0xFF1E6B3A)
    val wrong = Color(0xFFF2545B)
    val wrongEdge = Color(0xFFC2434A)
    val wrongTint = Color(0xFFFDECEC)
    val wrongText = Color(0xFFA32A30)
    // Progress track
    val track = Color(0xFFE8ECF4)
    // The pale blue used for the empty-blank line
    val blankLine = Color(0xFFC8D0FC)

    /** 24 side margin on every lesson screen; images, cards and buttons share it */
    val margin = 24.dp
    /** Radius 20 for cards and buttons, 28 for images */
    val radius = 20.dp
    val imageRadius = 28.dp
    /** 3D edge: 5 on answers, 6 on primary buttons */
    val answerEdge = 5.dp
    val primaryEdge3D = 6.dp

    val font = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )

    /** Type and control sizes are specified for a phone; tablets get a modest bump */
    @Composable
    fun scale(): Float = if (LocalConfiguration.current.screenWidthDp > 600) 1.25f else 1f

    @Composable
    fun isTablet(): Boolean = LocalConfiguration.current.screenWidthDp > 600

    // Type: one rounded family. Question 28/600, story 24/500, answer 22/600, button 22/600
    @Composable
    fun question() = TextStyle(
        fontFamily = font, fontWeight = FontWeight.Bold, fontSize = (28 * scale()).sp,
        lineHeight = 1.25.em, color = ink, textAlign = TextAlign.Center
    )

    @Composable
    fun story() = TextStyle(
        // Bold face: iOS renders its "regular" VAG heavier than Android's regular file, so bold matches
        fontFamily = font, fontWeight = FontWeight.Bold, fontSize = (24 * scale()).sp,
        lineHeight = 1.4.em, color = ink, textAlign = TextAlign.Center
    )

    @Composable
    fun answer() = TextStyle(
        fontFamily = font, fontWeight = FontWeight.Bold, fontSize = (22 * scale()).sp,
        color = ink, textAlign = TextAlign.Center
    )

    @Composable
    fun button() = TextStyle(
        fontFamily = font, fontWeight = FontWeight.Bold, fontSize = (22 * scale()).sp,
        letterSpacing = 0.6.sp, textAlign = TextAlign.Center
    )
}

// ---------------------------------------------------------------------------------------------
// Answer card (one component, four states)
// ---------------------------------------------------------------------------------------------

/** Where a question screen is in its select → CHECK → feedback flow */
enum class AnswerPhase { Answering, Correct, Wrong }

enum class AnswerState(
    val face: Color,
    val border: Color,
    val edge: Color,
    val text: Color
) {
    Normal(Color.White, LessonTheme.line, LessonTheme.lineEdge, LessonTheme.ink),
    Selected(LessonTheme.sky, LessonTheme.primary, LessonTheme.primaryEdge, LessonTheme.skyText),
    Correct(LessonTheme.correctTint, LessonTheme.correct, LessonTheme.correctEdge, LessonTheme.correctText),
    Wrong(LessonTheme.wrongTint, LessonTheme.wrong, LessonTheme.wrongEdge, LessonTheme.wrongText)
}

/**
 * Text pills, picture cards and fill-in chips are all this one card; only the content changes.
 * [modifier] sizes the card's face (e.g. fillMaxWidth().heightIn(min = 64.dp), or size(160.dp));
 * [content] is centred in it and gets the label colour for the current state. The face sinks
 * onto its edge while pressed.
 */
@Composable
fun AnswerCard(
    state: AnswerState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable BoxScope.(labelColor: Color) -> Unit
) {
    val shape = RoundedCornerShape(LessonTheme.radius)
    val edge = LessonTheme.answerEdge
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val faceOffset by animateDpAsState(if (pressed) edge else 0.dp, label = "answerPress")

    // The outer box reserves room for the edge so nothing below overlaps it
    Box(modifier = Modifier.padding(bottom = edge)) {
        Box(
            modifier = modifier.clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled
            ) {
                SoundEffectManager.playClickSound()
                onClick()
            },
            propagateMinConstraints = true
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = edge)
                    .background(state.edge, shape)
            )
            Box(
                modifier = Modifier
                    .offset(y = faceOffset)
                    .background(state.face, shape)
                    .border(2.dp, state.border, shape),
                contentAlignment = Alignment.Center
            ) {
                content(state.text)
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Primary button (CHECK / CONTINUE), pinned at the same bottom position everywhere
// ---------------------------------------------------------------------------------------------

enum class LessonButtonKind(val face: Color, val edge: Color, val text: Color) {
    Primary(LessonTheme.primary, LessonTheme.primaryEdge, Color.White),
    Correct(LessonTheme.correct, LessonTheme.correctEdge, Color.White),
    Wrong(LessonTheme.wrong, LessonTheme.wrongEdge, Color.White),
    Disabled(LessonTheme.line, LessonTheme.lineEdge, Color(0xFFA3A9BC))
}

@Composable
fun LessonPrimaryButton(
    title: String,
    modifier: Modifier = Modifier,
    kind: LessonButtonKind = LessonButtonKind.Primary,
    onClick: () -> Unit
) {
    LessonPrimaryButton(modifier = modifier, kind = kind, onClick = onClick) {
        Text(text = title.uppercase(), style = LessonTheme.button().copy(color = kind.text))
    }
}

/** Primary button with arbitrary content (e.g. an icon) instead of a text label */
@Composable
fun LessonPrimaryButton(
    modifier: Modifier = Modifier,
    kind: LessonButtonKind = LessonButtonKind.Primary,
    height: Dp = (58 * LessonTheme.scale()).dp,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(LessonTheme.radius)
    val edge = LessonTheme.primaryEdge3D
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val faceOffset by animateDpAsState(
        if (pressed && kind != LessonButtonKind.Disabled) edge else 0.dp,
        label = "primaryPress"
    )

    Box(modifier = modifier.fillMaxWidth().padding(bottom = edge)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clickable(interactionSource = interaction, indication = null) {
                    if (kind != LessonButtonKind.Disabled) {
                        SoundEffectManager.playClickSound()
                        onClick()
                    }
                },
            propagateMinConstraints = true
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = edge)
                    .background(kind.edge, shape)
            )
            Box(
                modifier = Modifier
                    .offset(y = faceOffset)
                    .background(kind.face, shape),
                contentAlignment = Alignment.Center,
                content = content
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Page layout: content scrolls, the primary button is pinned at the bottom
// ---------------------------------------------------------------------------------------------

@Composable
fun LessonPage(
    bottom: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = LessonTheme.margin, end = LessonTheme.margin, top = 8.dp, bottom = 16.dp),
            content = content
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = LessonTheme.margin, end = LessonTheme.margin, top = 8.dp, bottom = 16.dp)
        ) {
            bottom()
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Feedback sheet: green or coral, from the bottom, never a grey overlay
// ---------------------------------------------------------------------------------------------

/**
 * Bottom sheet shown after CHECK. Put it in a Box with the page and align it to the bottom
 * centre; it slides in when [visible] and carries the button that takes over CHECK's position.
 */
@Composable
fun BoxScope.LessonFeedbackSheet(
    visible: Boolean,
    correct: Boolean,
    onButton: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = Modifier.align(Alignment.BottomCenter),
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        val tint = if (correct) LessonTheme.correctTint else LessonTheme.wrongTint
        val textColor = if (correct) LessonTheme.correctText else LessonTheme.wrongText
        val composition by rememberLottieComposition(
            LottieCompositionSpec.RawRes(if (correct) R.raw.tick else R.raw.error)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(tint, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .navigationBarsPadding()
                .padding(start = LessonTheme.margin, end = LessonTheme.margin, top = 16.dp, bottom = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Plays once, not as a loop
                LottieAnimation(
                    composition = composition,
                    iterations = 1,
                    modifier = Modifier.size((56 * LessonTheme.scale()).dp)
                )
                Text(
                    text = if (correct) "Correct!" else "Not quite",
                    style = TextStyle(
                        fontFamily = LessonTheme.font,
                        fontWeight = FontWeight.Bold,
                        fontSize = (24 * LessonTheme.scale()).sp,
                        color = textColor
                    )
                )
            }
            Box(modifier = Modifier.padding(top = 12.dp)) {
                LessonPrimaryButton(
                    title = if (correct) "Continue" else "Try again",
                    kind = if (correct) LessonButtonKind.Correct else LessonButtonKind.Wrong,
                    onClick = onButton
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Header: back (hidden, not removed, on step 1) · progress with moon · close
// ---------------------------------------------------------------------------------------------

@Composable
fun LessonCircleButton(
    iconRes: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    hidden: Boolean = false,
    onClick: () -> Unit
) {
    LessonCircleButton(modifier = modifier, hidden = hidden, onClick = onClick) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(LessonTheme.primary),
            modifier = Modifier.size((18 * LessonTheme.scale()).dp)
        )
    }
}

/** The round raised button with any content (e.g. a vector chevron) centred in it */
@Composable
fun LessonCircleButton(
    modifier: Modifier = Modifier,
    hidden: Boolean = false,
    size: Dp = (44 * LessonTheme.scale()).dp,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val edge = (3 * LessonTheme.scale()).dp
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val faceOffset by animateDpAsState(if (pressed) edge else 0.dp, label = "circlePress")

    // Hidden but still occupying its space, so the bar never shifts
    Box(
        modifier = modifier
            .padding(bottom = edge)
            .size(size)
            .alpha(if (hidden) 0f else 1f)
            .then(if (hidden) Modifier.clearAndSetSemantics { } else Modifier)
            .clickable(interactionSource = interaction, indication = null, enabled = !hidden) {
                SoundEffectManager.playClickSound()
                onClick()
            },
        propagateMinConstraints = true
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = edge)
                .background(LessonTheme.lineEdge, CircleShape)
        )
        Box(
            modifier = Modifier
                .offset(y = faceOffset)
                .background(Color.White, CircleShape)
                .border(1.5.dp, LessonTheme.line, CircleShape),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

/** Five dots with the current one in the middle, stretched into a pill */
@Composable
fun LessonPageDots(modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        (-2..2).forEach { offset ->
            Box(
                modifier = Modifier
                    .size(width = if (offset == 0) 22.dp else 8.dp, height = 8.dp)
                    .background(
                        if (offset == 0) LessonTheme.primary else Color(0xFFD9DEEA),
                        CircleShape
                    )
            )
        }
    }
}

@Composable
fun LessonHeader(
    progress: Float,
    showBack: Boolean,
    onBack: () -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LessonCircleButton(
            iconRes = R.drawable.backarrow,
            contentDescription = "Previous Segment",
            hidden = !showBack,
            onClick = onBack
        )
        MoonProgressBar(
            progress = progress,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        )
        LessonCircleButton(
            iconRes = R.drawable.close,
            contentDescription = "Cancel Session",
            onClick = onClose
        )
    }
}


// ---------------------------------------------------------------------------------------------
// Bottom sheets (scrim + sheet) as a full-screen dialog window
// ---------------------------------------------------------------------------------------------

/** The scrim and the white sheet; [content] is laid out centred inside the sheet. */
@Composable
fun SheetScaffold(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(LessonTheme.ink.copy(alpha = 0.55f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss() }
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(start = LessonTheme.margin, end = LessonTheme.margin, top = 20.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}

/** Title, message, a safe primary action and a red text action for the destructive one. */
@Composable
fun ColumnScope.ConfirmSheetContent(
    title: String,
    message: String,
    primaryTitle: String,
    destructiveTitle: String,
    onPrimary: () -> Unit,
    onDestructive: () -> Unit,
    icon: @Composable () -> Unit
) {
    val k = LessonTheme.scale()
    Box(modifier = Modifier.padding(top = 8.dp)) { icon() }
    Text(
        text = title,
        style = LessonTheme.question().copy(fontSize = (28 * k).sp, textAlign = TextAlign.Center)
    )
    Text(
        text = message,
        style = LessonTheme.story().copy(
            fontSize = (19 * k).sp,
            color = Color(0xFF5A5F7A),
            textAlign = TextAlign.Center,
            lineHeight = 1.35.em
        ),
        modifier = Modifier.padding(bottom = 8.dp)
    )
    LessonPrimaryButton(title = primaryTitle, onClick = onPrimary)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                SoundEffectManager.playClickSound()
                onDestructive()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = destructiveTitle,
            style = LessonTheme.answer().copy(fontSize = (20 * k).sp, color = LessonTheme.wrongEdge)
        )
    }
}

@Composable
fun ConfirmSheet(
    title: String,
    message: String,
    primaryTitle: String,
    destructiveTitle: String,
    onPrimary: () -> Unit,
    onDestructive: () -> Unit,
    icon: @Composable () -> Unit
) {
    SheetScaffold(onDismiss = onPrimary) {
        ConfirmSheetContent(title, message, primaryTitle, destructiveTitle, onPrimary, onDestructive, icon)
    }
}

/** Round tinted icon badge for the confirm sheets */
@Composable
fun ConfirmSheetIcon(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    face: Color
) {
    val k = LessonTheme.scale()
    Box(
        modifier = Modifier
            .size((72 * k).dp)
            .background(face, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size((32 * k).dp)
        )
    }
}

// "Leave this lesson?" bottom sheet
@Composable
fun LeaveLessonSheet(
    onKeepLearning: () -> Unit,
    onLeave: () -> Unit
) {
    val k = LessonTheme.scale()
    ConfirmSheet(
        title = "Leave this lesson?",
        message = "You\u2019re doing great! If you leave now, your progress in this lesson won\u2019t be saved.",
        primaryTitle = "Keep learning",
        destructiveTitle = "Leave lesson",
        onPrimary = onKeepLearning,
        onDestructive = onLeave
    ) {
        Image(
            painter = painterResource(id = R.drawable.moon_progress_thumb),
            contentDescription = null,
            modifier = Modifier.size((84 * k).dp)
        )
    }
}
