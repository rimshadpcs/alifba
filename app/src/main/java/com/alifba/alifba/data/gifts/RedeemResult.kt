package com.alifba.alifba.data.gifts

import java.util.Date

sealed class RedeemResult {
    data class Success(
        val planType: PlanType,
        val premiumStatus: String?,
        val premiumExpiresAt: Date?
    ) : RedeemResult()

    object InvalidCode : RedeemResult()
    object AlreadyRedeemed : RedeemResult()
    object Expired : RedeemResult()
    object NotAuthenticated : RedeemResult()
    object NetworkError : RedeemResult()
    object ServerError : RedeemResult()
    data class UnknownError(val message: String) : RedeemResult()
}

