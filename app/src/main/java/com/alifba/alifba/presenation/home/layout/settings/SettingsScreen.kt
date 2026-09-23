package com.alifba.alifba.presenation.home.layout.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.alifba.alifba.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.alifba.alifba.presenation.SubscriptionViewModel
import com.alifba.alifba.ui_components.theme.black
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager

@SuppressLint("NewApi")
@Composable
fun SettingsScreen(navController: NavController) {
    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Bold)
    )
    val context = LocalContext.current

    // URLs for external links
    val privacyPolicyUrl = "https://alifba.xyz/privacy-policy"
    val termsAndConditionsUrl = "https://alifba.xyz/terms-of-service"
    val bugReportUrl = "https://alifba.userjot.com/board/bugs-feedback?"
    val feedbackUrl = "https://alifba.userjot.com/board/bugs-feedback?"
    val featureSuggestionUrl = "https://alifba.userjot.com/board/feature-request?"

    // State variables
    var showNotificationDialog by remember { mutableStateOf(false) }
    val isSoundEnabled = remember { mutableStateOf(SoundEffectManager.isSoundEnabled) }
    val subscriptionViewModel: SubscriptionViewModel = hiltViewModel()
    val isPremium by subscriptionViewModel.isPremium.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background with fun pattern or gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(white)
                )


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with back button and title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.goback),
                    contentDescription = "Back",
                    modifier = Modifier
                        .clickable {
                            SoundEffectManager.playClickSound()
                            navController.popBackStack()
                        }
                        .size(48.dp),
                )

                Text(
                    text = "Settings",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = navyBlue,
                    fontFamily = alifbaFont
                )

                // Empty box for symmetry
                Box(modifier = Modifier.size(48.dp))
            }

            // Settings categories with icons and colorful cards
            Spacer(modifier = Modifier.height(8.dp))

            // User settings category
//            SettingsCategoryHeader(title = "Your Profile", icon = R.drawable.profile_icon)

            SettingsCard {
                Column {
                    SettingsButton(
                        text = "Account",
                        icon = R.drawable.account,
                        onClick = {
                            SoundEffectManager.playClickSound()
                            navController.navigate("accountScreen")
                        }
                    )

                    Divider(
                        color = black.copy(alpha = 0.5f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    SubscriptionSettingsButton(
                        isPremium = isPremium,
                        onClick = {
                            SoundEffectManager.playClickSound()
                            navController.navigate("subscription")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App settings category
            SettingsCategoryHeader(title = "App Settings")

            SettingsCard {
                Column {
                    SettingsButton(
                        text = "Notifications + Reminders",
                        icon = R.drawable.notification,
                        onClick = {
                            SoundEffectManager.playClickSound()
                            showNotificationDialog = true
                        }
                    )

                    Divider(
                        color = black.copy(alpha = 0.5f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    SettingsButton(
                        text = "Button Sound",
                        icon = R.drawable.sound,
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
                }
            }

//            Spacer(modifier = Modifier.height(24.dp))
//
//            // Developer/Test category (Premium Toggle)
//            SettingsCategoryHeader(title = "Premium (Test)")
//
//            SettingsCard {
//                SettingsButton(
//                    text = if (isPremium) "Deactivate Premium" else "Activate Premium",
//                    icon = R.drawable.account,
//                    showToggle = true,
//                    isToggled = isPremium,
//                    onClick = {
//                        SoundEffectManager.playClickSound()
//                        subscriptionViewModel.togglePremium()
//                    }
//                )
//            }
//
//            Spacer(modifier = Modifier.height(24.dp))

            // Help category
            SettingsCategoryHeader(title = "Help Us Improve")

            SettingsCard {
                Column {
                    SettingsButton(
                        text = "Report Bugs",
                        icon = R.drawable.bug,
                        onClick = {
                            SoundEffectManager.playClickSound()
                            openUrl(context, bugReportUrl)
                        }
                    )

                    Divider(
                        color = black.copy(alpha = 0.5f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    SettingsButton(
                        text = "Send Feedback",
                        icon = R.drawable.feedback,
                        onClick = {
                            SoundEffectManager.playClickSound()
                            openUrl(context, feedbackUrl)
                        }
                    )

                    Divider(
                        color = black.copy(alpha = 0.5f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    SettingsButton(
                        text = "Suggest a Feature",
                        icon = R.drawable.suggestfeature,
                        onClick = {
                            SoundEffectManager.playClickSound()
                            openUrl(context, featureSuggestionUrl)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Legal category
            SettingsCategoryHeader(title = "Legal")

            SettingsCard {
                Column {
                    SettingsButton(
                        text = "Privacy Policy",
                        icon = R.drawable.privacy_policy,
                        onClick = {
                            SoundEffectManager.playClickSound()
                            openUrl(context, privacyPolicyUrl)
                        }
                    )

                    Divider(
                        color = black.copy(alpha = 0.5f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    SettingsButton(
                        text = "Terms and Conditions",
                        icon = R.drawable.terms_and_conditions,
                        onClick = {
                            SoundEffectManager.playClickSound()
                            openUrl(context, termsAndConditionsUrl)
                        }
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

@Composable
fun SettingsCategoryHeader(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
    ) {

        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = navyBlue,
            fontFamily = FontFamily(Font(R.font.vag_round, FontWeight.Bold))
        )
    }
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = white
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        content()
    }
}

@Composable
fun SettingsButton(
    text: String,
    icon: Int,
    onClick: () -> Unit,
    showToggle: Boolean = false,
    isToggled: Boolean = false
) {
    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Bold)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Text
        Text(
            text = text,
            style = LocalTextStyle.current.merge(
                TextStyle(
                    lineHeight = 1.5.em,
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    ),
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.None
                    )
                )
            ),
            color = Color.DarkGray,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = alifbaFont,
            modifier = Modifier.weight(1f)
        )

        if (showToggle) {
            Switch(
                checked = isToggled,
                onCheckedChange = { onClick() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = white,
                    checkedTrackColor = navyBlue,
                    checkedBorderColor = navyBlue,
                    uncheckedThumbColor = white,
                    uncheckedTrackColor = Color.Gray,
                    uncheckedBorderColor = black
                )
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.rightarrowsettings),
                contentDescription = "Arrow Right",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SubscriptionSettingsButton(
    isPremium: Boolean,
    onClick: () -> Unit
) {
    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Bold)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.premium),
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "Subscription",
            style = LocalTextStyle.current.merge(
                TextStyle(
                    lineHeight = 1.5.em,
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    ),
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.None
                    )
                )
            ),
            color = Color.DarkGray,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = alifbaFont,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .background(
                    color = Color(0xFF22C55E),
                    shape = RoundedCornerShape(999.dp)
                )
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isPremium) "PREMIUM" else "FREE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = white,
                fontFamily = alifbaFont
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Image(
            painter = painterResource(id = R.drawable.rightarrowsettings),
            contentDescription = "Arrow Right",
            modifier = Modifier.size(20.dp)
        )
    }
}
