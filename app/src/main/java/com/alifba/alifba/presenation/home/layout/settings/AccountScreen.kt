package com.alifba.alifba.presenation.home.layout.settings

import android.util.Log
import androidx.compose.foundation.Image
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
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.alifba.alifba.R
import com.alifba.alifba.presenation.Login.AuthViewModel
import com.alifba.alifba.ui_components.theme.darkPink
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.lightPink
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AccountScreen(authViewModel: AuthViewModel, navController: NavController) {
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
                    text = "Account",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = navyBlue,
                    fontFamily = alifbaFont
                )
                // Empty box for symmetry
                Box(modifier = Modifier.size(24.dp))
            }

            // User Details
            Text(
                text = "Parent Name: ${userProfile?.parentName ?: "Loading..."}",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = darkPink
                ),
                fontFamily = alifbaFont,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Email: ${userProfile?.email ?: "Loading..."}",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = darkPink
                ),
                fontFamily = alifbaFont,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Account ID: ${userProfile?.userId ?: "Loading..."}",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = darkPink
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

            CommonButton(
                onClick = {
                    authViewModel.logout() // Clears user data and signs out
                    navController.navigate("login") {
                        popUpTo("homeScreen") { inclusive = true } // Clear back stack
                    }
                },
                buttonText = "Logout",
                shadowColor = navyBlue,
                mainColor = lightNavyBlue,
                textColor = white
            )

        }
    }
}
