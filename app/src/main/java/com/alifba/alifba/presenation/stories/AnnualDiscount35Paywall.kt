package com.alifba.alifba.presenation.stories

import android.app.Activity
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.BackHandler
import com.alifba.alifba.R
import com.alifba.alifba.presenation.SubscriptionViewModel
import com.alifba.alifba.ui_components.theme.darkBlue
import com.alifba.alifba.ui_components.theme.lightCandyGreen
import com.alifba.alifba.ui_components.theme.navyBlue
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
import java.text.NumberFormat
import java.util.Currency

// This offering doesn't exist under offerings.current — it's a secondary offering fetched
// by identifier on purpose, so this discount is never shown to a user unless they were
// routed here explicitly (currently: tapping the day-30 discount-offer notification).
private const val ANNUAL_DISCOUNT_35_OFFERING_ID = "annual_discount_35"
private const val PREMIUM_ENTITLEMENT_ID = "Alifba Pro"

// Gemini-style spinning gradient ring around the CTA — the array repeats its first color at
// the end so the sweep gradient loops seamlessly with no visible seam as it rotates. Kept as
// an independent copy from DiscountPaywall.kt's own version (not shared code) per this
// file's separation from that paywall.
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
private class AnnualDiscount35RotatingSweepGradientBrush(
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
    enabled: Boolean,
    fontFamily: FontFamily,
    height: androidx.compose.ui.unit.Dp,
    fontSize: androidx.compose.ui.unit.TextUnit,
    cornerRadius: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rainbowRotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(3000, easing = LinearEasing)),
        label = "rainbowAngle"
    )
    val strokeWidth = 4.21875.dp
    val ringBrush = remember(angle) { AnnualDiscount35RotatingSweepGradientBrush(rainbowGradientColors, angle) }

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
fun AnnualDiscount35Paywall(
    onClose: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val subscriptionViewModel: SubscriptionViewModel = hiltViewModel()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    var isLoading by remember { mutableStateOf(true) }
    var loadFailed by remember { mutableStateOf(false) }
    var selectedPackage by remember { mutableStateOf<Package?>(null) }
    var isPurchasing by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var showIgnore by remember { mutableStateOf(false) }
    // Standard (non-discounted) annual price, read live from offerings.current — null until
    // fetched, and left null (not shown) rather than falling back to a hardcoded price if the
    // current offering or its annual package isn't available for some reason.
    var standardAnnualPrice by remember { mutableStateOf<String?>(null) }

    // Matches DiscountPaywall's own delay before its dismiss affordance appears — same
    // timing, but tapping it here just closes the screen, with none of that flow's
    // side effect of starting a persistent 24h banner/reminder for this discount.
    LaunchedEffect(Unit) {
        delay(3000)
        showIgnore = true
    }

    LaunchedEffect(Unit) {
        Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
            override fun onReceived(offerings: Offerings) {
                standardAnnualPrice = offerings.current?.availablePackages
                    ?.firstOrNull { it.packageType == PackageType.ANNUAL }
                    ?.product?.price?.formatted
                val offering = offerings[ANNUAL_DISCOUNT_35_OFFERING_ID]
                val annualPackage = offering?.availablePackages?.firstOrNull { it.packageType == PackageType.ANNUAL }
                selectedPackage = annualPackage
                loadFailed = annualPackage == null
                isLoading = false
            }

            override fun onError(error: PurchasesError) {
                loadFailed = true
                isLoading = false
            }
        })
    }

    fun handleEntitlementResult(info: CustomerInfo) {
        if (info.entitlements.active.containsKey(PREMIUM_ENTITLEMENT_ID)) {
            subscriptionViewModel.setPremium(true)
            onSuccess()
        }
    }

    val onPurchaseClick: () -> Unit = purchase@{
        val pkg = selectedPackage ?: return@purchase
        val currentActivity = activity ?: return@purchase
        isPurchasing = true
        Purchases.sharedInstance.purchase(
            PurchaseParams.Builder(currentActivity, pkg).build(),
            object : PurchaseCallback {
                override fun onCompleted(storeTransaction: StoreTransaction, customerInfo: CustomerInfo) {
                    isPurchasing = false
                    handleEntitlementResult(customerInfo)
                }

                override fun onError(error: PurchasesError, userCancelled: Boolean) {
                    isPurchasing = false
                }
            }
        )
    }

    val onRestoreClick: () -> Unit = {
        isRestoring = true
        Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                isRestoring = false
                handleEntitlementResult(customerInfo)
            }

            override fun onError(error: PurchasesError) {
                isRestoring = false
            }
        })
    }

    // Consumes the system back gesture/button while a transaction is in flight — the overlay
    // below blocks touches to the on-screen close/ignore/restore controls, but the hardware
    // back button is a separate input channel a touch-blocking overlay alone doesn't stop.
    BackHandler(enabled = isPurchasing || isRestoring) {}

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        when {
            isLoading -> {
                CircularProgressIndicator(color = navyBlue, modifier = Modifier.align(Alignment.Center))
            }
            // The offering may not exist yet, or the underlying store product may not be
            // fetchable (e.g. the iOS product is still awaiting App Store review) — close
            // rather than show a broken paywall with no package to sell.
            loadFailed || selectedPackage == null -> {
                LaunchedEffect(Unit) { onClose() }
            }
            else -> {
                AnnualDiscount35PaywallContent(
                    pkg = selectedPackage!!,
                    standardAnnualPrice = standardAnnualPrice,
                    isTablet = isTablet,
                    isPurchasing = isPurchasing,
                    isRestoring = isRestoring,
                    showIgnore = showIgnore,
                    onClose = onClose,
                    onPurchaseClick = onPurchaseClick,
                    onRestoreClick = onRestoreClick,
                )
            }
        }

        // Blocking overlay while a purchase or restore is in flight — on top of everything
        // above, so it visually blocks every button underneath (ignore offer/Restore/Claim
        // Offer) until the RevenueCat completion callback fires, success, failure, or
        // cancellation alike.
        if (isPurchasing || isRestoring) {
            PurchaseProcessingOverlay()
        }
    }
}

