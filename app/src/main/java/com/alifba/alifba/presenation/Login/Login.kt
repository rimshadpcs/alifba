package com.alifba.alifba.presenation.Login

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alifba.alifba.R
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alifba.alifba.ui_components.dialogs.LottieAnimationLoading
import com.alifba.alifba.ui_components.widgets.textFields.CustomInputField
import com.alifba.alifba.ui_components.widgets.textFields.PasswordInputField
import kotlinx.coroutines.flow.first
@Composable
fun LoginScreen(viewModel: AuthViewModel = hiltViewModel(), navController: NavController) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    var isSignUpVisible by remember { mutableStateOf(false) }
    var isLoginVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) } // Loading state
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.login_background),
            contentDescription = "background",
            contentScale = ContentScale.FillHeight,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(visible = isSignUpVisible || isLoginVisible, enter = fadeIn(), exit = fadeOut()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 50.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isSignUpVisible) "Sign Up" else "Log In",
                        fontSize = 24.sp,
                        color = navyBlue,
                        fontFamily = FontFamily(Font(R.font.more_sugar_regular, FontWeight.SemiBold))
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    CustomInputField(value = email, onValueChange = { email = it }, labelText = "Enter Email")
                    PasswordInputField(value = password, onValueChange = { password = it }, labelText = "Enter Password")

                    if (isSignUpVisible) {
                        PasswordInputField(value = repeatPassword, onValueChange = { repeatPassword = it }, labelText = "Repeat Password")
                    }

                    errorMessage?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            AnimatedVisibility(visible = !isSignUpVisible && !isLoginVisible, enter = fadeIn(), exit = fadeOut()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CommonButton(
                        onClick = { isSignUpVisible = true },
                        buttonText = "I'm new to Alifba",
                        shadowColor = navyBlue,
                        mainColor = lightNavyBlue,
                        textColor = white
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CommonButton(
                        onClick = { isLoginVisible = true },
                        buttonText = "I'm already with Alifba",
                        shadowColor = navyBlue,
                        mainColor = white,
                        textColor = navyBlue
                    )
                }
            }

            AnimatedVisibility(visible = isSignUpVisible || isLoginVisible, enter = fadeIn(), exit = fadeOut()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CommonButton(
                        onClick = {
                            isLoading = true // Show loading animation
                            if (isSignUpVisible) {
                                if (password == repeatPassword) {
                                    viewModel.signUp(email, password,
                                        onSuccess = {
                                            isLoading = false
                                            navController.navigate("onboarding") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        },
                                        onError = { error ->
                                            isLoading = false
                                            Toast.makeText(context, "Sign Up Failed: $error", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                } else {
                                    isLoading = false
                                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                viewModel.signIn(email, password,
                                    onResult = { hasProfiles ->
                                        isLoading = false
                                        navController.navigate(if (!hasProfiles) "createProfile" else "homeScreen") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    },
                                    onError = { error ->
                                        isLoading = false
                                        Toast.makeText(context, "Login Failed: $error", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        buttonText = if (isSignUpVisible) "Sign Up" else "Log In",
                        shadowColor = navyBlue,
                        mainColor = lightNavyBlue,
                        textColor = white
                    )


                    Spacer(modifier = Modifier.height(8.dp))
                    CommonButton(
                        onClick = {
                            isSignUpVisible = false
                            isLoginVisible = false
                        },
                        buttonText = "Back",
                        shadowColor = navyBlue,
                        mainColor = white,
                        textColor = navyBlue
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
