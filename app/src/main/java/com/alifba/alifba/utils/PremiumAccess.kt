package com.alifba.alifba.utils

import com.revenuecat.purchases.CustomerInfo

/**
 * The one premium rule on Android, matching iOS's PremiumManager.hasPremiumAccess: an active
 * RevenueCat entitlement. Every store product is attached to "Alifba Pro"; "premium" is the older
 * iOS entitlement, accepted so the same account reads the same on both platforms. Expired
 * subscriptions and gifts aren't active, so they never count.
 */
object PremiumAccess {
    private val entitlementIds = setOf("Alifba Pro", "premium")

    fun isPremium(customerInfo: CustomerInfo): Boolean =
        customerInfo.entitlements.active.keys.any { it in entitlementIds }
}
