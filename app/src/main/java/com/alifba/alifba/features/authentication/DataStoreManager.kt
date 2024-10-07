package com.alifba.alifba.features.authentication

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class DataStoreManager(
    private val dataStore: DataStore<Preferences>,
    private val coroutineScope: CoroutineScope
) {

    private object PreferencesKeys {
        val USER_ID = stringPreferencesKey("user_id")
        val EMAIL = stringPreferencesKey("email")
        val PASSWORD = stringPreferencesKey("password")
        val USER_PROFILE_EXISTS = booleanPreferencesKey("user_profile_exists")
    }

    // Declare properties before the init block
    val userId: StateFlow<String?> = dataStore.data
        .map { preferences ->
            val id = preferences[PreferencesKeys.USER_ID]
            if (id.isNullOrEmpty()) null else id
        }
        .stateIn(coroutineScope, SharingStarted.Eagerly, null)

    val email: StateFlow<String?> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.EMAIL] }
        .stateIn(coroutineScope, SharingStarted.Eagerly, null)

    val password: StateFlow<String?> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.PASSWORD] }
        .stateIn(coroutineScope, SharingStarted.Eagerly, null)

    init {
        coroutineScope.launch {
            userId.collect { id ->
                Log.d("DataStoreManager", "Collected userId: $id")
            }
        }
    }

    suspend fun saveUserDetails(email: String, password: String, userId: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.EMAIL] = email
            preferences[PreferencesKeys.PASSWORD] = password
            preferences[PreferencesKeys.USER_ID] = userId
        }
        Log.d("DataStoreManager", "Saved email: $email, password: $password, userId: $userId")
    }

    suspend fun saveUserProfileExists(exists: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_PROFILE_EXISTS] = exists
        }
    }

    suspend fun saveUserId(userId: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = userId
        }
    }
}
