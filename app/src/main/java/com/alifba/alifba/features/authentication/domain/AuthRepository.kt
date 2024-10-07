package com.alifba.alifba.features.authentication.domain

import com.alifba.alifba.presenation.Login.AuthState

interface AuthRepository {
    suspend fun signUpWithEmail(email: String, password: String): Result<Boolean>
    suspend fun signInWithEmail(email: String, password: String): Result<Boolean>
    suspend fun signOut(): Result<Boolean>

    suspend fun signInWithGoogle(idToken: String):Result<Boolean>

    suspend fun signInWithApple(appleToken:String): AuthState
}