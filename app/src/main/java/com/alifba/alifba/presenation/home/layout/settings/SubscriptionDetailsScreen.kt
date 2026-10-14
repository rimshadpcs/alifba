package com.alifba.alifba.presenation.home.layout.settings

import com.alifba.alifba.utils.PremiumAccess
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alifba.alifba.R
import com.alifba.alifba.presenation.SubscriptionViewModel
import com.alifba.alifba.presenation.stories.RevenueCatPaywall
import com.alifba.alifba.ui_components.widgets.AuthTheme
import com.alifba.alifba.ui_components.widgets.LessonCircleButton
import com.alifba.alifba.ui_components.widgets.LessonPrimaryButton
import com.alifba.alifba.ui_components.widgets.LessonTheme
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.restorePurchasesWith
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private val Gold = Color(0xFFF5B800)
private val GoldTint = Color(0xFFFFF3D6)
private val GoldBorder = Color(0xFFFFC93C)
private val GoldEdge = Color(0xFFF3DE9E)

private val PremiumFeatures = listOf(
    "Access to all lessons",
    "Access to the whole audio story library",
    "Multiple child profiles",
    "Progress synced on all devices"
)

@Composable
fun SubscriptionDetailsScreen(
    navController: NavController
    // In future we can inject detailed subscription info when available
) {
    val subscriptionViewModel: SubscriptionViewModel = hiltViewModel()
    val isPremium by subscriptionViewModel.isPremium.collectAsState()
    val k = LessonTheme.scale()

    var showPaywall by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var restoreMessage by remember { mutableStateOf<String?>(null) }
    var restoreIsError by remember { mutableStateOf(false) }
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
            .background(Color(0xFFF6F8FD))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Round back button on the left, centred title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Subscription",
                    style = LessonTheme.question().copy(fontSize = (32 * k).sp, textAlign = TextAlign.Center)
                )
                Box(modifier = Modifier.align(Alignment.CenterStart)) {
                    LessonCircleButton(
                        iconRes = R.drawable.backarrow,
                        contentDescription = "Back",
                        onClick = { navController.popBackStack() }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .padding(horizontal = LessonTheme.margin)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                CurrentPlanCard(isPremium = isPremium, k = k)
                PremiumCard(
                    isPremium = isPremium,
                    k = k,
                    onSeePlans = { showPaywall = true }
                )

                RestoreSection(
                    isRestoring = isRestoring,
                    message = restoreMessage,
                    messageIsError = restoreIsError,
                    k = k,
                    onRestore = {
                        isRestoring = true
                        restoreMessage = null
                        restoreIsError = false
                        Purchases.sharedInstance.restorePurchasesWith(
                            onError = { error ->
                                isRestoring = false
                                restoreIsError = true
                                restoreMessage = error.message
                            },
                            onSuccess = { info ->
                                isRestoring = false
                                val active = PremiumAccess.isPremium(info)
                                subscriptionViewModel.setPremium(active)
                                restoreMessage = if (active) "Purchases restored." else "No previous purchases found."
                            }
                        )
                    }
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
private fun CurrentPlanCard(isPremium: Boolean, k: Float) {
    SettingsGroup {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size((40 * k).dp)
                    .background(if (isPremium) GoldTint else LessonTheme.sky, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isPremium) {
                    Icon(
                        painter = painterResource(R.drawable.ic_crown_filled),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size((24 * k).dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.PersonOutline,
                        contentDescription = null,
                        tint = LessonTheme.primary,
                        modifier = Modifier.size((24 * k).dp)
                    )
                }
            }

            Column {
                Text(
                    text = "CURRENT PLAN",
                    style = LessonTheme.answer().copy(
                        fontSize = (13 * k).sp,
                        color = AuthTheme.secondaryText,
                        letterSpacing = 1.2.sp
                    )
                )
                Text(
                    text = if (isPremium) "Premium" else "Free",
                    style = LessonTheme.answer().copy(fontSize = (22 * k).sp)
                )
            }
        }
    }
}

@Composable
private fun PremiumCard(isPremium: Boolean, k: Float, onSeePlans: () -> Unit) {
    val shape = RoundedCornerShape(28.dp)
    val edge = 6.dp

    Box(modifier = Modifier.padding(bottom = edge)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = edge)
                    .background(GoldEdge, shape)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape)
                    .border(2.5.dp, GoldBorder, shape)
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size((52 * k).dp)
                            .background(GoldTint, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_crown_filled),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size((30 * k).dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Alifba Premium",
                            style = LessonTheme.question().copy(fontSize = (26 * k).sp)
                        )
                        Text(
                            text = "Everything, for the whole family",
                            style = LessonTheme.answer().copy(
                                fontSize = (16 * k).sp,
                                fontWeight = FontWeight.Normal,
                                color = AuthTheme.secondaryText
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                PremiumFeatures.forEachIndexed { index, feature ->
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(Color(0xFFEEF0F7))
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size((34 * k).dp)
                                .background(GoldTint, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Gold,
                                modifier = Modifier.size((18 * k).dp)
                            )
                        }
                        Text(
                            text = feature,
                            style = LessonTheme.answer().copy(fontSize = (19 * k).sp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (isPremium) {
                    Text(
                        text = "Your premium access works on any device when you sign in with the same Alifba account.",
                        style = LessonTheme.answer().copy(
                            fontSize = (15 * k).sp,
                            fontWeight = FontWeight.Normal,
                            color = AuthTheme.secondaryText
                        ),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(18.dp))
                    LessonPrimaryButton(title = "See premium plans", onClick = onSeePlans)
                }
            }
        }
    }
}

@Composable
private fun RestoreSection(
    isRestoring: Boolean,
    message: String?,
    messageIsError: Boolean,
    k: Float,
    onRestore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Already subscribed on another device?",
            style = LessonTheme.answer().copy(
                fontSize = (16 * k).sp,
                fontWeight = FontWeight.Normal,
                color = AuthTheme.secondaryText,
                textAlign = TextAlign.Center
            )
        )
        Text(
            text = if (isRestoring) "Restoring..." else "Restore purchases",
            style = LessonTheme.answer().copy(
                fontSize = (16 * k).sp,
                color = LessonTheme.primaryEdge,
                textDecoration = TextDecoration.Underline,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .heightIn(min = 36.dp)
                .wrapContentHeight(Alignment.CenterVertically)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = !isRestoring
                ) {
                    SoundEffectManager.playClickSound()
                    onRestore()
                }
        )
        if (message != null) {
            Text(
                text = message,
                style = LessonTheme.answer().copy(
                    fontSize = (14 * k).sp,
                    fontWeight = FontWeight.Normal,
                    color = if (messageIsError) LessonTheme.wrongEdge else AuthTheme.secondaryText,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

private suspend fun fetchActiveRevenueCatEntitlement(): Boolean? =
    suspendCancellableCoroutine { continuation ->
        Purchases.sharedInstance.getCustomerInfoWith(
            onError = { continuation.resume(null) },
            onSuccess = { info ->
                continuation.resume(PremiumAccess.isPremium(info))
            }
        )
    }
