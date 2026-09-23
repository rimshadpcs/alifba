package com.alifba.alifba.presenation.home.layout.settings

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.revenuecat.purchases.Purchases
import com.alifba.alifba.R
import com.alifba.alifba.presenation.SubscriptionViewModel
import com.alifba.alifba.presenation.stories.RevenueCatPaywall
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.revenuecat.purchases.getCustomerInfoWith
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Composable
fun SubscriptionDetailsScreen(
    navController: NavController
    // In future we can inject detailed subscription info when available
) {
    val subscriptionViewModel: SubscriptionViewModel = hiltViewModel()
    val isPremium by subscriptionViewModel.isPremium.collectAsState()

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600

    val alifbaFont = FontFamily(Font(R.font.vag_round, FontWeight.Bold))
    val alifbaFontNormal = FontFamily(Font(R.font.vag_round, FontWeight.Normal))

    var showPaywall by remember { mutableStateOf(false) }
    val showUpgrade = !isPremium
    LaunchedEffect(Unit) {
        val hasEntitlement = fetchActiveRevenueCatEntitlement()
        if (hasEntitlement != null && hasEntitlement != isPremium) {
            subscriptionViewModel.setPremium(hasEntitlement)
        }
    }

    LaunchedEffect(isPremium) {
        if (isPremium) {
            showPaywall = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(white)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(if (isTablet) 32.dp else 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (isTablet) 32.dp else 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.goback),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(if (isTablet) 48.dp else 40.dp)
                        .background(Color.Transparent)
                        .clickable {
                            navController.popBackStack()
                        }
                )

                Text(
                    text = "Subscription",
                    fontSize = if (isTablet) 32.sp else 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = navyBlue,
                    fontFamily = alifbaFont
                )

                Box(modifier = Modifier.size(if (isTablet) 48.dp else 40.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .let { base ->
                        if (isTablet) base.widthIn(max = 640.dp) else base
                    },
                shape = RoundedCornerShape(if (isTablet) 20.dp else 16.dp),
                colors = CardDefaults.cardColors(containerColor = white),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(if (isTablet) 24.dp else 20.dp),
                    verticalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 12.dp)
                ) {
                    Text(
                        text = if (isPremium) "You are on Alifba Premium" else "You are on the Free Plan",
                        style = TextStyle(
                            fontSize = if (isTablet) 22.sp else 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = navyBlue
                        ),
                        fontFamily = alifbaFont
                    )

                    if (isPremium) {
                        Text(
                            text = "Enjoy unlimited lessons, stories, and profiles.",
                            fontSize = if (isTablet) 18.sp else 16.sp,
                            color = Color.DarkGray,
                            fontFamily = alifbaFontNormal
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        InfoRow(
                            label = "Plan",
                            value = "Premium (in-app purchase)"
                        )
                    } else {
                        Text(
                            text = "You can learn with the free plan, or upgrade to unlock everything.",
                            fontSize = if (isTablet) 18.sp else 16.sp,
                            color = Color.DarkGray,
                            fontFamily = alifbaFontNormal
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        InfoRow(
                            label = "Plan",
                            value = "Free"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (isTablet) 32.dp else 24.dp))

            // Action
            if (showUpgrade) {
                CommonButton(
                    onClick = { showPaywall = true },
                    buttonText = "See Premium Plans",
                    shadowColor = navyBlue,
                    mainColor = navyBlue,
                    textColor = white
                )

                Spacer(modifier = Modifier.height(if (isTablet) 16.dp else 12.dp))

                Text(
                    text = "Upgrade to unlock all lessons, stories, and extra profiles.",
                    fontSize = if (isTablet) 16.sp else 14.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    fontFamily = alifbaFontNormal,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

        }

        if (showPaywall && showUpgrade) {
            RevenueCatPaywall(
                onClose = { showPaywall = false },
                requireParentGate = false,
                source = "settings"
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    val alifbaFont = FontFamily(Font(R.font.vag_round, FontWeight.Bold))
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            fontSize = if (isTablet) 14.sp else 12.sp,
            color = Color.Gray,
            fontFamily = alifbaFont
        )
        Text(
            text = value,
            fontSize = if (isTablet) 18.sp else 16.sp,
            fontWeight = FontWeight.Bold,
            color = navyBlue,
            fontFamily = alifbaFont
        )
    }
}

private suspend fun fetchActiveRevenueCatEntitlement(): Boolean? =
    suspendCancellableCoroutine { continuation ->
        Purchases.sharedInstance.getCustomerInfoWith(
            onError = { continuation.resume(null) },
            onSuccess = { info ->
                continuation.resume(info.entitlements.active.isNotEmpty())
            }
        )
    }
