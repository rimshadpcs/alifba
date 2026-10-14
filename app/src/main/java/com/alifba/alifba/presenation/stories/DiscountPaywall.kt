package com.alifba.alifba.presenation.stories

import com.alifba.alifba.utils.PremiumAccess
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.datastore.preferences.core.stringPreferencesKey
import com.alifba.alifba.features.authentication.dataStore
import kotlinx.coroutines.flow.firstOrNull
import com.alifba.alifba.ui_components.widgets.AuthTheme
import com.alifba.alifba.ui_components.widgets.BadgeEarnedBackground
import com.alifba.alifba.ui_components.widgets.LessonButtonKind
import com.alifba.alifba.ui_components.widgets.LessonPrimaryButton
import com.alifba.alifba.ui_components.widgets.LessonTheme
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

    // Whichever path opened this screen (skip chain, banner, notification), the "first skip
    // ever" discount has now been seen. Matches iOS markDownsellSeen() on appear.
    LaunchedEffect(Unit) {
        subscriptionViewModel.setHasSeenDownsellModal(true)
    }

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
                    if (PremiumAccess.isPremium(info)) {
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
                                if (PremiumAccess.isPremium(info)) {
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
        // The 24h window starts once (first time this offer is dismissed) and is never
        // restarted by later closes, so the countdown the banner shows is real. Matches iOS
        // DownsellManager.startDiscountTimerIfNeeded.
        if (subscriptionViewModel.discountExpiryTimestamp.value == 0L) {
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
        }

        onClose()
    }

    // Captured at onboarding (same key the main paywall reads); "your family" when unset
    var childName by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        childName = context.dataStore.data.firstOrNull()
            ?.get(stringPreferencesKey("onboarding_child_name"))
            ?.takeIf { it.isNotBlank() }
        // A signed-in parent sees the current child's name
        loadPaywallChildName(context)?.let { childName = it }
    }

    DiscountPaywallContent(
        timeRemaining = timeRemaining,
        selectedPackage = selectedPackage,
        originalPrice = originalPrice,
        monthlyPrice = monthlyPrice,
        showIgnore = showIgnore,
        isPurchasing = isPurchasing,
        onClose = onCloseAction,
        onClaimClick = onClaimClickAction,
        childName = childName
    )
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
    onClaimClick: () -> Unit,
    childName: String? = null
) {
    val k = LessonTheme.scale()
    val context = LocalContext.current
    val discountedPrice = selectedPackage?.product?.price?.formatted ?: "$39.99"
    val monthlyEquivalent = selectedPackage?.let {
        val amount = it.product.price.amountMicros.toDouble() / 1_000_000.0 / 12.0
        val formatter = java.text.NumberFormat.getCurrencyInstance().apply { currency = java.util.Currency.getInstance(it.product.price.currencyCode) }
        formatter.format(amount)
    } ?: "$3.33"
    // The offer has always promised 7 days free; use the store's trial length when it has one
    val trialDays = selectedPackage?.let { annualTrialDays(it) } ?: 7

    // Hours and minutes, or minutes and seconds in the last hour
    val hours = timeRemaining / 3600
    val minutes = (timeRemaining % 3600) / 60
    val seconds = timeRemaining % 60
    val tiles = if (hours >= 1) {
        listOf(String.format("%02d", hours) to "hours", String.format("%02d", minutes) to "min")
    } else {
        listOf(String.format("%02d", minutes) to "min", String.format("%02d", seconds) to "sec")
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF6F8FD))) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Hero on the badge-earned sunburst, rounded at the bottom, up under the status bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 44.dp, bottomEnd = 44.dp))
                ) {
                    BadgeEarnedBackground(modifier = Modifier.matchParentSize())

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(bottom = 58.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // No thanks appears after a few seconds but keeps its place
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .heightIn(min = 48.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "No thanks",
                                style = LessonTheme.answer().copy(fontSize = (17 * k).sp, color = LessonTheme.ink.copy(alpha = 0.75f)),
                                modifier = Modifier
                                    .alpha(if (showIgnore) 1f else 0f)
                                    .clickable(enabled = showIgnore && !isPurchasing) { onClose() }
                            )
                            // Unchanged: this screen's Restore has never been wired up on Android
                            Text(
                                text = "Restore",
                                style = LessonTheme.answer().copy(fontSize = (17 * k).sp, color = LessonTheme.primaryEdge),
                                modifier = Modifier.clickable(enabled = !isPurchasing) { }
                            )
                        }

                        GiftIllustration(modifier = Modifier
                            .padding(top = 2.dp)
                            .size((64 * k).dp))

                        Text(
                            text = "A gift for ${childName?.let { "$it’s" } ?: "your"} family",
                            style = LessonTheme.answer().copy(fontSize = (18 * k).sp, color = Color(0xFF8A5A00)),
                            modifier = Modifier.padding(top = 10.dp)
                        )
                        Text(
                            text = "20% off",
                            style = LessonTheme.question().copy(fontSize = (58 * k).sp, lineHeight = (64 * k).sp)
                        )
                        Text(
                            text = "Alifba Premium, for a whole year",
                            style = LessonTheme.answer().copy(fontSize = (18 * k).sp, fontWeight = FontWeight.Normal)
                        )
                        Text(
                            text = "GIFT ENDS IN",
                            style = LessonTheme.answer().copy(fontSize = (14 * k).sp, color = Color(0xFF8A6300), letterSpacing = 1.2.sp),
                            modifier = Modifier.padding(top = 18.dp)
                        )
                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            tiles.forEach { (value, label) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(modifier = Modifier.padding(bottom = 4.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size((66 * k).dp, (54 * k).dp)
                                                .offset(y = 4.dp)
                                                .background(LessonTheme.lineEdge, RoundedCornerShape(16.dp))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size((66 * k).dp, (54 * k).dp)
                                                .background(Color.White, RoundedCornerShape(16.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = value, style = LessonTheme.question().copy(fontSize = (28 * k).sp))
                                        }
                                    }
                                    Text(
                                        text = label,
                                        style = LessonTheme.answer().copy(fontSize = (14 * k).sp, color = LessonTheme.ink.copy(alpha = 0.8f)),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // The plan card overlaps the hero's rounded bottom
                Column(
                    modifier = Modifier
                        .widthIn(max = 560.dp)
                        .fillMaxWidth()
                        .offset(y = (-34).dp)
                        .padding(horizontal = LessonTheme.margin)
                ) {
                    DiscountPlanCard(
                        originalPrice = originalPrice,
                        discountedPrice = discountedPrice,
                        monthlyEquivalent = monthlyEquivalent,
                        monthlyPrice = monthlyPrice
                    )
                    PaywallTrialRow(
                        title = "$trialDays days free, no payment now",
                        steps = listOf(
                            Triple("Today", "Full access, free", TimelineMarker.CHECK),
                            Triple("Day ${maxOf(1, trialDays - 2)}", "We remind you", TimelineMarker.BELL),
                            Triple("Day $trialDays", "Plan starts", TimelineMarker.SUBSCRIPTION)
                        ),
                        modifier = Modifier.padding(top = 22.dp)
                    )
                    PaywallTestimonials(modifier = Modifier.padding(top = 20.dp))
                }
            }

            // Pinned claim panel
            val panelShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, panelShape, ambientColor = Color.Black.copy(alpha = 0.06f), spotColor = Color.Black.copy(alpha = 0.06f))
                    .background(Color.White, panelShape)
                    .border(1.5.dp, LessonTheme.line, panelShape),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = LessonTheme.margin)
                        .padding(top = 16.dp, bottom = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val enabled = selectedPackage != null && !isPurchasing
                    LessonPrimaryButton(
                        kind = if (enabled) LessonButtonKind.Primary else LessonButtonKind.Disabled,
                        height = (64 * k).dp,
                        onClick = { if (enabled) onClaimClick() }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isPurchasing) "Processing..." else "Claim my 20% gift",
                                style = LessonTheme.button().copy(fontSize = (21 * k).sp, color = Color.White),
                                maxLines = 1
                            )
                            if (!isPurchasing) {
                                Text(
                                    text = "$trialDays days free, then $discountedPrice/year",
                                    style = LessonTheme.answer().copy(
                                        fontSize = (14 * k).sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.White.copy(alpha = 0.9f)
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    val footStyle = LessonTheme.answer().copy(
                        fontSize = (13 * k).sp,
                        fontWeight = FontWeight.Normal,
                        color = AuthTheme.secondaryText
                    )
                    val linkStyle = footStyle.copy(color = LessonTheme.primaryEdge, textDecoration = TextDecoration.Underline)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Secured by Google Play · Cancel anytime ·", style = footStyle, maxLines = 1)
                        Text(
                            text = "Terms",
                            style = linkStyle,
                            modifier = Modifier.clickable { openDiscountUrl(context, "https://alifba.xyz/terms-of-service") }
                        )
                        Text(text = "·", style = footStyle)
                        Text(
                            text = "Privacy",
                            style = linkStyle,
                            modifier = Modifier.clickable { openDiscountUrl(context, "https://alifba.xyz/privacy-policy") }
                        )
                    }
                }
            }
        }

        if (isPurchasing) {
            PurchaseProcessingOverlay()
        }
    }
}

