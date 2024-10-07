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
import com.alifba.alifba.ui_components.widgets.textFields.CustomInputField
import com.alifba.alifba.ui_components.widgets.textFields.PasswordInputField

@Composable
fun LoginScreen(viewModel: AuthViewModel = hiltViewModel(),navController: NavController) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    var isSignUpVisible by remember { mutableStateOf(false) }
    var isLoginVisible by remember { mutableStateOf(false) }

    val alifbaFont = FontFamily(
        Font(R.font.more_sugar_regular, FontWeight.SemiBold)
    )



    var email by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.login_background),
            contentDescription = "background",
            contentScale = ContentScale.FillHeight,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween, // For top form and bottom buttons
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Form Section (Top of the screen)
            AnimatedVisibility(
                visible = isSignUpVisible || isLoginVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 50.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isSignUpVisible) "Sign Up" else "Log In",
                        fontSize = 24.sp,
                        color = navyBlue,
                        fontFamily = alifbaFont
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom Input Field for Email
                    CustomInputField(
                        value = email,
                        onValueChange = { email = it },
                        labelText = "Enter Email"
                    )

                    // Custom Input Field for Password
                    PasswordInputField(
                        value = password,
                        onValueChange = { password = it },
                        labelText = "Enter Password"
                    )

                    // Show repeat password field only for sign-up
                    if (isSignUpVisible) {
                        PasswordInputField(
                            value = repeatPassword,
                            onValueChange = { repeatPassword = it },
                            labelText = "Repeat Password"
                        )
                    }

                    errorMessage?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                   // GoogleAndAppleSignInButtons()
                }
            }

            // Buttons Section (Bottom of the screen)
            AnimatedVisibility(
                visible = !isSignUpVisible && !isLoginVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
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

            // Submit Buttons (For Sign Up / Login)
            AnimatedVisibility(
                visible = isSignUpVisible || isLoginVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CommonButton(
                        onClick = {
                            if (isSignUpVisible) {
                                // Handle sign-up logic
                                if (password == repeatPassword) {
                                    viewModel.signUp(email, password)
                                    Toast.makeText(context, "Sign Up Successful!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // Handle sign-in logic
                                viewModel.signIn(email, password)
                            }
                        },
                        buttonText = if (isSignUpVisible) "Sign Up" else "Log In",
                        shadowColor = navyBlue,
                        mainColor = lightNavyBlue,
                        textColor = white
                    )

                    // Handle the auth state changes
                    LaunchedEffect(authState) {
                        when (authState) {
                            is AuthState.Success -> {
                                Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                                // Navigate to the home screen
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                            is AuthState.NeedsProfile -> {
                                Toast.makeText(context, "Please complete your profile.", Toast.LENGTH_SHORT).show()
                                // Navigate to the profile registration screen
                                navController.navigate("createProfile") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                            is AuthState.Error -> {
                                Toast.makeText(context, "Login Failed: ${(authState as AuthState.Error).message}", Toast.LENGTH_SHORT).show()
                            }
                            else -> {}
                        }
                    }


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
}
