package com.alifba.alifba.ui_components.dialogs

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alifba.alifba.R
import com.alifba.alifba.ui_components.theme.darkCandyGreen
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.lightRed
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    val alifbaFontBold = FontFamily(Font(R.font.vag_round_boldd, FontWeight.Bold))
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600

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

    // Add a white surface background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(id = R.drawable.goback),
                        contentDescription = "Back",
                        tint = navyBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = "Salaam, prove you are a grown up",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = if (isTablet) 32.sp else MaterialTheme.typography.headlineMedium.fontSize
                ),
                textAlign = TextAlign.Center,
                color = navyBlue,
                fontFamily = alifbaFontBold
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Question
            Text(
                text = "Type these numbers:",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = if (isTablet) 20.sp else MaterialTheme.typography.bodyLarge.fontSize
                ),
                color = navyBlue,
                fontFamily = alifbaFontBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Time left: ${remainingSeconds}s",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = if (isTablet) 18.sp else MaterialTheme.typography.bodyMedium.fontSize
                ),
                color = lightRed,
                fontFamily = alifbaFontBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = question.words.joinToString(", "),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = if (isTablet) 34.sp else 26.sp
                ),
                textAlign = TextAlign.Center,
                color = darkCandyGreen,
                fontFamily = alifbaFontBold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Answer display - one dash per digit the child needs to type, in the order spoken
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                answer.padStart(question.digits.length, '_').forEach { char ->
                    Text(
                        text = if (char == '_') "_" else char.toString(),
                        modifier = Modifier.padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = if (isTablet) 32.sp else MaterialTheme.typography.headlineMedium.fontSize
                        ),
                        color = navyBlue,
                        fontFamily = alifbaFontBold
                    )
                }
            }

            if (showError) {
                Text(
                    text = "Incorrect answer. Try again!",
                    color = lightRed,
                    modifier = Modifier.padding(top = 8.dp),
                    fontFamily = alifbaFontBold,
                    fontSize = if (isTablet) 18.sp else 14.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Number pad
            NumberPad(
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
    onNumberClick: (String) -> Unit,
    onDelete: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        (0..2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (1..3).forEach { col ->
                    val number = row * 3 + col
                    NumberButton(
                        text = number.toString(),
                        modifier = Modifier.weight(1f),
                        mainColor = lightNavyBlue,
                        shadowColor = navyBlue,
                        onClick = { onNumberClick(number.toString()) }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Zero button
            NumberButton(
                text = "0",
                modifier = Modifier.weight(1f),
                mainColor = lightNavyBlue,
                shadowColor = navyBlue,
                onClick = { onNumberClick("0") }
            )

            // Delete button
            ActionButton(
                modifier = Modifier.weight(1f),
                mainColor = white,
                shadowColor = lightRed,
                onClick = onDelete
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.backspace),
                    contentDescription = "Delete",
                    tint = lightRed
                )
            }

            // Submit button
            ActionButton(
                modifier = Modifier.weight(1f),
                mainColor = white,
                shadowColor = darkCandyGreen,
                onClick = onSubmit
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.check),
                    contentDescription = "Submit",
                    tint = darkCandyGreen

                )
            }
        }
    }
}

@Composable
private fun NumberButton(
    text: String,
    modifier: Modifier = Modifier,
    mainColor: Color,
    shadowColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val coroutineScope = rememberCoroutineScope()

    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 0.dp else 5.dp,
        animationSpec = spring(),
        label = "buttonOffset"
    )

    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.TopCenter
    ) {
        // Shadow layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(shadowColor)
        )

        // Button face layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(bottom = offsetY)
                .clip(RoundedCornerShape(16.dp))
                .background(mainColor)
                .clickable(
                    onClick = {
                        coroutineScope.launch {
                            SoundEffectManager.playClickSound()
                            delay(100)
                            onClick()
                        }
                    },
                    interactionSource = interactionSource,
                    indication = null
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontFamily = FontFamily(Font(R.font.vag_round_boldd, FontWeight.Bold)),
                color = Color.White,
                fontSize = 24.sp

            )
        }
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    mainColor: Color,
    shadowColor: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val coroutineScope = rememberCoroutineScope()

    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 0.dp else 5.dp,
        animationSpec = spring(),
        label = "actionButtonOffset"
    )

    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.TopCenter
    ) {
        // Shadow layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(shadowColor)
        )

        // Button face layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(bottom = offsetY)
                .clip(RoundedCornerShape(16.dp))
                .background(mainColor)
                .clickable(
                    onClick = {
                        coroutineScope.launch {
                            SoundEffectManager.playClickSound()
                            delay(100)
                            onClick()
                        }
                    },
                    interactionSource = interactionSource,
                    indication = null
                ),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
