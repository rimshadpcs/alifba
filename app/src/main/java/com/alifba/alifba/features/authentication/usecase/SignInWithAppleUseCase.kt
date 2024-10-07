package com.alifba.alifba.features.authentication.usecase

import com.alifba.alifba.features.authentication.domain.AuthRepository
import com.alifba.alifba.presenation.Login.AuthState
import javax.inject.Inject

class SignInWithAppleUseCase @Inject constructor(
    private val authRepository: AuthRepository
){
    suspend operator fun invoke(appleToken:String):AuthState{
        return authRepository.signInWithApple(appleToken)
    }
}