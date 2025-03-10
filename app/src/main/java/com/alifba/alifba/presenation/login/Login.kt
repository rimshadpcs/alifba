package com.alifba.alifba.presenation.login

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alifba.alifba.R
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathSegment
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsProperties.ImeAction
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alifba.alifba.ui_components.dialogs.LottieAnimationLoading
import com.alifba.alifba.ui_components.widgets.textFields.CustomInputField
import com.alifba.alifba.ui_components.widgets.textFields.PasswordInputField
import com.onesignal.OneSignal

@Composable
fun LoginScreen(viewModel: AuthViewModel = hiltViewModel(), navController: NavController) {
    val context = LocalContext.current
    var isSignUpVisible by remember { mutableStateOf(false) }
    var isLoginVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    val errorMessage by remember { mutableStateOf<String?>(null) }
    var showEmailVerification by remember { mutableStateOf(false) }

    // Define animation specs for smoother transitions
    val fadeInSpec = fadeIn(animationSpec = tween(durationMillis = 500))
    val fadeOutSpec = fadeOut(animationSpec = tween(durationMillis = 300))

    if (showEmailVerification) {
        EmailVerificationScreen(
            viewModel = viewModel,
            navController = navController,
            email = email
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background image with overlay gradient for better text visibility
            Image(
                painter = painterResource(id = R.drawable.login_background),
                contentDescription = "background",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
            )

            // Semi-transparent gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                navyBlue.copy(alpha = 0.3f)
                            )
                        )
                    )
            )

            // App logo at the top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Image(
                    painter = painterResource(id = R.drawable.alifba_logo_transparent),
                    contentDescription = "Alifba Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(8.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(120.dp)) // Space for the logo

                // Login/SignUp Form
                AnimatedVisibility(
                    visible = isSignUpVisible || isLoginVisible,
                    enter = fadeInSpec + expandVertically(),
                    exit = fadeOutSpec + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = white.copy(alpha = 0.95f)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 6.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isSignUpVisible) "Sign Up" else "Log In",
                                fontSize = 28.sp,
                                color = navyBlue,
                                fontFamily = FontFamily(
                                    Font(
                                        R.font.more_sugar_regular,
                                        FontWeight.Bold
                                    )
                                ),
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            CustomInputField(
                                value = email,
                                onValueChange = { email = it },
                                labelText = "Email Address",
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "Email",
                                        tint = navyBlue
                                    )
                                },
                                keyboardType = KeyboardType.Email
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            PasswordInputField(
                                value = password,
                                onValueChange = { password = it },
                                labelText = "Password"
                            )

                            if (isSignUpVisible) {
                                Spacer(modifier = Modifier.height(8.dp))
                                PasswordInputField(
                                    value = repeatPassword,
                                    onValueChange = { repeatPassword = it },
                                    labelText = "Confirm Password",

                                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                                )
                            }

                            errorMessage?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Initial Buttons
                AnimatedVisibility(
                    visible = !isSignUpVisible && !isLoginVisible,
                    enter = fadeInSpec,
                    exit = fadeOutSpec
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Assalamu Alaikkum",
                            fontSize = 32.sp,
                            color = white,
                            fontFamily = FontFamily(
                                Font(
                                    R.font.more_sugar_regular,
                                    FontWeight.Bold
                                )
                            ),
                            modifier = Modifier.padding(bottom = 16.dp),
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.Black,
                                    offset = Offset(2.5f, 2.5f),
                                    blurRadius = 3f
                                )
                            )
                        )


                        CommonButton(
                            onClick = { isSignUpVisible = true },
                            buttonText = "I'm new to Alifba",
                            shadowColor = navyBlue,
                            mainColor = lightNavyBlue,
                            textColor = white,
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        CommonButton(
                            onClick = { isLoginVisible = true },
                            buttonText = "I'm already with Alifba",
                            shadowColor = navyBlue,
                            mainColor = white,
                            textColor = navyBlue,
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                        )
                    }
                }

                // Form Action Buttons
                AnimatedVisibility(
                    visible = isSignUpVisible || isLoginVisible,
                    enter = fadeInSpec,
                    exit = fadeOutSpec
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp, top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CommonButton(
                            onClick = {
                                isLoading = true
                                if (isSignUpVisible) {
                                    if (password == repeatPassword) {
                                        viewModel.signUp(email, password,
                                            onSuccess = {
                                                isLoading = false
                                                showEmailVerification = true
                                                OneSignal.login(email)
                                                OneSignal.User.addTag("user_type", "new_user")
                                                OneSignal.User.addTag("send_welcome_email", "true")
                                                OneSignal.User.addEmail(email)
                                            },
                                            onError = { error ->
                                                isLoading = false
                                                Toast.makeText(
                                                    context,
                                                    "Sign Up Failed: $error",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        )
                                    } else {
                                        isLoading = false
                                        Toast.makeText(
                                            context,
                                            "Passwords do not match",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    viewModel.signIn(email, password,
                                        onResult = { hasProfiles ->
                                            isLoading = false
                                            OneSignal.login(email)
                                            OneSignal.User.addTag("user_type", "returning_user")
                                            OneSignal.User.addEmail(email)
                                            navController.navigate(if (!hasProfiles) "createProfile" else "homeScreen") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        },
                                        onError = { error ->
                                            isLoading = false
                                            Toast.makeText(
                                                context,
                                                "Login Failed: $error",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            },
                            buttonText = if (isSignUpVisible) "Sign Up" else "Log In",
                            shadowColor = navyBlue,
                            mainColor = lightNavyBlue,
                            textColor = white,
                            modifier = Modifier
                                .fillMaxWidth(0.8f)

                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        CommonButton(
                            onClick = {
                                isSignUpVisible = false
                                isLoginVisible = false
                            },
                            buttonText = "Back",
                            shadowColor = navyBlue,
                            mainColor = white,
                            textColor = navyBlue,
                            modifier = Modifier
                                .fillMaxWidth(0.8f)

                        )
                    }
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
}