package com.alifba.alifba.presenation.stories

import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.alifba.alifba.BuildConfig
import androidx.datastore.preferences.core.stringPreferencesKey
import com.alifba.alifba.features.authentication.dataStore
import kotlinx.coroutines.flow.firstOrNull
import com.alifba.alifba.R
import com.alifba.alifba.presenation.SubscriptionViewModel
import com.alifba.alifba.ui_components.dialogs.ParentGate
import com.alifba.alifba.ui_components.theme.darkBlue
import com.alifba.alifba.ui_components.theme.darkPink
import com.alifba.alifba.ui_components.theme.darkPurple
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.sage
import com.alifba.alifba.service.SubscriptionTrialReminderReceiver
import com.alifba.alifba.utils.requestNotificationPermission
import com.alifba.alifba.utils.shouldAskNotifications
import com.posthog.PostHog
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PackageType
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.models.StoreTransaction
import kotlinx.coroutines.delay

// Gemini-style spinning gradient ring, matching DiscountPaywall.kt/AnnualDiscount35Paywall.kt's
// independent copies — the array repeats its first color at the end so the sweep gradient
// loops seamlessly with no visible seam as it rotates.
private val rainbowGradientColors = listOf(
    Color(0xFFFF5F6D), Color(0xFFFFC371), Color(0xFFF9F871),
    Color(0xFF6DE1D2), Color(0xFF7C83FD), Color(0xFFC589E8),
    Color(0xFFFF5F6D)
)

