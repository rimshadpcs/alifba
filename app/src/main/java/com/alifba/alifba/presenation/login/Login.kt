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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alifba.alifba.ui_components.dialogs.LottieAnimationLoading
import com.alifba.alifba.ui_components.widgets.DotLottieView
import com.alifba.alifba.ui_components.widgets.AuthBadge
import com.alifba.alifba.ui_components.widgets.AuthBanner
import com.alifba.alifba.ui_components.widgets.AuthCard
import com.alifba.alifba.ui_components.widgets.AuthOrDivider
import com.alifba.alifba.ui_components.widgets.AuthPillButton
import com.alifba.alifba.ui_components.widgets.AuthPillKind
import com.alifba.alifba.ui_components.widgets.AuthHintStyle
import com.alifba.alifba.ui_components.widgets.AuthSwitchPill
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.alifba.alifba.ui_components.widgets.AuthTextField
import com.alifba.alifba.ui_components.widgets.AuthTheme
import com.alifba.alifba.ui_components.widgets.LessonCircleButton
import com.alifba.alifba.ui_components.widgets.LessonPrimaryButton
import com.alifba.alifba.ui_components.widgets.LessonSecondaryButton
import com.alifba.alifba.ui_components.widgets.LessonTheme
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.em
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
private fun LandingContent(
    isTablet: Boolean,
    onGetStarted: () -> Unit,
    onLogIn: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val k = LessonTheme.scale()
    // Scales the hero down on short phones so the CTA never gets pushed off-screen.
    val mascotSize = if (isTablet) 320.dp else (configuration.screenHeightDp * 0.27f).dp.coerceIn(150.dp, 240.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = (configuration.screenHeightDp * if (isTablet) 0.1f else 0.06f).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .padding(horizontal = LessonTheme.margin),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // The waving Moon stays a Lottie animation
            DotLottieView(
                name = "moonwave",
                modifier = Modifier.size(mascotSize)
            )
            Spacer(modifier = Modifier.height(if (isTablet) 12.dp else 4.dp))
            Image(
                painter = painterResource(id = R.drawable.alifba_wordmark),
                contentDescription = "Alifba",
                contentScale = ContentScale.Fit,
                modifier = Modifier.width(if (isTablet) 340.dp else 210.dp)
            )
            Text(
                text = "Assalamu alaikum!",
                style = LessonTheme.question().copy(fontSize = (28 * k).sp, textAlign = TextAlign.Center),
                modifier = Modifier.padding(top = 14.dp)
            )
            Spacer(modifier = Modifier.height(34.dp))
            LessonPrimaryButton(title = "Get started", onClick = onGetStarted)
            Spacer(modifier = Modifier.height(12.dp))
            LessonSecondaryButton(title = "I already have an account", onClick = onLogIn)
        }
    }
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

    // Dismissable banner (replaces the system toast): the id re-arms the auto-dismiss timer when
    // the same message is shown twice in a row.
    var banner by remember { mutableStateOf<Pair<Int, String>?>(null) }
    var lastBannerText by remember { mutableStateOf("") }
    val showBanner: (String) -> Unit = { text -> banner = Pair((banner?.first ?: 0) + 1, text) }
    val cancelledMessage = "Sign-in was cancelled. Try again, or pick another option."
    val showAuthError: (String) -> Unit = { text ->
        showBanner(if (text.contains("cancel", ignoreCase = true)) cancelledMessage else text)
    }
    LaunchedEffect(banner?.first) {
        if (banner != null) {
            delay(4500)
            banner = null
        }
    }

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
                        showAuthError(error)
                    }
                )
            } else {
                isLoading = false
                showBanner("Google sign-in failed: missing token.")
            }
        } catch (e: ApiException) {
            isLoading = false
            // 12501 = the user closed the Google sheet
            if (e.statusCode == 12501) {
                showBanner(cancelledMessage)
            } else {
                showBanner("Google sign-in failed: ${e.localizedMessage}")
            }
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
                                    showBanner(error)
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

    val startAppleSignIn: () -> Unit = {
        if (activity == null) {
            showBanner("Unable to start Apple sign-in.")
        } else {
            viewModel.clearProviderSuggestion()
            isLoading = true
            viewModel.signInWithApple(
                activity = activity,
                onResult = handleOAuthSignInResult,
                onError = { error ->
                    isLoading = false
                    showAuthError(error)
                }
            )
        }
    }
    val startGoogleSignIn: () -> Unit = {
        viewModel.clearProviderSuggestion()
        isLoading = true
        googleLauncher.launch(googleSignInClient.signInIntent)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.login_background),
                contentDescription = "background",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
            )

            if (entryMode == LoginEntryMode.Landing) {
                LandingContent(
                    isTablet = isTablet,
                    onGetStarted = {
                        navController.navigate(
                            if (hasCompletedOnboarding) "authOptions/signup" else "onboarding"
                        )
                    },
                    onLogIn = { navController.navigate("authOptions/login") }
                )
            } else {
                val k = LessonTheme.scale()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 12.dp)
                    ) {
                        LessonCircleButton(
                            iconRes = R.drawable.backarrow,
                            contentDescription = "Back",
                            onClick = {
                                if (showEmailForm) {
                                    showEmailForm = false
                                } else {
                                    navController.popBackStack()
                                }
                            }
                        )
                    }

                    // The waving Moon stays a Lottie animation
                    DotLottieView(
                        name = "moonwave",
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(if (isTablet) 150.dp else 110.dp)
                    )

                    AuthCard(
                        modifier = Modifier
                            .widthIn(max = 560.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 17.dp)
                    ) {
                        if (isLoginVisible) {
                            Text(
                                text = "Welcome back",
                                style = LessonTheme.question().copy(fontSize = (28 * k).sp, textAlign = TextAlign.Center),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )

                            AuthTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    viewModel.clearProviderSuggestion()
                                },
                                label = "Email",
                                placeholder = "you@example.com",
                                icon = Icons.Outlined.Email,
                                keyboardType = KeyboardType.Email
                            )

                            AuthTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = "Password",
                                placeholder = "Your password",
                                icon = Icons.Outlined.Lock,
                                isPassword = true,
                                imeAction = androidx.compose.ui.text.input.ImeAction.Done
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Text(
                                    text = "Forgot password?",
                                    style = LessonTheme.answer().copy(
                                        fontSize = (17 * k).sp,
                                        color = LessonTheme.primaryEdge
                                    ),
                                    modifier = Modifier.clickable { navController.navigate("forgotPassword") }
                                )
                            }

                            LessonPrimaryButton(
                                title = "Log in",
                                onClick = {
                                    isLoading = true
                                    viewModel.signIn(email, password,
                                        onResult = handleSignInResult,
                                        onError = { error ->
                                            isLoading = false
                                            showBanner("Login Failed: $error")
                                        }
                                    )
                                }
                            )

                            AuthOrDivider(text = "or log in with")

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AuthPillButton(
                                    title = "Apple",
                                    kind = AuthPillKind.Apple,
                                    modifier = Modifier.weight(1f),
                                    enabled = !isLoading,
                                    onClick = startAppleSignIn
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.apple),
                                        contentDescription = null,
                                        modifier = Modifier.size((22 * k).dp)
                                    )
                                }
                                AuthPillButton(
                                    title = "Google",
                                    kind = AuthPillKind.Light,
                                    modifier = Modifier.weight(1f),
                                    enabled = !isLoading,
                                    onClick = startGoogleSignIn
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.google),
                                        contentDescription = null,
                                        modifier = Modifier.size((22 * k).dp)
                                    )
                                }
                            }
                        } else if (!showEmailForm) {
                            Text(
                                text = if (isSignUpVisible) "Create a parent account" else "Welcome back",
                                style = LessonTheme.question().copy(fontSize = (28 * k).sp, textAlign = TextAlign.Center)
                            )
                            Text(
                                text = if (isSignUpVisible) {
                                    "Save your children's progress and use Alifba on all your devices."
                                } else {
                                    "Log in to pick up where your children left off."
                                },
                                style = LessonTheme.story().copy(
                                    fontSize = (19 * k).sp,
                                    color = AuthTheme.secondaryText,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 1.3.em
                                ),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            AuthPillButton(
                                title = "Continue with Apple",
                                kind = AuthPillKind.Apple,
                                enabled = !isLoading,
                                onClick = startAppleSignIn
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.apple),
                                    contentDescription = null,
                                    modifier = Modifier.size((22 * k).dp)
                                )
                            }

                            Box(modifier = Modifier.fillMaxWidth()) {
                                AuthPillButton(
                                    title = "Continue with Google",
                                    kind = AuthPillKind.Light,
                                    enabled = !isLoading,
                                    onClick = startGoogleSignIn
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.google),
                                        contentDescription = null,
                                        modifier = Modifier.size((22 * k).dp)
                                    )
                                }
                                if (isSignUpVisible) {
                                    AuthBadge(
                                        text = "Best for multiple devices",
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 4.dp, y = (-10).dp)
                                    )
                                }
                            }

                            AuthOrDivider()

                            AuthPillButton(
                                title = "Continue with email",
                                kind = AuthPillKind.Light,
                                onClick = {
                                    viewModel.clearProviderSuggestion()
                                    showEmailForm = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Email,
                                    contentDescription = null,
                                    tint = LessonTheme.primary,
                                    modifier = Modifier.size((22 * k).dp)
                                )
                            }

                            val linkStyle = TextLinkStyles(
                                SpanStyle(
                                    color = LessonTheme.primaryEdge,
                                    textDecoration = TextDecoration.Underline
                                )
                            )
                            Text(
                                text = buildAnnotatedString {
                                    append("By continuing you agree to our ")
                                    withLink(LinkAnnotation.Url("https://alifba.xyz/terms-of-service", linkStyle)) {
                                        append("Terms")
                                    }
                                    append(" and ")
                                    withLink(LinkAnnotation.Url("https://alifba.xyz/privacy-policy", linkStyle)) {
                                        append("Privacy Policy")
                                    }
                                    append(".")
                                },
                                style = LessonTheme.story().copy(
                                    fontSize = (14 * k).sp,
                                    color = AuthTheme.secondaryText,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 1.3.em
                                ),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        } else {
                            Text(
                                text = "Create your account",
                                style = LessonTheme.question().copy(fontSize = (26 * k).sp, textAlign = TextAlign.Center),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )

                            AuthTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    viewModel.clearProviderSuggestion()
                                },
                                label = "Email",
                                placeholder = "you@example.com",
                                icon = Icons.Outlined.Email,
                                keyboardType = KeyboardType.Email
                            )

                            AuthTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = "Password",
                                placeholder = "Create a password",
                                icon = Icons.Outlined.Lock,
                                isPassword = true,
                                hint = "At least 8 characters",
                                hintStyle = if (password.length >= 8) AuthHintStyle.Good else AuthHintStyle.Neutral
                            )

                            val passwordsMismatch = repeatPassword.isNotEmpty() && repeatPassword != password
                            AuthTextField(
                                value = repeatPassword,
                                onValueChange = { repeatPassword = it },
                                label = "Confirm password",
                                placeholder = "Type it again",
                                icon = Icons.Outlined.Lock,
                                isPassword = true,
                                isError = passwordsMismatch,
                                hint = if (passwordsMismatch) "Passwords don't match yet" else null,
                                hintStyle = AuthHintStyle.Bad,
                                imeAction = androidx.compose.ui.text.input.ImeAction.Done
                            )

                            LessonPrimaryButton(
                                title = "Create account",
                                modifier = Modifier.padding(top = 4.dp),
                                onClick = {
                                    when {
                                        email.isBlank() || password.isEmpty() || repeatPassword.isEmpty() ->
                                            showBanner("Please fill all fields")
                                        password.length < 8 ->
                                            showBanner("Password must be at least 8 characters")
                                        password != repeatPassword ->
                                            showBanner("Passwords don't match yet")
                                        else -> {
                                            isLoading = true
                                            viewModel.signUp(email, password,
                                                onSuccess = {
                                                    isLoading = false
                                                },
                                                onError = { error ->
                                                    isLoading = false
                                                    showBanner("Sign Up Failed: $error")
                                                }
                                            )
                                        }
                                    }
                                }
                            )

                            val linkStyle = TextLinkStyles(
                                SpanStyle(
                                    color = LessonTheme.primaryEdge,
                                    textDecoration = TextDecoration.Underline
                                )
                            )
                            Text(
                                text = buildAnnotatedString {
                                    append("By continuing you agree to our ")
                                    withLink(LinkAnnotation.Url("https://alifba.xyz/terms-of-service", linkStyle)) {
                                        append("Terms")
                                    }
                                    append(" and ")
                                    withLink(LinkAnnotation.Url("https://alifba.xyz/privacy-policy", linkStyle)) {
                                        append("Privacy Policy")
                                    }
                                    append(".")
                                },
                                style = LessonTheme.story().copy(
                                    fontSize = (14 * k).sp,
                                    color = AuthTheme.secondaryText,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 1.3.em
                                )
                            )
                        }
                    }

                    // On the grass: switch between creating an account and logging in
                    if (isLoginVisible) {
                        Spacer(modifier = Modifier.height(24.dp))
                        AuthSwitchPill(
                            prefix = "New to Alifba?",
                            link = "Create an account",
                            onClick = {
                                navController.navigate("authOptions/signup") {
                                    popUpTo("authOptions/login") { inclusive = true }
                                }
                            }
                        )
                    } else if (showEmailForm) {
                        Spacer(modifier = Modifier.height(24.dp))
                        AuthSwitchPill(
                            prefix = "Already have an account?",
                            link = "Log in",
                            onClick = {
                                navController.navigate("authOptions/login") {
                                    popUpTo("authOptions/signup") { inclusive = true }
                                }
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
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
                                showBanner("Failed to save email. You can try again later.")
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
                                showBanner(error)
                            }
                        )
                    },
                    onNotVerified = {
                        showBanner("Not verified yet. Please check your email.")
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
                        showBanner("Verification email resent.")
                    },
                    onError = { error ->
                        showBanner("Failed to resend: $error")
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

        // Banner sits above everything, including the email-verification overlay
        banner?.let { lastBannerText = it.second }
        AnimatedVisibility(
            visible = banner != null,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            AuthBanner(
                message = lastBannerText,
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 17.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 96.dp),
                onClose = { banner = null }
            )
        }
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
