package com.alifba.alifba.presenation.login

import android.widget.Toast
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalConfiguration
import com.alifba.alifba.R
import com.alifba.alifba.service.DiscountNotificationScheduler
import com.alifba.alifba.ui_components.theme.black
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.theme.lightCandyGreen
import com.alifba.alifba.ui_components.theme.darkCandyGreen
import com.alifba.alifba.ui_components.theme.lightRed
import com.alifba.alifba.ui_components.theme.darkRed
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
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
import com.alifba.alifba.ui_components.widgets.textFields.GoogleAndAppleSignInButtons
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

enum class LoginEntryMode {
    Landing,
    SignUp,
    SignIn
}

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    navController: NavController,
    entryMode: LoginEntryMode = LoginEntryMode.Landing
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isTablet = screenWidth > 600.dp

    val isSignUpVisible = entryMode == LoginEntryMode.SignUp
    val isLoginVisible = entryMode == LoginEntryMode.SignIn
    val showAuthOptions = entryMode != LoginEntryMode.Landing
    var showEmailForm by remember(entryMode) { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    val errorMessage by remember { mutableStateOf<String?>(null) }
    var resendCooldownSeconds by remember { mutableStateOf(0) }
    var contactEmailInput by remember { mutableStateOf("") }
    var pendingNavigation by remember { mutableStateOf<(() -> Unit)?>(null) }

    val authState by viewModel.authState.collectAsState()
    val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsState(initial = false)
    val needsContactEmailPrompt by viewModel.needsContactEmailPrompt.collectAsState()
    val providerSuggestion by viewModel.providerSuggestion.collectAsState()
    val awaitingVerificationState = authState as? AuthState.AwaitingEmailVerification
    var verificationCheckCountdown by remember { mutableStateOf(5) }

    val handleSignUpVerified: () -> Unit = {
        isLoading = false
        // Keyed by Firebase uid (not email) — matches the identity RevenueCat/Firestore use
        // elsewhere in the app, so a OneSignal subscription can actually be cross-referenced
        // back to an app user.
        viewModel.syncOneSignalForCurrentUser("new_user")
        // Local, on-device 3/14/30-day discount reminders — independent of OneSignal, so
        // this fires even though the OneSignal call above is a separate system.
        DiscountNotificationScheduler.scheduleSignupSequence(context)
        // If the user bought Premium on the onboarding paywall while still anonymous,
        // this merges that anonymous RevenueCat identity into their new Firebase account
        // (dataStoreManager.userId was already saved at account-creation time in signUp()).
        viewModel.syncRevenueCatForCurrentUser()
        navController.navigate("createProfile") {
            popUpTo("login") { inclusive = true }
        }
    }

    // Shared post-sign-in navigation, used regardless of how the OneSignal user_type tag
    // was decided (plain email sign-in always "returning_user"; OAuth needs isNewUser — see
    // handleOAuthSignInResult below).
    val proceedAfterSignIn: (Boolean) -> Unit = { hasProfiles ->
        isLoading = false
        // Also covers Google/Apple sign-in creating a brand-new account (this same callback
        // handles both): merges any anonymous onboarding-paywall purchase into that account.
        viewModel.syncRevenueCatForCurrentUser()
        val destination = if (!hasProfiles) "createProfile" else "profileSelection"
        android.util.Log.d("LoginScreen", "Attempting to navigate to: $destination")
        val navigate = {
            try {
                navController.navigate(destination) {
                    popUpTo("login") { inclusive = true }
                }
            } catch (e: IllegalArgumentException) {
                android.util.Log.e("LoginScreen", "Navigation failed: ${e.message}")
                // Fallback: Navigate to homeScreen directly if profileSelection fails
                navController.navigate("homeScreen") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }

        if (needsContactEmailPrompt) {
            pendingNavigation = navigate
        } else {
            navigate()
        }
    }

    val handleSignInResult: (Boolean) -> Unit = { hasProfiles ->
        // Keyed by Firebase uid (not email) — see handleSignUpVerified above.
        viewModel.syncOneSignalForCurrentUser("returning_user")
        proceedAfterSignIn(hasProfiles)
    }

    // Google/Apple share one entry point for both signup and sign-in, so unlike
    // handleSignUpVerified/handleSignInResult above, this reads Firebase's own isNewUser
    // flag to decide the OneSignal user_type tag instead of assuming "returning_user".
    val handleOAuthSignInResult: (Boolean, Boolean) -> Unit = { hasProfiles, isNewUser ->
        viewModel.syncOneSignalForCurrentUser(if (isNewUser) "new_user" else "returning_user")
        if (isNewUser) {
            DiscountNotificationScheduler.scheduleSignupSequence(context)
        }
        proceedAfterSignIn(hasProfiles)
    }

    val activity = context as? Activity
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLoading = true
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (!idToken.isNullOrBlank()) {
                viewModel.signInWithGoogle(
                    idToken = idToken,
                    onResult = handleOAuthSignInResult,
                    onError = { error ->
                        isLoading = false
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                isLoading = false
                Toast.makeText(context, "Google sign-in failed: missing token.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            isLoading = false
            Toast.makeText(context, "Google sign-in failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.AwaitingEmailVerification) {
            isLoading = false
        }
    }

    LaunchedEffect(awaitingVerificationState?.email, awaitingVerificationState?.isSignUp) {
        if (awaitingVerificationState != null) {
            verificationCheckCountdown = 5
            while (isActive) {
                delay(1000)
                verificationCheckCountdown -= 1
                if (verificationCheckCountdown <= 0) {
                    verificationCheckCountdown = 5
                    viewModel.checkEmailVerified(
                        onVerified = {
                            viewModel.handleEmailVerified(
                                onSignInResult = handleSignInResult,
                                onSignUpVerified = handleSignUpVerified,
                                onError = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        onNotVerified = { }
                    )
                }
            }
        }
    }

    LaunchedEffect(resendCooldownSeconds) {
        if (resendCooldownSeconds > 0) {
            delay(1000)
            resendCooldownSeconds -= 1
        }
    }

    LaunchedEffect(providerSuggestion) {
        if (providerSuggestion != null) {
            showEmailForm = false
        }
    }

    // Define animation specs for smoother transitions
    val fadeInSpec = fadeIn(animationSpec = tween(durationMillis = 500))
    val fadeOutSpec = fadeOut(animationSpec = tween(durationMillis = 300))

    Box(modifier = Modifier.fillMaxSize()) {
            // Background image — no dulling overlay: the "Assalamu Alaikkum" text already
            // carries its own drop shadow for contrast, and the buttons below have solid
            // backgrounds, so nothing here depends on a scrim for legibility.
            Image(
                painter = painterResource(id = R.drawable.login_background),
                contentDescription = "background",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
            )

            // App logo at the top
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = if (isTablet) 64.dp else 32.dp),
//                contentAlignment = Alignment.TopCenter
//            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.alifba_logo_transparent),
//                    contentDescription = "Alifba Logo",
//                    modifier = Modifier
//                        .size(if (isTablet) 160.dp else 120.dp)
//                        .padding(8.dp)
//                )
//            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(if (isTablet) 60.dp else 40.dp)) // Reduced space for better centering

                Text(
                    text = "Assalamu Alaikkum",
                    fontSize = if (isTablet) 38.sp else 32.sp,
                    color = white,
                    fontFamily = FontFamily(
                        Font(
                            R.font.vag_round,
                            FontWeight.Bold
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (isTablet) 24.dp else 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(2.5f, 2.5f),
                            blurRadius = 3f
                        )
                    )
                )

                if (entryMode == LoginEntryMode.Landing) {
                    Column(
                        modifier = Modifier
                            .then(
                                if (isTablet) {
                                    Modifier
                                        .widthIn(max = 500.dp)
                                        .fillMaxWidth(0.8f)
                                } else {
                                    Modifier
                                        .widthIn(max = 300.dp)
                                        .fillMaxWidth(0.85f)
                                }
                            )
                            .padding(bottom = if (isTablet) 24.dp else 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CommonButton(
                            onClick = {
                                navController.navigate(
                                    if (hasCompletedOnboarding) "authOptions/signup" else "onboarding"
                                )
                            },
                            buttonText = "I'm new to Alifba",
                            shadowColor = navyBlue,
                            mainColor = lightNavyBlue,
                            textColor = white,
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isTablet) Modifier.height(80.dp)
                                    else Modifier
                                )
                        )

                        Spacer(modifier = Modifier.height(if (isTablet) 20.dp else 16.dp))

                        CommonButton(
                            onClick = {
                                navController.navigate("authOptions/login")
                            },
                            buttonText = "I'm already with Alifba",
                            shadowColor = navyBlue,
                            mainColor = white,
                            textColor = navyBlue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isTablet) Modifier.height(80.dp)
                                    else Modifier
                                )
                        )
                    }
                }

                AnimatedVisibility(
                    visible = showAuthOptions,
                    enter = fadeInSpec + expandVertically(),
                    exit = fadeOutSpec + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .then(
                                if (isTablet) {
                                    Modifier
                                        .widthIn(max = 500.dp)
                                        .fillMaxWidth(0.8f)
                                } else {
                                    Modifier.fillMaxWidth()
                                }
                            )
                            .padding(horizontal = if (isTablet) 32.dp else 16.dp),
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
                                .padding(if (isTablet) 32.dp else 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AnimatedVisibility(
                                visible = !showEmailForm,
                                enter = fadeInSpec + expandVertically(),
                                exit = fadeOutSpec + shrinkVertically()
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    GoogleAndAppleSignInButtons(
                                        onGoogleClick = {
                                            viewModel.clearProviderSuggestion()
                                            isLoading = true
                                            googleLauncher.launch(googleSignInClient.signInIntent)
                                        },
                                        onAppleClick = {
                                            if (activity == null) {
                                                Toast.makeText(context, "Unable to start Apple sign-in.", Toast.LENGTH_SHORT).show()
                                                return@GoogleAndAppleSignInButtons
                                            }
                                            viewModel.clearProviderSuggestion()
                                            isLoading = true
                                            viewModel.signInWithApple(
                                                activity = activity,
                                                onResult = handleOAuthSignInResult,
                                                onError = { error ->
                                                    isLoading = false
                                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(if (isTablet) 16.dp else 12.dp))

                                    CommonButton(
                                        onClick = {
                                            viewModel.clearProviderSuggestion()
                                            showEmailForm = true
                                        },
                                        buttonText = "Email and password",
                                        shadowColor = navyBlue,
                                        mainColor = lightNavyBlue,
                                        textColor = white,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .then(
                                                if (isTablet) Modifier.height(72.dp)
                                                else Modifier
                                            )
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = showEmailForm,
                                enter = fadeInSpec + expandVertically(),
                                exit = fadeOutSpec + shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = if (isTablet) 24.dp else 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (isSignUpVisible) "Sign Up" else "Log In",
                                        fontSize = if (isTablet) 30.sp else 24.sp,
                                        color = navyBlue,
                                        fontFamily = FontFamily(
                                            Font(
                                                R.font.vag_round,
                                                FontWeight.Bold
                                            )
                                        ),
                                        modifier = Modifier.padding(bottom = if (isTablet) 20.dp else 16.dp)
                                    )

                                    CustomInputField(
                                        value = email,
                                        onValueChange = {
                                            email = it
                                            viewModel.clearProviderSuggestion()
                                        },
                                        labelText = "Email Address",
                                        textColor = black,
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Email,
                                                contentDescription = "Email",
                                                tint = navyBlue
                                            )
                                        },
                                        keyboardType = KeyboardType.Email
                                    )

                                    Spacer(modifier = Modifier.height(if (isTablet) 12.dp else 8.dp))

                                    PasswordInputField(
                                        value = password,
                                        onValueChange = { password = it },
                                        labelText = "Password",
                                        textColor = black
                                    )

                                    if (isLoginVisible) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Forgot Password?",
                                            color = navyBlue,
                                            modifier = Modifier
                                                .clickable { navController.navigate("forgotPassword") }
                                                .align(Alignment.End)
                                        )
                                    }

                                    if (isSignUpVisible) {
                                        Spacer(modifier = Modifier.height(if (isTablet) 12.dp else 8.dp))
                                        PasswordInputField(
                                            value = repeatPassword,
                                            onValueChange = { repeatPassword = it },
                                            labelText = "Confirm Password",
                                            textColor = black,
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
                    }
                }

                // Form Action Buttons
                AnimatedVisibility(
                    visible = showEmailForm,
                    enter = fadeInSpec,
                    exit = fadeOutSpec
                ) {
                    Column(
                        modifier = Modifier
                            .then(
                                if (isTablet) {
                                    Modifier
                                        .widthIn(max = 400.dp)
                                        .fillMaxWidth(0.7f)
                                } else {
                                    Modifier
                                        .widthIn(max = 300.dp)
                                        .fillMaxWidth(0.85f)
                                }
                            )
                            .padding(bottom = if (isTablet) 16.dp else 12.dp, top = if (isTablet) 24.dp else 16.dp),
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
                                        onResult = handleSignInResult,
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
                                .fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(if (isTablet) 16.dp else 12.dp))

                    }
                }

                AnimatedVisibility(
                    visible = showAuthOptions,
                    enter = fadeInSpec,
                    exit = fadeOutSpec
                ) {
                    Column(
                        modifier = Modifier
                            .then(
                                if (isTablet) {
                                    Modifier
                                        .widthIn(max = 500.dp)
                                        .fillMaxWidth(0.8f)
                                } else {
                                    Modifier
                                        .widthIn(max = 300.dp)
                                        .fillMaxWidth(0.85f)
                                }
                            )
                            .padding(bottom = if (isTablet) 16.dp else 12.dp, top = if (isTablet) 16.dp else 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CommonButton(
                            onClick = {
                                if (showEmailForm) {
                                    showEmailForm = false
                                } else {
                                    navController.popBackStack()
                                }
                            },
                            buttonText = "Back",
                            shadowColor = navyBlue,
                            mainColor = white,
                            textColor = navyBlue,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
            }

    }

    if (isLoading) {
        LottieAnimationLoading(
            showDialog = remember { mutableStateOf(true) },
            isTransparentBackground = true,
            autoDismissMillis = null,
            isCancelable = false
        )
    }

    if (needsContactEmailPrompt) {
        AlertDialog(
            onDismissRequest = {
                viewModel.dismissContactEmailPrompt()
                pendingNavigation?.invoke()
                pendingNavigation = null
                contactEmailInput = ""
            },
            title = {
                Text(text = "Add a contact email")
            },
            text = {
                Column {
                    Text(
                        text = "We couldn't retrieve your email from the provider. Add a contact email so we can reach you if needed.",
                        fontSize = 14.sp,
                        color = navyBlue.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    CustomInputField(
                        value = contactEmailInput,
                        onValueChange = { contactEmailInput = it },
                        labelText = "Contact Email",
                        keyboardType = KeyboardType.Email
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (contactEmailInput.isBlank()) {
                            viewModel.dismissContactEmailPrompt()
                            pendingNavigation?.invoke()
                            pendingNavigation = null
                            contactEmailInput = ""
                            return@TextButton
                        }
                        viewModel.updateContactEmail(contactEmailInput.trim()) { success ->
                            if (!success) {
                                Toast.makeText(context, "Failed to save email. You can try again later.", Toast.LENGTH_SHORT).show()
                            }
                            pendingNavigation?.invoke()
                            pendingNavigation = null
                            contactEmailInput = ""
                        }
                    }
                ) {
                    Text(text = "Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.dismissContactEmailPrompt()
                        pendingNavigation?.invoke()
                        pendingNavigation = null
                        contactEmailInput = ""
                    }
                ) {
                    Text(text = "Skip")
                }
            }
        )
    }

    if (awaitingVerificationState != null) {
        EmailVerificationScreenFull(
            email = awaitingVerificationState.email,
            countdownSeconds = verificationCheckCountdown,
            resendCooldownSeconds = resendCooldownSeconds,
            onCheckNow = {
                viewModel.checkEmailVerified(
                    onVerified = {
                        viewModel.handleEmailVerified(
                            onSignInResult = handleSignInResult,
                            onSignUpVerified = handleSignUpVerified,
                            onError = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onNotVerified = {
                        Toast.makeText(
                            context,
                            "Not verified yet. Please check your email.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            },
            onResend = {
                if (resendCooldownSeconds > 0) {
                    return@EmailVerificationScreenFull
                }
                resendCooldownSeconds = 60
                viewModel.resendVerificationEmail(
                    onSuccess = {
                        Toast.makeText(
                            context,
                            "Verification email resent.",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onError = { error ->
                        Toast.makeText(
                            context,
                            "Failed to resend: $error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            },
            onUseDifferentEmail = {
                viewModel.logout()
            },
            onClose = {
                viewModel.logout()
            }
        )
    }
}

@Composable
private fun EmailVerificationScreenFull(
    email: String,
    countdownSeconds: Int,
    resendCooldownSeconds: Int,
    onCheckNow: () -> Unit,
    onResend: () -> Unit,
    onUseDifferentEmail: () -> Unit,
    onClose: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(white)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = navyBlue,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onClose() }
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.verification),
                    contentDescription = "Email Verification",
                    modifier = Modifier.height(if (isTablet) 150.dp else 120.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Verify your email",
                    fontSize = if (isTablet) 28.sp else 22.sp,
                    color = navyBlue,
                    fontFamily = FontFamily(Font(R.font.vag_round, FontWeight.Bold))
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "We sent a verification link to:",
                    fontSize = 16.sp,
                    color = navyBlue.copy(alpha = 0.85f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Text(
                    text = email,
                    fontSize = 16.sp,
                    color = navyBlue,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Open your email app and tap the verification link. Return here and we’ll continue automatically.",
                    fontSize = 14.sp,
                    color = black,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Checking for verification in ${countdownSeconds}s…",
                    fontSize = 13.sp,
                    color = Color.Red,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.Red,
                    strokeWidth = 2.dp
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isTablet) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CommonButton(
                            onClick = onCheckNow,
                            buttonText = "I've verified",
                            shadowColor = darkCandyGreen,
                            mainColor = lightCandyGreen,
                            textColor = white,
                            modifier = Modifier.weight(1f)
                        )

                        CommonButton(
                            onClick = onResend,
                            buttonText = if (resendCooldownSeconds > 0) {
                                "Resend in ${resendCooldownSeconds}s"
                            } else {
                                "Resend"
                            },
                            shadowColor = navyBlue,
                            mainColor = lightNavyBlue,
                            textColor = white,
                            modifier = Modifier.weight(1f)
                        )

                        CommonButton(
                            onClick = onUseDifferentEmail,
                            buttonText = "Use different email",
                            shadowColor = darkRed,
                            mainColor = lightRed,
                            textColor = white,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    CommonButton(
                        onClick = onCheckNow,
                        buttonText = "I've verified",
                        shadowColor = darkCandyGreen,
                        mainColor = lightCandyGreen,
                        textColor = white
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    CommonButton(
                        onClick = onResend,
                        buttonText = if (resendCooldownSeconds > 0) {
                            "Resend in ${resendCooldownSeconds}s"
                        } else {
                            "Resend Verification Email"
                        },
                        shadowColor = navyBlue,
                        mainColor = lightNavyBlue,
                        textColor = white
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    CommonButton(
                        onClick = onUseDifferentEmail,
                        buttonText = "Use a different email",
                        shadowColor = darkRed,
                        mainColor = lightRed,
                        textColor = white
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun EmailVerificationOverlayPreview() {
    EmailVerificationScreenFull(
        email = "parent@example.com",
        countdownSeconds = 5,
        resendCooldownSeconds = 42,
        onCheckNow = {},
        onResend = {},
        onUseDifferentEmail = {},
        onClose = {}
    )
}