/** The discounted annual plan: was / now, per month, and the regular monthly price */
@Composable
private fun DiscountPlanCard(originalPrice: String, discountedPrice: String, monthlyEquivalent: String, monthlyPrice: String) {
    val k = LessonTheme.scale()
    val shape = RoundedCornerShape(24.dp)
    val edge = 6.dp
    Box(modifier = Modifier.padding(bottom = edge)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = edge)
                    .background(Color(0xFFF3DE9E), shape)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape)
                    .border(2.5.dp, Color(0xFFFFC93C), shape)
                    .padding(vertical = 16.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "ANNUAL PLAN",
                    style = LessonTheme.answer().copy(fontSize = (13 * k).sp, color = Color(0xFF5B3A00), letterSpacing = 0.8.sp),
                    modifier = Modifier
                        .background(Color(0xFFFFC93C), CircleShape)
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                )
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = originalPrice,
                        style = LessonTheme.answer().copy(
                            fontSize = (20 * k).sp,
                            color = AuthTheme.secondaryText,
                            textDecoration = TextDecoration.LineThrough
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(text = discountedPrice, style = LessonTheme.question().copy(fontSize = (38 * k).sp))
                    Text(
                        text = "/year",
                        style = LessonTheme.answer().copy(fontSize = (18 * k).sp, color = AuthTheme.secondaryText),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Text(
                    text = "Just $monthlyEquivalent a month",
                    style = LessonTheme.answer().copy(fontSize = (17 * k).sp, color = LessonTheme.primaryEdge)
                )
                Text(
                    text = "The monthly plan is $monthlyPrice/month",
                    style = LessonTheme.answer().copy(
                        fontSize = (14 * k).sp,
                        fontWeight = FontWeight.Normal,
                        color = AuthTheme.secondaryText
                    )
                )
            }
        }
    }
}

