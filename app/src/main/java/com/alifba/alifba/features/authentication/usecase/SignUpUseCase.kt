package com.alifba.alifba.features.authentication.usecase

import com.alifba.alifba.features.authentication.domain.AuthRepository

class SignUpUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Boolean> {
        return repository.signUpWithEmail(email, password)
    }
}