@Composable
private fun AnnualDiscount35PaywallContent(
    pkg: Package,
    standardAnnualPrice: String?,
    isTablet: Boolean,
    isPurchasing: Boolean,
    isRestoring: Boolean,
    showIgnore: Boolean,
    onClose: () -> Unit,
    onPurchaseClick: () -> Unit,
    onRestoreClick: () -> Unit
) {
    val scale = if (isTablet) 1.3f else 1f
    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )

    val product = pkg.product
    val priceFormatted = product.price.formatted
    val monthlyEquivalent = run {
        val amount = product.price.amountMicros.toDouble() / 1_000_000.0 / 12.0
        val formatter = NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance(product.price.currencyCode)
        }
        formatter.format(amount)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showIgnore) {
                Text(
                    text = "ignore offer",
                    color = Color.Black.copy(alpha = 0.6f),
                    fontFamily = alifbaFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp * scale,
                    modifier = Modifier.clickable(enabled = !isPurchasing && !isRestoring, onClick = onClose)
                )
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }
            Text(
                text = if (isRestoring) "Restoring..." else "Restore",
                color = Color.Black.copy(alpha = 0.6f),
                fontFamily = alifbaFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp * scale,
                modifier = Modifier.clickable(enabled = !isPurchasing && !isRestoring, onClick = onRestoreClick)
            )
        }

        Spacer(modifier = Modifier.height(32.dp * scale))

        Image(
            painter = painterResource(id = R.drawable.limitedoffer),
            contentDescription = "Special Offer",
            modifier = Modifier.size(120.dp * scale)
        )

        Spacer(modifier = Modifier.height(12.dp * scale))

        Text(
            text = "35% OFF",
            fontFamily = alifbaFont,
            fontWeight = FontWeight.Bold,
            fontSize = 44.sp * scale,
            color = Color(0xFFf29c1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Annual Plan — just for you",
            fontFamily = alifbaFont,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp * scale,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(28.dp * scale))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                if (standardAnnualPrice != null) {
                    Text(
                        text = standardAnnualPrice,
                        fontFamily = alifbaFont,
                        fontSize = 18.sp * scale,
                        color = Color.Black.copy(alpha = 0.4f),
                        textDecoration = TextDecoration.LineThrough,
                        modifier = Modifier.padding(end = 8.dp * scale)
                    )
                }
                Text(
                    text = "$priceFormatted/year",
                    fontFamily = alifbaFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp * scale,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Just $monthlyEquivalent/mo",
                fontFamily = alifbaFont,
                fontSize = 15.sp * scale,
                color = Color.Black.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(if (isTablet) 40.dp else 28.dp))

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Text(
                text = "Instant Access, No Trial",
                fontFamily = alifbaFont,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp * scale,
                color = lightCandyGreen,
                modifier = Modifier
                    .padding(bottom = 24.dp * scale)
                    .align(Alignment.CenterHorizontally)
            )
            AnnualDiscountFeatureRow(
                marker = AnnualDiscountFeatureMarker.CHECK,
                title = "Today",
                subtitle = "Unlock full access to Alifba Premium right away",
                isLast = false,
                fontFamily = alifbaFont,
                scale = scale
            )
            AnnualDiscountFeatureRow(
                marker = AnnualDiscountFeatureMarker.SPARKLE,
                title = "Every Day",
                subtitle = "New lessons, stories, and activities all year",
                isLast = false,
                fontFamily = alifbaFont,
                scale = scale
            )
            AnnualDiscountFeatureRow(
                marker = AnnualDiscountFeatureMarker.CROWN,
                title = "Every Year",
                subtitle = "Renews automatically at this price — cancel anytime",
                isLast = true,
                fontFamily = alifbaFont,
                scale = scale
            )
        }

        Spacer(modifier = Modifier.height(if (isTablet) 24.dp else 16.dp))

        AnnualDiscountTestimonialCarousel(fontFamily = alifbaFont, scale = scale)

        Spacer(modifier = Modifier.height(if (isTablet) 32.dp else 20.dp))

        RainbowClaimOfferButton(
            text = if (isPurchasing) "Processing..." else "Claim Offer",
            onClick = onPurchaseClick,
            enabled = !isPurchasing,
            fontFamily = alifbaFont,
            height = 56.dp * scale,
            fontSize = 18.sp * scale,
            cornerRadius = 16.dp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Secured by Google Play. Cancel anytime.",
            fontFamily = alifbaFont,
            fontSize = 12.sp * scale,
            color = Color.Black.copy(alpha = 0.4f)
        )

        Spacer(modifier = Modifier.height(if (isTablet) 40.dp else 20.dp))
    }
}

