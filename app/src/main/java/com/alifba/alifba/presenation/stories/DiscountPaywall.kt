package com.alifba.alifba.presenation.stories

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.alifba.alifba.R
import com.alifba.alifba.presenation.SubscriptionViewModel
import com.alifba.alifba.service.SubscriptionTrialReminderReceiver
import com.alifba.alifba.ui_components.theme.darkBlue
import com.alifba.alifba.ui_components.theme.lightCandyGreen
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.utils.requestNotificationPermission
import com.alifba.alifba.utils.shouldAskNotifications
import com.revenuecat.purchases.*
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.models.StoreTransaction
import kotlinx.coroutines.delay

// Gemini-style spinning gradient ring around the CTA — the array repeats its first color at
// the end so the sweep gradient loops seamlessly with no visible seam as it rotates.
private val rainbowGradientColors = listOf(
    Color(0xFFFF5F6D), Color(0xFFFFC371), Color(0xFFF9F871),
    Color(0xFF6DE1D2), Color(0xFF7C83FD), Color(0xFFC589E8),
    Color(0xFFFF5F6D)
)

// Rotates only the gradient's *colors*, never the geometry it's drawn onto — using Android's
// native Shader.setLocalMatrix(), built exactly for this. Combined with Compose's own
// Modifier.border(width, brush, shape) for the actual stroke, shape and color are fully
// independent: the border never moves, only the colors animate. Verified working on the main
// paywall's RevenueCatPaywall.kt CTA — this is the same technique, ported here.
private class DiscountRotatingSweepGradientBrush(
    private val colors: List<Color>,
    private val angleDegrees: Float
) : ShaderBrush() {
    override fun createShader(size: androidx.compose.ui.geometry.Size): Shader {
        val shader = android.graphics.SweepGradient(
            size.width / 2f,
            size.height / 2f,
            colors.map { it.toArgb() }.toIntArray(),
            null
        )
        val matrix = android.graphics.Matrix()
        matrix.postRotate(angleDegrees, size.width / 2f, size.height / 2f)
        shader.setLocalMatrix(matrix)
        return shader
    }
}

@Composable
private fun RainbowClaimOfferButton(
    text: String,
    onClick: () -> Unit,
    fontFamily: FontFamily,
    height: Dp,
    fontSize: TextUnit,
    cornerRadius: Dp,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rainbowRotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(3000, easing = LinearEasing)),
        label = "rainbowAngle"
    )
    val strokeWidth = 4.21875.dp
    val ringBrush = remember(angle) { DiscountRotatingSweepGradientBrush(rainbowGradientColors, angle) }

    Box(
        modifier = modifier
            .height(height)
            .border(width = strokeWidth, brush = ringBrush, shape = RoundedCornerShape(cornerRadius))
    ) {
        Box(
            modifier = Modifier
                .padding(strokeWidth)
                .fillMaxSize()
                .clip(RoundedCornerShape((cornerRadius - strokeWidth).coerceAtLeast(0.dp)))
                .background(navyBlue)
                .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text, fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = fontSize, color = Color.White)
        }
    }
}

