package com.alifba.alifba.presenation.stories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.stringPreferencesKey
import com.alifba.alifba.R
import com.alifba.alifba.features.authentication.dataStore
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.widgets.DotLottieView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull

// Shown for a brief beat between tapping a discount-offer notification and the actual
// DiscountPaywall/AnnualDiscount35Paywall rendering, so the transition doesn't feel like an
// instant jump straight into a sales screen. Only ever inserted ahead of those two paywalls
// when the "discountPaywall"/"annualDiscount35Paywall" route was reached with
// viaNotification=true (see MainActivity.kt) — the standard-paywall-skip navigate() call
// (HomeScreen.kt) omits that query param, which resolves to its false default, so that path
// goes straight to the paywall with no transition.
@Composable
fun DiscountPaywallEntryTransition(
    mascotName: String,
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    // Same key/fallback pattern as RevenueCatPaywall's headline personalization — captured at
    // the child's-name onboarding screen, persisted independent of any account/profile (this
    // can fire for a returning signed-in user long after onboarding, so it's read fresh here
    // rather than threaded through as a param).
    var childName by remember { mutableStateOf("your little one") }
    LaunchedEffect(Unit) {
        val savedName = context.dataStore.data.firstOrNull()
            ?.get(stringPreferencesKey("onboarding_child_name"))
        if (!savedName.isNullOrBlank()) {
            childName = savedName
        }
    }

    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )

    LaunchedEffect(Unit) {
        delay(2200)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DotLottieView(
                name = mascotName,
                modifier = Modifier.size(180.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "We've got something special for you and $childName...",
                fontFamily = alifbaFont,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = navyBlue,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp)
            )
        }
    }
}