private enum class AnnualDiscountFeatureMarker { CHECK, SPARKLE, CROWN }

@Composable
private fun AnnualDiscountFeatureRow(
    marker: AnnualDiscountFeatureMarker,
    title: String,
    subtitle: String,
    isLast: Boolean,
    fontFamily: FontFamily,
    scale: Float
) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(24.dp * scale)
                    .background(Color.White, CircleShape)
                    .border(1.dp, Color.Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                when (marker) {
                    AnnualDiscountFeatureMarker.CHECK -> Icon(
                        Icons.Filled.Check, contentDescription = null, tint = Color.Black,
                        modifier = Modifier.size(14.dp * scale)
                    )
                    AnnualDiscountFeatureMarker.SPARKLE -> Icon(
                        Icons.Filled.Star, contentDescription = null, tint = Color.Black,
                        modifier = Modifier.size(14.dp * scale)
                    )
                    AnnualDiscountFeatureMarker.CROWN -> Image(
                        painter = painterResource(id = R.drawable.crown), contentDescription = null,
                        modifier = Modifier.size(14.dp * scale)
                    )
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.dp * scale)
                        .fillMaxHeight()
                        .background(Color.Black.copy(alpha = 0.15f))
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp * scale))
        Column(modifier = Modifier.padding(bottom = 24.dp * scale)) {
            Text(text = title, fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp * scale, color = Color.Black)
            Text(text = subtitle, fontFamily = fontFamily, fontSize = 14.sp * scale, color = Color.Black.copy(alpha = 0.6f))
        }
    }
}

// Same copy and visual language as RevenueCatPaywall's/DiscountPaywall's testimonial
// carousel — kept as an independent copy here (not shared code) per this file's separation
// from the other paywalls, but using the same already-approved review content.
private val annualDiscountTestimonials = listOf(
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
private fun AnnualDiscountTestimonialCarousel(fontFamily: FontFamily, scale: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp * scale)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp * scale)
    ) {
        annualDiscountTestimonials.forEach { (title, name, quote) ->
            AnnualDiscountTestimonialCard(title = title, name = name, quote = quote, fontFamily = fontFamily, scale = scale)
        }
    }
}

@Composable
private fun AnnualDiscountTestimonialCard(title: String, name: String, quote: String, fontFamily: FontFamily, scale: Float) {
    Column(
        modifier = Modifier
            .width(240.dp * scale)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp * scale, vertical = 6.dp * scale),
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
                    modifier = Modifier.size(12.dp * scale)
                )
                Spacer(modifier = Modifier.width(2.dp * scale))
            }
        }

        Spacer(modifier = Modifier.height(4.dp * scale))

        Text(
            text = title,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp * scale,
            color = darkBlue,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp * scale))
        Text(
            text = quote,
            fontFamily = fontFamily,
            fontSize = 11.sp * scale,
            color = darkBlue.copy(alpha = 0.75f),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(3.dp * scale))
        Text(
            text = name,
            fontFamily = fontFamily,
            fontSize = 10.sp * scale,
            color = darkBlue.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
    }
}

// Independent copy of the same blocking overlay used in DiscountPaywall.kt and
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
