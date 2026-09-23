package com.alifba.alifba.data.gifts

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RedeemGiftRequest(
    @Json(name = "code") val code: String
)

enum class PlanType {
    LIFETIME,
    YEARLY
}

@JsonClass(generateAdapter = true)
data class RedeemGiftResponse(
    @Json(name = "status") val status: String,
    @Json(name = "planType") val planType: PlanType
)

