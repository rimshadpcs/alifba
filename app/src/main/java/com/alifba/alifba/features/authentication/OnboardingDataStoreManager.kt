package com.alifba.alifba.features.authentication

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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

    // Child's name, captured on the very first onboarding screen — read once by Create Profile
    // to pre-fill its own child name field. Persisted (not passed as a nav argument) because the
    // route to Create Profile for a brand-new user goes through the signup flow first, which a
    // plain nav-arg wouldn't survive; this mirrors hasCompletedOnboarding's own write-during-
    // onboarding/read-after-signup pattern exactly.
    val onboardingChildName: Flow<String?> = dataStore.data
        .map { preferences -> preferences[stringPreferencesKey("onboarding_child_name")] }

    suspend fun setOnboardingChildName(name: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("onboarding_child_name")] = name
        }
    }
}