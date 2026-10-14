package com.alifba.alifba.presenation.stories

import com.alifba.alifba.utils.PremiumAccess
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import com.alifba.alifba.ui_components.widgets.AuthTheme
import com.alifba.alifba.ui_components.widgets.DotLottieView
import com.alifba.alifba.ui_components.widgets.LessonButtonKind
import com.alifba.alifba.ui_components.widgets.LessonPrimaryButton
import com.alifba.alifba.ui_components.widgets.LessonTheme
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager
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
import kotlinx.coroutines.tasks.await

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
        // A signed-in parent sees the current child's name
        loadPaywallChildName(context)?.let { onboardingChildName = it }
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

    // Restore: unchanged, just moved out of the old inline Text.clickable
    val restorePurchases = {
        if (!isRestoring) {
            isRestoring = true
            Purchases.sharedInstance.restorePurchases(
                object : ReceiveCustomerInfoCallback {
                    override fun onReceived(customerInfo: CustomerInfo) {
                        isRestoring = false
                        val isPro = PremiumAccess.isPremium(customerInfo)
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
    }

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
                    if (PremiumAccess.isPremium(info)) {
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
                                val isPro = PremiumAccess.isPremium(customerInfo)
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

    val onPurchaseClick = {
        val selectedIsAnnual = selectedPackage?.packageType == PackageType.ANNUAL
        val needsNotificationPermission = context.shouldAskNotifications()
        if (selectedIsAnnual && needsNotificationPermission && !hasPromptedNotification) {
            hasPromptedNotification = true
            pendingPurchase = true
            showNotificationRationale = true
        } else {
            startPurchase()
        }
    }

    // What the screen shows, worked out from the loaded offering (display only)
    val availablePackages = currentOfferings?.current?.availablePackages.orEmpty()
    val annual = availablePackages.firstOrNull { it.packageType == PackageType.ANNUAL }
    val monthly = availablePackages.firstOrNull { it.packageType == PackageType.MONTHLY }
    val lifetime = availablePackages.firstOrNull { it.packageType == PackageType.LIFETIME }
    val trialDays = annual?.let { annualTrialDays(it) } ?: 7

    val planTiles = listOfNotNull(
        monthly?.let {
            PaywallPlanTileModel(it.identifier, "Monthly", it.product.price.formatted, "per month", null, it)
        },
        annual?.let { pkg ->
            PaywallPlanTileModel(
                pkg.identifier,
                "Annual",
                pkg.product.price.formatted,
                buildAnnualMonthlyPriceBadge(pkg) ?: "per year",
                monthly?.let { buildAnnualSavingsBadge(it, pkg) } ?: "BEST VALUE",
                pkg
            )
        },
        lifetime?.let {
            PaywallPlanTileModel(it.identifier, "Lifetime", it.product.price.formatted, "pay once", null, it)
        }
    )

    val selectedPrice = selectedPackage?.product?.price?.formatted.orEmpty()
    val ctaTitle = when (selectedPackage?.packageType) {
        PackageType.MONTHLY -> "Start monthly plan"
        PackageType.ANNUAL -> "Start $trialDays-day free trial"
        PackageType.LIFETIME -> "Unlock lifetime access"
        else -> "Choose a plan"
    }
    val ctaSubtitle = when (selectedPackage?.packageType) {
        PackageType.MONTHLY -> "$selectedPrice/month · Cancel anytime"
        PackageType.ANNUAL -> "Then $selectedPrice/year · Cancel anytime"
        PackageType.LIFETIME -> "One-time payment · Yours forever"
        else -> "Select a plan to continue"
    }
    val footnoteLead = when (selectedPackage?.packageType) {
        PackageType.ANNUAL -> "No payment now."
        PackageType.LIFETIME -> "Pay once."
        else -> "Billed today."
    }
    // The annual reminder is scheduled 3 days in (SubscriptionTrialReminderReceiver)
    val trialSteps = if (selectedPackage?.packageType == PackageType.ANNUAL) {
        listOf(
            Triple("Today", "Full access, free", TimelineMarker.CHECK),
            Triple("Day 3", "We remind you", TimelineMarker.BELL),
            Triple("Day $trialDays", "Plan starts", TimelineMarker.SUBSCRIPTION)
        )
    } else emptyList()

    Box(modifier = Modifier.fillMaxSize()) {
        PaywallLayout(
            childName = onboardingChildName,
            showSkip = showSkip,
            isBusy = isPurchasing || isRestoring,
            isRestoring = isRestoring,
            isLoading = isLoading,
            // Before plans load, an error replaces the screen; after, it shows above the button
            statusError = if (currentOfferings == null) errorMessage else null,
            inlineMessage = if (currentOfferings != null) errorMessage else null,
            planTiles = planTiles,
            selectedId = selectedPackage?.identifier,
            trialSteps = trialSteps,
            ctaTitle = if (isPurchasing) "Processing..." else ctaTitle,
            ctaSubtitle = if (isPurchasing) null else ctaSubtitle,
            ctaEnabled = selectedPackage != null && !isPurchasing && !isRestoring,
            footnoteLead = footnoteLead,
            onSkip = { onClose() },
            onRestore = { restorePurchases() },
            onSelect = { tile -> tile.pkg?.let { selectedPackage = it } },
            onPurchase = { onPurchaseClick() },
            onRetry = { reloadKey++ },
            onOpenUrl = { openUrl(context, it) }
        )

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

        // Blocking overlay while a purchase or restore is in flight — on top of everything
        // above (background, Skip for now/Restore, plan cards, CTA), until the RevenueCat
        // completion callback fires, success, failure, or cancellation alike.
        if (isPurchasing || isRestoring) {
            PurchaseProcessingOverlay()
        }
    }
}

internal enum class TimelineMarker {
    CHECK,
    BELL,
    SUBSCRIPTION
}

private val PaywallGold = Color(0xFFFFC93C)

/** One plan card's text; [pkg] is null only in previews */
private data class PaywallPlanTileModel(
    val id: String,
    val title: String,
    val price: String,
    val detail: String,
    val badge: String?,
    val pkg: Package?
)

/**
 * The child the paywalls speak to: the current profile's name for a signed-in parent (this
 * device's active profile, as ProfileViewModel picks it), else the name typed during onboarding
 * (the paywall right after onboarding comes before sign-up).
 */
@Suppress("UNCHECKED_CAST")
internal suspend fun loadPaywallChildName(context: android.content.Context): String? {
    val prefs = context.dataStore.data.firstOrNull()
    val onboardingName = prefs?.get(stringPreferencesKey("onboarding_child_name"))?.trim()?.takeIf { it.isNotEmpty() }
    val uid = prefs?.get(stringPreferencesKey("user_id"))?.takeIf { it.isNotBlank() }
        ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        ?: return onboardingName
    return try {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val userDoc = db.collection("users").document(uid).get().await()
        val profiles = (userDoc.get("profiles") as? List<Map<String, Any>>).orEmpty()
        if (profiles.isEmpty()) return onboardingName
        val activeId = prefs?.get(stringPreferencesKey("device_id"))?.let { deviceId ->
            db.collection("users").document(uid).collection("devices").document(deviceId)
                .get().await().getString("activeProfileId")
        }
        val profile = profiles.firstOrNull { it["profileId"] == activeId }
            ?: profiles.getOrNull((userDoc.getLong("activeProfileIndex") ?: 0L).toInt())
            ?: profiles.first()
        (profile["childName"] as? String)?.trim()?.takeIf { it.isNotEmpty() } ?: onboardingName
    } catch (e: Exception) {
        onboardingName
    }
}

/** Free-trial length of the annual plan, in days, if the store product has one */
internal fun annualTrialDays(annual: Package): Int? {
    val period = annual.product.defaultOption?.freePhase?.billingPeriod ?: return null
    val days = when (period.unit) {
        com.revenuecat.purchases.models.Period.Unit.DAY -> period.value
        com.revenuecat.purchases.models.Period.Unit.WEEK -> period.value * 7
        com.revenuecat.purchases.models.Period.Unit.MONTH -> period.value * 30
        com.revenuecat.purchases.models.Period.Unit.YEAR -> period.value * 365
        else -> return null
    }
    return days.takeIf { it > 0 }
}

/**
 * The whole paywall screen, display only: the sky-and-hill art from onboarding, a short scroll
 * (Moon, headline, reviews, benefits, trial steps) and a pinned white panel with the plans, the
 * CTA and the terms line, so the price choice and the button never need scrolling to.
 */
@Composable
private fun PaywallLayout(
    childName: String?,
    showSkip: Boolean,
    isBusy: Boolean,
    isRestoring: Boolean,
    isLoading: Boolean,
    statusError: String?,
    inlineMessage: String?,
    planTiles: List<PaywallPlanTileModel>,
    selectedId: String?,
    trialSteps: List<Triple<String, String, TimelineMarker>>,
    ctaTitle: String,
    ctaSubtitle: String?,
    ctaEnabled: Boolean,
    footnoteLead: String,
    onSkip: () -> Unit,
    onRestore: () -> Unit,
    onSelect: (PaywallPlanTileModel) -> Unit,
    onPurchase: () -> Unit,
    onRetry: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    val k = LessonTheme.scale()

    Box(modifier = Modifier.fillMaxSize()) {
        // Same sky-and-hill art as onboarding
        Image(
            painter = painterResource(id = R.drawable.onboardingbgtwo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Skip appears after a few seconds but keeps its place, so nothing shifts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .heightIn(min = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Skip for now",
                    style = LessonTheme.answer().copy(fontSize = (17 * k).sp, color = LessonTheme.ink.copy(alpha = 0.75f)),
                    modifier = Modifier
                        .alpha(if (showSkip) 1f else 0f)
                        .clickable(enabled = showSkip && !isBusy) { onSkip() }
                )
                Text(
                    text = if (isRestoring) "Restoring..." else "Restore",
                    style = LessonTheme.answer().copy(fontSize = (17 * k).sp, color = LessonTheme.primaryEdge),
                    modifier = Modifier.clickable(enabled = !isBusy) { onRestore() }
                )
            }

            if (isLoading || statusError != null) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = LessonTheme.margin),
                    contentAlignment = Alignment.Center
                ) {
                    PaywallStatusCard(isLoading = isLoading, error = statusError, onRetry = onRetry)
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = LessonTheme.margin)
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 560.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DotLottieView(name = "moonwave", modifier = Modifier.size((76 * k).dp))
                        PaywallHeadline(childName = childName, modifier = Modifier.padding(top = 2.dp))
                        PaywallTestimonials(modifier = Modifier.padding(top = 16.dp))
                        PaywallBenefitsCard(modifier = Modifier.padding(top = 14.dp))
                        if (trialSteps.size == 3) {
                            PaywallTrialRow(steps = trialSteps, modifier = Modifier.padding(top = 18.dp))
                        }
                    }
                }

                PaywallPurchasePanel(
                    planTiles = planTiles,
                    selectedId = selectedId,
                    inlineMessage = inlineMessage,
                    ctaTitle = ctaTitle,
                    ctaSubtitle = ctaSubtitle,
                    ctaEnabled = ctaEnabled,
                    footnoteLead = footnoteLead,
                    onSelect = onSelect,
                    onPurchase = onPurchase,
                    onOpenUrl = onOpenUrl
                )
            }
        }
    }
}

