package com.alifba.alifba.presenation.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alifba.alifba.data.gifts.GiftCodeRepository
import com.alifba.alifba.data.gifts.PlanType
import com.alifba.alifba.data.gifts.RedeemResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DateFormat
import javax.inject.Inject

data class RedeemUiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val planDescription: String? = null
)

@HiltViewModel
class RedeemGiftViewModel @Inject constructor(
    private val giftCodeRepository: GiftCodeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RedeemUiState())
    val uiState: StateFlow<RedeemUiState> = _uiState

    fun redeem(code: String) {
        if (code.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    successMessage = null,
                    errorMessage = "Please enter a code."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = RedeemUiState(isLoading = true)

            val result = giftCodeRepository.redeemGiftCode(code.trim())

            _uiState.value = when (result) {
                is RedeemResult.Success -> {
                    val planMessage = when (result.planType) {
                        PlanType.LIFETIME -> "Lifetime access unlocked."
                        PlanType.YEARLY -> "Premium unlocked for 1 year."
                    }

                    val expiresText = result.premiumExpiresAt?.let { date ->
                        " Premium until " + DateFormat.getDateInstance().format(date) + "."
                    }.orEmpty()

                    RedeemUiState(
                        isLoading = false,
                        successMessage = planMessage + expiresText,
                        errorMessage = null,
                        planDescription = result.premiumStatus
                    )
                }

                RedeemResult.InvalidCode -> RedeemUiState(
                    isLoading = false,
                    errorMessage = "That code doesn’t look right. Please check and try again."
                )

                RedeemResult.AlreadyRedeemed -> RedeemUiState(
                    isLoading = false,
                    errorMessage = "This gift code has already been used."
                )

                RedeemResult.Expired -> RedeemUiState(
                    isLoading = false,
                    errorMessage = "This gift code has expired."
                )

                RedeemResult.NotAuthenticated -> RedeemUiState(
                    isLoading = false,
                    errorMessage = "You need to be signed in to redeem a code."
                )

                RedeemResult.NetworkError,
                RedeemResult.ServerError,
                is RedeemResult.UnknownError -> RedeemUiState(
                    isLoading = false,
                    errorMessage = "Something went wrong. Please try again."
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}

