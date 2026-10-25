package com.alifba.alifba.presenation.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
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
        // res/drawable-nodpi/splash_background.png (phone) vs
        // res/drawable-sw600dp-nodpi/splash_background.png (tablet) — same resource name, resolved
        // automatically by the OS per device, same sw600dp breakpoint this app already uses
        // everywhere else for isTablet. Must stay in a -nodpi folder: in plain drawable/ the OS
        // treats it as mdpi and upscales it 3x on xxhdpi phones to a ~145 MB bitmap, which
        // Canvas refuses to draw (crash on launch, ALIFBA-ANDROID-4).
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
                .offset(y = maxHeight * 0.14f)
        ) {
            val ink = Color(0xFF163A3D)
            val k = if (isTablet) 1.3f else 1f
            // Arabic: the system's Arabic face (Noto Naskh Arabic on Android), 38 semibold
            Text(
                text = "رَبِّ زِدْنِي عِلْمًا",
                color = ink,
                fontWeight = FontWeight.SemiBold,
                fontSize = (38 * k).sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Rabbi zidni ilma",
                color = ink,
                fontFamily = boldFont,
                fontWeight = FontWeight.Bold,
                fontSize = (30 * k).sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = (8 * k).dp)
            )
            // Bold face for the "medium" line: iOS draws its regular VAG heavier than Android's
            Text(
                text = "My Lord, increase me in knowledge",
                color = ink,
                fontFamily = boldFont,
                fontWeight = FontWeight.Bold,
                fontSize = (19 * k).sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = (8 * k).dp)
            )
            Text(
                text = "Qur\u2019an 20:114",
                color = ink,
                fontFamily = boldFont,
                fontWeight = FontWeight.Bold,
                fontSize = (15 * k).sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = (12 * k).dp)
                    .background(Color.White.copy(alpha = 0.55f), RoundedCornerShape((12 * k).dp))
                    .padding(horizontal = (12 * k).dp, vertical = (4 * k).dp)
            )
        }
    }
}