@Composable
private fun PaywallStatusCard(isLoading: Boolean, error: String?, onRetry: () -> Unit) {
    val k = LessonTheme.scale()
    Column(
        modifier = Modifier
            .widthIn(max = 560.dp)
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(24.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = LessonTheme.primary)
            Text(text = "Loading plans…", style = LessonTheme.answer().copy(fontSize = (17 * k).sp))
        } else {
            Text(text = "Something went wrong", style = LessonTheme.answer().copy(fontSize = (20 * k).sp))
            Text(
                text = error.orEmpty(),
                style = LessonTheme.answer().copy(
                    fontSize = (15 * k).sp,
                    fontWeight = FontWeight.Normal,
                    color = AuthTheme.secondaryText,
                    textAlign = TextAlign.Center
                )
            )
            LessonPrimaryButton(title = "Retry", modifier = Modifier.padding(top = 8.dp), onClick = onRetry)
        }
    }
}

/** "Give Rim the gift of / Islamic knowledge", the second line on a yellow marker */
@Composable
private fun PaywallHeadline(childName: String?, modifier: Modifier = Modifier) {
    val k = LessonTheme.scale()
    val style = LessonTheme.question().copy(fontSize = (28 * k).sp, textAlign = TextAlign.Center)
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Give ${childName ?: "your child"} the gift of",
            style = style,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "Islamic knowledge",
            style = style,
            modifier = Modifier.drawBehind {
                val inset = 4.dp.toPx()
                drawRoundRect(
                    color = PaywallGold.copy(alpha = 0.85f),
                    topLeft = Offset(-inset, size.height * 0.5f),
                    size = Size(size.width + inset * 2, size.height * 0.36f),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }
        )
    }
}

