package com.alifba.alifba.presenation.home.layout.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alifba.alifba.R
import com.alifba.alifba.presenation.SubscriptionViewModel
import com.alifba.alifba.ui_components.widgets.LessonCircleButton
import com.alifba.alifba.ui_components.widgets.LessonTheme
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager

@SuppressLint("NewApi")
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val k = LessonTheme.scale()

    // URLs for external links
    val privacyPolicyUrl = "https://alifba.xyz/privacy-policy"
    val termsAndConditionsUrl = "https://alifba.xyz/terms-of-service"
    val bugReportUrl = "https://alifba.userjot.com/board/bugs-feedback?"
    val feedbackUrl = "https://alifba.userjot.com/board/bugs-feedback?"
    val featureSuggestionUrl = "https://alifba.userjot.com/board/feature-request?"

    // State variables
    var showNotificationDialog by remember { mutableStateOf(false) }
    val isSoundEnabled = remember { mutableStateOf(SoundEffectManager.isSoundEnabled) }
    val isHapticEnabled = remember { mutableStateOf(SoundEffectManager.isHapticEnabled) }
    val subscriptionViewModel: SubscriptionViewModel = hiltViewModel()
    val isPremium by subscriptionViewModel.isPremium.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8FD))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Round back button on the left, centred title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Settings",
                    style = LessonTheme.question().copy(fontSize = (32 * k).sp, textAlign = TextAlign.Center)
                )
                Box(modifier = Modifier.align(Alignment.CenterStart)) {
                    LessonCircleButton(
                        iconRes = R.drawable.backarrow,
                        contentDescription = "Back",
                        onClick = { navController.popBackStack() }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .padding(start = LessonTheme.margin, end = LessonTheme.margin, bottom = 24.dp)
            ) {
                SettingsGroup {
                    SettingsRow(
                        title = "Account",
                        icon = rememberVectorPainter(Icons.Outlined.Person),
                        onClick = { navController.navigate("accountScreen") }
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "Subscription",
                        icon = painterResource(id = R.drawable.ic_crown),
                        tile = Color(0xFFFFF3D6),
                        tint = Color(0xFFF5B800),
                        badge = if (isPremium) {
                            SettingsBadge("PREMIUM", Color(0xFFFFC83D), Color(0xFF5B3A00))
                        } else {
                            SettingsBadge("FREE", LessonTheme.track, Color(0xFF5A5F7A))
                        },
                        onClick = { navController.navigate("subscription") }
                    )
                }

                SettingsSectionLabel("App settings")
                SettingsGroup {
                    SettingsRow(
                        title = "Notifications",
                        icon = rememberVectorPainter(Icons.Outlined.Notifications),
                        onClick = { showNotificationDialog = true }
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "Button sound",
                        icon = rememberVectorPainter(Icons.Outlined.VolumeUp),
                        showToggle = true,
                        isToggled = isSoundEnabled.value,
                        onClick = {
                            isSoundEnabled.value = !isSoundEnabled.value
                            SoundEffectManager.toggleSound(isSoundEnabled.value)
                            if (isSoundEnabled.value) {
                                SoundEffectManager.playClickSound()
                            }
                        }
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "Vibration",
                        icon = rememberVectorPainter(Icons.Outlined.Vibration),
                        showToggle = true,
                        isToggled = isHapticEnabled.value,
                        onClick = {
                            isHapticEnabled.value = !isHapticEnabled.value
                            SoundEffectManager.toggleHaptic(isHapticEnabled.value)
                            // A tick to show what was just turned on
                            if (isHapticEnabled.value) {
                                SoundEffectManager.tapHaptic()
                            }
                        }
                    )
                }

                SettingsSectionLabel("Help us improve")
                SettingsGroup {
                    SettingsRow(
                        title = "Report a bug",
                        icon = rememberVectorPainter(Icons.Outlined.BugReport),
                        onClick = { openUrl(context, bugReportUrl) }
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "Send feedback",
                        icon = rememberVectorPainter(Icons.Outlined.ChatBubbleOutline),
                        onClick = { openUrl(context, feedbackUrl) }
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "Suggest a feature",
                        icon = rememberVectorPainter(Icons.Outlined.Lightbulb),
                        onClick = { openUrl(context, featureSuggestionUrl) }
                    )
                }

                SettingsSectionLabel("Legal")
                SettingsGroup {
                    SettingsRow(
                        title = "Privacy policy",
                        icon = rememberVectorPainter(Icons.Outlined.Shield),
                        onClick = { openUrl(context, privacyPolicyUrl) }
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "Terms and conditions",
                        icon = rememberVectorPainter(Icons.Outlined.Description),
                        onClick = { openUrl(context, termsAndConditionsUrl) }
                    )
                }
            }
        }
    }

    // Show notification dialog if needed
    if (showNotificationDialog) {
        NotificationDialog(
            showDialog = showNotificationDialog,
            onDismiss = { showNotificationDialog = false },
            context = context
        )
    }
}

// Helper function to open URLs
private fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

data class SettingsBadge(val text: String, val face: Color, val ink: Color)

/** A white card with a 2dp border and a raised bottom edge, like the lesson answer cards */
@Composable
fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(LessonTheme.radius)
    val edge = LessonTheme.answerEdge
    Box(modifier = Modifier.padding(bottom = edge)) {
        Box(modifier = Modifier.fillMaxWidth(), propagateMinConstraints = false) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = edge)
                    .background(LessonTheme.lineEdge, shape)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(Color.White)
                    .border(2.dp, LessonTheme.line, shape),
                content = content
            )
        }
    }
}

@Composable
fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(Color(0xFFEEF0F7))
    )
}

@Composable
fun SettingsSectionLabel(title: String) {
    Text(
        text = title.uppercase(),
        style = LessonTheme.answer().copy(
            fontSize = (15 * LessonTheme.scale()).sp,
            letterSpacing = 1.5.sp,
            color = Color(0xFF5A5F7A)
        ),
        modifier = Modifier.padding(start = 12.dp, top = 26.dp, bottom = 10.dp)
    )
}

@Composable
fun SettingsRow(
    title: String,
    icon: Painter,
    onClick: () -> Unit,
    tile: Color = LessonTheme.sky,
    tint: Color = LessonTheme.primary,
    badge: SettingsBadge? = null,
    showToggle: Boolean = false,
    isToggled: Boolean = false
) {
    val k = LessonTheme.scale()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (pressed) LessonTheme.sky.copy(alpha = 0.6f) else Color.Transparent)
            .clickable(interactionSource = interaction, indication = null) {
                SoundEffectManager.playClickSound()
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size((40 * k).dp)
                .background(tile, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size((22 * k).dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            style = LessonTheme.answer().copy(fontSize = (19 * k).sp),
            modifier = Modifier.weight(1f)
        )

        if (badge != null) {
            Text(
                text = badge.text,
                style = LessonTheme.answer().copy(
                    fontSize = (13 * k).sp,
                    letterSpacing = 0.5.sp,
                    color = badge.ink
                ),
                modifier = Modifier
                    .background(badge.face, CircleShape)
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        if (showToggle) {
            Switch(
                checked = isToggled,
                onCheckedChange = null,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = LessonTheme.primary,
                    checkedBorderColor = LessonTheme.primary,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFD9DEEA),
                    uncheckedBorderColor = Color(0xFFD9DEEA)
                )
            )
        } else {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFA3A9BC),
                modifier = Modifier.size((22 * k).dp)
            )
        }
    }
}
