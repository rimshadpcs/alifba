package com.alifba.alifba.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.alifba.alifba.features.authentication.DataStoreManager
import com.alifba.alifba.features.authentication.domain.AuthRepository
import com.alifba.alifba.features.authentication.domain.AuthRepositoryImpl
import com.alifba.alifba.features.authentication.usecase.SignInUseCase
import com.alifba.alifba.features.authentication.usecase.SignInWithGoogleUseCase
import com.alifba.alifba.features.authentication.usecase.SignUpUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    fun provideAuthRepository(firebaseAuth: FirebaseAuth): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth)
    }

    @Provides
    fun provideSignUpUseCase(repository: AuthRepository): SignUpUseCase {
        return SignUpUseCase(repository)
    }

    @Provides
    fun provideSignInUseCase(repository: AuthRepository): SignInUseCase {
        return SignInUseCase(repository)
    }
    @Provides
    fun provideSignInWithGoogleUseCase(repository: AuthRepository): SignInWithGoogleUseCase {
        return SignInWithGoogleUseCase(repository)
    }
//
}