@Composable
fun DiscountPaywall(
    onClose: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val subscriptionViewModel: SubscriptionViewModel = hiltViewModel()

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val expiryTimestamp by subscriptionViewModel.discountExpiryTimestamp.collectAsState()

    var selectedPackage by remember { mutableStateOf<Package?>(null) }
    var originalPrice by remember { mutableStateOf("$49.99") }
    var monthlyPrice by remember { mutableStateOf("$4.99") }
    val timeRemaining by produceState(
        initialValue = if (expiryTimestamp == 0L || expiryTimestamp <= System.currentTimeMillis()) 600L
                       else ((expiryTimestamp - System.currentTimeMillis()) / 1000).coerceAtLeast(0L),
        key1 = expiryTimestamp
    ) {
        val now = System.currentTimeMillis()
        if (expiryTimestamp == 0L || expiryTimestamp <= now) {
            val startTime = System.currentTimeMillis()
            val duration = 600L
            while (true) {
                val elapsed = (System.currentTimeMillis() - startTime) / 1000
                value = (duration - elapsed).coerceAtLeast(0L)
                if (value == 0L) break
                delay(1000)
            }
        } else {
            while (true) {
                val current = System.currentTimeMillis()
                value = ((expiryTimestamp - current) / 1000).coerceAtLeast(0L)
                if (value == 0L) break
                delay(1000)
            }
        }
    }

    var showIgnore by remember { mutableStateOf(false) }
    var isPurchasing by remember { mutableStateOf(false) }

    // Consumes the system back gesture/button while a purchase is in flight — the overlay
    // passed into each content composable blocks touches to the on-screen close/ignore/claim
    // controls, but the hardware back button is a separate input channel a touch-blocking
    // overlay alone doesn't stop.
    BackHandler(enabled = isPurchasing) {}

    LaunchedEffect(Unit) {
        delay(3000)
        showIgnore = true
    }

    LaunchedEffect(Unit) {
        Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
            override fun onReceived(offerings: Offerings) {
                val discount = offerings["downsell_offering"]
                if (discount != null) {
                    selectedPackage = discount.availablePackages.firstOrNull { it.packageType == PackageType.ANNUAL }
                }
                val standardOffering = offerings.current
                standardOffering?.availablePackages?.firstOrNull { it.packageType == PackageType.ANNUAL }?.product?.price?.formatted?.let {
                    originalPrice = it
                }
                standardOffering?.availablePackages?.firstOrNull { it.packageType == PackageType.MONTHLY }?.product?.price?.formatted?.let {
                    monthlyPrice = it
                }
            }
            override fun onError(error: PurchasesError) {}
        })
    }

    val onClaimClickAction = {
        val pkg = selectedPackage
        if (pkg != null && activity != null && subscriptionViewModel.tryBeginPurchase()) {
            isPurchasing = true

            fun finishPurchaseAttempt() {
                isPurchasing = false
                subscriptionViewModel.endPurchase()
            }

            // Fresh check against RevenueCat, not the locally-cached isPremium flag — guards
            // against a purchase completed moments ago on a different paywall screen (e.g.
            // the main paywall) that this screen's own state doesn't know about yet.
            Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
                override fun onReceived(info: CustomerInfo) {
                    if (info.entitlements.active.containsKey("Alifba Pro")) {
                        finishPurchaseAttempt()
                        subscriptionViewModel.setPremium(true)
                        SubscriptionTrialReminderReceiver.cancelDiscountReminder(context)
                        onSuccess()
                        return
                    }

                    Purchases.sharedInstance.purchase(
                        PurchaseParams.Builder(activity, pkg).build(),
                        object : PurchaseCallback {
                            override fun onCompleted(tx: StoreTransaction, info: CustomerInfo) {
                                finishPurchaseAttempt()
                                if (info.entitlements.active.containsKey("Alifba Pro")) {
                                    subscriptionViewModel.setPremium(true)
                                    SubscriptionTrialReminderReceiver.cancelDiscountReminder(context)
                                    onSuccess()
                                }
                            }
                            override fun onError(error: PurchasesError, userCancelled: Boolean) {
                                finishPurchaseAttempt()
                            }
                        }
                    )
                }

                override fun onError(error: PurchasesError) {
                    // Couldn't verify current entitlement state — fail safe by not starting a
                    // purchase rather than risking a duplicate charge.
                    finishPurchaseAttempt()
                }
            })
        }
    }

    val onCloseAction = {
        val current = System.currentTimeMillis()
        val extension = current + (24 * 60 * 60 * 1000L)
        subscriptionViewModel.setDiscountExpiry(extension)

        // Ensure notification permission is granted
        if (context.shouldAskNotifications()) {
            requestNotificationPermission(context)
        }

        // Schedule notification 2 hours before expiry (22 hours from now)
        val reminderTime = extension - (2 * 60 * 60 * 1000L)
        if (reminderTime > current) {
            SubscriptionTrialReminderReceiver.scheduleDiscountReminder(context, reminderTime)
        }

        onClose()
    }

    if (isTablet) {
        DiscountPaywallContentTablet(
            timeRemaining = timeRemaining,
            selectedPackage = selectedPackage,
            originalPrice = originalPrice,
            monthlyPrice = monthlyPrice,
            showIgnore = showIgnore,
            isPurchasing = isPurchasing,
            onClose = onCloseAction,
            onClaimClick = onClaimClickAction
        )
    } else {
        DiscountPaywallContent(
            timeRemaining = timeRemaining,
            selectedPackage = selectedPackage,
            originalPrice = originalPrice,
            monthlyPrice = monthlyPrice,
            showIgnore = showIgnore,
            isPurchasing = isPurchasing,
            onClose = onCloseAction,
            onClaimClick = onClaimClickAction
        )
    }
}