private data class PaywallReview(val title: String, val quote: String, val name: String)

private val paywallReviews = listOf(
    PaywallReview(
        "Worth it for our family",
        "The quizzes are actually fun! My daughter doesn't even realise she's being tested. She just wants to earn the next badge.",
        "Aisha M."
    ),
    PaywallReview(
        "We chose Premium and love it",
        "The gamification is spot on. My son was so proud when he unlocked his first collectible badge.",
        "Yusra"
    ),
    PaywallReview(
        "A good deal to learn more",
        "We use it for car rides instead of videos. The stories keep them quiet and engaged.",
        "Amana"
    )
)

/** Swipeable parent reviews with page dots */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PaywallTestimonials(modifier: Modifier = Modifier) {
    val k = LessonTheme.scale()
    val pagerState = rememberPagerState(pageCount = { paywallReviews.size })
    val shape = RoundedCornerShape(LessonTheme.radius)
    val context = LocalContext.current
    // "Remove animations" in the system settings: no auto-advance
    val reduceMotion = remember {
        android.provider.Settings.Global.getFloat(
            context.contentResolver,
            android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }
    // Set once the parent swipes; from then on the cards stay where they put them
    var userSwiped by remember { mutableStateOf(false) }
    LaunchedEffect(pagerState) {
        pagerState.interactionSource.interactions.collect { interaction ->
            if (interaction is androidx.compose.foundation.interaction.DragInteraction.Start) userSwiped = true
        }
    }
    // Slides to the next review every 5 seconds, wrapping round, until the parent swipes. Stops
    // when the paywall leaves the screen.
    LaunchedEffect(userSwiped, reduceMotion) {
        if (userSwiped || reduceMotion) return@LaunchedEffect
        while (true) {
            delay(5_000)
            pagerState.animateScrollToPage(
                page = (pagerState.currentPage + 1) % paywallReviews.size,
                animationSpec = tween(durationMillis = 600)
            )
        }
    }

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height((148 * k).dp)
        ) { page ->
            val review = paywallReviews[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White, shape)
                    .border(2.dp, LessonTheme.line, shape)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(5) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = PaywallGold,
                            modifier = Modifier.size((16 * k).dp)
                        )
                    }
                }
                Text(
                    text = "“${review.title}”",
                    style = LessonTheme.answer().copy(fontSize = (17 * k).sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = review.quote,
                    style = LessonTheme.answer().copy(
                        fontSize = (14 * k).sp,
                        fontWeight = FontWeight.Normal,
                        color = LessonTheme.ink.copy(alpha = 0.85f)
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = review.name,
                    style = LessonTheme.answer().copy(fontSize = (13 * k).sp, color = AuthTheme.secondaryText)
                )
            }
        }

        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(paywallReviews.size) { index ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .size(width = if (selected) 22.dp else 8.dp, height = 8.dp)
                        .background(if (selected) LessonTheme.primary else Color(0xFFD9DEEA), CircleShape)
                )
            }
        }
    }
}

