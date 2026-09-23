package com.alifba.alifba.data.gifts

import android.util.Log
import com.alifba.alifba.features.authentication.DataStoreManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GiftCodeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val dataStoreManager: DataStoreManager
) {

    private val client = OkHttpClient()

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val requestAdapter = moshi.adapter(RedeemGiftRequest::class.java)
    private val responseAdapter = moshi.adapter(RedeemGiftResponse::class.java)

    companion object {
        private const val TAG = "GiftCodeRepository"
        private const val BACKEND_BASE_URL =
            "https://alifba-backend-301870169287.europe-west1.run.app"
    }

    suspend fun redeemGiftCode(code: String): RedeemResult {
        val currentUser = FirebaseAuth.getInstance().currentUser
            ?: return RedeemResult.NotAuthenticated
        val userId = currentUser.uid

        val url = "$BACKEND_BASE_URL/users/$userId/gifts/redeem"

        return try {
            val jsonBody = requestAdapter.toJson(RedeemGiftRequest(code = code))
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = jsonBody.toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            val responseBody = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val parsed = responseAdapter.fromJson(responseBody)
                if (parsed == null || parsed.status != "ok") {
                    Log.e(TAG, "Invalid redeem response: $responseBody")
                    return RedeemResult.ServerError
                }

                // Backend has already updated Firestore; read the user document.
                val userDoc = firestore
                    .collection("users")
                    .document(userId)
                    .get()
                    .await()

                val premiumStatus: String? = userDoc.getString("premiumStatus")
                val premiumExpiresAtMillis: Long? = userDoc.getLong("premiumExpiresAt")
                val premiumExpiresDate: Date? = premiumExpiresAtMillis?.let { Date(it) }

                val isPremium = premiumStatus == "LIFETIME" || premiumStatus == "TEMPORARY"
                dataStoreManager.setPremium(isPremium)

                RedeemResult.Success(
                    planType = parsed.planType,
                    premiumStatus = premiumStatus,
                    premiumExpiresAt = premiumExpiresDate
                )
            } else {
                val statusCode = response.code
                val errorText = responseBody

                when {
                    statusCode == 404 ||
                        (statusCode == 400 &&
                            errorText.contains("Invalid code", ignoreCase = true)) ->
                        RedeemResult.InvalidCode

                    statusCode == 400 &&
                        errorText.contains("Code has expired", ignoreCase = true) ->
                        RedeemResult.Expired

                    statusCode == 400 &&
                        errorText.contains(
                            "Gift cannot be redeemed in status",
                            ignoreCase = true
                        ) ->
                        RedeemResult.AlreadyRedeemed

                    statusCode >= 500 -> RedeemResult.ServerError

                    else -> RedeemResult.ServerError
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error redeeming gift code", e)
            RedeemResult.NetworkError
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error redeeming gift code", e)
            RedeemResult.UnknownError(e.message ?: "Unknown error")
        }
    }
}

