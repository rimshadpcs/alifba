package com.alifba.alifba.presenation.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alifba.alifba.R
import kotlinx.coroutines.delay

/**
 * First-screen illustrated splash: shown for a fixed duration immediately after the native
 * OS SplashScreen hands off (MainActivity's installSplashScreen/setKeepOnScreenCondition stays
 * a simple solid+icon splash, untouched), then navigates on to [nextRoute] — the app's existing
 * (already async-resolved) start destination. Purely timed, not tap-to-dismiss.
 */
@Composable
fun IllustratedSplashScreen(
    nextRoute: String,
    onFinished: (String) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600

    val boldFont = FontFamily(Font(R.font.vag_round_boldd, FontWeight.Bold))
    val regularFont = FontFamily(Font(R.font.vag_round, FontWeight.Normal))

    LaunchedEffect(nextRoute) {
        delay(2200)
        onFinished(nextRoute)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // res/drawable/splash_background.png (phone) vs res/drawable-sw600dp/splash_background.png
        // (tablet) — same resource name, resolved automatically by the OS per device, same
        // sw600dp breakpoint this app already uses everywhere else for isTablet.
        Image(
            painter = painterResource(id = R.drawable.splash_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Positioned by percentage of the real measured screen size (not fixed dp), so it stays
        // inside the sky area and clear of the 10% edge margin regardless of device aspect
        // ratio: offsetting the block's top to 12% down keeps its vertical center around ~20%,
        // comfortably inside the top-40% band; 80%-width keeps it off the left/right margins.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(0.8f)
                .offset(y = maxHeight * 0.12f)
        ) {
            Text(
                text = "Rabbi zidni ilma",
                color = Color.White,
                fontFamily = boldFont,
                fontWeight = FontWeight.Bold,
                fontSize = if (isTablet) 34.sp else 26.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "My lord, please increase me in knowledge",
                color = Color.White,
                fontFamily = regularFont,
                fontSize = if (isTablet) 18.sp else 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Qur'an 20:114",
                color = Color.White.copy(alpha = 0.85f),
                fontFamily = regularFont,
                fontSize = if (isTablet) 14.sp else 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