@Composable
private fun PaywallBenefitsCard(modifier: Modifier = Modifier) {
    val k = LessonTheme.scale()
    val shape = RoundedCornerShape(LessonTheme.radius)
    val edge = LessonTheme.answerEdge
    Box(modifier = modifier.padding(bottom = edge)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = edge)
                    .background(LessonTheme.lineEdge, shape)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape)
                    .border(2.dp, LessonTheme.line, shape)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Unlock all the benefits",
                    style = LessonTheme.answer().copy(fontSize = (21 * k).sp, textAlign = TextAlign.Center),
                    modifier = Modifier.fillMaxWidth()
                )
                PaywallBenefitRow(Icons.Outlined.People, "Multiple child profiles", "Their own progress and badges")
                PaywallBenefitRow(Icons.Outlined.CheckBox, "Every lesson", "Learn at your own pace, nothing locked")
                PaywallBenefitRow(Icons.Outlined.Headphones, "All audio stories", "Prophets, Sahaba and more")
                PaywallBenefitRow(Icons.Outlined.AutoAwesome, "New content regularly", "Fresh lessons, stories and activities")
            }
        }
    }
}

@Composable
private fun PaywallBenefitRow(icon: ImageVector, title: String, detail: String) {
    val k = LessonTheme.scale()
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(
            modifier = Modifier
                .size((40 * k).dp)
                .background(LessonTheme.sky, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = LessonTheme.primary, modifier = Modifier.size((22 * k).dp))
        }
        Column {
            Text(
                text = title,
                style = LessonTheme.answer().copy(fontSize = (17 * k).sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = detail,
                style = LessonTheme.answer().copy(
                    fontSize = (14 * k).sp,
                    fontWeight = FontWeight.Normal,
                    color = AuthTheme.secondaryText
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Today / Day N / Day M side by side, joined by a line: blue into the reminder, gold into the plan */
@Composable
internal fun PaywallTrialRow(
    steps: List<Triple<String, String, TimelineMarker>>,
    modifier: Modifier = Modifier,
    title: String = "How your free trial works"
) {
    val k = LessonTheme.scale()
    val iconSize = (34 * k).dp
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = title, style = LessonTheme.answer().copy(fontSize = (19 * k).sp))
        Box(modifier = Modifier.fillMaxWidth()) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(iconSize)
            ) {
                val third = size.width / 3f
                val y = size.height / 2f
                val stroke = 3.dp.toPx()
                drawLine(LessonTheme.primary, Offset(third / 2f, y), Offset(third * 1.5f, y), stroke)
                drawLine(PaywallGold, Offset(third * 1.5f, y), Offset(third * 2.5f, y), stroke)
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                steps.forEach { (title, detail, marker) ->
                    PaywallTrialStep(title, detail, marker, iconSize, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
internal fun PaywallTrialStep(title: String, detail: String, marker: TimelineMarker, iconSize: Dp, modifier: Modifier) {
    val k = LessonTheme.scale()
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .background(
                    when (marker) {
                        TimelineMarker.CHECK -> LessonTheme.primary
                        TimelineMarker.BELL -> Color.White
                        TimelineMarker.SUBSCRIPTION -> PaywallGold
                    },
                    CircleShape
                )
                .then(if (marker == TimelineMarker.BELL) Modifier.border(2.5.dp, LessonTheme.primary, CircleShape) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            when (marker) {
                TimelineMarker.CHECK -> Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(iconSize * 0.5f))
                TimelineMarker.BELL -> Icon(Icons.Outlined.Notifications, null, tint = LessonTheme.primary, modifier = Modifier.size(iconSize * 0.5f))
                TimelineMarker.SUBSCRIPTION -> Icon(
                    painter = painterResource(id = R.drawable.ic_crown_filled),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(iconSize * 0.52f)
                )
            }
        }
        Text(
            text = title,
            style = LessonTheme.answer().copy(fontSize = (16 * k).sp),
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            text = detail,
            style = LessonTheme.answer().copy(
                fontSize = (13 * k).sp,
                fontWeight = FontWeight.Normal,
                color = AuthTheme.secondaryText,
                textAlign = TextAlign.Center
            ),
            maxLines = 2
        )
    }
}

@Composable
private fun PaywallPurchasePanel(
    planTiles: List<PaywallPlanTileModel>,
    selectedId: String?,
    inlineMessage: String?,
    ctaTitle: String,
    ctaSubtitle: String?,
    ctaEnabled: Boolean,
    footnoteLead: String,
    onSelect: (PaywallPlanTileModel) -> Unit,
    onPurchase: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    val k = LessonTheme.scale()
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
                .padding(top = 26.dp, bottom = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                planTiles.forEach { tile ->
                    PaywallPlanTile(
                        model = tile,
                        isSelected = tile.id == selectedId,
                        onClick = { onSelect(tile) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (inlineMessage != null) {
                Text(
                    text = inlineMessage,
                    style = LessonTheme.answer().copy(
                        fontSize = (14 * k).sp,
                        fontWeight = FontWeight.Normal,
                        color = LessonTheme.wrongEdge,
                        textAlign = TextAlign.Center
                    )
                )
            }

            LessonPrimaryButton(
                kind = if (ctaEnabled) LessonButtonKind.Primary else LessonButtonKind.Disabled,
                height = (64 * k).dp,
                onClick = { if (ctaEnabled) onPurchase() }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = ctaTitle,
                        style = LessonTheme.button().copy(fontSize = (21 * k).sp, color = Color.White),
                        maxLines = 1
                    )
                    if (ctaSubtitle != null) {
                        Text(
                            text = ctaSubtitle,
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
                fontSize = (14 * k).sp,
                fontWeight = FontWeight.Normal,
                color = AuthTheme.secondaryText
            )
            val linkStyle = footStyle.copy(color = LessonTheme.primaryEdge, textDecoration = TextDecoration.Underline)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = footnoteLead, style = footStyle)
                Text(
                    text = "Terms",
                    style = linkStyle,
                    modifier = Modifier.clickable { onOpenUrl("https://alifba.xyz/terms-of-service") }
                )
                Text(text = "·", style = footStyle)
                Text(
                    text = "Privacy",
                    style = linkStyle,
                    modifier = Modifier.clickable { onOpenUrl("https://alifba.xyz/privacy-policy") }
                )
            }
        }
    }
}

/** Plan card: raised white tile, or sky blue with a blue edge when selected; optional gold badge */
@Composable
private fun PaywallPlanTile(
    model: PaywallPlanTileModel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val k = LessonTheme.scale()
    val shape = RoundedCornerShape(18.dp)
    val edge = 4.dp
    val accent = if (isSelected) LessonTheme.primaryEdge else LessonTheme.ink

    Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = edge)
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) {
                    SoundEffectManager.playClickSound()
                    onClick()
                }
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = edge)
                    .background(if (isSelected) LessonTheme.primaryEdge else LessonTheme.lineEdge, shape)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((88 * k).dp)
                    .background(if (isSelected) LessonTheme.sky else Color.White, shape)
                    .border(if (isSelected) 2.5.dp else 2.dp, if (isSelected) LessonTheme.primary else LessonTheme.line, shape)
                    .padding(horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = model.title,
                    style = LessonTheme.answer().copy(
                        fontSize = (15 * k).sp,
                        color = if (isSelected) LessonTheme.primaryEdge else AuthTheme.secondaryText
                    )
                )
                Text(
                    text = model.price,
                    style = LessonTheme.answer().copy(fontSize = (21 * k).sp, color = accent),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = model.detail,
                    style = LessonTheme.answer().copy(
                        fontSize = (13 * k).sp,
                        color = if (isSelected) LessonTheme.primaryEdge.copy(alpha = 0.85f) else AuthTheme.secondaryText
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (model.badge != null) {
            Text(
                text = model.badge.uppercase(),
                style = LessonTheme.answer().copy(fontSize = (12 * k).sp, color = Color(0xFF5B3A00)),
                modifier = Modifier
                    .offset(y = (-12).dp)
                    .background(PaywallGold, CircleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
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

@Composable
private fun PaywallLayoutPreview(selected: String) {
    PaywallLayout(
        childName = "Rim",
        showSkip = true,
        isBusy = false,
        isRestoring = false,
        isLoading = false,
        statusError = null,
        inlineMessage = null,
        planTiles = listOf(
            PaywallPlanTileModel("monthly", "Monthly", "$5.99", "per month", null, null),
            PaywallPlanTileModel("annual", "Annual", "$49.99", "$4.17/mo", "SAVE 30%", null),
            PaywallPlanTileModel("lifetime", "Lifetime", "$129.99", "pay once", null, null)
        ),
        selectedId = selected,
        trialSteps = if (selected == "annual") listOf(
            Triple("Today", "Full access, free", TimelineMarker.CHECK),
            Triple("Day 3", "We remind you", TimelineMarker.BELL),
            Triple("Day 7", "Plan starts", TimelineMarker.SUBSCRIPTION)
        ) else emptyList(),
        ctaTitle = if (selected == "annual") "Start 7-day free trial" else "Start monthly plan",
        ctaSubtitle = if (selected == "annual") "Then $49.99/year · Cancel anytime" else "$5.99/month · Cancel anytime",
        ctaEnabled = true,
        footnoteLead = if (selected == "annual") "No payment now." else "Billed today.",
        onSkip = {},
        onRestore = {},
        onSelect = {},
        onPurchase = {},
        onRetry = {},
        onOpenUrl = {}
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun RevenueCatPaywallPreview() {
    PaywallLayoutPreview(selected = "annual")
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun RevenueCatPaywallMonthlySelectedPreview() {
    PaywallLayoutPreview(selected = "monthly")
}

@Preview(showBackground = true, widthDp = 834, heightDp = 1194)
@Composable
private fun RevenueCatPaywallTabletPreview() {
    PaywallLayoutPreview(selected = "annual")
}

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