@Composable
fun DiscountPaywallContent(
    timeRemaining: Long,
    selectedPackage: Package?,
    originalPrice: String,
    monthlyPrice: String,
    showIgnore: Boolean,
    isPurchasing: Boolean,
    onClose: () -> Unit,
    onClaimClick: () -> Unit
) {
    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(animation = tween(1200), repeatMode = RepeatMode.Reverse),
        label = "pulseScale"
    )

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                if (showIgnore) {
                    Text(text = "ignore offer", color = Color.Black.copy(alpha = 0.6f), fontFamily = alifbaFont, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(enabled = !isPurchasing) { onClose() })
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                Text(text = "Restore", color = Color.Black.copy(alpha = 0.6f), fontFamily = alifbaFont, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(enabled = !isPurchasing) { })
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Wait, We have a special offer for you", fontFamily = alifbaFont, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.Black, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth().graphicsLayer { scaleX = pulseScale; scaleY = pulseScale }) {
                Image(painter = painterResource(id = R.drawable.limitedoffer), contentDescription = "Special Offer", modifier = Modifier.size(140.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "20%", fontFamily = alifbaFont, fontWeight = FontWeight.Bold, fontSize = 72.sp, color = Color(0xFFf29c1f), textAlign = TextAlign.Center, lineHeight = 72.sp)
                    Text(text = "OFF", fontFamily = alifbaFont, fontWeight = FontWeight.Bold, fontSize = 38.sp, color = Color(0xFFf29c1f), textAlign = TextAlign.Center)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "offer ends in", fontFamily = alifbaFont, fontSize = 14.sp, color = Color.Black.copy(alpha = 0.6f), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val hours = timeRemaining / 3600
                val minutes = (timeRemaining % 3600) / 60
                val seconds = timeRemaining % 60
                if (hours > 0) {
                    TimerBox(time = String.format("%02d", hours), label = "hrs", alifbaFont)
                    Text(" : ", color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                TimerBox(time = String.format("%02d", minutes), label = "min", alifbaFont)
                Text(" : ", color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                TimerBox(time = String.format("%02d", seconds), label = "sec", alifbaFont)
            }

            Spacer(modifier = Modifier.height(32.dp))
            val discountedPrice = selectedPackage?.product?.price?.formatted ?: "$39.99"
            val monthlyEquivalent = selectedPackage?.let {
                val amount = it.product.price.amountMicros.toDouble() / 1_000_000.0 / 12.0
                val formatter = java.text.NumberFormat.getCurrencyInstance().apply { currency = java.util.Currency.getInstance(it.product.price.currencyCode) }
                formatter.format(amount)
            } ?: "$3.33"

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = originalPrice, fontFamily = alifbaFont, fontSize = 20.sp, color = Color.Black.copy(alpha = 0.4f), textDecoration = TextDecoration.LineThrough)
                Text(text = " → ", color = Color.Black, fontSize = 20.sp)
                Text(text = "$discountedPrice/year", fontFamily = alifbaFont, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.Black)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Just $monthlyEquivalent/mo with this deal", fontFamily = alifbaFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = lightCandyGreen)
            Text(text = "(Standard price is $monthlyPrice/mo)", fontFamily = alifbaFont, fontSize = 14.sp, color = Color.Black.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(10.dp))
            TrialTimelineVertical(alifbaFont)

            Spacer(modifier = Modifier.height(16.dp))
            PaywallTestimonialCarousel(fontFamily = alifbaFont)

            Spacer(modifier = Modifier.height(20.dp))

            RainbowClaimOfferButton(
                text = if (isPurchasing) "Processing..." else "Claim Offer",
                onClick = onClaimClick,
                enabled = !isPurchasing,
                fontFamily = alifbaFont,
                height = 56.dp,
                fontSize = 18.sp,
                cornerRadius = 16.dp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Secured by Google Play. Cancel anytime.", color = Color.Black.copy(alpha = 0.4f), fontSize = 12.sp, fontFamily = alifbaFont)
        }

        if (isPurchasing) {
            PurchaseProcessingOverlay()
        }
    }
}

@Composable
fun DiscountPaywallContentTablet(
    timeRemaining: Long,
    selectedPackage: Package?,
    originalPrice: String,
    monthlyPrice: String,
    showIgnore: Boolean,
    isPurchasing: Boolean,
    onClose: () -> Unit,
    onClaimClick: () -> Unit
) {
    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulseTablet")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(animation = tween(1200), repeatMode = RepeatMode.Reverse),
        label = "pulseScaleTablet"
    )

    val scale = 1.5f
    val maxWidth = 850.dp

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            if (showIgnore) {
                Text(text = "ignore offer", color = Color.Black.copy(alpha = 0.6f), fontFamily = alifbaFont, fontWeight = FontWeight.Bold, fontSize = scaledSp(14, scale), modifier = Modifier.clickable(enabled = !isPurchasing) { onClose() })
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }
            Text(text = "Restore", color = Color.Black.copy(alpha = 0.6f), fontFamily = alifbaFont, fontWeight = FontWeight.Bold, fontSize = scaledSp(14, scale), modifier = Modifier.clickable(enabled = !isPurchasing) { })
        }

        Column(
            modifier = Modifier.widthIn(max = maxWidth).fillMaxSize().align(Alignment.TopCenter).padding(horizontal = 80.dp, vertical = 24.dp).padding(top = 40.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(scaledDp(20, scale)))
            Text(text = "Wait, We have a special offer for you", fontFamily = alifbaFont, fontWeight = FontWeight.Bold, fontSize = scaledSp(24, scale), color = Color.Black, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(scaledDp(10, scale)))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth().graphicsLayer { scaleX = pulseScale; scaleY = pulseScale }) {
                Image(painter = painterResource(id = R.drawable.limitedoffer), contentDescription = "Special Offer", modifier = Modifier.size(scaledDp(160, scale)))
                Spacer(modifier = Modifier.width(scaledDp(8, scale)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "20%", fontFamily = alifbaFont, fontWeight = FontWeight.Bold, fontSize = scaledSp(80, scale), color = Color(0xFFf29c1f), textAlign = TextAlign.Center, lineHeight = scaledSp(80, scale))
                    Text(text = "OFF", fontFamily = alifbaFont, fontWeight = FontWeight.Bold, fontSize = scaledSp(42, scale), color = Color(0xFFf29c1f), textAlign = TextAlign.Center)
                }
            }

            Spacer(modifier = Modifier.height(scaledDp(20, scale)))
            Text(text = "offer ends in", fontFamily = alifbaFont, fontSize = scaledSp(16, scale), color = Color.Black.copy(alpha = 0.6f), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(scaledDp(10, scale)))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val hours = timeRemaining / 3600
                val minutes = (timeRemaining % 3600) / 60
                val seconds = timeRemaining % 60
                if (hours > 0) {
                    TimerBox(time = String.format("%02d", hours), label = "hrs", fontFamily = alifbaFont, scale = scale)
                    Text(" : ", color = Color.Black, fontSize = scaledSp(24, scale), fontWeight = FontWeight.Bold)
                }
                TimerBox(time = String.format("%02d", minutes), label = "min", fontFamily = alifbaFont, scale = scale)
                Text(" : ", color = Color.Black, fontSize = scaledSp(24, scale), fontWeight = FontWeight.Bold)
                TimerBox(time = String.format("%02d", seconds), label = "sec", fontFamily = alifbaFont, scale = scale)
            }

            Spacer(modifier = Modifier.height(scaledDp(40, scale)))
            val discountedPrice = selectedPackage?.product?.price?.formatted ?: "$39.99"
            val monthlyEquivalent = selectedPackage?.let {
                val amount = it.product.price.amountMicros.toDouble() / 1_000_000.0 / 12.0
                val formatter = java.text.NumberFormat.getCurrencyInstance().apply { currency = java.util.Currency.getInstance(it.product.price.currencyCode) }
                formatter.format(amount)
            } ?: "$3.33"

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = originalPrice, fontFamily = alifbaFont, fontSize = scaledSp(22, scale), color = Color.Black.copy(alpha = 0.4f), textDecoration = TextDecoration.LineThrough)
                Text(text = " → ", color = Color.Black, fontSize = scaledSp(22, scale))
                Text(text = "$discountedPrice/year", fontFamily = alifbaFont, fontWeight = FontWeight.Bold, fontSize = scaledSp(28, scale), color = Color.Black)
            }
            Spacer(modifier = Modifier.height(scaledDp(10, scale)))
            Text(text = "Just $monthlyEquivalent/mo with this deal", fontFamily = alifbaFont, fontWeight = FontWeight.Bold, fontSize = scaledSp(18, scale), color = lightCandyGreen)
            Text(text = "(Standard price is $monthlyPrice/mo)", fontFamily = alifbaFont, fontSize = scaledSp(16, scale), color = Color.Black.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(scaledDp(20, scale)))
            TrialTimelineVertical(alifbaFont, scale)

            Spacer(modifier = Modifier.height(scaledDp(20, scale)))
            PaywallTestimonialCarousel(fontFamily = alifbaFont, scale = scale)

            Spacer(modifier = Modifier.height(scaledDp(30, scale)))

            RainbowClaimOfferButton(
                text = if (isPurchasing) "Processing..." else "Claim Offer",
                onClick = onClaimClick,
                enabled = !isPurchasing,
                fontFamily = alifbaFont,
                height = scaledDp(64, scale),
                fontSize = scaledSp(20, scale),
                cornerRadius = 16.dp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(scaledDp(14, scale)))
            Text(text = "Secured by Google Play. Cancel anytime.", color = Color.Black.copy(alpha = 0.4f), fontSize = scaledSp(13, scale), fontFamily = alifbaFont)
            Spacer(modifier = Modifier.height(scaledDp(20, scale)))
        }

        if (isPurchasing) {
            PurchaseProcessingOverlay()
        }
    }
}

