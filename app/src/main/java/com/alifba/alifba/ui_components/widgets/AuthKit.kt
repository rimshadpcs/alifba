package com.alifba.alifba.ui_components.widgets

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager

// Building blocks for the welcome / sign-up / log-in screens: the cream card, the pill social
// buttons, the text fields and the dismissable banner. Mirrors AuthKit.swift on iOS.

object AuthTheme {
    val cardBorder = Color(0xFFF1EBCB)
    val cardEdge = Color(0xFFEBE4C4)
    val appleEdge = Color(0xFF3A3A3F)
    val secondaryText = Color(0xFF5A5F7A)
    val badgeFace = Color(0xFFFFC83D)
    val badgeInk = Color(0xFF5B3A00)
}

// ---------------------------------------------------------------------------------------------
// Card: white, cream border, raised cream edge
// ---------------------------------------------------------------------------------------------

@Composable
fun AuthCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(28.dp)
    val edge = 7.dp
    Box(modifier = modifier.padding(bottom = edge)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = edge)
                    .background(AuthTheme.cardEdge, shape)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape)
                    .border(BorderStroke(2.dp, AuthTheme.cardBorder), shape)
                    .padding(horizontal = 22.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = content
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Pill buttons (Apple / Google / email) and the white secondary button
// ---------------------------------------------------------------------------------------------

enum class AuthPillKind(val face: Color, val edge: Color, val border: Color, val ink: Color) {
    Apple(Color.Black, AuthTheme.appleEdge, Color.Transparent, Color.White),
    Light(Color.White, LessonTheme.lineEdge, LessonTheme.line, LessonTheme.ink)
}

@Composable
fun AuthPillButton(
    title: String,
    kind: AuthPillKind,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    val k = LessonTheme.scale()
    val edge = 5.dp
    val shape = CircleShape
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val faceOffset by animateDpAsState(if (pressed) edge else 0.dp, label = "pillPress")

    Box(modifier = modifier.fillMaxWidth().padding(bottom = edge)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((56 * k).dp)
                .clickable(interactionSource = interaction, indication = null, enabled = enabled) {
                    SoundEffectManager.playClickSound()
                    onClick()
                },
            propagateMinConstraints = true
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = edge)
                    .background(kind.edge, shape)
            )
            Row(
                modifier = Modifier
                    .offset(y = faceOffset)
                    .background(kind.face, shape)
                    .border(2.dp, kind.border, shape),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon()
                Box(modifier = Modifier.size(12.dp))
                Text(
                    text = title,
                    style = LessonTheme.answer().copy(fontSize = (20 * k).sp, color = kind.ink)
                )
            }
        }
    }
}

/** The white raised secondary button used on the welcome screen */
@Composable
fun LessonSecondaryButton(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val k = LessonTheme.scale()
    AnswerCard(
        state = AnswerState.Normal,
        modifier = modifier.fillMaxWidth().height((58 * k).dp),
        onClick = onClick
    ) {
        Text(
            text = title,
            style = LessonTheme.answer().copy(fontSize = (20 * k).sp, color = LessonTheme.primaryEdge)
        )
    }
}

/** "BEST FOR MULTIPLE DEVICES" tag that straddles the top edge of the Google button */
@Composable
fun AuthBadge(text: String, modifier: Modifier = Modifier) {
    val k = LessonTheme.scale()
    Text(
        text = text.uppercase(),
        style = LessonTheme.answer().copy(
            fontSize = (12 * k).sp,
            letterSpacing = 0.6.sp,
            color = AuthTheme.badgeInk
        ),
        modifier = modifier
            .background(AuthTheme.badgeFace, CircleShape)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    )
}

@Composable
fun AuthOrDivider(text: String = "or") {
    val k = LessonTheme.scale()
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.weight(1f).height(2.dp).background(Color(0xFFEEF0F7)))
        Text(
            text = text,
            style = LessonTheme.answer().copy(fontSize = (17 * k).sp, color = AuthTheme.secondaryText)
        )
        Box(modifier = Modifier.weight(1f).height(2.dp).background(Color(0xFFEEF0F7)))
    }
}

