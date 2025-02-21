package com.alifba.alifba.presenation.home.layout.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.alifba.alifba.R
import com.alifba.alifba.ui_components.theme.darkPink
import com.alifba.alifba.ui_components.theme.lightPink
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager
import com.alifba.alifba.ui_components.widgets.texts.SettingsButton


@SuppressLint("NewApi")
@Composable
fun SettingsScreen(navController: NavController){
    val alifbaFont = FontFamily(
        Font(R.font.more_sugar_regular, FontWeight.SemiBold)
    )
    val context = LocalContext.current

    val privacyPolicyUrl = "https://alifba.xyz/privacy-policy"
    val openPrivacyPolicy = {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
        context.startActivity(intent)
    }
    val termsAndConditionsUrl = "https://alifba.xyz/terms-of-service"
    val openTermsAndConditions = {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(termsAndConditionsUrl))
        context.startActivity(intent)
    }

    val openEmailForBugs = {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("hello@alifba.xyz"))
            putExtra(Intent.EXTRA_SUBJECT,"Reporting bugs")
            putExtra(Intent.EXTRA_TEXT,"Hello, I want to report bug/bugs in the alifba app--")
        }
        context.startActivity(Intent.createChooser(intent, "Send email"))
    }
    val openEmailForFeedback = {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("hello@alifba.xyz"))
            putExtra(Intent.EXTRA_SUBJECT,"User feedback")
            putExtra(Intent.EXTRA_TEXT,"Hello, I want to give you an honest feedback on alifba app--")
        }
        context.startActivity(Intent.createChooser(intent, "Send email"))
    }

    var showDialog by remember { mutableStateOf(false) }

    // State to manage notification toggle
    var isNotificationsEnabled by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
    ) {
        Column(
            Modifier
                .background(white)
                .fillMaxSize()
        )
        {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.goback),
                        contentDescription = "Back",
                        modifier = Modifier
                            .clickable { navController.popBackStack() }
                            .size(48.dp),
                    )
                    Text(
                        text = "Settings",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = navyBlue,
                        fontFamily = alifbaFont
                    )
                    // Empty box for symmetry
                    Box(modifier = Modifier.size(24.dp))
                }
            }

            SettingsButton(
                text = "Account", onClick = {
                    navController.navigate("accountScreen")
                }
            )

            SettingsButton(
                text = "Notifications", onClick = { showDialog=true }
            )
            NotificationDialog(
                showDialog = showDialog,
                onDismiss = { showDialog = false },
                context = context
            )

            SoundToggleButton()
            SettingsButton(
                text = "Privacy Policy", onClick = {openPrivacyPolicy()}
            )
            SettingsButton(
                text = "Terms and Conditions", onClick = {openTermsAndConditions()}
            )
            SettingsButton(
                text = "Report bugs", onClick = {openEmailForBugs()}
            )
            SettingsButton(
                text = "Send Feedback", onClick = {openEmailForFeedback()}
            )

        }


    }
}
@Composable
fun SoundToggleButton() {
    val isEnabled = remember { mutableStateOf(SoundEffectManager.isSoundEnabled) }

    SettingsButton(
        text = "Button Sound",
        showToggle = true,
        isToggled = isEnabled.value,
        onClick = {
            isEnabled.value = !isEnabled.value
            SoundEffectManager.toggleSound(isEnabled.value)
        }
    )
}