// Rotates only the gradient's *colors*, never the geometry it's drawn onto — using Android's
// native Shader.setLocalMatrix(), which is built exactly for this ("re-sample this paint pattern
// rotated, independent of whatever path it's filling/stroking"). Combined with Compose's own
// Modifier.border(width, brush, shape) — a proven, already-correct primitive for stroking a
// shape's exact outline — this sidesteps every mistake from three earlier attempts: rotating the
// drawn rectangle itself (swings a wide button's corners outside its bounds into a diagonal
// line), and masking a Stroke draw with BlendMode.DstIn (doesn't erase pixels the stroke never
// touched, so the ungated gradient stayed visible everywhere). Here the shape never moves and the
// brush never touches geometry — they're fully independent by construction, not by coincidence.
private class RotatingSweepGradientBrush(
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
    onClick: () -> Unit,
    enabled: Boolean,
    height: Dp,
    cornerRadius: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rainbowRotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(3000, easing = LinearEasing)),
        label = "rainbowAngle"
    )
    val strokeWidth = 4.21875.dp
    val ringBrush = remember(angle) { RotatingSweepGradientBrush(rainbowGradientColors, angle) }

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
                .background(if (enabled) navyBlue else navyBlue.copy(alpha = 0.6f))
                .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun RevenueCatPaywall(
    onClose: () -> Unit,
    source: String,
    requireParentGate: Boolean = true,
    offeringId: String? = null,
    // Fired right before onClose(), but only on an actual purchase or restore — never on skip.
    // subscriptionViewModel.setPremium() below writes to DataStore asynchronously, so callers
    // that need to know "did they just buy" at onClose-time can't rely on reading isPremium.value
    // there (it hasn't necessarily propagated yet); this callback is the race-free signal.
    onPurchased: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val subscriptionViewModel: SubscriptionViewModel = hiltViewModel()
    val configuration = LocalConfiguration.current
    val scale = if (configuration.screenWidthDp >= 600) 1.3f else 1f
    val compactPhone = configuration.screenWidthDp < 390
    val headingSize = when {
        configuration.screenWidthDp >= 600 -> scaledSp(32, scale)
        compactPhone -> scaledSp(20, scale)
        else -> scaledSp(22, scale)
    }
    val contentVerticalPadding = if (configuration.screenWidthDp >= 600) 40.dp else scaledDp(24, scale)

    var currentOfferings by remember { mutableStateOf<Offerings?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedPackage by remember { mutableStateOf<Package?>(null) }
    var gateVerified by remember { mutableStateOf(false) }
    var showSkip by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var isPurchasing by remember { mutableStateOf(false) }

    // Consumes the system back gesture/button while a purchase or restore is in flight — the
    // overlay added below blocks touches to the on-screen Skip/Restore/CTA controls, but the
    // hardware back button is a separate input channel a touch-blocking overlay alone doesn't
    // stop.
    BackHandler(enabled = isPurchasing || isRestoring) {}
    var showNotificationRationale by remember { mutableStateOf(false) }
    var pendingPurchase by remember { mutableStateOf(false) }
    // Captured at the child's-name onboarding screen and persisted under this key — read
    // directly off the same DataStore OnboardingDataStoreManager wraps (matches
    // childProfileRegistration.kt's own read of it) rather than injecting the manager itself,
    // since this composable already has the Context it needs. This paywall renders immediately
    // after onboarding, before signup, so there's no account/profile yet to read a name from
    // any other source. Stays null (falls back to the generic headline) if unset.
    var onboardingChildName by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        val savedName = context.dataStore.data.firstOrNull()
            ?.get(stringPreferencesKey("onboarding_child_name"))
        if (!savedName.isNullOrBlank()) {
            onboardingChildName = savedName
        }
    }
    var hasPromptedNotification by remember { mutableStateOf(false) }

    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )

    var reloadKey by remember { mutableStateOf(0) }

    LaunchedEffect(source) {
        try {
            if (BuildConfig.DEBUG) {
                Log.d("PostHog", "capture viewed_paywall source=$source")
            }
            PostHog.capture(
                event = "viewed_paywall",
                properties = mapOf("source" to source)
            )
            if (BuildConfig.DEBUG) {
                PostHog.flush()
            }
        } catch (e: Exception) {
            Log.e("PostHog", "Failed to capture paywall view: ${e.message}", e)
        }
    }

    LaunchedEffect(source, requireParentGate, gateVerified) {
        showSkip = false
        if (!requireParentGate || gateVerified) {
            delay(3000)
            showSkip = true
        }
    }

    LaunchedEffect(reloadKey) {
        isLoading = true
        errorMessage = null
        Purchases.sharedInstance.getOfferings(
            object : ReceiveOfferingsCallback {
                override fun onReceived(offerings: Offerings) {
                    isLoading = false
                    val current = if (offeringId != null) offerings[offeringId] else offerings.current
                    if (current == null || current.availablePackages.isEmpty()) {
                        errorMessage = "No products available"
                        return
                    }
                    currentOfferings = offerings

                    val annual = current.availablePackages.firstOrNull { it.packageType == PackageType.ANNUAL }
                    val monthly = current.availablePackages.firstOrNull { it.packageType == PackageType.MONTHLY }
                    val lifetime = current.availablePackages.firstOrNull { it.packageType == PackageType.LIFETIME }
                    selectedPackage = annual ?: monthly ?: lifetime ?: current.availablePackages.firstOrNull()
                }

                override fun onError(error: PurchasesError) {
                    isLoading = false
                    errorMessage = error.message ?: "Failed to load products"
                }
            }
        )
    }

    if (requireParentGate && !gateVerified) {
        ParentGate(
            onVerified = { gateVerified = true },
            onDismiss = { onClose() }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.paywallbg),
            contentDescription = "Premium background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.45f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = scaledDp(24, scale), vertical = contentVerticalPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showSkip) {
                    Text(
                        text = "Skip for now",
                        fontFamily = alifbaFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = scaledSp(14, scale),
                        color = Color.Black,
                        modifier = Modifier.clickable(enabled = !isPurchasing && !isRestoring) { onClose() }
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                val restoreLabel = if (isRestoring) "Restoring..." else "Restore"
                Text(
                    text = restoreLabel,
                    fontFamily = alifbaFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = scaledSp(14, scale),
                    color = Color.Black,
                    modifier = Modifier.clickable(enabled = !isPurchasing && !isRestoring) {
                        if (isRestoring) return@clickable
                        isRestoring = true
                        Purchases.sharedInstance.restorePurchases(
                            object : ReceiveCustomerInfoCallback {
                                override fun onReceived(customerInfo: CustomerInfo) {
                                    isRestoring = false
                                    val isPro = customerInfo.entitlements.active
                                        .containsKey("Alifba Pro")
                                    subscriptionViewModel.setPremium(isPro)
                                    if (isPro) {
                                        onPurchased()
                                        onClose()
                                    } else {
                                        errorMessage = "No active subscription found"
                                    }
                                }

                                override fun onError(error: PurchasesError) {
                                    isRestoring = false
                                    errorMessage = error.message ?: "Restore failed"
                                }
                            }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(scaledDp(16, scale)))

            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(scaledDp(12, scale)))
                        Text(
                            text = "Loading plans…",
                            fontFamily = alifbaFont,
                            color = Color.Black
                        )
                    }
                }

                errorMessage != null -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Something went wrong",
                            fontFamily = alifbaFont,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(scaledDp(8, scale)))
                        Text(
                            text = errorMessage.orEmpty(),
                            fontFamily = alifbaFont,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(scaledDp(16, scale)))
                        Button(onClick = { reloadKey++ }) {
                            Text(text = "Retry", fontFamily = alifbaFont)
                        }
                    }
                }

                else -> {
                    val currentOffering = currentOfferings?.current
                    val availablePackages = currentOffering?.availablePackages.orEmpty()

                    val annual = availablePackages.firstOrNull {
                        it.packageType == PackageType.ANNUAL
                    }
                    val monthly = availablePackages.firstOrNull {
                        it.packageType == PackageType.MONTHLY
                    }
                    val lifetime = availablePackages.firstOrNull {
                        it.packageType == PackageType.LIFETIME
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Spacer(modifier = Modifier.height(scaledDp(8, scale)))

                        Text(
                            text = buildAnnotatedString {
                                if (onboardingChildName != null) {
                                    append("Give ")
                                    withStyle(
                                        style = SpanStyle(
                                            background = sage,
                                            color = Color.Black
                                        )
                                    ) {
                                        append(onboardingChildName)
                                    }
                                    append(" the\ngift of Islamic knowledge")
                                } else {
                                    append("Unlock ")
                                    withStyle(
                                        style = SpanStyle(
                                            background = sage,
                                            color = Color.Black
                                        )
                                    ) {
                                        append("Alifba Pro")
                                    }
                                    append("\nfor your family")
                                }
                            },
                            fontFamily = alifbaFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = headingSize,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Build a gentle daily Islamic habit",
                            fontFamily = alifbaFont,
                            fontWeight = FontWeight.Normal,
                            fontSize = scaledSp(16, scale),
                            color = Color.Black.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(scaledDp(16, scale)))

                        val testimonials = listOf(
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

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = scaledDp(4, scale))
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(scaledDp(12, scale))
                        ) {
                            testimonials.forEach { (title, name, quote) ->
                                TestimonialCard(
                                    title = title,
                                    name = name,
                                    quote = quote,
                                    fontFamily = alifbaFont,
                                    scale = scale
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(scaledDp(12, scale)))



                        Spacer(modifier = Modifier.height(scaledDp(12, scale)))

                        FeatureRow(
                            title = "Everything in Alifba Pro",
                            description = "Multiple profiles, unlimited learning, and full story access.",
                            iconResId = R.drawable.crown,
                            fontFamily = alifbaFont,
                            scale = scale
                        )
                        Spacer(modifier = Modifier.height(scaledDp(12, scale)))

                        FeatureRow(
                            title = "Bite-sized daily lessons",
                            description = "Meaningful 5-minute lessons to build a daily Islamic routine.",
                            iconResId = R.drawable.learning,
                            fontFamily = alifbaFont,
                            scale = scale
                        )
                        Spacer(modifier = Modifier.height(scaledDp(12, scale)))

                        FeatureRow(
                            title = "Prophet stories library",
                            description = "Unlimited access to all our immersive audio stories.",
                            iconResId = R.drawable.audiostory,
                            fontFamily = alifbaFont,
                            scale = scale
                        )
                        Spacer(modifier = Modifier.height(scaledDp(12, scale)))

                        FeatureRow(
                            title = "Up to 3 child profiles",
                            description = "Personalized learning paths and progress for each child.",
                            iconResId = R.drawable.profiles,
                            fontFamily = alifbaFont,
                            scale = scale
                        )
                        Spacer(modifier = Modifier.height(scaledDp(12, scale)))

                        FeatureRow(
                            title = "Strong Islamic character",
                            description = "Help your kids build kindness, gratitude, and good manners.",
                            iconResId = R.drawable.truetick,
                            fontFamily = alifbaFont,
                            scale = scale
                        )

                        Spacer(modifier = Modifier.height(scaledDp(16, scale)))

                        val monthlyPrice =
                            monthly?.product?.price?.formatted?.let { "$it/mo" } ?: "Monthly price"
                        val annualPrice =
                            annual?.product?.price?.formatted?.let { "$it/yr" } ?: "Annual price"
                        val lifetimePrice =
                            lifetime?.product?.price?.formatted ?: "Lifetime price"
                        
                        val timelineItems =
                            when (selectedPackage?.packageType) {
                                PackageType.MONTHLY -> listOf(
                                    Triple("Today:", monthlyPrice, TimelineMarker.SUBSCRIPTION)
                                )
                                PackageType.LIFETIME -> listOf(
                                    Triple("Today:", "Full access", TimelineMarker.SUBSCRIPTION),
                                    Triple("Pay once:", lifetimePrice, TimelineMarker.SUBSCRIPTION)
                                )
                                else -> listOf(
                                    Triple("Full access free", "(Unlock all features immediately)", TimelineMarker.CHECK),
                                    Triple("Reminder", "(We'll notify you about trial)", TimelineMarker.BELL),
                                    Triple("Trial ends", "(Your selected plan begins, no charges until then)", TimelineMarker.SUBSCRIPTION)
                                )
                            }

                        timelineItems.forEachIndexed { index, item ->
                            TimelineItem(
                                title = item.first,
                                detail = item.second,
                                icon = item.third,
                                fontFamily = alifbaFont,
                                isLast = index == timelineItems.lastIndex,
                                scale = scale
                            )
                            if (index != timelineItems.lastIndex) {
                                Spacer(modifier = Modifier.height(timelineItemGap(scale)))
                            }
                        }

                        Spacer(modifier = Modifier.height(scaledDp(12, scale)))

                        val planOptions = listOfNotNull(
                            monthly?.let { pkg ->
                                PlanOption(
                                    title = "Monthly",
                                    priceLine = "${pkg.product.price.formatted}/mo",
                                    strikeLine = null,
                                    secondaryLine = buildMonthlyWeeklyPriceLine(pkg),
                                    badgeText = null,
                                    pkg = pkg
                                )
                            },
                            annual?.let { pkg ->
                                PlanOption(
                                    title = "Annual",
                                    priceLine = buildAnnualPrimaryLine(pkg),
                                    strikeLine = monthly?.let { getStrikePrice(it, pkg) },
                                    secondaryLine = buildAnnualMonthlyEquivalentLine(pkg),
                                    badgeText = monthly?.let { buildAnnualSavingsBadge(it, pkg) } ?: "BEST VALUE",
                                    pkg = pkg
                                )
                            },
                            lifetime?.let { pkg ->
                                PlanOption(
                                    title = "Lifetime",
                                    priceLine = pkg.product.price.formatted,
                                    strikeLine = null,
                                    secondaryLine = null,
                                    badgeText = "Save forever",
                                    pkg = pkg
                                )
                            }
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(scaledDp(12, scale))
                        ) {
                            planOptions.forEach { option ->
                                val cardWidth = when (option.pkg.packageType) {
                                    PackageType.ANNUAL -> 160
                                    PackageType.LIFETIME -> 140
                                    else -> 120
                                }
                                PlanCard(
                                    title = option.title,
                                    priceLine = option.priceLine,
                                    strikeLine = option.strikeLine,
                                    secondaryLine = option.secondaryLine,
                                    badgeText = option.badgeText,
                                    isSelected = selectedPackage?.identifier == option.pkg.identifier,
                                    onClick = { selectedPackage = option.pkg },
                                    fontFamily = alifbaFont,
                                    highlightBorder = option.pkg.packageType == PackageType.ANNUAL,
                                    modifier = Modifier.width(scaledDp(cardWidth, scale)),
                                    scale = scale
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(scaledDp(16, scale)))

            val startPurchase = {
                val packageToPurchase = selectedPackage
                if (packageToPurchase != null && activity != null && subscriptionViewModel.tryBeginPurchase()) {
                    isPurchasing = true

                    fun finishPurchaseAttempt() {
                        isPurchasing = false
                        subscriptionViewModel.endPurchase()
                    }

                    // Fresh check against RevenueCat, not the locally-cached isPremium flag —
                    // guards against a purchase completed moments ago on a different paywall
                    // screen (e.g. the discount paywall) that this screen's own state doesn't
                    // know about yet.
                    Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
                        override fun onReceived(info: CustomerInfo) {
                            if (info.entitlements.active.containsKey("Alifba Pro")) {
                                finishPurchaseAttempt()
                                subscriptionViewModel.setPremium(true)
                                onPurchased()
                                onClose()
                                return
                            }

                            val params = PurchaseParams.Builder(activity, packageToPurchase).build()
                            Purchases.sharedInstance.purchase(
                                params,
                                object : PurchaseCallback {
                                    override fun onCompleted(
                                        storeTransaction: StoreTransaction,
                                        customerInfo: CustomerInfo
                                    ) {
                                        finishPurchaseAttempt()
                                        val isPro = customerInfo.entitlements.active
                                            .containsKey("Alifba Pro")
                                        subscriptionViewModel.setPremium(isPro)
                                        if (isPro) {
                                            SubscriptionTrialReminderReceiver.cancelDiscountReminder(context)
                                            when (packageToPurchase.packageType) {
                                                PackageType.ANNUAL -> {
                                                    SubscriptionTrialReminderReceiver.scheduleAnnualReminder(
                                                        context
                                                    )
                                                }
                                                else -> {
                                                    SubscriptionTrialReminderReceiver.cancelAll(context)
                                                }
                                            }
                                            onPurchased()
                                            onClose()
                                        }
                                    }

                                    override fun onError(
                                        error: PurchasesError,
                                        userCancelled: Boolean
                                    ) {
                                        finishPurchaseAttempt()
                                        errorMessage = error.message ?: "Purchase failed"
                                    }
                                }
                            )
                        }

                        override fun onError(error: PurchasesError) {
                            // Couldn't verify current entitlement state — fail safe by not
                            // starting a purchase rather than risking a duplicate charge.
                            finishPurchaseAttempt()
                            errorMessage = error.message ?: "Couldn't verify subscription status"
                        }
                    })
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val ctaTitle = when (selectedPackage?.packageType) {
                    PackageType.MONTHLY -> "Start Monthly Plan"
                    PackageType.ANNUAL -> "Start 7-Day Free Trial"
                    PackageType.LIFETIME -> "Unlock Lifetime Access"
                    else -> "Choose a plan"
                }
                val ctaSubtitle = when (selectedPackage?.packageType) {
                    PackageType.LIFETIME -> "One-time payment. Yours forever."
                    PackageType.MONTHLY -> "Billed today. Cancel anytime."
                    PackageType.ANNUAL -> "No payment now. Cancel anytime."
                    else -> "Select a plan to continue."
                }
                RainbowClaimOfferButton(
                    onClick = {
                        val selectedIsAnnual = selectedPackage?.packageType == PackageType.ANNUAL
                        val needsNotificationPermission = context.shouldAskNotifications()
                        if (selectedIsAnnual && needsNotificationPermission && !hasPromptedNotification) {
                            hasPromptedNotification = true
                            pendingPurchase = true
                            showNotificationRationale = true
                        } else {
                            startPurchase()
                        }
                    },
                    enabled = selectedPackage != null && !isPurchasing && !isRestoring,
                    height = scaledDp(52, scale),
                    cornerRadius = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isPurchasing) "Processing..." else ctaTitle,
                            fontFamily = alifbaFont,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = scaledSp(16, scale)
                        )
                        if (!isPurchasing) {
                            Text(
                                text = ctaSubtitle,
                                fontFamily = alifbaFont,
                                fontWeight = FontWeight.Normal,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = scaledSp(12, scale)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(scaledDp(4, scale)))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.VerifiedUser,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier
                            .width(scaledDp(16, scale))
                            .height(scaledDp(16, scale))
                    )
                    Spacer(modifier = Modifier.width(scaledDp(6, scale)))
                    Text(
                        text = "Google Play Protect",
                        fontFamily = alifbaFont,
                        color = Color.Black,
                        fontSize = scaledSp(12, scale)
                    )
                }

                Spacer(modifier = Modifier.height(scaledDp(4, scale)))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Privacy policy",
                        fontFamily = alifbaFont,
                        color = Color.Black,
                        modifier = Modifier.clickable {
                            openUrl(context, "https://alifba.xyz/privacy-policy")
                        }
                    )

                    Spacer(modifier = Modifier.width(scaledDp(24, scale)))

                    Text(
                        text = "Terms of service",
                        fontFamily = alifbaFont,
                        color = Color.Black,
                        modifier = Modifier.clickable {
                            openUrl(context, "https://alifba.xyz/terms-of-service")
                        }
                    )
                }

                if (showNotificationRationale) {
                    AlertDialog(
                        onDismissRequest = {
                            showNotificationRationale = false
                            if (pendingPurchase) {
                                pendingPurchase = false
                                startPurchase()
                            }
                        },
                        title = {
                            Text(text = "Enable Notifications")
                        },
                        text = {
                            Text(
                                text = "Allow notifications so we can remind you before your yearly subscription starts."
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    requestNotificationPermission(context)
                                    showNotificationRationale = false
                                    if (pendingPurchase) {
                                        pendingPurchase = false
                                        startPurchase()
                                    }
                                }
                            ) {
                                Text(text = "Allow")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showNotificationRationale = false
                                    if (pendingPurchase) {
                                        pendingPurchase = false
                                        startPurchase()
                                    }
                                }
                            ) {
                                Text(text = "Not now")
                            }
                        }
                    )
                }
            }

            // Blocking overlay while a purchase or restore is in flight — on top of everything
            // above (background, Skip for now/Restore, plan cards, CTA), until the RevenueCat
            // completion callback fires, success, failure, or cancellation alike.
            if (isPurchasing || isRestoring) {
                PurchaseProcessingOverlay()
            }
        }
    }
}

