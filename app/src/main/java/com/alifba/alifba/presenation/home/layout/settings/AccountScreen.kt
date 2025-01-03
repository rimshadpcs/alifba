package com.alifba.alifba.presenation.home.layout.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.alifba.alifba.R
import com.alifba.alifba.presenation.Login.AuthViewModel
import com.alifba.alifba.ui_components.theme.darkPink
import com.alifba.alifba.ui_components.theme.lightPink
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AccountScreen(authViewModel: AuthViewModel) {

    val userProfile = authViewModel.userProfileState.collectAsState().value
    val alifbaFont = FontFamily(
        Font(R.font.more_sugar_regular, FontWeight.SemiBold)
    )

    LaunchedEffect(Unit) {
        authViewModel.fetchUserProfile()
    }

    Box(
        Modifier
            .background(white)
            .fillMaxSize() // Fill the entire screen
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxSize(), // Ensures content is properly distributed
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween // Space out content vertically
        ) {
            // Title
            Text(
                text = "Account",
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
                color = Color.Gray,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(12.dp),
                fontFamily = alifbaFont
            )

            // User Details
            Text(
                text = "Parent Name: ${userProfile?.parentName ?: "Loading..."}",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = navyBlue
                ),
                fontFamily = alifbaFont,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Email: ${userProfile?.email ?: "Loading..."}",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = navyBlue
                ),
                fontFamily = alifbaFont,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Account ID: ${userProfile?.userId ?: "Loading..."}",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = navyBlue
                ),
                fontFamily = alifbaFont,
            )

            // Lottie Animation
            Spacer(modifier = Modifier.height(16.dp)) // Space between text and animation
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.accountscreen))
            val progress by animateLottieCompositionAsState(
                composition = composition,
                iterations = LottieConstants.IterateForever // Set to repeat indefinitely
            )
            LottieAnimation(
                composition = composition,
                progress = progress,
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp) // Optional padding
            )

            // Logout Button
            CommonButton(
                modifier = Modifier
                    .fillMaxWidth(0.8f) // Optional: Makes the button narrower
                    .padding(16.dp), // Add some padding
                onClick = { authViewModel.logout()},
                buttonText = "Logout",
                shadowColor = darkPink,
                mainColor = lightPink,
                textColor = white
            )
        }
    }
}
