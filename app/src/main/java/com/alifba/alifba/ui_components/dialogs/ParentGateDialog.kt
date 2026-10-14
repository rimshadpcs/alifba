package com.alifba.alifba.ui_components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alifba.alifba.R
import com.alifba.alifba.ui_components.widgets.AnswerCard
import com.alifba.alifba.ui_components.widgets.AnswerState
import com.alifba.alifba.ui_components.widgets.LessonCircleButton
import com.alifba.alifba.ui_components.widgets.LessonPrimaryButton
import com.alifba.alifba.ui_components.widgets.LessonTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

// A full-screen "prove you're a grown-up" gate shared by every screen that needs one
// (settings, paywall, profile switching) — shows a short sequence of spoken numbers and
// asks the child to type the matching digits in order, e.g. "four, five, seven, eight" -> 4578.
@Composable
fun ParentGate(
    onVerified: () -> Unit,
    onDismiss: () -> Unit
) {
    var answer by remember { mutableStateOf("") }
    var question by remember { mutableStateOf(generateQuestion()) }
    var showError by remember { mutableStateOf(false) }
    var remainingSeconds by remember { mutableStateOf(10) }
    val k = LessonTheme.scale()
    val grey = Color(0xFF5A5F7A)

    LaunchedEffect(question) {
        remainingSeconds = 10
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
        // Time ran out for this question: reset
        answer = ""
        showError = true
        question = generateQuestion()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp)
            ) {
                LessonCircleButton(
                    iconRes = R.drawable.backarrow,
                    contentDescription = "Back",
                    onClick = onDismiss
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LessonTheme.margin),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Spacer(modifier = Modifier.height(0.dp))
                Text(
                    text = "Salaam! Prove you are a grown-up",
                    style = LessonTheme.question().copy(fontSize = (28 * k).sp, textAlign = TextAlign.Center)
                )
                Text(
                    text = "Type these numbers",
                    style = LessonTheme.answer().copy(fontSize = (18 * k).sp, color = grey)
                )

                // The spoken digits
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LessonTheme.sky, RoundedCornerShape(LessonTheme.radius))
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = question.words.joinToString(", "),
                        style = LessonTheme.question().copy(fontSize = (30 * k).sp, textAlign = TextAlign.Center)
                    )
                }

                // Countdown
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val timerColor = if (remainingSeconds <= 3) LessonTheme.wrongEdge else grey
                    Icon(
                        painter = painterResource(id = R.drawable.ic_stopwatch),
                        contentDescription = null,
                        tint = timerColor,
                        modifier = Modifier.size((18 * k).dp)
                    )
                    Text(
                        text = "${remainingSeconds}s left",
                        style = LessonTheme.answer().copy(fontSize = (17 * k).sp, color = timerColor)
                    )
                }

                // One box per digit, in the order spoken
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(question.digits.length) { index ->
                        DigitBox(
                            digit = answer.getOrNull(index)?.toString().orEmpty(),
                            active = index == answer.length,
                            error = showError
                        )
                    }
                }

                Text(
                    text = if (showError) "Incorrect answer. Try again!" else "",
                    style = LessonTheme.answer().copy(fontSize = (17 * k).sp, color = LessonTheme.wrongEdge),
                    modifier = Modifier.heightIn(min = (22 * k).dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            NumberPad(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = LessonTheme.margin, end = LessonTheme.margin, bottom = 12.dp),
                onNumberClick = { num ->
                    if (answer.length < question.digits.length) {
                        answer += num
                        showError = false
                    }
                },
                onDelete = {
                    if (answer.isNotEmpty()) {
                        answer = answer.dropLast(1)
                        showError = false
                    }
                },
                onSubmit = {
                    if (answer == question.digits) {
                        onVerified()
                    } else {
                        showError = true
                        answer = ""
                        question = generateQuestion()
                    }
                }
            )
        }
    }
}

@Composable
private fun DigitBox(digit: String, active: Boolean, error: Boolean) {
    val k = LessonTheme.scale()
    val shape = RoundedCornerShape(14.dp)
    val border = when {
        error -> LessonTheme.wrong
        digit.isNotEmpty() || active -> LessonTheme.primary
        else -> LessonTheme.line
    }
    Box(
        modifier = Modifier
            .size(width = (56 * k).dp, height = (66 * k).dp)
            .background(if (error) LessonTheme.wrongTint else Color.White, shape)
            .border(2.dp, border, shape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = digit, style = LessonTheme.question().copy(fontSize = (28 * k).sp))
    }
}

private val parentGateNumberWords = listOf(
    "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"
)

private data class ParentGateQuestion(
    val words: List<String>,
    val digits: String
)

private fun generateQuestion(): ParentGateQuestion {
    val digitCount = 4
    val digits = List(digitCount) { Random.nextInt(0, 10) }
    return ParentGateQuestion(
        words = digits.map { parentGateNumberWords[it] },
        digits = digits.joinToString("")
    )
}


@Composable
private fun NumberPad(
    modifier: Modifier = Modifier,
    onNumberClick: (String) -> Unit,
    onDelete: () -> Unit,
    onSubmit: () -> Unit
) {
    val k = LessonTheme.scale()
    val keyHeight = (56 * k).dp

    @Composable
    fun RowScope.Key(digit: String) {
        // The weight has to sit on the Row's direct child, so the card gets its own wrapper
        Box(modifier = Modifier.weight(1f)) {
            AnswerCard(
                state = AnswerState.Normal,
                modifier = Modifier.fillMaxWidth().height(keyHeight),
                onClick = { onNumberClick(digit) }
            ) { color ->
                Text(text = digit, style = LessonTheme.question().copy(fontSize = (28 * k).sp, color = color))
            }
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        (0..2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                (1..3).forEach { col -> Key((row * 3 + col).toString()) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                AnswerCard(
                    state = AnswerState.Normal,
                    modifier = Modifier.fillMaxWidth().height(keyHeight),
                    onClick = onDelete
                ) { color ->
                    Icon(
                        painter = painterResource(id = R.drawable.backspace),
                        contentDescription = "Delete",
                        tint = color,
                        modifier = Modifier.size((26 * k).dp)
                    )
                }
            }
            Key("0")
            LessonPrimaryButton(
                modifier = Modifier.weight(1f),
                height = keyHeight - 1.dp,
                onClick = onSubmit
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.check),
                    contentDescription = "Submit",
                    tint = Color.White,
                    modifier = Modifier.size((26 * k).dp)
                )
            }
        }
    }
}
