package com.alifba.alifba.features.authentication.usecase

import com.alifba.alifba.features.authentication.domain.AuthRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<Boolean> {
        return authRepository.signInWithGoogle(idToken)
    }
}