/** Red present with a gold ribbon and bow (also the home map's gift button) */
@Composable
internal fun GiftIllustration(modifier: Modifier = Modifier) {
    val red = Color(0xFFF2545B)
    val redEdge = Color(0xFFC93A41)
    val gold = Color(0xFFFFC93C)
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val outline = Stroke(width = w * 0.035f)
        // Bow: two loops above the lid
        drawOval(gold, Offset(w * 0.22f, h * 0.06f), Size(w * 0.3f, h * 0.22f))
        drawOval(gold, Offset(w * 0.48f, h * 0.06f), Size(w * 0.3f, h * 0.22f))
        // Box body and lid
        drawRoundRect(red, Offset(w * 0.14f, h * 0.38f), Size(w * 0.72f, h * 0.56f), CornerRadius(w * 0.05f))
        drawRoundRect(redEdge, Offset(w * 0.14f, h * 0.38f), Size(w * 0.72f, h * 0.56f), CornerRadius(w * 0.05f), style = outline)
        drawRoundRect(red, Offset(w * 0.08f, h * 0.24f), Size(w * 0.84f, h * 0.18f), CornerRadius(w * 0.05f))
        drawRoundRect(redEdge, Offset(w * 0.08f, h * 0.24f), Size(w * 0.84f, h * 0.18f), CornerRadius(w * 0.05f), style = outline)
        // Ribbon down the middle
        drawRect(gold, Offset(w * 0.44f, h * 0.24f), Size(w * 0.12f, h * 0.7f))
    }
}

private fun openDiscountUrl(context: android.content.Context, url: String) {
    context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)))
}

@Preview(showBackground = true, widthDp = 834, heightDp = 1194)
@Composable
fun DiscountPaywallTabletPreview() {
    DiscountPaywallContent(timeRemaining = 85_000L, selectedPackage = null, originalPrice = "$49.99", monthlyPrice = "$4.99", showIgnore = true, isPurchasing = false, onClose = {}, onClaimClick = {}, childName = "Rim")
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun DiscountPaywallMobilePreview() {
    DiscountPaywallContent(timeRemaining = 599L, selectedPackage = null, originalPrice = "$49.99", monthlyPrice = "$4.99", showIgnore = true, isPurchasing = false, onClose = {}, onClaimClick = {}, childName = "Rim")
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