// ---------------------------------------------------------------------------------------------
// Text fields
// ---------------------------------------------------------------------------------------------

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector?,
    modifier: Modifier = Modifier,
    label: String? = null,
    hint: String? = null,
    hintStyle: AuthHintStyle = AuthHintStyle.Neutral,
    isError: Boolean = false,
    capitalization: androidx.compose.ui.text.input.KeyboardCapitalization = androidx.compose.ui.text.input.KeyboardCapitalization.None,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    imeAction: androidx.compose.ui.text.input.ImeAction = androidx.compose.ui.text.input.ImeAction.Next,
    /** A soft sky-blue ring around the field while it has focus */
    focusHalo: Boolean = false
) {
    val k = LessonTheme.scale()
    var focused by remember { mutableStateOf(false) }
    var revealed by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(18.dp)
    val textStyle = LessonTheme.answer().copy(fontSize = (19 * k).sp)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    if (label != null) {
        Text(
            text = label,
            style = LessonTheme.answer().copy(fontSize = (17 * k).sp),
            modifier = Modifier.padding(start = 4.dp)
        )
    }
    // The halo is drawn outside the field's own bounds, so it never moves the layout
    val showHalo = focusHalo && focused && !isError
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height((56 * k).dp)
            .then(
                if (showHalo) {
                    Modifier.drawBehind {
                        val grow = 4.dp.toPx()
                        drawRoundRect(
                            color = LessonTheme.sky,
                            topLeft = Offset(-grow, -grow),
                            size = Size(size.width + grow * 2, size.height + grow * 2),
                            cornerRadius = CornerRadius(22.dp.toPx())
                        )
                    }
                } else Modifier
            )
            .background(if (isError) LessonTheme.wrongTint else Color.White, shape)
            .border(
                2.dp,
                when {
                    isError -> LessonTheme.wrong
                    focused -> LessonTheme.primary
                    else -> LessonTheme.line
                },
                shape
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isError) LessonTheme.wrongEdge else LessonTheme.primary,
                modifier = Modifier.size((22 * k).dp)
            )
        }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) {
                Text(text = placeholder, style = textStyle.copy(color = Color(0xFFA3A9BC)))
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = textStyle,
                cursorBrush = SolidColor(LessonTheme.primary),
                visualTransformation = if (isPassword && !revealed) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(
                    capitalization = capitalization,
                    keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
                    imeAction = imeAction
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focused = it.isFocused }
            )
        }

        if (isPassword) {
            Icon(
                imageVector = if (revealed) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                contentDescription = if (revealed) "Hide password" else "Show password",
                tint = AuthTheme.secondaryText,
                modifier = Modifier
                    .size((22 * k).dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { revealed = !revealed }
            )
        }
    }
    if (hint != null) {
        AuthFieldHint(text = hint, style = hintStyle)
    }
    }
}

enum class AuthHintStyle { Neutral, Good, Bad }

/** Small helper line under a field: grey, green with a tick, or red with an exclamation mark */
@Composable
fun AuthFieldHint(text: String, style: AuthHintStyle, modifier: Modifier = Modifier) {
    val k = LessonTheme.scale()
    val color = when (style) {
        AuthHintStyle.Neutral -> AuthTheme.secondaryText
        AuthHintStyle.Good -> LessonTheme.correctText
        AuthHintStyle.Bad -> LessonTheme.wrongText
    }
    Row(
        modifier = modifier.padding(start = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        when (style) {
            AuthHintStyle.Good -> Icon(Icons.Filled.Check, null, tint = color, modifier = Modifier.size((18 * k).dp))
            AuthHintStyle.Bad -> Icon(Icons.Outlined.ErrorOutline, null, tint = color, modifier = Modifier.size((18 * k).dp))
            AuthHintStyle.Neutral -> {}
        }
        Text(text = text, style = LessonTheme.answer().copy(fontSize = (16 * k).sp, color = color))
    }
}

/** "New to Alifba? Create an account" / "Already have an account? Log in" pill on the grass */
@Composable
fun AuthSwitchPill(
    prefix: String,
    link: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val k = LessonTheme.scale()
    Row(
        modifier = modifier
            .widthIn(max = 560.dp)
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height((56 * k).dp)
            .background(Color(0xEBFFFCF3), CircleShape)
            .clickable {
                SoundEffectManager.playClickSound()
                onClick()
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = prefix, style = LessonTheme.answer().copy(fontSize = (19 * k).sp))
        Box(modifier = Modifier.size(6.dp))
        Text(
            text = link,
            style = LessonTheme.answer().copy(fontSize = (19 * k).sp, color = LessonTheme.primaryEdge)
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Dismissable banner (replaces the system toast)
// ---------------------------------------------------------------------------------------------

@Composable
fun AuthBanner(
    message: String,
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    val k = LessonTheme.scale()
    val shape = RoundedCornerShape(LessonTheme.radius)
    val edge = LessonTheme.answerEdge
    Box(modifier = modifier.padding(bottom = edge)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = edge)
                    .background(LessonTheme.lineEdge, shape)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape)
                    .border(2.dp, LessonTheme.line, shape)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size((44 * k).dp)
                        .background(LessonTheme.sky, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = LessonTheme.primary,
                        modifier = Modifier.size((24 * k).dp)
                    )
                }
                Text(
                    text = message,
                    style = LessonTheme.answer().copy(fontSize = (18 * k).sp, textAlign = TextAlign.Start),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Dismiss",
                    tint = LessonTheme.ink,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onClose
                        )
                )
            }
        }
    }
}