@Composable
private fun FeatureRow(
    title: String,
    description: String,
    iconResId: Int,
    fontFamily: FontFamily,
    scale: Float = 1f
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .width(scaledDp(32, scale))
                .height(scaledDp(32, scale))
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                modifier = Modifier
                    .width(scaledDp(18, scale))
                    .height(scaledDp(18, scale))
            )
        }

        Spacer(modifier = Modifier.width(scaledDp(12, scale)))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = scaledSp(18, scale),
                color = Color.Black
            )
            Text(
                text = description,
                fontFamily = fontFamily,
                color = Color.Black.copy(alpha = 0.75f),
                fontSize = scaledSp(14, scale)
            )
        }
    }
}

private data class PlanOption(
    val title: String,
    val priceLine: String,
    val strikeLine: String?,
    val secondaryLine: String?,
    val badgeText: String?,
    val pkg: Package
)

@Composable
private fun TestimonialCard(
    title: String,
    name: String,
    quote: String,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
    scale: Float = 1f
) {
    Card(
        modifier = modifier
            .width(scaledDp(240, scale)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = scaledDp(12, scale),
                vertical = scaledDp(6, scale)
            ),
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
                        tint = Color(0xffFD8300),
                        modifier = Modifier
                            .width(scaledDp(12, scale))
                            .height(scaledDp(12, scale))
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
                style = androidx.compose.ui.text.TextStyle(hyphens = Hyphens.None),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(scaledDp(4, scale)))
            Text(
                text = quote,
                fontFamily = fontFamily,
                fontSize = scaledSp(11, scale),
                color = darkBlue.copy(alpha = 0.75f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = androidx.compose.ui.text.TextStyle(hyphens = Hyphens.None)
            )
            Spacer(modifier = Modifier.height(scaledDp(3, scale)))
            Text(
                text = "$name",
                fontFamily = fontFamily,
                fontSize = scaledSp(10, scale),
                color = darkBlue.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}

private enum class TimelineMarker {
    CHECK,
    BELL,
    SUBSCRIPTION
}

@Composable
private fun TimelineItem(
    title: String,
    detail: String,
    icon: TimelineMarker,
    fontFamily: FontFamily,
    isLast: Boolean = false,
    scale: Float = 1f
) {
    val isTablet = scale > 1f
    val dotSize = if (isTablet) 18.dp else scaledDp(14, scale)
    val connectorGap = if (isTablet) 2.dp else scaledDp(2, scale)
    val connectorWidth = if (isTablet) 2.dp else scaledDp(2, scale)
    val connectorHeight = if (isTablet) 34.dp else scaledDp(26, scale)
    val iconSize = if (isTablet) 11.dp else scaledDp(9, scale)
    val contentAlpha = if (title.startsWith("Reminder")) 0.5f else 1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(contentAlpha),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(dotSize)
                    .height(dotSize)
                    .background(Color.White, CircleShape)
                    .border(
                        width = if (isTablet) 1.5.dp else 1.dp,
                        color = Color.Black,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (icon) {
                    TimelineMarker.CHECK -> {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier
                                .width(iconSize)
                                .height(iconSize)
                        )
                    }
                    TimelineMarker.BELL -> {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier
                                .width(iconSize)
                                .height(iconSize)
                        )
                    }
                    TimelineMarker.SUBSCRIPTION -> {
                        Image(
                            painter = painterResource(id = R.drawable.crown),
                            contentDescription = null,
                            modifier = Modifier
                                .width(iconSize)
                                .height(iconSize)
                        )
                    }
                }
            }
            if (!isLast) {
                Spacer(modifier = Modifier.height(connectorGap))
                Box(
                    modifier = Modifier
                        .width(connectorWidth)
                        .height(connectorHeight)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(scaledDp(12, scale)))

        Row {
            Text(
                text = title,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = scaledSp(14, scale),
                color = Color.Black
            )
            Spacer(modifier = Modifier.width(scaledDp(4, scale)))
            Text(
                text = detail,
                fontFamily = fontFamily,
                fontSize = scaledSp(14, scale),
                color = Color.Black.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    priceLine: String,
    strikeLine: String?,
    secondaryLine: String?,
    badgeText: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    fontFamily: FontFamily,
    highlightBorder: Boolean = false,
    modifier: Modifier = Modifier,
    scale: Float = 1f
) {
    val borderColor = when {
        isSelected -> navyBlue
        highlightBorder -> darkPurple
        else -> Color(0xFFE6E6E6)
    }
    val badgeColor =
        if (badgeText?.startsWith("Save", ignoreCase = true) == true) sage else darkPink
    val badgeTextColor = Color.White
    val cardHeight = if (highlightBorder || badgeText != null) 130 else 96

    Box(modifier = modifier.height(scaledDp(cardHeight, scale))) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(
                if (isSelected) scaledDp(4, scale) else scaledDp(1, scale),
                borderColor
            ),
            onClick = onClick
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = scaledDp(12, scale),
                        vertical = scaledDp(8, scale)
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        fontSize = scaledSp(12, scale),
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Normal,
                        color = darkBlue
                    )
                    Spacer(modifier = Modifier.height(scaledDp(2, scale)))
                    val priceFont = when {
                        highlightBorder -> scaledSp(15, scale)
                        strikeLine != null -> scaledSp(13, scale)
                        else -> scaledSp(14, scale)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = priceLine,
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = darkBlue,
                            fontSize = priceFont,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (strikeLine != null) {
                            Spacer(modifier = Modifier.width(scaledDp(6, scale)))
                            Text(
                                text = strikeLine,
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = darkBlue.copy(alpha = 0.6f),
                                fontSize = scaledSp(11, scale),
                                textDecoration = TextDecoration.LineThrough,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (secondaryLine != null) {
                        Spacer(modifier = Modifier.height(scaledDp(2, scale)))
                        Text(
                            text = secondaryLine,
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Normal,
                            color = darkBlue.copy(alpha = 0.75f),
                            fontSize = scaledSp(10, scale),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (badgeText != null) {
                        Spacer(modifier = Modifier.height(scaledDp(8, scale)))
                        Box(
                            modifier = Modifier
                                .background(
                                    color = badgeColor,
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = badgeText,
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = scaledSp(13, scale),
                                color = badgeTextColor,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(
                                    horizontal = scaledDp(12, scale),
                                    vertical = scaledDp(4, scale)
                                )
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(scaledDp(10, scale)))
                    }
                }
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = navyBlue,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .width(scaledDp(18, scale))
                            .height(scaledDp(18, scale))
                    )
                }
            }
        }
    }
}

private fun openUrl(context: android.content.Context, url: String) {
    val intent =
        android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
    context.startActivity(intent)
}

private fun buildMonthlyWeeklyPriceLine(monthly: Package): String {
    val weeklyPrice = monthly.product.price.amountMicros.toDouble() / 1_000_000.0 * 12.0 / 52.0
    val currency = java.util.Currency.getInstance(monthly.product.price.currencyCode)
    val formattedWeekly = formatCurrencyAmount(weeklyPrice, currency)
    return "About $formattedWeekly/week"
}

private fun buildAnnualPrimaryLine(annual: Package): String {
    return "${annual.product.price.formatted}/yr"
}

private fun buildAnnualMonthlyPriceBadge(annual: Package): String? {
    val annualCurrency = annual.product.price.currencyCode
    val annualMonthly = annual.product.price.amountMicros.toDouble() / 1_000_000.0 / 12.0
    if (annualMonthly <= 0.0) return null

    val currency = java.util.Currency.getInstance(annualCurrency)
    val annualFormatted = formatCurrencyAmount(annualMonthly, currency)
    return "$annualFormatted/mo"
}

// Reframes Annual's total into the same per-month unit Monthly is priced in, so the two cards
// are directly comparable at a glance instead of requiring mental math ($/yr vs $/mo).
private fun buildAnnualMonthlyEquivalentLine(annual: Package): String? =
    buildAnnualMonthlyPriceBadge(annual)?.let { "Just $it" }

// "SAVE X%" against what a full year would cost at the monthly rate — a concrete computed
// number converts better than a static "BEST VALUE" label, and stays correct automatically if
// either price changes.
private fun buildAnnualSavingsBadge(monthly: Package, annual: Package): String? {
    val monthlyPrice = monthly.product.price.amountMicros.toDouble() / 1_000_000.0
    val annualPrice = annual.product.price.amountMicros.toDouble() / 1_000_000.0
    val fullYearAtMonthlyRate = monthlyPrice * 12.0
    if (fullYearAtMonthlyRate <= annualPrice || fullYearAtMonthlyRate <= 0.0) return null
    val savingsPercent = (((fullYearAtMonthlyRate - annualPrice) / fullYearAtMonthlyRate) * 100).toInt()
    if (savingsPercent <= 0) return null
    return "SAVE $savingsPercent%"
}

// What a year would cost at the real monthly rate — the same basis buildAnnualSavingsBadge()
// uses, so the crossed-out price and the "SAVE X%" badge tell a consistent story instead of
// the old fabricated "current * 1.2" markup implying a fixed ~16.7% off regardless of the
// real discount.
private fun getStrikePrice(monthly: Package, annual: Package): String? {
    val monthlyPrice = monthly.product.price.amountMicros.toDouble() / 1_000_000.0
    val annualPrice = annual.product.price.amountMicros.toDouble() / 1_000_000.0
    val fullYearAtMonthlyRate = monthlyPrice * 12.0
    if (fullYearAtMonthlyRate <= annualPrice) return null
    val currency = java.util.Currency.getInstance(annual.product.price.currencyCode)
    return formatCurrencyAmount(fullYearAtMonthlyRate, currency)
}

private fun formatCurrencyAmount(amount: Double, currency: java.util.Currency): String {
    val formatter = java.text.NumberFormat.getCurrencyInstance().apply {
        this.currency = currency
        maximumFractionDigits = currency.defaultFractionDigits
        minimumFractionDigits = 0
    }
    return formatter.format(amount)
}

private fun scaledDp(base: Int, scale: Float) = (base * scale).dp

private fun scaledSp(base: Int, scale: Float) = (base * scale).sp

private fun timelineItemGap(scale: Float) = if (scale > 1f) 10.dp else scaledDp(8, scale)

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun RevenueCatPaywallPreview() {
    RevenueCatPaywallPreviewContent()
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "Monthly Selected")
@Composable
private fun RevenueCatPaywallMonthlySelectedPreview() {
    RevenueCatPaywallPreviewContent(selectedPackageType = PackageType.MONTHLY)
}

@Preview(showBackground = true, widthDp = 800, heightDp = 1280)
@Composable
private fun RevenueCatPaywallTabletPreview() {
    RevenueCatPaywallPreviewContent()
}

@Composable
private fun RevenueCatPaywallPreviewContent(
    selectedPackageType: PackageType = PackageType.ANNUAL
) {
    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )
    val configuration = LocalConfiguration.current
    val scale = if (configuration.screenWidthDp >= 600) 1.3f else 1f
    val compactPhone = configuration.screenWidthDp < 390
    val headingSize = when {
        configuration.screenWidthDp >= 600 -> scaledSp(32, scale)
        compactPhone -> scaledSp(20, scale)
        else -> scaledSp(22, scale)
    }
    val contentVerticalPadding = if (configuration.screenWidthDp >= 600) 40.dp else scaledDp(24, scale)
    val isMonthlySelected = selectedPackageType == PackageType.MONTHLY
    val isAnnualSelected = selectedPackageType == PackageType.ANNUAL
    val isLifetimeSelected = selectedPackageType == PackageType.LIFETIME
    val timelineItems = when (selectedPackageType) {
        PackageType.MONTHLY -> listOf(
            Triple("Today:", "$4.99/mo", TimelineMarker.SUBSCRIPTION)
        )
        PackageType.LIFETIME -> listOf(
            Triple("Today:", "Full access", TimelineMarker.SUBSCRIPTION),
            Triple("Pay once:", "$129.99", TimelineMarker.SUBSCRIPTION)
        )
        else -> listOf(
            Triple("Full access free", "(Unlock all features immediately)", TimelineMarker.CHECK),
            Triple("Reminder", "(We'll notify you about trial)", TimelineMarker.BELL),
            Triple("Trial ends", "(Your selected plan begins, no charges until then)", TimelineMarker.SUBSCRIPTION)
        )
    }
    val ctaTitle = when (selectedPackageType) {
        PackageType.MONTHLY -> "Start Monthly Plan"
        PackageType.LIFETIME -> "Get Lifetime Access"
        else -> "Start 7-Day Free Trial"
    }
    val ctaSubtitle = when (selectedPackageType) {
        PackageType.MONTHLY -> "Billed today. Cancel anytime."
        PackageType.LIFETIME -> "One-time payment. Lifetime updates."
        else -> "No payment now. Cancel subscriptions anytime."
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.paywallbg),
            contentDescription = "Premium background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.45f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = scaledDp(24, scale), vertical = contentVerticalPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Skip for now",
                    fontFamily = alifbaFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = scaledSp(14, scale),
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(scaledDp(16, scale)))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(scaledDp(8, scale)))

                Text(
                    text = buildAnnotatedString {
                        append("Unlock ")
                        withStyle(
                            style = SpanStyle(
                                background = sage,
                                color = Color.Black
                            )
                        ) {
                            append("Alifba Pro")
                        }
                        append("\nfor your family")
                    },
                    fontFamily = alifbaFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = headingSize,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(scaledDp(16, scale)))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = scaledDp(4, scale))
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(scaledDp(12, scale))
                ) {
                    TestimonialCard(
                        title = "Premium is worth for our family",
                        name = "Aisha M.",
                        quote = "The quizzes are actually fun, Like, my daughter doesn't even realize she's being tested. She just wants to earn the next badge.",
                        fontFamily = alifbaFont,
                        scale = scale
                    )
                    TestimonialCard(
                        title = "We chose Premium and love it",
                        name = "Yusra",
                        quote = "The gamification is spot on. My son was so proud when he unlocked his first collectible badge. Great motivation system!",
                        fontFamily = alifbaFont,
                        scale = scale
                    )
                    TestimonialCard(
                        title = "The premium is a good deal to learn more",
                        name = "Amana",
                        quote = "We use it for car rides instead of just watching videos. The audio quality is amazing, and the stories are perfect for keeping them quiet and engaged.",
                        fontFamily = alifbaFont,
                        scale = scale
                    )
                }

                Spacer(modifier = Modifier.height(scaledDp(12, scale)))


                Spacer(modifier = Modifier.height(scaledDp(12, scale)))

                FeatureRow(
                    title = "Everything in Alifba Pro",
                    description = "Multiple profiles, unlimited learning, and full audio story access.",
                    iconResId = R.drawable.crown,
                    fontFamily = alifbaFont,
                    scale = scale
                )

                Spacer(modifier = Modifier.height(scaledDp(12, scale)))

                timelineItems.forEachIndexed { index, item ->
                    TimelineItem(
                        title = item.first,
                        detail = item.second,
                        icon = item.third,
                        fontFamily = alifbaFont,
                        isLast = index == timelineItems.lastIndex,
                        scale = scale
                    )
                    if (index != timelineItems.lastIndex) {
                        Spacer(modifier = Modifier.height(timelineItemGap(scale)))
                    }
                }

                Spacer(modifier = Modifier.height(scaledDp(12, scale)))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(scaledDp(12, scale))
                ) {
                    PlanCard(
                        title = "Monthly",
                        priceLine = "$4.99/mo",
                        strikeLine = null,
                        secondaryLine = "About $1.15/week",
                        badgeText = null,
                        isSelected = isMonthlySelected,
                        onClick = {},
                        fontFamily = alifbaFont,
                        highlightBorder = false,
                        modifier = Modifier.width(scaledDp(120, scale)),
                        scale = scale
                    )
                    PlanCard(
                        title = "Annual",
                        priceLine = "$49.99/yr",
                        strikeLine = "$59.88/yr",
                        secondaryLine = "Just $4.17/mo",
                        badgeText = "SAVE 20%",
                        isSelected = isAnnualSelected,
                        onClick = {},
                        fontFamily = alifbaFont,
                        highlightBorder = true,
                        modifier = Modifier.width(scaledDp(160, scale)),
                        scale = scale
                    )
                    PlanCard(
                        title = "Lifetime",
                        priceLine = "$129.99",
                        strikeLine = null,
                        secondaryLine = null,
                        badgeText = "Save forever",
                        isSelected = isLifetimeSelected,
                        onClick = {},
                        fontFamily = alifbaFont,
                        highlightBorder = false,
                        modifier = Modifier.width(scaledDp(150, scale)),
                        scale = scale
                    )
                }

                Spacer(modifier = Modifier.height(scaledDp(12, scale)))
            }

            Spacer(modifier = Modifier.height(scaledDp(16, scale)))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RainbowClaimOfferButton(
                    onClick = {},
                    enabled = true,
                    height = scaledDp(52, scale),
                    cornerRadius = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = ctaTitle,
                            fontFamily = alifbaFont,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = scaledSp(16, scale)
                        )
                        Text(
                            text = ctaSubtitle,
                            fontFamily = alifbaFont,
                            fontWeight = FontWeight.Normal,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = scaledSp(12, scale)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(scaledDp(4, scale)))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.VerifiedUser,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier
                            .width(scaledDp(16, scale))
                            .height(scaledDp(16, scale))
                    )
                    Spacer(modifier = Modifier.width(scaledDp(6, scale)))
                    Text(
                        text = "Secured by Google Play",
                        fontFamily = alifbaFont,
                        color = Color.Black,
                        fontSize = scaledSp(12, scale),
                        fontWeight = FontWeight.Bold

                    )
                }

                Spacer(modifier = Modifier.height(scaledDp(4, scale)))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Privacy policy",
                        fontFamily = alifbaFont,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(scaledDp(24, scale)))

                    Text(
                        text = "Terms of service",
                        fontFamily = alifbaFont,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold

                    )
                }
            }
        }
    }
}

// Independent copy of the same blocking overlay used in DiscountPaywall.kt and
// AnnualDiscount35Paywall.kt — a dimmed scrim that's itself opaque enough to intercept every
// touch to whatever's underneath, so it doubles as the disabling mechanism for controls that
// weren't explicitly given an enabled = !isPurchasing check.
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
