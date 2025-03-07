package com.alifba.alifba.presenation.login

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alifba.alifba.R
import com.alifba.alifba.ui_components.dialogs.LottieAnimationLoading
import com.alifba.alifba.ui_components.theme.darkCandyGreen
import com.alifba.alifba.ui_components.theme.darkRed
import com.alifba.alifba.ui_components.theme.lightCandyGreen
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.lightRed
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.onesignal.OneSignal
import kotlinx.coroutines.delay

@Composable
fun EmailVerificationScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    navController: NavController,
    email: String
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var isCheckingVerification by remember { mutableStateOf(false) }
    var verificationSent by remember { mutableStateOf(true) }

    // Auto-check verification status every 10 seconds
    LaunchedEffect(isCheckingVerification) {
        if (isCheckingVerification) {
            delay(2000) // Show loading for at least 2 seconds
            viewModel.checkEmailVerified(
                onVerified = {
                    isCheckingVerification = false
                    isLoading = false
                    navController.navigate("onboarding") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNotVerified = {
                    isCheckingVerification = false
                    isLoading = false
                    Toast.makeText(
                        context,
                        "Email not verified yet. Please check your inbox and click the verification link.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(white)) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Empty spacer at the top for balance
            Spacer(modifier = Modifier.height(16.dp))

            // Content area (top section)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Envelope image
                Image(
                    painter = painterResource(id = R.drawable.verification),
                    contentDescription = "Email Verification",
                    modifier = Modifier.height(120.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Email Verification Required",
                    fontSize = 24.sp,
                    color = navyBlue,
                    fontFamily = FontFamily(Font(R.font.more_sugar_regular, FontWeight.SemiBold)),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "We've sent a verification email to:",
                    fontSize = 16.sp,
                    color = navyBlue,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = email,
                    fontSize = 18.sp,
                    color = navyBlue,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Please check your inbox and click the verification link before continuing.",
                    fontSize = 16.sp,
                    color = navyBlue,
                    textAlign = TextAlign.Center
                )
            }

            // Buttons area (bottom section)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                CommonButton(
                    onClick = {
                        isLoading = true
                        isCheckingVerification = true
                    },
                    buttonText = "I've Verified My Email",
                    shadowColor = navyBlue,
                    mainColor = lightNavyBlue,
                    textColor = white
                )

                Spacer(modifier = Modifier.height(16.dp))

                CommonButton(
                    onClick = {
                        isLoading = true
                        viewModel.resendVerificationEmail(
                            onSuccess = {
                                isLoading = false
                                verificationSent = true
                                Toast.makeText(
                                    context,
                                    "Verification email resent. Please check your inbox.",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            onError = { error ->
                                isLoading = false
                                Toast.makeText(
                                    context,
                                    "Failed to resend: $error",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    },
                    buttonText = "Resend Verification Email",
                    shadowColor = darkCandyGreen,
                    mainColor = lightCandyGreen,
                    textColor = white
                )

                Spacer(modifier = Modifier.height(16.dp))

                CommonButton(
                    onClick = {
                        // Log the user out and return to login screen
                        viewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    buttonText = "Back to Login",
                    shadowColor = darkRed,
                    mainColor = lightRed,
                    textColor = white
                )
            }
        }
    }

    if (isLoading) {
        LottieAnimationLoading(
            showDialog = remember { mutableStateOf(true) },
            lottieFileRes = R.raw.loading_lottie,
            isTransparentBackground = true
        )
    }
}