@Composable
fun TimerBox(time: String, label: String, fontFamily: FontFamily, scale: Float = 1f) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(scaledDp(60, scale)).background(navyBlue.copy(alpha = 0.08f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Text(text = time, color = Color.Black, fontSize = scaledSp(28, scale), fontWeight = FontWeight.Bold, fontFamily = fontFamily)
        }
        Text(text = label, color = Color.Black.copy(alpha = 0.5f), fontSize = scaledSp(12, scale), fontFamily = fontFamily)
    }
}

private enum class DiscountTimelineMarker { CHECK, BELL, SUBSCRIPTION }

@Composable
fun TrialTimelineVertical(fontFamily: FontFamily, scale: Float = 1f) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(text = "No Payment needed now", fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = scaledSp(20, scale), color = lightCandyGreen, modifier = Modifier.padding(bottom = scaledDp(24, scale)).align(Alignment.CenterHorizontally))
        TimelineRow(marker = DiscountTimelineMarker.CHECK, title = "Today", subtitle = "Start your full access to Alifba Premium", isLast = false, fontFamily = fontFamily, scale = scale)
        TimelineRow(marker = DiscountTimelineMarker.BELL, title = "Day 5", subtitle = "Get a reminder about when your trial will end", isLast = false, fontFamily = fontFamily, scale = scale)
        TimelineRow(marker = DiscountTimelineMarker.SUBSCRIPTION, title = "Day 7", subtitle = "You'll be charged today, cancel anytime before", isLast = true, fontFamily = fontFamily, scale = scale)
    }
}

