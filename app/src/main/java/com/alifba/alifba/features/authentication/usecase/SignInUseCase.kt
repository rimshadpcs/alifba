package com.alifba.alifba.features.authentication.usecase

import com.alifba.alifba.features.authentication.domain.AuthRepository

class SignInUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Boolean> {
        return repository.signInWithEmail(email, password)
    }
}
