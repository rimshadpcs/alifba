package com.alifba.alifba.presenation.home.layout

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.alifba.alifba.R
import com.alifba.alifba.presenation.login.AuthViewModel
import com.alifba.alifba.presenation.home.layout.profile.getAvatarHeadShots
import com.alifba.alifba.ui_components.theme.darkCandyGreen
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.lightRed
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
@Composable
fun ParentGate(
    onVerified: () -> Unit,
    onDismiss: () -> Unit
) {
    var answer by remember { mutableStateOf("") }
    var question by remember { mutableStateOf(generateQuestion()) }
    var showError by remember { mutableStateOf(false) }
    val alifbaFont = FontFamily(Font(R.font.vag_round, FontWeight.Bold))
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
                text = "Salaam, grown ups!",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = navyBlue,
                fontFamily = alifbaFont,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Question
            Text(
                text = "Please solve this math problem:",
                style = MaterialTheme.typography.bodyLarge,
                color = navyBlue,
                fontFamily = alifbaFont,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = question.text,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                color = darkCandyGreen,
                fontFamily = alifbaFont,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Answer display - modified to show only 2 dashes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                answer.padStart(2, '_').forEach { char ->
                    Text(
                        text = if (char == '_') "_" else char.toString(),
                        modifier = Modifier.padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.headlineMedium,
                        color = navyBlue,
                        fontFamily = alifbaFont,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (showError) {
                Text(
                    text = "Incorrect answer. Try again!",
                    color = lightRed,
                    modifier = Modifier.padding(top = 8.dp),
                    fontFamily = alifbaFont,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Number pad
            NumberPad(
                onNumberClick = { num ->
                    if (answer.length < 2) { // Modified to limit to 2 digits
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
                    if (answer.toIntOrNull() == question.result) {
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


private data class MathQuestion(
    val text: String,
    val result: Int
)

private fun generateQuestion(): MathQuestion {
    val operations = listOf(
        { a: Int, b: Int -> Triple("$a × $b = ?", a * b, "multiplication") },
        { a: Int, b: Int -> Triple("$a + $b = ?", a + b, "addition") }
    )

    // Modified to ensure two-digit answers
    val num1 = Random.nextInt(4, 10)
    val num2 = Random.nextInt(4, 10)
    val (text, result, _) = operations.random()(num1, num2)

    return MathQuestion(text, result)
}
// Update your HomeTopBar to use the ParentGate
@Composable
fun HomeTopBar(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var showParentGate by remember { mutableStateOf(false) }
    val userProfile by authViewModel.userProfileState.collectAsState()
    val avatarName = userProfile?.avatar ?: "deenasaur"
    val avatarDrawable = getAvatarHeadShots(avatarName)
    val coroutineScope = rememberCoroutineScope()

    // Show ParentGate if triggered
    if (showParentGate) {
        ParentGate(
            onVerified = {
                coroutineScope.launch {
                    SoundEffectManager.playClickSound()
                    delay(100)
                    navController.navigate("settings")
                }
                showParentGate = false
            },
            onDismiss = {
                showParentGate = false
            }
        )
    }

    Row(
        modifier = Modifier
            .background(Color.Transparent)
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TopBarIcons(
            painter = painterResource(id = avatarDrawable),
            contentDescription = "Profile",
            onClick = {
                coroutineScope.launch {
                    SoundEffectManager.playClickSound()
                    delay(100)
                    navController.navigate("profile")
                }
            }
        )

        TopBarIcons(
            painter = painterResource(id = R.drawable.setting),
            contentDescription = "Settings",
            onClick = {
                showParentGate = true
            }
        )
    }
}


@Composable
fun TopBarIcons(
    painter: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    shadowColor: Color = Color(0xFFAAAAAA), // Subtle shadow color
    mainColor: Color = Color.White // Main background color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val coroutineScope = rememberCoroutineScope()

    // Press animation: shadow offset
    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 0.dp else 5.dp,
        animationSpec = spring(),
        label = "IconOffset"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(84.dp) // Ensure equal width and height for a perfect circle
            .padding(8.dp) // Padding around the entire box
    ) {
        // Shadow layer
        Box(
            modifier = Modifier
                .size(74.dp) // Shadow size
                .clip(CircleShape)
                .background(shadowColor) // Shadow color
        )

        // Icon layer with press effect
        Box(
            modifier = Modifier
                .size(74.dp) // Same size as the shadow layer
                .padding(bottom = offsetY) // Apply offset for press effect
                .clip(CircleShape)
                .background(mainColor) // Main color of the icon
                .clickable(
                    onClick = {
                        coroutineScope.launch {
                            delay(100)
                            onClick()
                        }
                    },
                    interactionSource = interactionSource,
                    indication = null
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.size(50.dp) // Icon size within the circle
            )
        }
    }
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
                fontFamily = FontFamily(Font(R.font.vag_round, FontWeight.Bold)),
                fontWeight = FontWeight.Bold,
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