@Composable
private fun TimelineRow(marker: DiscountTimelineMarker, title: String, subtitle: String, isLast: Boolean, fontFamily: FontFamily, scale: Float = 1f) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(scaledDp(24, scale)).background(Color.White, CircleShape).border(1.dp, Color.Black, CircleShape), contentAlignment = Alignment.Center) {
                when (marker) {
                    DiscountTimelineMarker.CHECK -> Icon(Icons.Filled.Check, null, tint = Color.Black, modifier = Modifier.size(scaledDp(14, scale)))
                    DiscountTimelineMarker.BELL -> Icon(Icons.Filled.Notifications, null, tint = Color.Black, modifier = Modifier.size(scaledDp(14, scale)))
                    DiscountTimelineMarker.SUBSCRIPTION -> Image(painterResource(id = R.drawable.crown), null, modifier = Modifier.size(scaledDp(14, scale)))
                }
            }
            if (!isLast) Box(modifier = Modifier.width(scaledDp(1, scale)).fillMaxHeight().background(Color.Black.copy(alpha = 0.15f)))
        }
        Spacer(modifier = Modifier.width(scaledDp(16, scale)))
        Column(modifier = Modifier.padding(bottom = scaledDp(24, scale))) {
            Text(text = title, fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = scaledSp(16, scale), color = Color.Black)
            Text(text = subtitle, fontFamily = fontFamily, fontSize = scaledSp(14, scale), color = Color.Black.copy(alpha = 0.6f))
        }
    }
}

