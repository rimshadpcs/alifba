package com.alifba.alifba.presenation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alifba.alifba.features.authentication.DataStoreManager
import com.alifba.alifba.service.DiscountNotificationScheduler
import com.onesignal.OneSignal
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    val isPremium: StateFlow<Boolean> = dataStoreManager.isPremium
    val discountExpiryTimestamp: StateFlow<Long> = dataStoreManager.discountExpiryTimestamp
    val hasSeenDownsellModal: StateFlow<Boolean> = dataStoreManager.hasSeenDownsellModal
    val standardPaywallSkipCount: StateFlow<Int> = dataStoreManager.standardPaywallSkipCount

    // Shared across every paywall screen (RevenueCatPaywall, DiscountPaywall) so a purchase
    // started on one screen blocks a purchase from starting on a different screen while the
    // first is still resolving — each paywall's own local `isPurchasing` only guards against
    // double-taps on itself, not against two independently-instantiated paywalls.
    private val _isPurchaseInFlight = MutableStateFlow(false)
    val isPurchaseInFlight: StateFlow<Boolean> = _isPurchaseInFlight

    fun tryBeginPurchase(): Boolean {
        if (_isPurchaseInFlight.value) return false
        _isPurchaseInFlight.value = true
        return true
    }

    fun endPurchase() {
        _isPurchaseInFlight.value = false
    }

    // This is the one place "the user is now premium" gets recorded no matter which paywall
    // or restore path got them there (RevenueCatPaywall, DiscountPaywall,
    // AnnualDiscount35Paywall, restore purchases), so it's also the right single place to
    // cancel any still-pending signup discount notifications.
    fun setPremium(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setPremium(enabled)
        }
        // Lets a future OneSignal Journey skip users who've already converted, the same way
        // the local discount-notification sequence already self-cancels below.
        OneSignal.User.addTag("is_premium", enabled.toString())
        if (enabled) {
            DiscountNotificationScheduler.cancelAll(appContext)
        }
    }

    fun setDiscountExpiry(timestamp: Long) {
        viewModelScope.launch {
            dataStoreManager.setDiscountExpiryTimestamp(timestamp)
        }
    }

    fun setHasSeenDownsellModal(seen: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setHasSeenDownsellModal(seen)
        }
    }

    fun incrementStandardSkipCount() {
        viewModelScope.launch {
            dataStoreManager.incrementStandardSkipCount()
        }
    }

    fun togglePremium() {
        viewModelScope.launch {
            val current = isPremium.value
            dataStoreManager.setPremium(!current)
        }
    }
}

