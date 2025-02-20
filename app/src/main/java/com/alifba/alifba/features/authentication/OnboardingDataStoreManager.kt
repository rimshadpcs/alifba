package com.alifba.alifba.features.authentication

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore by preferencesDataStore(name = "user_preferences")

class OnboardingDataStoreManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore = context.dataStore

    val hasCompletedOnboarding: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey("has_completed_onboarding")] ?: false
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("has_completed_onboarding")] = completed
        }
    }
}