// Same copy and visual language as RevenueCatPaywall's testimonialsSection/TestimonialCard —
// kept as an independent copy here (not shared code) per this file's separation from the
// main paywall, but using the same already-approved review content for consistency.
private val paywallTestimonials = listOf(
    Triple(
        "Premium is worth for our family",
        "Aisha M.",
        "The quizzes are actually fun? Like, my daughter doesn't even realize she's being tested. She just wants to earn the next badge."
    ),
    Triple(
        "We chose Premium and love it",
        "Yusra",
        "The gamification is spot on. My son was so proud when he unlocked his first collectible badge. Great motivation system!"
    ),
    Triple(
        "The premium is a good deal to learn more",
        "Amana",
        "We use it for car rides instead of just watching videos. The audio quality is amazing, and the stories are perfect for keeping them quiet and engaged."
    )
)

@Composable
private fun PaywallTestimonialCarousel(fontFamily: FontFamily, scale: Float = 1f) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = scaledDp(4, scale))
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(scaledDp(12, scale))
    ) {
        paywallTestimonials.forEach { (title, name, quote) ->
            PaywallTestimonialCard(title = title, name = name, quote = quote, fontFamily = fontFamily, scale = scale)
        }
    }
}

@Composable
private fun PaywallTestimonialCard(title: String, name: String, quote: String, fontFamily: FontFamily, scale: Float = 1f) {
    Column(
        modifier = Modifier
            .width(scaledDp(240, scale))
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = scaledDp(12, scale), vertical = scaledDp(6, scale)),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(5) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFFFD8300),
                    modifier = Modifier.width(scaledDp(12, scale)).height(scaledDp(12, scale))
                )
                Spacer(modifier = Modifier.width(scaledDp(2, scale)))
            }
        }

        Spacer(modifier = Modifier.height(scaledDp(4, scale)))

        Text(
            text = title,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = scaledSp(12, scale),
            color = darkBlue,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(scaledDp(4, scale)))
        Text(
            text = quote,
            fontFamily = fontFamily,
            fontSize = scaledSp(11, scale),
            color = darkBlue.copy(alpha = 0.75f),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(scaledDp(3, scale)))
        Text(
            text = name,
            fontFamily = fontFamily,
            fontSize = scaledSp(10, scale),
            color = darkBlue.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
    }
}

private fun scaledDp(base: Int, scale: Float) = (base * scale).dp
private fun scaledSp(base: Int, scale: Float) = (base * scale).sp

@Preview(showBackground = true, widthDp = 1000, heightDp = 1300)
@Composable
fun DiscountPaywallTabletPreview() {
    DiscountPaywallContentTablet(timeRemaining = 599L, selectedPackage = null, originalPrice = "$49.99", monthlyPrice = "$4.99", showIgnore = true, isPurchasing = false, onClose = {}, onClaimClick = {})
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun DiscountPaywallMobilePreview() {
    DiscountPaywallContent(timeRemaining = 599L, selectedPackage = null, originalPrice = "$49.99", monthlyPrice = "$4.99", showIgnore = true, isPurchasing = false, onClose = {}, onClaimClick = {})
}

// Independent copy of the same blocking overlay used in AnnualDiscount35Paywall.kt and
// RevenueCatPaywall.kt — a dimmed scrim that's itself opaque enough to intercept every touch to
// whatever's underneath, so it doubles as the disabling mechanism for controls that weren't
// explicitly given an enabled = !isPurchasing check.
@Composable
private fun PurchaseProcessingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable(enabled = true, onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Processing...",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
